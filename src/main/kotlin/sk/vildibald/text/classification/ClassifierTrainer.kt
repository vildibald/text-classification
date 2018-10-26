package sk.vildibald.text.classification

import edu.stanford.nlp.classify.Dataset
import edu.stanford.nlp.classify.LinearClassifierFactory
import edu.stanford.nlp.ling.BasicDatum
import sk.vildibald.text.classification.util.createDatum
import java.io.File
import java.util.*

typealias Category = String

class ClassifierTrainer(private val convergenceTolerance: Double = 1e-4,
                        private val smoothingFactor: Double = 1.0,
                        private val numFeatures: Int = 50) {

    fun train(trainingFile: File): TextClassifier {
        data class TagContentDatum(val tag: String,
                                   val content: String,
                                   val datum: BasicDatum<String, String>)

        val processor = ProcessorFactory.createProcessor()
        var goldSet = trainingFile.readLines().map { it.split("\t") }.map { (tag, content) ->
            val datum = processor.createDatum(content)
            datum.setLabel(tag)
            TagContentDatum(tag, content, datum)
        }

        val random = Random()
//        random.setSeed(123)
        goldSet = goldSet.shuffled(random)
        val splitIdx = (goldSet.size * 0.8).toInt()

        val trainingData = Dataset<Category, String>(splitIdx)
        goldSet.take(splitIdx).forEach { trainingData.add(it.datum) }

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
            val scores = classifier.probabilityOf(tagContentDatum.datum).entrySet().toList()
                    .sortedByDescending { it.value }
            tagContentDatum.tag == scores.first().key
        }
        println("Training accuracy: ${String.format("%.1f", 100.0*correct/testData.size)}\n")
        return TextClassifier(classifier)
    }


}