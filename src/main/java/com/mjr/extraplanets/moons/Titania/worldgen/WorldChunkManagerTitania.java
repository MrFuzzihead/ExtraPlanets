package com.mjr.extraplanets.moons.Titania.worldgen;

import net.minecraft.world.biome.BiomeGenBase;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldChunkManagerSpace;

public class WorldChunkManagerTitania extends WorldChunkManagerSpace {

    @Override
    public BiomeGenBase getBiome() {
        return TitaniaBiomes.titania;
    }

}
