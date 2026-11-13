# ExtraPlanets — Code Review, Dependency Audit and Test Report

> Reviewed: `main` (commit `6f85867f7`, "Update all builds and deps"), 1.7.10 Forge / Galacticraft 3.5.0-GTNH.
> Scope: the active build (`src/main/java`, 1,260 files / 9.4 MB) plus `dependencies.gradle` and the Gradle setup.
> The `migrate/` tree (1.12.2 port + vendored Galacticraft/mjrlegendslib) is **not** part of the active build and is
> excluded except where noted.
>
> This document supersedes `docs/CODEBASE_ANALYSIS_FINDINGS.md`, which is kept for the history of the earlier fixes.

## Contents

1. [What changed in this pass](#1-what-changed-in-this-pass)
2. [Dependency audit (`dependencies.gradle`)](#2-dependency-audit)
3. [Test infrastructure](#3-test-infrastructure)
4. [Bugs found and still open](#4-bugs-found-and-still-open)
5. [Dead code inventory](#5-dead-code-inventory)
6. [Optimizations and refactors](#6-optimizations-and-refactors)
7. [Feature ideas](#7-feature-ideas)
8. [What cannot be tested without the game](#8-what-cannot-be-tested-without-the-game)
9. [Priority plan](#9-priority-plan)

---

## 1. What changed in this pass

| Area | Change |
|---|---|
| Build | New `addon.gradle.kts`: JaCoCo 0.8.16, Java 8 unit-test launcher, coverage report + ratchet, `gameTest` source set, `gameTestServer` task, `-PsmokeTest` hook |
| Dependencies | Removed the unused `ModularUI:1.3.4`; `ModularUI2` is now `implementation` instead of `api`; added JUnit/Mockito test dependencies; every entry annotated with why it has that scope |
| Tests | 108 headless JUnit 4 tests in `src/test/java` (was 0) |
| In-game tests | New never-shipped test mod in `src/gameTest/java`, 24 checks, run by `./gradlew gameTestServer` |
| Production code | `ExtraPlanets_Modules.init()` is now idempotent; `Module.copy()` no longer shares the prototype's requirement list; new `SmokeTestHook` + `ExtraPlanets.loadComplete` |
| Docs | `README.md` rewritten (badges, requirements, build/test commands), this file added |

Measured coverage of the unit suite (`jacocoTestReport`, generated classes excluded):
**1.8 % of 80,990 lines, 0.5 % of 34,022 branches**. That is expected for a mod that is 5 MB of worldgen and
rendering; the ratchet is set just below it so it can only go up.

All three commands pass on a clean checkout:

```
./gradlew test               # 91 tests, 0 failures
./gradlew check              # + spotless, checkstyle, coverage floor, gameTest compile
./gradlew gameTestServer     # 27/27 in-game checks, EXTRAPLANETS_GAMETEST_OK
./gradlew runServer -PsmokeTest   # EXTRAPLANETS_SMOKE_TEST_OK
```

---

## 2. Dependency audit

### 2.1 What the review found, and what was changed

| Dependency | Was | Now | Why |
|---|---|---|---|
| `com.github.GTNewHorizons:Galacticraft:3.5.0-GTNH:dev` | `api` | **`api`** (unchanged) | Correct. Galacticraft types appear in the public signatures of `com.mjr.extraplanets.api.*` (`ICargoEntity`, `ILandingPadAttachable`) and in ~650 files. A dependant must compile against the same GC version. |
| `com.github.GTNewHorizons:ModularUI2:2.4.0-1.7.10:dev` | `api` | **`implementation`** | ModularUI2 is used *only* inside `client/gui/GuiModuleManager` and `proxy/ClientProxy`. Nothing in the mod's public API mentions `com.cleanroommc.modularui`, so publishing it as an `api` dependency forced every dependant to compile against a 2.4.0 GUI library it never sees. `implementation` keeps it on the runtime classpath (which the GUI needs) without leaking it. |
| `com.github.GTNewHorizons:ModularUI:1.3.4:dev` | `compileOnly` (transitive = false) | **removed** | Dead. No file imports `com.gtnewhorizons.modularui` (ModularUI **1**); every GUI import is `com.cleanroommc.modularui` (ModularUI **2**). `./gradlew dependencies` also shows ModularUI2 already pulling `ModularUI:1.3.4` transitively, so the entry was purely redundant. |
| `com.github.GTNewHorizons:NotEnoughItems:2.9.5-GTNH:dev` | `devOnlyNonPublishable` | **`devOnlyNonPublishable`** (unchanged) | Correct. `NEIExtraPlanetsConfig` implements `IConfigureNEI`, which NEI discovers itself, so NEI must be on the compile *and* dev-runtime classpath but must never be a published dependency. `compileOnly` alone would make the NEI handler unavailable in `runClient`. |
| `com.github.GTNewHorizons:CodeChickenCore:1.5.0:dev` | `compileOnly` | **`compileOnly`** (unchanged) | Correct. Only supplies `codechicken.nei.*`/`codechicken.lib.gui.GuiDraw` for the optional NEI handler; not needed for the mod to run. Note NEI pulls `CodeChickenCore:1.4.22` transitively, and the explicit `1.5.0` wins — keep the explicit pin. |
| `com.github.GTNewHorizons:ForgeMultipart:1.8.0:dev` | `runtimeOnlyNonPublishable` | **`runtimeOnlyNonPublishable`** (unchanged) | Correct. `ExtraPlanets_MicroBlocks` registers microblocks entirely by reflection (`Class.forName("codechicken.microblock...")`), so multipart is needed neither to compile nor for the mod to run, but it must be present in the dev run to exercise the path. `devOnlyNonPublishable` would behave identically; `runtimeOnlyNonPublishable` is semantically cleaner because there are no compile-time references. |
| `curse.maven:cofh-core-69162:2388751` | `compileOnly` | **`compileOnly`** (unchanged) | Correct. Supplies `cofh.api.energy.IEnergyContainerItem`, which is only referenced through `@Optional.Interface(modid = "CoFHAPI|energy")`. |
| `curse.maven:mekanism-268560:2475797` | `compileOnly` | **`compileOnly`** (unchanged) | Correct, same pattern (`mekanism.api.energy.IEnergizedItem`). |
| `net.industrial-craft:industrialcraft-2:2.2.828-experimental:dev` | `compileOnly` | **`compileOnly`** (unchanged) | Correct, same pattern (`ic2.api.item.ISpecialElectricItem`, `ic2.api.recipe.Recipes`). Note it is already a transitive of Galacticraft, and GTNH's substitution rule resolves it to `curse.maven:ic2-242638:2353971`; the explicit entry is redundant but documents the optional integration set in one place. |
| `junit`, `mockito-core`, `mockito-inline` | — | **added** | See §3. |

### 2.2 Things worth knowing about the resolved tree

`./gradlew dependencies --configuration compileClasspath` shows:

* `ModularUI2` transitively drags in **`lwjgl3ify:3.0.34`** and a `-sources` artifact
  (`neverenoughanimation-1062347:7664003-sources-7663978`). Neither is used by this mod, and neither is a problem, but
  they do inflate the dev classpath and the IDE model. If startup or IDE sync ever gets slow, `exclude(group = ...)`
  on the MUI2 entry is the first thing to try.
* `IC2` is substituted from `net.industrial-craft` to `curse.maven:ic2-242638` by GTNH's well-known-repository rules.
  The `:dev` classifier in the declaration is ignored by the substitution, which means the classpath entry is
  effectively `curse.maven`-provided. Declaring `curse.maven:ic2-242638:2353971` directly would be more honest but
  would pin a CurseForge file id instead of a version string.
* The `Galacticraft` POM already brings IC2 onto the compile classpath, so the explicit IC2 line is not what makes
  `ic2.api.*` resolve.

### 2.3 Missing declarations

* **`usesMixins = false`** but ModularUI2 ships mixins; that is fine because they are MUI2's own, not this mod's.
* **No `modrinthProjectId` / `curseForgeProjectId`** in `gradle.properties`, so releases cannot be published by the
  `release-tags` workflow. Adding the numeric CurseForge id (`extraplanets` is project `2232%`) and the Modrinth slug
  would let CI publish without a manual upload.
* **`curseForgeRelations` is empty.** The mod hard-requires Galacticraft, so `requiredDependency:galacticraft` should
  be set; the resolved dependency otherwise lives only in `mcmod.info`'s prose.
* `Constants.DEPENDENCIES_FORGE` asks for `Forge@[10.13.4.1558,)` while the build targets `10.13.4.1614`. That is
  fine (the lower bound is intentional for pack compatibility), but it is worth a comment so nobody "fixes" it.

---

## 3. Test infrastructure

### 3.1 Why this shape

The reference implementation (`MrFuzzihead/PeripheralsPlusPlus`) uses three layers, and this mod needs the same
three for the same reasons:

1. **Headless JUnit** for everything that is pure data handling. Fast, runs in CI on every PR, no game.
2. **An in-game test mod** for everything that needs FML's registries. A separate `@Mod` in a separate source set,
   so it can never end up in the published jar.
3. **A smoke-test boot** so CI can assert that a real dedicated server reaches "world loaded" without an operator.

### 3.2 What was added

`addon.gradle.kts`:

* JaCoCo `0.8.16`, attached to `Test` and (for `gameTestServer`) to the forked `JavaExec` through an explicit
  `-javaagent` argument provider. The agent jar is a `@Classpath` task input, so the configuration cache stays happy.
* The unit suite runs on a **Java 8 launcher** (`JavaToolchainService`), which both matches the mod's bytecode target
  and keeps Mockito 4's inline mock maker well-behaved.
* `testCompileClasspath`/`testRuntimeClasspath` extend `compileOnly`, because several production classes implement
  compile-only supertypes (`cofh.api.energy`, `mekanism.api.energy`, `ic2.api.item`) and cannot even be *named*
  without them.
* The test working directory is redirected to `build/test-workdir`, so `Config.init()` (which writes
  `config/ExtraPlanets.cfg` relative to the working directory) does not pollute the checkout.
* `jacocoTestReport` (HTML + XML) and `jacocoTestCoverageVerification` with a floor just under the measured baseline.
  `check` depends on the verification, so CI fails on a coverage regression.
* `jacocoGameTestReport` reports the in-game run against the class files JaCoCo actually saw. It self-skips when the
  data is absent (see §8.5).
* A `gameTest` source set wired into `runServer`/`runClient` when `-PgameTest` or `gameTestServer` is requested, with
  `fml.queryResult=confirm` pre-answered.
* `-PsmokeTest` sets `extraplanets.smokeTest`, which `SmokeTestHook` turns into a clean exit at world load.

`src/test/java` (91 tests):

| Class | Covers |
|---|---|
| `ConfigDefaultsTest` (9) | The real `config/ExtraPlanets.cfg` defaults: planet/station dimension ids unique, 49 biome ids unique **and inside 0-255**, schematic GUI/page ids unique, `Config.init()` idempotent |
| `ConfigSyncContractTest` (18) | The server→client config sync: every synced name resolves to a real `public static int`, no name is synced twice, every `...ID` field is covered, each field round-trips through its own getter **and** setter, pair order is irrelevant, a missing/unknown/non-numeric pair affects nothing, a legacy positional payload is ignored, null/empty/odd-length packets change nothing, and save/restore around a remote session |
| `ExtraPlanets_BiomesTest` (10) | In-range ids are kept verbatim, out-of-range ids are relocated into the array, a collision with another mod's biome is reported but **not** remapped, name lookups are cached |
| `ModuleHelperInstallTest` (15) | `installModule`/`uninstallModule`/`consumeFromInventory`: wrong-slot and duplicate rejection, requirement consumption and refund (including a lazily declared cost), drop-when-inventory-full |
| `ModuleHelperTest` (24) | The suit-module NBT format: setup, add/replace/remove, active flag, subtype, unknown names, wrong tag types, copies-not-prototypes, slot compatibility |
| `ModuleRegistryTest` (12) | `init()` idempotency, 17 modules, unique lower_snake_case names, slot types match the `ModuleHelper`/`ItemArmor.armorType` convention per piece, every module has an icon and requirements, copies do not share lists |
| `ModuleRequirementOrderTest` (6) | Requirements resolve after the item they reference is registered, a config-disabled item drops its requirement instead of keeping a null stack, a declaration is evaluated once, `setRequirements` overrides a declaration, `copy()` of an unresolved module resolves |
| `PacketSimpleCodecTest` (10) | Real codec round trip for every packet type, plus truncated/out-of-range/negative ordinals are discarded instead of throwing |
| `MachineTileEntityTest` (4) | Every machine constructs without a world, refinery tanks are per-instance, `readFromNBT` tolerates an empty tag |

`src/gameTest/java` (24 checks, `./gradlew gameTestServer`):

| Group | Checks |
|---|---|
| config | biome ids inside the array, dimension ids negative, config file created, the sync payload is name/value pairs, the payload survives the real packet codec, replaying it over a scribbled config restores every id |
| blocks | mercury block set registered in the right namespace, every body block has a name, machine blocks registered |
| items | rocket line registered, space suit pieces registered, item ids round trip through `Item.getItemById` |
| tile entities | refinery survives `writeToNBT` → `TileEntity.createAndLoadEntity`, tank capacities preserved, solar panel round trips |
| ore dictionary | planet ores and blocks registered, every entry points at a real stack |
| recipes | crafting recipes exist, planet ore blocks smelt into the matching ore-dictionary ingot, the mod's own ore smelts into its ingot |
| modules | registry populated, every requirement resolves to a real item **in game**, the three ExtraPlanets-item requirements resolved to the actual registered items, install/uninstall round trip, a fresh suit has no modules |

### 3.3 Production fixes required by the tests

* `ExtraPlanets_Modules.init()` is now guarded by an `initialised` flag. Before, a second call (a reload, a test, or a
  future "reload config" feature) duplicated all 17 prototypes, and `ModuleHelper.getModules` would then return the
  first match while `setModules` wrote all of them.
* `Module.copy()` deep-copies the requirement list. It used to be a bare `super.clone()`, so a copy shared — and could
  mutate — the registry prototype's `List<ItemStack>`. Any GUI code that edited a copy's requirements silently
  changed the recipe for every suit of that type for the rest of the session.
* Config sync, module requirements and the slot-convention docs were fixed in the P1 pass below.

---

## 4. Bugs found and still open

Ordered by severity. Items marked ✅ were already fixed (in an earlier pass, or in the P1 pass recorded below); the
rest are still open.

### P1 — Config sync can silently mis-assign every ID after a broken field — ✅ FIXED

**Was:** `Config.getServerConfigOverride()` read 111 fields by name with `Field.getInt(null)` and **skipped** any field
that threw; `Config.setConfigOverride(List)` then walked the *name* list by index while reading the *value* list by the
same index. A single renamed, retyped or removed field made the server send 110 values and the client assign value *n*
to key *n+1* from the gap onwards — silently mapping, say, a biome id onto the schematic GUI id. Nothing crashed.

**Now:** the payload is a list of `(name, value)` pairs and the client keys every value by name, so there is no
position to shift. The names, and the getters/setters behind them, live in one table
(`Config.SYNC_ENTRIES`, built by `buildSyncEntries()`): the values are no longer read reflectively, so a renamed or
retyped field is a compile error instead of a skipped value. An unknown, non-string, non-numeric or odd-length payload
is reported and skipped entry by entry — a legacy positional payload is ignored outright rather than half-applied.

**Guarded by:** `ConfigSyncContractTest` (18 tests) — every name resolves to a real `public static int`, no name is
synced twice, **every `...ID` field in `Config` must be in the payload** (so a new id cannot be forgotten), each field
round-trips through its own getter *and* setter (catching a copy-pasted entry), pair order does not matter, and a
dropped pair cannot shift the rest. In game, `gameTestServer` encodes the real payload through the real
`PacketSimple`/`NetworkUtil` codec and replays it over a scribbled config, which is what a login does.

### P1 — `Config` is a 118 KB wall of mutable public statics — OPEN

`Config.java` has 317 `public static` fields, read directly by ~600 files. There is no way to reload config, no way to
have two configs (client and server) in one JVM, and `ConfigState` exists only so the tests can snapshot/restore the
`public static` values they touch. The sync half of this problem is now solved properly (the `SyncEntry` table is the
sanctioned way to read and write a config value that other sides must agree with), but the statics remain.
**Recommendation:** an instance-based `Config` (one per side) is the long-term fix; a `Config.get()` facade over the
existing fields would be a safe, incremental first step. Deliberately not attempted in this pass — it touches ~600
files and no test can prove a mechanical 600-file edit is behaviour-preserving.

### P1 — `ExtraPlanets_Modules.init()` depends on `ExtraPlanets_Items.init()` having run first — ✅ FIXED

**Was:** the module prototypes were built out of `ExtraPlanets_Items.tier1RadiationLayer`, `.tier1PressureLayer` and
`.advancedBattery`, which are assigned **inside** `ExtraPlanets_Items.init()`. Reordering `ExtraPlanets.preInit`, or
disabling those items by config, produced `new ItemStack(null)` and every requirement became a null stack: the module
cost nothing in the GUI and could never be installed, because `ModuleHelper.installModule` cannot match a null item.

**Now:** requirements are declared, not built. `Module.setRequirementSupplier(...)` stores a lambda that
`getRequirements()` evaluates once, on first read — long after preInit. The three ExtraPlanets-item costs go through
`Module.requirementOf(Item, int)`, which checks the item *reference* (null unambiguously means "never assigned") and
returns an empty list when the item is disabled, instead of a stack that can never be satisfied. Resolution is
`synchronized` and swaps the list reference rather than mutating in place, because a single-player client and its
integrated server share these prototypes across two threads; `copy()` resolves, so a copy is always complete.

**Guarded by:** `ModuleRequirementOrderTest` (6 tests) — construction before the item exists followed by resolution
after it appears, all three ExtraPlanets-item modules, the disabled-item drop, one-shot evaluation, `setRequirements`
overriding a declaration, and `copy()` of an unresolved module; plus `ModuleHelperInstallTest.installingConsumesADeferredRequirement`
for the install/refund path. In game, `gameTestServer` asserts each of the three resolved to the *actual* registered
item, which is the part a headless suite cannot see (`ItemStack#getItem()` is null outside a loaded game).

### P2 — `ModuleHelper` casts `IModularArmor` to `ElectricArmorBase` without checking

```java
public static int getArmourStoredPower(ItemStack item) {
    if (!(item.getItem() instanceof IModularArmor)) return 0;
    return (int) ((ElectricArmorBase) item.getItem()).getElectricityStored(item);   // CCE for any other modular item
}
```
Same in `takeArmourPower`. `IModularArmor` is a marker interface, so any addon (or future non-electric suit) that
implements it gets a `ClassCastException` from a HUD render path. `ModuleHelperTest.hasPowerIsFalseForAnItemThatIsNotAnElectricArmor`
pins the current behaviour so a fix is a deliberate change.
**Recommendation:** `if (!(item.getItem() instanceof ElectricArmorBase)) return 0;`.

### P2 — Setting a biome id to `0` overwrites the vanilla ocean biome

`ExtraPlanets_Biomes.resolveBiomeID` deliberately skips the collision check when `configuredID == 0`, on the grounds
that "the ocean biome itself owns slot 0". But it then returns 0 and the factory constructs a biome with id 0, which
`BiomeGenBase`'s constructor writes into `biomeList[0]` — replacing the ocean for the rest of the session. Every ocean
in the world becomes an ExtraPlanets biome. The comment reads as if this were handled; it is not.
**Recommendation:** treat 0 as out-of-range (relocate to the first free slot and log the same "please change the
config" message) instead of exempting it — this is a change in behaviour, so it should be a deliberate decision.
`ExtraPlanets_BiomesTest` covers the in-range, out-of-range and collision cases but deliberately not this one.

### P2 — `EntityVehicleBase.setPositionAndRotation2` references a client-only class without a guard

```java
if (this.riddenByEntity instanceof EntityPlayer && FMLClientHandler.instance().getClient().thePlayer.equals(...))
```
`cpw.mods.fml.client.FMLClientHandler` does not exist on a dedicated server. The method is only *called* from the
client-side tracker, so it works today by accident; a single future call from common code turns this into a
`NoClassDefFoundError` on the server. `EntityPoweredVehicleBase` has the same shape.
**Recommendation:** guard with `this.worldObj.isRemote` (which is what GC's own vehicle code does) or move the
"is this the local player" test behind a proxy method.

### P2 — `getElectricityStored` mutates the stack it is asked to read

`ElectricArmorBase.getElectricityStored` is a getter that calls `itemStack.setItemDamage(...)`. It is called from HUD
rendering, from `isModuleActive` chains and from `hasPower` — i.e. several times per tick — and each call writes the
damage value, which on a `MultiItemStack` in an inventory marks the stack dirty and re-syncs it to the client. It is
also the source of the "damage bar flickers while charging" reports. Correct, but expensive and surprising.
**Recommendation:** rename the side effect into an explicit `syncDamageBar(ItemStack)` and call it only from
`setElectricity`.

### P2 — `PacketSimple`'s constructor prints a stack trace instead of failing

```java
if (packetType.getDecodeClasses().length != data.size()) {
    GCLog.info(...);
    new RuntimeException().printStackTrace();     // and then it continues with mismatched data
}
```
A mismatched payload is exactly the bug the packet type system exists to prevent; printing to stdout and carrying on
means the client decodes garbage. It should `GCLog.severe` and return without setting `type`, or throw.
**Recommendation:** make it a hard failure in dev (`Config.debugMode`) and a `severe` log plus empty packet in
production.

### P2 — Module slot-convention documentation is inverted in two places — ✅ FIXED

`Module`'s javadoc says "0 = feet, 1 = legs, 2 = chest, 3 = head", and `ExtraPlanets_Modules.init()` groups the
registrations under `// -- Helmet (slot 3) --`, `// -- Chestplate (slot 2) --`, `// -- Leggings (slot 1) --`,
`// -- Boots (slot 0) --`. Both are wrong: `ModuleHelper.checkSlotCompatibility` compares the slot type against
`ItemArmor.armorType`, where **0 = helmet, 1 = chestplate, 2 = leggings, 3 = boots**, and the constructors use that
convention correctly. The code is right and the docs are wrong — which is worse, because the next contributor will
"fix" the code. `ModuleRegistryTest.theSlotTypeOfEveryModuleMatchesTheArmourPieceItBelongsTo` now pins the real
convention.
**Fixed:** both comments now say `armorType` and the real values (helmet 0, chestplate 1, leggings 2, boots 3).

### P3 — Naming that has become part of the save format

* `ExtraPlanetsUtli` (typo for `Utils`).
* `ExtraPlanets_Blocks.veunsDungeonBrick` — and the matching `"tile.veunsDungeonBrick"` microblock key.
* `ExtraPlanets_Armor.spaceSuitLegings`, `carbonLegings`, … (typo for `Leggings`).
* `Config`'s category constants mix camelCase, spaces and `ID`/`Id` (`"dimensionID"`, `"items"`, `"schematics GUI ID"`).

These are cosmetic, but the field names appear in NBT/registry/translation keys, so they must **not** be renamed
without a migration. Worth a comment on each saying so.

### Verified already fixed

✅ Rover destruction threshold (`> 2` → `> 40`) in both `EntityVehicleBase` and `EntityPoweredVehicleBase`.
✅ Full-suit air requirement (`canBreathe`/`handleGearType` per piece).
✅ Biome id registry with collision detection, out-of-range protection and lazy registration.
✅ All 65 ore-dictionary furnace recipes go through the guarded `addOreSmelting`.
✅ `PacketSimple.decodeInto` bounds-checks the buffer and the type ordinal.
✅ `MainHandler` no longer allocates a `Random` per tick and is client-only; client handlers moved to
`MainHandlerClient`.
✅ Space-suit render state is per-entity (`WeakHashMap`) with `glPushAttrib`/`glPopAttrib` in `finally`.
✅ `getArmorTexture` can no longer return `null`.

Still open from that earlier list: `Config.jupiterLightingServer` is read but never used (§5.4).

---

## 5. Dead code inventory

Found by comparing every declared top-level type against all references in `src/main/java` (reflection-only and
registry-only uses were checked by hand).

### 5.1 Unreferenced classes (safe to delete)

| Class | Notes |
|---|---|
| `blocks/BlockCandyBlocks` | Candy-cane block set, never instantiated or registered. Textures are also unused. |
| `blocks/BlockCandyBlocksHorizontal` | Same. |
| `entities/monsters/EntityCustomSlime` | Never registered; `EntityEvolvedIceSlime`/`EntityEvolvedMagmaCube` are the ones that are used. |
| `client/model/monsters/ModelEvolvedPowerSkeleton` | Renderer for the above; never referenced. |
| `client/render/entities/monsters/RenderEvolvedIceBlaze` | No matching entity exists. |
| `client/render/projectiles/RenderEntitySmallSnowball` | `EntitySmallSnowball` uses a different renderer (or none). |
| `moons/Europa/SkyProviderEuropa` | `SkyProviderHandler` never sets it; Europa gets no custom sky. |
| `moons/Titan/SkyProviderTitan` | Same — Titan renders with the default sky. |
| `world/features/WorldGenDeshTower` | Never called from any chunk provider. |

Deleting 9 classes and their textures is ~150 KB and removes 9 files from every future code search.

### 5.2 Unreferenced near-copies

`GenLayer*BiomeStabilize` exists once per body but is only ever constructed for Io and Triton — the other five
(`Oberon`, `Titan`, `Jupiter`, `Neptune`, `Saturn`, `Uranus`, `Kepler22b`) are dead copies. Note the Io copy is
itself misnamed `GenLayerTritonBiomeStabilize` (it lives in the `Io/worldgen/biomes` package), which is a second,
independent smell.

### 5.3 The `migrate/` tree (already untracked)

`migrate/` holds a 1.12.2 port plus a vendored copy of `micdoodle8/mods/galacticraft` and `mjrlegendslib`. It is
already listed in `.gitignore`, so it is not in the repository — but it is still in every working copy that has ever
built this mod, where it makes IDE searches, `grep` and file-name completion slower and shows up as "unused" code in
static analysis. Delete it locally; nothing references it.

### 5.4 Dead configuration

* `Config.jupiterLightingServer` — read from the config file, never used. Either implement it (there is a plan in
  `docs/JUPITER_LIGHTING_PLAN.md`) or delete the key.
* `Config.morePlanetsCompatibility`, `…Adv143`, `…New` — three overlapping flags that all disable a similar set of
  bodies, with subtly different which-bodies lists in `checkCompatibility()`. `morePlanetsCompatibilityAdv143`
  additionally disables the rocket *registration*, which is the only difference most users will notice. Collapse to
  one flag plus a "which bodies" list.
* `Config.pressureRadiationSettings` and friends are read but several are only consulted from the overlays; the
  server-side effect is missing (see §7).

---

## 6. Optimizations and refactors

### 6.1 Correctness / performance

1. **`Config` sync no longer uses reflection** (§4/P1, done). `SYNC_ENTRIES` holds a typed getter and setter per
   field, and the wire format is `(name, value)` pairs, so there are no string lookups and no positional mapping.
   A future `Config` facade should read and write through the same table.
2. **`ElectricArmorBase.getElectricityStored`** writes the damage bar on every read (§4). Split the read from the
   write.
3. **`ExtraPlanets_MicroBlocks` is ~430 lines of copy-paste reflection.** It is one `register(...)` call per block
   metadata value, eight times per body, 15 bodies. A single `Object[][] { {block, meta, "tile.name"} }` table plus a
   loop would shrink it by ~90 % and make a new planet a one-line change. It also currently swallows every failure
   (§6.2), so a typo in a tile name is invisible.
4. **`MainHandler` fog/thermal handlers** recompute `world.provider instanceof ...` chains every frame. Caching the
   "is this an ExtraPlanets body, and which" answer per dimension id would remove a handful of `instanceof` per frame
   on the client — small, but free.
5. **`ExtraPlanets_Recipes` is 172 KB in one class.** Splitting it per body (the rocket recipes already are) would make
   it navigable and would let a per-body recipe class be loaded only when that body is enabled.

### 6.2 Logging hygiene

* **11 empty `catch` blocks** and 44 `catch (Throwable t)` sites, mostly in worldgen static initialisers. A failure
  there means "this structure silently never generates".
* **7 `System.out`/`System.err`** call sites in production code (`EntityCustomBlueCreeper:285`,
  `Kepler22bDungeons:177`, `CustomCelestialSelection:669`, `MainHandler:519`, `PacketSimple:123`).
* **7 `printStackTrace()`** call sites.

`GCLog` already exists and is used correctly elsewhere; routing all of the above through
`GCLog.severe`/`GCLog.info` — and gating the verbose ones behind a `Config.debugMode` — costs a day and makes the two
locales where these bugs actually bite (startup and worldgen) diagnosable from a log file.

### 6.3 The worldgen copy-paste (the big one)

| Pattern | Copies | Size each |
|---|---|---|
| `structureComponent*VillageHouse.java` | **22** | ~59 KB |
| `structureComponent*VillageWoodHut.java` | **22** | ~59 KB |
| `MapGen*Village` / `WorldGen*Village` | 22 | ~10 KB |
| `GenLayer*Biome*` | 15 bodies × 5 layers | ~2 KB |

That is ~5 MB of the 9.4 MB source tree, and every one of the 44 structure files is the *same* class with a different
package name and a different block palette. Concretely: a bug fixed in `StructureComponentJupiterVillageHouse` has to
be fixed 21 more times, and has been missed at least once (the Io/Triton `GenLayer` misnaming in §5.2 is the same
failure mode).

**Recommended refactor (staged):**

1. Extract the palette (the `Block`/metadata pairs and the loot list) into a small `VillagePalette` object.
2. Turn the two structure classes into single parameterised classes that take a `VillagePalette` and a `VillageTheme`
   (wall material, roof material, door, window).
3. Replace the 44 classes with 15 one-line subclasses (or 15 registry entries), keeping the package paths for now so
   nothing else has to change.
4. Only then collapse the 15 subclasses into configuration.

This is the single largest maintainability win available and the one item on this list that will keep paying off.

### 6.4 Build / repo hygiene

* `gradle.properties` has `autoUpdateBuildScript = true`, which rewrites the build script on every run for everyone.
  For a fork that is actively diverging (this one has an `addon.gradle.kts` now) that is a constant merge hazard.
  Consider `false` plus a scheduled `updateBuildScript` job.
* `.github/workflows/build-and-test.yml` runs `build-and-test` from `GTNH-Actions-Workflows`, which does not know
  about `gameTestServer`. Adding one `./gradlew gameTestServer` step (with an `eula.txt` pre-created) would put the
  in-game suite in CI.
* `jitpack.yml` still calls `setupCIWorkspace25`; the project does not use lwjgl3ify, so plain
  `./gradlew setupCIWorkspace build` is enough and avoids downloading a modern-JDK launcher.

---

## 7. Feature ideas

Ordered by (value ÷ effort). None of these are implemented in this pass.

1. **Suit module tiers for the existing modules.** `Module` already carries a `subType`, and
   `ModuleRadiationShield`/`ModulePressureSeal` already use it as a tier. Night vision, protection+ and the solar
   panels could use the same tier mechanism instead of registering `_t1`/`_t2` variants as separate modules — fewer
   registry entries, one GUI row per module type, and the tier becomes part of the existing NBT format.
2. **A `/ep` command that reports the resolved ids.** `CommandEP` exists; a `dimension`/`biome` sub-command printing
   the effective (post-sync) ids and whether each one collides with another mod would turn the current
   "biome ID conflict" log line into something a pack author can act on in game.
3. **Server-side pressure/radiation.** The overlays and the suit modules model both, but the effects (damage over
   time, suit drain) are per-tick server work that is only partially wired. Finishing them makes the radiation and
   pressure layers craftable-for-a-reason, and the module tiers in (1) meaningful.
4. **The jetpack as a real flight controller.** `ModuleJetpack` currently only deducts power via a keybind on the
   client and does not apply thrust server-side, which is exploitable (the client can claim to be thrusting for
   free). Moving the thrust application into `tickServer` and validating `hasPower` there closes the hole and makes
   the T2 jetpack's higher max acceleration meaningful.
5. **Schematic pages for the rovers 2.0 / rocket 11.** The schematic id plumbing (`Config.SYNC_ENTRIES`,
   `SchematicRegistry`, `SchematicPage` config ids) is fully generic; adding a page is currently a 6-file change that
   could be a 1-file change with a small builder.
6. **In-game coverage in CI.** See §8.5 — the machinery is in place, only the class-dump flag is missing.

---

## 8. What cannot be tested without the game

Recorded here so the next person does not rediscover it. Each item was verified experimentally in this pass.

### 8.1 `GameRegistry` requires the FML `Loader`

`GameRegistry.registerBlock/registerItem/registerTileEntity` reach `Loader.instance()`, which constructs a
`ModClassLoader` with a hard cast to `net.minecraft.launchwrapper.LaunchClassLoader`. In a plain JUnit JVM the
application class loader is not a `LaunchClassLoader`, so the call throws
`ClassCastException: sun.misc.Launcher$AppClassLoader cannot be cast to net.minecraft.launchwrapper.LaunchClassLoader`.
Consequence: `ExtraPlanets_Blocks.init()`, `ExtraPlanets_Items.init()`, `ExtraPlanets_Recipes.init()` and every
registration class are **not** unit-testable. This is what the `gameTest` source set is for.

### 8.2 `TileEntity.writeToNBT` requires the tile entity mapping

`TileEntity.writeToNBT` looks the class up in FML's `nameToClassMap` and throws
`"class ... is missing a mapping! This is a bug!"` outside a loaded game. Tile-entity NBT round trips therefore live
in the game test, not the unit suite.

### 8.3 `ItemStack#getItem()` is `null` for early-registered items in a plain JVM

`Item` assigns a `RegistryDelegate` in its constructor from `GameData.getItemRegistry()`. For items created before
FML has populated the registry, the delegate is `null` and `ItemStack#getItem()` (which reads `delegate.get()`) returns
`null` even for vanilla items.

This has a second, sharper consequence that is easy to miss: `ItemStack#isItemEqual` compares delegates, so **in a
plain JVM two stacks of completely different items compare equal**. A headless test that asserts "a dirt block does not
satisfy a diamond requirement" fails for the wrong reason. `ModuleHelperInstallTest.consumingPrefersTheMatchingStack`
documents this in place; the positive case is tested headless and the negative case is tested in game.

### 8.4 `FluidRegistry` and `Forge Configuration` need FML injections

`FluidRegistry`'s static initialiser touches the FML side, and `net.minecraftforge.common.config.Configuration` reads
`FMLInjectionData.data()[6]` (the Minecraft home directory), which is `null` outside a launch and throws an NPE.
`FmlTestEnvironment` stubs the injection data so `Config.init()` can run headless; fluid-registry-dependent code
cannot (fluids must be created in the game test).

### 8.5 In-game coverage

The `gameTestServer` run writes `build/jacoco/gameTest.exec` with the agent restricted to `com.mjr.extraplanets.*`, and
`jacocoGameTestReport` reports it. That report is **empty today** because it needs RFB's post-transform class dump
under `<run dir>/RFB_CLASS_DUMP/Launch`, and RFB only writes it when class dumping is explicitly enabled. Reason: in a
1.7.10 dev launch the `LaunchClassLoader` rewrites every class through ASM, so the class ids in the exec file do not
match the class files on disk; reporting against the on-disk classes produces only
"Classes in bundle do not match with execution data" warnings. Enabling RFB's dump flag for gameTest runs (or
pointing `classDirectories` at a production jar) is the remaining step.

---

## 9. Priority plan

| Order | Item | Effort | Risk |
|---|---|---|---|
| 1 | `ModuleHelper` blind cast to `ElectricArmorBase` (§4 P2) | 10 lines | none |
| 2 | Guard the rover's client-only call with `isWorldRemote` (§4 P2) | 5 lines | none |
| 3 | **Delete the 9 unreferenced classes** (§5.1) | mechanical | none |
| 4 | Route `System.out`/`printStackTrace` through `GCLog`, add `debugMode` gating (§6.2) | 1 day | low |
| 5 | Split `getElectricityStored`'s read from its write (§4 P2) | 1 hour | low (touches the HUD) |
| 6 | Decide on biome id `0` (§4 P2) | 30 min + a decision | medium — behaviour change |
| 7 | `Config` instance facade (§4 P1) | 2-3 days | medium |
| 8 | Refactor the 44 worldgen copies (§6.3) | 1 week | medium, mechanical |
| 9 | Delete / implement the dead config keys (§5.4) | depends | low |
| 10 | Add `gameTestServer` + `smokeTest` to CI (§6.4) | 1 hour | none |
| 11 | Delete the local `migrate/` tree (§5.3) | 1 min | none |
| ✅ | Make `ExtraPlanets_Modules.init()` order-independent (§4 P1) | 2 hours | medium |
| ✅ | Name/value config sync payload with a compile-checked accessor table (§4 P1, §6.1) | 4 hours | medium |
| ✅ | Correct the inverted slot-convention docs (§4 P2) | 20 lines | none |

The test suite that now guards changes is the more valuable artefact than any single fix above: `./gradlew check` fails
on a config-id collision, a broken sync contract, a module-registry duplicate, a packet-codec regression, and a
coverage drop, and `./gradlew gameTestServer` fails on any registration, ore-dictionary, recipe or tile-entity
regression — none of which were guarded before this pass.
