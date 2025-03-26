
import cengine.editor.text.RopeModel
import cengine.editor.text.StringModel
import cengine.editor.text.TextModel

import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.measureTime
import kotlin.time.measureTimedValue


class TestCEngine {

    private val RANDOM_STRING_RANGE = 1..5
    private val REALISTIC_STRINGS = true

    @Test
    fun testTextModel() {
        val inital = "Hello World\nI like you!\n".repeat(10000)
        val models = StringModel(inital) to RopeModel(inital)

        repeat(10) {
            models.compareInsert()
            models.compareSubstring()
        }

        models.compareLCatIndexBounds()
        models.compareIndexAtLCBounds()

        repeat(10){
            models.compareLCatIndex()
            models.compareIndexAtLC()
            models.compareFindAllOccurrences()
        }
        repeat(10) {
            models.compareDeletes()
            models.compareCharAt()
        }
    }

    private fun Pair<TextModel, TextModel>.compareFindAllOccurrences(){
        val searchString = getRandomString()

        val (fValue, fTime) = measureTimedValue {
            first.findAllOccurrences(searchString)
        }
        val (sValue, sTime) = measureTimedValue {
            second.findAllOccurrences(searchString)
        }
        SysOut.log(
            "FindAllOccurences: (search: $searchString) $sValue == $fValue" +
                    "\n\t${first::class.simpleName}\t\t: ${fTime.inWholeNanoseconds}ns" +
                    "\n\t${second::class.simpleName}\t\t: ${sTime.inWholeNanoseconds}ns"
        )
        assertEquals(fValue, sValue)

    }

    private fun Pair<TextModel,TextModel>.compareLCatIndexBounds(){
        val range = 0..<this.first.length

        val (fValue, fTime) = measureTimedValue {
            first.getLineAndColumn(1)
        }
        val (sValue, sTime) = measureTimedValue {
            second.getLineAndColumn(1)
        }
        SysOut.log(
            "BoundsOf LC At Index: $sValue == $fValue" +
                    "\n\t${first::class.simpleName}\t\t: ${fTime.inWholeNanoseconds}ns" +
                    "\n\t${second::class.simpleName}\t\t: ${sTime.inWholeNanoseconds}ns"
        )
        assertEquals(fValue, sValue)

    }

    private fun Pair<TextModel,TextModel>.compareIndexAtLCBounds(){
        val range = 0..<this.first.length

        val (fValue, fTime) = measureTimedValue {
            first.indexOf(0,0)
        }
        val (sValue, sTime) = measureTimedValue {
            second.indexOf(0,0)
        }
        SysOut.log(
            "BoundsOf Index At LC: $fValue == $sValue" +
                    "\n\t${first::class.simpleName}\t\t: ${fTime.inWholeNanoseconds}ns" +
                    "\n\t${second::class.simpleName}\t\t: ${sTime.inWholeNanoseconds}ns"
        )
        assertEquals(fValue, sValue)

    }

    private fun Pair<TextModel, TextModel>.compareInsert() {
        val range = 0..<this.first.length
        val index = Random.nextInt(range)

        val string = getRandomString()
        val fTime = measureTime {
            first.insert(index, string)
        }
        val sTime = measureTime {
            second.insert(index, string)
        }
        SysOut.log(
            "CompareInserts: " +
                    "\n\t${first::class.simpleName}\t\t: ${fTime.inWholeNanoseconds}ns" +
                    "\n\t${second::class.simpleName}\t\t: ${sTime.inWholeNanoseconds}ns"
        )
        assertEquals(first.toString(), second.toString())

    }

    private fun Pair<TextModel, TextModel>.compareDeletes() {
        val maxlength = this.first.length
        val start = Random.nextInt(0, maxlength - 1)
        val end = Random.nextInt(start, maxlength)

        val fTime = measureTime {
            first.delete(start, end)
        }
        val sTime = measureTime {
            second.delete(start, end)
        }
        SysOut.log(
            "CompareDeletes: $end in 0..$maxlength" +
                    "\n\t${first::class.simpleName}\t\ttook ${fTime.inWholeNanoseconds}ns" +
                    "\n\t${second::class.simpleName}\t\ttook ${sTime.inWholeNanoseconds}ns"
        )
        assertEquals(first.toString(), second.toString())

    }

