package uilib.layout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import uilib.UIState

@Composable
fun HorizontalToolBar(
    left: @Composable (RowScope) -> Unit,
    right: @Composable (RowScope) -> Unit
){

    val scale = UIState.Scale.value

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = scale.SIZE_INSET_MEDIUM)
        ,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row {
            left(this)
        }

        Row {
            right(this)
        }
    }

}