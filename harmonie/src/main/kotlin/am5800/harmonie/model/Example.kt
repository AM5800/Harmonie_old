package am5800.harmonie.model


public class ExampleRange(public val start : Int, public val length : Int)

public class Example (public val text : String,
                      public val entityId : EntityId,
                      public val meanings : List<String>,
                      public val ranges : List<ExampleRange>)