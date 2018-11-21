package sk.vildibald.text.classification.data

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import sk.vildibald.text.classification.data.entities.BtcPrice
import sk.vildibald.text.classification.data.entities.News
import sk.vildibald.text.classification.util.toLocalDate
import java.io.FileInputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExcelReader {
    fun readBtcPrices(filepath: String, dateColumnNumber: Int, priceColumnNumber: Int):
            List<BtcPrice> = asSheet(filepath)
                    .drop(1)
                    .map {
                        BtcPrice(it.getCell(dateColumnNumber).dateCellValue.toLocalDate(),
                                it.getCell(priceColumnNumber).numericCellValue)
                    }

    fun readNews(filepath: String): List<News> {
        //Row index specifies the row in the worksheet (starting at 0):
        //Cell index specifies the column within the chosen row (starting at 0):
        val categoryColumnNumber = 0
        val dateColumnNumber = 1
        val snippetColumnNumber = 2
        val contentColumnNumber = 3

        val timeFormatter = DateTimeFormatter.ofPattern("MM.dd.yyyy")

        return asSheet(filepath).drop(1).filter { it.count() == 4 }.map {
            val localDate = try {
                it.getCell(dateColumnNumber).dateCellValue.toLocalDate()
            } catch (e: IllegalStateException) {
                val dateString = it.getCell(dateColumnNumber).stringCellValue
                val temporalAccessor = timeFormatter.parse(dateString)
                LocalDate.from(temporalAccessor)
            }
            val content = try {
                it.getCell(contentColumnNumber).stringCellValue
            } catch (e: IllegalStateException) {
                ""
            }
            News(it.getCell(categoryColumnNumber)?.stringCellValue ?: "",
                    localDate,
                    it.getCell(snippetColumnNumber)?.stringCellValue ?: "",
                    content)
        }
    }

    private fun asSheet(filepath: String): Sheet =
            WorkbookFactory.create(FileInputStream(filepath)).getSheetAt(0)
}