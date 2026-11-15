package com.mjr.extraplanets.moons.Iapetus.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Iapetus village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Iapetus-specific: the structure
 * registration for this body's {@code MapGenVillageIapetus}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesIapetus}.
 */
public abstract class StructureComponentIapetusVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageIapetus.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Iapetus village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentIapetusVillageStartPiece startPiece;

    public StructureComponentIapetusVillage() {}

    protected StructureComponentIapetusVillage(
        StructureComponentIapetusVillageStartPiece par1ComponentVillageStartPiece, int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @Override
    protected StructureComponent dispatchNext(StructureComponent startPiece, List components, Random rand, int x, int y,
        int z, int mode, int type) {
        return StructureVillagePiecesIapetus.getNextStructureComponent(
            (StructureComponentIapetusVillageStartPiece) startPiece,
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
        return StructureVillagePiecesIapetus.getNextStructureComponentVillagePath(
            (StructureComponentIapetusVillageStartPiece) startPiece,
            components,
            rand,
            x,
            y,
            z,
            mode,
            type);
    }
}
