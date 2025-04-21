package emulator.kit.common

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

interface ExecutionStage {

    val infos: List<Information>

    interface Information {
        var value: MutableState<String>
    }

    open class Message(initial: String) {
        var value by mutableStateOf(initial)
    }
}