package sk.vildibald.text.classification

import org.clulab.processors.Sentence
import sk.vildibald.text.classification.util.filterStopWords

class LowerCaseLemmaFeature : Feature {
    override fun createFeatures(sentence: Sentence): Iterable<String> {
        val lemmas = try {
            sentence.lemmas().get()
        } catch (e: Exception) {
            emptyArray<String>()
        }
        return lemmas.asList().filterStopWords().map {
            "S-${it.toLowerCase()}"
        }

    }

}
