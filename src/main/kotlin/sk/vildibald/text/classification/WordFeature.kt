package sk.vildibald.text.classification

import org.clulab.processors.Sentence
import sk.vildibald.text.classification.util.filterStopWords

class WordFeature : Feature {
    override fun createFeatures(sentence: Sentence): Iterable<String> =
            sentence.words().asList().filterStopWords().map { "S-$it" }


}
