package me.c3.ui.components.tree

import me.c3.ui.UIManager
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CScrollPane
import me.c3.ui.components.styled.CTextButton
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JFileChooser

class FileTree(uiManager: UIManager) : CPanel(uiManager, true) {
    private val projectButton = CTextButton(uiManager, "Project")
    private val title = CPanel(uiManager, false)
    private val content = CScrollPane(uiManager, true)

    init {
        attachMouseListener(uiManager)

        uiManager.addWSChangedListener {
            refreshWSTree(uiManager)
        }

        setDefaults(uiManager)
        refreshWSTree(uiManager)
    }

    private fun attachMouseListener(uiManager: UIManager){
        projectButton.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                super.mouseClicked(e)
                val fileChooser = JFileChooser()
                fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                val result = fileChooser.showOpenDialog(this@FileTree)
                if (result == JFileChooser.APPROVE_OPTION) {
                    val selectedFile = fileChooser.selectedFile
                    uiManager.setCurrWS(selectedFile.absolutePath)
                }
            }
        })
    }

    private fun refreshWSTree(uiManager: UIManager){
        content.setViewportView(uiManager.currWS().tree)
        content.revalidate()
        content.repaint()
    }

    private fun setDefaults(uiManager: UIManager){
        projectButton.foreground = uiManager.currTheme().textLaF.base
        projectButton.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        layout = BorderLayout()
        title.layout = FlowLayout(FlowLayout.LEFT)
        title.add(projectButton)

        this.add(title, BorderLayout.NORTH)
        this.add(content, BorderLayout.CENTER)

        border = BorderFactory.createEmptyBorder()
    }

}