package com.mjr.extraplanets.moons.Callisto.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Callisto village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Callisto-specific: the structure
 * registration for this body's {@code MapGenVillageCallisto}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesCallisto}.
 */
public abstract class StructureComponentCallistoVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageCallisto.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Callisto village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentCallistoVillageStartPiece startPiece;

    public StructureComponentCallistoVillage() {}

    protected StructureComponentCallistoVillage(
        StructureComponentCallistoVillageStartPiece par1ComponentVillageStartPiece, int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesCallisto.getNextStructureComponent(
            (StructureComponentCallistoVillageStartPiece) startPiece,
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
        return StructureVillagePiecesCallisto.getNextStructureComponentVillagePath(
            (StructureComponentCallistoVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
