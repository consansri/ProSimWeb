package cengine.lang.mif

import Constants
import cengine.lang.asm.Initializer
import cengine.util.integer.BigInt.Companion.toBigInt
import kotlinx.datetime.*
import kotlin.math.pow


fun Initializer.toMif(addrBitWidth: Int?, chunkSize: Int = 16, addrRadix: MifRadix = MifRadix.HEX, dataRadix: MifRadix = MifRadix.HEX): String {
    val content = contents()
    val entry = entry()
    val type = content.values.firstOrNull()?.first?.firstOrNull()?.type

    return buildString {
        appendLine("-- ${Constants.sign()}")

        val depthGiven = addrBitWidth?.let {
            2.0.pow(addrBitWidth).toBigInt()
        }

        val depthCalc = depthGiven ?: content.entries.lastOrNull()?.let {
            it.key + it.value.first.size
        }
        appendLine()
        appendLine("DEPTH           = ${depthCalc?.toString() ?: "UNKNOWN"};")
        appendLine("WIDTH           = ${type?.BITS ?: "UNKNOWN"};")
        appendLine("ADDRESS_RADIX   = $addrRadix;")
        appendLine("DATA_RADIX      = $dataRadix;")
        appendLine()
        appendLine("CONTENT BEGIN")
        appendLine()
        content.filter { it.value.first.isNotEmpty() }.forEach { (key, value) ->
            value.first.chunked(chunkSize).forEachIndexed { chunkIndex, chunk ->
                val startAddr = key + chunkIndex * chunkSize
                val endAddr = startAddr + chunk.size - 1
                appendLine("    [${startAddr.toString(addrRadix.radix)}..${endAddr.toString(addrRadix.radix)}] : ${chunk.joinToString(" ") { it.toString(dataRadix.radix) }};")
            }
        }
        appendLine()
        appendLine("END;")
        appendLine()
    }
}
