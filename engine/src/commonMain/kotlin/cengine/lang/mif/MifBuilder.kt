package cengine.lang.mif

import cengine.console.SysOut
import cengine.lang.obj.elf.*
import cengine.util.integer.*
import cengine.util.integer.BigInt.Companion.toBigInt
import cengine.util.integer.Int8.Companion.toInt8
import com.ionspin.kotlin.bignum.integer.BigInteger
import emulator.kit.memory.Memory
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

class MifBuilder(private val depth: Double, private val wordSize: UnsignedFixedSizeIntNumberT<*>) {

    constructor(wordSize: UnsignedFixedSizeIntNumberT<*>, addrSize: UnsignedFixedSizeIntNumberT<*>, id: String) : this(2.0.pow(addrSize.BITS), wordSize)

    private val addrSize: UnsignedFixedSizeIntNumberT<*> = IntNumberUtils.nearestUType(log2(depth).roundToInt() / 8)
    private var addrRDX: MifRadix = MifRadix.HEX
    private var dataRDX: MifRadix = MifRadix.HEX

    // Represents the ranges as a list of triples: (start address, end address, data value)
    private val ranges: MutableList<Range> = mutableListOf()

    init {
        // Initially, all addresses are filled with 0
        ranges.add(Range(0.toBigInt(), BigInt(BigInteger.parseString("1".repeat(addrSize.BITS), 2)), listOf(BigInt.ZERO)))
        SysOut.log("MifBuidler: addrwidth=${addrSize.BITS}, dataWidth=${wordSize.BITS}")
    }

    fun build(): String = buildString {
        appendLine("DEPTH = ${2.0.pow(addrSize.BITS).toBigInt()}; -- The size of memory in words")
        appendLine("WIDTH = ${wordSize.BITS}; -- The size of data in bits")
        appendLine("ADDRESS_RADIX = ${addrRDX.name}; -- The radix for address values")
        appendLine("DATA_RADIX = ${dataRDX.name}; -- The radix for data values")
        appendLine("CONTENT BEGIN")

        ranges.forEach { range ->
            appendLine(range.build())
        }

        appendLine("END;")
    }

    fun addContent(startAddr: String, endAddr: String, data: List<String>): MifBuilder {
        return addContent(BigInt.parse(startAddr, addrRDX.base), BigInt.parse(endAddr, addrRDX.base), data.map { BigInt.parse(it, dataRDX.base) })
    }

    fun addContent(startAddr: BigInt, endAddr: BigInt, data: List<BigInt>): MifBuilder {
        // Find the range where the new content starts and modify accordingly
        val modifiedRanges = mutableListOf<Range>()

        ranges.forEach { range ->
            when {
                // Range is fully before the new content, keep it unchanged
                range.end < startAddr -> modifiedRanges.add(range)

                // Range is fully after the new content, keep it unchanged
                range.start > endAddr -> modifiedRanges.add(range)

                // The range overlaps with the new content
                else -> {
                    // Split the range into three parts: before, overlap, and after

                    // Part before the new content
                    if (range.start < startAddr) {
                        modifiedRanges.add(Range(range.start, startAddr.dec(), range.data))
                    }

                    // The new content replaces this part of the range
                    modifiedRanges.add(Range(startAddr, endAddr, data))

                    // Part after the new content
                    if (range.end > endAddr) {
                        modifiedRanges.add(Range(endAddr.inc(), range.end, range.data))
                    }
                }
            }
        }

        ranges.clear()
        ranges.addAll(modifiedRanges)
        return this
    }

    fun addContent(startAddr: String, data: List<String>): MifBuilder {
        val start = BigInt.parse(startAddr, addrRDX.base)
        val end = start + data.size.toBigInt()

        return addContent(start, end, data.map {
            BigInt.parse(it, dataRDX.base)
        })
    }

    fun addContent(startAddr: BigInt, data: List<IntNumber<*>>): MifBuilder {
        // Find the range where the new content starts and modify accordingly
        if (data.isEmpty()) return this
        val newEnd = startAddr + (data.size - 1).toBigInt()
        val modifiedRanges = mutableListOf<Range>()

        ranges.forEach { range ->
            when {
                // Range is fully before the new content, keep it unchanged
                range.end < startAddr -> modifiedRanges.add(range)

                // Range is fully after the new content, keep it unchanged
                range.start > newEnd -> modifiedRanges.add(range)

                // The range overlaps with the new content
                else -> {
                    // Split the range into three parts: before, overlap, and after

                    // Part before the new content
                    if (range.start < startAddr) {
                        modifiedRanges.add(Range(range.start, startAddr - 1.toBigInt(), range.data))
                    }

                    // The new content replaces this part of the range
                    modifiedRanges.add(Range(startAddr, newEnd, data.map { it.toBigInt() }))

                    // Part after the new content
                    if (range.end > newEnd) {
                        modifiedRanges.add(Range(newEnd + 1.toBigInt(), range.end, range.data))
                    }
                }
            }
        }

        ranges.clear()
        ranges.addAll(modifiedRanges)
        return this
    }

