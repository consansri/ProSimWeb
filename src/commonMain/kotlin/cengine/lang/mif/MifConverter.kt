package cengine.lang.mif

import cengine.lang.mif.MifGenerator.Radix
import cengine.lang.obj.elf.*
import cengine.util.integer.*
import cengine.util.integer.BigInt.Companion.toBigInt
import cengine.util.integer.Int8.Companion.toInt8
import com.ionspin.kotlin.bignum.integer.BigInteger
import emulator.kit.memory.Memory
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

class MifConverter(val depth: Double, val wordSize: IntNumberStatic<*>) {

    constructor(wordSize: IntNumberStatic<*>, addrSize: IntNumberStatic<*>, id: String) : this(2.0.pow(addrSize.BITS), wordSize)

    val addrSize: IntNumberStatic<*> = IntNumber.nearestUType(log2(depth).roundToInt() / 8)
    var addrRDX: Radix = Radix.HEX
    var dataRDX: Radix = Radix.HEX

    // Represents the ranges as a list of triples: (start address, end address, data value)
    val ranges: MutableList<Range> = mutableListOf()

    init {
        // Initially, all addresses are filled with 0
        ranges.add(Range(0.toBigInt(), BigInt(BigInteger.parseString("1".repeat(addrSize.BITS), 2)), listOf(BigInt.ZERO)))
    }

    fun build(): String {
        val builder = StringBuilder()
        builder.append("DEPTH = ${depth.toString().takeWhile { it != '.' }}; -- The size of memory in words\n")
        builder.append("WIDTH = ${wordSize.BITS}; -- The size of data in bits\n")
        builder.append("ADDRESS_RADIX = ${addrRDX.name}; -- The radix for address values\n")
        builder.append("DATA_RADIX = ${dataRDX.name}; -- The radix for data values\n")
        builder.append("CONTENT BEGIN\n")

        ranges.forEach { range ->
            builder.append(range.build())
        }

        builder.append("END;\n")

        return builder.toString()
    }

    fun addContent(startAddr: String, endAddr: String, data: List<String>): MifConverter {
        return addContent(BigInt.parse(startAddr, addrRDX.radix), BigInt.parse(endAddr, addrRDX.radix), data.map { BigInt.parse(it, dataRDX.radix) })
    }

    fun addContent(startAddr: BigInt, endAddr: BigInt, data: List<BigInt>): MifConverter {
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

    fun addContent(startAddr: String, data: List<String>): MifConverter {
        val start = BigInt.parse(startAddr, addrRDX.radix)
        val end = start + data.size.toBigInt()

        return addContent(start, end, data.map {
            BigInt.parse(it, dataRDX.radix)
        })
    }

    fun addContent(startAddr: BigInt, data: List<IntNumber<*>>): MifConverter {
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
                    modifiedRanges.add(Range(startAddr, newEnd, data))

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

    fun setAddrRadix(radix: Radix): MifConverter {
        this.addrRDX = radix
        return this
    }

    fun setDataRadix(radix: Radix): MifConverter {
        this.dataRDX = radix
        return this
    }

    inner class Range(val start: BigInt, val end: BigInt, val data: List<IntNumber<*>>) {
        // Helper function to check if a range contains a specific address
        fun contains(addr: BigInt): Boolean = addr >= start && addr <= end

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
                "  ${start.addrRDX()} : ${data[0].dataRDX()};\n"
            } else if (data.size == 1) {
                // A range of addresses with a single repeating value
                "  [${start.addrRDX()}..${end.addrRDX()}] : ${data[0].dataRDX()};\n"
            } else {
                // A range of addresses with alternating values
                val dataStr = data.joinToString(" ") { it.dataRDX() }
                "  [${start.addrRDX()}..${end.addrRDX()}] : $dataStr;\n"
            }
            return string
        }

        fun init(memory: Memory<*, *>) {
            if (data.all { it.toULong() == 0UL }) return

            if (start == end) {
                memory.storeArray(start, data)
            } else if (data.size == 1) {
                var currAddr = start
                while (true) {
                    memory.storeEndianAware(currAddr, data.first())
                    if (currAddr == end) break
                    currAddr += 1
                }
            } else {
                memory.storeArray(start, data)
            }
        }

        override fun toString(): String = "Range: ${start}, ${end}, $data -> ${build()}"

    }

    // Word Radix Format

    private fun BigInt.addrRDX(): String = this.toUnsigned().toString(addrRDX.radix)
    private fun IntNumber<*>.dataRDX(): String = this.toUnsigned().toString(dataRDX.radix)

    override fun toString(): String {
        return build()
    }

    companion object {
        fun parseElf(file: ELFFile): MifConverter {
            return when (file) {
                is ELF32File -> parseElf32(file)
                is ELF64File -> parseElf64(file)
            }
        }

        private fun parseElf32(file: ELF32File): MifConverter {
            val builder = MifConverter(UInt8, UInt32, file.name)
            val bytes = file.content

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

        private fun parseElf64(file: ELF64File): MifConverter {
            val builder = MifConverter(UInt8, UInt64, file.name)
            val bytes = file.content

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