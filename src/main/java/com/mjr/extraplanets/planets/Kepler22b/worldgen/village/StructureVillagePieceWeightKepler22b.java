package com.mjr.extraplanets.planets.Kepler22b.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Kepler22b piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep
 * the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightKepler22b extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightKepler22b(Class<? extends StructureComponentKepler22bVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
