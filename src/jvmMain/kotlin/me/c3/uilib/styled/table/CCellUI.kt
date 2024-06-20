package me.c3.uilib.styled.table

import me.c3.uilib.UIManager
import me.c3.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.lang.ref.WeakReference
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.ComponentUI

class CCellUI(private val fontType: FontType) : ComponentUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val cell = c as? CCell ?: return

        UIManager.theme.addEvent(WeakReference(cell)) { _ ->
            setDefaults(cell)
        }

        UIManager.scale.addEvent(WeakReference(cell)) { _ ->
            setDefaults(cell)
        }

        setDefaults(cell)
    }

    private fun setDefaults(cell: CCell) {
        cell.isOpaque = false
        cell.border = BorderFactory.createEmptyBorder()
        cell.font = fontType.getFont()
        cell.fontMetrics = cell.getFontMetrics(cell.font)
        updateTextColors(cell)
    }

    fun updateTextColors(cell: CCell) {
        val customFG = cell.customFG
        val customBG = cell.customBG
        cell.background = customBG ?: Color(0, 0, 0, 0)
        cell.foreground = customFG ?: UIManager.theme.get().textLaF.base
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        val g2d = g?.create() as? Graphics2D ?: return super.paint(g, c)
        val cell = c as? CCell ?: return super.paint(g, c)

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        val string = cell.textToDraw()

        val y = cell.insets.top + cell.fontMetrics.ascent
        val x = cell.insets.left

        g2d.font = cell.font
        g2d.color = cell.background
        g2d.fillRect(0, 0, cell.bounds.width, cell.bounds.height)

        val contentBounds = cell.fontMetrics.getStringBounds(string, g2d)
        cell.preferredSize = contentBounds.bounds.size

        val offsetx = (cell.bounds.width - contentBounds.bounds.width) / 2
        val offsetY = (cell.bounds.height - contentBounds.bounds.height) / 2
        g2d.color = cell.foreground

        g2d.drawString(string, x + offsetx, y + offsetY)

        g2d.dispose()
    }

}