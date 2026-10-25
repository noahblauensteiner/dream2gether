package app.dream2gether.core.model

/** Fixed category list (docs/concept.md#categories). Keys are what the notes and the LLM use. */
/** [color] is the card colour on the moments dashboard when a moment has no photo. */
enum class Category(val key: String, val emoji: String, val label: String, val color: String) {
    US("us", "💞", "love & us", "#E8A0BF"),
    COZY("cozy", "🛋️", "cozy at home", "#E9C46A"),
    FOOD("food", "🍝", "food & drinks", "#F4A261"),
    OUTDOORS("outdoors", "🌲", "outdoors & nature", "#8AB17D"),
    ADVENTURE("adventure", "🧭", "adventure & travel", "#2A9D8F"),
    PEOPLE("people", "👯", "friends & family", "#B392AC"),
    CULTURE("culture", "🎬", "music, film, books, art", "#6D83F2"),
    SPORT("sport", "🧗", "sport & body", "#E76F51"),
    SILLY("silly", "🤪", "silly & funny", "#F6D55C"),
    LITTLE_THINGS("little-things", "✨", "little things", "#A8DADC"),
    WINS("wins", "🏆", "wins & proud moments", "#D4A373");

    companion object {
        const val NO_CATEGORY_COLOR = "#CDB4DB"

        private val byKey = entries.associateBy { it.key }

        fun fromKey(key: String): Category? = byKey[key.trim().lowercase()]
    }
}
