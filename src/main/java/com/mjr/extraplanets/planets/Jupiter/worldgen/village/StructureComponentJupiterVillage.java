package com.mjr.extraplanets.planets.Jupiter.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Jupiter village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Jupiter-specific: the structure
 * registration for this body's {@code MapGenVillageJupiter}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesJupiter}.
 */
public abstract class StructureComponentJupiterVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageJupiter.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Jupiter village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentJupiterVillageStartPiece startPiece;

    public StructureComponentJupiterVillage() {}

    protected StructureComponentJupiterVillage(
        StructureComponentJupiterVillageStartPiece par1ComponentVillageStartPiece, int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected boolean snapsGroundLevelToAir() {
        return true;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesJupiter.getNextStructureComponent(
            (StructureComponentJupiterVillageStartPiece) startPiece,
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
        return StructureVillagePiecesJupiter.getNextStructureComponentVillagePath(
            (StructureComponentJupiterVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
