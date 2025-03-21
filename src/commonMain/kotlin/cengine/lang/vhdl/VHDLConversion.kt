package cengine.lang.vhdl

import Constants
import cengine.lang.asm.Initializer

fun Initializer.toVHDL(packageName: String, memoryName: String, chunkSize: Int = 16): String {

    val content = contents()
    val entry = entry()
    val type = content.values.firstOrNull()?.first?.firstOrNull()?.type

    return buildString {
        appendLine("-- ${Constants.sign()}")
        appendLine()
        appendLine("library IEEE;")
        appendLine("use IEEE.STD_LOGIC_1164.ALL;")
        appendLine("use IEEE.NUMERIC_STD.ALL;")
        appendLine()
        appendLine("PACKAGE $packageName IS")
        appendLine("    constant entrypoint : std_logic_vector(${entry.type.BITS - 1} downto 0) := X\"${entry.zeroPaddedHex()}\";")
        appendLine()
        content.filter { it.value.first.isNotEmpty() }.forEach { (secaddr, values) ->
            val constName = "${memoryName}_${secaddr.toString(16)}"
            val typeName = "${constName}_t"
            val secDataList = values.first.chunked(chunkSize)
            appendLine("    type $typeName is array(0 to ${values.first.size - 1}) of std_logic_vector(${type?.BITS ?: "WORD_WIDTH"} - 1 downto 0);")
            appendLine("    constant $constName : $typeName := (")
            secDataList.forEachIndexed { index, chunk ->
                val separator = if (index == secDataList.size - 1) "" else ", "
                appendLine("        ${chunk.joinToString(", ") { "X\"${it.zeroPaddedHex()}\"" }}$separator ") // -- ${chunk.joinToString(" ") { it.int8s().toASCIIString() }}
            }
            appendLine("    );")
            appendLine()
        }
        appendLine("END $packageName;")

    }
}


