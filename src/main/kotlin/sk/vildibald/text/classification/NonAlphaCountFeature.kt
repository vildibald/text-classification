package sk.vildibald.text.classification

import org.clulab.processors.Sentence


class NonAlphaCountFeature : Feature {
    override fun createFeatures(sentence: Sentence): Iterable<String> {
        val counts = mutableMapOf<Char, Int>()
        sentence.words().forEach { word ->
            word.filterNot {
                it.isLetter() || it.isWhitespace() || it.isISOControl()
            }.forEach { c ->
                if (c.isDigit()) counts['d'] = (counts['d'] ?: 0) + 1
                else counts[c] = (counts[c] ?: 0) + 1
            }
        }

        return counts.map { (c, count) ->
            when (count) {
                1 -> "CHC-$c"
                2, 3 -> "CHC-$c-2+"
                4, 5, 6 -> "CHC-$c-4+"
                else -> "CHC-$c-7+"
            }
        }
    }

}
