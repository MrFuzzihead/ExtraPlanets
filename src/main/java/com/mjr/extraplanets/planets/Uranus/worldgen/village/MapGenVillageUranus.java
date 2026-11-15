package com.mjr.extraplanets.planets.Uranus.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.planets.Uranus.worldgen.UranusBiomes;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillageUranus extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays.asList(new BiomeGenBase[] { UranusBiomes.uranus });
    private static boolean initialized;

    static {
        try {
            MapGenVillageUranus.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Uranus village structures failed to register; villages will not generate", e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillageUranus.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartUranus.class, "UranusVillage");
            MapGenStructureIO.func_143031_a(StructureComponentUranusVillageField.class, "UranusField1");
            MapGenStructureIO.func_143031_a(StructureComponentUranusVillageField2.class, "UranusField2");
            MapGenStructureIO.func_143031_a(StructureComponentUranusVillageHouse.class, "UranusHouse");
            MapGenStructureIO.func_143031_a(StructureComponentUranusVillageRoadPiece.class, "UranusRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentUranusVillagePathGen.class, "UranusPath");
            MapGenStructureIO.func_143031_a(StructureComponentUranusVillageTorch.class, "UranusTorch");
            MapGenStructureIO.func_143031_a(StructureComponentUranusVillageStartPiece.class, "UranusWell");
            MapGenStructureIO.func_143031_a(StructureComponentUranusVillageWoodHut.class, "UranusWoodHut");
        }
        MapGenVillageUranus.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartUranus(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "UranusVillage";
    }
}
