package com.mjr.extraplanets.planets.Saturn.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Saturn village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Saturn-specific: the structure
 * registration for this body's {@code MapGenVillageSaturn}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesSaturn}.
 */
public abstract class StructureComponentSaturnVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageSaturn.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Saturn village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentSaturnVillageStartPiece startPiece;

    public StructureComponentSaturnVillage() {}

    protected StructureComponentSaturnVillage(StructureComponentSaturnVillageStartPiece par1ComponentVillageStartPiece,
        int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesSaturn.getNextStructureComponent(
            (StructureComponentSaturnVillageStartPiece) startPiece,
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
        return StructureVillagePiecesSaturn.getNextStructureComponentVillagePath(
            (StructureComponentSaturnVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
