package cengine.console

data object SysOut: IOContext {
    override fun stream(message: String) {
        print(message)
    }
}


