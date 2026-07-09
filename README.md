# Doughminder 🍞

An Android app that reminds you to feed your sourdough starter — and argues with you when you don't.

## Your starters

Add multiple starters, each with a name, pronouns (she/he/they), a flour color, and its own daily reminder time (default 8:00 AM). Each jar has a face whose mood tracks hunger: happy when fed, worried after a day, angry after two.

<p align="center"><img src="screenshots/01_home.png" width="320" alt="Home screen with a starter tile"></p>

## Adopting a starter

<p align="center"><img src="screenshots/02_add_starter.png" width="320" alt="New starter screen"></p>

## The daily nag — and the negotiation

At feeding time a notification asks *"Time to feed Bertha"* with three reply buttons right on it:

- **Yes, I fed her** → marks fed, resets the clock: *"Good. She forgives you. See you tomorrow, same time."*
- **Later** → she asks **"When?"** (1h / 3h / 6h), then **haggles you shorter** — "Six hours? Be honest with yourself. Bertha's fine for six hours; the problem is you won't remember at hour six. Three?" — before settling on whatever you pick and **setting a real alarm** for it. When it fires she calls out the promise: *"You said 1 hour. Time's up. Where's the flour?"* Each round escalates and stitches in real sourdough facts (hooch, acetone, pH vs. gluten) and guilt.
- **Leave me alone** → for a **counter** starter this is *refused*, because a room-temperature starter needs feeding about daily and can't be muted for days. It offers the honest fix — **Move to fridge** (drops her to ~weekly) — or a grudging 1h. Only **fridge/freezer** starters can actually be left until their next real feeding.

The copy is calibrated to the real timescale: a few hours late is *fine*, the danger is you forgetting; she only turns sour and weak after **days** overdue.

<p align="center"><img src="screenshots/05_notification.png" width="270" alt="Feeding reminder"> <img src="screenshots/06_argument.png" width="270" alt="The counter-offer after tapping Later"> <img src="screenshots/07_leave_me_alone.png" width="270" alt="Leave me alone, refused, with a fridge offer"></p>

## Recipes

Ships with **Sesame Sourdough (Loaf or Buns)** — a half-graham, wheat-gluten-boosted, sesame-crusted dough — broken into steps with per-step timers that fire a notification when it's time for the next move (build the starter, autolyse, mix, bulk rise, folds every 20 min, cold proof, bake). Add more in `app/src/main/java/com/edt/doughminder/data/Recipes.kt` (same `Recipe`/`RecipeStep` shape) and they appear automatically.

<p align="center"><img src="screenshots/03_recipes.png" width="320" alt="Recipes screen"></p>

## Settings

Default reminder time for new starters, the "Argue back" toggle, a "Make reminders reliable" button, and a test nag button.

<p align="center"><img src="screenshots/04_settings.png" width="320" alt="Settings screen"></p>

## Architecture notes

- Kotlin + Jetpack Compose (Material 3), warm dark theme (ink `#262624`, cream text, coral `#D97757`, serif display type). No backend — everything is on-device.
- `data/Sass.kt` is the argument engine: pre-written escalation chains with `{name}`/`{she}`/`{her}` pronoun templating and large fact/guilt pools, isolated behind small functions (`nagBody(depth, promised)`, `confirmBody(hours)`, `settledBody(hours)`, …) so a lightweight on-device LLM could replace the line-picking later without touching the notification plumbing.
- **The daily reminder fires on the dot** at the time you set, every 24h, whether or not the app is open. Both the daily reminder and the snoozes use `setAlarmClock` — the single most reliable alarm Android offers: exact, and exempt from Doze, app-standby, *and* (on most OEMs) battery-optimization killing, because breaking it would break the system clock. Each alarm re-arms the next one, and everything is re-armed on boot, app update, and app start. The app also holds `USE_EXACT_ALARM`, and Settings has a **"Make reminders reliable"** button that requests a battery-optimization exemption for the most aggressive OEMs.
- Starters and settings persist as JSON in DataStore.

## Build & install

**Release (the real app — signed, installable, what you want on your phone):**

```bash
./gradlew assembleRelease
adb install app/build/outputs/apk/release/app-release.apk
```

Release signing reads `keystore.properties` at the repo root (gitignored, along with the `.jks`). To recreate it on a fresh clone:

```bash
keytool -genkeypair -v -keystore app/doughminder-release.jks -alias doughminder \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass CHANGEME -keypass CHANGEME -dname "CN=Doughminder"
cat > keystore.properties <<'EOF'
storeFile=doughminder-release.jks
storePassword=CHANGEME
keyAlias=doughminder
keyPassword=CHANGEME
EOF
```

If `keystore.properties` is absent the release build is unsigned; debug builds (`./gradlew assembleDebug`) always work without it.

Requires a `local.properties` with your SDK path (Android Studio creates it automatically):

```properties
sdk.dir=/Users/you/Library/Android/sdk
```

On first launch, allow notifications. The daily reminder uses `setAlarmClock`, so it fires exactly on time without any extra permission — but on aggressive OEMs (Xiaomi, Samsung, etc.) tap **Settings → "Make reminders reliable"** once to exempt the app from background killing.
