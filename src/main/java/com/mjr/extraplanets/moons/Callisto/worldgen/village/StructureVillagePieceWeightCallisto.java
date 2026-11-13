package com.mjr.extraplanets.moons.Callisto.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Callisto piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightCallisto extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightCallisto(Class<? extends StructureComponentCallistoVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
