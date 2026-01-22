package com.mjr.extraplanets.moons.Iapetus.worldgen;

import net.minecraft.world.biome.BiomeGenBase;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldChunkManagerSpace;

public class WorldChunkManagerIapetus extends WorldChunkManagerSpace {

    @Override
    public BiomeGenBase getBiome() {
        return IapetusBiomes.iapetus;
    }

}
