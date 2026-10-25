# `d2g` on your Mac

The laptop tool. Same processing as the phone will do, running offline on your Mac, writing
into a space folder you open as an Obsidian vault.

## One-time setup

```sh
brew install openjdk@21
export JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"   # add to ~/.zshrc

git clone https://github.com/noahblauensteiner/dream2gether.git
cd dream2gether
git checkout ccr-b1b5ed18-ozzraj
./gradlew :cli:installDist
alias d2g="$PWD/cli/build/install/d2g/bin/d2g"                                      # add to ~/.zshrc
```

You also need [Obsidian](https://obsidian.md) 1.9 or newer (for the dashboard).

Later items need `brew install ffmpeg whisper-cpp llama.cpp` plus model files; their steps
get added here when they land.

After `git pull`, run `./gradlew :cli:installDist` again.

## Try the demo

```sh
d2g demo ~/dream2gether-demo
```

In Obsidian: **Open folder as vault** → `~/dream2gether-demo` → open **Start here**.
The demo is a fictional October for Alex & Sam. Its audio files are one second of silence.

## Make your own space

```sh
d2g init-space ~/dream2gether-space --name "Noah & …" --member noah:Noah --member kim:Kim
```

- `--member` is `id:Name`. The id is lowercase and goes into file names.
- Keep the space **outside** this repo. It holds your real nights and must never be committed.

## Typed chat → moments

Pretend you talked, type it, and let the local model pick the moments. Nothing leaves the Mac:
llama.cpp runs the model on the Mac's GPU, on `127.0.0.1` only.

```sh
brew install llama.cpp
export D2G_LLM_HF=ggml-org/gemma-3-12b-it-GGUF        # ~7 GB, downloaded once; add to ~/.zshrc

d2g process-transcript examples/chats/pretend-saturday.txt \
  --space ~/dream2gether-space --by noah --at "2026-10-24 22:41"
```

- The chat file has one line per turn, `Name: text` (see `examples/chats/`).
- `--at` says when you talked. "On Wednesday" is then resolved to the right date.
- **Phone-sized model** (what an Android phone could run): `--hf ggml-org/gemma-3-4b-it-GGUF`.
- **Faster repeats.** Each run loads the model again. Keep one server running instead:

  ```sh
  llama-server -hf ggml-org/gemma-3-12b-it-GGUF --port 8080      # in its own terminal
  export D2G_LLM_URL=http://127.0.0.1:8080
  ```

- **Same time twice.** The same `--by` and `--at` is the same night, so the second run is
  refused. Pick another time.

## Commands

| Command | What it does |
|---|---|
| `d2g init-space DIR --name … --member id:Name …` | New empty space with the Obsidian starter kit |
| `d2g demo DIR` | Demo space with a fictional October |
| `d2g process-transcript FILE --by id [--at …] [--space DIR]` | Typed chat → night note + moment notes |
| `d2g --help` | All commands and options |
