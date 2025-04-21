package ui.ide.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import cengine.console.SysOut


@Composable
fun HeadedColumn(
    rowIndexes: List<Int>,
    rowHeaderRenderer: @Composable (lineID: Int) -> Unit,
    rowContentRenderer: @Composable (lineID: Int) -> Unit,
    modifier: Modifier = Modifier
) {

    val lineHeaders = @Composable {
        rowIndexes.forEach {
            rowHeaderRenderer(it)
        }
    }

    val lineContents = @Composable {
        rowIndexes.forEach {
            rowContentRenderer(it)
        }
    }

    Layout(
        contents = listOf(lineHeaders, lineContents),
        modifier = modifier
    ) { (lineHeaderMeasurables, lineContentMeasurables), constraints ->

        val rowHeaderPlaceables = lineHeaderMeasurables.map {
            it.measure(constraints)
        }

        val rowContentPlaceables = lineContentMeasurables.map {

            it.measure(constraints)
        }

        val rowHeaderWidth = rowHeaderPlaceables.maxOfOrNull { it.width } ?: 0
        val rowContentWidth = rowContentPlaceables.maxOfOrNull { it.width } ?: 0

        val totalWidth = rowHeaderWidth + rowContentWidth
        val totalHeight = rowContentPlaceables.sumOf { it.height }

        layout(width = totalWidth, height = totalHeight) {
            val xPosition = 0
            var yPosition = 0

            SysOut.log(
                """
                Width: $totalWidth, Height: $totalHeight
                RowHeaderWidth: $rowHeaderWidth, RowContentWidth: $rowContentWidth
                
                RowContents: ${rowContentPlaceables.joinToString(",") { "Place: ${it.width}x${it.height}" }}
                RowHeaders: ${rowHeaderPlaceables.joinToString { "Place: ${it.width}x${it.height}" }}
            """.trimIndent()
            )

            for (i in rowHeaderPlaceables.indices) {
                val header = rowHeaderPlaceables[i]
                val content = rowContentPlaceables[i]

                header.place(xPosition, yPosition)
                content.place(xPosition + rowHeaderWidth, yPosition)
                yPosition += content.height
            }
        }
    }
}