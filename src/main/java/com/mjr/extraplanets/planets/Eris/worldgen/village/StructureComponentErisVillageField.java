package com.mjr.extraplanets.planets.Eris.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

public class StructureComponentErisVillageField extends StructureComponentErisVillage {

    public StructureComponentErisVillageField() {}

    public StructureComponentErisVillageField(StructureComponentErisVillageStartPiece par1ComponentVillageStartPiece,
        int par2, Random par3Random, StructureBoundingBox par4StructureBoundingBox, int par5) {
        super(par1ComponentVillageStartPiece, par2);
        this.coordBaseMode = par5;
        this.boundingBox = par4StructureBoundingBox;
    }

    @Override
    protected void func_143012_a(NBTTagCompound nbt) {
        super.func_143012_a(nbt);

        nbt.setInteger("AvgGroundLevel", this.averageGroundLevel);
    }

    @Override
    protected void func_143011_b(NBTTagCompound nbt) {
        super.func_143011_b(nbt);

        this.averageGroundLevel = nbt.getInteger("AvgGroundLevel");
    }

    @SuppressWarnings("rawtypes")
    public static StructureComponentErisVillageField func_74900_a(
        StructureComponentErisVillageStartPiece par0ComponentVillageStartPiece, List par1List, Random par2Random,
        int par3, int par4, int par5, int par6, int par7) {
        final StructureBoundingBox var8 = StructureBoundingBox
            .getComponentToAddBoundingBox(par3, par4, par5, 0, 0, 0, 13, 4, 9, par6);
        return StructureComponent.findIntersecting(par1List, var8) == null
            ? new StructureComponentErisVillageField(par0ComponentVillageStartPiece, par7, par2Random, var8, par6)
            : null;
    }

    @Override
    public boolean addComponentParts(World par1World, Random par2Random,
        StructureBoundingBox par3StructureBoundingBox) {
        return this.buildField(par1World, par2Random, par3StructureBoundingBox);
    }
}
