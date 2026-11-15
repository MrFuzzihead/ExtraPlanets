package com.mjr.extraplanets.planets.Ceres.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Ceres village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Ceres-specific: the structure
 * registration for this body's {@code MapGenVillageCeres}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesCeres}.
 */
public abstract class StructureComponentCeresVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageCeres.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Ceres village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentCeresVillageStartPiece startPiece;

    public StructureComponentCeresVillage() {}

    protected StructureComponentCeresVillage(StructureComponentCeresVillageStartPiece par1ComponentVillageStartPiece,
        int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesCeres.getNextStructureComponent(
            (StructureComponentCeresVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }

    @Override
    protected StructureComponent dispatchNextPath(StructureComponent startPiece, List components, Random rand, int x,
        int y, int z, int mode, int type) {
        return StructureVillagePiecesCeres.getNextStructureComponentVillagePath(
            (StructureComponentCeresVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
