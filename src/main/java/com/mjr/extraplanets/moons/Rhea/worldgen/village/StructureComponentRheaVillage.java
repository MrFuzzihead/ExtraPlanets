package com.mjr.extraplanets.moons.Rhea.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Rhea village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Rhea-specific: the structure
 * registration for this body's {@code MapGenVillageRhea}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesRhea}.
 */
public abstract class StructureComponentRheaVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageRhea.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Rhea village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentRheaVillageStartPiece startPiece;

    public StructureComponentRheaVillage() {}

    protected StructureComponentRheaVillage(StructureComponentRheaVillageStartPiece par1ComponentVillageStartPiece,
        int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesRhea.getNextStructureComponent(
            (StructureComponentRheaVillageStartPiece) startPiece,
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
        return StructureVillagePiecesRhea.getNextStructureComponentVillagePath(
            (StructureComponentRheaVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
