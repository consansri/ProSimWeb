package cengine.lang.asm.features

import cengine.editor.completion.Completion
import cengine.editor.completion.CompletionItemKind
import cengine.editor.completion.CompletionProvider
import cengine.editor.completion.CompletionProvider.Companion.asCompletions
import cengine.lang.asm.AsmSpec
import cengine.lang.asm.psi.AsmLabelDecl
import cengine.psi.core.PsiElement
import cengine.psi.elements.PsiFile
import cengine.psi.visitor.PsiElementVisitor

class AsmCompleter(spec: AsmSpec<*>) : CompletionProvider {

    private val directives: Set<String> = spec.dirTypes.map { it.keyWord }.toSet()
    private val instructions: Set<String> = spec.instrTypes.map { it.keyWord.lowercase() }.toSet()
    private val cachedCompletions: MutableMap<PsiFile, CompletionSet> = mutableMapOf()

    data class CompletionSet(
        val labels: Set<String>,
        val symbols: Set<String>,
        val macros: Set<String>,
    ) {
        fun asCompletions(prefix: String): List<Completion> = labels.asCompletions(prefix, false, CompletionItemKind.ENUM) + symbols.asCompletions(prefix, ignoreCase = false, CompletionItemKind.VARIABLE) + macros.asCompletions(prefix, ignoreCase = false, CompletionItemKind.FUNCTION)
    }

    override fun fetchCompletions(lineContentBefore: String, psiElement: PsiElement?, psiFile: PsiFile?): List<Completion> {
        val prefix = lineContentBefore.takeLastWhile { it.isLetterOrDigit() || it == '_' || it == '.' }

        val completionSet = cachedCompletions[psiFile]?.asCompletions(prefix) ?: emptyList()

        val directives = directives.asCompletions(prefix, true, CompletionItemKind.KEYWORD)
        val instructions = instructions.asCompletions(prefix, true, CompletionItemKind.KEYWORD)

        val completions = if (prefix.isNotEmpty()) completionSet + instructions + directives else emptyList()

        return completions
    }

    override fun buildCompletionSet(file: PsiFile) {
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
            file.children.forEach {
                it.accept(this)
            }
        }

        override fun visitElement(element: PsiElement) {
            when (element) {
                is AsmLabelDecl -> {
                    element.identifierToken?.value?.let {
                        labels.add(it)
                    }
                }

                else -> {}
            }
            element.children.forEach {
                it.accept(this)
            }
        }
    }
}