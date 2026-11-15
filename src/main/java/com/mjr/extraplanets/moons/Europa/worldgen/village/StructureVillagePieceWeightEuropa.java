package com.mjr.extraplanets.moons.Europa.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Europa piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightEuropa extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightEuropa(Class<? extends StructureComponentEuropaVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
