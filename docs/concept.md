# Concept

## The ritual

It's late, the lights are off, you're both in bed.
One of you taps the button. The phone (later, the bedside box) shows or says tonight's question:

> *"What was your favourite moment today?"*

You talk for two to fifteen minutes, then tap again. If you forget, it stops on its own after a
few minutes of silence. Then you go to sleep.

The next morning there's a small card: **"Last night you remembered 3 moments ✨"**, with
chestnuts at the market, the storm and the ramen that took six hours. At the end of the month you
get **"October Wrapped"**, a playful recap and a journal you can edit, add photos to and export.

## Principles

1. **Zero friction at night.** One tap, no typing and no reading. In V2 there isn't even a
   screen lighting up the room.
2. **Warm, not business.** Use emoji, stickers, hand-drawn touches and playful copy. Avoid
   axes, percentages, KPIs and anything that feels like a performance review of your
   relationship.
3. **Low effort by default, editable always.** Moments land in the journal automatically. You
   edit, merge, star or hide them whenever you feel like it, and never *have* to review.
4. **Your memories, your files.** Data lives at home and every month exports to a plain
   Markdown bundle you can keep forever.
5. **Privacy first.** Recording only happens on an explicit press and always stops on its own.
   Audio stays home. Any moment can be marked *private* so it never leaves the app. Details are
   in [architecture.md](architecture.md#privacy--trust).

## User flows

### 1. Nightly capture
- Tap **Record**. The screen dims to a single soft pulsing dot, and tonight's question is shown
  briefly, or read out quietly (optional).
- Tap again to stop. Auto-stop kicks in after ~3 min of silence or a hard cap of 30 min, so it
  never records the whole night.
- Long-press to cancel, which throws the recording away without processing it.
- A "forget last night" action deletes the whole session, including audio, transcript and
  moments.

### 2. Morning "Last night" card
- A notification or app card lists the extracted moments: title, emoji, date, a one-line
  summary.
- Quick actions: ⭐ star, ✏️ edit, 🔗 merge with an existing moment ("we talked about the
  cinema again"), 🙈 private, 🗑️ delete, 📷 add photo.

### 3. Weekly and monthly specials
- **Sunday:** *"What were your favourite things this weekend?"*
- **Last day of the month:** *"What was your moment of the month?"* The answer feeds the
  recap's highlights directly.

### 4. Monthly recap ("Month Wrapped")
A short swipeable story ending in the editable journal and an export button. See below.

### 5. Photos
- **V1:** add photos to a moment with the Android Photo Picker. On Android 12+ the picker can
  also show backed-up Google Photos items.
- **V1.5:** *"We found 6 photos from Sat 18 Oct, add some?"*, suggested from the phone's own
  gallery by date and location.

### 6. Dream list (V1.5, the "dream" in dream2gether)
When you say things like *"we should totally go back there"* or *"let's learn to make pho"*,
those get captured as dreams. When a later moment fulfils one, it's ticked off: *"Dream come true
✅ First bouldering session"*.

## Features by version

| Feature | V1 | V1.5 | V2 | Later |
|---|:-:|:-:|:-:|:-:|
| One-tap recording, auto-stop | ✅ | | | |
| Local transcription and moment extraction | ✅ | | | |
| Moments inbox: edit, merge, star, private | ✅ | | | |
| Evening questions (rotation, weekly/monthly specials) | ✅ | | | |
| Gentle bedtime reminder (opt-in) | ✅ | | | |
| Manual photo upload per moment | ✅ | | | |
| Monthly recap screen | ✅ | | | |
| Handover export (`.md` + photos) | ✅ | | | |
| Dream list | | ✅ | | |
| Photo suggestions from phone gallery | | ✅ | | |
| "One year ago tonight" | | ✅ | | |
| Bedside box with physical button and LED | | | ✅ | |
| E-ink "question of the night" / "3 new moments" | | | ✅ | |
| Two-button "talking stick" (who said what) | | | ✅ | |
| Google Photos album from the month's photos | | | | ✅ |
| Printed photo book / PDF | | | | ✅ |
| Year Wrapped | | | | ✅ |

## The monthly recap: fun, not business

A Spotify-Wrapped-style sequence of cards. Each card is one idea, big and playful:

1. **Cover**: a title Claude writes for the month (*"October: Chestnuts, Storms & a Very Brave
   Spider"*), the emoji of the month and a collage of the month's photos.
2. **Calendar of hearts**: every day with a moment shows its emoji, plus your longest streak of
   nights.
3. **Top highlights**: starred moments and the "moment of the month" answer.
4. **Category podium**: 🛋️ cozy · 🍝 food · 🤪 silly … as stickers on a podium, not a bar chart.
5. **Cast & places**: *"Starring: Mila & Jonas (2×)"*, *"Your spot: the farmers' market"*.
6. **Firsts**: *"First time: bouldering, ramen from scratch"*.
7. **Quote of the month**: one line from your own words.
8. **Who remembered what**: a playful split (*"Alex remembers the wins, Sam remembers the
   food"*).
9. **Dreams**: new dreams and dreams that came true.
10. **Journal**: the narrative, editable, with the **Export** button.

Numbers are computed in code so they're always right. Claude only writes the words: the title,
the journal and the "who remembered what" line.

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

## Open decisions

These change the build. My recommended default is in brackets.

1. **Language** you talk in at night (German? English? both?). This sets the transcription model
   and the recap language. *(Whisper multilingual `small`, language pinned per couple)*
2. **Is your partner on Android or iPhone?** *(both on Android → Kotlin/Compose; iPhone in the
   mix → Flutter)*
3. **Where does the hub run in V1?** *(the Raspberry Pi you'll use for V2 anyway; a laptop is
   fine for development)*
4. **Transcript text to the Claude API, or a fully local LLM?** *(Claude: much warmer, more
   accurate summaries for a few dollars a month; audio stays local either way)*
5. **Attribute moments to each of you, or keep everything "ours"?** *(best-effort attribution,
   editable; perfect attribution comes with the V2 talking stick)*
6. **Keep the audio?** Deleting is more private. Keeping it means you can hear your own voices
   years from now, which might be the most precious part. *(delete by default, with a per-night
   "keep this one" toggle)*
