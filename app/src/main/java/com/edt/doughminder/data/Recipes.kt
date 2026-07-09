package com.edt.doughminder.data

/**
 * Built-in recipes. Drop more in here (same shape) and the Recipes screen +
 * step timers pick them up automatically.
 */
object Recipes {
    val all = listOf(
        Recipe(
            id = "sesame-sourdough",
            title = "Sesame Sourdough (Loaf or Buns)",
            summary = "Half graham, half white, wheat-gluten boosted and sesame-crusted. " +
                "Bakes as one loaf or a tray of buns.",
            steps = listOf(
                RecipeStep(
                    "Build the starter",
                    "Mix 40g active starter + 40g water (29°C) + 40g flour. Cover and leave " +
                        "until it peaks — domed and bubbly, about 3–4h. Start the autolyse " +
                        "(next step) roughly 1h before it's ready.",
                    timerMinutes = 210,
                ),
                RecipeStep(
                    "Autolyse",
                    "Mix 500g flour (300g white + 200g graham) with 400g water (29°C) until no " +
                        "dry flour remains. Cover and rest ~1h — aim to finish as the starter peaks.",
                    timerMinutes = 60,
                ),
                RecipeStep(
                    "Mix the dough",
                    "To the autolyse add the peaked starter, 12.5g wheat gluten, 13g salt and " +
                        "40g water. Mix and knead until smooth and elastic.",
                ),
                RecipeStep(
                    "Bulk rise",
                    "Cover and let rise at room temperature until puffy and noticeably larger, " +
                        "about 3–4h.",
                    timerMinutes = 210,
                ),
                RecipeStep(
                    "Stretch & folds",
                    "During the first ~80 min of the rise, do a set of stretch-and-folds every " +
                        "20 min — 3 to 4 sets total. Restart this timer after each set.",
                    timerMinutes = 20,
                ),
                RecipeStep(
                    "Shape, seed & cold proof",
                    "Shape into one loaf or divide into buns. Stir 50g sesame seeds with 15g " +
                        "water into a slurry and coat the tops. Refrigerate 8–12h.",
                    timerMinutes = 600,
                ),
                RecipeStep(
                    "Bake at 250°C",
                    "Buns: 8 min top+bottom heat with boiling water poured into the bottom tray " +
                        "for steam, then 8–10 min fan/convection until golden. " +
                        "Loaf: bake in a preheated Dutch oven — lid on to steam, then lid off to " +
                        "brown (≈20 min + 20 min; check your oven). Cool before cutting.",
                    timerMinutes = 8,
                ),
            ),
        ),
    )
}
