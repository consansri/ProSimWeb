package cengine.lang.asm.ast

import cengine.lang.asm.ast.impl.ASNode
import cengine.lang.obj.elf.LinkerScript
import cengine.lang.obj.elf.Shdr
import cengine.psi.PsiManager
import cengine.util.buffer.Buffer
import cengine.util.integer.BigInt
import cengine.util.integer.BigInt.Companion.toBigInt
import cengine.util.integer.UInt32
import cengine.util.integer.UInt64

abstract class AsmCodeGenerator<T : AsmCodeGenerator.Section>(protected val linkerScript: LinkerScript, val psiManager: PsiManager<*, *>) {

    abstract val fileSuffix: String

    val symbols: MutableSet<Symbol<T>> = mutableSetOf()

    abstract val sections: MutableList<T>

    abstract var currentSection: T

    protected abstract fun orderSectionsAndResolveAddresses()

    protected abstract fun writeFile(): ByteArray

    suspend fun generate(ast: ASNode.Program): ByteArray {
        val statements = ast.getAllStatements()
        statements.forEach { stmnt ->
            execute(stmnt)
        }

        orderSectionsAndResolveAddresses()

        // Resolve Late Evaluation
        sections.forEach {
            it.resolveReservations()
        }

        return writeFile()
    }

    private fun addLabel(name: String): Boolean {
        if (symbols.find { it is Symbol.Label<*> && it.name == name && it.section == currentSection } != null) {
            return false
        }

        val found = symbols.find { it.name == name && it.section == currentSection }
        if (found != null) {
            symbols.remove(found)
        }

        symbols.add(Symbol.Label(
            name,
            currentSection,
            found?.binding ?: Symbol.Binding.LOCAL,
            currentSection.content.size.toBigInt()
        ))

        return true
    }

    private fun Section.resolveReservations() {
        reservations.forEach { def ->
            def.instr.nodes.filterIsInstance<ASNode.NumericExpr>().forEach { expr ->
                // Assign all Labels
                expr.assign(symbols, this, def.offset)
            }
            def.instr.type.lateEvaluation(this@AsmCodeGenerator, this, def.instr, def.offset.toInt())
        }
        reservations.clear()
    }

    suspend fun execute(stmnt: ASNode.Statement) {
        if (stmnt.label != null) {
            val added = addLabel(stmnt.label.identifier)
            if (!added) {
                stmnt.addError("Label ${stmnt.label.identifier} was already defined!")
            }
        }

        when (stmnt) {
            is ASNode.Statement.Dir -> {
                try {
                    stmnt.dir.type.build(this@AsmCodeGenerator, stmnt.dir)
                } catch (e: NotImplementedError) {
                    stmnt.dir.addError(e.message.toString())
                }
            }

            is ASNode.Statement.Empty -> {}

            is ASNode.Statement.Instr -> {
                stmnt.instruction.nodes.filterIsInstance<ASNode.NumericExpr>().forEach {
                    it.assign(symbols, currentSection, currentSection.content.size.toUInt())
                }
                try {
                    stmnt.instruction.type.resolve(this@AsmCodeGenerator, stmnt.instruction)
                } catch (e: Exception) {
                    stmnt.instruction.addError(e.message.toString())
                }
            }

            is ASNode.Statement.Error -> {}
        }
    }

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

        fun queueLateInit(instr: ASNode.Instruction, size: Int) {
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

    data class InstrReservation(val instr: ASNode.Instruction, val offset: UInt)


}