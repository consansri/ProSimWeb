package cengine.project

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import uilib.resource.BenIcons

@Serializable
enum class ViewType(val icon: ImageVector) {
    IDE(BenIcons.console),
    EMU(BenIcons.processorLight);

    fun next(): ViewType {
        val length = ViewType.entries.size
        val currIndex = ViewType.entries.indexOf(this)
        val nextIndex = (currIndex + 1) % length
        return ViewType.entries[nextIndex]
    }


}