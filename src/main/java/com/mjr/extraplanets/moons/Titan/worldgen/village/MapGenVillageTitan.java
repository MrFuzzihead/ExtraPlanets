package com.mjr.extraplanets.moons.Titan.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.moons.Titan.worldgen.biomes.BiomeGenTitan;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillageTitan extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays.asList(new BiomeGenBase[] { BiomeGenTitan.titan });
    private static boolean initialized;

    static {
        try {
            MapGenVillageTitan.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Titan village structures failed to register; villages will not generate", e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillageTitan.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartTitan.class, "TitanVillage");
            MapGenStructureIO.func_143031_a(StructureComponentTitanVillageField.class, "TitanField1");
            MapGenStructureIO.func_143031_a(StructureComponentTitanVillageField2.class, "TitanField2");
            MapGenStructureIO.func_143031_a(StructureComponentTitanVillageHouse.class, "TitanHouse");
            MapGenStructureIO.func_143031_a(StructureComponentTitanVillageRoadPiece.class, "TitanRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentTitanVillagePathGen.class, "TitanPath");
            MapGenStructureIO.func_143031_a(StructureComponentTitanVillageTorch.class, "TitanTorch");
            MapGenStructureIO.func_143031_a(StructureComponentTitanVillageStartPiece.class, "TitanWell");
            MapGenStructureIO.func_143031_a(StructureComponentTitanVillageWoodHut.class, "TitanWoodHut");
        }
        MapGenVillageTitan.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartTitan(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "TitanVillage";
    }
}
