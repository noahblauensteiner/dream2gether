package app.dream2gether.core.extract

import app.dream2gether.core.model.DateConfidence
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.minus

/**
 * Turns the model's date references (`today`, `weekday:saturday`, …) into real dates. The model
 * never does date arithmetic; this does. See docs/architecture.md#working-with-a-small-model.
 */
object DateRef {
    /** The forms the model may use. Also the JSON-schema pattern for `day_ref`. */
    const val PATTERN =
        "^(today|yesterday|this-week|weekend|unknown|" +
            "weekday:(monday|tuesday|wednesday|thursday|friday|saturday|sunday)|date:[0-9]{4}-[0-9]{2}-[0-9]{2})$"

    private const val EARLY_MORNING_HOUR = 5

    data class Resolved(val date: LocalDate?, val dateEnd: LocalDate?, val confidence: DateConfidence)

    /** The day a bedtime chat is about: after midnight it's still "tonight" until early morning. */
    fun eveningOf(recordedAt: LocalDateTime): LocalDate =
        if (recordedAt.hour < EARLY_MORNING_HOUR) recordedAt.date.minus(DatePeriod(days = 1)) else recordedAt.date

    fun resolve(ref: String, refEnd: String?, approx: Boolean, evening: LocalDate): Resolved {
        val start = range(ref.trim().lowercase(), evening) ?: return Resolved(null, null, DateConfidence.UNKNOWN)
        val end = refEnd?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }?.let { range(it, evening) }
        var from = start.first
        var to = end?.let { it.second ?: it.first } ?: start.second
        if (to != null && to < from) { val t = from; from = to; to = t }
        if (to == from) to = null
        val confidence = if (approx || to != null) DateConfidence.APPROX else DateConfidence.EXACT
        return Resolved(from, to, confidence)
    }

    /** A reference as (first day, last day or null). Null when unknown or in the future. */
    private fun range(ref: String, evening: LocalDate): Pair<LocalDate, LocalDate?>? = when {
        ref == "today" -> evening to null
        ref == "yesterday" -> evening.minus(DatePeriod(days = 1)) to null
        ref == "this-week" -> lastOnOrBefore(DayOfWeek.MONDAY, evening) to evening
        ref == "weekend" -> {
            val saturday = lastOnOrBefore(DayOfWeek.SATURDAY, evening)
            val sunday = saturday.plusDays(1)
            saturday to (if (sunday > evening) null else sunday)
        }
        ref.startsWith("weekday:") -> weekday(ref.removePrefix("weekday:"))?.let { lastOnOrBefore(it, evening) to null }
        ref.startsWith("date:") -> runCatching { LocalDate.parse(ref.removePrefix("date:")) }.getOrNull()
            ?.takeIf { it <= evening }?.let { it to null }
        else -> null
    }

    private fun weekday(name: String): DayOfWeek? = DayOfWeek.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }

    private fun lastOnOrBefore(day: DayOfWeek, date: LocalDate): LocalDate {
        val back = (date.dayOfWeek.ordinal - day.ordinal + 7) % 7
        return date.minus(DatePeriod(days = back))
    }

    private fun LocalDate.plusDays(n: Int) = minus(DatePeriod(days = -n))
}
