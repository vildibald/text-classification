package sk.vildibald.text.classification

import org.clulab.processors.Sentence
import sk.vildibald.text.classification.util.getOrElseKotlin

class WordPairFeature : Feature {

    override fun createFeatures(sentence: Sentence): Iterable<String> {
        val lemmas = sentence.lemmas().getOrElseKotlin { emptyArray() }
        val tags = sentence.tags().getOrElseKotlin { emptyArray() }

        if (lemmas.isEmpty() || tags.isEmpty()) return emptyList()

        return if (lemmas.size > 1)
            tags.zip(lemmas).asSequence().zipWithNext().filter { (first, second) ->
                val firstTag = first.first
                val secondTag = second.first
                matchesPosition(firstTag) && matchesPosition(secondTag)
            }.map { (first, second) ->
                val firstLemma = first.second
                val secondLemma = second.second
                "SWP-$firstLemma-$secondLemma"
            }.toList() else emptyList()
    }

    private fun matchesPosition(tag: String): Boolean = when (tag) {
        "NN", "NNS", "NNP", "NNPS", "JJ", "VB", "VBP", "VBD", "VBG", "VBN", "VBZ" -> true
//        "NN", "NNS", "NNP", "NNPS", "VB", "VBP", "VBD", "VBG", "VBN", "VBZ" -> true
        else -> false
    }

}
