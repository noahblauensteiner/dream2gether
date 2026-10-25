package app.dream2gether.core.model

/** The shared space: who's in it and how to talk about them. Stored as `space.md`. */
data class Space(
    val name: String,
    val members: List<Member>,
    val language: String = "en",
    val timezone: String,
    val glossaryPeople: List<String> = emptyList(),
    val glossaryPlaces: List<String> = emptyList(),
    val extras: NoteExtras = NoteExtras.NONE,
) {
    init {
        require(members.isNotEmpty()) { "A space needs at least one member" }
        require(members.map { it.id }.toSet().size == members.size) { "Member ids must be unique" }
    }

    fun member(id: String): Member? = members.firstOrNull { it.id == id }
}

/** [id] is a short lowercase slug used in file names (`alex`); [name] is for display (`Alex`). */
data class Member(val id: String, val name: String) {
    init {
        require(MEMBER_ID.matches(id)) { "Member id must be lowercase letters, digits or dashes: $id" }
    }

    private companion object {
        val MEMBER_ID = Regex("[a-z0-9]+(-[a-z0-9]+)*")
    }
}
