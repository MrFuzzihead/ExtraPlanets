package com.mjr.extraplanets.planets.Jupiter.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.planets.Jupiter.worldgen.JupiterBiomes;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillageJupiter extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays.asList(new BiomeGenBase[] { JupiterBiomes.jupiter });
    private static boolean initialized;

    static {
        try {
            MapGenVillageJupiter.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Jupiter village structures failed to register; villages will not generate", e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillageJupiter.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartJupiter.class, "JupiterVillage");
            MapGenStructureIO.func_143031_a(StructureComponentJupiterVillageField.class, "JupiterField1");
            MapGenStructureIO.func_143031_a(StructureComponentJupiterVillageField2.class, "JupiterField2");
            MapGenStructureIO.func_143031_a(StructureComponentJupiterVillageHouse.class, "JupiterHouse");
            MapGenStructureIO.func_143031_a(StructureComponentJupiterVillageRoadPiece.class, "JupiterRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentJupiterVillagePathGen.class, "JupiterPath");
            MapGenStructureIO.func_143031_a(StructureComponentJupiterVillageTorch.class, "JupiterTorch");
            MapGenStructureIO.func_143031_a(StructureComponentJupiterVillageStartPiece.class, "JupiterWell");
            MapGenStructureIO.func_143031_a(StructureComponentJupiterVillageWoodHut.class, "JupiterWoodHut");
        }
        MapGenVillageJupiter.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartJupiter(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "JupiterVillage";
    }
}
