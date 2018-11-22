package sk.vildibald.text.classification.util

import edu.stanford.nlp.ling.BasicDatum
import org.clulab.processors.Processor
import scala.Option
import sk.vildibald.text.classification.FeatureFactory
import sk.vildibald.text.classification.data.entities.Category
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

val stopWords = setOf("a", "an", "and", "are", "as", "at", "be", "by", "did", "for", "from", "has",
        "he", "i", "in", "is", "it", "its", "of", "on", "that", "the", "to", "was", "were", "will",
        "with", "hi", "hello", "question", "answer", "?", "'s"
)

fun Iterable<String>.filterStopWords(): Iterable<String> =
        this.filterNot { stopWords.contains(it.toLowerCase()) }

fun Processor.createDatum(content: String): BasicDatum<Category, String> {
    val document = this.mkDocument(content, false)
    // Adjective, noun, verb?  See Penn Treebank tags: http://www.ling.upenn.edu/courses/Fall_2007/ling001/penn_treebank_pos.html
    try {
        this.tagPartsOfSpeech(document)
    } catch (e: Exception) {
    }
    // Smart stemming of words: tomato == tomatoes, am == is == are, have == had
    try {
        this.lemmatize(document)
    } catch (e: AssertionError) {
    }

    return BasicDatum(FeatureFactory.createFeatures(document))
}

/**
 * A workaround for Scala's Option<[T]>.getOrElse, since it does not compile in Kotlin.
 */
fun <T> Option<T>.getOrElseKotlin(default: () -> T): T = try {
    this.get()
} catch (e: Exception) {
    default()
}

fun Date.toLocalDate(): LocalDate = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

val defaultRelevantPosClasses: Set<String> = setOf(
//        "NN", "NNS", "NNP", "NNPS", "JJ", "VB", "VBP", "VBD", "VBG", "VBN", "VBZ"
//        "NN", "NNS", "NNP", "NNPS", "JJ"
        "NN", "NNS", "NNP", "NNPS"
)
