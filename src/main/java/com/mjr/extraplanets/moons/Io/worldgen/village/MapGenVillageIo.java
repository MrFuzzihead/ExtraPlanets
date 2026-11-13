package com.mjr.extraplanets.moons.Io.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.moons.Io.worldgen.biomes.BiomeGenIo;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillageIo extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays.asList(new BiomeGenBase[] { BiomeGenIo.io });
    private static boolean initialized;

    static {
        try {
            MapGenVillageIo.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Io village structures failed to register; villages will not generate", e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillageIo.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartIo.class, "IoVillage");
            MapGenStructureIO.func_143031_a(StructureComponentIoVillageField.class, "IoField1");
            MapGenStructureIO.func_143031_a(StructureComponentIoVillageField2.class, "IoField2");
            MapGenStructureIO.func_143031_a(StructureComponentIoVillageHouse.class, "IoHouse");
            MapGenStructureIO.func_143031_a(StructureComponentIoVillageRoadPiece.class, "IoRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentIoVillagePathGen.class, "IoPath");
            MapGenStructureIO.func_143031_a(StructureComponentIoVillageTorch.class, "IoTorch");
            MapGenStructureIO.func_143031_a(StructureComponentIoVillageStartPiece.class, "IoWell");
            MapGenStructureIO.func_143031_a(StructureComponentIoVillageWoodHut.class, "IoWoodHut");
        }
        MapGenVillageIo.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartIo(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "IoVillage";
    }
}
