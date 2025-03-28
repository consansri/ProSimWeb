package cengine.lang.asm.features

import cengine.editor.completion.Completion
import cengine.editor.completion.CompletionItemKind
import cengine.editor.completion.CompletionProvider
import cengine.editor.completion.CompletionProvider.Companion.asCompletions
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.impl.ASDirType
import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.asm.ast.impl.AsmFile
import cengine.lang.asm.ast.lexer.AsmTokenType
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiElementVisitor
import cengine.psi.core.PsiFile

class AsmCompleter(targetSpec: TargetSpec<*>) : CompletionProvider {

    private val symbolRegex = targetSpec.prefices.symbol
    private val directives: Set<String> = targetSpec.allDirs.map { "." + it.getDetectionString().lowercase() }.filter { it.isNotEmpty() }.toSet()
    private val instructions: Set<String> = targetSpec.allInstrs.map { it.detectionName.lowercase() }.toSet()
    private val cachedCompletions: MutableMap<PsiFile, CompletionSet> = mutableMapOf()

    data class CompletionSet(
        val labels: Set<String>,
        val symbols: Set<String>,
        val macros: Set<String>,
    ) {
        fun asCompletions(prefix: String): List<Completion> = labels.asCompletions(prefix, false, CompletionItemKind.ENUM) + symbols.asCompletions(prefix, ignoreCase = false, CompletionItemKind.VARIABLE) + macros.asCompletions(prefix, ignoreCase = false, CompletionItemKind.FUNCTION)
    }

    override fun fetchCompletions(lineContentBefore: String, psiElement: PsiElement?, psiFile: PsiFile?): List<Completion> {
        val completionSet = cachedCompletions[psiFile]

        val prefix = lineContentBefore.takeLastWhile { symbolRegex.matches(it.toString()) }

        val directives = directives.asCompletions(prefix, true, CompletionItemKind.KEYWORD)
        val instructions = instructions.asCompletions(prefix, true, CompletionItemKind.KEYWORD)

        val completions = if (prefix.isNotEmpty()) (completionSet?.asCompletions(prefix) ?: emptyList()) + instructions + directives else emptyList()

        return completions
    }

    override fun buildCompletionSet(file: PsiFile) {
        if (file !is AsmFile) return

        val builder = CompletionSetBuilder()
        file.accept(builder)
        cachedCompletions.remove(file)
        cachedCompletions[file] = builder.getCompletions()
    }

    private class CompletionSetBuilder : PsiElementVisitor {
        val macros = mutableSetOf<String>()
        val labels = mutableSetOf<String>()
        val symbols = mutableSetOf<String>()

        fun getCompletions(): CompletionSet = CompletionSet(labels, symbols, macros)

        override fun visitFile(file: PsiFile) {
            if (file is AsmFile) {
                file.children.forEach {
                    it.accept(this)
                }
            }
        }

        override fun visitElement(element: PsiElement) {
            if (element !is ASNode) return
            when (element) {
                is ASNode.Label -> {
                    if (!labels.contains(element.identifier)) {
                        labels.add(element.identifier)
                    }
                }

                is ASNode.Program -> {
                    element.children.forEach {
                        it.accept(this)
                    }
                }

                is ASNode.Statement -> {
                    if (element.label != null) {
                        element.label.accept(this)
                    }

                    when(element){
                        is ASNode.Statement.Dir -> element.dir.accept(this)
                        is ASNode.Statement.Empty -> {}
                        is ASNode.Statement.Instr -> {}
                        is ASNode.Statement.Error -> {}
                    }
                }

                is ASNode.Directive -> {
                    when (element.type) {
                        ASDirType.MACRO -> {
                            val identifier = element.allTokens.firstOrNull { it.type == AsmTokenType.SYMBOL }
                            if (identifier != null) {
                                if (!macros.contains(identifier.value)) {
                                    macros.add(identifier.value)
                                }
                            }
                        }

                        ASDirType.SET, ASDirType.SET_ALT, ASDirType.EQU -> {
                            val identifier = element.allTokens.firstOrNull { it.type == AsmTokenType.SYMBOL }
                            if (identifier != null) {
                                symbols.add(identifier.value)
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

}