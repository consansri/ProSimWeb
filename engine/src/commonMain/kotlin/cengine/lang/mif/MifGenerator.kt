package cengine.lang.mif

import cengine.lang.asm.gas.AsmCodeGenerator
import cengine.lang.obj.elf.LinkerScript
import cengine.lang.obj.elf.Shdr
import cengine.util.buffer.Buffer
import cengine.util.integer.BigInt
import cengine.util.integer.BigInt.Companion.toBigInt
import cengine.util.integer.UInt32
import cengine.util.integer.UInt64
import cengine.util.integer.UnsignedFixedSizeIntNumberT
import com.ionspin.kotlin.bignum.integer.BigInteger

class MifGenerator<T : Buffer<*>>(linkerScript: LinkerScript, private val addrSize: UnsignedFixedSizeIntNumberT<*>, val bufferInit: () -> T) : AsmCodeGenerator<AsmCodeGenerator.Section>(linkerScript) {
    override val outputFileSuffix: String
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
        val builder = MifBuilder(text.content.type, addrSize, this::class.simpleName.toString())

        builder.setAddrRadix(MifRadix.HEX)
        builder.setDataRadix(MifRadix.HEX)

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


}