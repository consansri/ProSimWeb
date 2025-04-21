package cengine.psi.core

import cengine.console.IOContext
import cengine.console.SysOut
import cengine.psi.elements.PsiFile
import cengine.vfs.VirtualFile

/**
 * Language-specific parser interface
 */
interface PsiParser {

    suspend fun parse(file: VirtualFile, ioContext: IOContext = SysOut): PsiFile

}