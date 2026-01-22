package com.mjr.extraplanets.moons.Phobos.worldgen;

import net.minecraft.world.biome.BiomeGenBase;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldChunkManagerSpace;

public class WorldChunkManagerPhobos extends WorldChunkManagerSpace {

    @Override
    public BiomeGenBase getBiome() {
        return PhobosBiomes.phobos;
    }

}
