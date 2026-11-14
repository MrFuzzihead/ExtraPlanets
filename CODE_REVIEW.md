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
| Tests | 130 headless JUnit 4 tests in `src/test/java` (was 0) |
| In-game tests | New never-shipped test mod in `src/gameTest/java`, 40 checks, run by `./gradlew gameTestServer` |
| Production code | `ExtraPlanets_Modules.init()` is now idempotent; `Module.copy()` no longer shares the prototype's requirement list; new `SmokeTestHook` + `ExtraPlanets.loadComplete` |
| Docs | `README.md` rewritten (badges, requirements, build/test commands), this file added |

Measured coverage of the unit suite (`jacocoTestReport`, generated classes excluded):
**2.73 % of 63,418 lines, 0.81 % of 28,438 branches**. That is expected for a mod that is 5 MB of worldgen and
rendering; the ratchet is set just below it so it can only go up.

All four commands pass on a clean checkout:

```
./gradlew test               # 130 tests, 0 failures
./gradlew check              # + spotless, checkstyle, coverage floor, gameTest compile
./gradlew gameTestServer     # 40/40 in-game checks, EXTRAPLANETS_GAMETEST_OK
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
  `config/ExtraPlanets.cfg` relative to the working directory) does not pollute the checkout. The project directory is
  passed to the tests as `-Dextraplanets.projectDir` so `NoStrayTracesTest` can still lint `src/main/java`.
* `jacocoTestReport` (HTML + XML) and `jacocoTestCoverageVerification` with a floor just under the measured baseline.
  `check` depends on the verification, so CI fails on a coverage regression.
* `jacocoGameTestReport` reports the in-game run against the class files JaCoCo actually saw. It self-skips when the
  data is absent (see §8.6).
* A `gameTest` source set wired into `runServer`/`runClient` when `-PgameTest` or `gameTestServer` is requested, with
  `fml.queryResult=confirm` pre-answered.
* `-PsmokeTest` sets `extraplanets.smokeTest`, which `SmokeTestHook` turns into a clean exit at world load.

`src/test/java` (91 tests):

| Class | Covers |
|---|---|
| `ConfigDefaultsTest` (9) | The real `config/ExtraPlanets.cfg` defaults: planet/station dimension ids unique, 49 biome ids unique **and inside 0-255**, schematic GUI/page ids unique, `Config.init()` idempotent |
| `ConfigSyncContractTest` (18) | The server→client config sync: every synced name resolves to a real `public static int`, no name is synced twice, every `...ID` field is covered, each field round-trips through its own getter **and** setter, pair order is irrelevant, a missing/unknown/non-numeric pair affects nothing, a legacy positional payload is ignored, null/empty/odd-length packets change nothing, and save/restore around a remote session |
| `ExtraPlanets_BiomesTest` (12) | In-range ids are kept verbatim, out-of-range ids are relocated into the array, id `0` is relocated instead of replacing the vanilla ocean, a collision with another mod's biome is reported but **not** remapped, name lookups are cached |
| `ModuleHelperInstallTest` (15) | `installModule`/`uninstallModule`/`consumeFromInventory`: wrong-slot and duplicate rejection, requirement consumption and refund (including a lazily declared cost), drop-when-inventory-full |
| `ModuleHelperTest` (27) | The suit-module NBT format: setup, add/replace/remove, active flag, subtype, unknown names, wrong tag types, copies-not-prototypes, slot compatibility, and that a modular-but-not-electric suit reads 0 power instead of throwing |
| `ModuleRegistryTest` (12) | `init()` idempotency, 17 modules, unique lower_snake_case names, slot types match the `ModuleHelper`/`ItemArmor.armorType` convention per piece, every module has an icon and requirements, copies do not share lists |
| `ModuleRequirementOrderTest` (6) | Requirements resolve after the item they reference is registered, a config-disabled item drops its requirement instead of keeping a null stack, a declaration is evaluated once, `setRequirements` overrides a declaration, `copy()` of an unresolved module resolves |
| `PacketSimpleCodecTest` (13) | Real codec round trip for every packet type and for the real config payload, truncated/out-of-range/negative ordinals are discarded instead of throwing, and a payload of the wrong shape is refused rather than sent (loudly in `debugMode`) |
| `NoStrayTracesTest` (2) | Lints `src/main/java`: no `printStackTrace()`, no `System.out`/`System.err` outside the one documented CI marker, with `file:line` for each offence |
| `SkyProviderWiringTest` (2) | Parses `SkyProviderHandler` and requires `WorldProviderX` -> `SkyProviderX` for every body (orbits excepted) and a branch for every provider on disk |
| `StructureWiringTest` (2) | Every class in `world/features` must be called by real code somewhere; imports and comments do not count |
| `MachineTileEntityTest` (4) | Every machine constructs without a world, refinery tanks are per-instance, `readFromNBT` tolerates an empty tag |

`src/gameTest/java` (40 checks, incl. 396 village + 63 microblock + 100 recipe goldens, `./gradlew gameTestServer`):

| Group | Checks |
|---|---|
| config | biome ids inside the array, dimension ids negative, config file created, the sync payload is name/value pairs, the payload survives the real packet codec, replaying it over a scribbled config restores every id |
| blocks | mercury block set registered in the right namespace, every body block has a name, machine blocks registered |
| items | rocket line registered, space suit pieces registered, item ids round trip through `Item.getItemById` |
| tile entities | refinery survives `writeToNBT` → `TileEntity.createAndLoadEntity`, tank capacities preserved, solar panel round trips |
| ore dictionary | planet ores and blocks registered, every entry points at a real stack |
| recipes | crafting recipes exist, planet ore blocks smelt into the matching ore-dictionary ingot, the mod's own ore smelts into its ingot, plus a golden of this mod's crafting list **in order** and of its furnace recipes |
| modules | registry populated, every requirement resolves to a real item **in game**, the three ExtraPlanets-item requirements resolved to the actual registered items, install/uninstall round trip, a fresh suit has no modules |
| armor | reading the charge never writes the damage bar, charging does, a read never invents an NBT tag, a stale bar can be repaired (cannot run headless - see §8) |
| microblocks | every registered microblock material resolves back to its block and metadata in ForgeMultipart, and the whole registered set matches its golden |
| worldgen | the Tower of Desh still generates (run against the live world, frame/desh/mast asserted), all nine bodies' biome layer chains return registered biomes/more than one biome/never the ocean fallback, and **396 golden village snapshots**: every block-placing component per body in both coordinate branches, the piece dispatch per body for four fixed villages, and the spawn-grid set per body |

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

### P2 — `ModuleHelper` casts `IModularArmor` to `ElectricArmorBase` without checking — ✅ FIXED

```java
public static int getArmourStoredPower(ItemStack item) {
    if (!(item.getItem() instanceof IModularArmor)) return 0;
    return (int) ((ElectricArmorBase) item.getItem()).getElectricityStored(item);   // CCE for any other modular item
}
```
Same in `takeArmourPower`. `IModularArmor` is a marker interface, so any addon (or future non-electric suit) that
implements it gets a `ClassCastException` from a HUD render path.
**Fixed:** both helpers check `instanceof ElectricArmorBase` and treat anything else as "no power".
`ModuleHelperTest` now pins that a modular-but-not-electric suit reads 0, reports `hasPower` false and survives
`takeArmourPower` (the old test pinned the `ClassCastException`, which is why the fix had to be deliberate).

### P2 — Setting a biome id to `0` overwrites the vanilla ocean biome — ✅ FIXED

`ExtraPlanets_Biomes.resolveBiomeID` deliberately skips the collision check when `configuredID == 0`, on the grounds
that "the ocean biome itself owns slot 0". But it then returns 0 and the factory constructs a biome with id 0, which
`BiomeGenBase`'s constructor writes into `biomeList[0]` — replacing the ocean for the rest of the session. Every ocean
in the world becomes an ExtraPlanets biome. The comment reads as if this were handled; it is not.
**Fixed:** id 0 is now relocated to the first free slot with a dedicated message. It is deliberately *not* treated
as out of range, because the valid upper bound must stay whatever `BiomeGenBase.getBiomeGenArray().length` reports
(see the ID policy below). `findFreeBiomeID()` also no longer falls back to slot 0 when the array is full - it
throws instead, because replacing the ocean is worse than failing to register one biome.
`ExtraPlanets_BiomesTest` covers both new cases (`idZeroIsRelocatedInsteadOfReplacingTheOcean`,
`aRelocatedIdIsNeverZero`).

#### ID guards must not cap IDs (EndlessIDs)

Several modpacks now run `EndlessIDs`, which patches the biome array (and the block/item registries) to thousands of
slots. Any guard that hardcodes 255/4096 is therefore wrong twice over: it rejects IDs that are perfectly valid on an
extended instance, and it turns a working config into a relocated one. The rules this codebase follows:

* Always read the bound from the live registry (`BiomeGenBase.getBiomeGenArray().length`,
  `Block.blockRegistry`/`Item.itemRegistry` sizes), never a literal.
* Relocate only what would corrupt the world - an out-of-range ID, or an ID owned by a vanilla biome that must not
  be replaced (0 = ocean) - and always log the value the user should change.
* Never silently remap an in-range value, even on a collision: the ID is already written into every generated
  chunk, so changing it corrupts existing saves.
* `ConfigDefaultsTest.biomeIdsFitTheVanillaBiomeArray` asserts only that the *shipped defaults* fit vanilla's 256,
  i.e. that a player without an extender gets a working mod. It is not a cap on what a pack may configure.

### P2 — `EntityVehicleBase.setPositionAndRotation2` references a client-only class without a guard — ✅ FIXED

```java
if (this.riddenByEntity instanceof EntityPlayer && FMLClientHandler.instance().getClient().thePlayer.equals(...))
```
`cpw.mods.fml.client.FMLClientHandler` does not exist on a dedicated server. The method is only *called* from the
client-side tracker, so it works today by accident; a single future call from common code turns this into a
`NoClassDefFoundError` on the server. `EntityPoweredVehicleBase` has the same shape.
**Fixed:** both methods return immediately unless `worldObj != null && worldObj.isRemote`. The only caller really is
`NetHandlerPlayClient` (verified against the decompiled source), so this removes the latent crash without changing
behaviour.

**The same pattern elsewhere, surveyed:** `TileEntitySolar`, `TileEntityPoweredChargingPad`,
`TileEntityTier2/3LandingPad` and `KuiperBeltEvents` already guard their `FMLClientHandler` use with
`worldObj.isRemote` or `@SideOnly(CLIENT)` and are fine. Two families are still unguarded, both inside
`addInformation` tooltips, which only ever run client-side: `SpaceSuitArmor` (reads the sneak-key binding via
`FMLClientHandler.instance().getClient().gameSettings`) and the seven `Tier4..Tier10Rocket` item classes (builds a
throw-away rocket entity in `FMLClientHandler.instance().getWorldClient()` to read its fuel NBT). Both are latent in
exactly the way the vehicle code was. The clean fix is to route them through `CommonProxy` (a
`getClientWorld()`/`getKeyDisplayString()` seam) rather than sprinkling more side checks, so they are scheduled
together as one item below.

### P2 — `getElectricityStored` mutates the stack it is asked to read — ✅ FIXED

`ElectricArmorBase.getElectricityStored` is a getter that calls `itemStack.setItemDamage(...)`. It is called from HUD
rendering, from `isModuleActive` chains and from `hasPower` — i.e. several times per tick — and each call writes the
damage value, which on a `MultiItemStack` in an inventory marks the stack dirty and re-syncs it to the client. It is
also the source of the "damage bar flickers while charging" reports. Correct, but expensive and surprising.
**Fixed:** the getter is pure - no damage write, and it no longer creates an empty NBT tag on read either - and the
write moved into `syncDamageBar(ItemStack)`, which `setElectricity` calls and which skips the write when the bar is
already correct. Covered by four in-game checks (`armor:` group in `gameTestServer`): reads never move the bar,
charging does, a read never invents a tag, and a stale bar can be repaired. A headless test is not possible -
`ElectricArmorBase`'s constructor asks Galacticraft whether IC2 is loaded, which reaches into the launch classloader
and throws `ClassCastException: AppClassLoader cannot be cast to LaunchClassLoader` in a plain JVM (recorded in §8).

### P2 — `PacketSimple`'s constructor prints a stack trace instead of failing — ✅ FIXED

```java
if (packetType.getDecodeClasses().length != data.size()) {
    GCLog.info(...);
    new RuntimeException().printStackTrace();     // and then it continues with mismatched data
}
```
A mismatched payload is exactly the bug the packet type system exists to prevent; printing to stdout and carrying on
means the client decodes garbage.
**Fixed:** with `Config.debugMode` on it throws `IllegalArgumentException` (in development a mismatch is always a
coding error); otherwise it logs `severe` and leaves `type`/`data` null, and `encodeInto` refuses to write a packet
that was never built. The two other `printStackTrace()` calls in the class, and the `System.err` in `decodeInto`,
now log the throwable through `ExtraPlanetsLog` - see §6.2.

### P2 — Module slot-convention documentation is inverted in two places — ✅ FIXED

`Module`'s javadoc says "0 = feet, 1 = legs, 2 = chest, 3 = head", and `ExtraPlanets_Modules.init()` groups the
registrations under `// -- Helmet (slot 3) --`, `// -- Chestplate (slot 2) --`, `// -- Leggings (slot 1) --`,
`// -- Boots (slot 0) --`. Both are wrong: `ModuleHelper.checkSlotCompatibility` compares the slot type against
`ItemArmor.armorType`, where **0 = helmet, 1 = chestplate, 2 = leggings, 3 = boots**, and the constructors use that
convention correctly. The code is right and the docs are wrong — which is worse, because the next contributor will
"fix" the code. `ModuleRegistryTest.theSlotTypeOfEveryModuleMatchesTheArmourPieceItBelongsTo` now pins the real
convention.
**Fixed:** both comments now say `armorType` and the real values (helmet 0, chestplate 1, leggings 2, boots 3).

