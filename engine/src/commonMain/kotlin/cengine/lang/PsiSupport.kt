package cengine.lang

import cengine.editor.annotation.AnnotationProvider
import cengine.editor.completion.CompletionProvider
import cengine.editor.highlighting.HLInfo
import cengine.editor.highlighting.HighlightProvider
import cengine.psi.core.Interval
import cengine.psi.core.PsiFileTypeDef
import cengine.psi.elements.PsiFile
import cengine.psi.lexer.PsiLexer
import cengine.psi.lexer.PsiLexerSet
import cengine.psi.parser.PsiTreeParser
import cengine.psi.style.CodeStyle

data class PsiSupport(
    val lexerSet: PsiLexerSet,
    val psiTreeParser: PsiTreeParser,
    val psiFileType: PsiFileTypeDef = PsiFile,
    val completionProvider: CompletionProvider? = null,
    val annotationProvider: AnnotationProvider? = null,
    val highlightProvider: HighlightProvider? = object : HighlightProvider {

        override fun fastHighlight(text: String, inRange: IntRange): List<HLInfo> {
            val lexer = PsiLexer(text, lexerSet, inRange)
            val tokens = lexer.tokenize()
            val highlights = tokens.mapNotNull {
                val style = it.type.style
                if (style != null) {
                    HL(it, style)
                } else null
            }
            return highlights
        }

    },
){
    data class HL(val element: Interval, val style: CodeStyle) : HLInfo {
        override val range: IntRange
            get() = element.range
        override val color: Int get() = style.getDarkElseLight()
        override fun toString(): String {
            return "<$range:${style.name}>"
        }
    }
}