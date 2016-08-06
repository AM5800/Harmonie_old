package am5800.harmonie.app.vm.wordsList

import am5800.common.Lemma
import am5800.harmonie.app.model.localization.LocalizationService
import org.joda.time.DateTime
import org.joda.time.Seconds

class OnLearningWordsListItemViewModel(val lemma: Lemma,
                                       val dueDate: DateTime,
                                       private val localizationService: LocalizationService) : WordsListItemViewModel {
  val title = lemma.lemma

  val dueStatus: String
    get() {
      val now = DateTime()

      val delta = Seconds.secondsBetween(now, dueDate)

      val days = delta.toStandardDays().days
      val hours = delta.toStandardHours().hours
      val minutes = delta.toStandardMinutes().minutes
      val seconds = delta.seconds

      if (days > 0) {
        return localizationService.getCurrentTable().daysLeft.build(days)
      } else if (hours > 0) {
        return localizationService.getCurrentTable().hoursLeft.build(hours)
      } else if (minutes > 0) {
        return localizationService.getCurrentTable().minutesLeft.build(minutes)
      } else if (seconds > 0) {
        return localizationService.getCurrentTable().secondsLeft.build(seconds)
      } else {
        return localizationService.getCurrentTable().onDueStatus
      }
    }
}