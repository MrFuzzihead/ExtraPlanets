package com.mjr.extraplanets.moons.Oberon.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Oberon piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightOberon extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightOberon(Class<? extends StructureComponentOberonVillage> par1Class, int par2,
        int par3) {
        super(par1Class, par2, par3);
    }
}
