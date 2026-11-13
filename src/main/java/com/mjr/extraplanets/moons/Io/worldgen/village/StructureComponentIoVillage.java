package com.mjr.extraplanets.moons.Io.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Io village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Io-specific: the structure
 * registration for this body's {@code MapGenVillageIo}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesIo}.
 */
public abstract class StructureComponentIoVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageIo.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Io village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentIoVillageStartPiece startPiece;

    public StructureComponentIoVillage() {}

    protected StructureComponentIoVillage(StructureComponentIoVillageStartPiece par1ComponentVillageStartPiece,
        int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesIo.getNextStructureComponent(
            (StructureComponentIoVillageStartPiece) startPiece,
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
        return StructureVillagePiecesIo.getNextStructureComponentVillagePath(
            (StructureComponentIoVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
