package com.mjr.extraplanets.moons.Deimos.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.blocks.ExtraPlanets_Blocks;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Deimos village house. The 17x9x17 build code lives in
 * {@link StructureComponentVillageBase#buildHouse(World, Random, StructureBoundingBox, Block)} - it was the same 2,153
 * lines in 21 of the 22 bodies, differing only in which block the building is made of.
 */
public class StructureComponentDeimosVillageHouse extends StructureComponentDeimosVillage {

    public StructureComponentDeimosVillageHouse() {}

    public StructureComponentDeimosVillageHouse(
        StructureComponentDeimosVillageStartPiece par1ComponentVillageStartPiece, int par2, Random par3Random,
        StructureBoundingBox par4StructureBoundingBox, int par5) {
        super(par1ComponentVillageStartPiece, par2);
        this.coordBaseMode = par5;
        this.boundingBox = par4StructureBoundingBox;
    }

    public static StructureComponentDeimosVillageHouse func_74921_a(
        StructureComponentDeimosVillageStartPiece par0ComponentVillageStartPiece, List par1List, Random par2Random,
        int par3, int par4, int par5, int par6, int par7) {
        final StructureBoundingBox var8 = StructureBoundingBox
            .getComponentToAddBoundingBox(par3, par4, par5, 0, 0, 0, 17, 9, 17, par6);
        return StructureComponent.findIntersecting(par1List, var8) == null
            ? new StructureComponentDeimosVillageHouse(par0ComponentVillageStartPiece, par7, par2Random, var8, par6)
            : null;
    }

    @Override
    protected void func_143012_a(net.minecraft.nbt.NBTTagCompound nbt) {
        super.func_143012_a(nbt);

        nbt.setInteger("AvgGroundLevel", this.averageGroundLevel);
    }

    @Override
    protected void func_143011_b(net.minecraft.nbt.NBTTagCompound nbt) {
        super.func_143011_b(nbt);

        this.averageGroundLevel = nbt.getInteger("AvgGroundLevel");
    }

    @Override
    public boolean addComponentParts(World par1World, Random par2Random,
        StructureBoundingBox par3StructureBoundingBox) {
        return this.buildHouse(par1World, par2Random, par3StructureBoundingBox, ExtraPlanets_Blocks.deimosBlocks);
    }
}
