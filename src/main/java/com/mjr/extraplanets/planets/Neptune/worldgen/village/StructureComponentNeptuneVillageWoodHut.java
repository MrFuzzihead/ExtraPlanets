package com.mjr.extraplanets.planets.Neptune.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.blocks.ExtraPlanets_Blocks;
import com.mjr.extraplanets.worldgen.village.StructureComponentVillageStartPiece;
import com.mjr.extraplanets.worldgen.village.StructureComponentVillageWoodHut;

public class StructureComponentNeptuneVillageWoodHut extends StructureComponentVillageWoodHut {

    public StructureComponentNeptuneVillageWoodHut() {
        this.planetBlock = ExtraPlanets_Blocks.neptuneBlocks;
    }

    public StructureComponentNeptuneVillageWoodHut(StructureComponentVillageStartPiece par1ComponentVillageStartPiece,
        int par2, Random par3Random, StructureBoundingBox par4StructureBoundingBox, int par5) {
        super(par1ComponentVillageStartPiece, par2, par3Random, par4StructureBoundingBox, par5);
        this.planetBlock = ExtraPlanets_Blocks.neptuneBlocks;
    }

    @SuppressWarnings("rawtypes")
    public static StructureComponentNeptuneVillageWoodHut func_74908_a(
        StructureComponentVillageStartPiece par0ComponentVillageStartPiece, List par1List, Random par2Random, int par3,
        int par4, int par5, int par6, int par7) {
        final StructureBoundingBox var8 = StructureBoundingBox
            .getComponentToAddBoundingBox(par3, par4, par5, 0, 0, 0, 17, 9, 17, par6);
        return StructureComponent.findIntersecting(par1List, var8) == null
            ? new StructureComponentNeptuneVillageWoodHut(par0ComponentVillageStartPiece, par7, par2Random, var8, par6)
            : null;
    }
}
