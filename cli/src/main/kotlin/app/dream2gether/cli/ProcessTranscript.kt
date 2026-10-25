package app.dream2gether.cli

import app.dream2gether.core.llm.LanguageModel
import app.dream2gether.core.process.NightProcessor
import app.dream2gether.core.process.TypedChat
import app.dream2gether.core.space.SpaceReader
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toKotlinLocalDateTime
import java.nio.file.Path
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.readText

/** Which local model to use. Every command that extracts moments shares these. */
class LlmOptions : OptionGroup("Local model (pick one)") {
    val url by option("--llm-url", envvar = "D2G_LLM_URL", help = "Use a llama-server that's already running, e.g. http://127.0.0.1:8080")
    val model by option("--model", envvar = "D2G_LLM_MODEL", help = "Path to a .gguf model file").path()
    val hf by option("--hf", envvar = "D2G_LLM_HF", help = "Hugging Face repo llama-server downloads once, e.g. ggml-org/gemma-3-12b-it-GGUF")
    val binary by option("--llama-server", envvar = "D2G_LLAMA_SERVER", help = "llama-server binary").default("llama-server")
}

/** Opens the model the options point at. Tests swap this for a fake. */
typealias ModelOpener = (LlmOptions, (String) -> Unit) -> LanguageModel

val openLlamaServer: ModelOpener = { o, status ->
    when {
        o.url != null -> LlamaServer.connect(o.url!!)
        o.model != null -> LlamaServer.start(o.binary, ModelSource.File(o.model!!), status)
        o.hf != null -> LlamaServer.start(o.binary, ModelSource.HuggingFace(o.hf!!), status)
        else -> throw UsageError(
            "Pick a model: --hf ggml-org/gemma-3-12b-it-GGUF (downloads once), --model file.gguf, " +
                "or --llm-url of a running llama-server. Or set D2G_LLM_HF.",
        )
    }
}

class ProcessTranscript(private val openModel: ModelOpener) : CliktCommand(name = "process-transcript") {
    override fun help(context: Context) =
        "Turn a typed bedtime chat (one line per turn, optionally `Name: text`) into a night and its moments."

    private val file by argument("FILE").path(mustExist = true, canBeDir = false)
    private val space by option("--space", envvar = "D2G_SPACE", help = "Space folder (default: current folder)").path().default(Path.of("."))
    private val by by option("--by", help = "Member id of who's recording, e.g. noah").required()
    private val at by option("--at", help = "When you talked, \"YYYY-MM-DD HH:MM\" (default: now)")
    private val question by option("--question", help = "The evening question you answered").default(DEFAULT_QUESTION)
    private val llm by LlmOptions()

    override fun run() {
        val root = space.absolute().normalize()
        val store = FileSpaceStore(root)
        val spaceNote = runCatching { SpaceReader.space(store) }.getOrElse { throw UsageError("$root: ${it.message}") }
        val (recordedAt, offset) = whenTalked(at, ZoneId.of(spaceNote.timezone))
        val transcript = TypedChat.parse(file.readText())
        if (transcript.isEmpty()) throw UsageError("$file has no lines")

        val model = openModel(llm) { echo(it) }
        try {
            echo("Reading ${transcript.size} lines with ${model.name}…")
            val result = NightProcessor(store, model, ::nowIso).process(
                NightProcessor.NightInput(
                    recordedBy = by,
                    device = "mac (typed)",
                    recordedAt = recordedAt,
                    utcOffset = offset,
                    question = question,
                    transcript = transcript,
                    durationS = transcript.size * TypedChat.SECONDS_PER_LINE,
                    audioFile = null,
                    sttModel = "typed",
                ),
            )
            report(this, result)
        } catch (e: IllegalArgumentException) {
            throw UsageError(e.message ?: "Bad input")
        } catch (e: NightProcessor.AlreadyProcessed) {
            throw UsageError("${e.message}. Use another --at time.")
        } finally {
            (model as? AutoCloseable)?.close()
        }
    }

    companion object {
        const val DEFAULT_QUESTION = "What was your favourite moment today?"
    }
}

private val AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

internal fun whenTalked(at: String?, zone: ZoneId): Pair<LocalDateTime, UtcOffset> {
    val local = if (at == null) {
        java.time.LocalDateTime.now(zone).truncatedTo(ChronoUnit.MINUTES)
    } else {
        runCatching { java.time.LocalDateTime.parse(at.trim(), AT_FORMAT) }
            .getOrElse { throw UsageError("--at must look like \"2026-10-24 22:41\"") }
    }
    val offsetSeconds = zone.rules.getOffset(local).totalSeconds
    return local.toKotlinLocalDateTime() to UtcOffset(seconds = offsetSeconds)
}

internal fun nowIso(): String = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString()

internal fun report(cmd: CliktCommand, r: NightProcessor.Processed) {
    cmd.echo("🌙 Night saved: ${r.nightPath}")
    if (r.moments.isEmpty()) cmd.echo("   No favourite moments found in this one.")
    else cmd.echo("✨ ${r.moments.size} moment${if (r.moments.size == 1) "" else "s"}:")
    r.moments.zip(r.momentPaths).forEach { (m, path) ->
        val day = m.date?.let { app.dream2gether.core.notes.shortDay(it) } ?: "date unknown"
        cmd.echo("   ${m.emoji} ${m.title}  ($day)  $path")
    }
    r.night.dreams.forEach { cmd.echo("💭 ${it.text}") }
}

