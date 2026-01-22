package com.mjr.extraplanets.moons.Callisto.worldgen;

import net.minecraft.world.biome.BiomeGenBase;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldChunkManagerSpace;

public class WorldChunkManagerCallisto extends WorldChunkManagerSpace {

    @Override
    public BiomeGenBase getBiome() {
        return CallistoBiomes.callisto;
    }

}
