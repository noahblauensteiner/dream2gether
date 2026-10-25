package app.dream2gether.cli

import app.dream2gether.core.space.SpaceStore
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText

/** A space on the local disk. Paths can't escape [root]. */
class FileSpaceStore(val root: Path) : SpaceStore {
    private fun resolve(path: String): Path {
        val p = root.resolve(path).normalize()
        require(p.startsWith(root.normalize())) { "Path leaves the space: $path" }
        return p
    }

    override fun exists(path: String) = resolve(path).exists()

    override fun readText(path: String) = resolve(path).takeIf { it.isRegularFile() }?.readText()

    override fun writeText(path: String, text: String) {
        val p = resolve(path)
        p.parent?.createDirectories()
        p.writeText(text)
    }

    override fun writeBytes(path: String, bytes: ByteArray) {
        val p = resolve(path)
        p.parent?.createDirectories()
        p.writeBytes(bytes)
    }

    override fun ensureDir(path: String) {
        resolve(path).createDirectories()
    }

    override fun listFiles(dir: String): List<String> {
        val base = resolve(dir)
        if (!base.exists()) return emptyList()
        return Files.walk(base).use { s ->
            s.filter { it.isRegularFile() }.map { root.relativize(it).invariantSeparatorsPathString }.sorted().toList()
        }
    }
}
