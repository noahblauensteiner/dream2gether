package app.dream2gether.core.space

import app.dream2gether.core.model.Space
import app.dream2gether.core.notes.SpaceNote

/** Creates a new space: `space.md`, the folders and the Obsidian starter kit. */
object SpaceInit {
    class AlreadyASpace(path: String) : IllegalStateException("There's already a space here ($path)")

    fun create(store: SpaceStore, space: Space) {
        if (store.exists(SpaceLayout.SPACE_NOTE)) throw AlreadyASpace(SpaceLayout.SPACE_NOTE)
        store.writeText(SpaceLayout.SPACE_NOTE, SpaceNote.write(space))
        SpaceLayout.FOLDERS.forEach(store::ensureDir)
        store.writeText(StarterKit.START_HERE, StarterKit.startHere(space.name))
        store.writeText(StarterKit.MOMENTS_BASE, StarterKit.momentsBase)
        store.writeText(StarterKit.NIGHTS_BASE, StarterKit.nightsBase)
        store.writeText(StarterKit.CSS_SNIPPET, StarterKit.cssSnippet)
        // Only for a vault Obsidian hasn't touched yet: never overwrite someone's appearance settings.
        if (!store.exists(StarterKit.APPEARANCE)) store.writeText(StarterKit.APPEARANCE, StarterKit.APPEARANCE_JSON)
    }
}
