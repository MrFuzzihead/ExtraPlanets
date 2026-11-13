package com.mjr.extraplanets.planets.Eris.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.planets.Eris.worldgen.BiomeGenEris;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillageEris extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays.asList(new BiomeGenBase[] { BiomeGenEris.eris });
    private static boolean initialized;

    static {
        try {
            MapGenVillageEris.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Eris village structures failed to register; villages will not generate", e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillageEris.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartEris.class, "ErisVillage");
            MapGenStructureIO.func_143031_a(StructureComponentErisVillageField.class, "ErisField1");
            MapGenStructureIO.func_143031_a(StructureComponentErisVillageField2.class, "ErisField2");
            MapGenStructureIO.func_143031_a(StructureComponentErisVillageHouse.class, "ErisHouse");
            MapGenStructureIO.func_143031_a(StructureComponentErisVillageRoadPiece.class, "ErisRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentErisVillagePathGen.class, "ErisPath");
            MapGenStructureIO.func_143031_a(StructureComponentErisVillageTorch.class, "ErisTorch");
            MapGenStructureIO.func_143031_a(StructureComponentErisVillageStartPiece.class, "ErisWell");
            MapGenStructureIO.func_143031_a(StructureComponentErisVillageWoodHut.class, "ErisWoodHut");
        }
        MapGenVillageEris.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartEris(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "ErisVillage";
    }
}
