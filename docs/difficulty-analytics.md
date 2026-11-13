# Difficulty analytics (local-only)

Every solo mode (`DifficultyAnalytics.kt`, `app/src/main/java/com/oddduel/game/analytics/`)
reports its own session the instant it ends - elapsed time, rounds attempted, and the
streak/score reached - and keeps a running average "time per round" per mode on-device,
at `context.getExternalFilesDir("analytics")/difficulty_stats.json`.

## Why this exists

It's a proxy for how hard a genuinely engaged player is actually finding each mode's
*current* tuning (the constants in `TimeAttackViewModel`, `ReflexHunterViewModel`,
`SurvivalStreakViewModel`, `MemoryFlashViewModel`). A rising average time-per-round means
the pacing is outrunning real players; a falling one means there's room to push the curve
harder. It's meant to replace guessing with real numbers the next time someone revisits
difficulty tuning.

## What it is NOT

- **Not read by the running app anywhere.** Nothing in this file ever changes live
  gameplay. It is written-only from the app's side.
- **Not uploaded automatically, ever.** The file never leaves the device on its own, and
  the app has no network call or embedded credential that could send it anywhere. The
  only way it leaves the device is the player explicitly tapping Settings -> "Share
  Gameplay Stats", which hands the current JSON to Android's share sheet so *they* choose
  where it goes (e.g. back to whoever is maintaining the app, to fold into a future
  tuning pass).
- **Deliberately not something the app pushes to GitHub by itself.** An Android APK is
  public the instant it's installed - anyone can decompile it. Baking in anything with
  write access to this repository (a GitHub token, an upload endpoint with a secret key)
  would hand that same access to every single install, which is a real security hole, not
  a hypothetical one. If real per-device numbers are ever needed here, they arrive via the
  share flow above (a human sends them), not an automatic pipeline.

## Schema (`difficulty_stats.json`)

```json
{
  "schemaVersion": 1,
  "modes": {
    "<mode_key>": {
      "sessionCount": 0,
      "avgMsPerRound": 0.0,
      "bestResult": 0,
      "lastUpdatedEpochMs": 0
    }
  }
}
```

- `mode_key`: one of `time_attack`, `reflex_hunter`, `survival_streak`, `memory_flash`.
- `sessionCount`: how many *serious* sessions have been folded into the running average
  (see the per-mode minimum streak/score in `DifficultyAnalytics.MIN_SERIOUS_RESULT` -
  this filters out instant exits, misclicks and one-off testing).
- `avgMsPerRound`: incremental running mean of `elapsedMs / roundsPlayed` across those
  sessions - the number to actually look at when retuning a mode's pacing constants.
- `bestResult`: the best streak/score seen across those sessions, for context only.
- `lastUpdatedEpochMs`: when this mode's entry last changed.

## Workflow for using it

1. Ask a player (or do it yourself on a test device) to open Settings and tap "Share
   Gameplay Stats" after a session of real, serious play.
2. Hand the resulting JSON to whoever is tuning the app next (paste it into a chat with
   the assistant doing the update, attach it to an issue, whatever's convenient).
3. Compare `avgMsPerRound` per mode against that mode's current pacing constants (e.g.
   `ReflexHunterViewModel.START_INTERVAL_MS`/`MIN_INTERVAL_MS`) and adjust by hand.
4. Commit the tuning change as a normal code change - this file only informs that change,
   it never makes it automatically.
