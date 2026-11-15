package com.mjr.extraplanets.world.village;

/**
 * How many of one village piece type a village may contain.
 *
 * <p>
 * Identical in all 22 bodies apart from the generic bound on the piece class, which is the only reason the field is a
 * raw {@code Class<?>} here. The per-body subclasses keep the typed constructor, so the call sites that build the
 * weight list still only accept that body's components.
 * </p>
 */
public abstract class StructureVillagePieceWeightBase {

    public Class<?> villagePieceClass;
    public final int villagePieceWeight;
    public int villagePiecesSpawned;
    public int villagePiecesLimit;

    protected StructureVillagePieceWeightBase(Class<?> par1Class, int par2, int par3) {
        this.villagePieceClass = par1Class;
        this.villagePieceWeight = par2;
        this.villagePiecesLimit = (int) (par3 / 1.5D);
    }

    public boolean canSpawnMoreVillagePiecesOfType(int par1) {
        return this.villagePiecesLimit == 0 || this.villagePiecesSpawned < this.villagePiecesLimit;
    }

    public boolean canSpawnMoreVillagePieces() {
        return this.villagePiecesLimit == 0 || this.villagePiecesSpawned < this.villagePiecesLimit;
    }
}
