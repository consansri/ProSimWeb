package cengine.lang.mif

import cengine.console.SysOut
import cengine.lang.asm.AsmBinaryProvider
import cengine.lang.asm.AsmDisassembler
import cengine.lang.mif.psi.MifContentBlock
import cengine.lang.mif.semantic.MifData
import cengine.lang.mif.psi.MifDirective
import cengine.lang.mif.semantic.MifSemanticException
import cengine.psi.core.FileBuilderFn
import cengine.psi.core.PsiElement
import cengine.psi.core.PsiFileTypeDef
import cengine.psi.elements.PsiFile
import cengine.util.integer.*
import cengine.vfs.VirtualFile

class MifPsiFile(val data: MifData, file: VirtualFile, valid: Boolean, vararg children: PsiElement) : PsiFile(file, valid, MifPsiFile, *children), AsmBinaryProvider {

    companion object : PsiFileTypeDef {
        override val typeName: String = "MifPsiFile"
        override val builder: FileBuilderFn = FileBuilderFn@{ file, valid, children ->
            val directives = children.filterIsInstance<MifDirective>()
            val content = children.filterIsInstance<MifContentBlock>().firstOrNull()

            if (content == null) {
                return@FileBuilderFn PsiFile(file, false, PsiFile, *children).apply {
                    addError("Content block is missing!")
                }
            }

            try {
                val data = MifData.create(directives, content)
                MifPsiFile(data, file, valid, *children)
            } catch (e: MifSemanticException) {
                PsiFile(file, false, PsiFile, *children).apply {
                    if (e.element == null) {
                        addError(e.message ?: "Unknown error!")
                    }
                }
            }
        }
    }

    override val id: String = this.type.typeName

    override val addrType: UnsignedFixedSizeIntNumberT<*> = data.addrWidth
    override val wordType: FixedSizeIntNumberT<*> = data.width

    override fun entry(): UnsignedFixedSizeIntNumber<*> = data.addrWidth.ZERO

    override fun contents(): Map<UnsignedFixedSizeIntNumber<*>, Pair<List<FixedSizeIntNumber<*>>, List<AsmDisassembler.Label>>> {
        val contents = mutableMapOf<UnsignedFixedSizeIntNumber<*>, Pair<List<FixedSizeIntNumber<*>>, List<AsmDisassembler.Label>>>()

        try {
            data.memory.entries.forEach { (addr, values) ->
                contents[addr] = values to emptyList()
            }
        } catch (e: Exception) {
            SysOut.error(e.toString())
        }

        return contents
    }

}