package com.mjr.extraplanets.moons.Titania.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.moons.Titania.worldgen.BiomeGenTitania;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillageTitania extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays.asList(new BiomeGenBase[] { BiomeGenTitania.titania });
    private static boolean initialized;

    static {
        try {
            MapGenVillageTitania.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Titania village structures failed to register; villages will not generate", e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillageTitania.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartTitania.class, "TitaniaVillage");
            MapGenStructureIO.func_143031_a(StructureComponentTitaniaVillageField.class, "TitaniaField1");
            MapGenStructureIO.func_143031_a(StructureComponentTitaniaVillageField2.class, "TitaniaField2");
            MapGenStructureIO.func_143031_a(StructureComponentTitaniaVillageHouse.class, "TitaniaHouse");
            MapGenStructureIO.func_143031_a(StructureComponentTitaniaVillageRoadPiece.class, "TitaniaRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentTitaniaVillagePathGen.class, "TitaniaPath");
            MapGenStructureIO.func_143031_a(StructureComponentTitaniaVillageTorch.class, "TitaniaTorch");
            MapGenStructureIO.func_143031_a(StructureComponentTitaniaVillageStartPiece.class, "TitaniaWell");
            MapGenStructureIO.func_143031_a(StructureComponentTitaniaVillageWoodHut.class, "TitaniaWoodHut");
        }
        MapGenVillageTitania.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartTitania(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "TitaniaVillage";
    }
}
