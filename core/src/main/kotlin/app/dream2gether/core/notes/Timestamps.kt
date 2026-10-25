package app.dream2gether.core.notes

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.format
import kotlinx.datetime.format.parse

/** `62_000` → `01:02`; an hour or more → `1:02:03`. */
fun formatClock(ms: Long): String {
    val total = ms / 1000
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

/** Inverse of [formatClock]; also accepts `1:02`. Returns null when it isn't a clock time. */
fun parseClock(text: String): Long? {
    val parts = text.trim().split(":")
    if (parts.size !in 2..3 || parts.any { it.isEmpty() || !it.all(Char::isDigit) }) return null
    val nums = parts.map { it.toLong() }
    val seconds = if (nums.size == 3) nums[0] * 3600 + nums[1] * 60 + nums[2] else nums[0] * 60 + nums[1]
    return seconds * 1000
}

/** `2026-10-07T22:50:12+02:00` */
fun formatOffsetDateTime(dateTime: LocalDateTime, offset: UtcOffset): String =
    DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET.format { setDateTimeOffset(dateTime, offset) }

fun parseOffsetDateTime(text: String): Pair<LocalDateTime, UtcOffset> {
    val c = DateTimeComponents.parse(text.trim(), DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET)
    return c.toLocalDateTime() to c.toUtcOffset()
}

private val WEEKDAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** `Wed 7 Oct`, as shown in note headings. English only for V1. */
fun shortDay(date: LocalDate): String =
    "${WEEKDAYS[date.dayOfWeek.ordinal]} ${date.day} ${MONTHS[date.month.ordinal]}"
