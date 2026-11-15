package com.mjr.extraplanets.moons.Triton.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.moons.Triton.worldgen.biomes.BiomeGenTriton;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillageTriton extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays.asList(new BiomeGenBase[] { BiomeGenTriton.triton });
    private static boolean initialized;

    static {
        try {
            MapGenVillageTriton.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Triton village structures failed to register; villages will not generate", e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillageTriton.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartTriton.class, "TritonVillage");
            MapGenStructureIO.func_143031_a(StructureComponentTritonVillageField.class, "TritonField1");
            MapGenStructureIO.func_143031_a(StructureComponentTritonVillageField2.class, "TritonField2");
            MapGenStructureIO.func_143031_a(StructureComponentTritonVillageHouse.class, "TritonHouse");
            MapGenStructureIO.func_143031_a(StructureComponentTritonVillageRoadPiece.class, "TritonRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentTritonVillagePathGen.class, "TritonPath");
            MapGenStructureIO.func_143031_a(StructureComponentTritonVillageTorch.class, "TritonTorch");
            MapGenStructureIO.func_143031_a(StructureComponentTritonVillageStartPiece.class, "TritonWell");
            MapGenStructureIO.func_143031_a(StructureComponentTritonVillageWoodHut.class, "TritonWoodHut");
        }
        MapGenVillageTriton.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartTriton(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "TritonVillage";
    }
}
