package com.mjr.extraplanets.planets.Neptune.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.planets.Neptune.worldgen.biomes.BiomeGenNeptune;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillageNeptune extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays.asList(new BiomeGenBase[] { BiomeGenNeptune.neptune });
    private static boolean initialized;

    static {
        try {
            MapGenVillageNeptune.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Neptune village structures failed to register; villages will not generate", e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillageNeptune.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartNeptune.class, "NeptuneVillage");
            MapGenStructureIO.func_143031_a(StructureComponentNeptuneVillageField.class, "NeptuneField1");
            MapGenStructureIO.func_143031_a(StructureComponentNeptuneVillageField2.class, "NeptuneField2");
            MapGenStructureIO.func_143031_a(StructureComponentNeptuneVillageHouse.class, "NeptuneHouse");
            MapGenStructureIO.func_143031_a(StructureComponentNeptuneVillageRoadPiece.class, "NeptuneRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentNeptuneVillagePathGen.class, "NeptunePath");
            MapGenStructureIO.func_143031_a(StructureComponentNeptuneVillageTorch.class, "NeptuneTorch");
            MapGenStructureIO.func_143031_a(StructureComponentNeptuneVillageStartPiece.class, "NeptuneWell");
            MapGenStructureIO.func_143031_a(StructureComponentNeptuneVillageWoodHut.class, "NeptuneWoodHut");
        }
        MapGenVillageNeptune.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartNeptune(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "NeptuneVillage";
    }
}
