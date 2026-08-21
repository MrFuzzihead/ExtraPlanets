# Implementation Plan: Server-Side Jupiter Lightning (`jupiterLightingServer`)

## Status

- **Tracked from:** `CODEBASE_ANALYSIS_FINDINGS.md` — P1 "Network packet / MainHandler" related note + optimization #8
  ("Remove dead config keys (`jupiterLightingServer`) or implement the feature they promise").
- **State:** DRAFT — scope/balance decisions below are proposed, not yet agreed. No code written.
- **Config already exists:** `Config.jupiterLightingServer` is read in `Config.java` (
  `config/ExtraPlanets.cfg` → "Enable Server Real Lighting Bolts on Jupiter", default `true`) but is
  **not referenced anywhere in code** — it is currently a dead key.

---

## Executive Summary

Jupiter only has **cosmetic, client-side** fake lightning today: `MainHandler.onPlayer` (a
client-only `PlayerTickEvent`) spawns a client `EntityLightningBolt` around the player at ~1% per tick,
gated by `Config.jupiterLightingClient`. That bolt is a **no-op on the actual world** — on the client a
bolt only renders and plays the thunder sound; it never damages or ignites (that logic lives in the
server-side branch of `EntityLightningBolt.onUpdate`).

The config promises a second, real, **server-side** feature ("Real Lighting Bolts on Jupiter, SERVER
SIDE") that was never implemented. This plan implements it so the dead config key starts doing what it
advertises: Jupiter actually has real lightning, handled server-authoritatively and synced to clients
for the visuals + sound.

---

## 1. Background / Current Behavior (verified)

| | Client fake (`jupiterLightingClient`) | Server real (`jupiterLightingServer`) |
|---|---|---|
| Current impl | `MainHandler.onPlayer` — client `PlayerTickEvent` (FML bus, client-only) | **Nothing** — dead config |
| Bolts created | `EntityLightningBolt` in `WorldClient` | n/a |
| Damages / sets fire | No (client branch only renders + sounds) | Intended: yes (vanilla-style) |
| Synced to players | No (local-only) | Intended: yes (real server entity) |

### Key 1.7.10 mechanics to respect
- `EntityLightningBolt.onUpdate()` does the real damage/fire logic only in its **server branch**
  (`!worldObj.isRemote`). Spawning it in the **server** world makes it a real, world-affecting bolt.
- A server-spawned global/weather entity is **synced to nearby clients** via the global-entity spawn
  packet, so clients get the bolt render + thunder sound without needing the client-side fake path.
  *(Verify exact packet/tracking at implementation time.)*
- `WorldProvider` provides a cached `protected final Random rand` — reuse it (same RNG-caching
  principle applied in the MainHandler P1 fix; **do not** allocate `new Random()` per tick).
- `WorldProvider.onUpdate()` is ticked once per world tick on the **server** for that dimension — the
  natural server-authoritative hook (mirrors how GC drives per-planet effects).

---

## 2. Proposed Design

### 2a. Server spawn — hook `WorldProviderJupiter.onUpdate()`
`src/main/java/com/mjr/extraplanets/planets/Jupiter/WorldProviderJupiter.java` (currently has **no**
`onUpdate()` override):

- Add an override and a cooldown field:
  - `private int nextLightningTick;`
- `onUpdate()`:
  - `super.onUpdate();`
  - Early-out unless `Config.jupiterLightingServer && !this.worldObj.isRemote`.
  - Throttle: `if (this.nextLightningTick > 0) { this.nextLightningTick--; return; }`
    then reset the cooldown to a design-decision value, e.g. `80 + this.rand.nextInt(120)` ticks
    (~4–10 s between bolts). *(Proposed conservative default; see Open Questions.)*
  - Pick a target: choose a random loaded area near a random player in the dimension
    (`worldObj.playerEntities`), or a random point within ~48 blocks of it, clamped to loaded chunks.
    Compute ground height via `worldObj.getTopSolidOrLiquidBlock(x, z)`.
  - `this.worldObj.spawnEntityInWorld(new EntityLightningBolt(this.worldObj, x, y, z));`

### 2b. Entity damage / fire
Default to vanilla behavior (real bolt damages entities and can light blocks on fire on Jupiter),
because the config explicitly advertises "Real Lighting Bolts". Mitigate with the cooldown and by
favoring spots a modest distance from the targeted player (avoid instant/frequent hits). *(Balance is
the main open question — see §4.)*

### 2c. Suppress duplicate client fake lightning on Jupiter
When real server lightning is **enabled**, the client-only fake path in `MainHandler.onPlayer` should
be skipped on Jupiter (otherwise players see double bolts/sounds — the synced real bolt already
renders). Proposed:
- In `MainHandler.onPlayer`, gate the fake path as:
  `Config.jupiterLightingClient && !Config.jupiterLightingServer && <isJupiter>`.
- i.e. the client fake path becomes the "lighting with no server lightning" fallback.
- Document the precedence in the config comment and this plan.

### 2d. Config wiring
- Keep the existing `jupiterLightingServer` key (default `true`) — it is already parsed.
- Optionally add a `jupiterLightingServerCooldown` (int, ticks) so users can tune frequency. *(Proposed;
  not strictly required.)*
- Update the `Config.java` comment ("SERVER SIDE" → describe behavior) and clarify the relationship
  between the two overlapping lightning keys.

---

## 3. Files To Touch

| File | Change |
|---|---|
| `planets/Jupiter/WorldProviderJupiter.java` | Add `onUpdate()` + cooldown field; spawn real bolts server-side |
| `handlers/MainHandler.java` | Make fake client path skip Jupiter when `jupiterLightingServer` is on (prevent duplicates) |
| `Config.java` | Keep `jupiterLightingServer`; clarify comment; optional cooldown key |
| *(optional)* shared helper for "pick a random loaded spawn point near an entity" if cleaner than inlining |
| `docs/CODEBASE_ANALYSIS_FINDINGS.md` | Update optimization #8 + related note to link this plan |

---

## 4. Open Questions / Decisions Needed

1. **Should server bolts be dangerous (damage + fire) or gentle?** The config says "Real Lightning
   Bolts", implying vanilla damage, but an always-stormy planet can feel grief-y. Options: (a) vanilla
   damage, (b) damage only on direct proximity + short fire, (c) cosmetic "real" bolt (spawn on server
   for authority/sync but override `onUpdate()` to skip damage).
2. **Cooldown / frequency** — how often (ticks) and distance-from-player. Proposed default ~4–10 s at
   ~48-block offset; needs playtesting.
3. **Client/server precedence** — confirm `jupiterLightingServer` suppresses the client fake path when
   enabled (§2c), rather than both running.
4. **Should lightning only strike while "raining"/storming on Jupiter**, or always when the config is on?
   (Jupiter reports `canRainOrSnow() == false`; the current fake path runs unconditionally in-dimension.)
5. **Ambient strikes beyond near-player** — decide whether bolts also strike randomly across the loaded
   world, not just near players.

---

## 5. Acceptance Criteria

- [ ] `jupiterLightingServer` is read and actually used (no longer dead config).
- [ ] On the Jupiter server, real bolts spawn at the configured frequency and sync to clients (render +
      thunder sound).
- [ ] Bolts behave per the agreed option in §4.1.
- [ ] Disabling `jupiterLightingServer` (or the whole Jupiter dimension) fully disables the server
      feature; enabling it suppresses the duplicate client fake path on Jupiter.
- [ ] No per-tick `new Random()` allocation (reuses `WorldProvider.rand`).
- [ ] Perf: no per-tick entity churn beyond the throttled strikes; safe on dedicated servers.
- [ ] `compileJava` green; dedicated-server smoke test on the Jupiter dimension.