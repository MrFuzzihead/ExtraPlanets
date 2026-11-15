package com.mjr.extraplanets.moons.Ganymede.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Ganymede piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightGanymede extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightGanymede(Class<? extends StructureComponentGanymedeVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
