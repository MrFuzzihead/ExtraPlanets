package com.mjr.extraplanets.moons.Rhea.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.util.MathHelper;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.worldgen.village.StructureComponentVillagePathGen;
import com.mjr.extraplanets.worldgen.village.StructureComponentVillageStartPiece;

public class StructureComponentRheaVillagePathGen extends StructureComponentVillagePathGen {

    public StructureComponentRheaVillagePathGen() {}

    public StructureComponentRheaVillagePathGen(StructureComponentVillageStartPiece par1ComponentVillageStartPiece,
        int par2, Random par3Random, StructureBoundingBox par4StructureBoundingBox, int par5) {
        super(par1ComponentVillageStartPiece, par2, par3Random, par4StructureBoundingBox, par5);
    }

    @SuppressWarnings("rawtypes")
    public static StructureBoundingBox func_74933_a(StructureComponentVillageStartPiece par0ComponentVillageStartPiece,
        List par1List, Random par2Random, int par3, int par4, int par5, int par6) {
        for (int var7 = 7 * MathHelper.getRandomIntegerInRange(par2Random, 3, 5); var7 >= 7; var7 -= 7) {
            final StructureBoundingBox var8 = StructureBoundingBox
                .getComponentToAddBoundingBox(par3, par4, par5, 0, 0, 0, 3, 3, var7, par6);
            if (StructureComponent.findIntersecting(par1List, var8) == null) {
                return var8;
            }
        }
        return null;
    }
}
