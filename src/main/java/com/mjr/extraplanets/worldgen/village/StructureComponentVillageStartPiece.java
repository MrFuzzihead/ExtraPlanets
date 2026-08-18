package com.mjr.extraplanets.worldgen.village;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

public abstract class StructureComponentVillageStartPiece extends StructureComponentVillageWell {

    public WorldChunkManager worldChunkMngr;
    public int terrainType;
    public StructureVillagePieceWeight structVillagePieceWeight;
    public ArrayList<StructureVillagePieceWeight> structureVillageWeightedPieceList;
    public ArrayList<Object> field_74932_i = new ArrayList<Object>();
    public ArrayList<Object> field_74930_j = new ArrayList<Object>();

    public StructureComponentVillageStartPiece() {}

    public StructureComponentVillageStartPiece(WorldChunkManager par1WorldChunkManager, int par2, Random par3Random,
        int par4, int par5, ArrayList<StructureVillagePieceWeight> par6ArrayList, int par7) {
        super((StructureComponentVillageStartPiece) null, 0, par3Random, par4, par5);
        this.worldChunkMngr = par1WorldChunkManager;
        this.structureVillageWeightedPieceList = par6ArrayList;
        this.terrainType = par7;
        this.startPiece = this;
    }

    @Override
    protected void func_143012_a(NBTTagCompound nbt) {
        super.func_143012_a(nbt);

        nbt.setInteger("TerrainType", this.terrainType);
    }

    @Override
    protected void func_143011_b(NBTTagCompound nbt) {
        super.func_143011_b(nbt);

        this.terrainType = nbt.getInteger("TerrainType");
    }

    public WorldChunkManager getWorldChunkManager() {
        return this.worldChunkMngr;
    }

    /**
     * Builds a randomly-selected building (wood hut / field / house) for this body's village.
     * Implemented per-body so it can instantiate the body's concrete registered component classes.
     */
    public abstract StructureComponentVillage constructComponent(
        StructureVillagePieceWeight par1StructureVillagePieceWeight, List<StructureComponent> par2List,
        Random par3Random, int par4, int par5, int par6, int par7, int par8);

    /**
     * Builds a torch for this body's village.
     */
    public abstract StructureComponentVillageTorch constructTorch(int par1, Random par2Random,
        StructureBoundingBox par3StructureBoundingBox, int par4);

    /**
     * Builds a path for this body's village.
     */
    public abstract StructureComponentVillagePathGen constructPath(int par1, Random par2Random,
        StructureBoundingBox par3StructureBoundingBox, int par4);
}
