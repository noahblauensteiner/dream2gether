# Backlog

Pilot: Claude. Pulls the top open item, builds it, logs it with a timestamp in [log.md](log.md),
commits, pushes.
Specs live in [docs/](docs/). Each item is done when its checks pass and its tests are green.

**Status:** ⬜ todo · 🔨 doing · ✅ done · ⏸ waiting on you (Noah) · 🚫 blocked

**Ground rules**
- Never commit real recordings or a real space folder. Tests use synthetic data only.
- The app gets no network permission. Everything runs offline.
- Real-voice testing happens on your devices, never in CI.

## V1: M0 Foundation & laptop test

| ID | Item | Done when | Status |
|---|---|---|---|
| B-001 | Repo skeleton: Gradle multi-project (`core`, `cli`), Kotlin/JVM, version catalog, `.gitignore` that blocks audio and space folders | `./gradlew build` green | ✅ |
| B-002 | CI: GitHub Actions builds and tests on every push; a guard fails if audio files appear outside `eval/synthetic/` | Workflow green on the branch | 🔨 |
| B-003 | Core model: Space, Night, Moment, Collection, the categories enum, id generation (`s-…`, `m-…-xxxx`) | Unit tests | ✅ |
| B-004 | Note files: write and read space, night, moment and collection notes per [formats](docs/session-file-format.md). The parser tolerates Obsidian edits and keeps unknown content. | Round-trip tests, including hand-edited fixtures | ⬜ |
| B-005 | Date-reference resolver (`today`, `yesterday`, `weekday:x`, `date:…`, `unknown` → date + confidence) | Unit tests incl. week and month edges | ⬜ |
| B-006 | Extraction contract: JSON schema, prompt v1, output validation → moment notes, `generated_hash`, chunk merge | Tests with canned model outputs | ⬜ |
| B-007 | Engine interfaces `SpeechToText` and `LanguageModel`, with fakes | Used by tests | ⬜ |
| B-008 | CLI `d2g`: `init-space`, `process <audio>`, `process-transcript <txt>`. Calls local whisper.cpp and llama.cpp binaries. | End-to-end test with fakes; how-to in `cli/README.md` | ⬜ |
| B-009 | Synthetic eval set: 5 scripted English bedtime chats with expected moments (`eval/`) | Files plus a format check | ⬜ |
| B-010 | `d2g eval`: score extracted moments against expected ones (title, date, categories) | Report printed; test on canned outputs | ⬜ |
| B-011 | Obsidian starter kit: CSS snippet and a Bases view, created by `init-space` | Generated in tests | ⬜ |
| B-012 | Model bake-off: run `d2g eval` with 2–3 small LLMs plus whisper on your laptop; pick the defaults | Results in `eval/RESULTS.md` | ⏸ |
| B-013 | First look: open a generated space in Obsidian and say if it feels right | Your OK | ⏸ |

## V1: M1 Android recorder & archive

| ID | Item | Done when | Status |
|---|---|---|---|
| B-101 | Android app skeleton (Compose, Kotlin, uses `core`). Manifest has no `INTERNET` permission and `allowBackup=false`. CI builds a debug APK artifact. | APK in CI; a test asserts the manifest permissions | ⬜ |
| B-102 | Space folder: pick it via the system folder picker, create a new space or open an existing one | Instrumented or unit tests on the file layer | ⬜ |
| B-103 | Recorder: foreground service (microphone type), AAC `.m4a`, notification, auto-stop on silence and at the cap, long-press cancel | Unit tests for the auto-stop logic | ⬜ |
| B-104 | On stop: write the audio and a bare night note into the space | Test with a fake recorder | ⬜ |
| B-105 | Nights list built from the files, plus playback; rebuild the index when the folder changes | UI test or screenshot | ⬜ |
| B-106 | Phone test: install the APK, record 2 nights, copy the folder to the laptop, open it in Obsidian | Your OK | ⏸ |

## V1: M2 On-device processing

| ID | Item | Done when | Status |
|---|---|---|---|
| B-201 | whisper.cpp through JNI (NDK build in CI); import a model via the file picker | Transcribes a synthetic clip in an instrumented test or on your phone | ⬜ |
| B-202 | Decode `.m4a` to 16 kHz PCM with MediaCodec | Unit test | ⬜ |
| B-203 | On-device LLM engine (LiteRT-LM or llama.cpp) with constrained JSON output; model import | Extracts from a synthetic transcript | ⬜ |
| B-204 | Overnight WorkManager job (while charging) plus "process now"; status tracked in the night note | Tests | ⬜ |
| B-205 | Pick model sizes by the phone's RAM | Unit test | ⬜ |
| B-206 | Real-night test on your phone | Your OK | ⏸ |

## V1: M3 Daily use

| ID | Item | Status |
|---|---|---|
| B-301 | Morning "Last night" card and notification | ⬜ |
| B-302 | Moment screen: edit fields, ⭐ star, 🙈 private, ==highlight==, 💬 comments | ⬜ |
| B-303 | ▶️ Hear-it: play a moment's stretch of the recording | ⬜ |
| B-304 | Photos via Photo Picker, copied into the space | ⬜ |
| B-305 | Collections: "Add to…", reorder, list canvases (open in Obsidian) | ⬜ |
| B-306 | Detect conflict copies and ask which version to keep | ⬜ |
| B-307 | Share a moment as a card | ⬜ |
| B-308 | Merge two moments | ⬜ |

## V1: M4 Recap & export

| ID | Item | Status |
|---|---|---|
| B-401 | Stats engine in `core` (counts, streaks, podium, cast and places) | ⬜ |
| B-402 | Month note: generated stats, LLM title and journal; your edits kept | ⬜ |
| B-403 | Recap story screen ("Month Wrapped") | ⬜ |
| B-404 | Handover export bundle (zip with photos and voice clips) per [format](docs/journal-handover-format.md) | ⬜ |
| B-405 | Encrypted backup and restore, plus a monthly reminder | ⬜ |

## V1: M5 Habit

| ID | Item | Status |
|---|---|---|
| B-501 | Evening questions: rotation and specials | ⬜ |
| B-502 | Bedtime reminder (opt-in) | ⬜ |
| B-503 | Optional biometric app lock | ⬜ |
| B-504 | Polish: accessibility, dark-room UI, empty states | ⬜ |

## Later (to be broken down when V1 is done)

- **V1.5:** dream list, photo suggestions from the gallery, "one year ago tonight".
- **V2:** Pi bedside box: button daemon, LED, e-ink display, talking-stick mode, Syncthing.
- **Languages:** German, Dutch, then mixing languages mid-sentence.
- **Later:** iOS, our own desktop app (Compose Multiplatform), Year Wrapped, photo book,
  photo-album companion.
