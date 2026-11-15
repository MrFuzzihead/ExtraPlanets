package com.mjr.extraplanets.planets.Mercury.worldgen.village;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.util.MathHelper;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;

import com.mjr.extraplanets.world.village.StructureVillagePieceWeightBase;
import com.mjr.extraplanets.world.village.StructureVillagePiecesBase;

/**
 * The Mercury piece table. The growth rules live in {@link StructureVillagePiecesBase}; this supplies what is
 * genuinely Mercury-specific - which pieces may spawn and how often, the dispatch that builds them, and the two
 * fallbacks.
 */
public class StructureVillagePiecesMercury extends StructureVillagePiecesBase {

    public static final StructureVillagePiecesMercury INSTANCE = new StructureVillagePiecesMercury();

    public static ArrayList<StructureVillagePieceWeightMercury> getStructureVillageWeightedPieceList(Random par0Random,
        int par1) {
        return INSTANCE.weightedPieceList(par0Random, par1);
    }

    @Override
    public ArrayList<StructureVillagePieceWeightMercury> weightedPieceList(Random par0Random, int par1) {

        final ArrayList<StructureVillagePieceWeightMercury> var2 = new ArrayList<StructureVillagePieceWeightMercury>();
        var2.add(
            new StructureVillagePieceWeightMercury(
                StructureComponentMercuryVillageWoodHut.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 2 + par1, 5 + par1 * 3)));
        var2.add(
            new StructureVillagePieceWeightMercury(
                StructureComponentMercuryVillageField.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 3 + par1, 5 + par1)));
        var2.add(
            new StructureVillagePieceWeightMercury(
                StructureComponentMercuryVillageHouse.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 3 + par1, 4 + par1 * 2)));

        final Iterator<StructureVillagePieceWeightMercury> var3 = var2.iterator();

        while (var3.hasNext()) {
            if (var3.next().villagePiecesLimit == 0) {
                var3.remove();
            }
        }

        return var2;
    }

    static int func_75079_a(List<StructureVillagePieceWeightMercury> par0List) {
        return INSTANCE.sumLimits(par0List);
    }

    static StructureComponent getNextStructureComponent(
        StructureComponentMercuryVillageStartPiece par0ComponentVillageStartPiece, List<StructureComponent> par1List,
        Random par2Random, int par3, int par4, int par5, int par6, int par7) {
        return INSTANCE.next(par0ComponentVillageStartPiece, par1List, par2Random, par3, par4, par5, par6, par7);
    }

    static StructureComponent getNextStructureComponentVillagePath(
        StructureComponentMercuryVillageStartPiece par0ComponentVillageStartPiece, List<StructureComponent> par1List,
        Random par2Random, int par3, int par4, int par5, int par6, int par7) {
        return INSTANCE.nextPath(par0ComponentVillageStartPiece, par1List, par2Random, par3, par4, par5, par6, par7);
    }

    @Override
    protected StructureComponent create(StructureVillagePieceWeightBase weight, StructureComponent startPiece,
        List<StructureComponent> components, Random rand, int x, int y, int z, int mode, int type) {
        final Class<?> clazz = weight.villagePieceClass;
        final StructureComponentMercuryVillageStartPiece start = (StructureComponentMercuryVillageStartPiece) startPiece;

        if (clazz == StructureComponentMercuryVillageWoodHut.class) {
            return StructureComponentMercuryVillageWoodHut.func_74908_a(start, components, rand, x, y, z, mode, type);
        } else if (clazz == StructureComponentMercuryVillageField.class) {
            return StructureComponentMercuryVillageField.func_74900_a(start, components, rand, x, y, z, mode, type);
        } else if (clazz == StructureComponentMercuryVillageHouse.class) {
            return StructureComponentMercuryVillageHouse.func_74921_a(start, components, rand, x, y, z, mode, type);
        }

        return null;
    }

    @Override
    protected StructureComponent createTorch(StructureComponent startPiece, List<StructureComponent> components,
        Random rand, int x, int y, int z, int mode, int type) {
        final StructureBoundingBox box = StructureComponentMercuryVillageTorch
            .func_74904_a((StructureComponentMercuryVillageStartPiece) startPiece, components, rand, x, y, z, mode);

        return box == null ? null
            : new StructureComponentMercuryVillageTorch(
                (StructureComponentMercuryVillageStartPiece) startPiece,
                type,
                rand,
                box,
                mode);
    }

    @Override
    protected StructureComponent createPath(StructureComponent startPiece, List<StructureComponent> components,
        Random rand, int x, int y, int z, int mode, int type) {
        final StructureBoundingBox box = StructureComponentMercuryVillagePathGen
            .func_74933_a((StructureComponentMercuryVillageStartPiece) startPiece, components, rand, x, y, z, mode);

        if (box != null && box.minY > 10) {
            return new StructureComponentMercuryVillagePathGen(
                (StructureComponentMercuryVillageStartPiece) startPiece,
                type,
                rand,
                box,
                mode);
        }

        return null;
    }
}
