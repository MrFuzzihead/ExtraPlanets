package com.mjr.extraplanets.planets.Venus.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Venus village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Venus-specific: the structure
 * registration for this body's {@code MapGenVillageVenus}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesVenus}.
 */
public abstract class StructureComponentVenusVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageVenus.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Venus village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentVenusVillageStartPiece startPiece;

    public StructureComponentVenusVillage() {}

    protected StructureComponentVenusVillage(StructureComponentVenusVillageStartPiece par1ComponentVillageStartPiece,
        int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesVenus.getNextStructureComponent(
            (StructureComponentVenusVillageStartPiece) startPiece,
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
        return StructureVillagePiecesVenus.getNextStructureComponentVillagePath(
            (StructureComponentVenusVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
