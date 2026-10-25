package app.dream2gether.core.space

import app.dream2gether.core.model.Moment
import app.dream2gether.core.model.Space
import app.dream2gether.core.notes.MomentNote
import app.dream2gether.core.notes.SpaceNote
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/** Reading what's already in a space. Notes that don't parse are skipped, never fatal. */
object SpaceReader {
    class NotASpace : IllegalStateException("No space.md here. Create one with `d2g init-space`.")

    fun space(store: SpaceStore): Space =
        SpaceNote.read(store.readText(SpaceLayout.SPACE_NOTE) ?: throw NotASpace())

    fun moments(store: SpaceStore): List<Moment> =
        store.listFiles(SpaceLayout.MOMENTS).filter { it.endsWith(".md") }
            .mapNotNull { path -> store.readText(path)?.let { runCatching { MomentNote.read(it) }.getOrNull() } }

    /** Moments dated within [days] before [evening], newest first: context for spotting repeats. */
    fun recentMoments(store: SpaceStore, evening: LocalDate, days: Int = 30): List<Moment> {
        val from = evening.minus(DatePeriod(days = days))
        return moments(store).filter { m -> m.date != null && m.date in from..evening }.sortedByDescending { it.date }
    }
}
