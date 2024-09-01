package ui.uilib.layout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ui.uilib.UIState

@Composable
fun VerticalToolBar(
    upper: @Composable (ColumnScope) -> Unit,
    lower: @Composable (ColumnScope) -> Unit

) {

    val scale = UIState.Scale.value

    Column(
        Modifier
            .fillMaxHeight()
            .padding(scale.SIZE_INSET_MEDIUM),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column {
            upper(this)
        }

        Column {
            lower(this)
        }
    }

}