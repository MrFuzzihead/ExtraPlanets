package com.mjr.extraplanets.planets.Uranus.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Uranus village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Uranus-specific: the structure
 * registration for this body's {@code MapGenVillageUranus}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesUranus}.
 */
public abstract class StructureComponentUranusVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageUranus.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Uranus village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentUranusVillageStartPiece startPiece;

    public StructureComponentUranusVillage() {}

    protected StructureComponentUranusVillage(StructureComponentUranusVillageStartPiece par1ComponentVillageStartPiece,
        int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesUranus.getNextStructureComponent(
            (StructureComponentUranusVillageStartPiece) startPiece,
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
        return StructureVillagePiecesUranus.getNextStructureComponentVillagePath(
            (StructureComponentUranusVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
