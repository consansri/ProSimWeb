package cengine.lang.mif

import Constants
import cengine.console.IOContext
import cengine.lang.asm.AsmBinaryProvider
import cengine.util.integer.BigInt.Companion.toBigInt
import kotlin.math.log2
import kotlin.math.pow

fun AsmBinaryProvider.toMif(context: IOContext, addrBitWidth: Int, dataBitWidth: Int, chunkSize: Int): String {
    val sections = contents()
    entry()
    val type = sections.values.firstOrNull()?.first?.firstOrNull()?.type ?: return buildString {
        context.error("Content is empty!")
        appendLine("-- ${Constants.sign()}")
        appendLine("-- ERROR: Content is empty!")
    }

    val depth = 2.0.pow(addrBitWidth).toBigInt()

    if (dataBitWidth % type.BITS != 0) {
        context.error("The specified bit width ($dataBitWidth) is not a multiple of the source bit width (${type.BITS})!")
        return buildString {
            appendLine("-- ${Constants.sign()}")
            appendLine("-- ERROR: The specified bit width ($dataBitWidth) is not a multiple of the source bit width (${type.BITS})!")
        }
    }

    val widthCount = dataBitWidth / type.BITS
    val addrShift = log2(widthCount.toFloat()).toInt()

    return buildString {
        appendLine("-- ${Constants.sign()}")

        appendLine()
        appendLine("DEPTH           = ${depth};")
        appendLine("WIDTH           = ${dataBitWidth};")
        appendLine("ADDRESS_RADIX   = ${MifRadix.HEX};")
        appendLine("DATA_RADIX      = ${MifRadix.HEX};")
        appendLine()
        appendLine("CONTENT BEGIN")
        appendLine()
        sections.filter { it.value.first.isNotEmpty() }.forEach { (sectionAddr, sectionContent) ->
            appendLine("    -- Section ${sectionAddr.toString(16)}")
            sectionContent.first.chunked(widthCount).chunked(chunkSize).forEachIndexed { chunkIndex, chunk ->
                val startAddr = sectionAddr.shr(addrShift) + chunkIndex * chunkSize
                val endAddr = startAddr + chunk.size - 1
                val addrRange = "[${startAddr.toString(16)}..${endAddr.toString(16)}]"
                val content = chunk.map { value -> value.joinToString("") { it.uPaddedHex() } }.joinToString(" ") { it }
                appendLine("    $addrRange : $content;")
            }

            appendLine()
        }
        appendLine("END;")
        appendLine()
    }
}
