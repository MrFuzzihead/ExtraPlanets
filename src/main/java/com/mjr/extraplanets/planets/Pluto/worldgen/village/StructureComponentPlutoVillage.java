package com.mjr.extraplanets.planets.Pluto.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Pluto village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Pluto-specific: the structure
 * registration for this body's {@code MapGenVillagePluto}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesPluto}.
 */
public abstract class StructureComponentPlutoVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillagePluto.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Pluto village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentPlutoVillageStartPiece startPiece;

    public StructureComponentPlutoVillage() {}

    protected StructureComponentPlutoVillage(StructureComponentPlutoVillageStartPiece par1ComponentVillageStartPiece,
        int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesPluto.getNextStructureComponent(
            (StructureComponentPlutoVillageStartPiece) startPiece,
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
        return StructureVillagePiecesPluto.getNextStructureComponentVillagePath(
            (StructureComponentPlutoVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