### P2 — Europa and Titan rendered another body's sky — ✅ FIXED

`SkyProviderHandler` is a chain of `world.provider instanceof WorldProviderX` blocks, and two of them constructed the
wrong renderer: the Europa branch built `SkyProviderGanymede` and the Titan branch built `SkyProviderEris`. So Titan
showed Pluto, Dysnomia and Earth, and the correct `SkyProviderTitan` (saturn.png) and `SkyProviderEuropa` files sat
unused in their own packages. Nothing could catch it at runtime - the code compiles, runs, and simply draws the wrong
planets - and nothing did.
**Fixed:** both branches now construct their own body's renderer. `SkyProviderWiringTest` parses the handler and
enforces the identity mapping, so a future copy-paste fails the build.

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

### 5.1 Unreferenced classes - ✅ CLEANED UP

Each candidate was checked against git history before being deleted, which changed four of the nine verdicts.
"What nothing references" and "what nothing intended to reference" are not the same thing.

**Wired up instead of deleted (3):**

| Class | What it actually was |
|---|---|
| `moons/Titan/SkyProviderTitan` | A complete, *distinct* Titan sky (`saturn.png`, the vanilla sun, a larger disc, no Earth). `SkyProviderHandler` had a copy-paste bug and constructed `SkyProviderEris` in the Titan branch, so Titan rendered **Pluto, Dysnomia and Earth**. Now wired. |
| `moons/Europa/SkyProviderEuropa` | A byte-identical copy of `SkyProviderGanymede` (both render `jupiter.png`, as two Jovian moons should). The Europa branch constructed `SkyProviderGanymede` directly. Now wired, so each branch names its own body's renderer - see the guard below. |
| `world/features/WorldGenDeshTower` | **A regression, not dead code.** `git log -S` shows it was added to `BiomeDecoratorCeres` in c1192dc ("Added a Custom Structure of Tower of Desh to Ceres") and lost when that decorator was rewritten. Restored at the original 1-in-150-chunks rate. |