    private fun Pair<TextModel, TextModel>.compareSubstring() {
        val maxlength = this.first.length
        val start = Random.nextInt(0, maxlength - 1)
        val end = Random.nextInt(start, maxlength)

        val fSub: String
        val fTime = measureTime {
            fSub = first.substring(start, end)
        }
        val sSub: String
        val sTime = measureTime {
            sSub = second.substring(start, end)
        }
        SysOut.log("CompareSubstring: $end in 0..$maxlength " +
                "\n\t${first::class.simpleName}\t\ttook ${fTime.inWholeNanoseconds}ns" +
                "\n\t${second::class.simpleName}\t\ttook ${sTime.inWholeNanoseconds}ns")
        assertEquals(fSub, sSub)

    }

    private fun Pair<TextModel, TextModel>.compareCharAt() {
        val maxlength = this.first.length
        val start = Random.nextInt(0, maxlength - 1)

        val fChar: Char
        val fTime = measureTime {
            fChar = first.charAt(start)
        }
        val sChar: Char
        val sTime = measureTime {
            sChar = second.charAt(start)
        }
        SysOut.log("CompareCharAt: $start in 0..$maxlength " +
                "\n\t${first::class.simpleName}\t\ttook ${fTime.inWholeNanoseconds}ns" +
                "\n\t${second::class.simpleName}\t\ttook ${sTime.inWholeNanoseconds}ns")
        assertEquals(fChar, sChar)

    }

    private fun Pair<TextModel, TextModel>.compareIndexAtLC() {
        val line = Random.nextInt(0..<this.first.lines)
        val col = Random.nextInt(0..<256)

        val fIndex: Int
        val fTime = measureTime {
            fIndex = first.indexOf(line, col)
        }
        val sIndex: Int
        val sTime = measureTime {
            sIndex = second.indexOf(line, col)
        }
        SysOut.log("CompareIndexAtLC: Index: $fIndex pointsOn: ${first.charAt(fIndex)}" +
                "\n\t${first::class.simpleName}\t\ttook ${fTime.inWholeNanoseconds}ns" +
                "\n\t${second::class.simpleName}\t\ttook ${sTime.inWholeNanoseconds}ns")
        assertEquals(fIndex, sIndex)
    }

    private fun Pair<TextModel, TextModel>.compareLCatIndex() {
        val index = Random.nextInt(0..this.first.length)

        val fLC: Pair<Int, Int>
        val fTime = measureTime {
            fLC = first.getLineAndColumn(index)
        }
        val sLC: Pair<Int, Int>
        val sTime = measureTime {
            sLC = second.getLineAndColumn(index)
        }
        SysOut.log("CompareLCAtIndex: Line: ${fLC.first} Col: ${fLC.second} pointsOn: ${first.charAt(index)} " +
                "\n\t${first::class.simpleName}\t\ttook ${fTime.inWholeNanoseconds}ns" +
                "\n\t${second::class.simpleName}\t\ttook ${sTime.inWholeNanoseconds}ns")
        assertEquals(fLC, sLC)
    }

    private fun getRandomString(): String {
        val size = Random.nextInt(RANDOM_STRING_RANGE)
        return if(REALISTIC_STRINGS){
            generateRandomString(size)
        }else{
            (1..size).map { getRandomChar() }.joinToString("")
        }
    }

    private fun getRandomChar(): Char {
        val minCodePoint = 0
        val maxCodePoint = 0xFFFF

        val randomCodePoint = Random.nextInt(minCodePoint, maxCodePoint + 1)
        return randomCodePoint.toChar()
    }



    private fun generateRandomString(length: Int, charset: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 \n"): String {
        require(length >= 0) { "Length must be non-negative" }
        require(charset.isNotEmpty()) { "Charset must not be empty" }

        return (1..length)
            .map { charset[Random.nextInt(0, charset.length)] }
            .joinToString("")
    }


}


