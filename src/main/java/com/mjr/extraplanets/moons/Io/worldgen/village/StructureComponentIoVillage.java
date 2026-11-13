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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected StructureComponent getNextComponentNN(
        StructureComponentIoVillageStartPiece par1ComponentVillageStartPiece, List par2List, Random par3Random,
        int par4, int par5) {
        switch (this.coordBaseMode) {
            case 0:
                return StructureVillagePiecesIo.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX - 1,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ + par5,
                    1,
                    this.getComponentType());
            case 1:
                return StructureVillagePiecesIo.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX + par5,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ - 1,
                    2,
                    this.getComponentType());
            case 2:
                return StructureVillagePiecesIo.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX - 1,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ + par5,
                    1,
                    this.getComponentType());
            case 3:
                return StructureVillagePiecesIo.getNextStructureComponent(
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
        StructureComponentIoVillageStartPiece par1ComponentVillageStartPiece, List par2List, Random par3Random,
        int par4, int par5) {
        switch (this.coordBaseMode) {
            case 0:
                return StructureVillagePiecesIo.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.maxX + 1,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ + par5,
                    3,
                    this.getComponentType());
            case 1:
                return StructureVillagePiecesIo.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX + par5,
                    this.boundingBox.minY + par4,
                    this.boundingBox.maxZ + 1,
                    0,
                    this.getComponentType());
            case 2:
                return StructureVillagePiecesIo.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.maxX + 1,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ + par5,
                    3,
                    this.getComponentType());
            case 3:
                return StructureVillagePiecesIo.getNextStructureComponent(
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
