package sk.vildibald.text.classification

import edu.stanford.nlp.classify.LinearClassifier
import org.clulab.processors.Processor
import sk.vildibald.text.classification.util.createDatum

class TextClassifier(private val classifier: LinearClassifier<Category, String>,
                     private val processor: Processor = ProcessorFactory.createProcessor()) {
    fun classify(text: String): Classification {
        val counter = classifier.probabilityOf(processor.createDatum(text))
        val scores = counter.entrySet().toList().sortedByDescending { it.value }
        val best = scores.first()
        return Classification(best.key, best.value)
    }

    fun saveModel(modelName: String) =
            LinearClassifier.writeClassifier(classifier, modelName)

    companion object {
        fun fromModel(modelName: String): TextClassifier {
            val linearClassifier =
                    LinearClassifier.readClassifier<Category, String>(modelName)
            return TextClassifier(linearClassifier)
        }

    }

}