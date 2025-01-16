package cengine.psi.core

import cengine.editor.annotation.Annotation
import cengine.lang.asm.CodeStyle
import cengine.psi.feature.Highlightable
import cengine.util.integer.IntNumber.Companion.overlaps

import cengine.vfs.VirtualFile

/**
 * Service for managing PSI-related operations
 */
object PsiService {

    fun collectHighlights(file: PsiFile, inRange: IntRange): List<Pair<IntRange, CodeStyle>> {
        class HighlightRangeCollector : PsiElementVisitor {
            val highlights = mutableListOf<Pair<IntRange, CodeStyle>>()
            override fun visitFile(file: PsiFile) {
                if (!file.range.overlaps(inRange)) return
                file.children.forEach {
                    it.accept(this)
                }
            }

            override fun visitElement(element: PsiElement) {
                if (!element.range.overlaps(inRange)) return
                if (element is Highlightable) {
                    element.style?.let {
                        highlights.add(element.range to it)
                    }
                }

                element.children.forEach {
                    it.accept(this)
                }
            }
        }

        val collector = HighlightRangeCollector()
        file.accept(collector)
        return collector.highlights
    }

    fun collectNotations(file: PsiFile, inRange: IntRange? = null): Set<Annotation> {
        val all = mutableListOf<Annotation>()
        val collector = NotationRangeCollector(inRange){ element, annotations ->
            all.addAll(annotations)
        }
        file.accept(collector)
        return all.toSet()
    }

    fun annotationsMapped(file: PsiFile, inRange: IntRange? = null): Map<PsiElement, List<Annotation>> {
        val map = mutableMapOf<PsiElement, List<Annotation>>()
        val collector = NotationRangeCollector(inRange){ element, annotations ->
            map[element] = annotations
        }
        file.accept(collector)
        return map
    }

    fun findElementAt(file: PsiFile, offset: Int): PsiElement? {
        class Finder(private val targetOffset: Int) : PsiElementVisitor {
            var result: PsiElement? = null

            override fun visitFile(file: PsiFile) {
                file.children.forEach {
                    it.accept(this)
                }
            }

            override fun visitElement(element: PsiElement) {
                if (element.range.first <= targetOffset && targetOffset <= element.range.last) {
                    result = element
                    element.children.forEach { it.accept(this) }
                }
            }
        }

        val finder = Finder(offset)
        file.accept(finder)
        return finder.result
    }

    /**
     * Searches for references to [element]
     */
    fun findReferences(element: PsiElement): List<PsiReference> {
        // This is a simplistic implementation. In a real-world scenario, you'd need a more sophisticated approach to find references.
        val references = mutableListOf<PsiReference>()
        val possibleRoots = generateSequence(element) { it.parent }

        class ReferenceFinder : PsiElementVisitor {
            override fun visitFile(file: PsiFile) {
                file.children.forEach {
                    it.accept(this)
                }
            }

            override fun visitElement(element: PsiElement) {
                if (element is PsiReference && possibleRoots.any { root -> element.isReferenceTo(root) }) {
                    references.add(element)
                }

                element.children.forEach { it.accept(this) }
            }
        }

        val root = generateSequence(element) { it.parent }.last()
        root.accept(ReferenceFinder())
        return references
    }

    fun path(of: PsiElement): List<PsiElement> {

        val path = mutableListOf(of)

        var currElement = of

        while (true) {
            val parent = currElement.parent

            if (parent != null) {
                path.add(0, parent)
                currElement = parent
            } else {
                break
            }
        }

        return path
    }

    private class NotationRangeCollector(val inRange: IntRange? = null, val found: (PsiElement, List<Annotation>) -> Unit) : PsiElementVisitor {
        override fun visitFile(file: PsiFile) {
            if (inRange != null && !file.range.overlaps(inRange)) return

            if (file.annotations.isNotEmpty()) {
                found(file, file.annotations)
            }

            file.children.forEach {
                it.accept(this)
            }
        }

        override fun visitElement(element: PsiElement) {
            if (inRange != null && !element.range.overlaps(inRange)) return

            if (element.annotations.isNotEmpty()) {
                found(element, element.annotations)
            }

            element.children.forEach {
                it.accept(this)
            }
        }
    }
}