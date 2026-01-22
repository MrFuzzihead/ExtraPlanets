package com.mjr.extraplanets.moons.Rhea.worldgen;

import net.minecraft.world.biome.BiomeGenBase;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldChunkManagerSpace;

public class WorldChunkManagerRhea extends WorldChunkManagerSpace {

    @Override
    public BiomeGenBase getBiome() {
        return RheaBiomes.rhea;
    }

}
