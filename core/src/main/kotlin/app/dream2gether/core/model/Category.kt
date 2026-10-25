package app.dream2gether.core.model

/** Fixed category list (docs/concept.md#categories). Keys are what the notes and the LLM use. */
enum class Category(val key: String, val emoji: String, val label: String) {
    US("us", "💞", "love & us"),
    COZY("cozy", "🛋️", "cozy at home"),
    FOOD("food", "🍝", "food & drinks"),
    OUTDOORS("outdoors", "🌲", "outdoors & nature"),
    ADVENTURE("adventure", "🧭", "adventure & travel"),
    PEOPLE("people", "👯", "friends & family"),
    CULTURE("culture", "🎬", "music, film, books, art"),
    SPORT("sport", "🧗", "sport & body"),
    SILLY("silly", "🤪", "silly & funny"),
    LITTLE_THINGS("little-things", "✨", "little things"),
    WINS("wins", "🏆", "wins & proud moments");

    companion object {
        private val byKey = entries.associateBy { it.key }

        fun fromKey(key: String): Category? = byKey[key.trim().lowercase()]
    }
}
