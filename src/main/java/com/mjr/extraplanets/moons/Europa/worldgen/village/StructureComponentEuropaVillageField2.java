package com.mjr.extraplanets.moons.Europa.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.worldgen.village.StructureComponentVillage;
import com.mjr.extraplanets.worldgen.village.StructureComponentVillageField2;
import com.mjr.extraplanets.worldgen.village.StructureComponentVillageStartPiece;

public class StructureComponentEuropaVillageField2 extends StructureComponentVillageField2 {

    public StructureComponentEuropaVillageField2() {}

    public StructureComponentEuropaVillageField2(StructureComponentVillageStartPiece par1ComponentVillageStartPiece,
        int par2, Random par3Random, StructureBoundingBox par4StructureBoundingBox, int par5) {
        super(par1ComponentVillageStartPiece, par2, par3Random, par4StructureBoundingBox, par5);
    }

    @SuppressWarnings("rawtypes")
    public static StructureComponentEuropaVillageField2 func_74900_a(
        StructureComponentVillageStartPiece par0ComponentVillageStartPiece, List par1List, Random par2Random, int par3,
        int par4, int par5, int par6, int par7) {
        final StructureBoundingBox structureboundingbox = StructureBoundingBox
            .getComponentToAddBoundingBox(par3, par4, par5, 0, 0, 0, 13, 4, 9, par6);
        return StructureComponentVillage.canVillageGoDeeper(structureboundingbox)
            && StructureComponent.findIntersecting(par1List, structureboundingbox) == null
                ? new StructureComponentEuropaVillageField2(
                    par0ComponentVillageStartPiece,
                    par7,
                    par2Random,
                    structureboundingbox,
                    par6)
                : null;
    }
}
