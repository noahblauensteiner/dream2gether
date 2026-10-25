# Backlog

Pilot: Claude. Pulls the top open item, builds it, logs it with a timestamp in [log.md](log.md),
commits, pushes.
Specs live in [docs/](docs/). Each item is done when its checks pass and its tests are green.

**Status:** ⬜ todo · 🔨 doing · ✅ done · ⏸ waiting on you (Noah) · 🚫 blocked

**Ground rules**
- Never commit real recordings or a real space folder. Tests use synthetic data only.
- The app gets no network permission. Everything runs offline.
- Real-voice testing happens on your devices, never in CI.

## V1: M0b Mac + Obsidian (epic, now)

Goal: run the whole thing on your MacBook (M5, 24 GB) and see it in Obsidian. Everything runs
offline on the Mac (whisper.cpp and llama.cpp with Metal). The old items B-005–B-008 and B-011
are folded in here.

| ID | Item | You can try | Status |
|---|---|---|---|
| B-020 | **Vault + dashboard.** `d2g init-space` creates a vault: `space.md`, folders, a CSS snippet, a moments dashboard (Bases: cards by month, category, ⭐), and a "Start here" note. `d2g demo` fills it with synthetic October nights. Mac setup guide (Homebrew, one build command). | Open the vault in Obsidian and browse the dashboard | ✅ |
| B-021 | **Typed chat → moments.** `d2g process-transcript chat.txt --by <you> --at <date time>`. Local LLM via `llama-server`, output forced to a JSON schema. Date resolver ("on Saturday" → date), extraction prompt v1, `generated_hash`. Fake-LLM tests in CI. | Write a pretend bedtime chat and see moment notes appear | ⬜ |
| B-022 | **Voice memo → moments.** `d2g process memo.m4a`: ffmpeg → whisper.cpp → transcript, then B-021. Audio copied into the vault, so the night note plays it and each moment shows its ▶️ time range. | Drop in a phone or Mac voice memo | ⬜ |
| B-023 | **Record on the Mac.** `d2g record`: records from the Mac mic, stops on Enter, after ~3 min of silence, or at 30 min, then processes. The auto-stop logic lives in `core`, so Android reuses it. | Talk at night next to the MacBook | ⬜ |
| B-024 | **Month recap page.** `d2g recap 2026-10` writes `months/2026-10.md`: stats computed in code, a title and journal from the LLM, highlights, and the month's moments embedded. Your journal edits are kept on re-run. | Open "October Wrapped" in Obsidian | ⬜ |

## V1: M0 Foundation & laptop test

| ID | Item | Done when | Status |
|---|---|---|---|
| B-001 | Repo skeleton: Gradle multi-project (`core`, `cli`), Kotlin/JVM, version catalog, `.gitignore` that blocks audio and space folders | `./gradlew build` green | ✅ |
| B-002 | CI: GitHub Actions builds and tests on every push; a guard fails if audio files appear outside `eval/synthetic/` | Workflow green on the branch | ✅ |
| B-003 | Core model: Space, Night, Moment, Collection, the categories enum, id generation (`s-…`, `m-…-xxxx`) | Unit tests | ✅ |
| B-004 | Note files: write and read space, night, moment and collection notes per [formats](docs/session-file-format.md). The parser tolerates Obsidian edits and keeps unknown content. | Round-trip tests, including hand-edited fixtures | ✅ |
| B-009 | Synthetic eval set: 5 scripted English bedtime chats with expected moments (`eval/`) | Files plus a format check | ⬜ |
| B-010 | `d2g eval`: score extracted moments against expected ones (title, date, categories) | Report printed; test on canned outputs | ⬜ |
| B-012 | Model bake-off on your Mac: phone-size (~2–4B) vs Mac-size (~12B) LLMs, whisper `small.en` vs `large-v3-turbo`; pick defaults | Results in `eval/RESULTS.md` | ⏸ |
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
