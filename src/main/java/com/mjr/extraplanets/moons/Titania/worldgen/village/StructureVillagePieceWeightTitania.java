package com.mjr.extraplanets.moons.Titania.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Titania piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightTitania extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightTitania(Class<? extends StructureComponentTitaniaVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
