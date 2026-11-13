# ExtraPlanets

[![](https://img.shields.io/badge/Minecraft-1.7.10-62a34a)](https://minecraft.wiki/w/Java_Edition_1.7.10)
[![](https://img.shields.io/badge/Forge-10.13.4.1614-1e2b4f)](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.7.10.html)
[![](https://img.shields.io/badge/Galacticraft-3.5.0--GTNH-4c8dae)](https://github.com/GTNewHorizons/Galacticraft)
[![](https://img.shields.io/badge/ModularUI2-2.4.0-9146ff)](https://github.com/GTNewHorizons/ModularUI2)
[![](http://cf.way2muchnoise.eu/full_extraplanets_downloads.svg)](https://minecraft.curseforge.com/projects/extraplanets)
[![](http://cf.way2muchnoise.eu/versions/Minecraft_extraplanets_all.svg)](https://minecraft.curseforge.com/projects/extraplanets)

A Galacticraft addon for Minecraft 1.7.10: ten planets, twelve moons, four extra solar systems, rocket tiers 4-10,
rovers, bosses, machines, fluids, armour with an installable module system, and a pressure/radiation/thermal
survival layer on top of Galacticraft's oxygen model.

Originally written by MJRLegends.

## What it adds

| Kind | Content |
|---|---|
| Planets | Mercury, Venus, Ceres, Jupiter, Saturn, Uranus, Neptune, Pluto, Eris, Kepler-22b, Kuiper Belt |
| Moons | Callisto, Deimos, Europa, Ganymede, Iapetus, Io, Oberon, Phobos, Rhea, Titan, Titania, Triton |
| Solar systems | Kepler-22, Kepler-47, Kepler-62, Kepler-69 |
| Rockets | tiers 4-10, cargo variants, launch pads, landing pads, fuel loaders, vehicle chargers |
| Vehicles | Mars rover, Venus rover, powered (electric) rover |
| Machines | advanced/ultimate refinery, fuel loader, oxygen compressor & decompressor, solar panel, charging pad |
| Armour | carbon, palladium, magnesium, crystal, zinc, mercury, uranium, tungsten, red/green/blue/purple/yellow/white diamonds and gem sets, plus the modular Tier-1 space suit |
| Suit modules | jetpack (T1/T2), night vision, sensor glasses, solar panels, speed boost, step assist, gravity controller, enhanced/anti gravity, no-fall-damage, radiation shield, pressure seal, protection+, battery expansion |
| Survival systems | pressure, radiation, thermal padding and custom HUD overlays |
| World | dungeons and treasure chests, village worldgen, volcanoes, fossils, custom ores, 12+ fluids, ForgeMultipart microblocks |
| Integration | NotEnoughItems recipe handlers, IC2 machine recipes, CoFH RF and Mekanism Joules for electric armour |

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | 1.7.10 |
| Minecraft Forge | 10.13.4.1614 |
| [Galacticraft](https://github.com/GTNewHorizons/Galacticraft) | 3.5.0-GTNH (required at runtime) |
| [ModularUI2](https://github.com/GTNewHorizons/ModularUI2) | 2.4.0 (required, used by the suit module GUI) |
| NotEnoughItems, CodeChickenCore | optional (recipe handlers) |
| IC2, CoFH Core, Mekanism, ForgeMultipart | optional (power bridges, microblocks) |

## Building

Everything is handled by the `com.gtnewhorizons.gtnhconvention` plugin:

```sh
./gradlew build              # compile, unit tests, format check, coverage gate
./gradlew test               # unit tests only
./gradlew jacocoTestReport   # HTML coverage -> build/reports/jacoco/test/html
./gradlew runClient          # dev client
./gradlew runServer          # dev server
```

The build must be run with a JDK 17+ toolchain; the mod itself compiles to Java 8 bytecode (via Jabel) and the unit
tests run on a Java 8 launcher.

### Tests

| Command | What it runs |
|---|---|
| `./gradlew test` | 122 headless JUnit 4 tests (JUnit 4.13.2 + Mockito 4.11.0, Java 8 launcher) |
| `./gradlew check` | the above plus Spotless, Checkstyle, the coverage floor and the gameTest compile |
| `./gradlew gameTestServer` | boots a real dedicated server, runs 34 in-game checks (including 286 golden village snapshots), then exits with the result |
| `./gradlew gameTestServer -PrecordGoldens` | re-records the village golden hashes - only after a change you trust |
| `./gradlew runServer -PsmokeTest` | boots a real server, prints `EXTRAPLANETS_SMOKE_TEST_OK` and exits |

The unit suite covers what does not need a game: the config-file defaults (unique dimension/station/biome/schematic
ids, all biome ids inside the 1.7.10 256-slot array), the server→client config sync contract, the suit module NBT
format, the biome registry's collision/relocation rules, and the `PacketSimple` wire format including its malformed
input handling.

The in-game mod lives in `src/gameTest/java` and is never shipped. It covers what a plain JVM cannot: the real block,
item and tile-entity registries, the real ore dictionary, furnace results, and a live `TileEntity#writeToNBT` /
`createAndLoadEntity` round trip.

```sh
mkdir -p run/server && echo "eula=true" > run/server/eula.txt
./gradlew gameTestServer
```

`CODE_REVIEW.md` records the dependency audit, the bug/optimization/refactor backlog, the dead-code inventory and the
test-coverage baseline.

### Configuration

`config/ExtraPlanets.cfg` is generated on first launch. Every celestial body has an enable flag plus a dimension id
and (where relevant) biome ids, and the machine/armour/fluid features each have their own toggle. The ids are
server-authoritative: they are pushed to a connecting client so a mismatch cannot desync dimensions.

## Contributing

Bug reports and pull requests are welcome. Please read `CODE_REVIEW.md` before structural changes: it records the
known limitations and the ordered refactor plan.

## Translators

Every string lives in `src/main/resources/assets/extraplanets/lang/`. Take `en_US.lang` as the base and submit a pull
request or an issue with the translated file.

## License

<a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc-nd/4.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/">Creative Commons
Attribution-NonCommercial-NoDerivatives 4.0 International License</a>. See
[tldrlegal](https://tldrlegal.com/license/creative-commons-attribution-noncommercial-noderivs-(cc-nc-nd)#summary) for a
summary.

## Credit

ExtraPlanets was created by MJRLegends. Galacticraft was created by micdoodle8 and is maintained for GTNH by the
GTNewHorizons team.
