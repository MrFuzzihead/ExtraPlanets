package com.mjr.extraplanets.planets.Venus.worldgen;

import net.minecraft.world.biome.BiomeGenBase;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldChunkManagerSpace;

public class WorldChunkManagerVenus extends WorldChunkManagerSpace {

    @Override
    public BiomeGenBase getBiome() {
        return VenusBiomes.venus;
    }

}
