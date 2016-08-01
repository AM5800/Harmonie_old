package dataProcessor.db

import dataProcessor.parsing.MeaningsParseResult


interface MeaningsWriter {
  fun write(data: List<MeaningsParseResult>)
}