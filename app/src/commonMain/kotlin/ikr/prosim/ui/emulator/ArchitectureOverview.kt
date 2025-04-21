package ui.emulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import emulator.kit.ArchConfig
import emulator.kit.Architecture
import ui.uilib.interactable.Selector
import ui.uilib.label.CLabel
import uilib.UIState

@Composable
fun ArchitectureOverview(arch: Architecture<*, *>?, baseStyle: TextStyle, baseLargeStyle: TextStyle) {

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
    val vScrollState = rememberScrollState()

    if (arch != null) {
        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(theme.COLOR_BG_1),
                contentAlignment = Alignment.Center
            ) {
                CLabel(text = "Architecture: ${arch.description.name}", textStyle = baseLargeStyle)
            }

            Row(
                Modifier.fillMaxSize()
                    .verticalScroll(vScrollState),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {

                arch.settings.forEach {
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CLabel(text = it.name, textStyle = baseStyle)
                        when (it) {
                            is ArchConfig.Setting.Any -> TODO()
                            is ArchConfig.Setting.Bool -> TODO()
                            is ArchConfig.Setting.Enumeration -> {
                                Selector(
                                    it.enumValues, initial = it.state.value as? Enum<*>, itemContent = { isSelected, value ->
                                        CLabel(text = value.name, textStyle = baseStyle)
                                    },
                                    onSelectionChanged = { newVal ->
                                        it.loadFromString(arch, newVal.name)
                                    }
                                )
                            }
                        }

                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CLabel(text = "No Architecture Selected!", textStyle = baseStyle)
        }
    }
}
