package com.mjr.extraplanets.planets.Uranus.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Uranus piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightUranus extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightUranus(Class<? extends StructureComponentUranusVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
