package com.mjr.extraplanets.moons.Deimos.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.moons.Deimos.worldgen.BiomeGenDeimos;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillageDeimos extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays.asList(new BiomeGenBase[] { BiomeGenDeimos.deimos });
    private static boolean initialized;

    static {
        try {
            MapGenVillageDeimos.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Deimos village structures failed to register; villages will not generate", e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillageDeimos.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartDeimos.class, "DeimosVillage");
            MapGenStructureIO.func_143031_a(StructureComponentDeimosVillageField.class, "DeimosField1");
            MapGenStructureIO.func_143031_a(StructureComponentDeimosVillageField2.class, "DeimosField2");
            MapGenStructureIO.func_143031_a(StructureComponentDeimosVillageHouse.class, "DeimosHouse");
            MapGenStructureIO.func_143031_a(StructureComponentDeimosVillageRoadPiece.class, "DeimosRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentDeimosVillagePathGen.class, "DeimosPath");
            MapGenStructureIO.func_143031_a(StructureComponentDeimosVillageTorch.class, "DeimosTorch");
            MapGenStructureIO.func_143031_a(StructureComponentDeimosVillageStartPiece.class, "DeimosWell");
            MapGenStructureIO.func_143031_a(StructureComponentDeimosVillageWoodHut.class, "DeimosWoodHut");
        }
        MapGenVillageDeimos.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartDeimos(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "DeimosVillage";
    }
}
