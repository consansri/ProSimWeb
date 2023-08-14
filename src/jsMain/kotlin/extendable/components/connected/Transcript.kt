package extendable.components.connected

import extendable.components.types.MutVal

class Transcript(private val compiledHeaders: List<String> = listOf(), private val disassembledHeaders: List<String> = listOf()) {

    val disassembled = mutableListOf<Row>()
    val compiled = mutableListOf<Row>()

    fun clear() {
        disassembled.clear()
        compiled.clear()
    }

    fun clear(type: Type) {
        when (type) {
            Type.COMPILED -> compiled.clear()
            Type.DISASSEMBLED -> disassembled.clear()
        }
    }

    fun deactivated(): Boolean {
        return compiledHeaders.isEmpty() && disassembledHeaders.isEmpty()
    }

    fun addRow(type: Type, row: Row) {
        disassembled.add(row)
    }

    fun addContent(type: Type, content: List<Row>) {
        when (type) {
            Type.COMPILED -> compiled.addAll(content)
            Type.DISASSEMBLED -> disassembled.addAll(content)
        }
    }

    fun getHeaders(type: Type): List<String> {
        return when (type) {
            Type.COMPILED -> compiledHeaders
            Type.DISASSEMBLED -> disassembledHeaders
        }
    }

    fun getContent(type: Type): List<Row> {
        return when (type) {
            Type.COMPILED -> compiled
            Type.DISASSEMBLED -> disassembled
        }
    }

    abstract class Row(vararg addresses: MutVal.Value) {
        private val addresses = addresses.toMutableList()

        fun addAddresses(vararg addresses: MutVal.Value){
            this.addresses.addAll(addresses)
        }
        fun getAddresses():List<MutVal.Value>{
            return addresses
        }
        abstract fun getContent(): List<Entry>
        data class Entry(val orientation: Orientation, val content: String)
        enum class Orientation{
            LEFT,
            CENTER,
            RIGHT
        }
    }

    enum class Type {
        COMPILED,
        DISASSEMBLED
    }




}