package emulator

import cengine.console.SysOut
import emulator.archs.ArchIKRMini
import emulator.archs.ArchIKRRisc2
import emulator.archs.ArchRV32
import emulator.archs.ArchRV64
import emulator.kit.ArchConfig
import emulator.kit.Architecture
import kotlin.reflect.KClass

/**
 *  This enum-class contains every specific Architecture
 *  <NEEDS TO BE EXTENDED>
 */
enum class EmuLink(private val arch: () -> Architecture<*, *>, val classType: KClass<*>) {
    RV32I({ ArchRV32() }, ArchRV32::class),
    RV64I({ ArchRV64() }, ArchRV64::class),
    //T6502({ ArchT6502() }, ArchT6502::class),
    IKRMINI({ ArchIKRMini() }, ArchIKRMini::class),
    IKRRISC2({ ArchIKRRisc2() }, ArchIKRRisc2::class);

    fun load(): Architecture<*, *> {
        val loaded = arch()
        loaded.resetMicroArch()
        SysOut.log("Architecture $name loaded!")
        return loaded
    }

    fun descr(): ArchConfig.Description {
        return arch().description
    }

    override fun toString(): String {
        return descr().fullName
    }
}