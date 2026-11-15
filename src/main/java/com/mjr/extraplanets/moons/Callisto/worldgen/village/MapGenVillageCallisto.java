package com.mjr.extraplanets.moons.Callisto.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.moons.Callisto.worldgen.BiomeGenCallisto;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillageCallisto extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays
        .asList(new BiomeGenBase[] { BiomeGenCallisto.callisto });
    private static boolean initialized;

    static {
        try {
            MapGenVillageCallisto.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Callisto village structures failed to register; villages will not generate", e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillageCallisto.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartCallisto.class, "CallistoVillage");
            MapGenStructureIO.func_143031_a(StructureComponentCallistoVillageField.class, "CallistoField1");
            MapGenStructureIO.func_143031_a(StructureComponentCallistoVillageField2.class, "CallistoField2");
            MapGenStructureIO.func_143031_a(StructureComponentCallistoVillageHouse.class, "CallistoHouse");
            MapGenStructureIO.func_143031_a(StructureComponentCallistoVillageRoadPiece.class, "CallistoRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentCallistoVillagePathGen.class, "CallistoPath");
            MapGenStructureIO.func_143031_a(StructureComponentCallistoVillageTorch.class, "CallistoTorch");
            MapGenStructureIO.func_143031_a(StructureComponentCallistoVillageStartPiece.class, "CallistoWell");
            MapGenStructureIO.func_143031_a(StructureComponentCallistoVillageWoodHut.class, "CallistoWoodHut");
        }
        MapGenVillageCallisto.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartCallisto(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "CallistoVillage";
    }
}
