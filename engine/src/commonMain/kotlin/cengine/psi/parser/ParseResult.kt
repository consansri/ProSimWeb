package cengine.psi.parser

import cengine.psi.core.PsiFileTypeDef
import cengine.psi.lexer.PsiToken

// Data class to hold the results of the PsiBuilder run
data class ParseResult(
    val psiFileType: PsiFileTypeDef,
    val tokens: List<PsiToken>,
    val completedMarkers: List<CompletedMarkerInfo>,
    val errors: List<IndexedParseError>,
)
