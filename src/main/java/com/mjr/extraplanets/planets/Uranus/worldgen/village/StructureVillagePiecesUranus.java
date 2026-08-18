package com.mjr.extraplanets.planets.Uranus.worldgen.village;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.util.MathHelper;

public class StructureVillagePiecesUranus {

    public static ArrayList<StructureVillagePieceWeightUranus> getStructureVillageWeightedPieceList(Random par0Random,
        int par1) {
        final ArrayList<StructureVillagePieceWeightUranus> var2 = new ArrayList<StructureVillagePieceWeightUranus>();
        var2.add(
            new StructureVillagePieceWeightUranus(
                StructureComponentUranusVillageWoodHut.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 2 + par1, 5 + par1 * 3)));
        var2.add(
            new StructureVillagePieceWeightUranus(
                StructureComponentUranusVillageField.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 3 + par1, 5 + par1)));
        var2.add(
            new StructureVillagePieceWeightUranus(
                StructureComponentUranusVillageHouse.class,
                5,
                MathHelper.getRandomIntegerInRange(par0Random, 3 + par1, 4 + par1 * 2)));

        final Iterator<StructureVillagePieceWeightUranus> var3 = var2.iterator();

        while (var3.hasNext()) {
            if (var3.next().villagePiecesLimit == 0) {
                var3.remove();
            }
        }

        return var2;
    }
}
