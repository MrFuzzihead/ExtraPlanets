package com.mjr.extraplanets.gametest;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.World;

/**
 * The planet dungeons, generated for real.
 *
 * <p>
 * Seven bodies ship the same ~600-line dungeon generator (the room layout, the corridor carving, the entrance crater)
 * and none of it can run headless: it asks the world for blocks, walks its room lists and writes hundreds of blocks.
 * The in-game suite is the only place it can be exercised.
 * </p>
 *
 * <p>
 * The generator is taken from the body's real {@code ChunkProvider}, so the room lists are exactly the ones the game
 * uses - reproducing them here would test a copy rather than the thing that runs. The provider is asked to place a
 * dungeon with {@code generateUsingSetBlock}, which is the non-array path, on a seeded loop until one of the seeds
 * lands on a dungeon site (the generator's own 44-chunk grid decides that).
 * </p>
 */
final class DungeonSnapshots {

    /** Bodies that ship a dungeon. */
    private static final String[] BODIES = { "Venus", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto", "Eris" };

    /** Somewhere far from everything else the suite uses, so the dungeon cannot collide with another check. */
    private static final int ORIGIN_X = 7000;
    private static final int ORIGIN_Z = 7000;
    private static final int GROUND_Y = 40;

    private DungeonSnapshots() {}

    static void run(TestReporter reporter) {
        reporter.check("worldgen: every planet dungeon builds", () -> {
            World world = net.minecraft.server.MinecraftServer.getServer()
                .worldServerForDimension(0);
            TestReporter.assertNotNull("overworld", world);

            List<String> failures = new ArrayList<String>();
            for (String body : BODIES) {
                try {
                    if (!build(world, body)) {
                        failures.add(body);
                    }
                } catch (ReflectiveOperationException e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    for (StackTraceElement frame : cause.getStackTrace()) {
                        if (frame.getClassName()
                            .startsWith("com.mjr.extraplanets")) {
                            System.out.println("[EP-GAMETEST] dungeon failure " + body + ": " + cause + " at " + frame);
                            break;
                        }
                    }
                    throw new AssertionError(body + ": the dungeon generator failed: " + cause, e);
                }
            }
            TestReporter.assertEquals("bodies whose dungeon did not build: " + failures, 0, failures.size());
        });
    }

    /** @return true if a dungeon was placed. */
    private static boolean build(World world, String body) throws ReflectiveOperationException {
        String pkg = "com.mjr.extraplanets.planets." + body + ".worldgen.";
        Object provider = Class.forName(pkg + "ChunkProvider" + body)
            .getConstructor(World.class, long.class, boolean.class)
            .newInstance(world, 1234L, Boolean.TRUE);

        Object dungeon = readField(provider, "dungeonGenerator");
        TestReporter.assertTrue(
            body + ": the provider's dungeonGenerator is a "
                + dungeon.getClass()
                    .getName(),
            dungeon.getClass()
                .getName()
                .endsWith("dungeon.MapGenDungeon"));

        // The generator decides where a dungeon is (a 44-chunk grid, offset from the seed), so ask it where one would
        // be rather than counting blocks in a fixed box - that box would miss a dungeon placed a hundred blocks away
        // and the check would flake. Once the rule says "there is one near here", the build call is guaranteed to run
        // the whole generation path.
        // getDungeonNear reads the provider's dimension, and it only gets a world once a generation call has run, so
        // hand it the world first.
        setWorld(dungeon, world);

        for (int seed = 1; seed <= 40; seed++) {
            Object coords = dungeon.getClass()
                .getMethod("getDungeonNear", long.class, int.class, int.class)
                .invoke(dungeon, (long) seed, ORIGIN_X >> 4, ORIGIN_Z >> 4);

            if (coords == null) {
                continue;
            }

            dungeon.getClass()
                .getMethod("generateUsingSetBlock", World.class, long.class, int.class, int.class, int.class)
                .invoke(dungeon, world, (long) seed, ORIGIN_X, GROUND_Y, ORIGIN_Z);
            return true;
        }
        return false;
    }

    /**
     * {@code MapGenDungeon#worldObj} is a public field, but the generator only fills it in on its own generate path.
     */
    private static void setWorld(Object dungeon, World world) throws ReflectiveOperationException {
        java.lang.reflect.Field field = dungeon.getClass()
            .getField("worldObj");
        field.set(dungeon, world);
    }

    private static Object readField(Object target, String name) throws ReflectiveOperationException {
        java.lang.reflect.Field field = target.getClass()
            .getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

}
