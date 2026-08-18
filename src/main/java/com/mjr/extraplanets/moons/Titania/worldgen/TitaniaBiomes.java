package com.mjr.extraplanets.moons.Titania.worldgen;

import net.minecraft.world.biome.BiomeGenBase;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.ExtraPlanets_Biomes;

import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSpider;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedZombie;

public class TitaniaBiomes extends BiomeGenBase {

    public static final BiomeGenBase titania = ExtraPlanets_Biomes.getBiome(
        "titania", Config.titaniaBiomeID, biomeID -> new BiomeGenTitania(biomeID).setBiomeName("titania"));

    @SuppressWarnings("unchecked")
    TitaniaBiomes(int var1) {
        super(var1);
        this.spawnableMonsterList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableMonsterList.add(new SpawnListEntry(EntityEvolvedZombie.class, 10, 4, 4));
        this.spawnableMonsterList.add(new SpawnListEntry(EntityEvolvedSpider.class, 10, 4, 4));
        this.rainfall = 0F;
    }

    @Override
    public TitaniaBiomes setColor(int var1) {
        return (TitaniaBiomes) super.setColor(var1);
    }

    @Override
    public float getSpawningChance() {
        return 0.01F;
    }
}
