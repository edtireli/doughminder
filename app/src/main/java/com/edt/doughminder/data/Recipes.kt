package com.edt.doughminder.data

/**
 * Built-in recipes. Placeholder content for now — drop real recipes in here
 * (same shape) and the Recipes screen + step timers pick them up automatically.
 */
object Recipes {
    val all = listOf(
        Recipe(
            id = "classic-loaf",
            title = "Classic Sourdough Loaf",
            summary = "A sample recipe so you can see how steps and timers work. Real recipes coming soon.",
            steps = listOf(
                RecipeStep("Feed the starter", "1:1:1 starter, flour, water. Wait until doubled and bubbly.", timerMinutes = 240),
                RecipeStep("Autolyse", "Mix flour and water, rest covered.", timerMinutes = 60),
                RecipeStep("Mix", "Add starter and salt, mix until incorporated."),
                RecipeStep("Bulk ferment + folds", "Stretch and fold every 30 min, 4 rounds.", timerMinutes = 30),
                RecipeStep("Shape + cold proof", "Shape, into banneton, into the fridge overnight.", timerMinutes = 720),
                RecipeStep("Bake", "Dutch oven at 230°C: 20 min lid on, 20–25 min lid off.", timerMinutes = 20),
            ),
        ),
    )
}