    fun setAddrRadix(radix: MifRadix): MifBuilder {
        this.addrRDX = radix
        return this
    }

    fun setDataRadix(radix: MifRadix): MifBuilder {
        this.dataRDX = radix
        return this
    }

    inner class Range(val start: BigInt, val end: BigInt, val data: List<BigInt>) {
        // Helper function to check if a range contains a specific address
        fun contains(addr: BigInt): Boolean = addr in start..end

        // Helper function to check if a range overlaps with another range
        fun overlaps(startAddr: BigInt, endAddr: BigInt): Boolean =
            !(startAddr > end || endAddr < start)

        // Splits the range into parts that come before and after a specific address
        fun split(addr: BigInt): Pair<Range?, Range?> {
            return if (addr > start && addr < end) {
                Pair(
                    Range(start, addr - 1.toBigInt(), data),
                    Range(addr + 1.toBigInt(), end, data)
                )
            } else {
                Pair(null, null)
            }
        }

        fun build(): String {
            val string = if (start == end) {
                // Single address
                "  ${start.addrRDX()} : ${data[0].dataRDX()};"
            } else if (data.size == 1) {
                // A range of addresses with a single repeating value
                "  [${start.addrRDX()}..${end.addrRDX()}] : ${data[0].dataRDX()};"
            } else {
                // A range of addresses with alternating values
                val dataStr = data.joinToString(" ") { it.dataRDX() }
                "  [${start.addrRDX()}..${end.addrRDX()}] : $dataStr;"
            }
            return string
        }

        fun init(memory: Memory<*, *>) {
            if (data.all { it == BigInt.ZERO }) return

            if (start == end) {
                memory.storeValues(start, data.map { wordSize.to(it) })
            } else if (data.size == 1) {
                var currAddr = start
                while (true) {
                    memory.storeEndianAwareValue(currAddr, wordSize.to(data.first()))
                    if (currAddr == end) break
                    currAddr += 1
                }
            } else {
                memory.storeValues(start, data.map { wordSize.to(it) })
            }
        }

        override fun toString(): String = "Range: ${start}, ${end}, $data -> ${build()}"

    }

    // Word Radix Format

    private fun BigInt.addrRDX(): String = addrSize.to(this).toString(addrRDX.base)
    private fun BigInt.dataRDX(): String = wordSize.to(this).toString(dataRDX.base)

    override fun toString(): String {
        return build()
    }

    companion object {
        fun parseElf(file: ELFFile): MifBuilder {
            return when (file) {
                is ELF32File -> parseElf32(file)
                is ELF64File -> parseElf64(file)
            }
        }

        private fun parseElf32(file: ELF32File): MifBuilder {
            val builder = MifBuilder(UInt8, UInt32, file.id)
            val bytes = file.bytes

            file.programHeaders.forEach {
                if (it !is ELF32_Phdr) return@forEach
                val startAddr = it.p_vaddr.toBigInt()
                val startOffset = it.p_offset
                val size = it.p_filesz

                val segmentBytes = bytes.copyOfRange(startOffset.toInt(), (startOffset + size).toInt()).map { byte -> byte.toInt8() }
                builder.addContent(startAddr, segmentBytes)
            }

            return builder
        }

        private fun parseElf64(file: ELF64File): MifBuilder {
            val builder = MifBuilder(UInt8, UInt64, file.id)
            val bytes = file.bytes

            file.programHeaders.forEach {
                if (it !is ELF64_Phdr) return@forEach
                val startAddr = it.p_vaddr.toBigInt()
                val startOffset = it.p_offset
                val size = it.p_filesz

                val segmentBytes = bytes.copyOfRange(startOffset.toInt(), (startOffset + size).toInt()).map { byte -> byte.toInt8() }
                builder.addContent(startAddr, segmentBytes)
            }

            return builder
        }
    }
}