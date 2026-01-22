package com.mjr.extraplanets.planets.Pluto.worldgen;

import net.minecraft.world.biome.BiomeGenBase;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldChunkManagerSpace;

public class WorldChunkManagerPluto extends WorldChunkManagerSpace {

    @Override
    public BiomeGenBase getBiome() {
        return PlutoBiomes.pluto;
    }

}
