package com.mjr.extraplanets.moons.Europa.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Europa village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Europa-specific: the structure
 * registration for this body's {@code MapGenVillageEuropa}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesEuropa}.
 */
public abstract class StructureComponentEuropaVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageEuropa.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Europa village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentEuropaVillageStartPiece startPiece;

    public StructureComponentEuropaVillage() {}

    protected StructureComponentEuropaVillage(StructureComponentEuropaVillageStartPiece par1ComponentVillageStartPiece,
        int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesEuropa.getNextStructureComponent(
            (StructureComponentEuropaVillageStartPiece) startPiece,
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
        return StructureVillagePiecesEuropa.getNextStructureComponentVillagePath(
            (StructureComponentEuropaVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
