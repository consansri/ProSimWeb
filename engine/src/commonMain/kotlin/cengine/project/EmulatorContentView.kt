package cengine.project

import kotlinx.serialization.Serializable

@Serializable
enum class EmulatorContentView {
    ObjFileSelection,
    ArchOverview,
    RegView,
    MemView
}