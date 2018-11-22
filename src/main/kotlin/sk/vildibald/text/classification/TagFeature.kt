package sk.vildibald.text.classification

import org.clulab.processors.Sentence
import sk.vildibald.text.classification.util.defaultRelevantPosClasses
import sk.vildibald.text.classification.util.getOrElseKotlin

class TagFeature(private val partOfSpeechClasses: Set<String> = defaultRelevantPosClasses)
    : Feature {
    override fun createFeatures(sentence: Sentence): Iterable<String> {
        val lemmas = sentence.lemmas().getOrElseKotlin { emptyArray() }
        val tags = sentence.tags().getOrElseKotlin { emptyArray() }

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