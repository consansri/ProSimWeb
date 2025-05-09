package cengine.lang.asm.psi

import cengine.lang.asm.AsmTreeParser
import cengine.psi.elements.PsiStatement
import cengine.psi.parser.PsiBuilder

interface AsmDirectiveT : PsiStatement.PsiStatementTypeDef {

    val keyWord: String
    override val typeName: String get() = "dir('$keyWord')"

    /**
     * Parses the arguments/parameters following a specific directive token.
     * @param marker Already consumed the directive token.
     * @return True if parsing succeeded, false otherwise.
     */
    fun PsiBuilder.parse(asmTreeParser: AsmTreeParser, marker: PsiBuilder.Marker): Boolean

}