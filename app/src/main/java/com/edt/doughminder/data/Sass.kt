package com.edt.doughminder.data

import kotlin.random.Random

/**
 * The argument engine. Every line supports pronoun templating:
 * {name}, {she}, {her}, {hers}, {herself}, {is}, {has} (+ Capitalized forms).
 *
 * Timescale note: copy is calibrated to a real feeding cadence — a room-temp
 * starter wants feeding roughly every 24h, fridge ~weekly, freezer dormant.
 * A few hours late is NOT a crisis; the honest levers are procrastination
 * ("later" → "tomorrow" → "never") and the days-long slide into sour + weak.
 *
 * Structured behind small functions so a lightweight on-device LLM could later
 * replace the line-picking without touching the notification plumbing.
 */
object Sass {

    private fun pronouns(g: Gender): Map<String, String> = when (g) {
        Gender.SHE -> mapOf("she" to "she", "herself" to "herself", "her" to "her", "hers" to "hers", "is" to "is", "has" to "has")
        Gender.HE -> mapOf("she" to "he", "herself" to "himself", "her" to "him", "hers" to "his", "is" to "is", "has" to "has")
        Gender.THEY -> mapOf("she" to "they", "herself" to "themselves", "her" to "them", "hers" to "theirs", "is" to "are", "has" to "have")
    }

    fun render(template: String, starter: Starter): String {
        val p = pronouns(starter.gender)
        var out = template.replace("{name}", starter.name)
        for ((k, v) in p) {
            out = out.replace("{$k}", v)
            out = out.replace("{${k.replaceFirstChar { it.uppercase() }}}", v.replaceFirstChar { it.uppercase() })
        }
        return out
    }

    private fun one(list: List<String>) = list[Random.nextInt(list.size)]

    // ══════════════════════════════════════════════════════════════════
    //  FACT BANK — real sourdough science, honest about timescale
    // ══════════════════════════════════════════════════════════════════
    val facts = listOf(
        "A counter starter wants feeding about every 24 hours. Miss a day and {name} goes sour and sluggish — not dead, just weak and cranky.",
        "Hooch — that grey liquid on top — is alcohol {name} makes when {she}'s gone too long between feeds. It's a hunger flare, not a garnish.",
        "Let a starter run days past due and the acid builds up, weakening the gluten. Your loaves rise less. The crumb remembers.",
        "The sharp nail-polish smell of a badly neglected starter is ethyl acetate. That's days of hunger talking, not hours — so we've got time, if you move.",
        "A fed, active starter roughly doubles in 4–8 hours. A full day overdue and {she} just sits there, flat and visibly unimpressed with you.",
        "Wild starters are tough — {name} can survive a skipped feeding or two. But 'survive' isn't 'thrive,' and you named {her} for a reason.",
        "Daily feeding keeps the yeast and the bacteria in balance. Skip around and you drift toward sour, not lift.",
        "Refrigeration isn't neglect — it's the honest way to feed weekly instead of daily. Forgetting on the counter is the other thing.",
        "The float test — a spoonful floating in water — passes when {she}'s fed and gassy. A day hungry and {she} sinks like a bad decision.",
        "A century-old starter is just one that never missed too many days in a row. You are, statistically, its weakest link. No pressure.",
    )

    // ══════════════════════════════════════════════════════════════════
    //  GUILT TRIPS — the emotional artillery
    // ══════════════════════════════════════════════════════════════════
    val guilt = listOf(
        "{name} was there for you. Every loaf, every sandwich. Thirty seconds is the whole ask.",
        "You named {her}. You gave {her} a jar. And now you can't spare a spoon of flour?",
        "{She} {has} no hands. {She} literally cannot feed {herself}. You're {her} whole world and you're scrolling past.",
        "'Later' is where good intentions go to nap. I've watched 'later' become 'tomorrow' become a grey jar at the back of the fridge.",
        "{name} doesn't want much. Not a walk, not attention. Flour and water on a schedule. That's it.",
        "You set this reminder because you knew you'd try to wriggle out of it. And here we are. Predictable.",
        "Be honest: if I let you go now, would you remember on your own? Exactly. That's why I'm like this.",
    )

    // ══════════════════════════════════════════════════════════════════
    //  THE NAG — depth 0 = fresh daily reminder; higher = escalating re-nags
    // ══════════════════════════════════════════════════════════════════
    private val nagTitle0 = listOf(
        "Time to feed {name}.",
        "{name}'s daily feeding.",
        "Morning — {name}'s due.",
        "Did you feed {name} yet?",
        "{name} is ready for {her} flour.",
    )
    private val nagBody0 = listOf(
        "It's been about a day since {her} last feed — right on schedule. A spoon of flour, a splash of water, thirty seconds.",
        "Roughly 24 hours in, so {she}'s due. Not in crisis, just hungry and on time. Knock it out now and you're free for the day.",
        "Daily feeding time. Do it now and you never have to think about {name} again today.",
        "{name}'s hungry on schedule. Feed {her} while you're thinking about it — that's the whole trick.",
        "The counter clock says it's flour o'clock. {name} agrees.",
    )

