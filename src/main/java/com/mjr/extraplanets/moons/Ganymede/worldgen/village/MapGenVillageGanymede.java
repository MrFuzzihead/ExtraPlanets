package com.mjr.extraplanets.moons.Ganymede.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.moons.Ganymede.worldgen.BiomeGenGanymede;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillageGanymede extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays
        .asList(new BiomeGenBase[] { BiomeGenGanymede.ganymede });
    private static boolean initialized;

    static {
        try {
            MapGenVillageGanymede.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Ganymede village structures failed to register; villages will not generate", e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillageGanymede.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartGanymede.class, "GanymedeVillage");
            MapGenStructureIO.func_143031_a(StructureComponentGanymedeVillageField.class, "GanymedeField1");
            MapGenStructureIO.func_143031_a(StructureComponentGanymedeVillageField2.class, "GanymedeField2");
            MapGenStructureIO.func_143031_a(StructureComponentGanymedeVillageHouse.class, "GanymedeHouse");
            MapGenStructureIO.func_143031_a(StructureComponentGanymedeVillageRoadPiece.class, "GanymedeRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentGanymedeVillagePathGen.class, "GanymedePath");
            MapGenStructureIO.func_143031_a(StructureComponentGanymedeVillageTorch.class, "GanymedeTorch");
            MapGenStructureIO.func_143031_a(StructureComponentGanymedeVillageStartPiece.class, "GanymedeWell");
            MapGenStructureIO.func_143031_a(StructureComponentGanymedeVillageWoodHut.class, "GanymedeWoodHut");
        }
        MapGenVillageGanymede.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartGanymede(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "GanymedeVillage";
    }
}
