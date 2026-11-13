package com.mjr.extraplanets.planets.Eris.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Eris village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Eris-specific: the structure
 * registration for this body's {@code MapGenVillageEris}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesEris}.
 */
public abstract class StructureComponentErisVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageEris.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Eris village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentErisVillageStartPiece startPiece;

    public StructureComponentErisVillage() {}

    protected StructureComponentErisVillage(StructureComponentErisVillageStartPiece par1ComponentVillageStartPiece,
        int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesEris.getNextStructureComponent(
            (StructureComponentErisVillageStartPiece) startPiece,
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
        return StructureVillagePiecesEris.getNextStructureComponentVillagePath(
            (StructureComponentErisVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
