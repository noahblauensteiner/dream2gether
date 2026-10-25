package app.dream2gether.cli

import app.dream2gether.core.model.Member
import app.dream2gether.core.model.Space
import app.dream2gether.core.space.DemoSpace
import app.dream2gether.core.space.SpaceInit
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Path
import java.time.ZoneId
import kotlin.io.path.absolute
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

class D2g : CliktCommand(name = "d2g") {
    override fun help(context: Context) = "dream2gether laptop tool. Everything runs offline on this machine."
    override fun run() = Unit
}

class InitSpace : CliktCommand(name = "init-space") {
    override fun help(context: Context) = "Create a new space (an Obsidian vault) in DIR."

    private val dir by argument("DIR").path()
    private val name by option("--name", help = "Display name, e.g. \"Noah & Kim\"").required()
    private val members by option("--member", help = "id:Name, e.g. noah:Noah (repeat per person)").multiple(required = true)
    private val timezone by option("--timezone", help = "IANA zone, default: this Mac's").default(ZoneId.systemDefault().id)

    override fun run() {
        val space = Space(name = name, members = members.map(::parseMember), timezone = timezone)
        val root = dir.absolute()
        root.createDirectories()
        try {
            SpaceInit.create(FileSpaceStore(root), space)
        } catch (e: SpaceInit.AlreadyASpace) {
            throw UsageError(e.message ?: "Already a space")
        }
        echo("✨ Space created at $root")
        echo("   Open it in Obsidian: Open folder as vault → $root")
    }

    private fun parseMember(spec: String): Member {
        val id = spec.substringBefore(":").trim()
        val display = spec.substringAfter(":", id).trim()
        return try {
            Member(id, display)
        } catch (e: IllegalArgumentException) {
            throw UsageError("--member $spec: ${e.message}")
        }
    }
}

class Demo : CliktCommand(name = "demo") {
    override fun help(context: Context) = "Create a demo space with a fictional October, to try the Obsidian dashboard."

    private val dir by argument("DIR").path()

    override fun run() {
        val root = dir.absolute()
        if (root.exists() && root.listDirectoryEntries().isNotEmpty()) throw UsageError("$root isn't empty")
        root.createDirectories()
        DemoSpace.create(FileSpaceStore(root))
        echo("✨ Demo space created at $root")
        echo("   Open it in Obsidian (Open folder as vault), then open \"Start here\".")
    }
}

fun d2g(openModel: ModelOpener = openLlamaServer): CliktCommand =
    D2g().subcommands(InitSpace(), Demo(), ProcessTranscript(openModel))

fun main(args: Array<String>) = d2g().main(args)

internal fun Path.isEmptyDir() = !exists() || listDirectoryEntries().isEmpty()
