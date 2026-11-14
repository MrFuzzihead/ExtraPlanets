package com.mjr.extraplanets.recipes;

import net.minecraft.item.ItemStack;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.items.ExtraPlanets_Items;

import micdoodle8.mods.galacticraft.api.recipe.CompressorRecipes;
import micdoodle8.mods.galacticraft.planets.asteroids.items.AsteroidsItems;

/**
 * Galacticraft compressor recipes.
 *
 * <p>
 * Split out of the single 2,844-line recipe class (§6.1 of {@code CODE_REVIEW.md}); the methods inside are unchanged
 * and are still called in the same order, because crafting recipes are matched by position.
 * </p>
 */
public class RecipesCompressor {

    private RecipesCompressor() {}

    static void addRecipes() {
        registerCompressorRecipes();
    }

    static void registerCompressorRecipes() {
        if (Config.mercury) {
            CompressorRecipes.addShapelessRecipe(
                new ItemStack(ExtraPlanets_Items.compressedMercury, 1, 0),
                new ItemStack(ExtraPlanets_Items.ingotMercury, 1, 0),
                new ItemStack(ExtraPlanets_Items.ingotMercury, 1, 0));
        }
        if (Config.venus) {
            CompressorRecipes.addShapelessRecipe(
                new ItemStack(ExtraPlanets_Items.tier4Items, 1, 4),
                new ItemStack(ExtraPlanets_Items.tier4Items, 1, 5),
                new ItemStack(ExtraPlanets_Items.tier4Items, 1, 5));
            CompressorRecipes.addShapelessRecipe(
                new ItemStack(ExtraPlanets_Items.tier4Items, 1, 3),
                new ItemStack(AsteroidsItems.basicItem, 1, 0),
                new ItemStack(ExtraPlanets_Items.tier4Items, 1, 4));
        }
        if (Config.jupiter) {
            CompressorRecipes.addShapelessRecipe(
                new ItemStack(ExtraPlanets_Items.tier5Items, 1, 4),
                new ItemStack(ExtraPlanets_Items.tier5Items, 1, 5),
                new ItemStack(ExtraPlanets_Items.tier5Items, 1, 5));
            CompressorRecipes.addShapelessRecipe(
                new ItemStack(ExtraPlanets_Items.tier5Items, 1, 6),
                new ItemStack(ExtraPlanets_Items.tier5Items, 1, 7),
                new ItemStack(ExtraPlanets_Items.tier5Items, 1, 7));
            CompressorRecipes.addShapelessRecipe(
                new ItemStack(ExtraPlanets_Items.tier5Items, 1, 3),
                new ItemStack(ExtraPlanets_Items.tier4Items, 1, 3),
                new ItemStack(ExtraPlanets_Items.tier5Items, 1, 4));
        }
        if (Config.saturn) {
            if (Config.morePlanetsCompatibilityAdv143) {
                CompressorRecipes.addShapelessRecipe(
                    new ItemStack(ExtraPlanets_Items.tier6Items, 1, 0),
                    new ItemStack(ExtraPlanets_Items.tier6Items, 1, 1),
                    new ItemStack(ExtraPlanets_Items.tier6Items, 1, 1));
            } else {
                CompressorRecipes.addShapelessRecipe(
                    new ItemStack(ExtraPlanets_Items.tier6Items, 1, 4),
                    new ItemStack(ExtraPlanets_Items.tier6Items, 1, 5),
                    new ItemStack(ExtraPlanets_Items.tier6Items, 1, 5));
                CompressorRecipes.addShapelessRecipe(
                    new ItemStack(ExtraPlanets_Items.tier6Items, 1, 3),
                    new ItemStack(ExtraPlanets_Items.tier5Items, 1, 3),
                    new ItemStack(ExtraPlanets_Items.tier6Items, 1, 4));
            }
        }
        if (Config.uranus) {
            if (Config.morePlanetsCompatibilityAdv143) {
                CompressorRecipes.addShapelessRecipe(
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 0),
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 1),
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 1));
                CompressorRecipes.addShapelessRecipe(
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 2),
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 0),
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 0));
            } else {
                CompressorRecipes.addShapelessRecipe(
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 4),
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 5),
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 5));
                CompressorRecipes.addShapelessRecipe(
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 6),
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 4),
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 4));
                CompressorRecipes.addShapelessRecipe(
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 3),
                    new ItemStack(ExtraPlanets_Items.tier6Items, 1, 3),
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 6));
            }
        }
        if (Config.neptune) {
            if (Config.morePlanetsCompatibilityAdv143) {
                CompressorRecipes.addShapelessRecipe(
                    new ItemStack(ExtraPlanets_Items.tier8Items, 1, 0),
                    new ItemStack(ExtraPlanets_Items.tier8Items, 1, 1),
                    new ItemStack(ExtraPlanets_Items.tier8Items, 1, 1));
            } else {
                CompressorRecipes.addShapelessRecipe(
                    new ItemStack(ExtraPlanets_Items.tier8Items, 1, 4),
                    new ItemStack(ExtraPlanets_Items.tier8Items, 1, 5),
                    new ItemStack(ExtraPlanets_Items.tier8Items, 1, 5));
                CompressorRecipes.addShapelessRecipe(
                    new ItemStack(ExtraPlanets_Items.tier8Items, 1, 3),
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 3),
                    new ItemStack(ExtraPlanets_Items.tier8Items, 1, 4));
            }
        }
        if (Config.pluto) {
            CompressorRecipes.addShapelessRecipe(
                new ItemStack(ExtraPlanets_Items.tier9Items, 1, 4),
                new ItemStack(ExtraPlanets_Items.tier9Items, 1, 5),
                new ItemStack(ExtraPlanets_Items.tier9Items, 1, 5));
            CompressorRecipes.addShapelessRecipe(
                new ItemStack(ExtraPlanets_Items.tier9Items, 1, 3),
                new ItemStack(ExtraPlanets_Items.tier8Items, 1, 3),
                new ItemStack(ExtraPlanets_Items.tier9Items, 1, 4));
        }
        if (Config.eris) {
            if (Config.morePlanetsCompatibilityAdv143) {
                CompressorRecipes.addShapelessRecipe(
                    new ItemStack(ExtraPlanets_Items.tier10Items, 1, 0),
                    new ItemStack(ExtraPlanets_Items.tier10Items, 1, 1),
                    new ItemStack(ExtraPlanets_Items.tier10Items, 1, 1));
            } else {
                CompressorRecipes.addShapelessRecipe(
                    new ItemStack(ExtraPlanets_Items.tier10Items, 1, 4),
                    new ItemStack(ExtraPlanets_Items.tier10Items, 1, 5),
                    new ItemStack(ExtraPlanets_Items.tier10Items, 1, 5));
                CompressorRecipes.addShapelessRecipe(
                    new ItemStack(ExtraPlanets_Items.tier10Items, 1, 3),
                    new ItemStack(ExtraPlanets_Items.tier9Items, 1, 3),
                    new ItemStack(ExtraPlanets_Items.tier10Items, 1, 4));
            }
        }
    }
}
