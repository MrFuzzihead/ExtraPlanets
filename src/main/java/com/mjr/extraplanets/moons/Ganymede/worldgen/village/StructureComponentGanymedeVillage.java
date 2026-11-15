package com.mjr.extraplanets.moons.Ganymede.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Ganymede village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Ganymede-specific: the structure
 * registration for this body's {@code MapGenVillageGanymede}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesGanymede}.
 */
public abstract class StructureComponentGanymedeVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageGanymede.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Ganymede village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentGanymedeVillageStartPiece startPiece;

    public StructureComponentGanymedeVillage() {}

    protected StructureComponentGanymedeVillage(
        StructureComponentGanymedeVillageStartPiece par1ComponentVillageStartPiece, int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesGanymede.getNextStructureComponent(
            (StructureComponentGanymedeVillageStartPiece) startPiece,
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
        return StructureVillagePiecesGanymede.getNextStructureComponentVillagePath(
            (StructureComponentGanymedeVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
