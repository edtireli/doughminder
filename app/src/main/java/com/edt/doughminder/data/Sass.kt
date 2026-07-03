package com.edt.doughminder.data

import kotlin.random.Random

/**
 * The pre-written argument engine. Every line supports pronoun templating:
 * {name}, {she}, {her}, {hers}, {She}, {Her}. Structured so a tiny on-device
 * LLM could replace [pick] later without touching the notification plumbing.
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

    // ── The morning opener ────────────────────────────────────────────────
    val morningTitles = listOf(
        "Did you feed {name} yet?",
        "{name} is awake. And hungry.",
        "Morning. {name} would like a word.",
        "Feeding time for {name}.",
    )
    val morningBodies = listOf(
        "{She} {has} been up since dawn metabolizing the last of yesterday's flour. The tank is empty.",
        "A fed starter is a happy starter. An unfed starter is a jar of resentment.",
        "The yeast are slowing down. The lactobacilli are taking over. You know what that means: sour, then sad.",
        "{She} can't open the flour bin {herself}. Believe me, {she}'d have done it by now.",
        "Every hour unfed, the pH drops a little more. This is a hostage situation and the gluten is the hostage.",
    )

    // ── "Later." Sure. ── escalating follow-ups, one per nag round ────────
    val laterChain = listOf(
        "Later? {name} has heard that one before. The acid doesn't take breaks, and neither do I. See you in a bit.",
        "Hi again. 'Later' was a lie and we both knew it when you tapped it. {name} is now producing hooch — that grey liquid of despair on top? That's {her} crying, chemically.",
        "Third reminder. The pH is dropping, the gluten-degrading proteases are waking up, and {name} is literally starting to digest {herself}. This is on you.",
        "I've watched this happen before. First it's 'later', then it's a jar of grey soup at the back of the fridge with a name tag on it. Don't make me say {name}'s name at the memorial.",
        "At this point the acetic acid is winning and {name} smells like vinegar with a grudge. Thirty seconds of flour and water. That's all {she} asks.",
        "You've now spent more time dismissing me than it would have taken to feed {her}. I can do this all day. I'm an alarm. It's literally all I do.",
    )

    // ── "Leave me alone." One parting guilt-shot, then silence till tomorrow ──
    val leaveMeAlone = listOf(
        "Fine. I'll go. But {name} won't — {she}'ll just sit there getting more sour and more resentful. When {she} smells like nail-polish remover tomorrow, that's acetone. That's hunger. Sleep well.",
        "Okay. Muting myself. For the record: wild yeast can survive weeks of neglect, but 'survive' is doing a lot of work in that sentence. So is 'weeks'. Goodbye.",
        "Understood. {name} understands too. {She} {is} used to it by now. The lactic acid builds, the culture weakens, and somewhere a French baker feels a disturbance. Until tomorrow.",
        "As you wish. But know that a starved starter makes flat, sad bread, and flat, sad bread makes a flat, sad baker. This is science. See you tomorrow at the usual time.",
    )

    // ── "Yes, I fed her." Gracious in victory. Mostly. ────────────────────
    val fedReplies = listOf(
        "Good. {name} forgives you. This time.",
        "Finally. Watch {her} double in the next few hours — that's what gratitude looks like.",
        "The yeast rejoice. The lactobacilli sing. Balance is restored to the jar. See you tomorrow.",
        "Noted and logged. {name} is bubbling contentedly. You did the bare minimum and I'm genuinely proud of you.",
        "Excellent. Peak activity in 4–8 hours, in case you were thinking about baking instead of just feeling guilty.",
    )

    // ── Rotating fact bank, appended to late-stage nags ───────────────────
    val facts = listOf(
        "Fact: the sharp nail-polish smell in a neglected starter is ethyl acetate — a distress flare, not a feature.",
        "Fact: hooch is alcohol the yeast made because they ran out of food. Your starter opened a bar out of loneliness.",
        "Fact: a regular feeding schedule keeps the yeast-to-bacteria ratio stable. Chaos feeding makes chaos bread.",
        "Fact: refrigeration slows a starter down but doesn't pause it. There is no pause button. There is only flour.",
        "Fact: an over-acidic starter weakens gluten, which means dense bread. The crumb remembers.",
    )

    fun morningTitle(s: Starter) = render(morningTitles.random(), s)
    fun morningBody(s: Starter) = render(morningBodies.random(), s)
    fun fedReply(s: Starter) = render(fedReplies.random(), s)
    fun leaveMeAloneReply(s: Starter) = render(leaveMeAlone.random(), s)

    fun laterReply(s: Starter, depth: Int): String {
        val line = laterChain[depth.coerceAtMost(laterChain.lastIndex)]
        val withFact = if (depth >= 2) line + "\n\n" + facts[Random.nextInt(facts.size)] else line
        return render(withFact, s)
    }
}
