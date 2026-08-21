package com.mjr.extraplanets.moons.Ganymede.worldgen;

import net.minecraft.world.biome.BiomeGenBase;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.ExtraPlanets_Biomes;

import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSpider;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedZombie;

public class GanymedeBiomes extends BiomeGenBase {

    public static final BiomeGenBase ganymede = ExtraPlanets_Biomes.getBiome(
        "ganymede",
        Config.ganymedeBiomeID,
        biomeID -> new BiomeGenGanymede(biomeID).setBiomeName("ganymede"));

    @SuppressWarnings("unchecked")
    GanymedeBiomes(int var1) {
        super(var1);
        this.spawnableMonsterList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableMonsterList.add(new SpawnListEntry(EntityEvolvedZombie.class, 10, 4, 4));
        this.spawnableMonsterList.add(new SpawnListEntry(EntityEvolvedSpider.class, 10, 4, 4));
        this.rainfall = 0F;
    }

    @Override
    public GanymedeBiomes setColor(int var1) {
        return (GanymedeBiomes) super.setColor(var1);
    }

    @Override
    public float getSpawningChance() {
        return 0.01F;
    }
}
