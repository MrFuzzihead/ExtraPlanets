package com.mjr.extraplanets.moons.Callisto.worldgen.village;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.world.World;

import com.mjr.extraplanets.world.village.StructureVillageStartBase;

/**
 * The Callisto village start. The growth algorithm lives in {@link StructureVillageStartBase}; this names the pieces.
 */
public class StructureVillageStartCallisto extends StructureVillageStartBase {

    public StructureVillageStartCallisto() {}

    public StructureVillageStartCallisto(World par1World, Random par2Random, int par3, int par4, int par5) {
        super(par3, par4);

        final ArrayList<StructureVillagePieceWeightCallisto> weights = StructureVillagePiecesCallisto
            .getStructureVillageWeightedPieceList(par2Random, par5);

        this.buildVillage(
            new StructureComponentCallistoVillageStartPiece(
                par1World.getWorldChunkManager(),
                0,
                par2Random,
                (par3 << 4) + 2,
                (par4 << 4) + 2,
                weights,
                par5),
            par2Random);
    }

    /**
     * currently only defined for Villages, returns true if Village has more than 2 non-road components
     */
    @Override
    public boolean isSizeableStructure() {
        return true;
    }
}
