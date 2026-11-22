package com.example.data.catalog

import com.example.R
import com.example.data.model.LostInternetRelic
import com.example.data.model.MemeArtifact
import com.example.data.model.SoundArtifact
import com.example.data.model.SoundSynthPreset
import com.example.data.model.TimelineMilestone
import com.example.data.model.ViralLegend
import com.example.ui.theme.AcidLime
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.GlitchCrimson

object CuratedArchiveCatalog {

    val viralLegends: List<ViralLegend> = listOf(
        ViralLegend(
            id = "legend_harold",
            archiveCode = "LEG-001",
            name = "Hide the Pain Harold",
            alias = "András Arató",
            era = "2011 – PRESENT",
            viralOrigin = "Hungarian electrical engineer photographed for Dreamstime stock portraits whose polite smile masked existential dread.",
            shortStory = "Originally shot during a stock photography session in Budapest, Arató's expression resonated globally as the universal face of enduring mild catastrophe with dignity.",
            popularDialogue = "\"Everything is totally fine. (Internal screaming in 24-bit audio)\"",
            sourceUrl = "https://en.wikipedia.org/wiki/Hide_the_Pain_Harold",
            relatedMemeIds = listOf("meme_harold", "meme_this_is_fine"),
            artworkRes = R.drawable.img_viral_legends_1795377163746,
            accentColor = AcidLime
        ),
        ViralLegend(
            id = "legend_kabosu",
            archiveCode = "LEG-002",
            name = "Doge (Kabosu)",
            alias = "The Shiba Inu Sovereign",
            era = "2010 – 2024",
            viralOrigin = "A 2010 blog photo by Atsuko Sato showing Kabosu with raised eyebrows and crossed paws on a couch.",
            shortStory = "By 2013, Comic Sans internal monologues ('much wow', 'very archive') turned one rescued Shiba Inu into a decade-defining linguistic dialect.",
            popularDialogue = "\"Such museum. Very culture. Much archive. Wow.\"",
            sourceUrl = "https://en.wikipedia.org/wiki/Doge_(meme)",
            relatedMemeIds = listOf("meme_doge", "meme_cheems"),
            artworkRes = R.drawable.img_meme_universe_1795377163747,
            accentColor = ElectricPurple
        ),
        ViralLegend(
            id = "legend_keyboard_cat",
            archiveCode = "LEG-003",
            name = "Keyboard Cat",
            alias = "Fatso (1984 / 2007)",
            era = "2007 – CLASSIC ERA",
            viralOrigin = "VHS footage recorded by Charlie Schmidt in 1984 and uploaded to YouTube in June 2007.",
            shortStory = "Wearing a blue shirt and hammering out an upbeat ragtime riff, Fatso became the internet's official cue to exit embarrassing moments gracefully.",
            popularDialogue = "\"Play him off, Keyboard Cat.\"",
            sourceUrl = "https://en.wikipedia.org/wiki/Keyboard_Cat",
            relatedMemeIds = listOf("meme_nyan_cat", "meme_keyboard_cat"),
            artworkRes = R.drawable.img_hero_museum_1795377163161,
            accentColor = CyberCyan
        ),
        ViralLegend(
            id = "legend_rickroll",
            archiveCode = "LEG-004",
            name = "The Rickroll Architect",
            alias = "Bait-and-Switch Protocol",
            era = "2007 – ETERNAL",
            viralOrigin = "Evolved from 4chan's 'duckroll' prank into hyperlinking disguised URLs to Rick Astley's 1987 synth-pop video.",
            shortStory = "No hyperlink on the 2000s web could be trusted. Clicking a promised game trailer meant surrendering to an unskippable drum fill and trench coat.",
            popularDialogue = "\"Never gonna give you up, never gonna let you down.\"",
            sourceUrl = "https://en.wikipedia.org/wiki/Rickrolling",
            relatedMemeIds = listOf("meme_rickroll", "meme_trollface"),
            artworkRes = R.drawable.img_viral_legends_1795377163746,
            accentColor = GlitchCrimson
        ),
        ViralLegend(
            id = "legend_grumpy_cat",
            archiveCode = "LEG-005",
            name = "Grumpy Cat",
            alias = "Tardar Sauce",
            era = "2012 – 2019",
            viralOrigin = "Posted to Reddit r/pics in September 2012; her permanent scowl was caused by feline dwarfism and an underbite.",
            shortStory = "While the web overflowed with sugary optimism, Tardar Sauce became the patron saint of deadpan refusal—spawning books, coffee lines, and museum waxwork.",
            popularDialogue = "\"I had fun once. It was awful.\"",
            sourceUrl = "https://en.wikipedia.org/wiki/Grumpy_Cat",
            relatedMemeIds = listOf("meme_grumpy_cat", "meme_doge"),
            artworkRes = R.drawable.img_meme_universe_1795377163747,
            accentColor = AcidLime
        ),
        ViralLegend(
            id = "legend_success_kid",
            archiveCode = "LEG-006",
            name = "Success Kid",
            alias = "Sammy Griner",
            era = "2007 – PRESENT",
            viralOrigin = "A Flickr beach photo snapped by Laney Griner in August 2007 showing 11-month-old Sammy clenching a fistful of sand.",
            shortStory = "From 'I Hate Sandcastles' captions to Advice Animal stardom, Sammy's triumphant fist later helped crowdfund his father's kidney transplant in 2015.",
            popularDialogue = "\"Woke up 2 minutes before the alarm. Didn't even feel tired.\"",
            sourceUrl = "https://en.wikipedia.org/wiki/Success_Kid",
            relatedMemeIds = listOf("meme_success_kid", "meme_bad_luck_brian"),
            artworkRes = R.drawable.img_lost_internet_1795377164160,
            accentColor = ElectricPurple
        )
    )

