package emulator.kit.register

interface FieldProvider {

    val name: String

    fun get(id: Int): String

}