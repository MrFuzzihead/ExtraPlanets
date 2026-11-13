package com.mjr.extraplanets.moons.Rhea.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Rhea piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightRhea extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightRhea(Class<? extends StructureComponentRheaVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
