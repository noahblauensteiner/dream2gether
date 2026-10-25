package app.dream2gether.core.space

/**
 * Where a space's files live, addressed by paths relative to the space root (`moments/2026-10/x.md`).
 * The CLI uses plain files; Android will use its folder-picker storage.
 */
interface SpaceStore {
    fun exists(path: String): Boolean
    fun readText(path: String): String?
    fun writeText(path: String, text: String)
    fun writeBytes(path: String, bytes: ByteArray)
    fun ensureDir(path: String)

    /** Files (not folders) under [dir], recursively, as space-relative paths. */
    fun listFiles(dir: String): List<String>
}

/** For tests and previews. */
class InMemorySpaceStore : SpaceStore {
    val files = linkedMapOf<String, ByteArray>()
    val dirs = linkedSetOf<String>()

    override fun exists(path: String) = path in files || path in dirs
    override fun readText(path: String) = files[path]?.decodeToString()
    override fun writeText(path: String, text: String) = writeBytes(path, text.encodeToByteArray())
    override fun writeBytes(path: String, bytes: ByteArray) { files[path] = bytes }
    override fun ensureDir(path: String) { dirs += path }
    override fun listFiles(dir: String) = files.keys.filter { dir.isEmpty() || it.startsWith("$dir/") }.sorted()
}
