package cengine.lang.asm.target.ikrrisc2

import cengine.lang.asm.psi.AsmRegisterT

enum class IkrR2BaseRegs: AsmRegisterT {
    R0,
    R1,
    R2,
    R3,
    R4,
    R5,
    R6,
    R7,
    R8,
    R9,
    R10,
    R11,
    R12,
    R13,
    R14,
    R15,
    R16,
    R17,
    R18,
    R19,
    R20,
    R21,
    R22,
    R23,
    R24,
    R25,
    R26,
    R27,
    R28,
    R29,
    R30,
    R31;

    override val displayName = name.lowercase()
    override val recognizable: List<String> = listOf(name.lowercase(), "x$ordinal")
    override val numericalValue: UInt = ordinal.toUInt()

    companion object{
        val allNames = entries.map { it.recognizable }.flatten().toSet().toTypedArray()
    }

}