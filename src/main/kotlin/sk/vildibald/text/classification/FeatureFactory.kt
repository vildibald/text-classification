package sk.vildibald.text.classification

import org.clulab.processors.Document

object FeatureFactory {
    private val creators = listOf(WordFeature(), LowerCaseLemmaFeature(), WordPairFeature())

    fun createFeatures(document: Document): Set<String> =
        document.sentences().flatMap { sentence ->
            creators.filter { it.hasSentenceFeatures() }.flatMap { it.createFeatures(sentence) }
        }.toSet()

}