package com.mjr.extraplanets.moons.Triton.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.worldgen.village.StructureComponentVillageField;
import com.mjr.extraplanets.worldgen.village.StructureComponentVillageStartPiece;

public class StructureComponentTritonVillageField extends StructureComponentVillageField {

    public StructureComponentTritonVillageField() {}

    public StructureComponentTritonVillageField(StructureComponentVillageStartPiece par1ComponentVillageStartPiece,
        int par2, Random par3Random, StructureBoundingBox par4StructureBoundingBox, int par5) {
        super(par1ComponentVillageStartPiece, par2, par3Random, par4StructureBoundingBox, par5);
    }

    @SuppressWarnings("rawtypes")
    public static StructureComponentTritonVillageField func_74900_a(
        StructureComponentVillageStartPiece par0ComponentVillageStartPiece, List par1List, Random par2Random, int par3,
        int par4, int par5, int par6, int par7) {
        final StructureBoundingBox var8 = StructureBoundingBox
            .getComponentToAddBoundingBox(par3, par4, par5, 0, 0, 0, 13, 4, 9, par6);
        return StructureComponent.findIntersecting(par1List, var8) == null
            ? new StructureComponentTritonVillageField(par0ComponentVillageStartPiece, par7, par2Random, var8, par6)
            : null;
    }
}
