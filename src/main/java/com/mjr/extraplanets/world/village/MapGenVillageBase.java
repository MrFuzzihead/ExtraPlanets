package com.mjr.extraplanets.world.village;

import java.util.Random;

import net.minecraft.world.gen.structure.MapGenStructure;

/**
 * The parts of a village map generator that are the same for every body.
 *
 * <p>
 * ExtraPlanets ships one {@code MapGenVillage<Body>} per body (see {@code CODE_REVIEW.md} §6.3). The spawn grid below
 * was byte-identical in all 22: a village is placed on a 32-chunk grid with an 8-chunk jitter, salted with 10387312,
 * which is the vanilla rule. What stays per body is the registration, the {@code StructureStart} it builds and its
 * registration name.
 * </p>
 *
 * <p>
 * Note that no biome check happens here - the per-body {@code villageSpawnBiomes} list is never read, so villages
 * generate on every biome of every body. That is existing behaviour, preserved deliberately: changing it would move
 * every village in every existing world.
 * </p>
 */
public abstract class MapGenVillageBase extends MapGenStructure {

    /** Always 0 in ExtraPlanets; kept as a field because the start's constructor takes it. */
    protected final int terrainType = 0;

    @Override
    protected boolean canSpawnStructureAtCoords(int i, int j) {
        final byte numChunks = 32; // 32
        final byte offsetChunks = 8; // 8
        final int oldi = i;
        final int oldj = j;

        if (i < 0) {
            i -= numChunks - 1;
        }

        if (j < 0) {
            j -= numChunks - 1;
        }

        int randX = i / numChunks;
        int randZ = j / numChunks;
        final Random var7 = this.worldObj.setRandomSeed(i, j, 10387312);
        randX *= numChunks;
        randZ *= numChunks;
        randX += var7.nextInt(numChunks - offsetChunks);
        randZ += var7.nextInt(numChunks - offsetChunks);
        return oldi == randX && oldj == randZ;
    }
}
