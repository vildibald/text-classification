package sk.vildibald.text.classification

import edu.stanford.nlp.classify.Dataset
import edu.stanford.nlp.classify.LinearClassifierFactory
import edu.stanford.nlp.ling.BasicDatum
import org.clulab.processors.Processor
import sk.vildibald.text.classification.util.createDatum
import java.io.File

typealias Category = String

class ClassifierTrainer(private val convergenceTolerance: Double = 1e-4,
                        private val smoothingFactor: Double = 1.0,
                        private val numFeatures: Int = 50) {

    private val processor = ProcessorFactory.createProcessor()

    private data class CategoryContent(val category: String,
                                       val content: String) {
        fun toDatum(processor: Processor = ProcessorFactory.createProcessor())
                : BasicDatum<String, String> {
            val datum = processor.createDatum(content)
            datum.setLabel(category)
            return datum
        }
    }

    fun train(trainingFile: File, datasetMultiplyFactor: Int = 1): TextClassifier {

        val goldSet = trainingFile.readLines().asSequence().map { it.split("\t") }
                .map { (category, content) ->
                    CategoryContent(category, content)
                }.toList().shuffled()

        val splitIdx = (goldSet.size * 0.8).toInt()

        val trainingData = Dataset<Category, String>(splitIdx)
        goldSet.take(splitIdx).multiplied(datasetMultiplyFactor).shuffled().forEach {
            trainingData.add(it.toDatum(processor))
        }

        val testData = goldSet.drop(splitIdx)
        println("Total features: ${trainingData.numFeatures()}")

        // Train a classifier.
        // Convergence tolerance = 1e-4.  Sigma smoothing = 1.0 (decrease if system is over-trained)
        val linearClassifierFactory = LinearClassifierFactory<Category, String>(
                convergenceTolerance, false, smoothingFactor)
        val classifier = linearClassifierFactory.trainClassifier(trainingData)

        val bestFeatures = classifier.toBiggestWeightFeaturesString(false, numFeatures, true)
        println("Top $numFeatures overall features:\n$bestFeatures\n")

        val correct = testData.count { tagContentDatum ->
            val scores = classifier.probabilityOf(tagContentDatum.toDatum(processor)).entrySet()
                    .toList().sortedByDescending { it.value }
            tagContentDatum.category == scores.first().key
        }
        println("Training accuracy: ${String.format("%.1f", 100.0 * correct / testData.size)}%\n")
        return TextClassifier(classifier)
    }

    private fun multiplyDataset(datasetRows: Iterable<CategoryContent>, multiplyFactor: Int):
            Iterable<CategoryContent> =
            multiplyFactor.takeIf { it > 1 }?.let {
                datasetRows.flatMap { datum ->
                    (0 until multiplyFactor).map {
                        datum.copy(content = datum.content.split(" ").shuffled()
                                .joinToString(separator = " "))
                    } + datum
                }
            } ?: datasetRows

    private fun Iterable<CategoryContent>.multiplied(multiplyFactor: Int)
            : Iterable<CategoryContent> =
            multiplyDataset(this, multiplyFactor)
}