    val memeCategories = listOf("All", "2000s", "2010s", "2020s", "Reaction", "Chaotic")

    val memes: List<MemeArtifact> = listOf(
        MemeArtifact(
            id = "meme_nyan_cat",
            archiveCode = "MEM-011",
            title = "Nyan Cat Rainbow Loop",
            category = "2010s",
            year = 2011,
            summary = "Pixel-art Pop-Tart cat streaking across deep space trailing a rainbow while an endless Vocaloid melody plays.",
            impactNote = "Defined early 2010s YouTube endurance challenges and pixel-art remix culture.",
            tags = listOf("pixel", "cat", "youtube", "loop"),
            artworkRes = R.drawable.img_meme_universe_1795377163747,
            accentColor = ElectricPurple
        ),
        MemeArtifact(
            id = "meme_doge",
            archiveCode = "MEM-012",
            title = "Such Doge / Comic Sans",
            category = "2010s",
            year = 2013,
            summary = "Two-word broken-grammar captions floating in multicolored Comic Sans around a side-eyeing Shiba Inu.",
            impactNote = "Inspired an entire cryptocurrency and became Know Your Meme's 2013 Meme of the Year.",
            tags = listOf("shiba", "comic sans", "wow", "tumblr"),
            artworkRes = R.drawable.img_meme_universe_1795377163747,
            accentColor = AcidLime
        ),
        MemeArtifact(
            id = "meme_rickroll",
            archiveCode = "MEM-013",
            title = "Rickroll Redirect",
            category = "2000s",
            year = 2007,
            summary = "Disguising a link to something urgent only to ambush the visitor with 1987 synth-pop.",
            impactNote = "Turned URL skepticism into a global collaborative inside joke—even hijacking the 2008 Macy's parade.",
            tags = listOf("link", "prank", "4chan", "music"),
            artworkRes = R.drawable.img_hero_museum_1795377163161,
            accentColor = GlitchCrimson
        ),
        MemeArtifact(
            id = "meme_this_is_fine",
            archiveCode = "MEM-014",
            title = "This Is Fine",
            category = "Reaction",
            year = 2013,
            summary = "KC Green's bowler-hatted cartoon dog sipping coffee inside a burning room.",
            impactNote = "Still the single most deployed reaction panel for deadline week and server outages.",
            tags = listOf("fire", "coffee", "comic", "burnout"),
            artworkRes = R.drawable.img_lost_internet_1795377164160,
            accentColor = GlitchCrimson
        ),
        MemeArtifact(
            id = "meme_harold",
            archiveCode = "MEM-015",
            title = "Hide the Pain Smile",
            category = "Reaction",
            year = 2014,
            summary = "Stock photo portrait capturing the razor-thin line between polite composure and despair.",
            impactNote = "Transformed anonymous corporate stock photography into high-empathy internet folklore.",
            tags = listOf("stock", "smile", "pain", "work"),
            artworkRes = R.drawable.img_viral_legends_1795377163746,
            accentColor = AcidLime
        ),
        MemeArtifact(
            id = "meme_trollface",
            archiveCode = "MEM-016",
            title = "MS Paint Trollface",
            category = "2000s",
            year = 2008,
            summary = "Wrinkled MS Paint grin drawn by Carlos Ramirez that anchored the entire Rage Comic era.",
            impactNote = "Laid the visual grammar for early Reddit and DeviantArt comic templates.",
            tags = listOf("mspaint", "rage comic", "2008", "problem"),
            artworkRes = R.drawable.img_meme_universe_1795377163747,
            accentColor = CyberCyan
        ),
        MemeArtifact(
            id = "meme_keyboard_cat",
            archiveCode = "MEM-017",
            title = "Play Him Off, Keyboard Cat",
            category = "2000s",
            year = 2009,
            summary = "Splicing classic fail clips straight into Fatso cheerfully jamming on an electronic keyboard.",
            impactNote = "Pioneered the 'reaction remix' edit format that modern short-form video still relies on.",
            tags = listOf("cat", "vhs", "remix", "fail"),
            artworkRes = R.drawable.img_hero_museum_1795377163161,
            accentColor = CyberCyan
        ),
        MemeArtifact(
            id = "meme_grumpy_cat",
            archiveCode = "MEM-018",
            title = "NO. (Grumpy Cat)",
            category = "Reaction",
            year = 2012,
            summary = "Top-and-bottom Impact font declarations of absolute pessimism.",
            impactNote = "Crowned the quintessential Advice Animal format of the early 2010s.",
            tags = listOf("cat", "no", "impact", "reddit"),
            artworkRes = R.drawable.img_meme_universe_1795377163747,
            accentColor = AcidLime
        ),
        MemeArtifact(
            id = "meme_brainrot_2024",
            archiveCode = "MEM-019",
            title = "Hyper-Irony & Glitch Core",
            category = "Chaotic",
            year = 2024,
            summary = "Deep-fried bass boosts, surreal 3D loops, and post-ironic layered audio-visual collages.",
            impactNote = "Demonstrates how Gen-Z humor compressed decades of meme syntax into half-second sensory bursts.",
            tags = listOf("chaotic", "2024", "irony", "glitch"),
            artworkRes = R.drawable.img_hero_museum_1795377163161,
            accentColor = ElectricPurple
        ),
        MemeArtifact(
            id = "meme_success_kid",
            archiveCode = "MEM-020",
            title = "Sand-Fist Triumph",
            category = "2000s",
            year = 2008,
            summary = "Toddler clenching sand with laser-focused determination over small everyday victories.",
            impactNote = "One of the earliest photos to jump from Flickr to global mainstream ad campaigns.",
            tags = listOf("win", "flickr", "toddler", "classic"),
            artworkRes = R.drawable.img_lost_internet_1795377164160,
            accentColor = ElectricPurple
        ),
        MemeArtifact(
            id = "meme_npc_loop",
            archiveCode = "MEM-021",
            title = "Synchronized Loop Era",
            category = "2020s",
            year = 2023,
            summary = "Algorithmic choreography, duets, and deadpan live-stream persona performance art.",
            impactNote = "Blurs the boundary between interactive gaming avatars and live creator theater.",
            tags = listOf("2020s", "live", "duet", "performance"),
            artworkRes = R.drawable.img_viral_legends_1795377163746,
            accentColor = CyberCyan
        ),
        MemeArtifact(
            id = "meme_bad_luck_brian",
            archiveCode = "MEM-022",
            title = "Bad Luck Brian",
            category = "Chaotic",
            year = 2012,
            summary = "Plaid vest, braces, and a yearbook smile paired with comical worst-case scenarios.",
            impactNote = "Archetype of self-deprecating internet storytelling before short-form video took over.",
            tags = listOf("yearbook", "2012", "unlucky", "classic"),
            artworkRes = R.drawable.img_lost_internet_1795377164160,
            accentColor = GlitchCrimson
        )
    )

