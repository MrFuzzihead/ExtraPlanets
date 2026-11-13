package com.mjr.extraplanets.planets.Uranus.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

public class StructureComponentUranusVillageField2 extends StructureComponentUranusVillage {

    public StructureComponentUranusVillageField2() {}

    public StructureComponentUranusVillageField2(
        StructureComponentUranusVillageStartPiece par1ComponentVillageStartPiece, int par2, Random par3Random,
        StructureBoundingBox par4StructureBoundingBox, int par5) {
        super(par1ComponentVillageStartPiece, par2);
        this.coordBaseMode = par5;
        this.boundingBox = par4StructureBoundingBox;
        this.cropTypeA = this.getRandomFieldCrop(par3Random);
        this.cropTypeB = this.getRandomFieldCrop(par3Random);
        this.cropTypeC = this.getRandomFieldCrop(par3Random);
        this.cropTypeD = this.getRandomFieldCrop(par3Random);
    }

    @Override
    protected void func_143012_a(NBTTagCompound nbt) {
        super.func_143012_a(nbt);

        nbt.setInteger("AvgGroundLevel", this.averageGroundLevel);
        nbt.setInteger("CropTypeA", Block.getIdFromBlock(this.cropTypeA));
        nbt.setInteger("CropTypeB", Block.getIdFromBlock(this.cropTypeB));
        nbt.setInteger("CropTypeC", Block.getIdFromBlock(this.cropTypeC));
        nbt.setInteger("CropTypeD", Block.getIdFromBlock(this.cropTypeD));
    }

    @Override
    protected void func_143011_b(NBTTagCompound nbt) {
        super.func_143011_b(nbt);

        this.averageGroundLevel = nbt.getInteger("AvgGroundLevel");
        this.cropTypeA = Block.getBlockById(nbt.getInteger("CropTypeA"));
        this.cropTypeB = Block.getBlockById(nbt.getInteger("CropTypeB"));
        this.cropTypeC = Block.getBlockById(nbt.getInteger("CropTypeC"));
        this.cropTypeD = Block.getBlockById(nbt.getInteger("CropTypeD"));
    }

    @SuppressWarnings("rawtypes")
    public static StructureComponentUranusVillageField2 func_74900_a(
        StructureComponentUranusVillageStartPiece par0ComponentVillageStartPiece, List par1List, Random par2Random,
        int par3, int par4, int par5, int par6, int par7) {
        final StructureBoundingBox structureboundingbox = StructureBoundingBox
            .getComponentToAddBoundingBox(par3, par4, par5, 0, 0, 0, 13, 4, 9, par6);
        return StructureComponentUranusVillage.canVillageGoDeeper(structureboundingbox)
            && StructureComponent.findIntersecting(par1List, structureboundingbox) == null
                ? new StructureComponentUranusVillageField2(
                    par0ComponentVillageStartPiece,
                    par7,
                    par2Random,
                    structureboundingbox,
                    par6)
                : null;
    }

    /**
     * second Part of Structure generating, this for example places Spiderwebs, Mob Spawners, it closes Mineshafts at
     * the end, it adds Fences...
     */
    @Override
    public boolean addComponentParts(World par1World, Random par2Random,
        StructureBoundingBox par3StructureBoundingBox) {
        return this.buildField2(par1World, par2Random, par3StructureBoundingBox);
    }
}
