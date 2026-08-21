package com.mjr.extraplanets.moons.Iapetus.worldgen;

import net.minecraft.world.biome.BiomeGenBase;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.ExtraPlanets_Biomes;

import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSpider;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedZombie;

public class IapetusBiomes extends BiomeGenBase {

    public static final BiomeGenBase iapetus = ExtraPlanets_Biomes
        .getBiome("iapetus", Config.iapetusBiomeID, biomeID -> new BiomeGenIapetus(biomeID).setBiomeName("iapetus"));

    @SuppressWarnings("unchecked")
    IapetusBiomes(int var1) {
        super(var1);
        this.spawnableMonsterList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableMonsterList.add(new SpawnListEntry(EntityEvolvedZombie.class, 10, 4, 4));
        this.spawnableMonsterList.add(new SpawnListEntry(EntityEvolvedSpider.class, 10, 4, 4));
        this.rainfall = 0F;
    }

    @Override
    public IapetusBiomes setColor(int var1) {
        return (IapetusBiomes) super.setColor(var1);
    }

    @Override
    public float getSpawningChance() {
        return 0.01F;
    }
}
