package sk.vildibald.text.classification

import org.clulab.processors.Sentence
import sk.vildibald.text.classification.util.defaultRelevantPosClasses
import sk.vildibald.text.classification.util.getOrElseKotlin

class WordPairFeature(private val partOfSpeechClasses: Set<String> = defaultRelevantPosClasses)
    : Feature {

    override fun createFeatures(sentence: Sentence): Iterable<String> {
        val lemmas = sentence.lemmas().getOrElseKotlin { emptyArray() }
        val tags = sentence.tags().getOrElseKotlin { emptyArray() }

        if (lemmas.isEmpty() || tags.isEmpty()) return emptyList()

        return if (lemmas.size > 1)
            tags.zip(lemmas).asSequence().zipWithNext().filter { (first, second) ->
                val firstTag = first.first
                val secondTag = second.first
                firstTag in partOfSpeechClasses && secondTag in partOfSpeechClasses
            }.map { (first, second) ->
                val firstLemma = first.second
                val secondLemma = second.second
                "SWP-$firstLemma-$secondLemma"
            }.toList() else emptyList()
    }



}
