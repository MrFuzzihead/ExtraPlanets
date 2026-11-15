package com.mjr.extraplanets.recipes;

import java.util.HashMap;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.items.ExtraPlanets_Items;

import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.api.recipe.SpaceStationRecipe;
import micdoodle8.mods.galacticraft.api.world.SpaceStationType;
import micdoodle8.mods.galacticraft.planets.mars.MarsModule;

/**
 * Space station and satellite recipes.
 *
 * <p>
 * Split out of the single 2,844-line recipe class (§6.1 of {@code CODE_REVIEW.md}); the methods inside are unchanged
 * and are still called in the same order, because crafting recipes are matched by position.
 * </p>
 */
public class RecipesSatellites {

    private RecipesSatellites() {}

    static void addRecipes() {
        registerSatellitesRecipes();
    }

    static void registerSatellitesRecipes() {
        HashMap<Object, Integer> inputMap;
        if (Config.mercurySpaceStation && Config.mercury) {
            inputMap = new HashMap<Object, Integer>();
            inputMap.put("ingotTin", 32);
            inputMap.put("waferAdvanced", 1);
            inputMap.put(new ItemStack(ExtraPlanets_Items.ingotMercury, 1, 0), 24);
            inputMap.put(Items.iron_ingot, 24);
            GalacticraftRegistry.registerSpaceStation(
                new SpaceStationType(Config.mercurySpaceStationID, Config.mercuryID, new SpaceStationRecipe(inputMap)));
        }
        if (Config.venusSpaceStation && Config.venus) {
            inputMap = new HashMap<Object, Integer>();
            inputMap.put("ingotTin", 32);
            inputMap.put("waferAdvanced", 1);
            inputMap.put(new ItemStack(ExtraPlanets_Items.tier4Items, 1, 5), 24);
            inputMap.put(Items.iron_ingot, 24);
            GalacticraftRegistry.registerSpaceStation(
                new SpaceStationType(Config.venusSpaceStationID, Config.venusID, new SpaceStationRecipe(inputMap)));
        }
        if (Config.ceresSpaceStation && Config.ceres) {
            inputMap = new HashMap<Object, Integer>();
            inputMap.put("ingotTin", 32);
            inputMap.put("waferAdvanced", 1);
            inputMap.put(new ItemStack(ExtraPlanets_Items.ingotUranium, 1, 0), 24);
            inputMap.put(Items.iron_ingot, 24);
            GalacticraftRegistry.registerSpaceStation(
                new SpaceStationType(Config.ceresSpaceStationID, Config.ceresID, new SpaceStationRecipe(inputMap)));
        }
        if (Config.marsSpaceStation && MarsModule.planetMars != null) {
            inputMap = new HashMap<Object, Integer>();
            inputMap.put("ingotTin", 32);
            inputMap.put("waferAdvanced", 1);
            inputMap.put(Items.iron_ingot, 24);
            GalacticraftRegistry.registerSpaceStation(
                new SpaceStationType(
                    Config.marsSpaceStationID,
                    MarsModule.planetMars.getDimensionID(),
                    new SpaceStationRecipe(inputMap)));
        }
        if (Config.jupiterSpaceStation && Config.jupiter) {
            inputMap = new HashMap<Object, Integer>();
            inputMap.put("ingotTin", 32);
            inputMap.put(new ItemStack(ExtraPlanets_Items.tier5Items, 1, 5), 24);
            inputMap.put(new ItemStack(ExtraPlanets_Items.wafers, 1, 3), 3);
            inputMap.put(Items.iron_ingot, 24);
            GalacticraftRegistry.registerSpaceStation(
                new SpaceStationType(Config.jupiterSpaceStationID, Config.jupiterID, new SpaceStationRecipe(inputMap)));
        }
        if (Config.saturnSpaceStation && Config.saturn) {
            inputMap = new HashMap<Object, Integer>();
            inputMap.put("ingotTin", 32);
            inputMap.put("waferAdvanced", 1);
            if (Config.morePlanetsCompatibilityAdv143)
                inputMap.put(new ItemStack(ExtraPlanets_Items.tier6Items, 1, 1), 24);
            else inputMap.put(new ItemStack(ExtraPlanets_Items.tier6Items, 1, 5), 24);
            inputMap.put(Items.iron_ingot, 24);
            GalacticraftRegistry.registerSpaceStation(
                new SpaceStationType(Config.saturnSpaceStationID, Config.saturnID, new SpaceStationRecipe(inputMap)));
        }
        if (Config.uranusSpaceStation && Config.uranus) {
            inputMap = new HashMap<Object, Integer>();
            inputMap.put("ingotTin", 32);
            if (Config.morePlanetsCompatibilityAdv143)
                inputMap.put(new ItemStack(ExtraPlanets_Items.tier7Items, 1, 1), 24);
            else inputMap.put(new ItemStack(ExtraPlanets_Items.tier7Items, 1, 5), 24);
            inputMap.put(new ItemStack(ExtraPlanets_Items.wafers, 1, 5), 3);
            inputMap.put(Items.iron_ingot, 24);
            GalacticraftRegistry.registerSpaceStation(
                new SpaceStationType(Config.uranusSpaceStationID, Config.uranusID, new SpaceStationRecipe(inputMap)));
        }
        if (Config.neptuneSpaceStation && Config.neptune) {
            inputMap = new HashMap<Object, Integer>();
            inputMap.put("ingotTin", 32);
            if (Config.morePlanetsCompatibilityAdv143)
                inputMap.put(new ItemStack(ExtraPlanets_Items.tier8Items, 1, 1), 24);
            else inputMap.put(new ItemStack(ExtraPlanets_Items.tier8Items, 1, 5), 24);
            inputMap.put(new ItemStack(ExtraPlanets_Items.wafers, 1, 4), 3);
            inputMap.put(Items.iron_ingot, 24);
            GalacticraftRegistry.registerSpaceStation(
                new SpaceStationType(Config.neptuneSpaceStationID, Config.neptuneID, new SpaceStationRecipe(inputMap)));
        }
        if (Config.plutoSpaceStation && Config.pluto) {
            inputMap = new HashMap<Object, Integer>();
            inputMap.put("ingotTin", 32);
            inputMap.put("waferAdvanced", 1);
            inputMap.put(new ItemStack(ExtraPlanets_Items.tier9Items, 1, 5), 24);
            inputMap.put(Items.iron_ingot, 24);
            GalacticraftRegistry.registerSpaceStation(
                new SpaceStationType(Config.plutoSpaceStationID, Config.plutoID, new SpaceStationRecipe(inputMap)));
        }
        if (Config.erisSpaceStation && Config.eris) {
            inputMap = new HashMap<Object, Integer>();
            inputMap.put("ingotTin", 32);
            inputMap.put("waferAdvanced", 1);
            inputMap.put(Items.iron_ingot, 24);
            if (Config.morePlanetsCompatibilityAdv143)
                inputMap.put(new ItemStack(ExtraPlanets_Items.tier10Items, 1, 1), 24);
            else inputMap.put(new ItemStack(ExtraPlanets_Items.tier10Items, 1, 5), 24);
            GalacticraftRegistry.registerSpaceStation(
                new SpaceStationType(Config.erisSpaceStationID, Config.erisID, new SpaceStationRecipe(inputMap)));
        }
        if (Config.kepler22bSpaceStation && Config.kepler22b && Config.keplerSolarSystems) {
            inputMap = new HashMap<Object, Integer>();
            inputMap.put(new ItemStack(ExtraPlanets_Items.tier11Items, 1, 0), 24);
            inputMap.put(new ItemStack(ExtraPlanets_Items.tier11Items, 1, 1), 24);
            inputMap.put(new ItemStack(ExtraPlanets_Items.tier11Items, 1, 2), 24);
            inputMap.put(new ItemStack(ExtraPlanets_Items.tier11Items, 1, 3), 24);
            inputMap.put(new ItemStack(ExtraPlanets_Items.tier11Items, 1, 4), 24);
            GalacticraftRegistry.registerSpaceStation(
                new SpaceStationType(
                    Config.kepler22bSpaceStationID,
                    Config.kepler22bID,
                    new SpaceStationRecipe(inputMap)));
        }
    }
}
