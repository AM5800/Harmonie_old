package dataProcessor.db

import dataProcessor.FormOccurrence


interface FillTheGapsWriter {
  fun write(result: List<FormOccurrence>)
}