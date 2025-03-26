package cengine.lang.asm.ast.lexer

import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.RegTypeInterface
import cengine.lang.asm.ast.TargetSpec
import cengine.psi.lexer.core.Token
import cengine.psi.lexer.impl.BaseLexer

class AsmLexer(input: String, private val targetSpec: TargetSpec<*>) : BaseLexer(input) {

    private val prefices: Prefices get() = targetSpec.prefices
    private val regs: List<Pair<RegTypeInterface, Set<String>>> = targetSpec.allRegs.map { it to it.recognizable.toSet() }
    private val instrs = targetSpec.allInstrs.map { it to it.detectionName.lowercase() }
    private val dirs: List<Pair<DirTypeInterface, String>> = targetSpec.allDirs.map { it to it.getDetectionString().lowercase() }.filter { it.second.isNotEmpty() }
    private val regexMap: Map<AsmTokenType, Regex?> = AsmTokenType.entries.associateWith { it.getRegex(prefices) }

    /**
     * For Caching the peeked token at a certain Index
     */
    private var peeked: Pair<Int, Token>? = null

    override fun peek(ignoreLeadingSpaces: Boolean, ignoreComments: Boolean): AsmToken {
        peeked?.let {
            if (it.first == position) return it.second as AsmToken
        }
        val initialPos = position
        val token = consume(ignoreLeadingSpaces, ignoreComments)
        peeked = initialPos to token
        position = initialPos
        return token
    }

    override fun consume(ignoreLeadingSpaces: Boolean, ignoreComments: Boolean): AsmToken {
        val regs = regs

        if (!hasMoreTokens()) {
            return AsmToken(AsmTokenType.EOF, "", position..<position)
        }

        if (ignoreComments) {
            val token = peek(ignoreLeadingSpaces, false)
            if (token.type.isComment()) {
                ignored.add(consume(ignoreLeadingSpaces, false))
            }
        }

        if (!hasMoreTokens()) {
            return AsmToken(AsmTokenType.EOF, "", position..<position)
        }

        if (ignoreLeadingSpaces) skipSpaces()

        if (!hasMoreTokens()) {
            return AsmToken(AsmTokenType.EOF, "", position..<position)
        }

        for (type in AsmTokenType.entries) {

            val regex = regexMap[type] ?: continue
            //val contentToMatch = input.substring(index)
            val match = try {
                regex.matchAt(input, index) ?: continue
            } catch (e: Exception) {
                continue
            }

            //regex.find(input, index) ?: continue

            val matchedText = match.value
            val startPosition = position

            advance(matchedText.length)

            val endPosition = position

            if (type == AsmTokenType.SYMBOL) {
                // Check directives

                if (matchedText.startsWith(".")) {
                    val dirType = dirs.firstOrNull { "." + it.second == matchedText.lowercase() }
                    if (dirType != null) {
                        return AsmToken(
                            AsmTokenType.DIRECTIVE,
                            matchedText,
                            startPosition..<endPosition
                        )
                    }
                }

                // check for instruction
                val instrType = instrs.firstOrNull { it.second == matchedText.lowercase() }
                if (instrType != null) {
                    return AsmToken(
                        AsmTokenType.INSTRNAME,
                        matchedText,
                        startPosition..<endPosition
                    )
                }

                val regType = regs.firstOrNull { it.second.contains(matchedText.lowercase()) }
                if (regType != null) {
                    return AsmToken(
                        AsmTokenType.REGISTER,
                        matchedText,
                        startPosition..<endPosition
                    )
                }
            }

            val onlyNumber = when (type) {
                AsmTokenType.INT_BIN -> matchedText.removePrefix(prefices.bin)
                AsmTokenType.INT_HEX -> matchedText.removePrefix(prefices.hex)
                AsmTokenType.INT_OCT -> matchedText.removePrefix(prefices.oct)
                AsmTokenType.INT_DEC -> matchedText.removePrefix(prefices.dec)
                else -> null
            }

            val token = AsmToken(
                type,
                matchedText,
                startPosition..<endPosition,
                onlyNumber
            )

            return token
        }

        //throw Lexer.InvalidCharException(peekChar(), index)
        val char = input[index].toString()
        val start = index
        advance()
        val token = AsmToken(AsmTokenType.UNDEFINED, char, start..<index)
        return token
        //SysOut.error("InvalidTokenException $token $index")
    }

    interface Prefices {
        val hex: String
        val oct: String
        val bin: String
        val dec: String
        val comment: String
        val symbol: Regex
    }
}