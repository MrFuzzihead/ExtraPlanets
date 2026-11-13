package com.mjr.extraplanets.gametest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

/**
 * Golden snapshots of the village structures.
 *
 * <p>
 * The village code is 22 copies of the same 14 classes (see {@code CODE_REVIEW.md} §6.3) and the plan is to collapse
 * them into one shared implementation plus per-body palettes. That is a mechanical refactor of ~4,300 lines of
 * block-placement code per body, which is exactly the kind of change that produces "the village has a hole in the
 * roof" or "the village overwrites itself" without failing to compile.
 * </p>
 *
 * <p>
 * So each component is generated against a prepared scratch plateau with a fixed seed, and every block (id and
 * metadata) inside its bounding box is folded into one hash. The hashes are committed; after the refactor they must
 * be identical. This is a characterisation test: it does not assert what a village <em>should</em> look like, only
 * that it does not change.
 * </p>
 *
 * <p>
 * Determinism: the component is constructed through its own {@code func_74921_a} factory and placed on a flat plateau
 * that is rebuilt before each component, so neither the surrounding terrain nor the world seed can influence the
 * result. Everything else comes from the {@link Random} passed in.
 * </p>
 *
 * <p>
 * Recording: run {@code ./gradlew gameTestServer -PrecordGoldens} once to write
 * {@code src/gameTest/resources/village-golden.txt}. Never record after a change you do not trust - the point of the
 * file is that it was produced by code that was known good.
 * </p>
 */
final class VillageSnapshots {

    /** Bodies under {@code moons/}. */
    private static final String[] MOONS = { "Callisto", "Deimos", "Europa", "Ganymede", "Iapetus", "Io", "Oberon",
        "Phobos", "Rhea", "Titan", "Titania", "Triton" };

    /** Bodies under {@code planets/}. */
    private static final String[] PLANETS = { "Ceres", "Eris", "Jupiter", "Kepler22b", "Mercury", "Neptune", "Pluto",
        "Saturn", "Uranus", "Venus" };

    /** The components whose output is snapshotted: the two 2,150-line classes the refactor targets. */
    private static final String[] COMPONENTS = { "VillageHouse", "VillageWoodHut" };

    /**
     * Two of the four rotations, which is enough to cover both coordinate branches of
     * {@code StructureComponent#placeBlockAtCurrentPosition}: modes 0 and 2 walk X (with opposite signs) and modes 1
     * and 3 walk Z, so 0 and 1 exercise each branch once.
     */
    private static final int[] ROTATIONS = { 0, 1 };

    /**
     * Top of the scratch plateau. 63 is deliberate: {@code getAverageGroundLevel} scans for air starting at y=64 and
     * only counts columns whose bounding box contains y=64, so a village placed higher than that gets ground level
     * -1 and silently generates nothing.
     */
    private static final int PLATEAU_TOP = 63;

    private static final int SCRATCH_X = 3000;
    private static final int SCRATCH_Z = 3000;
    /** Half-width of the prepared area around the scratch origin; a component's box reaches 16 blocks. */
    private static final int SCRATCH_RADIUS = 24;

    private VillageSnapshots() {}

