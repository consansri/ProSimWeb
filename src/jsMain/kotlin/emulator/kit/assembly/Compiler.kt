package emulator.kit.assembly

import emulator.kit.Settings
import emulator.kit.Architecture
import emulator.kit.common.FileHandler
import emulator.kit.common.RegContainer
import emulator.kit.common.Transcript
import emulator.kit.types.Variable
import emulator.kit.types.HTMLTools
import kotlin.time.measureTime

/**
 * The [Compiler] is the first instance which analyzes the text input. Common pre analyzed tokens will be delivered to each Syntax implementation. Also the [Compiler] fires the compilation events in the following order.
 *
 * 1. common analysis ([analyze])
 * 2. specific analysis ([parse] which uses the given logic from [syntax])
 * 3. highlight tokens ([highlight])
 * 4. convert syntax tree to binary ([assemble] which uses the given logic from [assembly])
 *
 * @param syntax gets an object of the architecture specific [Syntax]-Class implementation from the assembler configuration through the [Architecture].
 * @param assembly gets an object of the architecture specific [Assembly]-Class implementation from the assembler configuration through the [Architecture].
 * @param regexCollection contains the standard token regular expressions.
 * @param hlFlagCollection contains the standard token highlighting flags.
 *
 */
class Compiler(
    private val architecture: Architecture,
    private val syntax: Syntax,
    private val assembly: Assembly,
    private val regexCollection: RegexCollection,
    private val hlFlagCollection: HLFlagCollection
) {

    private var tokenList: MutableList<Token> = mutableListOf()
    private var tokenLines: MutableList<MutableList<Token>> = mutableListOf()
    private var dryLines: List<String>? = null
    private var hlLines: MutableList<String>? = null
    private var dryContent = ""
    private var syntaxTree: Syntax.SyntaxTree? = null
    private var isBuildable = false
    private var assemblyMap: Assembly.AssemblyMap = Assembly.AssemblyMap()

    private fun initCode(code: String) {
        tokenList = mutableListOf()
        tokenLines = mutableListOf()
        dryContent = code
        dryLines = dryContent.split(*Settings.LINEBREAKS.toTypedArray())
        hlLines = dryLines?.toMutableList()
    }

    fun isBuildable(): Boolean = isBuildable
    fun getAssemblyMap(): Assembly.AssemblyMap = assemblyMap
    fun setCode(code: String, shouldHighlight: Boolean): Boolean {
        initCode(code)

        architecture.getConsole().compilerInfo("building ...")
        val parseTime = measureTime {
            analyze()
            parse()
        }
        architecture.getConsole().compilerInfo("build\ttook ${parseTime.inWholeMicroseconds}µs\t(${if (isBuildable) "success" else "has errors"})")

        if (shouldHighlight) {
            val hlTime = measureTime {
                highlight()
            }
            architecture.getConsole().compilerInfo("highlight\ttook ${hlTime.inWholeMicroseconds}µs")
        }

        assemble()

        return isBuildable
    }

    fun reassemble() {
        assemble()
    }

    private fun analyze() {
        dryLines?.let {
            val file = architecture.getFileHandler().getCurrent()
            for (lineID in it.indices) {
                val line = it[lineID]
                val tempTokenList = mutableListOf<Token>()
                var remainingLine = line
                var startIndex = 0

                while (remainingLine.isNotEmpty()) {
                    val space = regexCollection.space.find(remainingLine)
                    if (space != null) {
                        tokenList += Token.Space(LineLoc(file, lineID, startIndex, startIndex + space.value.length), space.value, tokenList.size)
                        tempTokenList += Token.Space(LineLoc(file, lineID, startIndex, startIndex + space.value.length), space.value, tokenList.size)
                        startIndex += space.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val binary = regexCollection.binary.find(remainingLine)
                    if (binary != null) {
                        tokenList += Token.Constant.Binary(LineLoc(file, lineID, startIndex, startIndex + binary.value.length), binary.value, tokenList.size)
                        tempTokenList += Token.Constant.Binary(LineLoc(file, lineID, startIndex, startIndex + binary.value.length), binary.value, tokenList.size)
                        startIndex += binary.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val hex = regexCollection.hex.find(remainingLine)
                    if (hex != null) {
                        tokenList += Token.Constant.Hex(LineLoc(file, lineID, startIndex, startIndex + hex.value.length), hex.value, tokenList.size)
                        tempTokenList += Token.Constant.Hex(LineLoc(file, lineID, startIndex, startIndex + hex.value.length), hex.value, tokenList.size)
                        startIndex += hex.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val dec = regexCollection.dec.find(remainingLine)
                    if (dec != null) {
                        tokenList += Token.Constant.Dec(LineLoc(file, lineID, startIndex, startIndex + dec.value.length), dec.value, tokenList.size, syntax.decimalValueSize)
                        tempTokenList += Token.Constant.Dec(LineLoc(file, lineID, startIndex, startIndex + dec.value.length), dec.value, tokenList.size, syntax.decimalValueSize)
                        startIndex += dec.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val udec = regexCollection.udec.find(remainingLine)
                    if (udec != null) {
                        tokenList += Token.Constant.UDec(LineLoc(file, lineID, startIndex, startIndex + udec.value.length), udec.value, tokenList.size)
                        tempTokenList += Token.Constant.UDec(LineLoc(file, lineID, startIndex, startIndex + udec.value.length), udec.value, tokenList.size)
                        startIndex += udec.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val ascii = regexCollection.ascii.find(remainingLine)
                    if (ascii != null) {
                        tokenList += Token.Constant.Ascii(LineLoc(file, lineID, startIndex, startIndex + ascii.value.length), ascii.value, tokenList.size)
                        tempTokenList += Token.Constant.Ascii(LineLoc(file, lineID, startIndex, startIndex + ascii.value.length), ascii.value, tokenList.size)
                        startIndex += ascii.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val string = regexCollection.string.find(remainingLine)
                    if (string != null) {
                        tokenList += Token.Constant.String(LineLoc(file, lineID, startIndex, startIndex + string.value.length), string.value, tokenList.size)
                        tempTokenList += Token.Constant.String(LineLoc(file, lineID, startIndex, startIndex + string.value.length), string.value, tokenList.size)
                        startIndex += string.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val symbol = regexCollection.symbol.find(remainingLine)
                    if (symbol != null) {
                        tokenList += Token.Symbol(LineLoc(file, lineID, startIndex, startIndex + symbol.value.length), symbol.value, tokenList.size)
                        tempTokenList += Token.Symbol(LineLoc(file, lineID, startIndex, startIndex + symbol.value.length), symbol.value, tokenList.size)
                        startIndex += symbol.value.length
                        remainingLine = line.substring(startIndex)
                        continue
                    }

                    val regRes = regexCollection.alphaNumeric.find(remainingLine)
                    if (regRes != null) {
                        val reg = architecture.getRegContainer().getReg(regRes.value)
                        if (reg != null) {
                            tokenList += Token.Register(LineLoc(file, lineID, startIndex, startIndex + regRes.value.length), regRes.value, reg, tokenList.size)
                            tempTokenList += Token.Register(LineLoc(file, lineID, startIndex, startIndex + regRes.value.length), regRes.value, reg, tokenList.size)
                            startIndex += regRes.value.length
                            remainingLine = line.substring(startIndex)
                            continue
                        }
                    }

                    // apply rest
                    val alphaNumeric = regexCollection.alphaNumeric.find(remainingLine)
                    val word = regexCollection.word.find(remainingLine)

                    if (alphaNumeric != null && word != null) {
                        if (alphaNumeric.value.length == word.value.length) {
                            tokenList += Token.Word(LineLoc(file, lineID, startIndex, startIndex + word.value.length), word.value, tokenList.size)
                            tempTokenList += Token.Word(LineLoc(file, lineID, startIndex, startIndex + word.value.length), word.value, tokenList.size)
                            startIndex += word.value.length
                            remainingLine = line.substring(startIndex)
                            continue
                        } else {
                            tokenList += Token.AlphaNum(LineLoc(file, lineID, startIndex, startIndex + alphaNumeric.value.length), alphaNumeric.value, tokenList.size)
                            tempTokenList += Token.AlphaNum(LineLoc(file, lineID, startIndex, startIndex + alphaNumeric.value.length), alphaNumeric.value, tokenList.size)
                            startIndex += alphaNumeric.value.length
                            remainingLine = line.substring(startIndex)
                            continue
                        }
                    } else {
                        if (alphaNumeric != null) {
                            tokenList += Token.AlphaNum(LineLoc(file, lineID, startIndex, startIndex + alphaNumeric.value.length), alphaNumeric.value, tokenList.size)
                            tempTokenList += Token.AlphaNum(LineLoc(file, lineID, startIndex, startIndex + alphaNumeric.value.length), alphaNumeric.value, tokenList.size)
                            startIndex += alphaNumeric.value.length
                            remainingLine = line.substring(startIndex)
                            continue
                        }
                    }

                    architecture.getConsole().warn("Assembly: no match found for $remainingLine")
                    break;
                }
                tokenLines.add(lineID, tempTokenList)
                tokenList += Token.NewLine(LineLoc(file, lineID, line.length, line.length + 2), "\n", tokenList.size)
            }
        }
    }

    private fun parse() {
        architecture.getTranscript().clear()
        syntax.clear()
        architecture.getConsole().clear()
        architecture.getConsole().compilerInfo("building... ")
        syntaxTree = syntax.check(this, tokenLines, architecture.getFileHandler().getAllFiles().filter { it != architecture.getFileHandler().getCurrent() }, architecture.getTranscript())

        syntaxTree?.rootNode?.allWarnings?.let {
            for (warning in it) {
                if (warning.linkedTreeNode.getAllTokens().isNotEmpty()) {
                    if (warning.linkedTreeNode.getAllTokens().first().isPseudo()) {
                        architecture.getConsole().warn("pseudo: Warning ${warning.message}")
                    } else {
                        architecture.getConsole().warn("line ${warning.linkedTreeNode.getAllTokens().first().lineLoc.lineID + 1}: Warning ${warning.message}")
                    }
                } else {
                    architecture.getConsole().error("GlobalWarning: " + warning.message)
                }
            }
        }

        syntaxTree?.rootNode?.allErrors?.let {
            for (error in it) {
                if (error.linkedTreeNode.getAllTokens().isNotEmpty()) {
                    if (error.linkedTreeNode.getAllTokens().first().isPseudo()) {
                        architecture.getConsole().error("pseudo: Error ${error.message} \n[${error.linkedTreeNode.getAllTokens().joinToString(" ") { it.content }}]")
                    } else {
                        architecture.getConsole().error("line ${error.linkedTreeNode.getAllTokens().first().lineLoc.lineID + 1}: Error ${error.message} \n[${error.linkedTreeNode.getAllTokens().joinToString(" ") { it.content }}]")
                    }
                } else {
                    architecture.getConsole().error("GlobalError: " + error.message)
                }
            }
            isBuildable = it.isEmpty()
        }

    }

    private fun highlight() {

        for (lineID in tokenLines.indices) {
            val tokenLine = tokenLines[lineID]
            var hlLine = ""

            for (token in tokenLine) {
                if (syntaxTree?.rootNode != null) {
                    val node = syntaxTree?.contains(token)?.elementNode
                    if (node != null) {
                        val hlFlag = node.highlighting.getHLFlag(token)
                        if (hlFlag != null) {
                            token.hl(architecture, hlFlag, node.name)
                            hlLine += token.hlContent
                            continue
                        }
                    }
                    if (syntaxTree?.errorsContain(token) == true) {
                        token.hl(architecture, hlFlagCollection.error ?: "", "error")
                        hlLine += token.hlContent
                        continue
                    }
                }

                if (syntax.applyStandardHLForRest) {
                    when (token) {
                        is Token.AlphaNum -> {
                            hlFlagCollection.alphaNum?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Constant.Binary -> {
                            hlFlagCollection.const_bin?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Constant.Dec -> {
                            hlFlagCollection.const_dec?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Constant.Hex -> {
                            hlFlagCollection.const_hex?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Constant.UDec -> {
                            hlFlagCollection.const_udec?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Constant.Ascii -> {
                            hlFlagCollection.const_ascii?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Constant.String -> {
                            hlFlagCollection.const_ascii?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Register -> {
                            hlFlagCollection.register?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Symbol -> {
                            hlFlagCollection.symbol?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Word -> {
                            hlFlagCollection.word?.let {
                                token.hl(architecture, it)
                            }
                        }

                        is Token.Space -> {
                            hlFlagCollection.whitespace?.let {
                                token.hl(architecture, it)
                            }
                        }

                        else -> {

                        }
                    }
                } else {
                    token.hl(architecture, "")
                }

                hlLine += token.hlContent
            }

            hlLines?.let {
                it[lineID] = hlLine
            }
        }
    }

    private fun assemble() {
        architecture.getMemory().clear()
        architecture.getRegContainer().pc.reset()
        architecture.getTranscript().clear(Transcript.Type.DISASSEMBLED)

        if (isBuildable) {
            syntaxTree?.let {
                val assembleTime = measureTime {
                    assemblyMap = assembly.generateByteCode(architecture, it)
                }
                architecture.getConsole().compilerInfo("assembl\ttook ${assembleTime.inWholeMicroseconds}µs")


                val disassembleTime = measureTime {
                    assembly.generateTranscript(architecture, it)
                }
                architecture.getConsole().compilerInfo("disassembl\ttook ${disassembleTime.inWholeMicroseconds}µs")

                architecture.getFileHandler().getCurrent().linkGrammarTree(it)
            }
        } else {
            syntaxTree?.let {
                architecture.getFileHandler().getCurrent().linkGrammarTree(it)
            }
        }
    }

    fun pseudoAnalyze(content: String, lineID: Int = Settings.COMPILER_TOKEN_PSEUDOID): List<Token> {
        val tokens = mutableListOf<Token>()
        var remaining = content
        var startIndex = 0
        val file = architecture.getFileHandler().getCurrent()
        while (remaining.isNotEmpty()) {
            val space = regexCollection.space.find(remaining)
            if (space != null) {
                tokens += Token.Space(LineLoc(file, lineID, startIndex, startIndex + space.value.length), space.value, lineID)
                startIndex += space.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val binary = regexCollection.binary.find(remaining)
            if (binary != null) {
                tokens += Token.Constant.Binary(LineLoc(file, lineID, startIndex, startIndex + binary.value.length), binary.value, lineID)
                startIndex += binary.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val hex = regexCollection.hex.find(remaining)
            if (hex != null) {
                tokens += Token.Constant.Hex(LineLoc(file, lineID, startIndex, startIndex + hex.value.length), hex.value, lineID)
                startIndex += hex.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val dec = regexCollection.dec.find(remaining)
            if (dec != null) {
                tokens += Token.Constant.Dec(LineLoc(file, lineID, startIndex, startIndex + dec.value.length), dec.value, lineID, syntax.decimalValueSize)
                startIndex += dec.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val udec = regexCollection.udec.find(remaining)
            if (udec != null) {
                tokens += Token.Constant.UDec(LineLoc(file, lineID, startIndex, startIndex + udec.value.length), udec.value, lineID)
                startIndex += udec.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val ascii = regexCollection.ascii.find(remaining)
            if (ascii != null) {
                tokens += Token.Constant.Ascii(LineLoc(file, lineID, startIndex, startIndex + ascii.value.length), ascii.value, lineID)
                startIndex += ascii.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val string = regexCollection.string.find(remaining)
            if (string != null) {
                tokens += Token.Constant.String(LineLoc(file, lineID, startIndex, startIndex + string.value.length), string.value, lineID)
                startIndex += string.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val symbol = regexCollection.symbol.find(remaining)
            if (symbol != null) {
                tokens += Token.Symbol(LineLoc(file, lineID, startIndex, startIndex + symbol.value.length), symbol.value, lineID)
                startIndex += symbol.value.length
                remaining = content.substring(startIndex)
                continue
            }

            val regRes = regexCollection.alphaNumeric.find(remaining)
            if (regRes != null) {
                val reg = architecture.getRegContainer().getReg(regRes.value)
                if (reg != null) {
                    tokens += Token.Register(LineLoc(file, lineID, startIndex, startIndex + regRes.value.length), regRes.value, reg, lineID)
                    startIndex += regRes.value.length
                    remaining = content.substring(startIndex)
                    continue

                }
            }

            // apply rest
            val alphaNumeric = regexCollection.alphaNumeric.find(remaining)
            val word = regexCollection.word.find(remaining)

            if (alphaNumeric != null && word != null) {
                if (alphaNumeric.value.length == word.value.length) {
                    tokens += Token.Word(LineLoc(file, lineID, startIndex, startIndex + word.value.length), word.value, lineID)
                    startIndex += word.value.length
                    remaining = content.substring(startIndex)
                    continue
                } else {
                    tokens += Token.AlphaNum(LineLoc(file, lineID, startIndex, startIndex + alphaNumeric.value.length), alphaNumeric.value, lineID)
                    startIndex += alphaNumeric.value.length
                    remaining = content.substring(startIndex)
                    continue
                }
            } else {
                if (alphaNumeric != null) {
                    tokens += Token.AlphaNum(LineLoc(file, lineID, startIndex, startIndex + alphaNumeric.value.length), alphaNumeric.value, lineID)
                    startIndex += alphaNumeric.value.length
                    remaining = content.substring(startIndex)
                    continue
                }
            }

            architecture.getConsole().warn("Assembly.analyze($content): no match found for $remaining")
            break;
        }

        return tokens
    }

    fun getHLContent(): String {
        val stringBuilder = StringBuilder()
        hlLines?.let {
            for (line in it) {
                stringBuilder.append("$line\n")
            }
        }
        val hlContent = stringBuilder.toString()
        return if (hlContent.isNotEmpty()) {
            hlContent
        } else {
            dryContent
        }
    }

    fun getGrammarTree(): Syntax.SyntaxTree? = syntaxTree

    fun logTip(message: String, lineID: Int = -1) {
        if(lineID != -1){
            architecture.getConsole().log("--Compiler-Tip: $message")
        }else{
            architecture.getConsole().log("--Compiler-Tip: line ${lineID + 1} -> $message")
        }

    }

    sealed class Token(val lineLoc: LineLoc, val content: String, val id: Int) {

        var hlContent = content
        abstract val type: TokenType

        fun isPseudo(): Boolean {
            return id == Settings.COMPILER_TOKEN_PSEUDOID && lineLoc.lineID == Settings.COMPILER_TOKEN_PSEUDOID
        }

        fun hl(architecture: Architecture, hlFlag: String, title: String = "") {
            hlContent = architecture.highlight(HTMLTools.encodeHTML(content), id, title, hlFlag, "token")
        }

        override fun toString(): String = content

        class NewLine(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.NEWLINE
        }

        class Space(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.SPACE
        }

        class Symbol(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.SYMBOL
        }

        sealed class Constant(lineLoc: LineLoc, content: kotlin.String, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.CONSTANT
            abstract fun getValue(): Variable.Value

            class Ascii(lineLoc: LineLoc, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(): Variable.Value {
                    val binChars = StringBuilder()
                    val byteArray = content.substring(1, content.length - 1).encodeToByteArray()
                    for (byte in byteArray) {
                        val bin = byte.toInt().toString(2)
                        binChars.append(bin)
                    }
                    return Variable.Value.Bin(binChars.toString())
                }
            }

            class String(lineLoc: LineLoc, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(): Variable.Value {
                    val hexStr = StringBuilder()
                    val trimmedContent = content.substring(1, content.length - 1)
                    for (char in trimmedContent) {
                        val hexChar = char.code.toString(16)
                        hexStr.append(hexChar)
                    }
                    return Variable.Value.Hex(hexStr.toString())
                }
            }

            class Binary(lineLoc: LineLoc, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(): Variable.Value {
                    return if (content.contains('-')) -Variable.Value.Bin(content.trimStart('-'), Variable.Tools.getNearestSize(content.trimStart('-').removePrefix(Settings.PRESTRING_BINARY).length)) else Variable.Value.Bin(content)
                }
            }

            class Hex(lineLoc: LineLoc, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(): Variable.Value {
                    return if (content.contains('-')) -Variable.Value.Hex(content.trimStart('-'), Variable.Tools.getNearestSize(content.trimStart('-').removePrefix(Settings.PRESTRING_HEX).length * 4)) else Variable.Value.Hex(content)
                }
            }

            class Dec(lineLoc: LineLoc, content: kotlin.String, id: Int, val size: Variable.Size) : Constant(lineLoc, content, id) {
                override fun getValue(): Variable.Value {
                    return Variable.Value.Dec(content, size)
                }
            }

            class UDec(lineLoc: LineLoc, content: kotlin.String, id: Int) : Constant(lineLoc, content, id) {
                override fun getValue(): Variable.Value {
                    return Variable.Value.UDec(content)
                }
            }

        }

        class Register(lineLoc: LineLoc, content: String, val reg: RegContainer.Register, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.REGISTER
        }

        class AlphaNum(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.ALPHANUM
        }

        class Word(lineLoc: LineLoc, content: String, id: Int) : Token(lineLoc, content, id) {
            override val type = TokenType.WORD
        }
    }

    enum class TokenType {
        SPACE,
        NEWLINE,
        SYMBOL,
        CONSTANT,
        REGISTER,
        ALPHANUM,
        WORD
    }

    data class HLFlagCollection(
        val alphaNum: String? = null,
        val word: String? = null,
        val const_hex: String? = null,
        val const_bin: String? = null,
        val const_dec: String? = null,
        val const_udec: String? = null,
        val const_ascii: String? = null,
        val const_string: String? = null,
        val register: String? = null,
        val symbol: String? = null,
        val instruction: String? = null,
        val comment: String? = null,
        val whitespace: String? = null,
        val error: String? = null
    )

    data class RegexCollection(
        val space: Regex,
        val symbol: Regex,
        val binary: Regex,
        val hex: Regex,
        val dec: Regex,
        val udec: Regex,
        val ascii: Regex,
        val string: Regex,
        val alphaNumeric: Regex,
        val word: Regex,
    )

    data class LineLoc(val file: FileHandler.File, var lineID: Int, val startIndex: Int, val endIndex: Int)
    // endIndex means index after last Character

}