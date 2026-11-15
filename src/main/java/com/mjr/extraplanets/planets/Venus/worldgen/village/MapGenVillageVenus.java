package com.mjr.extraplanets.planets.Venus.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.planets.Venus.worldgen.BiomeGenVenus;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillageVenus extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays.asList(new BiomeGenBase[] { BiomeGenVenus.venus });
    private static boolean initialized;

    static {
        try {
            MapGenVillageVenus.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Venus village structures failed to register; villages will not generate", e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillageVenus.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartVenus.class, "VenusVillage");
            MapGenStructureIO.func_143031_a(StructureComponentVenusVillageField.class, "VenusField1");
            MapGenStructureIO.func_143031_a(StructureComponentVenusVillageField2.class, "VenusField2");
            MapGenStructureIO.func_143031_a(StructureComponentVenusVillageHouse.class, "VenusHouse");
            MapGenStructureIO.func_143031_a(StructureComponentVenusVillageRoadPiece.class, "VenusRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentVenusVillagePathGen.class, "VenusPath");
            MapGenStructureIO.func_143031_a(StructureComponentVenusVillageTorch.class, "VenusTorch");
            MapGenStructureIO.func_143031_a(StructureComponentVenusVillageStartPiece.class, "VenusWell");
            MapGenStructureIO.func_143031_a(StructureComponentVenusVillageWoodHut.class, "VenusWoodHut");
        }
        MapGenVillageVenus.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartVenus(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "VenusVillage";
    }
}
