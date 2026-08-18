package com.mjr.extraplanets.planets.Venus.worldgen.village;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.blocks.ExtraPlanets_Blocks;
import com.mjr.extraplanets.worldgen.village.StructureComponentVillage;
import com.mjr.extraplanets.worldgen.village.StructureComponentVillagePathGen;
import com.mjr.extraplanets.worldgen.village.StructureComponentVillageStartPiece;
import com.mjr.extraplanets.worldgen.village.StructureComponentVillageTorch;
import com.mjr.extraplanets.worldgen.village.StructureVillagePieceWeight;

public class StructureComponentVenusVillageStartPiece extends StructureComponentVillageStartPiece {

    public StructureComponentVenusVillageStartPiece() {
        this.planetBlock = ExtraPlanets_Blocks.venusBlocks;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public StructureComponentVenusVillageStartPiece(WorldChunkManager par1WorldChunkManager, int par2,
        Random par3Random, int par4, int par5, ArrayList<StructureVillagePieceWeightVenus> par6ArrayList, int par7) {
        super(par1WorldChunkManager, par2, par3Random, par4, par5, (ArrayList) par6ArrayList, par7);
        this.planetBlock = ExtraPlanets_Blocks.venusBlocks;
    }

    @Override
    public StructureComponentVillage constructComponent(StructureVillagePieceWeight par1StructureVillagePieceWeight,
        List<StructureComponent> par2List, Random par3Random, int par4, int par5, int par6, int par7, int par8) {
        final Class<?> var9 = par1StructureVillagePieceWeight.villagePieceClass;

        if (var9 == StructureComponentVenusVillageWoodHut.class) {
            return StructureComponentVenusVillageWoodHut
                .func_74908_a(this, par2List, par3Random, par4, par5, par6, par7, par8);
        } else if (var9 == StructureComponentVenusVillageField.class) {
            return StructureComponentVenusVillageField
                .func_74900_a(this, par2List, par3Random, par4, par5, par6, par7, par8);
        } else if (var9 == StructureComponentVenusVillageHouse.class) {
            return StructureComponentVenusVillageHouse
                .func_74921_a(this, par2List, par3Random, par4, par5, par6, par7, par8);
        }

        return null;
    }

    @Override
    public StructureComponentVillageTorch constructTorch(int par1, Random par2Random,
        StructureBoundingBox par3StructureBoundingBox, int par4) {
        return new StructureComponentVenusVillageTorch(this, par1, par2Random, par3StructureBoundingBox, par4);
    }

    @Override
    public StructureComponentVillagePathGen constructPath(int par1, Random par2Random,
        StructureBoundingBox par3StructureBoundingBox, int par4) {
        return new StructureComponentVenusVillagePathGen(this, par1, par2Random, par3StructureBoundingBox, par4);
    }
}
