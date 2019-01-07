package sk.vildibald.text.classification

import sk.vildibald.text.classification.data.ExcelReader
import sk.vildibald.text.classification.data.detectPeaks
import sk.vildibald.text.classification.data.entities.Category
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate


const val DATASET = "news.txt"
const val SAVED_MODEL_NAME = "newsModel.model"
const val EXCEL_NEWS = "CCNNewsLatest.xlsx"
const val EXCEL_PRICES = "CMCHistoricalDataLatest.xlsx"
const val DATE_COLUMN_EXCEL = 0
const val PRICE_COLUMN_EXCEL = 4

//const val EXCEL_NEWS = "CCNNews.xlsx"

// delete SAVED_MODEL_NAME file before modifying this
const val DATASET_MULTIPLY_TRAINING_FACTOR = 15
// delete DATASET and SAVED_MODEL_NAME files before modifying this
const val DAYS_DIFF = 1L

val FROM_DATE = LocalDate.parse("2017-04-04")


//val testQuery = "Trump says: I will make America great again by banning Bitcoin. Hell yeah!"
//val testQuery = "European Central Bank will adopt Bitcoin as its main currency. You can throw your current cash to trash"
//val testQuery = "From new year on, wages in Microsoft will be payed in Bitcoin"
//val testQuery = "The new miner rush has started. GPUs are now expensive as hell"

// 25.10.2018 20:00
// Classification: Increase, Probability: 0,91
val testQuery = "This week, the US stock market deleted all of its gains made in 2018 amidst a " +
        "major sell-off. The crash had no impact on the crypto market, showing no signs of inverse correlation. " +
        "Possibly affected by the trade war between China and the US along with the increase in " +
        "Fed rates, the US stock market suffered one of its worst short-term crashes in recent history. " +
        "Analysts stated that Asia markets are more vulnerable than the US if the stock market " +
        "crash of the US is to intensify, given the gradual decline in the growth rate of China’s economy. " +
        "“Then, there are worries about what’s happening abroad. China, the world’s " +
        "second-biggest economy, is showing slower growth. Last week, it reported economic growth of 6.5 percent in the third quarter, falling short of expectations. And it has been drawn into a trade dispute with the U.S., with each side digging in on tariffs on billions of dollars of each other’s imports,” CNBC investing editor reported. "
/////////////////////////////////

fun main(args: Array<String>) {
    if (!File(DATASET).exists())
        prepareDataset(EXCEL_PRICES, EXCEL_NEWS)

    if (File(SAVED_MODEL_NAME).exists())
        loadModel()
    else
        trainModelAndSave()

}

fun loadModel() {
    val classifier = TextClassifier.fromModel(SAVED_MODEL_NAME)
    test(classifier)
}

fun trainModelAndSave() {
    val file = File(DATASET)
    val trainer = ClassifierTrainer()
    val classifier = trainer.train(file, DATASET_MULTIPLY_TRAINING_FACTOR)
    classifier.saveModel(SAVED_MODEL_NAME)
    test(classifier)
}

private fun test(classifier: TextClassifier) {
    val (category, probability) = classifier.classify(testQuery)
    println("Q: $testQuery")
    println("Classification: $category")
    println("Probability: ${String.format("%.2f", probability)}")
}

private fun prepareDataset(pricesFilePath: String, newsFilePath: String) {
    val datasetFile = File(DATASET)
    if (datasetFile.exists()) return

    val excelReader = ExcelReader()


    val news = excelReader.readNews(newsFilePath)
            .asSequence()
            .sortedBy { it.date }
            .filter { it.date > FROM_DATE }
            .toList()
    val prices = excelReader.readBtcPrices(pricesFilePath, DATE_COLUMN_EXCEL, PRICE_COLUMN_EXCEL)
            .asSequence()
            .sortedBy { it.date }
            .filter { it.date > FROM_DATE }
            .toList()

    val pricePeaks = prices.detectPeaks()
    val peakIndices = pricePeaks.peaks.map { it.index }
    val valleyIndices = pricePeaks.valleys.map { it.index }

    val datasetLines = prices
            .filterIndexed { index, _ -> index in valleyIndices }
            .flatMap { valley ->
                news.asSequence().filter {
                    it.date == valley.date
//                    it.date.isBefore(valley.date.plusDays(1)) &&
//                            it.date.isAfter(valley.date.minusDays(DAYS_DIFF))
                    //                    it.date.isBefore(valley.date.plusDays(4)) &&
                    //                            it.date.isAfter(valley.date)
                }.map {
                    "${Category.INCREASE}\t" +
                            "${it.snippet.replace("\t", " ").replace("\n", " ")} " +
                            it.content.replace("\t", " ").replace("\n", " ")
                }.toList()
            } + prices
            .filterIndexed { index, _ -> index in peakIndices }
            .flatMap { peak ->
                news.asSequence().filter {
                    it.date == peak.date
//                    it.date.isBefore(peak.date.plusDays(1)) &&
//                            it.date.isAfter(peak.date.minusDays(DAYS_DIFF))
                    //                    it.date.isBefore(peak.date.plusDays(4)) &&
                    //                            it.date.isAfter(peak.date)
                }.map {
                    "${Category.DECREASE}\t" +
                            "${it.snippet.replace("\t", " ").replace("\n", " ")} " +
                            it.content.replace("\t", " ").replace("\n", " ")
                }.toList()
            }

    Files.write(Paths.get(DATASET), datasetLines)
}