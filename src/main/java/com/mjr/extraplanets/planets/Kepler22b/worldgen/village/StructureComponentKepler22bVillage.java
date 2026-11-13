package com.mjr.extraplanets.planets.Kepler22b.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Kepler22b village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Kepler22b-specific: the structure
 * registration for this body's {@code MapGenVillageKepler22b}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesKepler22b}.
 */
public abstract class StructureComponentKepler22bVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageKepler22b.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog.severe(
                "[ExtraPlanets] Kepler22b village structures failed to register; villages will not generate",
                e);
        }
    }

    protected StructureComponentKepler22bVillageStartPiece startPiece;

    public StructureComponentKepler22bVillage() {}

    protected StructureComponentKepler22bVillage(
        StructureComponentKepler22bVillageStartPiece par1ComponentVillageStartPiece, int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesKepler22b.getNextStructureComponent(
            (StructureComponentKepler22bVillageStartPiece) startPiece,
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
        return StructureVillagePiecesKepler22b.getNextStructureComponentVillagePath(
            (StructureComponentKepler22bVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
