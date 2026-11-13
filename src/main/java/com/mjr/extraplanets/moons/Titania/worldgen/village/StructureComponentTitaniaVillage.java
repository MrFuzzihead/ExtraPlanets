package com.mjr.extraplanets.moons.Titania.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Titania village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Titania-specific: the structure
 * registration for this body's {@code MapGenVillageTitania}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesTitania}.
 */
public abstract class StructureComponentTitaniaVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageTitania.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Titania village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentTitaniaVillageStartPiece startPiece;

    public StructureComponentTitaniaVillage() {}

    protected StructureComponentTitaniaVillage(
        StructureComponentTitaniaVillageStartPiece par1ComponentVillageStartPiece, int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesTitania.getNextStructureComponent(
            (StructureComponentTitaniaVillageStartPiece) startPiece,
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
        return StructureVillagePiecesTitania.getNextStructureComponentVillagePath(
            (StructureComponentTitaniaVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
