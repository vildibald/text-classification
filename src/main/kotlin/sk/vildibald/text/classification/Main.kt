package sk.vildibald.text.classification

import sk.vildibald.text.classification.data.ExcelReader
import sk.vildibald.text.classification.data.detectPeaks
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths


val dataset = "news.txt"
val savedModelName = "newsModel.model"

//val testQuery = "Trump says: I will make America great again by banning Bitcoin. Hell yeah!"
//val testQuery = "European Central Bank will adopt Bitcoin as its main currency. You can throw your current cash to trash"
//val testQuery = "From new year on, wages in Microsoft will be payed in Bitcoin"
//val testQuery = "The new miner rush has started. GPUs are now expensive as hell"

// 25.10.2018 20:00
// Classification: Decrease, Probability: 0,88
val testQuery = "This week, the US stock market deleted all of its gains made in 2018 amidst a " +
        "major sell-off. The crash had no impact on the crypto market, showing no signs of inverse correlation. " +
        "Possibly affected by the trade war between China and the US along with the increase in " +
        "Fed rates, the US stock market suffered one of its worst short-term crashes in recent history. " +
        "Analysts stated that Asia markets are more vulnerable than the US if the stock market " +
        "crash of the US is to intensify, given the gradual decline in the growth rate of China’s economy. " +
        "“Then, there are worries about what’s happening abroad. China, the world’s " +
        "second-biggest economy, is showing slower growth. Last week, it reported economic growth of 6.5 percent in the third quarter, falling short of expectations. And it has been drawn into a trade dispute with the U.S., with each side digging in on tariffs on billions of dollars of each other’s imports,” CNBC investing editor reported. "
/////////////////////////////////

const val PRICE_INCREASE_CATEGORY = "Increase"
const val PRICE_DECREASE_CATEGORY = "Decrease"

/////////////////////////////////

fun main(args: Array<String>) {
    if (!File(dataset).exists()) {
        val pricesFile = "CMCHistoricalData.xlsx"
        val newsFile = "CCNNews.xlsx"
        prepareDataset(pricesFile, newsFile)
    }
    if (File(savedModelName).exists())
        loadModel()
    else
        trainModelAndSave()

}

fun loadModel() {
    val classifier = TextClassifier.fromModel(savedModelName)
    test(classifier)
}

fun trainModelAndSave() {
    val file = File(dataset)
    val trainer = ClassifierTrainer()
    val classifier = trainer.train(file)
    classifier.saveModel(savedModelName)
    test(classifier)
}

private fun test(classifier: TextClassifier) {
    val (category, probability) = classifier.classify(testQuery)
    println("Q: $testQuery")
    println("Classification: $category")
    println("Probability: ${String.format("%.2f", probability)}")
}

private fun prepareDataset(pricesFilePath: String, newsFilePath: String) {
    val datasetFile = File(dataset)
    if (datasetFile.exists()) return


    val excelReader = ExcelReader()

    val news = excelReader.readNews(newsFilePath).sortedBy { it.date }
    val prices = excelReader.readBtcPrices(pricesFilePath)
    val pricePeaks = prices.detectPeaks()
    val peakIndices = pricePeaks.peaks.map { it.index }
    val valleyIndices = pricePeaks.valleys.map { it.index }

    val datasetLines = prices.filterIndexed { index, _ -> index in valleyIndices }.flatMap { btc ->
        news.filter { it.date == btc.date || it.date == btc.date.minusDays(1) }.map {
            "$PRICE_INCREASE_CATEGORY\t${it.snippet} ${it.content}"
        }
    } + prices.filterIndexed { index, _ -> index in peakIndices }.flatMap { btc ->
        news.filter { it.date == btc.date || it.date == btc.date.minusDays(1) }.map {
            "$PRICE_DECREASE_CATEGORY\t${it.snippet} ${it.content}"
        }
    }

    // Accuracy without multiply ~ 0.4
    // Accuracy with multiply ~ 0.8
    multiplyDataset(datasetLines)

    Files.write(Paths.get(dataset), multiplyDataset(datasetLines))
}


fun multiplyDataset(datasetLines: List<String>, howManyTimes: Int = 10): List<String> =
        datasetLines.flatMap { line ->
            val category = line.substringBefore("\t")
            (0 until howManyTimes).map {
                category + "\t" + line.substringAfter("\t").split(" ").shuffled()
                        .joinToString(separator = " ")
            } + line
        }
