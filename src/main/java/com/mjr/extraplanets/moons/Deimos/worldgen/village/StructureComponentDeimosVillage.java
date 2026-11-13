package com.mjr.extraplanets.moons.Deimos.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Deimos village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Deimos-specific: the structure
 * registration for this body's {@code MapGenVillageDeimos}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesDeimos}.
 */
public abstract class StructureComponentDeimosVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageDeimos.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Deimos village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentDeimosVillageStartPiece startPiece;

    public StructureComponentDeimosVillage() {}

    protected StructureComponentDeimosVillage(StructureComponentDeimosVillageStartPiece par1ComponentVillageStartPiece,
        int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesDeimos.getNextStructureComponent(
            (StructureComponentDeimosVillageStartPiece) startPiece,
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
        return StructureVillagePiecesDeimos.getNextStructureComponentVillagePath(
            (StructureComponentDeimosVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
