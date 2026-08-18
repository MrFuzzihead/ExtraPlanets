package com.mjr.extraplanets.planets.Pluto.worldgen.village;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.util.MathHelper;

public class StructureVillagePiecesPluto {

    public static ArrayList<StructureVillagePieceWeightPluto> getStructureVillageWeightedPieceList(Random par0Random,
        int par1) {
        final ArrayList<StructureVillagePieceWeightPluto> var2 = new ArrayList<StructureVillagePieceWeightPluto>();
        var2.add(
            new StructureVillagePieceWeightPluto(
                StructureComponentPlutoVillageWoodHut.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 2 + par1, 5 + par1 * 3)));
        var2.add(
            new StructureVillagePieceWeightPluto(
                StructureComponentPlutoVillageField.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 3 + par1, 5 + par1)));
        var2.add(
            new StructureVillagePieceWeightPluto(
                StructureComponentPlutoVillageHouse.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 3 + par1, 4 + par1 * 2)));

        final Iterator<StructureVillagePieceWeightPluto> var3 = var2.iterator();

        while (var3.hasNext()) {
            if (var3.next().villagePiecesLimit == 0) {
                var3.remove();
            }
        }

        return var2;
    }
}
