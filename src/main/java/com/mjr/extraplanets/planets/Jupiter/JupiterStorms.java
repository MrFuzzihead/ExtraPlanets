package com.mjr.extraplanets.planets.Jupiter;

import java.util.Random;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Puts lightning bolts on Jupiter.
 *
 * <p>
 * Jupiter has no weather system ({@code canRainOrSnow()} is false and the dimension never thunders), so vanilla's
 * thunder never fires here. ExtraPlanets has always had a client-side fake bolt for atmosphere; this class is the
 * shared part so the server can spawn <em>real</em> ones through the same rules.
 * </p>
 *
 * <p>
 * A bolt goes through {@link World#addWeatherEffect}: on a server that broadcasts the spawn packet to everyone within
 * 512 blocks and the bolt does the vanilla damage/fire work, and on a client it simply renders and thunders. So the
 * same call is "cosmetic" on the client and "real" on the server, which is exactly the difference the two config keys
 * describe.
 * </p>
 */
public final class JupiterStorms {

    /** How far from the target a bolt may land, in blocks. Never on top of the player. */
    private static final int MIN_DISTANCE = 16;
    private static final int MAX_DISTANCE = 64;

    /**
     * How many random spots to try before giving up. Failure only happens at the edge of the loaded world or over
     * liquid, so a handful of attempts is plenty.
     */
    private static final int ATTEMPTS = 8;

    /** Every this many world ticks the storm considers a strike: 5 seconds. */
    public static final int CHECK_INTERVAL = 100;

    /** Of those considerations, one in this many actually strikes: one bolt per 20 seconds on average. */
    public static final int ONE_IN = 4;

    private JupiterStorms() {}

    /**
     * @param totalWorldTime the world's tick counter.
     * @param enabled        {@code Config.jupiterLightingServer}.
     * @param isJupiter      whether the world's provider is the Jupiter one.
     * @param roll           a roll in {@code [0, ONE_IN)}, normally {@code world.rand.nextInt(ONE_IN)}.
     * @return true when this world tick should spawn a bolt.
     */
    public static boolean shouldStrike(long totalWorldTime, boolean enabled, boolean isJupiter, int roll) {
        return enabled && isJupiter && totalWorldTime > 0 && totalWorldTime % CHECK_INTERVAL == 0 && roll == 0;
    }

    /**
     * Strikes somewhere around the given position.
     *
     * @param world   world to strike in; may be a client world (cosmetic) or a server world (real).
     * @param centreX x to strike around.
     * @param centreZ z to strike around.
     * @param rand    the RNG to use - the world's or the provider's, never a fresh one per call.
     * @return true if a bolt was placed.
     */
    public static boolean strikeNear(World world, double centreX, double centreZ, Random rand) {
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            double angle = rand.nextDouble() * Math.PI * 2.0D;
            double distance = MIN_DISTANCE + rand.nextInt(MAX_DISTANCE - MIN_DISTANCE);
            int x = MathHelper.floor_double(centreX + Math.cos(angle) * distance);
            int z = MathHelper.floor_double(centreZ + Math.sin(angle) * distance);

            // A bolt outside the loaded world would either be lost or force a chunk load; skip those spots.
            if (!world.blockExists(x, 64, z)) {
                continue;
            }

            int y = world.getTopSolidOrLiquidBlock(x, z);
            if (y <= 0) {
                continue;
            }

            // Do not strike open liquid: the bolt would be swallowed and the fire/damage rules make no sense at sea.
            if (world.getBlock(x, y - 1, z)
                .getMaterial()
                .isLiquid()) {
                continue;
            }

            return world.addWeatherEffect(new EntityLightningBolt(world, x, y, z));
        }
        return false;
    }
}
