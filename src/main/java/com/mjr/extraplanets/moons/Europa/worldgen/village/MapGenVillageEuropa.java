package com.mjr.extraplanets.moons.Europa.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.moons.Europa.worldgen.BiomeGenEuropa;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillageEuropa extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays.asList(new BiomeGenBase[] { BiomeGenEuropa.europa });
    private static boolean initialized;

    static {
        try {
            MapGenVillageEuropa.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Europa village structures failed to register; villages will not generate", e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillageEuropa.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartEuropa.class, "EuropaVillage");
            MapGenStructureIO.func_143031_a(StructureComponentEuropaVillageField.class, "EuropaField1");
            MapGenStructureIO.func_143031_a(StructureComponentEuropaVillageField2.class, "EuropaField2");
            MapGenStructureIO.func_143031_a(StructureComponentEuropaVillageHouse.class, "EuropaHouse");
            MapGenStructureIO.func_143031_a(StructureComponentEuropaVillageRoadPiece.class, "EuropaRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentEuropaVillagePathGen.class, "EuropaPath");
            MapGenStructureIO.func_143031_a(StructureComponentEuropaVillageTorch.class, "EuropaTorch");
            MapGenStructureIO.func_143031_a(StructureComponentEuropaVillageStartPiece.class, "EuropaWell");
            MapGenStructureIO.func_143031_a(StructureComponentEuropaVillageWoodHut.class, "EuropaWoodHut");
        }
        MapGenVillageEuropa.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartEuropa(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "EuropaVillage";
    }
}
