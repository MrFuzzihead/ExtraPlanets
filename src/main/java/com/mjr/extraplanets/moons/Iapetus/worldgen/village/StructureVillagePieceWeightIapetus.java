package com.mjr.extraplanets.moons.Iapetus.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Iapetus piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightIapetus extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightIapetus(Class<? extends StructureComponentIapetusVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
