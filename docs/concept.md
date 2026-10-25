# Concept

## The ritual

It's late, the lights are off, you're both in bed.
One of you taps the button. The phone (later, the bedside box) shows tonight's question:

> *"What was your favourite moment today?"*

You talk for two to fifteen minutes, then tap again. If you forget, it stops on its own after a
few minutes of silence. Then you go to sleep. The phone is on the charger, so it does the
processing overnight.

The next morning there's a small card: **"Last night you remembered 3 moments ✨"**, with
chestnuts at the market, the storm and the ramen that took six hours. Tap ▶️ on a moment to hear
yourselves telling it. At the end of the month you get **"October Wrapped"**, a playful recap and
a journal you can edit, add photos to and export.

## Principles

1. **Nothing leaves the device.** Recording, transcription and summarising all happen on the
   phone (V1) or on the bedside box (V2). There are no cloud services, no accounts and no
   analytics. The V1 app doesn't even ask for network permission. The only time anything leaves
   is when *you* export or back up, to a place *you* choose.
2. **Your voices are the treasure.** Recordings are kept. Transcripts and summaries are only the
   index into them.
3. **Zero friction at night.** One tap, no typing and no reading. In V2 there isn't even a
   screen lighting up the room.
4. **Warm, not business.** Use emoji, stickers, hand-drawn touches and playful copy. Avoid
   axes, percentages, KPIs and anything that feels like a performance review of your
   relationship.
5. **Low effort by default, editable always.** Moments land in the journal automatically. You
   edit, merge, star or hide them whenever you feel like it, and never *have* to review.
6. **Your memories, your files.** Every month exports to a plain Markdown bundle. A full
   encrypted backup is always one tap away.

## User flows

### 1. Nightly capture
- Tap **Record**. The screen dims to a single soft pulsing dot, and tonight's question is shown
  briefly.
- A persistent notification shows that the microphone is on, for as long as it is.
- Tap again to stop. Auto-stop kicks in after ~3 min of silence or a hard cap of 30 min, so it
  never records the whole night.
- Long-press to cancel, which throws the recording away.
- Processing starts once the phone is charging (or right away with "process now").

### 2. Morning "Last night" card
- A notification or app card lists the extracted moments: title, emoji, date, a one-line summary
  and a ▶️ button that plays the part of the recording where you talked about it.
