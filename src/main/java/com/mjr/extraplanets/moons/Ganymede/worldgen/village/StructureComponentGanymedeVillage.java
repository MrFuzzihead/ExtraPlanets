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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected StructureComponent getNextComponentNN(
        StructureComponentGanymedeVillageStartPiece par1ComponentVillageStartPiece, List par2List, Random par3Random,
        int par4, int par5) {
        switch (this.coordBaseMode) {
            case 0:
                return StructureVillagePiecesGanymede.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX - 1,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ + par5,
                    1,
                    this.getComponentType());
            case 1:
                return StructureVillagePiecesGanymede.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX + par5,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ - 1,
                    2,
                    this.getComponentType());
            case 2:
                return StructureVillagePiecesGanymede.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX - 1,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ + par5,
                    1,
                    this.getComponentType());
            case 3:
                return StructureVillagePiecesGanymede.getNextStructureComponent(
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
        StructureComponentGanymedeVillageStartPiece par1ComponentVillageStartPiece, List par2List, Random par3Random,
        int par4, int par5) {
        switch (this.coordBaseMode) {
            case 0:
                return StructureVillagePiecesGanymede.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.maxX + 1,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ + par5,
                    3,
                    this.getComponentType());
            case 1:
                return StructureVillagePiecesGanymede.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.minX + par5,
                    this.boundingBox.minY + par4,
                    this.boundingBox.maxZ + 1,
                    0,
                    this.getComponentType());
            case 2:
                return StructureVillagePiecesGanymede.getNextStructureComponent(
                    par1ComponentVillageStartPiece,
                    par2List,
                    par3Random,
                    this.boundingBox.maxX + 1,
                    this.boundingBox.minY + par4,
                    this.boundingBox.minZ + par5,
                    3,
                    this.getComponentType());
            case 3:
                return StructureVillagePiecesGanymede.getNextStructureComponent(
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
