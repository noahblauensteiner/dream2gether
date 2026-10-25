package app.dream2gether.core.space

/** Files that make a fresh space feel at home in Obsidian. */
object StarterKit {
    const val START_HERE = "Start here.md"
    const val MOMENTS_BASE = "Moments.base"
    const val NIGHTS_BASE = "Nights.base"
    const val CSS_SNIPPET = ".obsidian/snippets/dream2gether.css"
    const val APPEARANCE = ".obsidian/appearance.json"

    fun startHere(spaceName: String) = """
        # 🌙 Start here

        Welcome to **$spaceName**: your favourite moments, one note each.

        - 💫 **Moments**: the dashboard below, or open [[Moments.base]]. Views: all, by month, starred, table.
        - 🌙 **Nights**: every recording with its transcript, in [[Nights.base]].
        - 🗓️ **Months**: recap pages in the `months` folder.
        - 📚 **Collections**: albums, projects and artworks in the `collections` folder.

        ![[Moments.base]]

        ## Tips

        - ⭐ Star a moment: tick `star` in its properties.
        - 🙈 Keep a moment out of recaps and exports: tick `private`.
        - 💬 Comment under a moment's **Comments** heading:

          ```
          > [!comment] Your name · 25 Oct
          > What you want to say.
          ```

        - ==Highlight== text by wrapping it in `==`.
        - Dashboard empty? Turn on **Settings → Core plugins → Bases** (Obsidian 1.9 or newer).
        - Colours look plain? Turn on the `dream2gether` snippet under **Settings → Appearance → CSS snippets**.
    """.trimIndent() + "\n"

    /** Moment dashboard. Syntax: Obsidian "Bases syntax" docs. */
    val momentsBase = """
        filters:
          and:
            - 'type == "moment"'
        formulas:
          month: 'date.format("YYYY-MM")'
          when: 'date.format("ddd D MMM YYYY")'
        properties:
          emoji:
            displayName: " "
          title:
            displayName: Moment
          formula.when:
            displayName: When
          categories:
            displayName: Categories
          who:
            displayName: Whose
        views:
          - type: cards
            name: All moments
            order:
              - emoji
              - title
              - formula.when
              - categories
            sort:
              - property: date
                direction: DESC
            image: note.cover
            imageFit: cover
            imageAspectRatio: 0.6
          - type: cards
            name: By month
            groupBy:
              property: formula.month
              direction: DESC
            order:
              - emoji
              - title
              - formula.when
              - categories
            sort:
              - property: date
                direction: ASC
            image: note.cover
            imageFit: cover
            imageAspectRatio: 0.6
          - type: cards
            name: Starred ⭐
            filters:
              and:
                - star == true
            order:
              - emoji
              - title
              - formula.when
            sort:
              - property: date
                direction: DESC
            image: note.cover
            imageFit: cover
            imageAspectRatio: 0.6
          - type: table
            name: Table
            order:
              - date
              - emoji
              - title
              - categories
              - who
              - places
              - people
              - star
              - private
            sort:
              - property: date
                direction: DESC
    """.trimIndent() + "\n"

    val nightsBase = """
        filters:
          and:
            - 'type == "night"'
        formulas:
          minutes: '(duration_s / 60).round()'
        properties:
          formula.minutes:
            displayName: Minutes
          recorded_by:
            displayName: Recorded by
        views:
          - type: table
            name: Nights
            order:
              - file.name
              - recorded_at
              - formula.minutes
              - recorded_by
              - status
              - question
            sort:
              - property: recorded_at
                direction: DESC
    """.trimIndent() + "\n"

    val cssSnippet = """
        /* dream2gether: warm touches for the space. Toggle in Settings → Appearance → CSS snippets. */
        .callout[data-callout="comment"] {
          --callout-color: 231, 111, 81;
          --callout-icon: lucide-message-circle-heart;
        }
        .markdown-rendered mark,
        .cm-s-obsidian span.cm-highlight {
          background-color: rgba(244, 162, 97, 0.35);
          color: inherit;
        }
    """.trimIndent() + "\n"

    const val APPEARANCE_JSON = "{\n  \"enabledCssSnippets\": [\"dream2gether\"]\n}\n"
}
