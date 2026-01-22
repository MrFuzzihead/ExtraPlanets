package com.mjr.extraplanets.moons.Ganymede.worldgen;

import net.minecraft.world.biome.BiomeGenBase;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldChunkManagerSpace;

public class WorldChunkManagerGanymede extends WorldChunkManagerSpace {

    @Override
    public BiomeGenBase getBiome() {
        return GanymedeBiomes.ganymede;
    }

}
