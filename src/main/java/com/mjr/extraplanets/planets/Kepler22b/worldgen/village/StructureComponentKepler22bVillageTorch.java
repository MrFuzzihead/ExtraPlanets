package com.mjr.extraplanets.planets.Kepler22b.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

public class StructureComponentKepler22bVillageTorch extends StructureComponentKepler22bVillage {

    public StructureComponentKepler22bVillageTorch() {}

    public StructureComponentKepler22bVillageTorch(
        StructureComponentKepler22bVillageStartPiece par1ComponentVillageStartPiece, int par2, Random par3Random,
        StructureBoundingBox par4StructureBoundingBox, int par5) {
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
    public static StructureBoundingBox func_74904_a(
        StructureComponentKepler22bVillageStartPiece par0ComponentVillageStartPiece, List par1List, Random par2Random,
        int par3, int par4, int par5, int par6) {
        final StructureBoundingBox var7 = StructureBoundingBox
            .getComponentToAddBoundingBox(par3, par4, par5, 0, 0, 0, 3, 4, 2, par6);
        return StructureComponent.findIntersecting(par1List, var7) != null ? null : var7;
    }

    /**
     * second Part of Structure generating, this for example places Spiderwebs, Mob Spawners, it closes Mineshafts at
     * the end, it adds Fences...
     */
    @Override
    public boolean addComponentParts(World par1World, Random par2Random,
        StructureBoundingBox par3StructureBoundingBox) {
        return this.buildTorch(par1World, par2Random, par3StructureBoundingBox);
    }
}
