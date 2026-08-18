package com.mjr.extraplanets.worldgen.village;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

public abstract class StructureVillagePiecesBase {

    private static int func_75079_a(List<StructureVillagePieceWeight> par0List) {
        boolean var1 = false;
        int var2 = 0;
        StructureVillagePieceWeight var4;

        for (final Iterator<StructureVillagePieceWeight> var3 = par0List.iterator(); var3
            .hasNext(); var2 += var4.villagePieceWeight) {
            var4 = var3.next();

            if (var4.villagePiecesLimit > 0 && var4.villagePiecesSpawned < var4.villagePiecesLimit) {
                var1 = true;
            }
        }

        return var1 ? var2 : -1;
    }

    private static StructureComponentVillage func_75083_a(
        StructureComponentVillageStartPiece par0ComponentVillageStartPiece,
        StructureVillagePieceWeight par1StructureVillagePieceWeight, List<StructureComponent> par2List,
        Random par3Random, int par4, int par5, int par6, int par7, int par8) {
        return par0ComponentVillageStartPiece
            .constructComponent(par1StructureVillagePieceWeight, par2List, par3Random, par4, par5, par6, par7, par8);
    }

    private static StructureComponentVillage getNextVillageComponent(
        StructureComponentVillageStartPiece par0ComponentVillageStartPiece, List<StructureComponent> par1List,
        Random par2Random, int par3, int par4, int par5, int par6, int par7) {
        final int var8 = StructureVillagePiecesBase
            .func_75079_a(par0ComponentVillageStartPiece.structureVillageWeightedPieceList);

        if (var8 <= 0) {
            return null;
        } else {
            int var9 = 0;

            while (var9 < 5) {
                ++var9;
                int var10 = par2Random.nextInt(var8);
                final Iterator<StructureVillagePieceWeight> var11 = par0ComponentVillageStartPiece.structureVillageWeightedPieceList
                    .iterator();

                while (var11.hasNext()) {
                    final StructureVillagePieceWeight var12 = var11.next();
                    var10 -= var12.villagePieceWeight;

                    if (var10 < 0) {
                        if (!var12.canSpawnMoreVillagePiecesOfType(par7)
                            || var12 == par0ComponentVillageStartPiece.structVillagePieceWeight
                                && par0ComponentVillageStartPiece.structureVillageWeightedPieceList.size() > 1) {
                            break;
                        }

                        final StructureComponentVillage var13 = StructureVillagePiecesBase.func_75083_a(
                            par0ComponentVillageStartPiece,
                            var12,
                            par1List,
                            par2Random,
                            par3,
                            par4,
                            par5,
                            par6,
                            par7);

                        if (var13 != null) {
                            ++var12.villagePiecesSpawned;
                            par0ComponentVillageStartPiece.structVillagePieceWeight = var12;

                            if (!var12.canSpawnMoreVillagePieces()) {
                                par0ComponentVillageStartPiece.structureVillageWeightedPieceList.remove(var12);
                            }

                            return var13;
                        }
                    }
                }
            }

            final StructureBoundingBox var14 = StructureComponentVillageTorch
                .func_74904_a(par0ComponentVillageStartPiece, par1List, par2Random, par3, par4, par5, par6);

            if (var14 != null) {
                return par0ComponentVillageStartPiece.constructTorch(par7, par2Random, var14, par6);
            } else {
                return null;
            }
        }
    }

    /**
     * attempts to find a next Structure Component to be spawned, private Village function
     */
    private static StructureComponent getNextVillageStructureComponent(
        StructureComponentVillageStartPiece par0ComponentVillageStartPiece, List<StructureComponent> par1List,
        Random par2Random, int par3, int par4, int par5, int par6, int par7) {
        if (par7 > 50) {
            return null;
        } else if (Math.abs(par3 - par0ComponentVillageStartPiece.getBoundingBox().minX) <= 112
            && Math.abs(par5 - par0ComponentVillageStartPiece.getBoundingBox().minZ) <= 112) {
                final StructureComponentVillage var8 = StructureVillagePiecesBase.getNextVillageComponent(
                    par0ComponentVillageStartPiece,
                    par1List,
                    par2Random,
                    par3,
                    par4,
                    par5,
                    par6,
                    par7 + 1);

                if (var8 != null) {
                    par1List.add(var8);
                    par0ComponentVillageStartPiece.field_74932_i.add(var8);
                    return var8;
                }

                return null;
            } else {
                return null;
            }
    }

    private static StructureComponent getNextComponentVillagePath(
        StructureComponentVillageStartPiece par0ComponentVillageStartPiece, List<StructureComponent> par1List,
        Random par2Random, int par3, int par4, int par5, int par6, int par7) {
        if (par7 > 3 + par0ComponentVillageStartPiece.terrainType) {
            return null;
        } else if (Math.abs(par3 - par0ComponentVillageStartPiece.getBoundingBox().minX) <= 112
            && Math.abs(par5 - par0ComponentVillageStartPiece.getBoundingBox().minZ) <= 112) {
                final StructureBoundingBox var8 = StructureComponentVillagePathGen
                    .func_74933_a(par0ComponentVillageStartPiece, par1List, par2Random, par3, par4, par5, par6);

                if (var8 != null && var8.minY > 10) {
                    final StructureComponentVillagePathGen var9 = par0ComponentVillageStartPiece
                        .constructPath(par7, par2Random, var8, par6);

                    par1List.add(var9);
                    par0ComponentVillageStartPiece.field_74930_j.add(var9);
                    return var9;
                }

                return null;
            } else {
                return null;
            }
    }

    /**
     * attempts to find a next Structure Component to be spawned
     */
    static StructureComponent getNextStructureComponent(
        StructureComponentVillageStartPiece par0ComponentVillageStartPiece, List<StructureComponent> par1List,
        Random par2Random, int par3, int par4, int par5, int par6, int par7) {
        return StructureVillagePiecesBase.getNextVillageStructureComponent(
            par0ComponentVillageStartPiece,
            par1List,
            par2Random,
            par3,
            par4,
            par5,
            par6,
            par7);
    }

    static StructureComponent getNextStructureComponentVillagePath(
        StructureComponentVillageStartPiece par0ComponentVillageStartPiece, List<StructureComponent> par1List,
        Random par2Random, int par3, int par4, int par5, int par6, int par7) {
        return StructureVillagePiecesBase.getNextComponentVillagePath(
            par0ComponentVillageStartPiece,
            par1List,
            par2Random,
            par3,
            par4,
            par5,
            par6,
            par7);
    }
}
