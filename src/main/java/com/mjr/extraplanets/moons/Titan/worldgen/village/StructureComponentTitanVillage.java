package com.mjr.extraplanets.moons.Titan.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Titan village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Titan-specific: the structure
 * registration for this body's {@code MapGenVillageTitan}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesTitan}.
 */
public abstract class StructureComponentTitanVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageTitan.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Titan village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentTitanVillageStartPiece startPiece;

    public StructureComponentTitanVillage() {}

    protected StructureComponentTitanVillage(StructureComponentTitanVillageStartPiece par1ComponentVillageStartPiece,
        int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesTitan.getNextStructureComponent(
            (StructureComponentTitanVillageStartPiece) startPiece,
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
        return StructureVillagePiecesTitan.getNextStructureComponentVillagePath(
            (StructureComponentTitanVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
