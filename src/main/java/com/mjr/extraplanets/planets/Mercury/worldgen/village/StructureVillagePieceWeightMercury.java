package com.mjr.extraplanets.planets.Mercury.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Mercury piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightMercury extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightMercury(Class<? extends StructureComponentMercuryVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
