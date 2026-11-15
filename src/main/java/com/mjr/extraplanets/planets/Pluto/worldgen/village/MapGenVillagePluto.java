package com.mjr.extraplanets.planets.Pluto.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.planets.Pluto.worldgen.BiomeGenPluto;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillagePluto extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays.asList(new BiomeGenBase[] { BiomeGenPluto.pluto });
    private static boolean initialized;

    static {
        try {
            MapGenVillagePluto.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Pluto village structures failed to register; villages will not generate", e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillagePluto.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartPluto.class, "PlutoVillage");
            MapGenStructureIO.func_143031_a(StructureComponentPlutoVillageField.class, "PlutoField1");
            MapGenStructureIO.func_143031_a(StructureComponentPlutoVillageField2.class, "PlutoField2");
            MapGenStructureIO.func_143031_a(StructureComponentPlutoVillageHouse.class, "PlutoHouse");
            MapGenStructureIO.func_143031_a(StructureComponentPlutoVillageRoadPiece.class, "PlutoRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentPlutoVillagePathGen.class, "PlutoPath");
            MapGenStructureIO.func_143031_a(StructureComponentPlutoVillageTorch.class, "PlutoTorch");
            MapGenStructureIO.func_143031_a(StructureComponentPlutoVillageStartPiece.class, "PlutoWell");
            MapGenStructureIO.func_143031_a(StructureComponentPlutoVillageWoodHut.class, "PlutoWoodHut");
        }
        MapGenVillagePluto.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartPluto(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "PlutoVillage";
    }
}
