package com.mjr.extraplanets.gametest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import com.mjr.extraplanets.world.features.WorldGenBasicHideout;
import com.mjr.extraplanets.world.features.WorldGenBigBlueTree;
import com.mjr.extraplanets.world.features.WorldGenBigPurpleTree;
import com.mjr.extraplanets.world.features.WorldGenBigRedTree;
import com.mjr.extraplanets.world.features.WorldGenBlueHut;
import com.mjr.extraplanets.world.features.WorldGenBlueTower;
import com.mjr.extraplanets.world.features.WorldGenBrownHut;
import com.mjr.extraplanets.world.features.WorldGenCustomIceSpike;
import com.mjr.extraplanets.world.features.WorldGenDeshTower;
import com.mjr.extraplanets.world.features.WorldGenFrozenNitrogenPile;
import com.mjr.extraplanets.world.features.WorldGenGreenHut;
import com.mjr.extraplanets.world.features.WorldGenIgloo;
import com.mjr.extraplanets.world.features.WorldGenLogTree;
import com.mjr.extraplanets.world.features.WorldGenNuclearPile;
import com.mjr.extraplanets.world.features.WorldGenPurpleHut;
import com.mjr.extraplanets.world.features.WorldGenRedHut;
import com.mjr.extraplanets.world.features.WorldGenSlimeTree;
import com.mjr.extraplanets.world.features.WorldGenVolcano;
import com.mjr.extraplanets.world.features.WorldGenYellowHut;

/**
 * Every world feature, generated for real.
 *
 * <p>
 * These are the largest untested classes in the mod (the huts are 500 lines of block placement each, the volcano is
 * 1,500) and none of them can run headless: a generator asks the world for the blocks under it, walks to the surface
 * and writes hundreds of blocks. The in-game suite is the only place they can be exercised at all.
 * </p>
 *
 * <p>
 * Each one runs on a cleared, flat plateau, so the placement check passes for reasons the test controls rather than
 * because of the terrain it happened to find. A generator that placed nothing fails the check - which is what
 * "returns false from checkValidSpawn and silently does nothing" looks like.
 * </p>
 */
final class WorldFeatureSnapshots {

    /** Origin of the scratch plateau; far from the village harness so the two cannot interfere. */
    private static final int ORIGIN_X = 5000;
    private static final int ORIGIN_Z = 5000;

    /** Top of the plateau. 63 keeps y=64 inside the generators' ground-level probes, as in the village harness. */
    private static final int GROUND_Y = 63;

    /** Half-width of the prepared area. The volcano is the biggest at 25 blocks. */
    private static final int RADIUS = 40;

    private WorldFeatureSnapshots() {}

    private static Map<String, WorldGenerator> generators() {
        Map<String, WorldGenerator> generators = new LinkedHashMap<String, WorldGenerator>();
        generators.put("blueHut", new WorldGenBlueHut());
        generators.put("brownHut", new WorldGenBrownHut());
        generators.put("greenHut", new WorldGenGreenHut());
        generators.put("purpleHut", new WorldGenPurpleHut());
        generators.put("redHut", new WorldGenRedHut());
        generators.put("yellowHut", new WorldGenYellowHut());
        generators.put("blueTower", new WorldGenBlueTower());
        generators.put("hideout", new WorldGenBasicHideout());
        generators.put("bigBlueTree", new WorldGenBigBlueTree());
        generators.put("bigPurpleTree", new WorldGenBigPurpleTree());
        generators.put("bigRedTree", new WorldGenBigRedTree());
        generators.put("logTree", new WorldGenLogTree());
        generators.put("slimeTree", new WorldGenSlimeTree());
        generators.put("igloo", new WorldGenIgloo());
        generators.put("iceSpike", new WorldGenCustomIceSpike());
        generators.put("frozenNitrogenPile", new WorldGenFrozenNitrogenPile());
        generators.put("nuclearPile", new WorldGenNuclearPile());
        generators.put("volcano", new WorldGenVolcano());
        generators.put("deshTower", new WorldGenDeshTower());
        return generators;
    }