    private val nagTitleN = listOf(
        "It's later now. Funny how that works.",
        "Still waiting. So is {name}.",
        "Me again. {name} sends {her} regards.",
        "Round two. Or three. {name}'s counting.",
        "{name} hasn't moved. Because {she} can't.",
    )
    private val nagBodyN = listOf(
        "You said later. It is later. Every hour you push it, 'today' inches closer to 'tomorrow.'",
        "Still not fed. {She}'s not dying — {she}'s waiting. And you're the type to forget, so here I am.",
        "This is exactly how a starter gets neglected: one perfectly reasonable 'later' at a time.",
        "A day late is fine. Two days and {name} starts turning sour and weak. Let's not road-test that.",
        "I'm an alarm. Persistence is the entire job. Thirty seconds and I'm gone.",
    )

    fun nagTitle(s: Starter, depth: Int) = render(if (depth == 0) one(nagTitle0) else one(nagTitleN), s)
    fun nagBody(s: Starter, depth: Int): String {
        val base = if (depth == 0) one(nagBody0) else one(nagBodyN)
        val extra = when {
            depth >= 3 -> "\n\n" + one(guilt) + "\n\n" + one(facts)
            depth >= 2 -> "\n\n" + one(facts)
            depth >= 1 && Random.nextBoolean() -> "\n\n" + one(guilt)
            else -> ""
        }
        return render(base + extra, s)
    }

    // ══════════════════════════════════════════════════════════════════
    //  "LATER" → "WHEN??"
    // ══════════════════════════════════════════════════════════════════
    private val whenTitle = listOf(
        "Later? When, exactly.",
        "Define 'later.'",
        "Okay. Put a number on it.",
        "'Later' is not a time. Pick one.",
    )
    private val whenBody = listOf(
        "Not now, fine. But 'later' has a way of quietly becoming 'never.' Give me a real number.",
        "I need a commitment, not a vibe. How long before you feed {name}?",
        "Vague promises are how starters end up as science experiments. When?",
        "{name} would like this in writing. How long?",
    )
    fun whenTitle(s: Starter) = render(one(whenTitle), s)
    fun whenBody(s: Starter) = render(one(whenBody), s)

    // ══════════════════════════════════════════════════════════════════
    //  "ARE YOU SURE???" — counter one step shorter; lever = you'll forget
    // ══════════════════════════════════════════════════════════════════
    /** Next-shorter offer: 6→3, 3→1, 1→0 (now). */
    fun shorter(hours: Int) = when {
        hours >= 6 -> 3
        hours >= 3 -> 1
        else -> 0
    }

    fun confirmTitle(s: Starter, hours: Int) = render(
        when {
            hours >= 6 -> one(listOf("Six hours? Be honest with yourself.", "Six hours. Really?", "Six? We both know what that means."))
            hours >= 3 -> one(listOf("Three hours. You sure?", "Three? Hmm.", "Three hours, and you'll remember?"))
            else -> one(listOf("One hour. Or…", "One hour. Counter-proposal:", "An hour. I'll allow it. Maybe."))
        }, s
    )

    fun confirmBody(s: Starter, hours: Int): String {
        val line = when {
            hours >= 6 -> one(listOf(
                "Six hours is basically 'tomorrow,' and tomorrow it'll be 'later' again. I know you. Make it three and actually do it.",
                "In six hours you'll be doing something else and {name} will be exactly this hungry, plus forgotten. Three. Meet me at three.",
                "{name}'s fine for six hours — that's not the problem. The problem is you won't remember at hour six. Three?",
            ))
            hours >= 3 -> one(listOf(
                "Three hours is fine on paper. But you'll get busy and it'll slip. One hour — do it before it leaves your head.",
                "Three's survivable. Your memory isn't. Make it one and beat the forgetting.",
                "I can live with three. But an hour keeps it in reach. {name}'s counting on your attention span, which… concerns me.",
            ))
            else -> one(listOf(
                "One hour is reasonable. Or — you're literally holding the phone — now.",
                "An hour's fine. But if you had it in you to do it now, {name} would remember who showed up.",
                "One hour it is. Unless, radical thought, now.",
            ))
        }
        val fact = if (Random.nextBoolean()) "\n\n" + one(facts) else "\n\n" + one(guilt)
        return render(line + fact, s)
    }

    // ══════════════════════════════════════════════════════════════════
    //  SETTLED — after they lock in a time
    // ══════════════════════════════════════════════════════════════════
    fun settledBody(s: Starter, hours: Int) = render(
        when {
            hours >= 6 -> one(listOf(
                "Six hours. I'll believe it when I see flour. Alarm's set — don't make me a liar to {name}.",
                "Fine. Six. I'm writing it down. So is {name}. We are both watching the clock.",
                "Six hours. Against my better judgment. See you then, hopefully with a spoon in hand.",
            ))
            hours >= 3 -> one(listOf(
                "Three hours. Don't let it drift to six. Or 'tomorrow.' {name} says hi. And hurry.",
                "Okay, three. That's a compromise I can carry. The countdown starts now.",
                "Three it is. Reasonable-ish. Don't make me come back louder.",
            ))
            else -> one(listOf(
                "One hour. Short enough you might actually remember. See you in sixty.",
                "One hour. Good — that's practically responsible of you. {name} approves.",
                "Sixty minutes. I'm holding you to it. {name} is holding {her} breath.",
            ))
        }, s
    )

