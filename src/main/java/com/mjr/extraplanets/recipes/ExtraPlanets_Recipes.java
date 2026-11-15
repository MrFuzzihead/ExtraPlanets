package com.mjr.extraplanets.recipes;

import com.mjr.extraplanets.Config;

public class ExtraPlanets_Recipes {

    public static void init() {
        // Order matters: crafting recipes are matched by position, so each pass must stay in the same place it was.
        if (Config.morePlanetsCompatibilityAdv143 == false) registerRocketCraftingRecipes();
        RecipesFurnace.addRecipes();
        RecipesCrafting.addRecipes();
        RecipesSpaceSuit.addRecipes();
        RecipesLayers.addRecipes();
        RecipesCompressor.addRecipes();
        RecipesCircuitFabricator.addRecipes();
        RecipesSatellites.addRecipes();
    }

    private static void registerRocketCraftingRecipes() {
        if (Config.venus) Tier4RocketRecipes.registerRocketCraftingRecipe();
        if (Config.jupiter) Tier5RocketRecipes.registerRocketCraftingRecipe();
        if (Config.saturn) Tier6RocketRecipes.registerRocketCraftingRecipe();
        if (Config.uranus) Tier7RocketRecipes.registerRocketCraftingRecipe();
        if (Config.neptune) Tier8RocketRecipes.registerRocketCraftingRecipe();
        if (Config.pluto) Tier9RocketRecipes.registerRocketCraftingRecipe();
        if (Config.eris) Tier10RocketRecipes.registerRocketCraftingRecipe();
        if (Config.marsRover) MarsRoverRecipes.registerRoverCraftingRecipe();
        if (Config.venusRover) VenusRoverRecipes.registerRoverCraftingRecipe();
    }
}
