package com.mjr.extraplanets.worldgen.village;

public class StructureVillagePieceWeight {

    public Class<? extends StructureComponentVillage> villagePieceClass;
    public final int villagePieceWeight;
    public int villagePiecesSpawned;
    public int villagePiecesLimit;

    public StructureVillagePieceWeight(Class<? extends StructureComponentVillage> par1Class, int par2, int par3) {
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
