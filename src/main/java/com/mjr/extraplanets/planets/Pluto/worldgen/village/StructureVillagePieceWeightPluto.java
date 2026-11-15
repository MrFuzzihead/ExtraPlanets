package com.mjr.extraplanets.planets.Pluto.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Pluto piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightPluto extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightPluto(Class<? extends StructureComponentPlutoVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