    val soundCategories = listOf("All", "Retro Web", "Meme Sting", "8-Bit Arcade", "Glitch Lab")

    val sounds: List<SoundArtifact> = listOf(
        SoundArtifact(
            id = "snd_dialup",
            archiveCode = "SND-001",
            title = "56K Dial-Up Handshake",
            category = "Retro Web",
            eraTag = "1996",
            durationLabel = "02.4s",
            durationMs = 2400L,
            description = "Synthesized FSK carrier tones and modem negotiation burst before loading a single JPEG.",
            preset = SoundSynthPreset.DIALUP_HANDSHAKE,
            accentColor = CyberCyan
        ),
        SoundArtifact(
            id = "snd_vine_boom",
            archiveCode = "SND-002",
            title = "Sub-Bass Plot Twist",
            category = "Meme Sting",
            eraTag = "2015",
            durationLabel = "01.1s",
            durationMs = 1100L,
            description = "Punchy pitch-dropping sub-bass impact that turns any mundane eyebrow raise into high drama.",
            preset = SoundSynthPreset.VINE_BOOM,
            accentColor = GlitchCrimson
        ),
        SoundArtifact(
            id = "snd_8bit_up",
            archiveCode = "SND-003",
            title = "Flash Game Level Clear",
            category = "8-Bit Arcade",
            eraTag = "2006",
            durationLabel = "01.3s",
            durationMs = 1300L,
            description = "Crisp square-wave arpeggio celebrating surviving an impossible browser platformer.",
            preset = SoundSynthPreset.EIGHT_BIT_LEVELUP,
            accentColor = AcidLime
        ),
        SoundArtifact(
            id = "snd_crt_degauss",
            archiveCode = "SND-004",
            title = "CRT Degauss Coil Pulse",
            category = "Retro Web",
            eraTag = "1999",
            durationLabel = "01.6s",
            durationMs = 1600L,
            description = "Heavy magnetic coil hum and glass-screen shimmer when booting a desktop monitor.",
            preset = SoundSynthPreset.CRT_DEGAUSS,
            accentColor = ElectricPurple
        ),
        SoundArtifact(
            id = "snd_modem_glitch",
            archiveCode = "SND-005",
            title = "Packet Loss Glitch Burst",
            category = "Glitch Lab",
            eraTag = "2021",
            durationLabel = "01.5s",
            durationMs = 1500L,
            description = "Bit-crushed step-sequenced telemetry scatter for cyber-museum transitions.",
            preset = SoundSynthPreset.MODEM_GLITCH,
            accentColor = ElectricPurple
        ),
        SoundArtifact(
            id = "snd_airhorn",
            archiveCode = "SND-006",
            title = "MLG Triple Horn Fanfare",
            category = "Meme Sting",
            eraTag = "2014",
            durationLabel = "01.4s",
            durationMs = 1400L,
            description = "Detuned sawtooth brass triad honoring peak 2014 montage parody editing.",
            preset = SoundSynthPreset.AIRHORN_FANFARE,
            accentColor = AcidLime
        ),
        SoundArtifact(
            id = "snd_dramatic_sting",
            archiveCode = "SND-007",
            title = "Dramatic Chipmunk Turn",
            category = "Meme Sting",
            eraTag = "2007",
            durationLabel = "01.5s",
            durationMs = 1500L,
            description = "Three-note orchestral synth swell timed to a sudden slow-zoom close-up.",
            preset = SoundSynthPreset.DRAMATIC_STING,
            accentColor = GlitchCrimson
        ),
        SoundArtifact(
            id = "snd_synthwave",
            archiveCode = "SND-008",
            title = "Midnight Archive Pad",
            category = "Glitch Lab",
            eraTag = "2026",
            durationLabel = "02.2s",
            durationMs = 2200L,
            description = "Warm detuned analog chord sustaining inside the museum's dark gallery hall.",
            preset = SoundSynthPreset.SYNTHWAVE_CHORD,
            accentColor = CyberCyan
        )
    )

