package sk.vildibald.text.classification

import org.clulab.processors.Processor
import org.clulab.processors.fastnlp.FastNLPProcessor

object ProcessorFactory {
    fun createProcessor(): Processor =
//            CoreNLPProcessor(true, false, ShallowNLPProcessor.NO_DISCOURSE(), 100)
    FastNLPProcessor(true, false, FastNLPProcessor.WITH_DISCOURSE())
//    CluProcessorWithStanford()
}