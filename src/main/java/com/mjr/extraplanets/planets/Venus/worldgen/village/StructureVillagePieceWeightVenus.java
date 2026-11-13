package com.mjr.extraplanets.planets.Venus.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Venus piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightVenus extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightVenus(Class<? extends StructureComponentVenusVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
