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

## Commands

| Command | What it does |
|---|---|
| `d2g init-space DIR --name … --member id:Name …` | New empty space with the Obsidian starter kit |
| `d2g demo DIR` | Demo space with a fictional October |
| `d2g --help` | All commands and options |
