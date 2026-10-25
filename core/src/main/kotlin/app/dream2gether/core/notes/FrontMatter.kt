package app.dream2gether.core.notes

import app.dream2gether.core.model.YamlValue

/**
 * The small YAML subset our notes use for properties: flat `key: value` pairs, scalars and lists.
 * Reads both list styles (`[a, b]` and Obsidian's `- a` blocks); writes block lists like Obsidian
 * does, so a note opened and saved in Obsidian doesn't change shape.
 */
class FrontMatter(val entries: List<Pair<String, YamlValue>>) {
    private val byKey = entries.toMap()

    val keys: Set<String> get() = byKey.keys

    fun string(key: String): String? = when (val v = byKey[key]) {
        is YamlValue.Scalar -> v.text.ifEmpty { null }
        is YamlValue.Items -> v.items.firstOrNull()
        null -> null
    }

    fun list(key: String): List<String> = when (val v = byKey[key]) {
        is YamlValue.Items -> v.items
        is YamlValue.Scalar -> if (v.text.isEmpty()) emptyList() else listOf(v.text)
        null -> emptyList()
    }

    fun bool(key: String): Boolean = string(key)?.lowercase() == "true"

    fun int(key: String): Int? = string(key)?.toIntOrNull()

    /** `"[[s-2026-10-07-2250-sam]]"` → `s-2026-10-07-2250-sam`. */
    fun link(key: String): String? = string(key)?.let(::unwrapLink)

    /** Entries whose keys aren't in [known]: properties someone added in Obsidian. */
    fun extras(known: Set<String>): List<Pair<String, YamlValue>> = entries.filter { it.first !in known }

    companion object {
        private val KEY_VALUE = Regex("""^([A-Za-z0-9_][A-Za-z0-9_\-]*):(?:\s+(.*))?$""")
        private val LIST_ITEM = Regex("""^\s*-\s+(.*)$""")

        fun parse(yaml: String): FrontMatter {
            val entries = mutableListOf<Pair<String, YamlValue>>()
            var openKey: String? = null
            for (raw in yaml.lines()) {
                val line = raw.trimEnd()
                if (line.isBlank() || line.trimStart().startsWith("#")) continue
                val item = LIST_ITEM.matchEntire(line)
                if (item != null && openKey != null) {
                    val idx = entries.indexOfLast { it.first == openKey }
                    val prev = entries[idx].second
                    val items = if (prev is YamlValue.Items) prev.items else emptyList()
                    entries[idx] = openKey to YamlValue.Items(items + scalar(item.groupValues[1]))
                    continue
                }
                val kv = KEY_VALUE.matchEntire(line) ?: continue
                val key = kv.groupValues[1]
                val rest = kv.groupValues[2].trim()
                val value = when {
                    rest.isEmpty() -> YamlValue.Scalar("")
                    rest.startsWith("[") && !rest.startsWith("[[") -> YamlValue.Items(flowItems(rest))
                    else -> YamlValue.Scalar(scalar(rest))
                }
                openKey = if (rest.isEmpty()) key else null
                entries.removeAll { it.first == key }
                entries += key to value
            }
            return FrontMatter(entries)
        }

        /** Reads one scalar: unquotes `"…"`/`'…'`, drops a trailing ` # comment` on plain values. */
        private fun scalar(text: String): String {
            val t = text.trim()
            return when {
                t.startsWith("\"") -> readDoubleQuoted(t)
                t.startsWith("'") -> readSingleQuoted(t)
                else -> t.substringBefore(" #").trim()
            }
        }

        private fun readDoubleQuoted(t: String): String {
            val sb = StringBuilder()
            var i = 1
            while (i < t.length) {
                val c = t[i]
                when {
                    c == '\\' && i + 1 < t.length -> {
                        sb.append(
                            when (val n = t[i + 1]) {
                                'n' -> '\n'
                                't' -> '\t'
                                else -> n
                            },
                        )
                        i += 2
                    }
                    c == '"' -> return sb.toString()
                    else -> { sb.append(c); i++ }
                }
            }
            return sb.toString()
        }

        private fun readSingleQuoted(t: String): String {
            val sb = StringBuilder()
            var i = 1
            while (i < t.length) {
                val c = t[i]
                if (c == '\'') {
                    if (i + 1 < t.length && t[i + 1] == '\'') { sb.append('\''); i += 2; continue }
                    return sb.toString()
                }
                sb.append(c)
                i++
            }
            return sb.toString()
        }

        private fun flowItems(text: String): List<String> {
            val inner = text.trim().removePrefix("[").removeSuffix("]")
            val items = mutableListOf<String>()
            val cur = StringBuilder()
            var quote: Char? = null
            for (c in inner) {
                when {
                    quote != null -> { cur.append(c); if (c == quote) quote = null }
                    c == '"' || c == '\'' -> { cur.append(c); quote = c }
                    c == ',' -> { items += cur.toString(); cur.clear() }
                    else -> cur.append(c)
                }
            }
            items += cur.toString()
            return items.map { scalar(it) }.filter { it.isNotEmpty() }
        }
    }
}

fun unwrapLink(text: String): String = text.trim().removePrefix("[[").removeSuffix("]]").substringBefore("|")

fun wrapLink(target: String): String = "[[$target]]"

/** Builds a front matter block in a fixed key order. */
class FrontMatterWriter {
    private val sb = StringBuilder()

    fun string(key: String, value: String?) = apply {
        sb.append(key).append(':')
        if (!value.isNullOrEmpty()) sb.append(' ').append(quoteIfNeeded(value))
        sb.append('\n')
    }

    /** Values YAML reads as typed data, written bare: dates, numbers, booleans. */
    fun raw(key: String, value: Any?) = apply {
        sb.append(key).append(':')
        if (value != null) sb.append(' ').append(value.toString())
        sb.append('\n')
    }

    fun link(key: String, target: String?) = string(key, target?.let(::wrapLink))

    fun list(key: String, items: List<String>) = apply {
        if (items.isEmpty()) {
            sb.append(key).append(": []\n")
        } else {
            sb.append(key).append(":\n")
            items.forEach { sb.append("  - ").append(quoteIfNeeded(it)).append('\n') }
        }
    }

    fun value(key: String, value: YamlValue) = when (value) {
        is YamlValue.Scalar -> string(key, value.text)
        is YamlValue.Items -> list(key, value.items)
    }

    fun build(): String = sb.toString()

    companion object {
        private val LOOKS_TYPED = Regex("""^(true|false|yes|no|on|off|null|~|[-+]?[\d._:]+|\d{4}-\d{2}-\d{2}.*)$""", RegexOption.IGNORE_CASE)
        private const val SPECIAL_START = "[]{}\"'#&*!|>%@`,-?:"

        fun quoteIfNeeded(s: String): String {
            val needs = s.first() in SPECIAL_START || s.contains(": ") || s.contains(" #") ||
                s.endsWith(":") || s != s.trim() || s.contains('\n') || LOOKS_TYPED.matches(s)
            if (!needs) return s
            val escaped = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
            return "\"$escaped\""
        }
    }
}
