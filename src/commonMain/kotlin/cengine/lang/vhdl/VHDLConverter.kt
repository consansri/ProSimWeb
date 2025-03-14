package cengine.lang.vhdl

import cengine.lang.asm.Initializer


fun Initializer.toVHDL(packageName: String, memoryName: String): String {

    val content = contents()
    val entry = entry()
    val type = content.values.firstOrNull()?.first?.firstOrNull()?.type

    return buildString {
        appendLine("library IEEE;")
        appendLine("use IEEE.STD_LOGIC_1164.ALL;")
        appendLine("use IEEE.STD_LOGIC_ARITH.ALL;")
        appendLine("use IEEE.STD_LOGIC_UNSIGNED.ALL;")
        appendLine()
        appendLine("package $packageName is")
        appendLine()
        if (type == null) {
            appendLine("    -- COULDN'T EVALUATE WORD_WIDTH!")
            appendLine()
        }
        appendLine("    constant entrypoint : std_logic_vector(${entry.type.BITS - 1} downto 0) := X\"${entry.zeroPaddedHex()}\"")
        appendLine()

        content.filter { it.value.first.isNotEmpty() }.forEach { (secaddr, values) ->
            val constName = "${memoryName}_${secaddr.toString(16)}"
            val typeName = "${constName}_t"

            appendLine("    type $typeName is array(0 to ${values.first.size - 1}) of std_logic_vector(${type?.BITS ?: "WORD_WIDTH"} - 1 downto 0);")
            appendLine("    constant $constName : $typeName := (")

            val secDataList = values.first
            secDataList.forEachIndexed { index, value ->
                val separator = if (index < secDataList.size - 1) "," else ""
                appendLine("        X\"${value.zeroPaddedHex()}\"$separator")
            }
            appendLine("    );")
        }

        appendLine()
        appendLine("end $packageName;")
    }
}


