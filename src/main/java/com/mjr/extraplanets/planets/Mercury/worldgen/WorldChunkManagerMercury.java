package com.mjr.extraplanets.planets.Mercury.worldgen;

import net.minecraft.world.biome.BiomeGenBase;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldChunkManagerSpace;

public class WorldChunkManagerMercury extends WorldChunkManagerSpace {

    @Override
    public BiomeGenBase getBiome() {
        return MercuryBiomes.mercury;
    }

}
