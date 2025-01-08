package cengine.psi.core

import cengine.editor.annotation.Annotation

/**
 * Base Element for all Elements in a PSI tree.
 */
interface PsiElement : Interval {

    val pathName: String
    var parent: PsiElement?
    val children: List<PsiElement>
    val annotations: List<Annotation>
    val additionalInfo: String

    override var range: IntRange

    fun print(prefix: String): String = "$prefix${this::class.simpleName}: $additionalInfo\n" + ArrayList(children).joinToString("\n") { it.print(prefix + "\t") }

    fun inserted(index: Int, length: Int) {
        range = IntRange(range.first, range.last + length)
        val affectedChildren = children.filter { index <= it.range.last }
        affectedChildren.forEach { child ->
            when {
                index <= child.range.first -> {
                    child.move(length)
                }

                index in child.range -> {
                    child.inserted(index, length)
                }
            }
        }
    }

    fun deleted(start: Int, end: Int) {
        val length = end - start
        range = IntRange(range.first, range.last - length)
        val affectedChildren = children
        affectedChildren.forEach { child ->
            when {
                end <= child.range.first -> {
                    child.move(-length)
                }

                start >= child.range.last -> {
                    // No change needed
                }

                else -> {
                    val childStart = start.coerceAtLeast(child.range.first)
                    val childEnd = end.coerceAtMost(child.range.last)
                    child.deleted(childStart, childEnd)
                }
            }
        }
    }

    fun move(offset: Int) {
        range = IntRange(range.first + offset, range.last + offset)
        children.forEach {
            it.move(offset)
        }
    }

    fun getFile(): PsiFile? {
        return if (this is PsiFile) {
            this
        } else {
            parent?.getFile()
        }
    }

    fun traverseUp(visitor: PsiElementVisitor) {
        //nativeLog("${visitor::class.simpleName} at ${this::class.simpleName}")
        visitor.visitElement(this)
        parent?.traverseUp(visitor)
    }

    fun traverseDown(visitor: PsiElementVisitor) {
        //nativeLog("${visitor::class.simpleName} at ${this::class.simpleName}")
        visitor.visitElement(this)
        children.forEach {
            it.traverseDown(visitor)
        }
    }

    fun accept(visitor: PsiElementVisitor)
}