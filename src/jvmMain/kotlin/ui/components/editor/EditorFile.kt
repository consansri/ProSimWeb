package me.c3.ui.components.editor

import emulator.kit.*
import emulator.kit.assembly.Compiler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import javax.swing.text.SimpleAttributeSet

class EditorFile(val file: File, var hlState: List<Compiler.Token>? = null, private val onEditEvent: (EditorFile) -> Unit) {
    private var bufferedContent = file.readText()
        set(value) {
            field = value
            CoroutineScope(Dispatchers.IO).launch {
                store()
            }
        }
    fun contentAsString(): String = bufferedContent
    fun getName(): String = file.name
    fun toCompilerFile(): Compiler.CompilerFile = file.toCompilerFile()
    fun reload() {
        bufferedContent = file.readText()
    }

    fun edit(content: String) {
        bufferedContent = content
    }

    suspend fun store() {
        withContext(Dispatchers.IO){
            try {
                file.writeText(bufferedContent)
            } catch (e: FileNotFoundException) {
                nativeWarn("File ${getName()} isn't writeable!")
            }
        }
    }

    fun getRawDocument(): CDocument {
        val document = CDocument()
        val attrs = SimpleAttributeSet()
        document.insertString(0, bufferedContent, attrs)
        return document
    }
}
