package com.mjr.extraplanets.planets.Mercury.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.planets.Mercury.worldgen.BiomeGenMercury;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillageMercury extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays.asList(new BiomeGenBase[] { BiomeGenMercury.mercury });
    private static boolean initialized;

    static {
        try {
            MapGenVillageMercury.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Mercury village structures failed to register; villages will not generate", e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillageMercury.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartMercury.class, "MercuryVillage");
            MapGenStructureIO.func_143031_a(StructureComponentMercuryVillageField.class, "MercuryField1");
            MapGenStructureIO.func_143031_a(StructureComponentMercuryVillageField2.class, "MercuryField2");
            MapGenStructureIO.func_143031_a(StructureComponentMercuryVillageHouse.class, "MercuryHouse");
            MapGenStructureIO.func_143031_a(StructureComponentMercuryVillageRoadPiece.class, "MercuryRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentMercuryVillagePathGen.class, "MercuryPath");
            MapGenStructureIO.func_143031_a(StructureComponentMercuryVillageTorch.class, "MercuryTorch");
            MapGenStructureIO.func_143031_a(StructureComponentMercuryVillageStartPiece.class, "MercuryWell");
            MapGenStructureIO.func_143031_a(StructureComponentMercuryVillageWoodHut.class, "MercuryWoodHut");
        }
        MapGenVillageMercury.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartMercury(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "MercuryVillage";
    }
}
