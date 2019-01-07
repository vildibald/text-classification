package sk.vildibald.text.classification

import org.clulab.processors.Sentence
import sk.vildibald.text.classification.util.defaultRelevantPosClasses
import sk.vildibald.text.classification.util.GetOrElseInKotlin

class TagFeature(private val partOfSpeechClasses: Set<String> = defaultRelevantPosClasses)
    : Feature {
    override fun createFeatures(sentence: Sentence): Iterable<String> {
        val lemmas = sentence.lemmas().GetOrElseInKotlin { emptyArray() }
        val tags = sentence.tags().GetOrElseInKotlin { emptyArray() }

        if (lemmas.isEmpty() || tags.isEmpty()) return emptyList()
        return if (lemmas.size > 1)
            tags.zip(lemmas).asSequence().filter { (tag, _) ->
                tag in partOfSpeechClasses
            }.map { (_, lemma) ->
                "T-$lemma"
            }.toList()
        else emptyList()
    }
}