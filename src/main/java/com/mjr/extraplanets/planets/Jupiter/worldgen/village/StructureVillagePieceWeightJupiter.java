package com.mjr.extraplanets.planets.Jupiter.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Jupiter piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightJupiter extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightJupiter(Class<? extends StructureComponentJupiterVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
