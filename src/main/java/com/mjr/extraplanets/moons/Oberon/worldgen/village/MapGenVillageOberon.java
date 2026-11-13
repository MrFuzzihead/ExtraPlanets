package com.mjr.extraplanets.moons.Oberon.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.moons.Oberon.worldgen.biomes.BiomeGenOberon;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillageOberon extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays.asList(new BiomeGenBase[] { BiomeGenOberon.oberon });
    private static boolean initialized;

    static {
        try {
            MapGenVillageOberon.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Oberon village structures failed to register; villages will not generate", e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillageOberon.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartOberon.class, "OberonVillage");
            MapGenStructureIO.func_143031_a(StructureComponentOberonVillageField.class, "OberonField1");
            MapGenStructureIO.func_143031_a(StructureComponentOberonVillageField2.class, "OberonField2");
            MapGenStructureIO.func_143031_a(StructureComponentOberonVillageHouse.class, "OberonHouse");
            MapGenStructureIO.func_143031_a(StructureComponentOberonVillageRoadPiece.class, "OberonRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentOberonVillagePathGen.class, "OberonPath");
            MapGenStructureIO.func_143031_a(StructureComponentOberonVillageTorch.class, "OberonTorch");
            MapGenStructureIO.func_143031_a(StructureComponentOberonVillageStartPiece.class, "OberonWell");
            MapGenStructureIO.func_143031_a(StructureComponentOberonVillageWoodHut.class, "OberonWoodHut");
        }
        MapGenVillageOberon.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartOberon(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "OberonVillage";
    }
}
