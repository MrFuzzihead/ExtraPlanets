package com.mjr.extraplanets.moons.Io.worldgen.village;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;

/**
 * The Io piece weights. The bookkeeping lives in {@link StructureVillagePieceWeightBase}; this exists to keep the
 * constructor typed to this body's village components.
 */
public class StructureVillagePieceWeightIo extends StructureVillagePieceWeightBase {

    public StructureVillagePieceWeightIo(Class<? extends StructureComponentIoVillage> par1Class, int par2, int par3) {
        super(par1Class, par2, par3);
    }
}
