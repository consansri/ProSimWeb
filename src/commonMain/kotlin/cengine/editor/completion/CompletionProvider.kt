package cengine.editor.completion

import cengine.psi.core.PsiElement
import cengine.psi.core.PsiFile

/**
 * The CompletionProvider interface is responsible for providing code completions.
 * It fetches possible completions based on the content before the cursor and the current state of the PsiElement and PsiFile.
 */
interface CompletionProvider {

    /**
     * Fetches a list of possible completions based on the current line content before the cursor,
     * the current PsiElement, and the PsiFile.
     *
     * @param lineContentBefore The content of the line before the cursor position.
     * @param psiElement The current PsiElement at the cursor.
     * @param psiFile The current PsiFile being edited.
     * @return A list of Completion objects representing possible completions.
     */
    fun fetchCompletions(lineContentBefore: String, psiElement: PsiElement?, psiFile: PsiFile?): List<Completion>

    /**
     * Builds the completion set for a given PsiFile.
     *
     * @param file The PsiFile for which the completion set is to be built.
     */
    fun buildCompletionSet(file: PsiFile)

    companion object {
        /**
         * Converts a collection of strings into a list of Completion objects, filtering by a given prefix.
         *
         * @param prefix The prefix to filter the collection of strings.
         * @param ignoreCase Whether the prefix matching should ignore case.
         * @param kind The kind of completion item to associate with each completion.
         * @return A list of Completion objects for the given prefix.
         */
        fun Collection<String>.asCompletions(prefix: String, ignoreCase: Boolean, kind: CompletionItemKind?): List<Completion> {
            return this
                .filter { it.startsWith(prefix, ignoreCase) && prefix.length != it.length }
                .map { keyword ->
                    Completion(
                        keyword,
                        keyword.substring(prefix.length),
                        kind
                    )
                }
        }
    }
}