**Deleted (8 classes):**

| Class | Why it was safe |
|---|---|
| `blocks/BlockCandyBlocks`, `blocks/BlockCandyBlocksHorizontal` | Never instantiated. They were also already broken: they register `candyCaneRed` (camelCase) while the assets are `candy_cane_red.png`, so the icons would never have resolved. Removed with their 40 textures (728 KB) and 479 dead `lang` lines (`tile.candy_blocks.*`, `tile.cake_blocks.*`, and the orphaned cake/icing keys, which never had classes either). |
| `entities/monsters/EntityCustomSlime` | Never registered or spawned; `EntityEvolvedIceSlime`/`EntityEvolvedMagmaCube` are the ones in use. |
| `client/model/monsters/ModelEvolvedPowerSkeleton` | A stale duplicate: the registered `RenderEvolvedPowerSkeleton` uses Galacticraft's `ModelEvolvedSkeleton`. |
| `entities/monsters/EntityEvolvedIceBlaze`, `client/model/monsters/ModelIceBlaze`, `client/render/entities/monsters/RenderEvolvedIceBlaze` | A whole ice-blaze that was never finished: the entity is registered nowhere, spawned by no chunk provider and gated by no config flag, and the model and renderer exist only to serve it. (The earlier note "no matching entity exists" was wrong - the entity file is there, it is just as unreachable as its renderer.) |
| `client/render/projectiles/RenderEntitySmallSnowball` | Superseded: `ClientProxy` registers `EntitySmallSnowball` with vanilla's `RenderSnowball(Items.snowball)`. |

