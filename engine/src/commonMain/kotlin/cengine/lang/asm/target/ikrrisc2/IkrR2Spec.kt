package cengine.lang.asm.target.ikrrisc2

import cengine.lang.asm.AsmSpec
import cengine.lang.asm.psi.AsmDirective
import cengine.lang.asm.psi.AsmDirectiveT
import cengine.lang.asm.psi.AsmInstructionT
import cengine.lang.mif.MifGenerator
import cengine.lang.obj.elf.LinkerScript
import cengine.util.Endianness
import cengine.util.buffer.Buffer32
import cengine.util.integer.BigInt
import cengine.util.integer.Int32
import cengine.util.integer.UInt32
import cengine.util.integer.UInt64
import cengine.util.integer.UInt64.Companion.toUInt64
import emulator.EmuLink

data object IkrR2Spec : AsmSpec<MifGenerator<Buffer32>> {
    override val name: String = "IKR RISC-II"
    override val emuLink: EmuLink = EmuLink.IKRRISC2
    override val instrTypes: List<AsmInstructionT> = IkrR2InstrT.entries
    override val dirTypes: List<AsmDirectiveT> = AsmDirective.all
    override val commentSlAlt: String = ";"
    override val litIntBinPrefix: String = "%"
    override val litIntHexPrefix: String = "$"
    override val addPunctuations: Set<String> = setOf("#")

    override val contentExample: String
        get() = """
            ; $name example
            
            .equ    LIST, 0x1000
            .equ    N, 8

            main:   addli r30 := r0, #${'$'}FFFF
                    bsr test_lru
            .doom:  bra .doom

            test_lru: addi r30 := r30, #-1
                      std (r30, 0) := r31
                      addi r2 := r0, #LIST
                      addi r3 := r0, #N
                      bsr lru_init
                      addi r4 := r0, #4
                      bsr lru
                      addi r4 := r0, #7
                      bsr lru
                      addi r4 := r0, #2
                      bsr lru
                      ldd r31 := (r30, 0)
                      addi r30 := r30, #1
                      jmp r31
                      
            lru_init: add r5 := r3, r0
            .loop:    beq r5, .end_init
                      addi r5 := r5, #-1
                      str (r2, r5) := r5
                      bra .loop
            .end_init:     jmp r31   

            lru: addi r1 := r0, #0
                 addi r5 := r3, #-1
            .oloop: blt r5, .oloop_end
                    ldr r7 := (r2, r5)
                    cmps r8 := r7, r4
                    bne r8, .end_if
                    addi r6 := r5, #-1
            .iloop: blt r6, .iloop_end
                    ldr r8 := (r2, r6)
                    add r9 := r2, r6
                    std (r9, 1) := r8
                    addi r6 := r6, #-1
                    bra .iloop
            .iloop_end: std (r2, 0) := r4
                        addi r7 := r3, #-1
                        ldr r1 := (r2, r7)
                        bra .oloop_end
            .end_if:    addi r5 := r5, #-1
                        bra .oloop
            .oloop_end: jmp r31
            
        """.trimIndent()

    override fun createGenerator(): MifGenerator<Buffer32> = MifGenerator(object : LinkerScript {
        override val textStart: BigInt = BigInt.ZERO
        override val dataStart: BigInt? = null
        override val rodataStart: BigInt? = null
        override val segmentAlign: UInt64 = 0x10000U.toUInt64()
    }, UInt32) {
        Buffer32(Endianness.BIG)
    }

    override fun toString(): String = name
}