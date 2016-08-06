package am5800.harmonie.app.vm.wordsList

import am5800.common.Lemma
import am5800.harmonie.app.model.localization.LocalizationService
import org.joda.time.DateTime
import org.joda.time.Seconds


class OnLearningWordsListItemViewModel(val lemma: Lemma,
                                       dueDate: DateTime,
                                       localizationService: LocalizationService) : WordsListItemViewModel {
  val title = lemma.lemma

  var dueStatusString: String = ""
  var status: Status = Status.Ok

  init {
    val now = DateTime()

    val delta = Seconds.secondsBetween(now, dueDate)

    val days = delta.toStandardDays().days
    val hours = delta.toStandardHours().hours
    val minutes = delta.toStandardMinutes().minutes
    val seconds = delta.seconds

    if (days > 0) {
      dueStatusString = localizationService.getCurrentTable().daysLeft.build(days)
    } else if (hours > 0) {
      dueStatusString = localizationService.getCurrentTable().hoursLeft.build(hours)
    } else if (minutes > 0) {
      dueStatusString = localizationService.getCurrentTable().minutesLeft.build(minutes)
    } else if (seconds > 0) {
      dueStatusString = localizationService.getCurrentTable().lessThanMinute
      status = Status.Warning
    } else {
      dueStatusString = localizationService.getCurrentTable().onDueStatus
      status = Status.Error
    }
  }
}