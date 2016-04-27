package dataProcessor.corpus

import java.io.File

class CorpusInfo(val infoFile: File,
                 val formatId: String,
                 val metadata: Map<String, String>) {

}