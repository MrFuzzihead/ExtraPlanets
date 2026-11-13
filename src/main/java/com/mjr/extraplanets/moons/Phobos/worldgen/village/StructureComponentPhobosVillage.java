package com.mjr.extraplanets.moons.Phobos.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Phobos village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Phobos-specific: the structure
 * registration for this body's {@code MapGenVillagePhobos}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesPhobos}.
 */
public abstract class StructureComponentPhobosVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillagePhobos.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Phobos village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentPhobosVillageStartPiece startPiece;

    public StructureComponentPhobosVillage() {}

    protected StructureComponentPhobosVillage(StructureComponentPhobosVillageStartPiece par1ComponentVillageStartPiece,
        int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesPhobos.getNextStructureComponent(
            (StructureComponentPhobosVillageStartPiece) startPiece,
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
        return StructureVillagePiecesPhobos.getNextStructureComponentVillagePath(
            (StructureComponentPhobosVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
