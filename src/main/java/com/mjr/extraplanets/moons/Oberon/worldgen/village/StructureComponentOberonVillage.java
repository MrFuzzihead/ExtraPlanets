package com.mjr.extraplanets.moons.Oberon.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Oberon village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Oberon-specific: the structure
 * registration for this body's {@code MapGenVillageOberon}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesOberon}.
 */
public abstract class StructureComponentOberonVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageOberon.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Oberon village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentOberonVillageStartPiece startPiece;

    public StructureComponentOberonVillage() {}

    protected StructureComponentOberonVillage(StructureComponentOberonVillageStartPiece par1ComponentVillageStartPiece,
        int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesOberon.getNextStructureComponent(
            (StructureComponentOberonVillageStartPiece) startPiece,
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
        return StructureVillagePiecesOberon.getNextStructureComponentVillagePath(
            (StructureComponentOberonVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
