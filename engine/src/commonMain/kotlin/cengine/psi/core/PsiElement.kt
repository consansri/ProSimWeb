package cengine.psi.core

import cengine.editor.annotation.Annotation
import cengine.psi.elements.PsiFile
import cengine.psi.visitor.PsiElementVisitor

/**
 * Base Element for all Elements in a PSI tree.
 */
abstract class PsiElement(open val type: PsiElementType, vararg val children: PsiElement) : Interval {

    abstract override var range: IntRange

    open val additionalInfo: String
        get() = ""

    var parent: PsiElement? = null
        private set

    open val annotations:  MutableList<Annotation> = mutableListOf()

    val childrenRecursive: Sequence<PsiElement>
        get() = sequence {
            yieldAll(children.asSequence())
            children.forEach { yieldAll(it.childrenRecursive) }
        }

    init {
        children.forEach {
            it.parent = this
        }
    }



    open fun print(prefix: String): String = "$prefix${this::class.simpleName}: $additionalInfo\n" + children.joinToString("\n") { it.print("$prefix    ") }

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
        //cengine.console.SysOut.log("${visitor::class.simpleName} at ${this::class.simpleName}")
        visitor.visitElement(this)
        parent?.traverseUp(visitor)
    }

    fun traverseDown(visitor: PsiElementVisitor) {
        //cengine.console.SysOut.log("${visitor::class.simpleName} at ${this::class.simpleName}")
        visitor.visitElement(this)
        children.forEach {
            it.traverseDown(visitor)
        }
    }

    open fun accept(visitor: PsiElementVisitor) {
        visitor.visitElement(this)
    }

    fun addError(message: String) {
        annotations.add(Annotation.error(this, message))
    }

    fun addWarn(message: String) {
        annotations.add(Annotation.warn(this, message))
    }

    fun addInfo(message: String) {
        annotations.add(Annotation.info(this, message))
    }
}