package com.mjr.extraplanets.planets.Ceres.worldgen;

import net.minecraft.world.biome.BiomeGenBase;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.ExtraPlanets_Biomes;

import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSpider;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedZombie;

public class CeresBiomes extends BiomeGenBase {

    public static final BiomeGenBase ceres = ExtraPlanets_Biomes
        .getBiome("ceres", Config.ceresBiomeID, biomeID -> new BiomeGenCeres(biomeID).setBiomeName("ceres"));

    @SuppressWarnings("unchecked")
    CeresBiomes(int var1) {
        super(var1);
        this.spawnableMonsterList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableMonsterList.add(new SpawnListEntry(EntityEvolvedZombie.class, 10, 4, 4));
        this.spawnableMonsterList.add(new SpawnListEntry(EntityEvolvedSpider.class, 10, 4, 4));
        this.rainfall = 0F;
    }

    @Override
    public CeresBiomes setColor(int var1) {
        return (CeresBiomes) super.setColor(var1);
    }

    @Override
    public float getSpawningChance() {
        return 0.01F;
    }
}
