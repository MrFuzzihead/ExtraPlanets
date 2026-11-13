package com.mjr.extraplanets.planets.Ceres.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Ceres piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightCeres extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightCeres(Class<? extends StructureComponentCeresVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
