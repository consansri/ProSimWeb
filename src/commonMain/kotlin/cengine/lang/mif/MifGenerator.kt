package cengine.lang.mif

import cengine.lang.asm.AsmLang
import cengine.lang.asm.ast.AsmCodeGenerator
import cengine.lang.asm.ast.impl.AsmFile
import cengine.lang.obj.elf.LinkerScript
import cengine.lang.obj.elf.Shdr
import cengine.psi.PsiManager
import cengine.util.buffer.Buffer
import cengine.util.integer.BigInt
import cengine.util.integer.BigInt.Companion.toBigInt
import cengine.util.integer.IntNumberStatic
import cengine.util.integer.UInt32
import cengine.util.integer.UInt64
import com.ionspin.kotlin.bignum.integer.BigInteger

class MifGenerator<T : Buffer<*>>(linkerScript: LinkerScript, psiManager: PsiManager<*, *>,private val addrSize: IntNumberStatic<*>, val bufferInit: () -> T) : AsmCodeGenerator<AsmCodeGenerator.Section>(linkerScript, psiManager) {
    override val fileSuffix: String
        get() = ".mif"

    override val sections: MutableList<Section> = mutableListOf()

    private val text = getOrCreateSection(".text", Shdr.SHT_text, Shdr.SHF_text.toUInt64())

    override var currentSection: Section = text

    override fun orderSectionsAndResolveAddresses() {
        var globalAddress = 0.toBigInt()

        // .text
        var addr = linkerScript.textStart ?: globalAddress

        sections.filter { section ->
            section.isText()
        }.forEach { section ->
            section.address = addr
            addr += section.content.size.toBigInt()
        }

        globalAddress = addr

        // .data, .bss
        addr = linkerScript.dataStart ?: globalAddress

        sections.filter { section ->
            section.isData()
        }.forEach { section ->
            section.address = addr
            addr += section.content.size.toBigInt()
        }

        globalAddress = addr

        // .rodata
        addr = linkerScript.rodataStart ?: globalAddress

        sections.filter { section ->
            section.isRoData()
        }.forEach { section ->
            section.address = addr
            addr += section.content.size.toBigInt()
        }
    }

    override fun writeFile(): ByteArray {
        val builder = MifConverter(text.content.type, addrSize, this::class.simpleName.toString())

        builder.setAddrRadix(Radix.HEX)
        builder.setDataRadix(Radix.HEX)

        sections.filter {
            it.isProg()
        }.forEach { section ->
            val addr = section.address
            builder.addContent(addr, section.content.asList())
        }

        return builder.build().encodeToByteArray()
    }

    override fun createNewSection(name: String, type: UInt32, flags: UInt64, link: Section?, info: String?): Section {
        return object : Section {
            override val name: String = name
            override var type: UInt32 = type
            override var flags: UInt64 = flags
            override var link: Section? = link
            override var info: String? = info
            override var address: BigInt = BigInt(BigInteger.ZERO)
            override val content: Buffer<*> = bufferInit()
            override val reservations: MutableList<InstrReservation> = mutableListOf()
        }
    }

    enum class Radix(val radix: Int) {
        HEX(16),
        OCT(8),
        BIN(2),
        DEC(10);

        companion object {
            fun getRadix(string: String): Radix {
                return entries.firstOrNull { it.name == string.uppercase() } ?: HEX
            }
        }
    }
}