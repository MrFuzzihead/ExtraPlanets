package com.mjr.extraplanets.planets.Neptune.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Neptune piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightNeptune extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightNeptune(Class<? extends StructureComponentNeptuneVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