    static void run(TestReporter reporter) {
        reporter.check("worldgen: every structure generator places its blocks", () -> {
            World world = net.minecraft.server.MinecraftServer.getServer()
                .worldServerForDimension(0);
            TestReporter.assertNotNull("overworld", world);

            prepare(world);
            for (Map.Entry<String, WorldGenerator> feature : generators().entrySet()) {
                // The ice spike only builds on snow/ice, and it has its own generate() overload that takes the surface
                // block; everything else expects ordinary ground.
                Block surface = "iceSpike".equals(feature.getKey()) ? Blocks.snow : Blocks.grass;
                reset(world, surface);
                int placed = generate(world, feature.getKey(), feature.getValue());
                // The ice spike decorates an existing snow surface and is allowed to decide not to; everything else
                // builds a structure whenever its placement check passes.
                int expected = "iceSpike".equals(feature.getKey()) ? 1 : 20;
                TestReporter.assertTrue(
                    feature.getKey() + ": the generator placed nothing (its placement check probably refused the"
                        + " prepared ground)",
                    placed >= expected);
            }
            reset(world, Blocks.grass);
        });
    }

    /** Runs one generator on the cleared plateau. @return how many blocks it changed. */
    private static int generate(World world, String name, WorldGenerator generator) {
        // The ice spike is allowed to decline: it only decorates an existing snow surface when the roll says so, so it
        // gets several attempts before it counts as a failure. Everything else gets one fixed seed, so a failure is
        // reproducible.
        int attempts = "iceSpike".equals(name) ? 16 : 1;
        int placed = 0;

        for (int attempt = 0; attempt < attempts; attempt++) {
            Random rand = new Random(name.hashCode() + attempt * 31L);
            int before = countAir(world);

            if (generator instanceof com.mjr.extraplanets.world.features.WorldGenCustomIceSpike) {
                ((com.mjr.extraplanets.world.features.WorldGenCustomIceSpike) generator)
                    .generate(world, rand, ORIGIN_X, GROUND_Y + 1, ORIGIN_Z, Blocks.snow);
            } else {
                boolean generated = generator.generate(world, rand, ORIGIN_X, GROUND_Y + 1, ORIGIN_Z);
                TestReporter.assertTrue(name + ": generate() returned false", generated);
            }

            placed = before - countAir(world);
            if (placed > 0) {
                return placed;
            }
        }

        return placed;
    }

    /** @return the number of air blocks in the plateau volume, i.e. how much of it is empty. */
    private static int countAir(World world) {
        int air = 0;
        for (int x = ORIGIN_X - RADIUS; x <= ORIGIN_X + RADIUS; x++) {
            for (int z = ORIGIN_Z - RADIUS; z <= ORIGIN_Z + RADIUS; z++) {
                for (int y = GROUND_Y - 2; y <= GROUND_Y + 40; y++) {
                    if (world.getBlock(x, y, z) == Blocks.air) {
                        air++;
                    }
                }
            }
        }
        return air;
    }

    private static void prepare(World world) {
        for (int x = ORIGIN_X - RADIUS; x <= ORIGIN_X + RADIUS; x++) {
            for (int z = ORIGIN_Z - RADIUS; z <= ORIGIN_Z + RADIUS; z++) {
                for (int y = GROUND_Y - 8; y < GROUND_Y; y++) {
                    setFast(world, x, y, z, Blocks.stone);
                }
                setFast(world, x, GROUND_Y, z, Blocks.grass);
            }
        }
        reset(world, Blocks.grass);
    }

    private static void reset(World world, Block surface) {
        for (int x = ORIGIN_X - RADIUS; x <= ORIGIN_X + RADIUS; x++) {
            for (int z = ORIGIN_Z - RADIUS; z <= ORIGIN_Z + RADIUS; z++) {
                for (int y = GROUND_Y + 1; y <= GROUND_Y + 42; y++) {
                    setFast(world, x, y, z, Blocks.air);
                }
                setFast(world, x, GROUND_Y, z, surface);
            }
        }
        for (int chunkX = (ORIGIN_X - RADIUS) >> 4; chunkX <= (ORIGIN_X + RADIUS) >> 4; chunkX++) {
            for (int chunkZ = (ORIGIN_Z - RADIUS) >> 4; chunkZ <= (ORIGIN_Z + RADIUS) >> 4; chunkZ++) {
                world.getChunkFromChunkCoords(chunkX, chunkZ)
                    .generateSkylightMap();
            }
        }
    }

    /** Chunk-level write: the generators under test still go through the real {@code World#setBlock} path. */
    private static void setFast(World world, int x, int y, int z, Block block) {
        world.getChunkFromBlockCoords(x, z)
            .func_150807_a(x & 15, y, z & 15, block, 0);
    }
}
