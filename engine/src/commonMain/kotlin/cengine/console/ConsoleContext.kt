package cengine.console

import cengine.vfs.VirtualFile

interface ConsoleContext: IOContext {

    var directory: VirtualFile

}