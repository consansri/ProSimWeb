package cengine.lang.vhdl

import Constants
import cengine.console.IOContext
import cengine.lang.asm.AsmBinaryProvider
import kotlin.math.log2

fun AsmBinaryProvider.toVHDL(context: IOContext, packageName: String, memoryName: String, dataWidth: Int, chunkSize: Int): String {

    val content = contents()
    val entry = entry()

    return buildString {
        appendLine("-- ${Constants.sign()}")
        appendLine()
        appendLine("library IEEE;")
        appendLine("use IEEE.STD_LOGIC_1164.ALL;")
        appendLine("use IEEE.NUMERIC_STD.ALL;")
        appendLine()
        appendLine("PACKAGE $packageName IS")
        appendLine("    constant entrypoint : unsigned := ${entry};")
        appendLine()
        content.filter { it.value.first.isNotEmpty() }.forEach { (secAddr, sectionContent) ->

            val type = sectionContent.first.first().type

            // Check if dataWidth is valid!
            if (dataWidth % type.BITS != 0 && dataWidth > 0) {
                context.error("The specified bit width ($dataWidth) is not a multiple of the source bit width (${type.BITS})!")
                appendLine("-- ERROR: (Section: ${secAddr.toString(16)}) The specified bit width ($dataWidth) is not a multiple of the source bit width (${type.BITS})!")
            } else {
                val widthCount = dataWidth / type.BITS
                val addrShift = log2(widthCount.toFloat()).toInt()

                val startAddr = secAddr.shr(addrShift)

                val constName = "${memoryName}_${startAddr.toString(16)}"
                val typeName = "${constName}_t"
                val secDataList = sectionContent.first.chunked(widthCount).chunked(chunkSize)

                appendLine("    type $typeName is array(0 to ${sectionContent.first.size - 1}) of std_logic_vector(${dataWidth - 1} downto 0);")
                appendLine("    constant $constName : $typeName := (")
                secDataList.forEachIndexed { chunkIndex, chunk ->
                    val separator = if (chunkIndex == secDataList.size - 1) "" else ", "

                    val contentLine = chunk.joinToString(", ") { valueChunk -> "X\"${valueChunk.joinToString("") { part -> part.uPaddedHex() }}\"" }

                    appendLine("        $contentLine$separator ") // -- ${chunk.joinToString(" ") { it.int8s().toASCIIString() }}
                }
                appendLine("    );")
                appendLine()
            }
        }
        appendLine("END $packageName;")

    }
}


