package com.mjr.extraplanets.moons.Io.worldgen.village;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.util.MathHelper;

public class StructureVillagePiecesIo {

    public static ArrayList<StructureVillagePieceWeightIo> getStructureVillageWeightedPieceList(Random par0Random,
        int par1) {
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
}
