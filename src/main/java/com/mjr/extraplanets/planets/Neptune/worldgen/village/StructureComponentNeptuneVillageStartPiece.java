package com.mjr.extraplanets.planets.Neptune.worldgen.village;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.biome.WorldChunkManager;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

public class StructureComponentNeptuneVillageStartPiece extends StructureComponentNeptuneVillageWell {

    public WorldChunkManager worldChunkMngr;

    public StructureComponentNeptuneVillageStartPiece() {}

    public StructureComponentNeptuneVillageStartPiece(WorldChunkManager par1WorldChunkManager, int par2,
        Random par3Random, int par4, int par5, ArrayList<StructureVillagePieceWeightNeptune> par6ArrayList, int par7) {
        super((StructureComponentNeptuneVillageStartPiece) null, 0, par3Random, par4, par5);
        this.worldChunkMngr = par1WorldChunkManager;
        // A copy, because the shared growth code removes exhausted piece types as it goes.
        this.structureVillageWeightedPieceList = new java.util.ArrayList<StructureVillagePieceWeightBase>(
            par6ArrayList);
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
}
