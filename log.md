# Log

Pilot log. Newest on top. Caveman talk. Times in UTC.

## 2026-10-25 02:04 UTC · B-021 typed chat → moments ✅
- `d2g process-transcript chat.txt --by noah --at "…"`: local AI reads chat, writes night + moment notes.
- AI talks to `llama-server` on Mac (127.0.0.1 only). Answer forced into JSON shape.
  Me check shape with llama.cpp's own converter: it works.
- AI never does date maths. It says "weekday:wednesday", code finds the date. After midnight still counts as the evening.
- Guards: fake quotes thrown away, unknown people → "both", only real categories, glossary spelling wins.
- Long chats cut in pieces, same moment twice = merged.
- `generated_hash` saved, so later re-runs keep your edits (new item B-025).
- 55 tests green.
- **Try on Mac:**
  1. `git pull`, `./gradlew :cli:installDist`, `brew install llama.cpp`
  2. `d2g init-space ~/dream2gether-space --name "Noah & …" --member noah:Noah --member <id>:<Name>`
  3. `export D2G_LLM_HF=ggml-org/gemma-3-12b-it-GGUF` (first run downloads ~7 GB)
  4. `d2g process-transcript examples/chats/pretend-saturday.txt --space ~/dream2gether-space --by noah --at "2026-10-24 22:41"`
  5. Look in Obsidian: dashboard shows the moments? Good titles? Dates right?
- Next: B-022 voice memo → moments.

## 2026-10-25 01:39 UTC · B-020 vault + dashboard ✅
- `d2g init-space` makes vault: `space.md`, folders, Start here, `Moments.base` dashboard, `Nights.base`, CSS.
- `d2g demo` makes fake October (Alex & Sam): 12 nights, 12 moments, 1 album. Audio = 1 sec silence.
- Dashboard views: All moments (cards), By month, Starred ⭐, Table. Card no photo = category colour.
- Bases syntax from Obsidian help repo. Card image keys (`image`, `imageFit`) = best guess, check on Mac.
- 36 tests green.
- **Try on Mac** (setup in `cli/README.md`):
  1. `brew install openjdk@21`, set `JAVA_HOME`, clone, `./gradlew :cli:installDist`
  2. `d2g demo ~/dream2gether-demo`
  3. Obsidian → Open folder as vault → `~/dream2gether-demo` → Start here
  4. Tell me: cards look nice? colours? By month ok? Audio player shows?
- Next: B-021 typed chat → moments.

## 2026-10-25 01:21 UTC · new epic: Mac + Obsidian 🧭
- You grill done: Mac runs full pipeline (`d2g`), Obsidian shows. Mac = M5, 24 GB.
- Input: voice memo file, record on Mac, typed chat. All three.
- Show first: nights + moments, moments dashboard, month recap page.
- Me make 5 items: B-020 vault + dashboard, B-021 typed chat → moments, B-022 voice memo,
  B-023 record on Mac, B-024 month recap page. Old B-005–B-008, B-011 folded in.
- Each item ends with something you try on Mac.
- Next: B-020.

## 2026-10-25 01:16 UTC · B-004 note files ✅ · B-002 CI ✅
- CI green on GitHub. Privacy guard works.
- Me teach brain to write + read all notes: space, night, moment, collection.
- Obsidian-proof: weird list styles, extra properties, extra sections, CRLF, hand comments, `==highlights==`.
  Read → write again → nothing lost.
- 24 tests green.
- Docs examples now match what code writes (block lists).
- Next: B-005 date resolver ("on Saturday" → real date).

## 2026-10-25 01:10 UTC · B-003 core model ✅
- Brain shapes made: Space, Member, Night, Segment, Moment, Comment, Collection, 11 categories.
- Ids: `s-2026-10-07-2250-sam` for nights, `m-2026-10-07-ramen-from-scratch-k3f9` for moments.
  Random tail = two phones never clash.
- 8 tests green.
- Log now has clock times, like you ask.
- Next: B-004 note files (write + read, Obsidian-safe).

## 2026-10-25 01:08 UTC · B-001 skeleton ✅ · B-002 CI 🔨
- Gradle project made: `core` (shared brain), `cli` (laptop tool `d2g`). Kotlin 2.2, JDK 21.
- `.gitignore` blocks audio, spaces, models. Only `eval/synthetic/` audio allowed.
- Maven Central angry at cloud box (429). Me use Google mirror, local only. CI uses normal Maven.
- CI workflow pushed: privacy guard + build. Wait green.

## 2026-10-25 01:02 UTC · kickoff
- Backlog made. V1 cut into items B-001 … B-504.
- Cloud box: Java + Gradle yes. Android SDK no. Model download no. Me build `core` + `cli` here,
  Android APK via GitHub Actions.
- Real voices never leave home. Me test with fake chats. You test real nights on laptop/phone (⏸ items).
- Next: B-001 skeleton.
