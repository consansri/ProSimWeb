package cengine.lang.asm.gas

import cengine.console.IOContext
import cengine.console.SysOut
import cengine.lang.asm.psi.AsmInstruction
import cengine.lang.obj.elf.LinkerScript
import cengine.lang.obj.elf.Shdr
import cengine.util.buffer.Buffer
import cengine.util.integer.BigInt
import cengine.util.integer.UInt32
import cengine.util.integer.UInt64

abstract class AsmCodeGenerator<T : AsmCodeGenerator.Section>(
    val linkerScript: LinkerScript,
    protected val io: IOContext = SysOut
) {

    abstract val outputFileSuffix: String

    // --- State ---
    val symbols: MutableSet<Symbol<T>> = mutableSetOf()
    abstract val sections: MutableList<T>
    abstract var currentSection: T
        protected set

    abstract fun orderSectionsAndResolveAddresses()

    // Helper Functions

    fun getOrCreateAbsSymbolInCurrentSection(name: String, value: BigInt): Boolean {
        return symbols.add(Symbol.Abs(name, currentSection, symbols.firstOrNull { it.name == name }?.binding ?: Symbol.Binding.LOCAL, value))
    }

    fun getOrCreateSectionAndSetCurrent(name: String, type: UInt32 = Shdr.SHT_NULL, flags: UInt64 = UInt64.ZERO, link: T? = null, info: String? = null): T {
        currentSection = getOrCreateSection(name, type, flags, link, info)
        return currentSection
    }

    fun getOrCreateSection(name: String, type: UInt32 = Shdr.SHT_NULL, flags: UInt64 = UInt64.ZERO, link: T? = null, info: String? = null): T {
        val section = sections.firstOrNull { it.name == name }
        if (section != null) return section
        val created = createNewSection(name, type, flags, link, info)
        sections.add(created)
        return created
    }

    // Abstract Functions

    abstract fun writeFile(): ByteArray

    abstract fun createNewSection(name: String, type: UInt32 = Shdr.SHT_NULL, flags: UInt64 = UInt64.ZERO, link: T? = null, info: String? = null): T

    interface Section {
        val name: String
        var type: UInt32
        var flags: UInt64
        var link: Section?
        var info: String?
        var address: BigInt

        val content: Buffer<*>
        val reservations: MutableList<InstrReservation>

        fun queueLateInit(instr: AsmInstruction, size: Int) {
            reservations.add(InstrReservation(instr, content.size.toUInt()))
            content.pad(size)
        }

        fun print(): String = "$name: size ${content.size}"

        fun isProg(): Boolean = type == Shdr.SHT_PROGBITS
        fun isText(): Boolean = isProg() && (Shdr.SHF_EXECINSTR + Shdr.SHF_ALLOC).toUInt64() == flags
        fun isData(): Boolean = isProg() && (Shdr.SHF_WRITE + Shdr.SHF_ALLOC).toUInt64() == flags
        fun isRoData(): Boolean = isProg() && Shdr.SHF_ALLOC.toUInt64() == flags
    }

    sealed class Symbol<T : Section>(val name: String, val section: T, var binding: Binding = Binding.LOCAL) {

        abstract override fun equals(other: Any?): Boolean
        abstract override fun hashCode(): Int
        abstract override fun toString(): String

        class Abs<T : Section>(name: String, section: T, binding: Binding, val value: BigInt) : Symbol<T>(name, section, binding) {
            override fun toString(): String = "Abs($name, $value, $binding)"

            override fun equals(other: Any?): Boolean {
                if (other !is Abs<*>) return false
                if (other.name != name) return false
                if (other.section != section) return false
                return true
            }

            override fun hashCode(): Int {
                var result = this::class.hashCode()
                result = 31 * result + name.hashCode()
                result = 31 * result + section.hashCode()
                return result
            }
        }

        class Label<T : Section>(name: String, link: T, binding: Binding, val offset: BigInt) : Symbol<T>(name, link, binding) {
            val local = name.all { it.isDigit() }
            fun address(): BigInt = section.address + offset

            override fun toString(): String = "Label($name, $offset, $binding)"

            override fun equals(other: Any?): Boolean {
                if (other !is Label<*>) return false
                if (other.name != name) return false
                if (other.section != section) return false
                return true
            }

            override fun hashCode(): Int {
                var result = name.hashCode()
                result = 31 * result + section.hashCode()
                return result
            }

        }

        enum class Binding {
            LOCAL,
            GLOBAL,
            WEAK
        }
    }

    data class InstrReservation(val instr: AsmInstruction, val offset: UInt)
}