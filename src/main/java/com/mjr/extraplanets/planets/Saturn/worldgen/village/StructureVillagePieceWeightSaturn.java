package com.mjr.extraplanets.planets.Saturn.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Saturn piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightSaturn extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightSaturn(Class<? extends StructureComponentSaturnVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
