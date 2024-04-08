package me.c3.ui

import emulator.Link
import me.c3.ui.components.editor.CodeEditor
import me.c3.ui.events.EventManager
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.icons.BenIcons
import java.io.File
import java.nio.file.Paths
import javax.swing.JFrame


class UIManager() {

    val archManager = ArchManager(Link.RV32I.arch)

    val icons = BenIcons()

    val themeManager = ThemeManager(icons)
    val scaleManager = ScaleManager()
    val eventManager = EventManager(archManager)

    private val anyEventListeners = mutableListOf<() -> Unit>()

    val editor = CodeEditor(this)

    private var ws = Workspace(Paths.get("").toAbsolutePath().toString(), editor, this)
    private val wsChangedListeners = mutableListOf<(Workspace) -> Unit>()

    init {
        themeManager.addThemeChangeListener {
            triggerAnyEvent()
        }
        scaleManager.addScaleChangeEvent {
            triggerAnyEvent()
        }
        eventManager.addExeEventListener {
            triggerAnyEvent()
        }
        eventManager.addCompileListener {
            triggerAnyEvent()
        }
        addWSChangedListener {
            triggerAnyEvent()
        }
    }

    fun currTheme() = themeManager.currentTheme
    fun currScale() = scaleManager.currentScaling
    fun currArch() = archManager.curr
    fun currWS() = ws
    fun setCurrWS(path: String) {
        ws = Workspace(path, editor, this)
        triggerWSChanged()
    }

    fun addAnyEventListener(event: () -> Unit) {
        anyEventListeners.add(event)
    }

    fun addWSChangedListener(event: (Workspace) -> Unit) {
        wsChangedListeners.add(event)
    }

    private fun triggerAnyEvent() {
        anyEventListeners.forEach {
            it()
        }
    }

    private fun triggerWSChanged() {
        wsChangedListeners.forEach {
            it(currWS())
        }
    }

}