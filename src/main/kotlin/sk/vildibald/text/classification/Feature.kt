package sk.vildibald.text.classification

import org.clulab.processors.Sentence

interface Feature {
    fun hasSentenceFeatures(): Boolean = true
    fun createFeatures(sentence: Sentence): Iterable<String>
}