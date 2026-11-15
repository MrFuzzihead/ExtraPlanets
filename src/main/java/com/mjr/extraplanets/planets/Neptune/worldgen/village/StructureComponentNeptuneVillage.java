package com.mjr.extraplanets.planets.Neptune.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Neptune village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Neptune-specific: the structure
 * registration for this body's {@code MapGenVillageNeptune}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesNeptune}.
 */
public abstract class StructureComponentNeptuneVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageNeptune.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Neptune village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentNeptuneVillageStartPiece startPiece;

    public StructureComponentNeptuneVillage() {}

    protected StructureComponentNeptuneVillage(
        StructureComponentNeptuneVillageStartPiece par1ComponentVillageStartPiece, int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesNeptune.getNextStructureComponent(
            (StructureComponentNeptuneVillageStartPiece) startPiece,
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
        return StructureVillagePiecesNeptune.getNextStructureComponentVillagePath(
            (StructureComponentNeptuneVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
