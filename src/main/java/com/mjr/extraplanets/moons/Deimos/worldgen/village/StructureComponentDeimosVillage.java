package com.mjr.extraplanets.moons.Deimos.worldgen.village;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.util.ExtraPlanetsLog;
import com.mjr.extraplanets.world.village.StructureComponentVillageBase;

/**
 * The Deimos village component base. Everything shared with the other bodies lives in
 * {@link StructureComponentVillageBase}; this class adds what is genuinely Deimos-specific: the structure
 * registration for this body's {@code MapGenVillageDeimos}, the start-piece type, and the two helpers that look up
 * neighbouring pieces through {@code StructureVillagePiecesDeimos}.
 */
public abstract class StructureComponentDeimosVillage extends StructureComponentVillageBase {

    static {
        try {
            MapGenVillageDeimos.initiateStructures();
        } catch (Throwable e) {
            ExtraPlanetsLog
                .severe("[ExtraPlanets] Deimos village structures failed to register; villages will not generate", e);
        }
    }

    protected StructureComponentDeimosVillageStartPiece startPiece;

    public StructureComponentDeimosVillage() {}

    protected StructureComponentDeimosVillage(StructureComponentDeimosVillageStartPiece par1ComponentVillageStartPiece,
        int par2) {
        super(par2);
        this.startPiece = par1ComponentVillageStartPiece;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected StructureComponent getNextComponentNN(
        StructureComponentDeimosVillageStartPiece par1ComponentVillageStartPiece, List par2List, Random par3Random,
        int par4, int par5) {
        switch (this.coordBaseMode) {
            case 0:
                return StructureVillagePiecesDeimos.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX - 1,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ + par5,
                    1,
                    this.getComponentType());
            case 1:
                return StructureVillagePiecesDeimos.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX + par5,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ - 1,
                    2,
                    this.getComponentType());
            case 2:
                return StructureVillagePiecesDeimos.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX - 1,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ + par5,
                    1,
                    this.getComponentType());
            case 3:
                return StructureVillagePiecesDeimos.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX + par5,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ - 1,
                    2,
                    this.getComponentType());
            default:
                return null;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected StructureComponent getNextComponentPP(
        StructureComponentDeimosVillageStartPiece par1ComponentVillageStartPiece, List par2List, Random par3Random,
        int par4, int par5) {
        switch (this.coordBaseMode) {
            case 0:
                return StructureVillagePiecesDeimos.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.maxX + 1,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ + par5,
                    3,
                    this.getComponentType());
            case 1:
                return StructureVillagePiecesDeimos.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX + par5,
                    this.boundingBox.minY + par4,
                    this.boundingBox.maxZ + 1,
                    0,
                    this.getComponentType());
            case 2:
                return StructureVillagePiecesDeimos.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.maxX + 1,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ + par5,
                    3,
                    this.getComponentType());
            case 3:
                return StructureVillagePiecesDeimos.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX + par5,
                    this.boundingBox.minY + par4,
                    this.boundingBox.maxZ + 1,
                    0,
                    this.getComponentType());
            default:
                return null;
        }
    }

}
