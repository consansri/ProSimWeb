package me.c3.ui.styled

import me.c3.ui.components.styled.CIconButton
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.AbstractButton
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicButtonUI

class CIconButtonUI : BasicButtonUI() {


    var inset = 2
    var cornerRadius = 10

    companion object {
        val HOVER_COLOR = Color(0x55777777, true)
    }

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val button = c as? CIconButton ?: return

        button.isContentAreaFilled = false
        button.isFocusPainted = false
        button.isFocusable = false
        button.border = BorderFactory.createEmptyBorder(inset, inset, inset, inset)

        // Apply hover effect
        button.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                if (!button.isDeactivated) {
                    button.background = HOVER_COLOR
                    button.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                }
            }

            override fun mouseExited(e: MouseEvent?) {
                button.background = null // Reset background to default
            }
        })
    }

    override fun paint(g: Graphics?, c: JComponent?) {
        val button = c as? AbstractButton ?: return
        val g2 = g as? Graphics2D ?: return
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val width = button.width
        val height = button.height

        // Paint button background
        g2.color = button.background
        g2.fillRoundRect(inset, inset, width - inset * 2, height - inset * 2, cornerRadius, cornerRadius)

        // Paint button
        super.paint(g, c)
        g2.dispose()
    }

    override fun getPreferredSize(c: JComponent?): Dimension {
        val button = c as? AbstractButton ?: return super.getPreferredSize(c)
        val preferredSize = super.getPreferredSize(button)
        return Dimension(preferredSize.width + inset * 2, preferredSize.height + inset * 2)
    }

    override fun getMinimumSize(c: JComponent?): Dimension {
        return getPreferredSize(c)
    }

    override fun getMaximumSize(c: JComponent?): Dimension {
        return getPreferredSize(c)
    }
}