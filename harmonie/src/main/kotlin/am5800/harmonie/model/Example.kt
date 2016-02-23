package am5800.harmonie.model


class ExampleRange(val start: Int, val length: Int)

class Example(val text: String,
              val entityId: EntityId,
              val meanings: List<String>,
              val ranges: List<ExampleRange>)