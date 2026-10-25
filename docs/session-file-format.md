# Space folder & file formats

A couple (or family) shares one **space**: a folder of plain Markdown notes shared between their
devices. It's also a ready-made [Obsidian](https://obsidian.md) vault, so you can open it on a
laptop and browse, edit, comment, arrange and build albums with no extra app.

- **Any member's phone can record.** That phone processes the night itself and writes:
  - one **night note**: metadata, the audio, links to its moments, the full transcript;
  - one **moment note** per favourite moment: properties, the story, quote, photos, comments.
- **The files are the source of truth.** Each phone's database is only an index, rebuilt from
  the files. Edits made in Obsidian show up in the app and the other way round.
- **Sharing between devices** starts as manual copying (see [syncing](#syncing)). Syncthing
  comes later.

## Folder layout

```
dream2gether-space/
├── space.md                                     members, language, timezone, glossary
├── sessions/2026-10/
│   └── s-2026-10-07-2250-sam.md                 night note (one per recording)
├── moments/2026-10/
│   ├── m-2026-10-07-ramen-n9qe.md               moment note (one per moment)
│   └── m-2026-10-17-storm-t3ob.md
├── audio/2026-10/
│   └── s-2026-10-07-2250-sam.m4a                full-night recording
├── photos/2026-10/
│   └── m-2026-10-07-ramen-n9qe-1.jpg
├── collections/
│   ├── best-food-2026.md                        album / project / artwork list
│   └── october-board.canvas                     free-form board (Obsidian Canvas)
├── months/
│   └── 2026-10.md                               month note: recap + journal
├── dreams.md                                    dream list (V1.5)
└── .obsidian/snippets/dream2gether.css          optional styling for Obsidian
```

- **Unique names.**
  - Night notes are `s-{date}-{HHmm}-{member}`.
  - Moment notes are `m-{happened_on}-{slug}-{4 random chars}`.
  - Photos are `{moment id}-{n}`.

  So two devices never create the same file name. Copying folders back and forth never clashes
  on new files.
- **Names never change.** File names stay fixed even when you edit a title, so links never break.
  The display title is the `title` property.
- **Links by name.** Notes link with Obsidian wikilinks by file name (`[[m-2026-10-07-ramen-n9qe]]`).
  The app resolves them the same way, by name anywhere in the space.

## `space.md`

```markdown
---
type: space
schema: dream2gether/space@1
name: Alex & Sam
members:
  - alex
  - sam
member_names:
  - Alex
  - Sam
language: en
timezone: Europe/Vienna
glossary_people:
  - Mila
  - Jonas
  - Grandma Rosi
  - Pepper
glossary_places:
  - farmers' market
  - boulder gym
  - old cinema
---

# Alex & Sam 🌙
```

The glossary keeps names spelled the same in every transcript. Properties are kept flat so
they're editable in Obsidian's properties panel.

## Night note

```markdown
---
type: night
schema: dream2gether/night@1
id: s-2026-10-07-2250-sam
recorded_by: sam
device: sams-phone
recorded_at: 2026-10-07T22:50:12+02:00
duration_s: 612
language: en
question: What was your favourite moment today?
audio: "[[s-2026-10-07-2250-sam.m4a]]"
status: extracted
stt_model: whisper-small.en
llm_model: gemma-4-e2b
prompt_version: extract@1
processed_at: 2026-10-08T03:12:40+02:00
---

# 🌙 Wed 7 Oct · 10 min · recorded by Sam

> *What was your favourite moment today?*

![[s-2026-10-07-2250-sam.m4a]]

## Moments

- [[m-2026-10-07-ramen-n9qe|🍜 Ramen from scratch]] · 01:02

## Dreams

- 💭 Learn to make proper pho · 07:31

## Transcript

**[00:00]** Okay. Favourite moment today, go.
**[00:03]** Easy. The ramen.
**[00:05]** Six hours for one bowl of soup and I'd do it again tomorrow.
…
```

- The audio embed gives a player in Obsidian.
- Transcript lines are `**[mm:ss]** text`. Lines in a language other than the night's get a tag:
  `**[03:12 · nl]**`.

## Moment note

```markdown
---
type: moment
schema: dream2gether/moment@1
id: m-2026-10-07-ramen-n9qe
title: Ramen from scratch
emoji: 🍜
date: 2026-10-07
date_end:
date_confidence: exact
who: both
categories:
  - food
  - cozy
places:
  - home
people: []
first_time: true
star: true
private: false
night: "[[s-2026-10-07-2250-sam]]"
audio_from: "01:02"
audio_to: "02:14"
photo_hint:
generated_hash: 9f2c41d0
tags:
  - moment
---

# 🍜 Ramen from scratch

Six hours of broth, two burnt eggs, one perfect bowl each. We ate on the living room floor
because the table was still covered in flour.

> "Six hours for one bowl of soup and I'd do it again tomorrow."

![[m-2026-10-07-ramen-n9qe-1.jpg]]

## Comments

> [!comment] Alex · 8 Oct
> ==Best soup of my life.== Next time: less salt.
```

| Feature | How it's stored | In the app | In Obsidian |
|---|---|---|---|
| ⭐ Star / highlight a moment | `star: true` | tap ⭐ | properties panel |
| Highlight text | `==text==` | select → highlight | type `==` or use the format menu |
| Comment | `> [!comment] {name} · {date}` callout under `## Comments` | 💬 under the moment | type a callout |
| Private | `private: true`: stays in the space, never in recaps or exports | 🙈 | properties panel |
| Categories | `categories` list ([keys](concept.md#categories)) | chips | properties panel; Bases views can filter on it |
| Photos | `![[photo]]` embeds | 📷 add | drag an image in |
| Hear it | `audio_from` / `audio_to` into the night's audio | ▶️ plays that stretch | open the night note's player |

**Re-processing never clobbers your edits.**
- `generated_hash` is a hash of the moment as the app wrote it.
- When a night is re-processed (better model, new language), the app replaces only moment notes
  whose content still matches that hash, meaning nobody touched them.
- Edited moments stay as they are. The app offers the new version as a suggestion instead.

## Collections: albums, projects, artworks

A collection is a note that embeds moments. Its order is the order of the lines: drag to
reorder in the app, or move the lines in Obsidian.

```markdown
---
type: collection
schema: dream2gether/collection@1
kind: album          # album | project | artwork | photo-book | other
title: 🍝 Best food of 2026
cover: "[[m-2026-10-07-ramen-n9qe-1.jpg]]"
---

# 🍝 Best food of 2026

Everything we loved eating this year.

![[m-2026-10-03-chestnuts-p8nc]]
![[m-2026-10-07-ramen-n9qe]]
```

- **App:** "Add to…" on any moment appends an embed line to the chosen collection.
- **Obsidian:** the embeds show each moment in full, so the note reads like a little album.
- **Boards (artworks, mood boards):** use Obsidian Canvas (`.canvas`, the open JSON Canvas
  format) to place moment notes and photos freely. In V1 the app lists canvases and opens them
  in Obsidian; it doesn't edit them.

## Month note

`months/2026-10.md` holds the month's title, the generated stats (as properties), the journal
text (yours to edit), the highlights, and embeds of the month's moments. In Obsidian it's a
live page.

**Export** renders it into the self-contained [handover format](journal-handover-format.md):
moment text copied in, plain Markdown links, photos and voice clips in the bundle.

## Syncing

- **Start: manual.**
  - Copy the space folder between phone and laptop over USB. On the phone the space lives in a
    normal folder (e.g. `Documents/dream2gether-space`), so it shows up over USB.
  - Because new files never share a name, copying in both directions is safe. Use "copy newer
    files" so edited notes aren't overwritten by older copies; FreeFileSync or `rsync --update`
    do this.
- **Later: Syncthing**, the same folder synced automatically between phones and laptop.
- **Same file edited on two devices.** Whichever way it syncs, a duplicate or conflict copy can
  appear. The app spots it and asks *"Two versions of 'Ramen from scratch'. Keep which?"*.
  Because every moment has its own file, this is rare.

## Obsidian starter kit

The app creates these when it sets up a new space:

- `.obsidian/snippets/dream2gether.css`: warm styling for `[!comment]` callouts and moment
  headers.
- `collections/All moments.base`: an Obsidian Bases view (built-in database views) listing
  moment notes as cards, filterable by month, category and star. The exact file gets worked out
  in M0.
