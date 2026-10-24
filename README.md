# dream2gether 🌙

> Lights off, one press, *"What was your favourite moment today?"*
> dream2gether listens, writes the moments down, and at the end of the month hands you
> a warm little journal of everything you loved together.

A home tinker project for couples who talk about their favourite moments at night.

## How it works

1. **Press** – tap the button in the app (V1) or on a bedside box (V2) and just talk.
2. **Remember** – the recording is transcribed at home and Claude picks out the favourite
   moments (what, when, where, who, a quote). It's ready when you wake up: *"Last night you
   remembered 3 moments ✨"*.
3. **Relive** – every month gets a playful recap ("October Wrapped") and a journal you can
   edit, add photos to and export as a single Markdown handover file.

## Roadmap

| Version | Capture | What you get |
|---|---|---|
| **V1** | Android app (one tap) | moments inbox & editing, evening questions, manual photos, monthly recap, `fav-moments-YYYY-MM.md` export |
| **V1.5** | Android app | dream list ("we should totally…"), photo suggestions from the phone gallery by date, "one year ago" |
| **V2** | Raspberry Pi bedside box with a physical button | no screen at night, LED feedback, optional e-ink "question of the night", two-button "talking stick" mode |
| **Later** | – | Google Photos album, printed photo book / PDF, Year Wrapped |

## Docs

- [Concept](docs/concept.md): the ritual, user flows, features per version, the fun monthly
  recap, evening questions, open decisions
- [Architecture](docs/architecture.md): hub + clients, pipeline, data model, tech stack,
  privacy, Pi hardware, cost
- [Journal handover format](docs/journal-handover-format.md): the `.md` contract for what
  lands in a month's journal
- [Example handover file](examples/fav-moments-2026-10.md): a fictional October 2026

## Planned repo layout

```
hub/        Python service: pipeline (transcribe → extract → recap → export), API, CLI
app/        Android app (Kotlin + Jetpack Compose)
box/        Raspberry Pi button daemon (V2)
docs/       concept, architecture, formats
examples/   sample handover files
```

## Status

💭 Concept stage. First build step: a pipeline spike (`audio file → transcript → moments JSON`)
to check that the core idea works on real recordings before any UI exists.
