package ui.uilib.params

import androidx.compose.ui.unit.Dp
import uilib.UIState

enum class IconType {
    SMALL,
    MEDIUM,
    LARGE;

    fun getSize(): Dp{
        val scale = UIState.Scale.value
        return when(this){
            SMALL -> scale.SIZE_CONTROL_SMALL
            MEDIUM -> scale.SIZE_CONTROL_MEDIUM
            LARGE -> scale.SIZE_CONTROL_LARGE
        }
    }
}