package com.mjr.extraplanets.planets.Mercury.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Mercury village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Mercury-specific: the structure
 * registration for this body's {@code MapGenVillageMercury}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesMercury}.
 */
public abstract class StructureComponentMercuryVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageMercury.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Mercury village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentMercuryVillageStartPiece startPiece;

    public StructureComponentMercuryVillage() {}

    protected StructureComponentMercuryVillage(
        StructureComponentMercuryVillageStartPiece par1ComponentVillageStartPiece, int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesMercury.getNextStructureComponent(
            (StructureComponentMercuryVillageStartPiece) startPiece,
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
        return StructureVillagePiecesMercury.getNextStructureComponentVillagePath(
            (StructureComponentMercuryVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
