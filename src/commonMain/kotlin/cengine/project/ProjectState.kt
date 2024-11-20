package cengine.project

import cengine.lang.asm.ast.TargetSpec
import cengine.vfs.FPath
import kotlinx.serialization.Serializable
import ui.EmulatorContentView
import ui.ToolContentType
import ui.ViewType

@Serializable
data class ProjectState(
    val absRootPath: String,
    val target: String,
    private var _viewType: ViewType = ViewType.IDE,
    val ide: IDEViewState = IDEViewState(),
    val emu: EMUViewState = EMUViewState(),
) {
    var viewType: ViewType
        get() = _viewType
        set(value) {
            _viewType = value
            ProjectStateManager.projectStateChanged()
        }

    @Serializable
    data class IDEViewState(
        private var _openFiles: List<FPath> = emptyList(),
        private var _leftContent: ToolContentType? = null,
        private var _rightContent: ToolContentType? = null,
        private var _bottomContent: ToolContentType? = null,
        private var _leftWidth: Float = 200f,
        private var _bottomHeight: Float = 200f,
        private var _rightWidth: Float = 200f
    ) {
        var openFiles: List<FPath>
            get() = _openFiles
            set(value) {
                _openFiles = value
                ProjectStateManager.projectStateChanged()
            }
        var leftContent: ToolContentType?
            get() = _leftContent
            set(value) {
                _leftContent = value
                ProjectStateManager.projectStateChanged()
            }
        var rightContent: ToolContentType?
            get() = _rightContent
            set(value) {
                _rightContent = value
                ProjectStateManager.projectStateChanged()
            }
        var bottomContent: ToolContentType?
            get() = _bottomContent
            set(value) {
                _bottomContent = value
                ProjectStateManager.projectStateChanged()
            }
        var leftWidth: Float
            get() = _leftWidth
            set(value) {
                _leftWidth = value
                ProjectStateManager.projectStateChanged()
            }
        var bottomHeight: Float
            get() = _bottomHeight
            set(value) {
                _bottomHeight = value
                ProjectStateManager.projectStateChanged()
            }
        var rightWidth: Float
            get() = _rightWidth
            set(value) {
                _rightWidth = value
                ProjectStateManager.projectStateChanged()
            }
    }

    @Serializable
    data class EMUViewState(
        private var _objFilePath: FPath? = null,
        private var _leftContent: EmulatorContentView? = null,
        private var _bottomContent: EmulatorContentView? = null,
        private var _rightContent: EmulatorContentView? = null,
        private var _leftWidth: Float = 200f,
        private var _bottomHeight: Float = 200f,
        private var _rightWidth: Float = 200f
    ) {
        var objFilePath: FPath?
            get() = _objFilePath
            set(value) {
                _objFilePath = value
                ProjectStateManager.projectStateChanged()
            }
        var leftContent: EmulatorContentView?
            get() = _leftContent
            set(value) {
                _leftContent = value
                ProjectStateManager.projectStateChanged()
            }
        var rightContent: EmulatorContentView?
            get() = _rightContent
            set(value) {
                _rightContent = value
                ProjectStateManager.projectStateChanged()
            }
        var bottomContent: EmulatorContentView?
            get() = _bottomContent
            set(value) {
                _bottomContent = value
                ProjectStateManager.projectStateChanged()
            }
        var leftWidth: Float
            get() = _leftWidth
            set(value) {
                _leftWidth = value
                ProjectStateManager.projectStateChanged()
            }
        var bottomHeight: Float
            get() = _bottomHeight
            set(value) {
                _bottomHeight = value
                ProjectStateManager.projectStateChanged()
            }
        var rightWidth: Float
            get() = _rightWidth
            set(value) {
                _rightWidth = value
                ProjectStateManager.projectStateChanged()
            }
    }

    fun getTarget(): TargetSpec<*>? = TargetSpec.specs.firstOrNull { it.name == target }
}
