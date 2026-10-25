# dream2gether 🌙

> Lights off, one press, *"What was your favourite moment today?"*
> dream2gether listens, writes the moments down, and at the end of the month hands you
> a warm little journal of everything you loved together, with your own voices telling it.

A home tinker project for couples who talk about their favourite moments at night.
**Nothing leaves your devices.** Either partner's phone records. That phone transcribes and
summarises the night itself and writes one Markdown file. Your phones share those files directly
with each other, with no cloud and no servers.

## How it works

1. **Press.** Tap record in the app (V1) or press the button on the bedside box (V2), and talk.
2. **Remember.** Overnight, while the phone charges, the recording is transcribed and a small
   on-device language model picks out the favourite moments: what, when, where, and a quote. In
   the morning you see *"Last night you remembered 3 moments ✨"*.
3. **Relive.** Each moment links back to the part of the recording where you told it, so you can
   tap ▶️ and hear yourselves. Every month gets a playful recap ("October Wrapped") and a journal
   you can edit, add photos to and export as a Markdown handover bundle.

## Roadmap

| Version | Platform | What you get |
|---|---|---|
| **V1** | Any Android phone, English | Shared space (either phone records, a synced folder of `.md` files), one-tap recording, audio archive, on-device transcription and moment extraction, moments inbox & editing, ▶️ hear-it playback, evening questions, manual photos, monthly recap, handover export, encrypted backup |
| **V1.5** | Android | Dream list ("we should totally…"), photo suggestions from the phone gallery, "one year ago tonight" |
| **V2** | Raspberry Pi bedside box | Physical button, no screen at night, LED feedback, optional e-ink question. The box does everything locally; the phone browses over home Wi-Fi. "Talking stick" mode records who said what |
| **Languages** | all | 🇩🇪 German, 🇳🇱 Dutch, then mixing languages mid-sentence |
| **Later** | | iOS, Year Wrapped with voice clips, printed photo book, optional photo-album companion |

## Docs

- [Concept](docs/concept.md): the nightly routine, user flows, features per version, the fun
  monthly recap, evening questions, language roadmap
- [Architecture](docs/architecture.md): on-device processing steps, the shared core, models,
  data model, privacy guarantees, backup, the Pi box
- [Space & session file format](docs/session-file-format.md): the shared folder and the
  per-night `.md` file (moments plus transcript)
- [Journal handover format](docs/journal-handover-format.md): the `.md` bundle that says what
  lands in a month's journal
- [Example handover file](examples/fav-moments-2026-10.md): a fictional October 2026

## Planned repo layout

```
core/       Pure Kotlin: data model, processing steps, prompts, stats, Markdown export
app/        Android app (Kotlin + Jetpack Compose), on-device speech-to-text + LLM
box/        Raspberry Pi bedside box (V2)
docs/       concept, architecture, formats
examples/   sample handover files
```

## Status

💭 Concept stage. First build step: a laptop test, entirely offline, that turns a recording into
a transcript and then into moments in JSON. It's for choosing models and testing prompts on real
evenings before porting to the phone.
