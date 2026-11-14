package com.mjr.extraplanets.gametest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

import micdoodle8.mods.galacticraft.core.blocks.GCBlocks;

/**
 * Golden snapshots of the village structures.
 *
 * <p>
 * The village code is 22 copies of the same 14 classes (see {@code CODE_REVIEW.md} §6.3) and the plan is to collapse
 * them into one shared implementation plus per-body palettes. That is a mechanical refactor of thousands of lines of
 * block-placement code per body, which is exactly the kind of change that produces "the village has a hole in the
 * roof" without failing to compile.
 * </p>
 *
 * <p>
 * So each component is generated against a prepared scratch plateau with a fixed seed, and every block (id and
 * metadata) inside its bounding box is folded into one hash. The hashes are committed; after the refactor they must be
 * identical. This is a characterisation test: it does not assert what a village <em>should</em> look like, only that it
 * does not change.
 * </p>
 *
 * <p>
 * Determinism: the component is constructed the way the piece-generation code constructs it (through its own static
 * factory where it has one), placed on a flat plateau, and its bounding box is cleared before it runs, so neither the
 * surrounding terrain nor whatever an earlier case built can influence the result. Everything else comes from the
 * {@link Random} passed in.
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

    /**
     * Every component that places blocks. The ones that build children but no blocks of their own (the start piece,
     * the road piece, the piece-weight list) are not here: their behaviour is covered by the piece dispatch, which is
     * a class comparison, not block placement.
     */
    private static final String[] COMPONENTS = { "VillageHouse", "VillageWoodHut", "VillageWell", "VillageField",
        "VillageField2", "VillageTorch", "VillagePathGen" };

    /**
     * The rotations to cover. {@code StructureComponent#getXWithOffset} has one branch for modes 0/2 and one for 1/3,
     * so 0 and 1 exercise each of them. The well takes its rotation from the {@link Random} inside its constructor,
     * so it ignores this and gets a single case.
     */
    private static final int[] ROTATIONS = { 0, 1 };

    private static final String ROTATION_FREE_COMPONENT = "VillageWell";

    private static final int SCRATCH_X = 3000;
    private static final int SCRATCH_Z = 3000;

    /**
     * Top of the scratch plateau. 63 is deliberate: {@code getAverageGroundLevel} scans for air starting at y=64 and
     * only counts columns whose bounding box contains y=64, so a village placed higher than that gets ground level
     * -1 and silently generates nothing.
     */
    private static final int PLATEAU_TOP = 63;

    /** Half-width of the prepared plateau; a component's box reaches 16 blocks for a 90-degree rotation. */
    private static final int SCRATCH_RADIUS = 24;

    private VillageSnapshots() {}

    /** Runs the check. */
    static void run(TestReporter reporter) {
        reporter.check("worldgen: village components build exactly the same blocks (golden)", () -> {
            World world = net.minecraft.server.MinecraftServer.getServer()
                .worldServerForDimension(0);
            TestReporter.assertNotNull("overworld", world);

            Map<String, Long> actual = snapshot(world);
            TestReporter.assertEquals("snapshot count", expectedCases(), actual.size());

            verify(
                actual,
                "village block output changed, which means the generation is no longer identical",
                key -> !key.startsWith("dispatch/") && !key.startsWith("placement/"));
        });
    }

    /**
     * Compares a family of snapshots against the committed goldens, or records them when
     * {@code -PrecordGoldens} is set.
     */
    private static void verify(Map<String, Long> actual, String whatChanged,
        java.util.function.Predicate<String> belongsToThisFamily) {
        GoldenFiles.verify(actual, "village-golden.txt", belongsToThisFamily, whatChanged);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Piece dispatch - the classes that place no blocks
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Snapshots what a village is <em>made of</em>: the ordered list of component classes and their bounding boxes,
     * for a few fixed seeds and chunk positions.
     *
     * <p>
     * The block snapshots cannot see this. {@code StructureVillageStart}, {@code StructureVillagePieces},
     * {@code StructureComponentVillageStartPiece} and {@code StructureVillagePieceWeight} decide <em>which</em> pieces
     * exist and where; a mistake there shows up as a differently shaped village rather than a different block, so it
     * needs its own harness. Nothing here reads the terrain: a village start only asks the world for its chunk
     * manager, and that manager is never consulted (the per-body {@code villageSpawnBiomes} list is dead code), so
     * the result is a pure function of the seed and the chunk coordinates.
     * </p>
     */
    static void runDispatch(TestReporter reporter) {
        reporter.check("worldgen: village piece dispatch is unchanged (golden)", () -> {
            World world = net.minecraft.server.MinecraftServer.getServer()
                .worldServerForDimension(0);
            TestReporter.assertNotNull("overworld", world);

            Map<String, Long> actual = new TreeMap<String, Long>();
            int[][] cases = { { 0, 0 }, { 3, -2 }, { -7, 5 }, { 40, 40 } };
            for (String body : bodies()) {
                for (int i = 0; i < cases.length; i++) {
                    actual.put(
                        "dispatch/" + body + "/c" + i,
                        dispatchHash(world, body, cases[i][0], cases[i][1], 7000L + i));
                }
            }
            verify(
                actual,
                "village piece dispatch changed, which means villages are built from different pieces",
                key -> key.startsWith("dispatch/"));
        });

        reporter.check("worldgen: village placement is unchanged (golden)", () -> {
            World world = net.minecraft.server.MinecraftServer.getServer()
                .worldServerForDimension(0);
            TestReporter.assertNotNull("overworld", world);

            Map<String, Long> actual = new TreeMap<String, Long>();
            for (String body : bodies()) {
                actual.put("placement/" + body, placementHash(world, body));
            }
            verify(
                actual,
                "village placement changed, which means villages spawn in different chunks",
                key -> key.startsWith("placement/"));
        });
    }

    /** @return a hash of the component classes and bounding boxes of one generated village. */
    private static long dispatchHash(World world, String body, int chunkX, int chunkZ, long seed) {
        try {
            Class<?> startClass = Class.forName(packageOf(body) + "StructureVillageStart" + body);
            Object start = startClass.getConstructor(World.class, Random.class, int.class, int.class, int.class)
                .newInstance(world, new Random(seed), chunkX, chunkZ, 0);

            @SuppressWarnings("unchecked")
            List<StructureComponent> components = ((net.minecraft.world.gen.structure.StructureStart) start)
                .getComponents();
            TestReporter.assertTrue(body + ": the village has no components", components.size() > 0);

            long hash = 1125899906842597L;
            for (StructureComponent component : components) {
                StructureBoundingBox box = boundingBoxOf(component);
                hash = hash * 31 + component.getClass()
                    .getSimpleName()
                    .hashCode();
                hash = hash * 31 + box.minX;
                hash = hash * 31 + box.minY;
                hash = hash * 31 + box.minZ;
                hash = hash * 31 + box.maxX;
                hash = hash * 31 + box.maxY;
                hash = hash * 31 + box.maxZ;
            }
            hash = hash * 31 + components.size();
            return hash;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(body + ": the village start does not have the expected shape", e);
        }
    }

    /** @return a hash of every chunk, in a fixed window, where this body wants a village. */
    private static long placementHash(World world, String body) {
        final int radius = 48;
        try {
            Class<?> mapGenClass = Class.forName(packageOf(body) + "MapGenVillage" + body);
            Object mapGen = mapGenClass.getConstructor()
                .newInstance();

            // canSpawnStructureAtCoords only needs worldObj for World#setRandomSeed; it never reads the terrain.
            // Both the field and the method may be declared anywhere up the hierarchy (the shared base holds the
            // spawn grid), so look them up by walking it rather than assuming where they live.
            findField(mapGenClass, "worldObj").set(mapGen, world);

            Method canSpawn = findMethod(mapGenClass, "canSpawnStructureAtCoords", int.class, int.class);
            TestReporter.assertNotNull(body + ": no canSpawnStructureAtCoords to call", canSpawn);
            canSpawn.setAccessible(true);

            long hash = 1125899906842597L;
            int found = 0;
            for (int x = -radius; x < radius; x++) {
                for (int z = -radius; z < radius; z++) {
                    if ((Boolean) canSpawn.invoke(mapGen, x, z)) {
                        hash = hash * 31 + x;
                        hash = hash * 31 + z;
                        found++;
                    }
                }
            }
            TestReporter.assertTrue(
                body + ": no villages are placed in a " + (2 * radius) + "x" + (2 * radius) + " chunk window",
                found > 0);
            return hash;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(body + ": the map generator does not have the expected shape", e);
        }
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
                for (int rotation : rotationsFor(component)) {
                    // A distinct, stable seed per case, and the box is cleared before every run, so a hash cannot
                    // depend on whichever case ran before it.
                    long seed = 1000L + index;
                    String key = body + "/" + component + "/r" + rotation;
                    hashes.put(
                        key,
                        generateAndHash(
                            world,
                            body,
                            component,
                            seed,
                            SCRATCH_X,
                            PLATEAU_TOP + 1,
                            SCRATCH_Z,
                            rotation,
                            key));
                    index++;
                }
            }
        }
        resetScratch(world, SCRATCH_X, SCRATCH_Z);
        return hashes;
    }

    private static int expectedCases() {
        int cases = 0;
        for (String component : COMPONENTS) cases += rotationsFor(component).length;
        return cases * bodies().size();
    }

    private static int[] rotationsFor(String component) {
        return ROTATION_FREE_COMPONENT.equals(component) ? new int[] { 0 } : ROTATIONS;
    }

    private static long generateAndHash(World world, String body, String component, long seed, int x, int y, int z,
        int rotation, String key) {
        Random rand = new Random(seed);
        String pkg = packageOf(body);
        try {
            Class<?> pieces = Class.forName(pkg + "StructureVillagePieces" + body);
            Class<?> startPieceClass = Class.forName(pkg + "StructureComponent" + body + "VillageStartPiece");
            Class<?> componentClass = Class.forName(pkg + "StructureComponent" + body + component);

            Method weights = pieces.getMethod("getStructureVillageWeightedPieceList", Random.class, int.class);
            Object pieceList = weights.invoke(null, rand, 0);

            Object startPiece = startPieceClass
                .getConstructor(
                    WorldChunkManager.class,
                    int.class,
                    Random.class,
                    int.class,
                    int.class,
                    ArrayList.class,
                    int.class)
                .newInstance(world.getWorldChunkManager(), 0, rand, (x << 4) + 2, (z << 4) + 2, pieceList, 0);

            StructureComponent structure = build(componentClass, startPieceClass, startPiece, rand, x, y, z, rotation);
            StructureBoundingBox box = expanded(boundingBoxOf(structure));

            // Reset the whole scratch area, not just this box: a component's behaviour can depend on blocks outside
            // its own footprint (the path generator asks for the highest solid block in a column, which an earlier
            // case's house would answer), and the ground layer must be restored too, since the path replaces it.
            resetScratch(world, SCRATCH_X, SCRATCH_Z);

            // Snapshot the pristine state so the hash can be of what the component *changed*. Some components reach
            // outside their own bounding box (the path generator paves the ground block one below its box, the
            // foundation helper fills downwards), and comparing against the baseline keeps the snapshot about the
            // component's output rather than about the scratch plateau.
            int[] before = readRegion(world, box);

            boolean placed = structure.addComponentParts(world, rand, box);
            TestReporter.assertTrue(key + ": addComponentParts reported failure", placed);

            return hashOfChanges(world, box, before, key);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(key + ": village classes do not have the expected shape", e);
        }
    }

    /**
     * Builds a component the way the piece-generation code does. The three shapes in use are: a static factory that
     * returns the component (house, wood hut, fields), a static factory that returns just the bounding box (torch,
     * path) and the well, whose constructor takes coordinates and picks its own rotation.
     */
    private static StructureComponent build(Class<?> componentClass, Class<?> startPieceClass, Object startPiece,
        Random rand, int x, int y, int z, int rotation) throws ReflectiveOperationException {
        Method componentFactory = findStatic(componentClass, componentClass, 8);
        if (componentFactory != null) {
            return (StructureComponent) componentFactory
                .invoke(null, startPiece, new ArrayList<Object>(), rand, x, y, z, rotation, 0);
        }

        Method boxFactory = findStatic(componentClass, StructureBoundingBox.class, 7);
        if (boxFactory != null) {
            StructureBoundingBox box = (StructureBoundingBox) boxFactory
                .invoke(null, startPiece, new ArrayList<Object>(), rand, x, y, z, rotation);
            TestReporter.assertNotNull("the bounding box factory returned null", box);
            return (StructureComponent) componentClass
                .getConstructor(startPieceClass, int.class, Random.class, StructureBoundingBox.class, int.class)
                .newInstance(startPiece, 0, rand, box, rotation);
        }

        Constructor<?> byCoordinates = componentClass
            .getConstructor(startPieceClass, int.class, Random.class, int.class, int.class);
        return (StructureComponent) byCoordinates.newInstance(startPiece, 0, rand, x, z);
    }

    /**
     * @return the component's static factory with the given return type and parameter count, or null.
     */
    private static Method findStatic(Class<?> componentClass, Class<?> returnType, int parameterCount) {
        Method found = null;
        for (Method method : componentClass.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) continue;
            if (method.getReturnType() != returnType) continue;
            Class<?>[] params = method.getParameterTypes();
            if (params.length != parameterCount) continue;
            if (!params[1].equals(List.class) || !params[2].equals(Random.class)) continue;
            if (found != null) {
                throw new AssertionError(
                    componentClass.getName() + " has two candidate factories: "
                        + found.getName()
                        + " and "
                        + method.getName());
            }
            found = method;
        }
        return found;
    }

    /**
     * Folds into one number every block the component changed inside the box. Unchanged cells contribute nothing, so
     * the snapshot measures the component and not the scratch terrain it was placed on.
     */
    private static long hashOfChanges(World world, StructureBoundingBox box, int[] before, String key) {
        int[] after = readRegion(world, box);
        long hash = 1125899906842597L;
        int changed = 0;
        for (int i = 0; i < after.length; i++) {
            if (after[i] == before[i]) {
                hash = hash * 31;
            } else {
                hash = hash * 31 + after[i];
                changed++;
            }
        }
        // A component that changed nothing is not a useful snapshot. (A path only replaces the ground block under
        // itself, so "changed > 0" is the right bar; an absolute block count is not.)
        TestReporter.assertTrue(key + ": the component changed no blocks at all", changed > 0);
        return hash;
    }

    /** @return the block id and metadata of every cell in the box, row-major. */
    private static int[] readRegion(World world, StructureBoundingBox box) {
        int width = box.maxX - box.minX + 1;
        int depth = box.maxZ - box.minZ + 1;
        int height = box.maxY - box.minY + 1;
        int[] region = new int[width * height * depth];
        int i = 0;
        for (int y = box.minY; y <= box.maxY; y++) {
            for (int z = box.minZ; z <= box.maxZ; z++) {
                for (int x = box.minX; x <= box.maxX; x++) {
                    region[i++] = Block.getIdFromBlock(world.getBlock(x, y, z)) * 16 + world.getBlockMetadata(x, y, z);
                }
            }
        }
        return region;
    }

    /**
     * The component's own bounding box, grown by two blocks: components legitimately write just outside it (paths
     * pave the block under themselves, the foundation helper fills below), and covering that is also what makes an
     * off-by-one regression visible.
     */
    private static StructureBoundingBox expanded(StructureBoundingBox box) {
        return new StructureBoundingBox(
            box.minX - 2,
            Math.max(PLATEAU_TOP - 2, box.minY - 2),
            box.minZ - 2,
            box.maxX + 2,
            box.maxY + 2,
            box.maxZ + 2);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Scratch terrain
    // ---------------------------------------------------------------------------------------------------------

    /** Built once per run: solid ground below {@link #PLATEAU_TOP}, open air above. */
    private static void preparePlateau(World world, int x, int z) {
        for (int dx = -SCRATCH_RADIUS; dx <= SCRATCH_RADIUS; dx++) {
            for (int dz = -SCRATCH_RADIUS; dz <= SCRATCH_RADIUS; dz++) {
                for (int y = PLATEAU_TOP - 10; y < PLATEAU_TOP; y++) {
                    setFast(world, x + dx, y, z + dz, Blocks.stone, 0);
                }
                // The top layer is Galacticraft's moon gravel, which is what the village path generator looks for:
                // it only paves over that block (or air), so a stone surface would make every path place nothing.
                setFast(world, x + dx, PLATEAU_TOP, z + dz, GCBlocks.blockMoon, 5);
            }
        }
        resetScratch(world, x, z);
    }

    /**
     * Puts the scratch area back to open air over moon gravel. Every case starts from this exact state, which is what
     * makes the snapshots independent of each other and of the run before.
     */
    private static void resetScratch(World world, int x, int z) {
        for (int dx = -SCRATCH_RADIUS; dx <= SCRATCH_RADIUS; dx++) {
            for (int dz = -SCRATCH_RADIUS; dz <= SCRATCH_RADIUS; dz++) {
                setFast(world, x + dx, PLATEAU_TOP, z + dz, GCBlocks.blockMoon, 5);
                for (int y = PLATEAU_TOP + 1; y <= PLATEAU_TOP + 24; y++) {
                    setFast(world, x + dx, y, z + dz, Blocks.air, 0);
                }
            }
        }
    }

    /**
     * Writes straight into the chunk, skipping the notifications {@code World#setBlock} issues. Only used to prepare
     * the scratch area: the code under test still runs against the real {@code World#setBlock} path.
     */
    private static void setFast(World world, int x, int y, int z, Block block, int meta) {
        net.minecraft.world.chunk.Chunk chunk = world.getChunkFromBlockCoords(x, z);
        chunk.func_150807_a(x & 15, y, z & 15, block, meta);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------------------

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

    /** Finds a field on the class or any of its superclasses. */
    private static java.lang.reflect.Field findField(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                java.lang.reflect.Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // keep walking
            }
        }
        throw new AssertionError("no field '" + name + "' on " + type.getName() + " or its superclasses");
    }

    /** Finds a method on the class or any of its superclasses. */
    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Method method = current.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                // keep walking
            }
        }
        return null;
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
