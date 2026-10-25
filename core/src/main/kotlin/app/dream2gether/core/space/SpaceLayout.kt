package app.dream2gether.core.space

import app.dream2gether.core.model.MomentId
import app.dream2gether.core.model.NightId

/** Where each kind of file goes inside a space (docs/session-file-format.md#folder-layout). */
object SpaceLayout {
    const val SPACE_NOTE = "space.md"
    const val NIGHTS = "sessions"
    const val MOMENTS = "moments"
    const val AUDIO = "audio"
    const val PHOTOS = "photos"
    const val COLLECTIONS = "collections"
    const val MONTHS = "months"
    val FOLDERS = listOf(NIGHTS, MOMENTS, AUDIO, PHOTOS, COLLECTIONS, MONTHS)

    fun nightNote(id: NightId) = "$NIGHTS/${month(id.value)}/${id.value}.md"
    fun momentNote(id: MomentId) = "$MOMENTS/${month(id.value)}/${id.value}.md"
    fun audio(id: NightId, extension: String) = "$AUDIO/${month(id.value)}/${id.value}.$extension"
    fun photo(id: MomentId, n: Int, extension: String) = "$PHOTOS/${month(id.value)}/${id.value}-$n.$extension"
    fun collection(fileName: String) = "$COLLECTIONS/$fileName.md"
    fun monthNote(yearMonth: String) = "$MONTHS/$yearMonth.md"

    /** `s-2026-10-07-…` / `m-2026-10-07-…` → `2026-10`. */
    private fun month(id: String) = id.substring(2, 9)
}
