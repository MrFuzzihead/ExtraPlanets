package com.mjr.extraplanets.moons.Rhea.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.blocks.ExtraPlanets_Blocks;

public class StructureComponentRheaVillageWell extends StructureComponentRheaVillage {

    public StructureComponentRheaVillageWell() {}

    public StructureComponentRheaVillageWell(StructureComponentRheaVillageStartPiece par1ComponentVillageStartPiece,
        int par2, Random par3Random, int par4, int par5) {
        super(par1ComponentVillageStartPiece, par2);
        this.coordBaseMode = par3Random.nextInt(4);

        switch (this.coordBaseMode) {
            case 0:
            case 2:
                this.boundingBox = new StructureBoundingBox(par4, 64, par5, par4 + 6 - 1, 78, par5 + 6 - 1);
                break;
            default:
                this.boundingBox = new StructureBoundingBox(par4, 64, par5, par4 + 6 - 1, 78, par5 + 6 - 1);
        }
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void buildComponent(StructureComponent par1StructureComponent, List par2List, Random par3Random) {
        StructureVillagePiecesRhea.getNextStructureComponentVillagePath(
            (StructureComponentRheaVillageStartPiece) par1StructureComponent,
            par2List,
            par3Random,
            this.boundingBox.minX - 1,
            this.boundingBox.maxY - 4,
            this.boundingBox.minZ + 1,
            1,
            this.getComponentType());
        StructureVillagePiecesRhea.getNextStructureComponentVillagePath(
            (StructureComponentRheaVillageStartPiece) par1StructureComponent,
            par2List,
            par3Random,
            this.boundingBox.maxX + 1,
            this.boundingBox.maxY - 4,
            this.boundingBox.minZ + 1,
            3,
            this.getComponentType());
        StructureVillagePiecesRhea.getNextStructureComponentVillagePath(
            (StructureComponentRheaVillageStartPiece) par1StructureComponent,
            par2List,
            par3Random,
            this.boundingBox.minX + 1,
            this.boundingBox.maxY - 4,
            this.boundingBox.minZ - 1,
            2,
            this.getComponentType());
        StructureVillagePiecesRhea.getNextStructureComponentVillagePath(
            (StructureComponentRheaVillageStartPiece) par1StructureComponent,
            par2List,
            par3Random,
            this.boundingBox.minX + 1,
            this.boundingBox.maxY - 4,
            this.boundingBox.maxZ + 1,
            0,
            this.getComponentType());
    }

    @Override
    public boolean addComponentParts(World par1World, Random par2Random,
        StructureBoundingBox par3StructureBoundingBox) {
        return this.buildWell(par1World, par2Random, par3StructureBoundingBox, ExtraPlanets_Blocks.rheaBlocks);
    }

    protected void fillWithBlocksAndMetadata(World par1World, StructureBoundingBox par2StructureBoundingBox, int par3,
        int par4, int par5, int par6, int par7, int par8, Block par9, Block par10, boolean par11) {
        final Block var12 = this.getBiomeSpecificBlock(par9, 0);
        final int var13 = this.getBiomeSpecificBlockMetadata(par9, 0);
        final Block var14 = this.getBiomeSpecificBlock(par10, 0);
        final int var15 = this.getBiomeSpecificBlockMetadata(par10, 0);
        super.fillWithMetadataBlocks(
            par1World,
            par2StructureBoundingBox,
            par3,
            par4,
            par5,
            par6,
            par7,
            par8,
            var12,
            var13,
            var14,
            var15,
            par11);
    }
}
