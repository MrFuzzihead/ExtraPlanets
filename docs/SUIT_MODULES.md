# Space Suit Module System — Design & Implementation Plan

## Overview

Replace the multi-tier space suit system with a single electric-powered suit that uses
**modules** (installed per-piece via a future GUI) to upgrade protection, add functionality,
and handle radiation/pressure tiers. Each module has a passive power cost (drained every tick)
and/or a use cost (drained on specific actions).

---

## Slot Constants

| Slot | Piece | Module GUI icon slot ID |
|------|-------|------------------------|
| 0 | Boots (FEET) | 0 |
| 1 | Leggings (LEGS) | 1 |
| 2 | Chestplate (CHEST) | 2 |
| 3 | Helmet (HEAD) | 3 |

---

## Universal Modules (any slot)

### Radiation Shield (T1 / T2 / T3)
- **Effect:** Increases the radiation tier the suit can handle (T1 → T2 → T3).
- **Recipe ingredient placeholder:** Use the tiered space suit materials from upstream
  (e.g. Tier2/Tier3/Tier4 suit materials that would have been used for higher-tier suits).
- **Power cost:** 10 / 20 / 30 per tick (passive).

### Pressure Seal (T1 / T2)
- **Effect:** Increases the pressure protection tier (T1 → T2).
- **Recipe ingredient placeholder:** Same as above — lower tier suit materials.
- **Power cost:** 5 / 10 per tick (passive).

### Protection + (Iron → Diamond → etc.)
- **Effect:** Bumps the armor protection of that piece by one material tier per module level.
  Base suit is Iron `{2,6,5,2}`. Each module shifts it up one step.
- **Recipe ingredient placeholder:** The vanilla armor piece of the target tier,
  e.g. Diamond Chestplate → Protection + module on the chestplate.
- **Power cost:** 5 per tick (passive).

### Battery Expansion
- **Effect:** Increases max energy storage of that piece by 25% (stackable up to 4× = 100% extra).
- **Recipe ingredient placeholder:** Advanced/massive batteries from the mod.
- **Power cost:** 0 (no drain).

---

## Helmet Modules (slot 3)

### Night Vision
- **Effect:** Grants permanent night vision while active (no potion flicker).
- **Recipe ingredient placeholder:** Golden carrot + redstone + glass? Or a potion of night vision.
- **Power cost:** 1 per tick (passive).

### Sensor Glasses
- **Effect:** Shows GC sensor glasses overlay (valuable blocks, player entities).
- **Recipe ingredient placeholder:** GC Sensor Glasses item.
- **Power cost:** 5 per tick (use).

### Solar Panel (Basic / Advanced)
- **Effect:** Recharges all 4 suit pieces slowly in sunlight (0.005 / 0.01 gJ per 60 ticks per piece).
- **Recipe ingredient placeholder:** IC2 Solar Panel block (Basic) / GC Solar Panel (Advanced).
- **Power cost:** 0 (generates power).

---

## Chestplate Modules (slot 2)

### Jetpack (T1 / T2)
- **Effect:** Vertical thrust while holding space in mid-air. T1 = 0.15 accel / 0.5 max.
  T2 = faster (values TBD). Draws from suit power.
- **Recipe ingredient placeholder:** IC2 Jetpack (T1) / GC Electric Jetpack (T2).
- **Power cost:** 35 per tick (use, while thrusting).

---

## Leggings Modules (slot 1)

### Speed Boost (T1 / T2)
- **Effect:** Increases movement speed by 10% (T1) / 20% (T2).
- **Recipe ingredient placeholder:** Sugar + rabbit's foot? Or potion of swiftness.
  **TBD** — need to decide items.
- **Power cost:** 5 / 10 per tick (passive).

### Step Assist
- **Effect:** Auto-jump up 1-block heights (like Minecraft 1.8+ automatic stepping).
- **Recipe ingredient placeholder:** Piston + redstone? Or slime block.
  **TBD** — need to decide items.
- **Power cost:** 2 per tick (passive).

---

## Boots Modules (slot 0)

### Gravity Controller
- **Effect:** Replaces the separate `spaceSuitGravityBoots` item. Provides GC gravity override:
  `gravityOverrideIfLow = 55`, `gravityOverrideIfHigh = 75`.
- **Recipe ingredient placeholder:** The old gravity boots item itself (after removing it as a wearable).
- **Power cost:** 5 per tick (passive).

### Enhanced Gravity
- **Effect:** Stronger gravity override: 80 low / 90 high.
- **Recipe ingredient placeholder:** Nether star? Or advanced tier material.
- **Power cost:** 10 per tick (passive).

