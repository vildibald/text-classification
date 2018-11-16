package sk.vildibald.text.classification

import org.clulab.processors.Sentence
import sk.vildibald.text.classification.util.filterStopWords
import sk.vildibald.text.classification.util.getOrElseKotlin

class LowerCaseLemmaFeature : Feature {
    override fun createFeatures(sentence: Sentence): Iterable<String> {
        val lemmas = sentence.lemmas().getOrElseKotlin { emptyArray() }
        return lemmas.asList().filterStopWords().map {
            "S-${it.toLowerCase()}"
        }

    }

}
