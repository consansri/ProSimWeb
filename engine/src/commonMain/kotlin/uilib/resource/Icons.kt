package ui.uilib.resource


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import uilib.UIState


interface Icons {

    val name: String

    // Define Icons as strings representing the resource paths
    val appLogo: ImageVector
    val add: ImageVector
    val autoscroll: ImageVector
    val backwards: ImageVector
    val bars: ImageVector
    val build: ImageVector
    val cancel: ImageVector
    val clearStorage: ImageVector
    val combineCells: ImageVector
    val continuousExe: ImageVector
    val console: ImageVector
    val darkmode: ImageVector
    val deleteBlack: ImageVector
    val deleteRed: ImageVector
    val disassembler: ImageVector
    val edit: ImageVector
    val export: ImageVector
    val fileCompiled: ImageVector
    val fileNotCompiled: ImageVector
    val forwards: ImageVector
    val home: ImageVector
    val import: ImageVector
    val info: ImageVector
    val lightmode: ImageVector
    val logo: ImageVector
    val pin: ImageVector
    val processor: ImageVector
    val processorBold: ImageVector
    val processorLight: ImageVector
    val recompile: ImageVector
    val refresh: ImageVector
    val reportBug: ImageVector
    val returnSubroutine: ImageVector
    val reverse: ImageVector
    val settings: ImageVector
    val singleExe: ImageVector
    val splitCells: ImageVector
    val statusError: ImageVector
    val statusFine: ImageVector
    val statusLoading: ImageVector
    val stepInto: ImageVector
    val stepMultiple: ImageVector
    val stepOut: ImageVector
    val stepOver: ImageVector
    val tag: ImageVector

    // Controls

    // Window Decorations
    val decrease: ImageVector
    val increase: ImageVector
    val close: ImageVector

    // File Tree Icons
    val folder: ImageVector
    val file: ImageVector
    val asmFile: ImageVector
    val chevronRight: ImageVector
    val chevronDown: ImageVector

    fun allIcons(): List<ImageVector> {
        return listOf(
            appLogo,
            add,
            autoscroll,
            backwards,
            bars,
            build,
            cancel,
            clearStorage,
            combineCells,
            continuousExe,
            console,
            darkmode,
            deleteBlack,
            deleteRed,
            disassembler,
            edit,
            export,
            fileCompiled,
            fileNotCompiled,
            forwards,
            home,
            import,
            info,
            lightmode,
            logo,
            pin,
            processor,
            processorBold,
            processorLight,
            recompile,
            refresh,
            reportBug,
            returnSubroutine,
            reverse,
            settings,
            singleExe,
            splitCells,
            statusError,
            statusFine,
            statusLoading,
            stepInto,
            stepMultiple,
            stepOut,
            stepOver,
            tag
        )
    }

    companion object {
        @Composable
        fun Icons(icons: List<ImageVector>) {
            icons.forEachIndexed { index, it ->
                Image(it, index.toString(), modifier = Modifier.size(UIState.Scale.value.SIZE_CONTROL_MEDIUM), colorFilter = ColorFilter.tint(UIState.Theme.value.COLOR_FG_0))
            }
        }
    }

}