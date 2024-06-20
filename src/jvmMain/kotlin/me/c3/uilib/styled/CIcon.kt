package me.c3.uilib.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import me.c3.uilib.UIManager
import me.c3.uilib.styled.CIconButton.Mode
import java.awt.Color
import java.lang.ref.WeakReference
import javax.swing.JLabel

class CIcon(val svgIcon: FlatSVGIcon, val mode: Mode = Mode.PRIMARY_NORMAL) : JLabel() {

    init {
        UIManager.theme.addEvent(WeakReference(this)) { _ ->
            setDefaults()
        }

        UIManager.scale.addEvent(WeakReference(this)) { _ ->
            setDefaults()
        }

        setDefaults()
    }

    private fun setDefaults() {
        isOpaque = false
        background = Color(0, 0, 0, 0)
        border = when (mode) {
            Mode.PRIMARY_NORMAL, Mode.SECONDARY_NORMAL, Mode.GRADIENT_NORMAL -> UIManager.scale.get().controlScale.getNormalInsetBorder()
            Mode.GRADIENT_SMALL, Mode.SECONDARY_SMALL, Mode.PRIMARY_SMALL -> UIManager.scale.get().controlScale.getSmallInsetBorder()
        }

        svgIcon.colorFilter = ColorFilter {
            when (mode) {
                Mode.PRIMARY_NORMAL, Mode.PRIMARY_SMALL -> UIManager.theme.get().iconLaF.iconFgPrimary
                Mode.SECONDARY_NORMAL, Mode.SECONDARY_SMALL -> UIManager.theme.get().iconLaF.iconFgSecondary
                Mode.GRADIENT_NORMAL, Mode.GRADIENT_SMALL -> null
            }
        }

        val size = when (mode) {
            Mode.PRIMARY_SMALL, Mode.SECONDARY_SMALL, Mode.GRADIENT_SMALL -> UIManager.scale.get().controlScale.smallSize
            Mode.PRIMARY_NORMAL, Mode.SECONDARY_NORMAL, Mode.GRADIENT_NORMAL -> UIManager.scale.get().controlScale.normalSize
        }

        icon = svgIcon.derive(size, size)
    }


}