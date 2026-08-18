package com.mjr.extraplanets.moons.Deimos.worldgen.village;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.util.MathHelper;

public class StructureVillagePiecesDeimos {

    public static ArrayList<StructureVillagePieceWeightDeimos> getStructureVillageWeightedPieceList(Random par0Random,
        int par1) {
        final ArrayList<StructureVillagePieceWeightDeimos> var2 = new ArrayList<StructureVillagePieceWeightDeimos>();
        var2.add(
            new StructureVillagePieceWeightDeimos(
                StructureComponentDeimosVillageWoodHut.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 2 + par1, 5 + par1 * 3)));
        var2.add(
            new StructureVillagePieceWeightDeimos(
                StructureComponentDeimosVillageField.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 3 + par1, 5 + par1)));
        var2.add(
            new StructureVillagePieceWeightDeimos(
                StructureComponentDeimosVillageHouse.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 3 + par1, 4 + par1 * 2)));

        final Iterator<StructureVillagePieceWeightDeimos> var3 = var2.iterator();

        while (var3.hasNext()) {
            if (var3.next().villagePiecesLimit == 0) {
                var3.remove();
            }
        }

        return var2;
    }
}
