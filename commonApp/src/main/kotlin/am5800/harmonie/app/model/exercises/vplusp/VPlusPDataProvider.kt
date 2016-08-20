package am5800.harmonie.app.model.exercises.vplusp

interface VPlusPDataProvider {
  fun getAllTopics(): List<String>
  fun get(topic: String): List<VPlusPData>
}