### Anti-Gravity Jump
- **Effect:** Increased jump height (1.5×).
- **Recipe ingredient placeholder:** Piston + slime block? Or feather + redstone.
  **TBD** — need to decide items.
- **Power cost:** 5 per tick (use, on jump).

### No Fall Damage
- **Effect:** Negates all fall damage while active.
- **Recipe ingredient placeholder:** Feather + iron boots? Or previous tier module item.
- **Power cost:** 100 per tick (use, while falling).

---

## Data Model

Modules are stored as NBT on each suit ItemStack:

```nbt
{
  modules: [
    { module: "gravity_controller", active: 1 },
    { module: "radiation_shield_t1", active: 1 },
    { module: "night_vision", active: 0 }
  ]
}
```

The `ModuleHelper` class handles read/write to NBT, install (consumes requirements from
inventory), uninstall (returns items), and power cost checks.

---

## Tick Loop

In `SpaceSuitArmor.onArmorTick()` (server-side only):

1. Read installed modules from the ItemStack NBT.
2. For each **active** module:
   a. Deduct `passivePowerCost` from the piece every second (20 ticks).
   b. If `passivePowerCost > 0` and power is insufficient → deactivate the module.
   c. Call `module.tickServer(player)` for per-tick logic (jetpack thrust, night vision
      potion effect, gravity override via IArmorGravity, etc.).

---

## GUI (Deferred)

When implemented: shift + right-click the armor item (in hand or worn) opens a GUI
showing installed modules (togglable on/off), available modules for that slot with
install buttons and their material requirements, and the piece's current energy bar.

---

## Item / Recipe TBDs

These need concrete items decided for module crafting:

| Module | Recipe Item | Notes |
|--------|-------------|-------|
| Night Vision | Golden carrot + glass + glowstone dust? | Potion of night vision brewed into a module |
| Speed Boost | ? | Sugar + rabbit's foot? Potion of swiftness? |
| Step Assist | ? | Piston + slime block? Redstone + observer-like logic? |
| Anti-Gravity Jump | ? | Feather + slime ball? Phantom membrane equivalent? |
| Protection + | Vanilla diamond armor piece | Uses the same slot's armor piece |
| Pressure Seal / Radiation Shield | Higher-tier suit materials | From upstream that we no longer craft into suits |
| Solar Panel | IC2 solar panel / GC solar panel | Direct block item |

---

## Implementation Order

### Phase 1 — Foundation ✅ DONE
1. `Module.java` — abstract base with name, slot, power costs, tick methods
2. `ModuleHelper.java` — NBT read/write, install/uninstall, power costs
3. `ExtraPlanets_Modules.java` — static registry of all module instances
4. `IModularArmor.java` — interface for armor items that accept modules
5. Wiring: `ExtraPlanets_Modules.init()` called from `ExtraPlanets.preInit()`
6. Wire passive power drain into `SpaceSuitArmor.onArmorTick()`

### Phase 2 — Universal Modules ✅ DONE
7. `ModuleRadiationShield.java` — T1/T2/T3
8. `ModulePressureSeal.java` — T1/T2
9. `ModuleProtectionPlus.java` — bump armor tier (tier 1 = Diamond)
10. `ModuleBatteryExpansion.java` — increase max energy by 25% per module

### Phase 3 — Helmet Modules ✅ DONE
11. `ModuleNightVision.java` — potion effect
12. `ModuleSensorGlasses.java` — marker for GC overlay (rendering deferred to GUI phase)
13. `ModuleSolarPanel.java` — recharge (0.005/0.01 gJ per 60s per piece)

### Phase 4 — Chestplate Modules ✅ DONE
14. `ModuleJetpack.java` — vertical thrust (T1/T2) — key handling deferred to GUI phase

### Phase 5 — Leggings Modules ✅ DONE
15. `ModuleSpeedBoost.java` — movement speed (T1/T2 via potion effect)
16. `ModuleStepAssist.java` — auto-step (sets stepHeight = 1.0)

### Phase 6 — Boots Modules ✅ DONE
17. `ModuleGravityController.java` — GC gravity override (55/75)
18. `ModuleEnhancedGravity.java` — stronger gravity override (80/90)
19. `ModuleAntiGravityJump.java` — jump boost potion effect
20. `ModuleNoFallDamage.java` — fall negate

### Phase 7 — Cleanup 🔲 NEXT
21. Remove `spaceSuitGravityBoots` item (replace with Gravity Controller module)
22. Update `SpaceSuitArmor.gravityOverrideIfLow/IfHigh` to check for the module ✅ (done)
23. Update `IBreathableArmor` and radiation/pressure interfaces to check modules
24. Add recipes for all modules

### Phase 8 — GUI 🔲 FUTURE
25. Container + GUI for module management
26. Shift+right-click handler in `MainHandler`