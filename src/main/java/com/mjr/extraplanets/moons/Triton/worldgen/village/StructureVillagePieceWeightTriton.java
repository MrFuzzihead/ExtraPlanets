package com.mjr.extraplanets.moons.Triton.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Triton piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightTriton extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightTriton(Class<? extends StructureComponentTritonVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
