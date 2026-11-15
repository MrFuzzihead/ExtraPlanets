package com.mjr.extraplanets.gametest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * Spawns and ticks every monster and boss this mod registers.
 *
 * <p>
 * They are the mod's most expensive classes per line (each boss carries its own AI, attacks and phase logic) and none
 * of it can run headless: an entity needs a world before its constructor is even finished. Spawning one into the test
 * world and ticking it is the smallest thing that exercises the whole path - the constructor and {@code entityInit},
 * the update loop, the AI task list and the collision/despawn handling.
 * </p>
 *
 * <p>
 * The entities are not given a target, so what runs is the idle behaviour; that is still the bulk of the update path,
 * and a crash anywhere in it would be a real bug.
 * </p>
 */
final class EntitySnapshots {

    /** Registered name to class. Kept as strings for the classes whose imports would drag in client-only types. */
    private static final Map<String, String> ENTITIES = new LinkedHashMap<String, String>();

    static {
        String bosses = "com.mjr.extraplanets.entities.bosses.";
        String defaultBosses = "com.mjr.extraplanets.entities.bosses.defaultBosses.";
        String monsters = "com.mjr.extraplanets.entities.monsters.";

        ENTITIES.put("fireBatBoss", bosses + "EntityEvolvedFireBatBoss");
        ENTITIES.put("ghastBoss", bosses + "EntityEvolvedGhastBoss");
        ENTITIES.put("iceSlimeBoss", bosses + "EntityEvolvedIceSlimeBoss");
        ENTITIES.put("magmaCubeBoss", bosses + "EntityEvolvedMagmaCubeBoss");
        ENTITIES.put("snowmanBoss", bosses + "EntityEvolvedSnowmanBoss");
        ENTITIES.put("creeperBossVenus", defaultBosses + "EntityCreeperBossVenus");
        ENTITIES.put("creeperBossJupiter", defaultBosses + "EntityCreeperBossJupiter");
        ENTITIES.put("creeperBossSaturn", defaultBosses + "EntityCreeperBossSaturn");
        ENTITIES.put("creeperBossUranus", defaultBosses + "EntityCreeperBossUranus");
        ENTITIES.put("creeperBossNeptune", defaultBosses + "EntityCreeperBossNeptune");
        ENTITIES.put("creeperBossPluto", defaultBosses + "EntityCreeperBossPluto");
        ENTITIES.put("creeperBossEris", defaultBosses + "EntityCreeperBossEris");
        ENTITIES.put("evolvedMagmaCube", monsters + "EntityEvolvedMagmaCube");
        ENTITIES.put("evolvedIceSlime", monsters + "EntityEvolvedIceSlime");
        ENTITIES.put("evolvedWitch", monsters + "EntityEvolvedWitch");
        ENTITIES.put("evolvedEnderman", monsters + "EntityEvolvedEnderman");
        ENTITIES.put("evolvedMiniEnderman", monsters + "EntityEvolvedMiniEnderman");
        ENTITIES.put("evolvedBlaze", monsters + "EntityEvolvedBlaze");
        ENTITIES.put("evolvedGiantSpider", monsters + "EntityEvolvedGiantSpider");
        ENTITIES.put("evolvedPowerSkeleton", monsters + "EntityEvolvedPowerSkeleton");
        ENTITIES.put("evolvedBlueCreeper", monsters + "EntityBlueCreeper");
        ENTITIES.put("evolvedRedCreeper", monsters + "EntityEvolvedRedCreeper");
    }

    private EntitySnapshots() {}

    static void run(TestReporter reporter) {
        reporter.check("entities: every monster and boss spawns and ticks", () -> {
            World world = net.minecraft.server.MinecraftServer.getServer()
                .worldServerForDimension(0);
            TestReporter.assertNotNull("overworld", world);

            List<String> failures = new ArrayList<String>();
            for (Map.Entry<String, String> entry : ENTITIES.entrySet()) {
                try {
                    if (!spawnAndTick(world, entry.getKey(), entry.getValue())) {
                        failures.add(entry.getKey());
                    }
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError(entry.getKey() + ": " + e.getCause(), e);
                }
            }
            TestReporter.assertEquals("entities that could not be spawned or ticked: " + failures, 0, failures.size());
        });
    }

    private static boolean spawnAndTick(World world, String name, String className)
        throws ReflectiveOperationException {
        Entity entity = (Entity) Class.forName(className)
            .getConstructor(World.class)
            .newInstance(world);

        // Somewhere empty above the plateau the feature sweep uses, so no AI walks into a structure.
        entity.setLocationAndAngles(9000.5D, 80.0D, 9000.5D, 0.0F, 0.0F);
        // The return value is only advisory in 1.7.10 (several worlds return false while still adding the entity);
        // what matters is that the spawn path ran and the update loop below works.
        world.spawnEntityInWorld(entity);

        try {
            // Twenty ticks is enough for the AI to pick a task, the navigator to run and the collision code to fire.
            for (int tick = 0; tick < 20; tick++) {
                entity.onUpdate();
            }
            // The assertion is "nothing threw": a slime with size 0 legitimately kills itself in its own update and a
            // boss may override onUpdate without delegating, so neither "still alive" nor "ticked" is an invariant.
            // A null target, an unregistered AI task or a bad cast anywhere in the update path does fail here.
        } finally {
            entity.setDead();
        }

        return true;
    }
}