- Quick actions: ⭐ star, ✏️ edit, 🔗 merge with an existing moment ("we talked about the cinema
  again"), 🙈 private, 🗑️ delete, 📷 add photo.
- "Forget last night" deletes the whole session, audio included.

### 3. Weekly and monthly specials
- **Sunday:** *"What were your favourite things this weekend?"*
- **Last day of the month:** *"What was your moment of the month?"* The answer feeds the recap's
  highlights directly.

### 4. Monthly recap ("Month Wrapped")
A short swipeable story ending in the editable journal and an export button. See below.

### 5. Photos
- **V1:** add photos to a moment with the Android Photo Picker. The app gets only the photos you
  pick and copies them into its own storage.
- **V1.5:** *"There are 6 photos from Sat 18 Oct on this phone, add some?"*, matched locally by
  date and location.

### 6. Dream list (V1.5, the "dream" in dream2gether)
When you say things like *"we should totally go back there"* or *"let's learn to make pho"*,
those get captured as dreams. When a later moment fulfils one, it's ticked off: *"Dream come true
✅ First bouldering session"*.

### 7. Backup
Export an encrypted archive to a USB stick or a computer: every recording, transcript, moment
and photo. It restores onto a new phone and imports into the V2 box. The app never backs up to
the cloud by itself.

## Features by version

| Feature | V1 | V1.5 | V2 | Later |
|---|:-:|:-:|:-:|:-:|
| One-tap recording, auto-stop, audio archive | ✅ | | | |
| On-device transcription (English) | ✅ | | | |
| On-device moment extraction | ✅ | | | |
| ▶️ Hear-it playback per moment | ✅ | | | |
| Moments inbox: edit, merge, star, private | ✅ | | | |
| Evening questions (rotation, weekly/monthly specials) | ✅ | | | |
| Gentle bedtime reminder (opt-in) | ✅ | | | |
| Manual photo upload per moment | ✅ | | | |
| Monthly recap screen | ✅ | | | |
| Handover export (`.md` + photos + voice clips) | ✅ | | | |
| Encrypted full backup and restore | ✅ | | | |
| Dream list | | ✅ | | |
| Photo suggestions from the phone's gallery | | ✅ | | |
| "One year ago tonight" (with the old recording) | | ✅ | | |
| Bedside box: button, LED, local processing, archive | | | ✅ | |
| E-ink "question of the night" / "3 new moments" | | | ✅ | |
| Talking-stick mode (who said what) | | | ✅ | |
| 🇩🇪 German, 🇳🇱 Dutch | | | | ✅ |
| Mixing languages mid-sentence | | | | ✅ |
| iOS app | | | | ✅ |
| Year Wrapped with a "year in voices" montage | | | | ✅ |
| Printed photo book / PDF | | | | ✅ |
| Photo-album companion, run separately from the app on the export bundle | | | | ✅ |

## The monthly recap: fun, not business

A Spotify-Wrapped-style sequence of cards. Each card is one idea, big and playful:

1. **Cover**: a title for the month (*"October: Chestnuts, Storms & a Very Brave Spider"*), the
   emoji of the month and a collage of the month's photos.
2. **Calendar of hearts**: every day with a moment shows its emoji, plus your longest streak of
   nights.
3. **Top highlights**: starred moments and the "moment of the month" answer.
4. **Voice of the month** 🔊: a short clip of you two telling your favourite moment.
5. **Category podium**: 🛋️ cozy · 🍝 food · 🤪 silly … as stickers on a podium, not a bar chart.
6. **Cast & places**: *"Starring: Mila & Jonas (2×)"*, *"Your spot: the farmers' market"*.
7. **Firsts**: *"First time: bouldering, ramen from scratch"*.
8. **Quote of the month**: one line from your own words.
9. **Who remembered what**: a playful split, once moments have a "who" (see below).
10. **Dreams**: new dreams and dreams that came true.
11. **Journal**: the narrative, editable, with the **Export** button.

Numbers are computed in code so they're always right. The on-device model only writes the words:
the title, the journal and the playful lines. A small model writes simpler prose than a big cloud
model. That's fine, because the journal is a starting point you can edit, and the real highlight
is hearing your own voices.

**Who said it.** V1 has one microphone and no reliable way to tell your voices apart. So moments
default to "both of us", and you can set "who" and quote attributions yourself. V2's talking-stick
mode records it properly.

### Categories
A fixed list keeps the stats consistent across months. Free-form tags can be added on top.

| Key | Label |
|---|---|
| `us` | 💞 love & us |
| `cozy` | 🛋️ cozy at home |
| `food` | 🍝 food & drinks |
| `outdoors` | 🌲 outdoors & nature |
| `adventure` | 🧭 adventure & travel |
| `people` | 👯 friends & family |
| `culture` | 🎬 music, film, books, art |
| `sport` | 🧗 sport & body |
| `silly` | 🤪 silly & funny |
| `little-things` | ✨ little things |
| `wins` | 🏆 wins & proud moments |

## Evening questions

**Rotation**
- Default nightly: *"What was your favourite moment today?"*
- Every ~4th night: a random pick from the bank, so it doesn't get stale.
- Sunday: weekend favourites. Last day of the month: moment of the month.
- Special dates such as anniversaries, birthdays and the end of a holiday get their own
  question (*"Favourite memory of us from this past year?"*).
- The question is stored with the recording so extraction knows what you were answering.

**Starter bank**
- What made you laugh today?
- What's a small thing I did today that you liked?
- What was the best thing you ate today?
- Which moment from today would you replay?
- What surprised you today?
- What are you proud of today?
- A smell, sound or taste from today you want to remember?
- Where did you feel most like yourself today?
- What's something we should do again?
- What are you looking forward to this week?
- What was the coziest moment of the week?
- Who made your day better today?
- What did you learn today?
- What would today's title be if it were a movie?
- Which moment today felt like "us"?

## Languages

| Step | What | Notes |
|---|---|---|
| **V1** | 🇬🇧 English | English-only speech model (smaller and more accurate for English). UI, prompts and journal in English. |
| **Next** | 🇩🇪 German, 🇳🇱 Dutch | Multilingual speech model, with the language picked per couple. Prompts, examples, category labels and UI translated. Journal written in the language you choose. |
| **Later** | Mixing languages mid-sentence | E.g. "we went to the *markt* and it was *so gezellig*". It's a known weak spot of today's speech models and needs testing on your own recordings first. See [architecture](architecture.md#languages). |

The data model stores a language per recording, and per segment, from day one. Old English
nights and new mixed ones can then live side by side without a migration.

## Decided

- **Platform:** Android only for V1. iOS maybe later; the shared core is kept portable for it.
- **Privacy:** everything stays on the device, including the language model.
- **Audio:** kept, as the core of the archive. Deleting a single night is always possible.
- **Language:** English first.

## Still open

1. **Which phone will do the recording?** On-device models need RAM. A phone with ~8 GB or more
   runs a better model; a lighter one works with smaller models.
2. **Moments: auto-accept or review first?** *(recommended: auto-accept, edit when you like)*
