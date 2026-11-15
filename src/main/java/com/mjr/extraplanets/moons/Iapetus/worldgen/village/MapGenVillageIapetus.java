package com.mjr.extraplanets.moons.Iapetus.worldgen.village;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.moons.Iapetus.worldgen.BiomeGenIapetus;
import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.util.MessageUtilities;
import com.mjr.extraplanets.world.village.MapGenVillageBase;

public class MapGenVillageIapetus extends MapGenVillageBase {

    public static List<BiomeGenBase> villageSpawnBiomes = Arrays.asList(new BiomeGenBase[] { BiomeGenIapetus.iapetus });
    private static boolean initialized;

    static {
        try {
            MapGenVillageIapetus.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Iapetus village structures failed to register; villages will not generate", e);
        }
    }

    public static void initiateStructures() throws Throwable {
        if (!MapGenVillageIapetus.initialized) {
            MapGenStructureIO.registerStructure(StructureVillageStartIapetus.class, "IapetusVillage");
            MapGenStructureIO.func_143031_a(StructureComponentIapetusVillageField.class, "IapetusField1");
            MapGenStructureIO.func_143031_a(StructureComponentIapetusVillageField2.class, "IapetusField2");
            MapGenStructureIO.func_143031_a(StructureComponentIapetusVillageHouse.class, "IapetusHouse");
            MapGenStructureIO.func_143031_a(StructureComponentIapetusVillageRoadPiece.class, "IapetusRoadPiece");
            MapGenStructureIO.func_143031_a(StructureComponentIapetusVillagePathGen.class, "IapetusPath");
            MapGenStructureIO.func_143031_a(StructureComponentIapetusVillageTorch.class, "IapetusTorch");
            MapGenStructureIO.func_143031_a(StructureComponentIapetusVillageStartPiece.class, "IapetusWell");
            MapGenStructureIO.func_143031_a(StructureComponentIapetusVillageWoodHut.class, "IapetusWoodHut");
        }
        MapGenVillageIapetus.initialized = true;
    }

    @Override
    protected StructureStart getStructureStart(int par1, int par2) {
        if (Config.debugMode)
            MessageUtilities.debugMessageToLog("Generating Village at x" + par1 * 16 + " z" + par2 * 16);
        return new StructureVillageStartIapetus(this.worldObj, this.rand, par1, par2, this.terrainType);
    }

    @Override
    public String func_143025_a() {
        return "IapetusVillage";
    }
}