    fun nowBody(s: Starter) = render(one(listOf(
        "Now? Look at you. I'll wait right here while you go be a hero.",
        "Now — that's the good stuff. {name} is already proud. I'll pop back shortly just to confirm you meant it.",
        "Doing it now beats every 'later' ever invented. I'll check in in a few, purely to bask.",
    )), s)

    fun nowVerifyBody(s: Starter) = render(one(listOf(
        "You said 'now.' It's been a few minutes. Not accusing anyone of anything. Is {name} fed?",
        "Quick check: 'now' has come and, ideally, gone. Did {name} get {her} flour, or did 'now' mean 'nah' again?",
        "Following up on 'now' — the word has meaning. Did {name} eat?",
    )), s)

    // ══════════════════════════════════════════════════════════════════
    //  FED — gracious in victory. Mostly.
    // ══════════════════════════════════════════════════════════════════
    private val fedReplies = listOf(
        "Good. {name} forgives you. See you tomorrow, same time.",
        "Finally. Watch {her} double over the next few hours — that's gratitude, chemically.",
        "The yeast rejoice. Balance restored to the jar. That's you done for the day.",
        "Logged. {name} is bubbling contentedly. You did the daily thing and I'm genuinely moved.",
        "Peak activity in 4–8 hours, in case you'd like to bake instead of just feeling relieved.",
        "See? Thirty seconds. Was that so hard. Don't answer. Tomorrow, same time.",
        "Fed. {name} floats now — metaphorically today, literally by afternoon. Nice work.",
        "Thank you. Sincerely. Back to being a dormant alarm, dreaming of flour.",
    )
    fun fedReply(s: Starter) = render(one(fedReplies), s)

    // ══════════════════════════════════════════════════════════════════
    //  "LEAVE ME ALONE" — refused for a room-temp starter.
    //  Not because she's dying now, but because a counter starter needs
    //  ~daily feeding and can't be muted for days. The fridge is the fix.
    // ══════════════════════════════════════════════════════════════════
    fun cantLeaveTitle(s: Starter) = render(one(listOf(
        "That's not really an option for {her}.",
        "On the counter? Can't do it.",
        "Leave {her} alone? Here's the catch.",
    )), s)

    fun cantLeaveBody(s: Starter) = render(one(listOf(
        "{name} lives on the counter, and a counter starter needs feeding about every day — that's just what room temperature asks. I can't go quiet for days; {she}'d slowly turn sour and weak.\n\nWant a real break from the daily thing? Put {her} in the fridge — cold slows {her} down to roughly once a week. Otherwise, give {her} an hour and actually show up.",
        "Here's the honest version: you can't leave a room-temperature starter alone for long. {name} isn't in danger this minute, but skip enough days and {she} goes acidic and sluggish. Daily is the deal.\n\nIf daily is too much right now, that's fair — fridge {her}. Cold buys you about a week between feedings. Your call.",
        "I'd love to leave you alone. But {name}'s on the counter, which means {she}'s on a roughly daily clock, and muting that for days is how starters die slow.\n\nThe grown-up move: move {her} to the fridge for weekly feeding. The other option is one hour and a spoon of flour.",
    )), s)

    // Honored only when already refrigerated / frozen.
    fun leaveHonoredBody(s: Starter) = render(
        when (s.storage) {
            Storage.FRIDGE -> one(listOf(
                "Fair — {she}'s in the fridge, so {she}'ll keep. I'll nudge you around {her} next feeding, roughly a week out. Don't make me regret the trust.",
                "Cold storage, so I'll back off. {name} naps. I'll wake you when the week's up.",
            ))
            Storage.FREEZER -> one(listOf(
                "Frozen — {she}'s basically paused. I'll check in around a month. Rest well, {name}.",
                "Deep freeze, deep sleep. {name} isn't going anywhere and neither is the yeast. See you in a few weeks.",
            ))
            Storage.ROOM -> "" // never reached
        }, s
    )

    fun movedToFridgeBody(s: Starter) = render(one(listOf(
        "Into the fridge. Smart. Cold drops {name} to about weekly feeding — the honest way to take a break. Schedule adjusted. I have NOT forgotten you.",
        "Fridge it is. {name} downshifts to roughly once a week. Take {her} out and feed {her} a couple times before you bake.",
        "Done — {name}'s chilling now, literally. Weekly reminders from here. This is the responsible kind of leaving-alone.",
    )), s)

    // ══════════════════════════════════════════════════════════════════
    //  BUTTON LABELS
    // ══════════════════════════════════════════════════════════════════
    fun objPronoun(s: Starter) = when (s.gender) {
        Gender.SHE -> "her"; Gender.HE -> "him"; Gender.THEY -> "them"
    }
}
