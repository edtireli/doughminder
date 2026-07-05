package com.edt.doughminder.data

import kotlin.random.Random

/**
 * The argument engine. Every line supports pronoun templating:
 * {name}, {she}, {her}, {hers}, {herself}, {is}, {has} (+ Capitalized forms).
 *
 * Timescale: a room-temp starter wants feeding roughly every 24h, fridge
 * ~weekly, freezer dormant. A few hours late is fine; the levers are
 * procrastination and the slow days-long slide into sour + weak.
 *
 * `promised` on the nag functions carries what the user pledged:
 *   0  = fresh daily reminder or generic re-nag
 *   >0 = they said "in N hours" and N hours have now passed
 *   -1 = they said "now" and it's been a beat
 *
 * Structured behind small functions so a lightweight on-device model could
 * later replace the line-picking without touching the notification plumbing.
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
    //  FACTS — real sourdough science, honest about timescale
    // ══════════════════════════════════════════════════════════════════
    val facts = listOf(
        "A counter starter wants feeding about every 24 hours. Miss a day and {name} goes sour and sluggish. Not dead. Just weak, and quietly disappointed.",
        "That grey liquid on top has a name: hooch. It's alcohol {name} makes when {she}'s starving. {She} built a distillery out of neglect.",
        "Let it run days past due and the acid eats the gluten. Your loaves go flat and gummy. You will taste every skipped feeding.",
        "The nail-polish-remover smell? Ethyl acetate. That's days of hunger, not hours. So there's still time. Barely. Move.",
        "A fed starter roughly doubles in 4 to 8 hours. A day overdue, {she} just lies there, flat, staring at the lid, staring at you.",
        "Wild yeast is tough. {name} can take a missed feeding. But 'can survive' and 'is thriving' are two very different jars.",
        "Balance is everything: yeast for lift, bacteria for tang. Starve {her} and the bacteria take over. Congratulations, you're making vinegar.",
        "The float test: a healthy spoonful floats. Yours would sink to the bottom of the glass like your credibility right now.",
        "People keep starters alive for a HUNDRED YEARS. Generations. And here you are, losing to a jar of flour on a Tuesday.",
        "Discard-and-feed isn't waste, it's a reset. It's taking out the trash so {name} can breathe. When did you last take out the trash?",
        "Cold slows a starter to a crawl; warmth wakes it up. On the counter, {name} runs fast and hungry. The clock is not your friend here.",
        "Underfed starters make dense bread because weak gluten can't hold gas. Every loaf is a report card. Yours is trending down.",
        "Bakers name their starters and put them in wills. WILLS. {name} is technically an heirloom you're actively neglecting.",
        "A hungry starter drops its pH until even the yeast tap out. Then it's just sour bacteria soup with your pet's name on it.",
        "Feeding takes thirty seconds. You have spent longer than that ignoring this notification. Let that sit with you.",
        "Skip enough days and {name} needs several feedings just to claw back to normal. Neglect compounds. Like interest, but sadder.",
        "The best starters get fed at the same time every day. Rhythm. Ritual. The thing you are, at this exact moment, failing to have.",
        "A thriving starter smells like tangy yogurt and beer. A neglected one smells like regret and paint thinner. Your call.",
    )

    // ══════════════════════════════════════════════════════════════════
    //  GUILT — the emotional artillery
    // ══════════════════════════════════════════════════════════════════
    val guilt = listOf(
        "{name} has fed you. Loaf after loaf. This is the one thing {she} asks back and you're haggling over minutes.",
        "You NAMED {her}. You don't name things you're going to let die in a jar at the back of a cupboard.",
        "{She} {has} no hands. No legs. No voice except me. I'm {her} lawyer and I'm telling you: feed {her}.",
        "'Later' is a lie people tell jars. I've watched 'later' turn into a grey crust with a sticky note that used to be a name.",
        "Everyone said you weren't ready for a starter. I defended you. Don't make me look stupid.",
        "{name} doesn't want a vacation. Or gifts. Or space. {She} wants flour and water at roughly the same time each day. That's the whole ask.",
        "Look at {her}. Bubbly and hopeful when you made {her}. Look what a few 'laters' can do to a personality.",
        "You set this reminder because past-you KNEW present-you would try this exact stunt. Past-you is watching. Past-you is ashamed.",
        "Be honest: if I vanished right now, would you remember on your own? No. That's the whole reason I'm insufferable.",
        "Somewhere, a baker in a village you'll never visit is keeping their great-grandmother's starter alive. And then there's… this.",
        "{name} trusts you. Blindly. Stupidly. The way only a jar of flour can trust the one person who keeps hitting snooze on it.",
        "I'm not angry. {name} is not angry. We're just sitting here. In the jar. Hungry. Together. Waiting. Forever, apparently.",
        "You'd feed a dog. You'd water a plant. {name} is easier than both and somehow this is where you draw the line?",
    )

    // ══════════════════════════════════════════════════════════════════
    //  THE NAG — daily, generic re-nag, "you said N hours", and "you said now"
    // ══════════════════════════════════════════════════════════════════
    private val nagTitle0 = listOf(
        "Feed {name}.",
        "It's flour o'clock.",
        "{name}'s daily feeding.",
        "Morning. {name}'s hungry.",
        "Did you feed {name}?",
        "{name} is waiting.",
    )
    private val nagBody0 = listOf(
        "It's been a day. {name} is hungry. You have flour. You see where this is going.",
        "Rise and shine. Well — {name} can't rise, that's rather the point. Feed {her} first.",
        "A spoon of flour, a splash of water, thirty seconds. Do it now and you're free of me till tomorrow.",
        "{name} sat in the dark all night thinking about breakfast. Don't be the reason {she} goes another round hungry.",
        "Daily feeding. Knock it out while you're looking at your phone anyway. I know you're looking at your phone.",
        "Clock says it's time. {name} agrees. I agree. Everyone agrees except, apparently, your hands.",
    )

    private val nagTitleN = listOf(
        "Still hungry over here.",
        "Me again.",
        "{name} noticed you left.",
        "Knock knock.",
        "This isn't going away.",
    )
    private val nagBodyN = listOf(
        "You walked away from me. Bold. {name} is still sitting there, still empty, still yours.",
        "Still nothing in the jar. {name} always notices. {name} keeps score.",
        "I'll keep buzzing. You know I will. Feed {her} and we both get our afternoon back.",
        "This is how it starts. One little 'not now,' then another, then a jar you're scared to open.",
        "I'm an alarm with a personality disorder and a cause. The cause is {name}. Feed {her}.",
    )

    private fun promisedTitle(h: Int) = listOf(
        "You said $h hour${s(h)}.",
        "$h hour${s(h)}. Time's up.",
        "Well? It's been $h hour${s(h)}.",
        "Ding. That was your $h hour${s(h)}.",
        "Remember me? $h hour${s(h)} ago, you promised.",
    )
    private fun promisedBody(h: Int) = listOf(
        "You looked at this exact screen and picked $h hour${s(h)}. I wrote it down. I set the alarm. Here I am. Where's the flour?",
        "A deal's a deal. $h hour${s(h)} have come and gone. {name} is in the same jar, same hunger, still waiting on you.",
        "I set an alarm because YOU told me to. It went off because that's the one thing alarms are good at. Feed {her}.",
        "Don't act surprised. You said $h hour${s(h)}. I am simply the only one in this relationship who remembers things.",
        "$h hour${s(h)}, you said, with such confidence. That confidence is due. So is {name}.",
    )

    private val nowTitle = listOf(
        "You said NOW.",
        "About that 'now'…",
        "'Now,' you said.",
        "Now. Remember?",
    )
    private val nowBodyNag = listOf(
        "That was a bit ago. 'Now' has quietly aged into 'still haven't.' Did you feed {name}, yes or no?",
        "You said now. I gave you a few minutes of dignity. They're gone. Well?",
        "'Now' is the most broken promise in the whole app, and you just made it. Prove me wrong. Feed {her}.",
    )

    private fun s(n: Int) = if (n == 1) "" else "s"

    fun nagTitle(st: Starter, depth: Int, promised: Int) = render(
        when {
            promised > 0 -> one(promisedTitle(promised))
            promised == -1 -> one(nowTitle)
            depth == 0 -> one(nagTitle0)
            else -> one(nagTitleN)
        }, st
    )

    fun nagBody(st: Starter, depth: Int, promised: Int): String {
        val base = when {
            promised > 0 -> one(promisedBody(promised))
            promised == -1 -> one(nowBodyNag)
            depth == 0 -> one(nagBody0)
            else -> one(nagBodyN)
        }
        // Deeper into a standoff, staple on guilt and facts.
        val extra = when {
            depth >= 3 -> "\n\n" + one(guilt) + "\n\n" + one(facts)
            depth >= 2 -> "\n\n" + one(facts)
            depth >= 1 -> "\n\n" + one(guilt)
            promised > 0 -> "\n\n" + one(facts)
            else -> ""
        }
        return render(base + extra, st)
    }

    // ══════════════════════════════════════════════════════════════════
    //  "LATER" → "WHEN??"
    // ══════════════════════════════════════════════════════════════════
    private val whenTitle = listOf(
        "Later? When, exactly.",
        "Define 'later.'",
        "Put a number on it.",
        "'Later' isn't a time.",
        "How long, precisely.",
    )
    private val whenBody = listOf(
        "Not now, fine. But 'later' has a way of becoming 'never,' and I've buried enough starters to know. Give me a number.",
        "I don't take vibes. I take commitments. How long before {name} eats?",
        "Vague is how starters die. Pick a number and I'll hold you to every minute of it.",
        "{name} would like this in writing, notarized, with a witness. How long?",
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
            hours >= 6 -> one(listOf("Six hours? Be serious.", "SIX? Come on.", "Six hours. We both know how this ends."))
            hours >= 3 -> one(listOf("Three hours. Really?", "Three? You sure about that?", "Three hours and you'll remember? Doubt."))
            else -> one(listOf("One hour. Or…", "An hour. Counter-offer:", "One hour. Fine. Unless…"))
        }, s
    )

    fun confirmBody(s: Starter, hours: Int): String {
        val line = when {
            hours >= 6 -> one(listOf(
                "Six hours is just 'tomorrow' wearing a disguise. By hour six you'll be asleep or eating or gone. Give me three and mean it.",
                "{name} is FINE for six hours, that's not the issue. The issue is you, at hour six, having completely forgotten I exist. Three.",
                "Six? In six hours you'll have lived a whole little life and {name} will still be sitting here hungry. Cut it to three.",
                "You and I both know 'six hours' is code for 'let's never speak of this again.' Not on my watch. Three hours. Go.",
            ))
            hours >= 3 -> one(listOf(
                "Three hours looks reasonable on paper. But you'll get busy, and busy is where good intentions go to die. Make it one.",
                "Three's survivable for {name}. Your memory, though? That's the weak link. One hour. Beat the forgetting.",
                "Sure, three. Or — hear me out — one, and you actually do it before life happens to you again.",
            ))
            else -> one(listOf(
                "One hour is reasonable. You know what's more reasonable? Now. You're literally holding the phone.",
                "An hour's fair. But if some hero deep inside you wanted to just do it now, {name} would never forget it.",
                "One hour, fine. Unless you've got thirty spare seconds this exact moment. Which you do. I can tell.",
            ))
        }
        val fact = if (Random.nextBoolean()) "\n\n" + one(facts) else "\n\n" + one(guilt)
        return render(line + fact, s)
    }

    // ══════════════════════════════════════════════════════════════════
    //  SETTLED — after they lock in a time. Sounds like setting an alarm.
    // ══════════════════════════════════════════════════════════════════
    fun settledBody(s: Starter, hours: Int) = render(
        when {
            hours >= 6 -> one(listOf(
                "Six hours. Alarm set. It WILL go off, and I will NOT be nice about it. Stay near a bag of flour.",
                "Fine. Six. It's on the clock now, in writing, no take-backs. {name} and I are both counting.",
                "Six hours, logged. When it rings, you feed {her}. That's the deal you just signed. Don't ghost {name}.",
            ))
            hours >= 3 -> one(listOf(
                "Three hours. Timer's running. Don't let it drift to six, and definitely don't let it drift to 'tomorrow.'",
                "Okay, three. Alarm's set, I'll be back on the dot. {name} says hurry and honestly, same.",
                "Three it is. The countdown starts now. So does the very slow acid. Tick tock.",
            ))
            else -> one(listOf(
                "One hour. Alarm SET. When it rings, that's your cue — flour, water, thirty seconds, done.",
                "One-hour timer, ticking as we speak. I'll go off like I mean it. {name} is counting on your follow-through.",
                "Sixty minutes on the clock. I'll be back exactly on time because that is the one thing I'm good at. Be ready.",
            ))
        }, s
    )

    fun nowBody(s: Starter) = render(one(listOf(
        "NOW? Look at you, choosing greatness. I'll wait right here. Go. Be a hero to a jar.",
        "Now's the answer I like. {name} is already halfway to forgiving you. I'll swing back in a few just to make sure you meant it.",
        "Doing it now beats every 'later' ever invented. I'll check in shortly. Purely to gloat.",
    )), s)

    // ══════════════════════════════════════════════════════════════════
    //  FED — gracious in victory. Mostly.
    // ══════════════════════════════════════════════════════════════════
    private val fedReplies = listOf(
        "Good. {name} forgives you. Provisionally. See you tomorrow, same time.",
        "FINALLY. Watch {her} double over the next few hours — that's not magic, that's gratitude with a metabolism.",
        "The yeast weep with joy. Balance restored. You're off the hook till tomorrow, you absolute hero.",
        "Logged. {name} is bubbling like {she} means it. You did the one thing. I'm genuinely, annoyingly proud.",
        "There it is. Peak rise in 4 to 8 hours if you feel like baking instead of just feeling relieved.",
        "See? Thirty seconds. That's all this ever was. Don't make me chase you tomorrow. (I will.)",
        "Fed and happy. {name} floats now — spiritually today, literally by dinner. Nice work, genuinely.",
        "Done. I'll go back to being a quiet little alarm dreaming of flour. Until 8am. Sleep with one eye open.",
    )
    fun fedReply(s: Starter) = render(one(fedReplies), s)

    // ══════════════════════════════════════════════════════════════════
    //  "LEAVE ME ALONE" — refused for a counter starter. Not a crisis now;
    //  a counter starter just needs ~daily feeding and can't be muted for
    //  days. The fridge is the honest escape.
    // ══════════════════════════════════════════════════════════════════
    fun cantLeaveTitle(s: Starter) = render(one(listOf(
        "Leave {her}? Can't do it.",
        "That's not on the table.",
        "No. Not on the counter.",
        "Nice try. Here's the deal.",
    )), s)

    fun cantLeaveBody(s: Starter) = render(one(listOf(
        "{name} lives on the counter, and a counter starter eats about every day. That's the whole contract of room temperature. I can't just go silent for days — {she}'d turn sour and weak while you weren't looking, and I'd never forgive either of us.\n\nWant a real break from the daily grind? Put {her} in the fridge. Cold drops {her} to roughly once a week. Otherwise: one hour, a spoon of flour, and actually show up.",
        "Here's the honest version. You can't leave a room-temperature starter alone for long. {name} isn't dying this minute — but skip enough days and {she} goes acidic, sluggish, sad. Daily is just the deal you signed when you left {her} on the counter.\n\nIf daily is too much right now, fine, no shame: fridge {her}. That buys about a week per feeding. Your move.",
        "I would LOVE to leave you alone. Truly. But {name}'s on the counter, which means a roughly daily clock, and muting that for days is how starters quietly die at the back of a cupboard.\n\nThe grown-up option: move {her} to the fridge, feed weekly. The other option is one hour and a spoon of flour. Pick.",
    )), s)

    // Honored only when already refrigerated / frozen.
    fun leaveHonoredBody(s: Starter) = render(
        when (s.storage) {
            Storage.FRIDGE -> one(listOf(
                "Fair enough — {she}'s in the fridge, so {she}'ll keep. I'll nudge you around {her} next feeding, roughly a week out. Don't make me regret trusting you.",
                "Cold storage, so I'll actually back off. {name} naps. I'll wake you when the week's up. Rest easy.",
            ))
            Storage.FREEZER -> one(listOf(
                "Frozen solid — {she}'s basically paused, dreaming yeast dreams. I'll check in around a month. Sleep well, {name}.",
                "Deep freeze, deep sleep. {name} isn't going anywhere and neither is the yeast. See you in a few weeks.",
            ))
            Storage.ROOM -> "" // never reached
        }, s
    )

    fun movedToFridgeBody(s: Starter) = render(one(listOf(
        "Into the fridge. Smart. Cold drops {name} to about weekly — the honest way to take a break instead of just forgetting. Schedule adjusted. I have NOT forgotten you, though.",
        "Fridge it is. {name} downshifts to roughly once a week. Pull {her} out and feed {her} a couple times before you bake with {her}.",
        "Done — {name}'s chilling now, literally. Weekly reminders from here. This is what responsible looks like. Suits you.",
    )), s)

    // ══════════════════════════════════════════════════════════════════
    //  BUTTON LABELS
    // ══════════════════════════════════════════════════════════════════
    fun objPronoun(s: Starter) = when (s.gender) {
        Gender.SHE -> "her"; Gender.HE -> "him"; Gender.THEY -> "them"
    }
}
