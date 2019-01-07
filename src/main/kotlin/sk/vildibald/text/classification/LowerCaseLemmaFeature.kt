package sk.vildibald.text.classification

import org.clulab.processors.Sentence
import sk.vildibald.text.classification.util.filterStopWords
import sk.vildibald.text.classification.util.GetOrElseInKotlin

class LowerCaseLemmaFeature : Feature {
    override fun createFeatures(sentence: Sentence): Iterable<String> =
            sentence.lemmas()
                    .GetOrElseInKotlin { emptyArray() }
                    .asList()
                    .filterStopWords()
                    .map { "S-${it.toLowerCase()}" }

}
