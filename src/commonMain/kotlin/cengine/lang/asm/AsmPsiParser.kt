package cengine.lang.asm

import cengine.editor.annotation.Annotation
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.ast.impl.ASNodeType
import cengine.lang.asm.ast.impl.AsmFile
import cengine.lang.asm.ast.lexer.AsmTokenType
import cengine.psi.PsiManager
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile
import cengine.psi.core.PsiParser
import cengine.vfs.VirtualFile
import debug.DebugTools
import nativeLog

class AsmPsiParser(private val spec: TargetSpec<*>, private val languageService: AsmLang) : PsiParser<AsmFile> {

    override suspend fun parse(file: VirtualFile, manager: PsiManager<*, *>): AsmFile {
        nativeLog("Parsing ${file.path} ...")

        val content = file.getAsUTF8String()

        val lexer = spec.createLexer(content)
        lexer.reset(content)

        val program = ASNode.buildNode(ASNodeType.PROGRAM, lexer, spec) as ASNode.Program

        program.accept(PsiParser.ParentLinker())

        val labelCollector = LabelCollector()
        program.accept(labelCollector)

        program.accept(LabelLinker(labelCollector.labels))

        //nativeLog("AsmPsiParser parses file: $fileName!")

        val generator = spec.createGenerator(manager)
        generator.generate(program)

        val asmFile = AsmFile(file, manager, program)

        if (DebugTools.KIT_showPSITree) {
            nativeLog(asmFile.print(file.name))
        }

        return asmFile
    }


    fun reparseStatements(fromIndex: Int, toIndex: Int, asmFile: AsmFile): List<ASNode.Statement> {
        val lexer = spec.createLexer("")
        val content = asmFile.file.getAsUTF8String().substring(fromIndex, toIndex)
        lexer.reset(content)
        val program = ASNode.buildNode(ASNodeType.PROGRAM, lexer, spec) as ASNode.Program
        return program.getAllStatements()
    }

    private class LabelLinker(val labels: Set<ASNode.Label>) : PsiElementVisitor {
        override fun visitFile(file: PsiFile) {
            if (file !is AsmFile) return
            file.children.forEach {
                it.accept(this)
            }
        }

        override fun visitElement(element: PsiElement) {
            if (element !is ASNode) return

            when (element) {
                is ASNode.NumericExpr.Operand.Identifier -> {
                    val identifier = element.symToken.value.take(element.symToken.value.length - 1)
                    if (element.symToken.type == AsmTokenType.L_LABEL_REF) {
                        val reference = when {
                            element.symToken.value.endsWith("f", true) -> {
                                labels.firstOrNull { it.type == ASNode.Label.Type.NUMERIC && it.range.first > element.range.last && it.identifier == identifier }
                            }

                            element.symToken.value.endsWith("b", true) -> {
                                labels.lastOrNull { it.type == ASNode.Label.Type.NUMERIC && it.range.last < element.range.first && it.identifier == identifier }
                            }

                            else -> null
                        }
                        element.referencedElement = reference
                    } else {
                        val reference = labels.firstOrNull { it.identifier == element.symToken.value }
                        element.referencedElement = reference
                    }
                    return
                }

                is ASNode.StringExpr.Operand.Identifier -> {
                    val identifier = element.symbol.value.take(element.symbol.value.length - 1)
                    if (element.symbol.type == AsmTokenType.L_LABEL_REF) {
                        val reference = when {
                            element.symbol.value.endsWith("f", true) -> {
                                labels.firstOrNull { it.type == ASNode.Label.Type.NUMERIC && it.range.first > element.range.last && it.identifier == identifier }
                            }

                            element.symbol.value.endsWith("b", true) -> {
                                labels.lastOrNull { it.type == ASNode.Label.Type.NUMERIC && it.range.last < element.range.first && it.identifier == identifier }
                            }

                            else -> null
                        }
                        element.referencedElement = reference
                    } else {
                        val reference = labels.firstOrNull { it.identifier == element.symbol.value }
                        element.referencedElement = reference
                    }
                    return
                }

                else -> {}
            }

            element.children.forEach {
                it.accept(this)
            }
        }
    }

    private class LabelCollector : PsiElementVisitor {
        val labels: MutableSet<ASNode.Label> = mutableSetOf()

        override fun visitFile(file: PsiFile) {
            if (file !is AsmFile) return
            file.children.forEach {
                it.accept(this)
            }
        }

        override fun visitElement(element: PsiElement) {
            if (element !is ASNode) return

            when (element) {
                is ASNode.Label -> {
                    if (labels.firstOrNull { it.type != ASNode.Label.Type.NUMERIC && it.identifier == element.identifier } != null) {
                        element.annotations.add(Annotation.error(element, "Label is already defined!"))
                        return
                    }

                    labels.add(element)
                    return
                }

                is ASNode.Program -> {}
                is ASNode.Statement.Dir -> {}
                is ASNode.Statement.Empty -> {}
                is ASNode.Statement.Instr -> {}
                is ASNode.Statement.Error -> {}
                else -> return
            }

            element.children.forEach {
                it.accept(this)
            }
        }
    }


}