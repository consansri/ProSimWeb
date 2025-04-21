package cengine.lang.asm.features

import cengine.editor.highlighting.HLInfo
import cengine.editor.highlighting.HighlightProvider
import cengine.lang.asm.AsmSpec
import cengine.psi.core.Interval
import cengine.psi.core.PsiElement
import cengine.psi.lexer.PsiLexer
import cengine.psi.style.CodeStyle

class AsmHighlighter(spec: AsmSpec<*>) : HighlightProvider {
    private val cache = mutableMapOf<PsiElement, List<HLInfo>>()

    private val lexerSet = spec.createLexerSet()

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

    data class HL(val element: Interval, val style: CodeStyle) : HLInfo {
        override val range: IntRange
            get() = element.range
        override val color: Int get() = style.getDarkElseLight()
        override fun toString(): String {
            return "<$range:${style.name}>"
        }
    }
}