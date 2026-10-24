# Journal handover format

`fav-moments-YYYY-MM.md`: one Markdown file per month that says exactly **what lands in that
month's journal**.

It is the contract between dream2gether and anything downstream: a Google Photos album, a
printed photo book, Notion, or just a folder you keep forever. V1 only *produces* it; nothing
consumes it automatically yet. That's deliberate, because it lets us design the integrations
later without touching the app.

**Design goals**

- Readable as-is by a human, in any Markdown viewer.
- Easy for a program to parse:
  - YAML front matter for metadata and stats;
  - a predictable block per moment;
  - one machine-readable album plan at the end.
- Self-contained: the file plus its `photos/` folder is the complete month.

A full example is in [`examples/fav-moments-2026-10.md`](../examples/fav-moments-2026-10.md).

## Bundle layout

```
fav-moments-2026-10/
├── fav-moments-2026-10.md
└── photos/
    ├── 2026-10-03-chestnuts-1.jpg
    ├── 2026-10-07-ramen-1.jpg
    └── …
```

- Photos are named `{happened_on}-{slug}-{n}.{ext}`.
- They are copied as originals, with EXIF kept, since this is a keepsake.
- The app shares the bundle as a zip.

## What lands in the journal

| Included | Excluded |
|---|---|
| Moments whose `happened_on` falls in the month (not the recording date: Sep 30 told on Oct 2 belongs to September) | Moments marked **private** |
| Approximate dates, placed by their start date | Moments that are hidden, deleted or merged into another moment |
| Photos attached to included moments | Transcripts and audio (never exported) |
| Dreams added or fulfilled this month | Dreams marked private |
| The latest journal text (your edits win over generated text) | |

- **The database is the source of truth.** Re-exporting regenerates the file. Hand edits to the
  `.md` are not imported back in V1.
- **Late moments.** If a moment for an already-exported month is added later, the app shows
  *"1 late moment added to September"* and offers a re-export.

## Structure

1. YAML front matter
2. `# {emoji} {title}` and a subtitle line
3. `## Journal`: the narrative, written in "we" voice
4. `## Highlights`: starred moments, with the moment of the month first
5. `## Moments`, grouped by ISO week: `### Week 41 · 5–11 Oct`
6. `## The month in numbers`
7. `## Dream list`
8. `## Album plan`: one fenced `yaml` block

### Front matter

| Field | Type | Notes |
|---|---|---|
| `schema` | string | `dream2gether/journal-handover@1` |
| `type` | `monthly` | `weekly`, `yearly` and `custom` are reserved for later |
| `period` | `YYYY-MM` | |
| `title`, `emoji` | string | Claude-written, editable |
| `couple.a`, `couple.b` | string | display names |
| `language` | BCP 47 | e.g. `de`, `en` |
| `timezone` | IANA | e.g. `Europe/Vienna` |
| `generated_at` | ISO 8601 | |
| `journal_edited` | bool | true if the journal text was changed by hand |
| `stats` | map | `nights_recorded`, `moments_in_journal`, `moments_private`, `longest_streak_nights`, `photos`, `categories` (key → count), `who` (`both`/`a`/`b` → count), `top_people`, `top_places`, `firsts` |

Stats are computed by the hub, never by the LLM.

### Moment block

```markdown
#### {emoji} {title}
<!-- moment: {id} -->
**When** {Sat 17 Oct | around 26–28 Oct (approx.)} · **Where** {place} · **Who** {both | name} · **Tags** {categories}{ · ✨ first time}{ · ✅ dream come true}{ · ⭐}

{description, 1–4 sentences}

> "{quote}" — {quote_by}

![{caption}]({photos/file})
_No photo yet · hint: {photo_hint}_
```

- The HTML comment carries the stable moment id. It's invisible when rendered, and parsers use
  it to anchor each block.
- Each moment has either image lines or the "no photo yet" line, never both.
- **Where**: `–` when unknown.
- **Tags**: category keys from [concept.md](concept.md#categories), plus free tags.

### Album plan

The final section is a single fenced `yaml` block. It's a ready-made brief for building a photo
album: which moments, which dates, which photos are already picked, and what to look for when a
moment has no photo yet. See the [Google Photos notes](architecture.md#google-photos-later-whats-possible)
for why the user still picks the photos themselves.

```yaml
album:
  title: "Fav moments · October 2026"
  description: "October: Chestnuts, Storms & a Very Brave Spider"
  cover: photos/2026-10-07-ramen-1.jpg
  items:
    - moment: m-2026-10-17-storm
      date: 2026-10-17              # or: { from: 2026-10-26, to: 2026-10-28 }
      photos: []
      want_photo: true
      search_hints: [window, rain, tea]
```

## Versioning

Adding fields keeps `@1`. Renaming or removing anything means `@2`, and the exporter keeps
writing the old version until all consumers have moved.
