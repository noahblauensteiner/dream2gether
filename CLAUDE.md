# dream2gether

## Talk style
Caveman mode. Short. Few words. Bullets over prose. No fluff, no recap of what user said.
Code and docs normal quality; chat replies terse.

## Project
Fully on-device couples' favourite-moments recorder. See `docs/`.
- V1 any Android phone, English only, no network permission.
- Shared space = folder of Markdown notes, also an Obsidian vault. Either phone records, processes on-device, writes night note + one note per moment.
- Sync: manual USB copy first, Syncthing later. No Git/cloud sync.
- Laptop view = Obsidian. No Electron.
- Files = source of truth. Phone DB = rebuildable index.
- Keep audio. Nothing leaves devices except user export/backup.

## Pilot workflow
- `backlog.md` = work queue. Take top ⬜ item not blocked. Mark 🔨 while working, ✅ when done.
- After each item: tests green, add caveman entry on top of `log.md`, commit, push.
- Every log heading has a UTC timestamp: `## YYYY-MM-DD HH:MM UTC · B-xxx title status` (`TZ=UTC date '+%Y-%m-%d %H:%M UTC'`).
- ⏸ items wait for Noah. Skip them, don't fake them.
- Never commit real recordings or space folders. Synthetic test data only.
- Noah tests on a MacBook (Apple M5, 24 GB) with Obsidian. Each Mac-epic item must end with steps he can run there.

## Build
- `./gradlew build` (JDK 21). Modules: `core` (shared logic), `cli` (laptop tool `d2g`).
- Cloud box only: Maven Central rate-limits; `~/.gradle/init.d/mirror.gradle.kts` points at Google's mirror. Not in repo.