    val timelineMilestones: List<TimelineMilestone> = listOf(
        TimelineMilestone("tl_2004", "ARC-2004", 2004, "FEB", 2, "Web 2.0 & Personal Profiles", "Platform", "Social profiles, custom HTML themes, and top-friend rankings turn teenagers into front-end tinkerers.", "MYSPACE ERA"),
        TimelineMilestone("tl_2005", "ARC-2005", 2005, "APR", 4, "Me at the Zoo", "Video", "An 18-second clip uploaded in San Diego unlocks user-generated video broadcasting for the planet.", "YOUTUBE BIRTH"),
        TimelineMilestone("tl_2007", "ARC-2007", 2007, "MAY", 5, "The Great Rickroll Boom", "Meme", "Bait-and-switch hyperlinks hijack gaming forums and turn 1987 pop into an unbreakable prank.", "LINK PRANK"),
        TimelineMilestone("tl_2009", "ARC-2009", 2009, "MAY", 5, "Keyboard Cat Remix Wave", "Legend", "VHS cat footage becomes the universal punchline appended to viral fail videos.", "REMIX GRAMMAR"),
        TimelineMilestone("tl_2011", "ARC-2011", 2011, "APR", 4, "Nyan Cat & 10-Hour Loops", "Meme", "Endless-loop pixel animation tests attention spans and early YouTube server endurance.", "PIXEL LOOP"),
        TimelineMilestone("tl_2013", "ARC-2013", 2013, "JAN", 1, "Vine 6-Second Theater", "Platform", "Six-second looping constraints spark rapid-fire visual comedy, jump cuts, and audio catchphrases.", "6-SEC LOOP"),
        TimelineMilestone("tl_2013b", "ARC-2013B", 2013, "DEC", 12, "Doge & Comic Sans Syntax", "Legend", "Internal dog monologue becomes an instantly recognizable global dialect.", "SHIBA ERA"),
        TimelineMilestone("tl_2016", "ARC-2016", 2016, "AUG", 8, "Reaction GIF Economy", "Reaction", "Every group chat replaces paragraphs with looping two-second film and meme reactions.", "CHAT LANGUAGE"),
        TimelineMilestone("tl_2020", "ARC-2020", 2020, "MAR", 3, "Quarantine Audio Remixes", "Sound", "Shared living-room boredom synchronizes millions around common sound templates and sea shanties.", "SOUND SYNC"),
        TimelineMilestone("tl_2022", "ARC-2022", 2022, "NOV", 11, "Post-Irony & Archival Nostalgia", "Trend", "Gen-Z revives 2000s digicams, CRT scanlines, and Y2K typography as deliberate art.", "Y2K REVIVAL"),
        TimelineMilestone("tl_2025", "ARC-2025", 2025, "JUN", 6, "Micro-Trend Velocity Peak", "Trend", "Cultural jokes rise, mutate across five platforms, and achieve nostalgia status within 72 hours.", "HYPER-ARCHIVE"),
        TimelineMilestone("tl_2026", "ARC-2026", 2026, "NOV", 11, "The Living Digital Museum", "Archive", "Curators and visitors collaborate to preserve fleeting internet folklore before links rot.", "PERMANENT RECORD")
    )