Net: **8 classes and 40 textures (728 KB) deleted, 3 classes given the call sites they were written for, ~480 dead
`lang` lines removed.** Two guards keep it that way:

* `SkyProviderWiringTest` parses `SkyProviderHandler` and requires the identity mapping `WorldProviderX` ->
  `SkyProviderX` (orbit providers excepted, they legitimately share `SkyProviderOrbit`). It reports
  `WorldProviderTitan -> SkyProviderEris` if the copy-paste is reintroduced.
* `StructureWiringTest` requires every class in `world/features` to be referenced by real code somewhere (imports and
  comments do not count), so a generator can no longer be left behind by a decorator rewrite.

### 5.2 Unreferenced near-copies - ✅ CLEANED UP

`GenLayer*BiomeStabilize` exists once per body, and **all nine were dead** - not the seven this section originally
claimed. `git grep` at every commit that ever touched those files (eight of them, from the "Converted X over to multi
biome system" series onwards) finds no reference from any `WorldChunkManager*` or `GenLayer*`: they have been
unreferenced since the day they were written, unlike the Tower of Desh, which was live and then lost.

They are also all the *same* class: byte-identical apart from the package and class name, one 38-line implementation
of the standard "snap biome cells to 4-block boundaries" layer. The copy in `Io/worldgen/biomes` was even named
`GenLayerTritonBiomeStabilize`. **All nine deleted** (~400 lines of duplication), with the exact change recorded here
instead of applied, because wiring them changes worldgen:

* Each body's `GenLayerX.makeTheWorld` builds `GenLayerXBiomes` -> six `GenLayerZoom` -> `GenLayerVoronoiZoom`, and
  the layer belongs on the line *before* the voronoi zoom:
  `biomes = new GenLayerXBiomeStabilize(1000L, biomes);`
* That is a visible change - biomes become square, 4-block aligned instead of voronoi polygons - and therefore a
  change in biome layout, surface blocks and mob spawns for newly generated chunks, with the usual seam where new
  chunks meet already-generated ones in existing worlds. It has never been enabled in any release, so it is a
  decision to make deliberately, not a regression to repair. If it is wanted, it is now one shared class plus one line
  per chunk manager, and the guard below covers it.

**New guard** (in-game, `worldgen:` group): every one of the nine bodies' chains is asked for a 256x256 window and
must return only registered biome ids, at least two distinct biomes (a degenerate chain would mean "this planet is one
biome"), and never the vanilla ocean fallback. The chains are pure functions of the seed - they read no world - so
this needs no dimension loading, and it is the only place they can be checked at all, because the biome objects they
return only exist once the game has registered them.

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

### 6.1 Correctness / performance — ✅ DONE

1. **`Config` sync no longer uses reflection** (§4/P1, done). `SYNC_ENTRIES` holds a typed getter and setter per
   field, and the wire format is `(name, value)` pairs, so there are no string lookups and no positional mapping.
   A future `Config` facade should read and write through the same table.
2. **`ElectricArmorBase.getElectricityStored`** writes the damage bar on every read (§4). Split the read from the
   write.
3. **`ExtraPlanets_MicroBlocks` was 369 lines of copy-paste reflection** — 63 reflective registrations, eight lines
   each, inside one bare `catch (Exception e) {}`. **Done:** one `register(...)` call per run of contiguous metadata
   (35 of them) driven by a name list, so the file is 189 lines and a new body is one line. Failures are logged
   per material instead of swallowed, and the absence of ForgeMultipart is a debug line rather than a silent catch.
   The table was generated mechanically from the pre-refactor file (63 parsed calls in, 63 registrations out), and the
   in-game suite now checks that every registered material resolves *back* to the block and metadata it was registered
   for, plus a golden of the whole set (mutation-tested with a tile-name typo).
   **Found while doing it:** `uranusBlocks` metadata 0 is registered **twice**, once correctly as
   `tile.uranus.surface` and once as `tile.uranusBlocks` (not a valid translation key). Left exactly as it is:
   ForgeMultipart assigns material ids in registration order, so deleting the stray entry would renumber every
   material registered after it and break microblocks in existing worlds. Worth removing only alongside a version bump
   that accepts that.
4. **`MainHandler`'s per-tick `instanceof` chains. Done:** `WorldHazards` answers "where does this world's pressure /
   radiation come from" from the provider *type*, memoised per class, and the level itself is still read from `Config`
   every call (the server syncs those at runtime, so the value must not be cached). Taking the class instead of the
   instance is also what makes it testable: `SomeProvider.class` loads a type without initialising it, so
   `WorldHazardsTest` covers all six cases headless.
5. **`ExtraPlanets_Recipes` was 2,844 lines in one class. Done**, split into one class per registration pass
   (`RecipesFurnace`, `RecipesCrafting`, `RecipesSpaceSuit`, `RecipesLayers`, `RecipesCompressor`,
   `RecipesCircuitFabricator`, `RecipesSatellites`), leaving `ExtraPlanets_Recipes.init()` as the ordered call list.
   Deliberately **not** split per body, which is what this section originally suggested: crafting recipes are matched
   by *position* in the list, and grouping by body instead of by pass would reorder them, which is a behaviour change
   (a dropped or moved recipe lets an earlier vague recipe match something it never used to). The split is covered by
   a new golden of the recipe list - 12 fingerprints over this mod's crafting recipes in order plus 87 furnace
   entries - which is what proves the move was inert. `RecipesCrafting` is still 2,122 lines; splitting *that* per
   body is order-safe (bodies appear in sequence) and is a follow-up if wanted.
   **Found while doing it:** 7 of this mod's crafting recipes produce an `ItemStack` whose item is not registered, so
   they can never be crafted, and `getItemDamage()` on one of those stacks throws (it goes through the registry
   delegate). The test counts them and prints the total.

### 6.2 Logging hygiene — ✅ DONE

`GCLog` has no `(String, Throwable)` overload, which is why every `catch` block in this mod called
`e.printStackTrace()`: it writes to the console, so the trace never reaches `logs/fml-*.log` and a user crash report
contains no evidence of what happened. `com.mjr.extraplanets.util.ExtraPlanetsLog` now provides
`info/warn/severe/debug`, with throwable overloads, on the same Forge relaunch-log sink under this mod's own name.

* **7 `printStackTrace()` sites** — all replaced with `ExtraPlanetsLog.severe(...)`/`warn(...)` plus the throwable
  (`CustomCelestialSelection`, `EntityVehicleBase`, `ExtraPlanetsChannelHandler` ×2, `PacketSimple` ×3).
* **`System.out`/`System.err` in production** — the two leftover debug prints in `EntityCustomBlueCreeper`'s AI are
  gone, `MainHandler`'s sealer diagnostics and `Kepler22bDungeons`' spawner warning go through the logger
  (`debug`/`warn`), and `CustomCelestialSelection`'s render guard logs the throwable. The one remaining stdout write is
  `SmokeTestHook`'s marker, which is a deliberate CI contract.
* **`NoStrayTracesTest`** fails the build if either habit returns, with `file:line` for each offence. Verified by
  adding a violation and watching it fail.
* **Empty `catch` blocks are gone** (they are how a failure becomes invisible: the mod loads, the feature silently
  does nothing, and there is no log line to search for). The last four were `CustomCelestialSelection`'s two
  per-frame reads (they now log once per session through `logRenderInfoFailure`, because logging per frame would bury
  the log), `RenderEvolvedPowerSkeleton`'s Battlegear2 probe (an absent mod is a debug line, a *changed API* is an
  error - the two used to be the same empty block), and two `GuiModuleManager` reflective lookups that degraded a
  ModularUI2 feature silently.
* **The 44 `catch (Throwable)` sites are all in the village registration blocks** and all log now (steps 0-6 of §6.3
  converted them). `SmokeTestHook`'s one is documented and deliberate: outside FML a throw is the expected answer.
* **`NoStrayTracesTest` now also fails the build on an empty `catch` block**, with `file:line`, after stripping
  comments (javadoc legitimately quotes the pattern it forbids). Verified by planting an empty catch and watching it
  name the line.

### 6.3 The worldgen copy-paste (the big one) — DONE (80% removed; Kepler22b's two variant buildings kept)

**Measured:** 308 files under `**/village/`, **131,287 lines**, for 22 bodies x 14 classes. Their shape is now known
exactly:

* Per class type there are only 1-3 *distinct implementations*; the rest differ solely by package and class name.
  `StructureVillageStart*` is one variant across all 22 bodies.
* `StructureComponent<Body>VillageHouse` and `...VillageWoodHut` were **the same 2,152-line build code in 21 of the 22
  bodies** - the "house" and the "wood hut" are the same 17x9x17 building. Only Kepler22b differs (498 lines: its own
  metadata scheme).
* The per-body component bases were the same 244 lines apart from three things: the body name, the
  `MapGenVillage<Body>.initiateStructures()` static block, and one 8-line "snap the ground level to air at y=64" fast
  path that **only Jupiter** has.
* The palette seam the rest of the refactor needs already exists: `StructureComponent*Village` overrides
  `placeBlockAtCurrentPosition`/`fillWithBlocks`/`func_151554_b` and routes them through `getBiomeSpecificBlock` and
  `getBiomeSpecificBlockMetadata`. The copies were hand-edited anyway (Kepler22b's 0 -> 1, 2 -> 11), which is where the
  duplication actually hurts.

**Step 0 - the safety net (done).** `VillageSnapshots` (in-game) builds every block-placing component - house, wood
hut, well, field, field2, torch, path - for all 22 bodies, in both coordinate branches, on a prepared scratch plateau
from a fixed seed, and folds every block id and metadata the component *changes* into one hash: **286 snapshots**,
committed to `src/gameTest/resources/village-golden.txt`. It is a characterisation test: it does not say what a village
should look like, only that it does not change. Verified three ways - deterministic across runs, fails on a single
changed metadata value in a house *and* in a path (mutation-tested twice), and all the `World`-side inputs are pinned.

**Step 1 - the wood hut (done).** In the 21 bodies where it is literally the same code,
`StructureComponent<VillageWoodHut>` extends `...VillageHouse` and keeps only its constructor and static factory:
44,652 lines deleted.

**Step 2 - the shared root and the house (done).** `com.mjr.extraplanets.world.village.StructureComponentVillageBase`
now holds everything that was identical in all 22 component bases (villager bookkeeping, ground-level probe, villager
spawner, the four placement overrides) plus `buildHouse(World, Random, StructureBoundingBox, Block)` - the 2,153-line
building, taking the body's block as a parameter. The 22 per-body bases are 138 lines of registration and piece
lookup, and the 21 non-Kepler22b houses are 62 lines each. Jupiter's odd ground-level fast path survives as a
`snapsGroundLevelToAir()` override rather than being silently unified.

**Step 3 - the other block-placing components (done).** `buildTorch`, `buildField`, `buildField2`, `buildPath` and
`buildWell` join `buildHouse` in the shared base, so torch, field, field2, path and well are one implementation each
plus a per-body delegate (62 lines -> a handful). Two things the snapshot harness caught while wiring this up: Field2's
crop types had to move to the base with its build code (its constructor picks them from the `Random`), and the shared
crop helper needed a distinct name because Io's own copy is `private`. `buildWell` takes the body's block as a
parameter - it is the only one of the five that names `ExtraPlanets_Blocks`.

**Step 4 - the classes that place no blocks (done, partly).** These cannot be seen by a block snapshot, so they got
their own harness first:

* **Piece dispatch.** `StructureVillageStart` is constructed for four fixed (seed, chunk) pairs per body and the
  ordered list of (component class, bounding box) is hashed - 88 more snapshots. Nothing in that path reads the
  terrain (a village start asks the world for its chunk manager and never uses it), so it is a pure function of the
  seed. Mutation-tested by changing one piece weight in Jupiter: only Jupiter's four cases changed.
* **Placement.** `MapGenVillage.canSpawnStructureAtCoords` is called over a 96x96-chunk window per body and the set of
  spawning chunks is hashed - 22 more snapshots. Mutation-tested by changing the grid spacing from 32 to 16.
* Both harnesses originally assumed the method/field they poke live on the per-body class; step 4 moved them into a
  base, so they now walk the hierarchy instead of failing.

Then: `MapGenVillageBase` holds the vanilla 32-chunk spawn grid (the per-body files keep the registration, the start
they build and their registration name - 83 -> 58 lines), and `StructureVillagePieceWeightBase` holds the piece
counting (the per-body subclass exists only to keep the constructor typed to that body's components).

**Step 5 - the piece lookup, the start algorithm and the pending queues (done).** Three more things turned out to be
identical in all 22 bodies: the negative/positive piece lookups in `StructureComponent<Body>Village` (a switch on
`coordBaseMode` around two static calls), the `StructureVillageStart` growth loop, and the two "pending pieces" queues.
They now live in `StructureComponentVillageBase` (which gained one abstract hook, `dispatchNext`/`dispatchNextPath`,
that forwards to the body's `StructureVillagePieces`) and in `StructureVillageStartBase`. The per-body starts also
carried a loop that counted non-road components into a local nobody read; that is gone.

That step produced the bug this whole harness exists for: the per-body start pieces still **declared their own copies**
of the two pending queues, which shadowed the shared ones - so the growing village filled the subclass's lists while
the shared start drained the empty base ones, and every village would have been a single well. The dispatch snapshot
caught it immediately (all 88 hashes moved), the fields were removed, and the hashes came back. Same shadowing trap as
`averageGroundLevel` in step 2 - a field that moves to a base must be *deleted* from the subclass.

**Step 6 - the piece table (done).** The four working statics of `StructureVillagePieces<Body>` (the weight sum, the
weighted random pick and the two growth guards) were identical in all 22; what differs is data (the weight table) and
three factories (the dispatch switch, the torch fallback, the path construction), which are now hooks on
`StructureVillagePiecesBase`. To make that possible the three pieces of state the shared code touches -
`structureVillageWeightedPieceList`, `structVillagePieceWeight` and `terrainType` - moved to
`StructureComponentVillageBase` and were **deleted** from the 22 start pieces, per the shadowing rule learned in
step 5. The per-body classes keep a static `INSTANCE` and their original static signatures as forwarders, so the ~60
call sites did not change.

**Final result: 131,287 -> 25,745 lines in the village tree (80% removed), 4.6 MB -> ~1 MB, 308 files -> 308 thin
per-body classes, with all 396 golden snapshots unchanged.** The 1,493 lines that now hold everything shared live in
`com.mjr.extraplanets.world.village`.

What is left is *not* duplication:

* 22 thin classes per component type - they have to stay separate, because the class is both the piece-dispatch key
  (`villagePieceClass == X.class`) and the `MapGenStructureIO` registration key.
* Kepler22b's house, wood hut, path and the rest of its set, and Io's five components: these genuinely differ (their
  own metadata or block palettes), not by formatting. Sharing them means expressing the difference as a palette, which
  is a behaviour change with no duplication payoff - Kepler22b's 498 diff lines are all metadata values, and Io's are
  a handful of different blocks. They are left alone deliberately, and the goldens keep them honest. Still untouched on purpose: Kepler22b's house and wood hut (the only pair with their
own metadata scheme) and its well, plus Io's five components (Io's buildings genuinely differ - different blocks, not
different formatting).

**Optional follow-ups, none of them duplication:**

1. Collapse the 22 thin subclasses into configuration keys - the largest structural change left, but it means
   replacing the class-as-dispatch-key design with a registry, and `MapGenStructureIO` still needs distinct classes.
   Low value now that each is a few dozen lines.
2. Kepler22b's buildings, if someone wants them expressed as a palette. The first step is to prove its metadata
   mapping is a pure function of (block, metadata); if it is not, they should stay as they are.

Do not reorder these: the harness covers a class before that class is touched.

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
6. **In-game coverage in CI.** See §8.6 — the machinery is in place, only the class-dump flag is missing.

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

### 8.4 Galacticraft asks `Launch.classLoader` in constructors

`ElectricArmorBase`'s constructor calls `EnergyConfigHandler.isIndustrialCraft2Loaded()`, which resolves
`Launch.classLoader` and casts it to FML's `LaunchClassLoader`. In a plain JVM that is
`ClassCastException: sun.misc.Launcher$AppClassLoader cannot be cast to net.minecraft.launchwrapper.LaunchClassLoader`,
so **no `ElectricArmorBase` subclass can be instantiated headless at all** - not even a test stub. This was found while
trying to unit-test the `getElectricityStored` fix, which is why those four checks live in the in-game suite instead
(`armor:` group). The same applies to any class whose constructor reaches into GC's `ConfigManagerCore` static state.

### 8.5 `FluidRegistry` and `Forge Configuration` need FML injections

`FluidRegistry`'s static initialiser touches the FML side, and `net.minecraftforge.common.config.Configuration` reads
`FMLInjectionData.data()[6]` (the Minecraft home directory), which is `null` outside a launch and throws an NPE.
`FmlTestEnvironment` stubs the injection data so `Config.init()` can run headless; fluid-registry-dependent code
cannot (fluids must be created in the game test).

### 8.6 In-game coverage

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
| 1 | `CommonProxy.getClientWorld()`/`getKeyDisplayString()` seam for the 8 remaining tooltip-only client calls (§4 P2) | 2 hours | low |
| 3 | Empty `catch` blocks and `catch (Throwable)` in worldgen (§6.2) | 1 day | low |
| 4 | `Config` instance facade (§4 P1) | 2-3 days | medium |
| 5 | Refactor the 44 worldgen copies (§6.3) | 1 week | medium, mechanical |
| 6 | Delete / implement the dead config keys (§5.4) | depends | low |
| 7 | Add `gameTestServer` + `smokeTest` to CI (§6.4) | 1 hour | none |
| 8 | Delete the local `migrate/` tree (§5.3) | 1 min | none |
| ✅ | Make `ExtraPlanets_Modules.init()` order-independent (§4 P1) | 2 hours | medium |
| ✅ | Name/value config sync payload with a compile-checked accessor table (§4 P1, §6.1) | 4 hours | medium |
| ✅ | `ModuleHelper` cast guard, biome id 0, vehicle client guard, `getElectricityStored` split, `PacketSimple` failure, logging sweep (§4 P2, §6.2) | 1 day | low |
| ✅ | Dead code (§5.1, §5.2): 17 classes + 728 KB of textures deleted, 3 unwired features restored, 2 wiring guards added | 1 day | none |
| ✅ | Village refactor steps 0-6 (§6.3): three snapshot harnesses + 105,542 lines of duplication removed (80%) | 3 days | none (harness-gated) |
| ✅ | §6.1: microblock table + `WorldHazards` + recipe split, with goldens and a headless classifier test | 1 day | none |
| ✅ | §6.2: empty catch blocks logged, `NoStrayTracesTest` extended to forbid them | 2 hours | none |
| | Optional: enable biome-edge stabilisation (§5.2) - one shared class + one line per chunk manager | 2 hours | medium - worldgen change |

The test suite that now guards changes is the more valuable artefact than any single fix above: `./gradlew check` fails
on a config-id collision, a broken sync contract, a module-registry duplicate, a packet-codec regression, and a
coverage drop, and `./gradlew gameTestServer` fails on any registration, ore-dictionary, recipe or tile-entity
regression — none of which were guarded before this pass.
