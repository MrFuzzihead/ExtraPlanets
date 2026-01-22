package com.mjr.extraplanets.moons.Deimos.worldgen;

import net.minecraft.world.biome.BiomeGenBase;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldChunkManagerSpace;

public class WorldChunkManagerDeimos extends WorldChunkManagerSpace {

    @Override
    public BiomeGenBase getBiome() {
        return DeimosBiomes.deimos;
    }

}