    val lostInternetRelics: List<LostInternetRelic> = listOf(
        LostInternetRelic(
            id = "lost_flash",
            archiveCode = "LST-101",
            title = "Adobe Flash Player",
            platformEra = "Browser Sandbox",
            yearsActive = "1996 – 2020",
            epitaph = "Powered an entire generation of indie stick-figure animations, Newgrounds epics, and classroom mini-games.",
            nostalgiaNote = "End-of-life December 31, 2020 — preserved today by community emulators.",
            badgeLabel = "PLUGIN RETIRED"
        ),
        LostInternetRelic(
            id = "lost_vine",
            archiveCode = "LST-102",
            title = "Vine 6-Second Loops",
            platformEra = "Mobile Micro-Video",
            yearsActive = "2013 – 2017",
            epitaph = "Proved six seconds was plenty of time to invent timeless catchphrases, rhythmic edits, and comedic timing.",
            nostalgiaNote = "Every modern short-form feed still echoes Vine's pacing.",
            badgeLabel = "6.0s CONSTRAINT"
        ),
        LostInternetRelic(
            id = "lost_msn",
            archiveCode = "LST-103",
            title = "MSN Messenger Nudge",
            platformEra = "Desktop Chat Client",
            yearsActive = "1999 – 2013",
            epitaph = "Physically shook your friend's chat window with a metallic rattle when they took more than 12 seconds to reply.",
            nostalgiaNote = "Accompanied by custom blinking pixel emoticons and 'Now Playing' status flexing.",
            badgeLabel = "WINDOW SHAKE"
        ),
        LostInternetRelic(
            id = "lost_geocities",
            archiveCode = "LST-104",
            title = "GeoCities Under Construction GIFs",
            platformEra = "Web 1.0 Neighborhoods",
            yearsActive = "1994 – 2009",
            epitaph = "Hand-coded HTML shrines bursting with tiled starfields, MIDI background loops, and visitor hit counters.",
            nostalgiaNote = "Before algorithmic feeds, everyone owned a weird neon corner of the web.",
            badgeLabel = "HIT COUNTER: 004219"
        ),
        LostInternetRelic(
            id = "lost_tumblrite",
            archiveCode = "LST-105",
            title = "2012 Custom CSS Dashboards",
            platformEra = "Microblogging",
            yearsActive = "2010 – 2016",
            epitaph = "Falling snow cursors, auto-playing indie playlists, and chaotic reblog chains stretching for miles.",
            nostalgiaNote = "Invented modern fandom aesthetics and lowercase emotional punctuation.",
            badgeLabel = "REBLOG CHAIN"
        ),
        LostInternetRelic(
            id = "lost_club_penguin",
            archiveCode = "LST-106",
            title = "Tipping the Iceberg",
            platformEra = "Virtual World",
            yearsActive = "2005 – 2017",
            epitaph = "Ten years of penguins drilling together on an iceberg—until the devs finally let it flip on the final week.",
            nostalgiaNote = "One of gaming's greatest collective farewell payoffs.",
            badgeLabel = "WORLD OFFLINE"
        )
    )

    val defaultBattlesSeed: List<Triple<String, String, Triple<String, String, String>>> = listOf(
        Triple("battle_nyan_vs_doge", "Wholesome Chaos Crown", Triple("Nyan Cat", "Doge (Kabosu)", "2010s")),
        Triple("battle_vine_vs_tiktok", "Short-Form Comedy Legacy", Triple("Vine 6s Loops", "2020s Sound Loops", "VIDEO ERA")),
        Triple("battle_flash_vs_html5", "Browser Creativity Golden Age", Triple("Flash & Newgrounds", "Modern WebGL Canvas", "WEB TECH")),
        Triple("battle_2000s_vs_2010s", "Peak Meme Aesthetic", Triple("2000s Impact Font", "2010s Surreal Deep-Fry", "CULTURE"))
    )

    val totalCuratedArtifacts: Int
        get() = viralLegends.size + memes.size + sounds.size + timelineMilestones.size + lostInternetRelics.size
}
