package com.mjr.extraplanets.moons.Deimos.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Deimos piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightDeimos extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightDeimos(Class<? extends StructureComponentDeimosVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
