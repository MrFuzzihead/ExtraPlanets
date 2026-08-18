package com.mjr.extraplanets.planets.Jupiter.worldgen.village;

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

public class StructureComponentJupiterVillageStartPiece extends StructureComponentVillageStartPiece {

    public StructureComponentJupiterVillageStartPiece() {
        this.planetBlock = ExtraPlanets_Blocks.jupiterBlocks;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public StructureComponentJupiterVillageStartPiece(WorldChunkManager par1WorldChunkManager, int par2,
        Random par3Random, int par4, int par5, ArrayList<StructureVillagePieceWeightJupiter> par6ArrayList, int par7) {
        super(par1WorldChunkManager, par2, par3Random, par4, par5, (ArrayList) par6ArrayList, par7);
        this.planetBlock = ExtraPlanets_Blocks.jupiterBlocks;
    }

    @Override
    public StructureComponentVillage constructComponent(StructureVillagePieceWeight par1StructureVillagePieceWeight,
        List<StructureComponent> par2List, Random par3Random, int par4, int par5, int par6, int par7, int par8) {
        final Class<?> var9 = par1StructureVillagePieceWeight.villagePieceClass;

        if (var9 == StructureComponentJupiterVillageWoodHut.class) {
            return StructureComponentJupiterVillageWoodHut
                .func_74908_a(this, par2List, par3Random, par4, par5, par6, par7, par8);
        } else if (var9 == StructureComponentJupiterVillageField.class) {
            return StructureComponentJupiterVillageField
                .func_74900_a(this, par2List, par3Random, par4, par5, par6, par7, par8);
        } else if (var9 == StructureComponentJupiterVillageHouse.class) {
            return StructureComponentJupiterVillageHouse
                .func_74921_a(this, par2List, par3Random, par4, par5, par6, par7, par8);
        }

        return null;
    }

    @Override
    public StructureComponentVillageTorch constructTorch(int par1, Random par2Random,
        StructureBoundingBox par3StructureBoundingBox, int par4) {
        return new StructureComponentJupiterVillageTorch(this, par1, par2Random, par3StructureBoundingBox, par4);
    }

    @Override
    public StructureComponentVillagePathGen constructPath(int par1, Random par2Random,
        StructureBoundingBox par3StructureBoundingBox, int par4) {
        return new StructureComponentJupiterVillagePathGen(this, par1, par2Random, par3StructureBoundingBox, par4);
    }
}
