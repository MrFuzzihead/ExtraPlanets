package com.mjr.extraplanets.planets.Kepler22b.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.planets.Kepler22b.worldgen.Kepler22bBiomes;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillageKepler22b extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays
        .asList(new BiomeGenBase[] { Kepler22bBiomes.kepler22bPlains });
    private static boolean initialized;

    static {
        try {
            MapGenVillageKepler22b.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog.severe(
                "[ExtraPlanets] Kepler22b village structures failed to register; villages will not generate",
                e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillageKepler22b.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartKepler22b.class, "Kepler22bVillage");
            MapGenStructureIO.func_143031_a(StructureComponentKepler22bVillageField.class, "Kepler22bField1");
            MapGenStructureIO.func_143031_a(StructureComponentKepler22bVillageField2.class, "Kepler22bField2");
            MapGenStructureIO.func_143031_a(StructureComponentKepler22bVillageHouse.class, "Kepler22bHouse");
            MapGenStructureIO.func_143031_a(StructureComponentKepler22bVillageRoadPiece.class, "Kepler22bRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentKepler22bVillagePathGen.class, "Kepler22bPath");
            MapGenStructureIO.func_143031_a(StructureComponentKepler22bVillageTorch.class, "Kepler22bTorch");
            MapGenStructureIO.func_143031_a(StructureComponentKepler22bVillageStartPiece.class, "Kepler22bWell");
            MapGenStructureIO.func_143031_a(StructureComponentKepler22bVillageWoodHut.class, "Kepler22bWoodHut");
        }
        MapGenVillageKepler22b.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartKepler22b(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "Kepler22bVillage";
    }
}
