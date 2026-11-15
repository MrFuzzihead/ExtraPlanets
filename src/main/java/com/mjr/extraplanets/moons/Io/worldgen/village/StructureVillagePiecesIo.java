package com.mjr.extraplanets.moons.Io.worldgen.village;

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
 * The Io piece table. The growth rules live in {@link StructureVillagePiecesBase}; this supplies what is
 * genuinely Io-specific - which pieces may spawn and how often, the dispatch that builds them, and the two
 * fallbacks.
 */
public class StructureVillagePiecesIo extends StructureVillagePiecesBase {

    public static final StructureVillagePiecesIo INSTANCE = new StructureVillagePiecesIo();

    public static ArrayList<StructureVillagePieceWeightIo> getStructureVillageWeightedPieceList(Random par0Random,
        int par1) {
        return INSTANCE.weightedPieceList(par0Random, par1);
    }

    @Override
    public ArrayList<StructureVillagePieceWeightIo> weightedPieceList(Random par0Random, int par1) {

        final ArrayList<StructureVillagePieceWeightIo> var2 = new ArrayList<StructureVillagePieceWeightIo>();
        var2.add(
            new StructureVillagePieceWeightIo(
                StructureComponentIoVillageWoodHut.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 2 + par1, 5 + par1 * 3)));
        var2.add(
            new StructureVillagePieceWeightIo(
                StructureComponentIoVillageField.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 3 + par1, 5 + par1)));
        var2.add(
            new StructureVillagePieceWeightIo(
                StructureComponentIoVillageHouse.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 3 + par1, 4 + par1 * 2)));

        final Iterator<StructureVillagePieceWeightIo> var3 = var2.iterator();

        while (var3.hasNext()) {
            if (var3.next().villagePiecesLimit == 0) {
                var3.remove();
            }
        }

        return var2;
    }

    static int func_75079_a(List<StructureVillagePieceWeightIo> par0List) {
        return INSTANCE.sumLimits(par0List);
    }

    static StructureComponent getNextStructureComponent(
        StructureComponentIoVillageStartPiece par0ComponentVillageStartPiece, List<StructureComponent> par1List,
        Random par2Random, int par3, int par4, int par5, int par6, int par7) {
        return INSTANCE.next(par0ComponentVillageStartPiece, par1List, par2Random, par3, par4, par5, par6, par7);
    }

    static StructureComponent getNextStructureComponentVillagePath(
        StructureComponentIoVillageStartPiece par0ComponentVillageStartPiece, List<StructureComponent> par1List,
        Random par2Random, int par3, int par4, int par5, int par6, int par7) {
        return INSTANCE.nextPath(par0ComponentVillageStartPiece, par1List, par2Random, par3, par4, par5, par6, par7);
    }

    @Override
    protected StructureComponent create(StructureVillagePieceWeightBase weight, StructureComponent startPiece,
        List<StructureComponent> components, Random rand, int x, int y, int z, int mode, int type) {
        final Class<?> clazz = weight.villagePieceClass;
        final StructureComponentIoVillageStartPiece start = (StructureComponentIoVillageStartPiece) startPiece;

        if (clazz == StructureComponentIoVillageWoodHut.class) {
            return StructureComponentIoVillageWoodHut.func_74908_a(start, components, rand, x, y, z, mode, type);
        } else if (clazz == StructureComponentIoVillageField.class) {
            return StructureComponentIoVillageField.func_74900_a(start, components, rand, x, y, z, mode, type);
        } else if (clazz == StructureComponentIoVillageHouse.class) {
            return StructureComponentIoVillageHouse.func_74921_a(start, components, rand, x, y, z, mode, type);
        }

        return null;
    }

    @Override
    protected StructureComponent createTorch(StructureComponent startPiece, List<StructureComponent> components,
        Random rand, int x, int y, int z, int mode, int type) {
        final StructureBoundingBox box = StructureComponentIoVillageTorch
            .func_74904_a((StructureComponentIoVillageStartPiece) startPiece, components, rand, x, y, z, mode);

        return box == null ? null
            : new StructureComponentIoVillageTorch(
                (StructureComponentIoVillageStartPiece) startPiece,
                type,
                rand,
                box,
                mode);
    }

    @Override
    protected StructureComponent createPath(StructureComponent startPiece, List<StructureComponent> components,
        Random rand, int x, int y, int z, int mode, int type) {
        final StructureBoundingBox box = StructureComponentIoVillagePathGen
            .func_74933_a((StructureComponentIoVillageStartPiece) startPiece, components, rand, x, y, z, mode);

        if (box != null && box.minY > 10) {
            return new StructureComponentIoVillagePathGen(
                (StructureComponentIoVillageStartPiece) startPiece,
                type,
                rand,
                box,
                mode);
        }

        return null;
    }
}
