package com.mjr.extraplanets.moons.Triton.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Triton village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Triton-specific: the structure
 * registration for this body's {@code MapGenVillageTriton}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesTriton}.
 */
public abstract class StructureComponentTritonVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageTriton.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Triton village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentTritonVillageStartPiece startPiece;

    public StructureComponentTritonVillage() {}

    protected StructureComponentTritonVillage(StructureComponentTritonVillageStartPiece par1ComponentVillageStartPiece,
        int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesTriton.getNextStructureComponent(
            (StructureComponentTritonVillageStartPiece) startPiece,
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
        return StructureVillagePiecesTriton.getNextStructureComponentVillagePath(
            (StructureComponentTritonVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
