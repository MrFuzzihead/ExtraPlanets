package com.mjr.extraplanets.planets.Kepler22b.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.worldgen.village.StructureComponentVillageStartPiece;
import com.mjr.extraplanets.worldgen.village.StructureComponentVillageTorch;

public class StructureComponentKepler22bVillageTorch extends StructureComponentVillageTorch {

    public StructureComponentKepler22bVillageTorch() {}

    public StructureComponentKepler22bVillageTorch(StructureComponentVillageStartPiece par1ComponentVillageStartPiece,
        int par2, Random par3Random, StructureBoundingBox par4StructureBoundingBox, int par5) {
        super(par1ComponentVillageStartPiece, par2, par3Random, par4StructureBoundingBox, par5);
    }

    @SuppressWarnings("rawtypes")
    public static StructureBoundingBox func_74904_a(StructureComponentVillageStartPiece par0ComponentVillageStartPiece,
        List par1List, Random par2Random, int par3, int par4, int par5, int par6) {
        final StructureBoundingBox var7 = StructureBoundingBox
            .getComponentToAddBoundingBox(par3, par4, par5, 0, 0, 0, 3, 4, 2, par6);
        return StructureComponent.findIntersecting(par1List, var7) != null ? null : var7;
    }
}