    /** Runs the check. */
    static void run(TestReporter reporter) {
        reporter.check("worldgen: village components build exactly the same blocks (golden)", () -> {
            World world = net.minecraft.server.MinecraftServer.getServer()
                .worldServerForDimension(0);
            TestReporter.assertNotNull("overworld", world);

            Map<String, Long> actual = snapshot(world);
            TestReporter
                .assertEquals("snapshot count", bodies().size() * COMPONENTS.length * ROTATIONS.length, actual.size());

            File goldenFile = goldenFile();
            if (Boolean.getBoolean("extraplanets.recordGoldens")) {
                write(goldenFile, actual);
                TestReporter.assertTrue("recorded " + actual.size() + " hashes to " + goldenFile, goldenFile.isFile());
                return;
            }

            TestReporter.assertTrue(
                "missing " + goldenFile + "; record it with ./gradlew gameTestServer -PrecordGoldens",
                goldenFile.isFile());
            TestReporter.assertEquals("golden file is empty", false, read(goldenFile).isEmpty());

            Map<String, Long> expected = read(goldenFile);
            List<String> failures = new ArrayList<String>();
            for (Map.Entry<String, Long> entry : expected.entrySet()) {
                Long now = actual.get(entry.getKey());
                if (now == null) {
                    failures.add(entry.getKey() + ": no longer produced");
                } else if (!now.equals(entry.getValue())) {
                    failures.add(
                        entry.getKey() + ": " + Long.toHexString(entry.getValue()) + " -> " + Long.toHexString(now));
                }
            }
            for (String key : actual.keySet()) {
                if (!expected.containsKey(key)) {
                    failures.add(key + ": new component (record goldens deliberately)");
                }
            }

            TestReporter.assertEquals(
                "village block output changed, which means the generation is no longer identical:\n  "
                    + String.join("\n  ", failures),
                0,
                failures.size());
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // Generation
    // ---------------------------------------------------------------------------------------------------------

    private static Map<String, Long> snapshot(World world) {
        Map<String, Long> hashes = new TreeMap<String, Long>();
        preparePlateau(world, SCRATCH_X, SCRATCH_Z);
        int index = 0;
        for (String body : bodies()) {
            for (String component : COMPONENTS) {
                for (int rotation : ROTATIONS) {
                    // A distinct, stable seed per case. Every component is built in the same cleared scratch box, so
                    // its hash cannot depend on whichever component ran before it.
                    long seed = 1000L + index;
                    clearScratch(world, SCRATCH_X, SCRATCH_Z);
                    hashes.put(
                        body + "/" + component + "/r" + rotation,
                        generateAndHash(world, body, component, seed, SCRATCH_X, SCRATCH_Z, rotation));
                    index++;
                }
            }
        }
        clearScratch(world, SCRATCH_X, SCRATCH_Z);
        return hashes;
    }

    private static long generateAndHash(World world, String body, String component, long seed, int x, int z,
        int rotation) {
        Random rand = new Random(seed);
        String pkg = packageOf(body);
        try {
            Class<?> pieces = Class.forName(pkg + "StructureVillagePieces" + body);
            Class<?> startPieceClass = Class.forName(pkg + "StructureComponent" + body + "VillageStartPiece");
            Class<?> componentClass = Class.forName(pkg + "StructureComponent" + body + component);

            Method weights = pieces.getMethod("getStructureVillageWeightedPieceList", Random.class, int.class);
            Object pieceList = weights.invoke(null, rand, 0);

            Constructor<?> startCtor = startPieceClass.getConstructor(
                WorldChunkManager.class,
                int.class,
                Random.class,
                int.class,
                int.class,
                ArrayList.class,
                int.class);
            Object startPiece = startCtor
                .newInstance(world.getWorldChunkManager(), 0, rand, (x << 4) + 2, (z << 4) + 2, pieceList, 0);

            // The component's own factory builds the correct bounding box for its size and rotation, which is the
            // same call the piece-generation code makes; constructing the box by hand would test a different thing.
            // Its name is not consistent (func_74921_a for houses, func_74908_a for wood huts), so find it by shape.
            Method factory = findFactory(componentClass);
            Object built = factory
                .invoke(null, startPiece, new ArrayList<Object>(), rand, x, PLATEAU_TOP + 1, z, rotation, 0);
            TestReporter.assertNotNull(body + "/" + component + ": the factory returned null", built);

            StructureComponent structure = (StructureComponent) built;
            boolean placed = structure.addComponentParts(world, rand, boundingBoxOf(structure));
            TestReporter.assertTrue(body + "/" + component + "/r" + rotation + ": addComponentParts failed", placed);

            return hash(world, boundingBoxOf(structure), body + "/" + component + "/r" + rotation);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(body + "/" + component + ": village classes do not have the expected shape", e);
        }
    }

    private static long hash(World world, StructureBoundingBox box, String key) {
        long hash = 1125899906842597L;
        int blocks = 0;
        for (int y = box.minY; y <= box.maxY; y++) {
            for (int z = box.minZ; z <= box.maxZ; z++) {
                for (int x = box.minX; x <= box.maxX; x++) {
                    Block block = world.getBlock(x, y, z);
                    int meta = world.getBlockMetadata(x, y, z);
                    hash = hash * 31 + Block.getIdFromBlock(block) * 16L + meta;
                    if (block != Blocks.air) {
                        blocks++;
                    }
                }
            }
        }
        // A component that placed almost nothing is not a useful snapshot; fail loudly rather than recording a hash
        // of air. A house shell is ~500 non-air cells of the 17x9x17 box; the rest is interior, cleared by the
        // component's own fillWithAir calls.
        System.out.println("[EP-GAMETEST] " + key + " box=" + box + " blocks=" + blocks);
        TestReporter.assertTrue(key + ": the component placed only " + blocks + " blocks", blocks > 200);
        return hash;
    }

    // ---------------------------------------------------------------------------------------------------------
    // Scratch terrain
    // ---------------------------------------------------------------------------------------------------------

    /** Built once per run: solid ground below {@link #PLATEAU_TOP}, open air above. */
    private static void preparePlateau(World world, int x, int z) {
        for (int dx = -SCRATCH_RADIUS; dx <= SCRATCH_RADIUS; dx++) {
            for (int dz = -SCRATCH_RADIUS; dz <= SCRATCH_RADIUS; dz++) {
                for (int y = PLATEAU_TOP - 10; y <= PLATEAU_TOP; y++) {
                    setFast(world, x + dx, y, z + dz, Blocks.stone);
                }
                for (int y = PLATEAU_TOP + 1; y <= PLATEAU_TOP + 24; y++) {
                    setFast(world, x + dx, y, z + dz, Blocks.air);
                }
            }
        }
    }

    /**
     * Removes every block a component could have written, so each snapshot starts from the same empty space. A
     * component writes only inside its bounding box (17x9x17, offset by up to 16 blocks for a 90-degree rotation),
     * so a 49x49x25 box around the origin covers all four orientations with room to spare.
     */
    private static void clearScratch(World world, int x, int z) {
        for (int dx = -SCRATCH_RADIUS; dx <= SCRATCH_RADIUS; dx++) {
            for (int dz = -SCRATCH_RADIUS; dz <= SCRATCH_RADIUS; dz++) {
                for (int y = PLATEAU_TOP + 1; y <= PLATEAU_TOP + 24; y++) {
                    setFast(world, x + dx, y, z + dz, Blocks.air);
                }
            }
        }
    }

    /**
     * Writes straight into the chunk, skipping the notifications {@code World#setBlock} issues. Only used to prepare
     * the scratch area: the code under test still runs against the real {@code World#setBlock} path.
     */
    private static void setFast(World world, int x, int y, int z, Block block) {
        net.minecraft.world.chunk.Chunk chunk = world.getChunkFromBlockCoords(x, z);
        chunk.func_150807_a(x & 15, y, z & 15, block, 0);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------------------

    /** @return the component's {@code (StartPiece, List, Random, x, y, z, coordMode, type)} static factory. */
    private static Method findFactory(Class<?> componentClass) {
        Method found = null;
        for (Method method : componentClass.getMethods()) {
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) continue;
            if (method.getReturnType() != componentClass) continue;
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 8 || !params[1].equals(List.class) || !params[2].equals(Random.class)) continue;
            if (found != null) {
                throw new AssertionError(
                    componentClass.getName() + " has two candidate factories: "
                        + found.getName()
                        + " and "
                        + method.getName());
            }
            found = method;
        }
        if (found == null) {
            throw new AssertionError(componentClass.getName() + " has no static factory to build it with");
        }
        return found;
    }

    private static List<String> bodies() {
        List<String> bodies = new ArrayList<String>();
        for (String body : MOONS) bodies.add(body);
        for (String body : PLANETS) bodies.add(body);
        return bodies;
    }

    private static String packageOf(String body) {
        for (String moon : MOONS) {
            if (moon.equals(body)) {
                return "com.mjr.extraplanets.moons." + body + ".worldgen.village.";
            }
        }
        return "com.mjr.extraplanets.planets." + body + ".worldgen.village.";
    }

    private static StructureBoundingBox boundingBoxOf(StructureComponent component) {
        // boundingBox is protected on StructureComponent and there is no accessor before 1.8.
        try {
            java.lang.reflect.Field field = StructureComponent.class.getDeclaredField("boundingBox");
            field.setAccessible(true);
            return (StructureBoundingBox) field.get(component);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("StructureComponent.boundingBox is gone", e);
        }
    }

    private static File goldenFile() {
        String dir = System.getProperty("extraplanets.projectDir");
        TestReporter.assertNotNull("-Dextraplanets.projectDir must be set by addon.gradle.kts", dir);
        return new File(dir, "src/gameTest/resources/village-golden.txt");
    }

    private static void write(File file, Map<String, Long> hashes) {
        StringBuilder text = new StringBuilder();
        text.append("# Village component golden hashes - see gametest/VillageSnapshots.java\n");
        text.append("# key<TAB>hash. Regenerate only with ./gradlew gameTestServer -PrecordGoldens.\n");
        for (Map.Entry<String, Long> entry : hashes.entrySet()) {
            text.append(entry.getKey())
                .append('\t')
                .append(Long.toHexString(entry.getValue()))
                .append('\n');
        }
        try {
            file.getParentFile()
                .mkdirs();
            Files.write(
                file.toPath(),
                text.toString()
                    .getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            throw new IllegalStateException("cannot write " + file, e);
        }
    }

    private static Map<String, Long> read(File file) {
        Map<String, Long> hashes = new LinkedHashMap<String, Long>();
        try {
            for (String line : Files.readAllLines(file.toPath(), Charset.forName("UTF-8"))) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\t");
                hashes.put(parts[0], Long.parseUnsignedLong(parts[1], 16));
            }
        } catch (IOException e) {
            throw new IllegalStateException("cannot read " + file, e);
        }
        return hashes;
    }
}
