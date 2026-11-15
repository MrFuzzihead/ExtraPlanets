package com.mjr.extraplanets.moons.Titania.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

/**
 * The wood hut. The build code is inherited from {@link StructureComponentTitaniaVillageHouse}: the two piece types are
 * the same 17x9x17 building in every body (they were 2,152 duplicated lines each), so the only thing this class has to
 * provide is its own type - the village piece dispatch compares classes, and {@code MapGenStructureIO} registers it
 * under its own name.
 */
public class StructureComponentTitaniaVillageWoodHut extends StructureComponentTitaniaVillageHouse {

    public StructureComponentTitaniaVillageWoodHut() {}

    public StructureComponentTitaniaVillageWoodHut(
        StructureComponentTitaniaVillageStartPiece par1ComponentVillageStartPiece, int par2, Random par3Random,
        StructureBoundingBox par4StructureBoundingBox, int par5) {
        super(par1ComponentVillageStartPiece, par2, par3Random, par4StructureBoundingBox, par5);
    }

    public static StructureComponentTitaniaVillageWoodHut func_74908_a(
        StructureComponentTitaniaVillageStartPiece par0ComponentVillageStartPiece, List<StructureComponent> par1List,
        Random par2Random, int par3, int par4, int par5, int par6, int par7) {
        final StructureBoundingBox var8 = StructureBoundingBox
            .getComponentToAddBoundingBox(par3, par4, par5, 0, 0, 0, 17, 9, 17, par6);
        return StructureComponent.findIntersecting(par1List, var8) == null
            ? new StructureComponentTitaniaVillageWoodHut(par0ComponentVillageStartPiece, par7, par2Random, var8, par6)
            : null;
    }
}
