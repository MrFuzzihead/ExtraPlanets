package com.mjr.extraplanets.planets.Eris.worldgen;

import net.minecraft.world.biome.BiomeGenBase;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldChunkManagerSpace;

public class WorldChunkManagerEris extends WorldChunkManagerSpace {

    @Override
    public BiomeGenBase getBiome() {
        return ErisBiomes.eris;
    }

}
