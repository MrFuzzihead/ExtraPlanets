package com.mjr.extraplanets.planets.Eris.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Eris piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightEris extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightEris(Class<? extends StructureComponentErisVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
