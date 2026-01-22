package com.mjr.extraplanets.moons.Europa.worldgen;

import net.minecraft.world.biome.BiomeGenBase;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldChunkManagerSpace;

public class WorldChunkManagerEuropa extends WorldChunkManagerSpace {

    @Override
    public BiomeGenBase getBiome() {
        return EuropaBiomes.europa;
    }

}
