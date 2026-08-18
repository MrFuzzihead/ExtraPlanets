package com.mjr.extraplanets.planets.Kepler22b.worldgen.village;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.util.MathHelper;

public class StructureVillagePiecesKepler22b {

    public static ArrayList<StructureVillagePieceWeightKepler22b> getStructureVillageWeightedPieceList(
        Random par0Random, int par1) {
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
}
