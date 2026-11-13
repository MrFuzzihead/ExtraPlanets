package com.mjr.extraplanets.moons.Phobos.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Phobos piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightPhobos extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightPhobos(Class<? extends StructureComponentPhobosVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
