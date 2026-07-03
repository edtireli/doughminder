# Doughminder 🍞

An Android app that reminds you to feed your sourdough starter — and argues with you when you don't.

## Your starters

Add multiple starters, each with a name, pronouns (she/he/they), a flour color, and its own daily reminder time (default 8:00 AM). Each jar has a face whose mood tracks hunger: happy when fed, worried after a day, angry after two.

<p align="center"><img src="screenshots/01_home.png" width="320" alt="Home screen with a starter tile"></p>

## Adopting a starter

<p align="center"><img src="screenshots/02_add_starter.png" width="320" alt="New starter screen"></p>

## The daily nag

At reminder time a notification asks *"Did you feed Bertha yet?"* with three reply buttons right on it:

- **Yes, I fed her** → marks fed: *"Good. She forgives you. This time."*
- **Later** → an instant comeback, then a follow-up after 15/30/60 min (configurable), escalating through a pre-written argument chain laced with real sourdough facts (hooch, acetone smell, pH vs. gluten).
- **Leave me alone** → one guilt-trip parting shot, then silence until tomorrow.

<p align="center"><img src="screenshots/05_notification.png" width="320" alt="Feeding reminder notification"> <img src="screenshots/06_argument.png" width="320" alt="The comeback after tapping Later"></p>

## Recipes

Recipes with per-step timers that fire notifications when it's time for the next move. Add recipes in `app/src/main/java/com/edt/doughminder/data/Recipes.kt` (same `Recipe`/`RecipeStep` shape) and they appear automatically.

<p align="center"><img src="screenshots/03_recipes.png" width="320" alt="Recipes screen"></p>

## Settings

Default reminder time for new starters, the "Argue back" toggle, the snooze length, and a test nag button.

<p align="center"><img src="screenshots/04_settings.png" width="320" alt="Settings screen"></p>

## Architecture notes

- Kotlin + Jetpack Compose (Material 3), warm dark theme (ink `#262624`, cream text, coral `#D97757`, serif display type). No backend — everything is on-device.
- `data/Sass.kt` is the argument engine: pre-written escalation chains with `{name}`/`{she}`/`{her}` pronoun templating, isolated behind small functions (`morningTitle`, `laterReply(depth)`, …) so a lightweight on-device LLM could replace the line-picking later without touching the notification plumbing.
- Reminders use `AlarmManager` (exact when permitted, inexact fallback), re-armed on boot, app update, and app start. Starters and settings persist as JSON in DataStore.

## Build & install

```bash
./gradlew assembleDebug        # JDK path comes from gradle.properties
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or open the folder in Android Studio and hit Run.

Requires a `local.properties` with your SDK path (Android Studio creates it automatically):

```properties
sdk.dir=/Users/you/Library/Android/sdk
```

On first launch, allow notifications. For minute-exact reminders on Android 14+, grant "Alarms & reminders" in system settings — the app falls back to approximately-on-time otherwise.
