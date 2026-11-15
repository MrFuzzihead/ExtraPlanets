package com.mjr.extraplanets.planets.Kepler22b.worldgen.village;

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
 * The Kepler22b piece table. The growth rules live in {@link StructureVillagePiecesBase}; this supplies what is
 * genuinely Kepler22b-specific - which pieces may spawn and how often, the dispatch that builds them, and the two
 * fallbacks.
 */
public class StructureVillagePiecesKepler22b extends StructureVillagePiecesBase {

    public static final StructureVillagePiecesKepler22b INSTANCE = new StructureVillagePiecesKepler22b();

    public static ArrayList<StructureVillagePieceWeightKepler22b> getStructureVillageWeightedPieceList(
        Random par0Random, int par1) {
        return INSTANCE.weightedPieceList(par0Random, par1);
    }

    @Override
    public ArrayList<StructureVillagePieceWeightKepler22b> weightedPieceList(Random par0Random, int par1) {
        final ArrayList<StructureVillagePieceWeightKepler22b> var2 = new ArrayList<StructureVillagePieceWeightKepler22b>();
        var2.add(
            new StructureVillagePieceWeightKepler22b(
                StructureComponentKepler22bVillageWoodHut.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 2 + par1, 5 + par1 * 3)));
        var2.add(
            new StructureVillagePieceWeightKepler22b(
                StructureComponentKepler22bVillageField.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 3 + par1, 5 + par1)));
        var2.add(
            new StructureVillagePieceWeightKepler22b(
                StructureComponentKepler22bVillageHouse.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 3 + par1, 4 + par1 * 2)));

        final Iterator<StructureVillagePieceWeightKepler22b> var3 = var2.iterator();

        while (var3.hasNext()) {
            if (var3.next().villagePiecesLimit == 0) {
                var3.remove();
            }
        }

        return var2;
    }

    static int func_75079_a(List<StructureVillagePieceWeightKepler22b> par0List) {
        return INSTANCE.sumLimits(par0List);
    }

    static StructureComponent getNextStructureComponent(
        StructureComponentKepler22bVillageStartPiece par0ComponentVillageStartPiece, List<StructureComponent> par1List,
        Random par2Random, int par3, int par4, int par5, int par6, int par7) {
        return INSTANCE.next(par0ComponentVillageStartPiece, par1List, par2Random, par3, par4, par5, par6, par7);
    }

    static StructureComponent getNextStructureComponentVillagePath(
        StructureComponentKepler22bVillageStartPiece par0ComponentVillageStartPiece, List<StructureComponent> par1List,
        Random par2Random, int par3, int par4, int par5, int par6, int par7) {
        return INSTANCE.nextPath(par0ComponentVillageStartPiece, par1List, par2Random, par3, par4, par5, par6, par7);
    }

    @Override
    protected StructureComponent create(StructureVillagePieceWeightBase weight, StructureComponent startPiece,
        List<StructureComponent> components, Random rand, int x, int y, int z, int mode, int type) {
        final Class<?> clazz = weight.villagePieceClass;
        final StructureComponentKepler22bVillageStartPiece start = (StructureComponentKepler22bVillageStartPiece) startPiece;

        if (clazz == StructureComponentKepler22bVillageWoodHut.class) {
            return StructureComponentKepler22bVillageWoodHut.func_74908_a(start, components, rand, x, y, z, mode, type);
        } else if (clazz == StructureComponentKepler22bVillageField.class) {
            return StructureComponentKepler22bVillageField.func_74900_a(start, components, rand, x, y, z, mode, type);
        } else if (clazz == StructureComponentKepler22bVillageHouse.class) {
            return StructureComponentKepler22bVillageHouse.func_74921_a(start, components, rand, x, y, z, mode, type);
        }

        return null;
    }

    @Override
    protected StructureComponent createTorch(StructureComponent startPiece, List<StructureComponent> components,
        Random rand, int x, int y, int z, int mode, int type) {
        final StructureBoundingBox box = StructureComponentKepler22bVillageTorch
            .func_74904_a((StructureComponentKepler22bVillageStartPiece) startPiece, components, rand, x, y, z, mode);

        return box == null ? null
            : new StructureComponentKepler22bVillageTorch(
                (StructureComponentKepler22bVillageStartPiece) startPiece,
                type,
                rand,
                box,
                mode);
    }

    @Override
    protected StructureComponent createPath(StructureComponent startPiece, List<StructureComponent> components,
        Random rand, int x, int y, int z, int mode, int type) {
        final StructureBoundingBox box = StructureComponentKepler22bVillagePathGen
            .func_74933_a((StructureComponentKepler22bVillageStartPiece) startPiece, components, rand, x, y, z, mode);

        if (box != null && box.minY > 10) {
            return new StructureComponentKepler22bVillagePathGen(
                (StructureComponentKepler22bVillageStartPiece) startPiece,
                type,
                rand,
                box,
                mode);
        }

        return null;
    }
}
