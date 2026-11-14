package com.mjr.extraplanets.recipes;

import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.items.ExtraPlanets_Items;

import micdoodle8.mods.galacticraft.api.recipe.CircuitFabricatorRecipes;
import micdoodle8.mods.galacticraft.core.items.GCItems;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import micdoodle8.mods.galacticraft.planets.asteroids.items.AsteroidsItems;

/**
 * Circuit fabricator recipes.
 *
 * <p>
 * Split out of the single 2,844-line recipe class (§6.1 of {@code CODE_REVIEW.md}); the methods inside are unchanged
 * and are still called in the same order, because crafting recipes are matched by position.
 * </p>
 */
public class RecipesCircuitFabricator {

    private RecipesCircuitFabricator() {}

    static void addRecipes() {
        registerCircuitFabricatorRecipes();
    }

    static void registerCircuitFabricatorRecipes() {
        List<ItemStack> silicons = OreDictionary.getOres(ConfigManagerCore.otherModsSilicon);
        int siliconCount = silicons.size();
        for (int j = 0; j <= siliconCount; j++) {
            ItemStack silicon;
            if (j == 0) silicon = new ItemStack(GCItems.basicItem, 1, 2);
            else silicon = silicons.get(j - 1);
            CircuitFabricatorRecipes.addRecipe(
                new ItemStack(ExtraPlanets_Items.wafers, 3, 0),
                new ItemStack[] { new ItemStack(Items.diamond), silicon, silicon, new ItemStack(Items.redstone),
                    new ItemStack(Blocks.redstone_lamp) });
            if (Config.venus) CircuitFabricatorRecipes.addRecipe(
                new ItemStack(ExtraPlanets_Items.wafers, 3, 1),
                new ItemStack[] { new ItemStack(ExtraPlanets_Items.tier4Items, 1, 5), silicon, silicon,
                    new ItemStack(Items.redstone), new ItemStack(Items.comparator) });
            CircuitFabricatorRecipes.addRecipe(
                new ItemStack(ExtraPlanets_Items.wafers, 3, 2),
                new ItemStack[] { new ItemStack(AsteroidsItems.basicItem, 1, 5), silicon, silicon,
                    new ItemStack(Items.redstone), new ItemStack(Blocks.redstone_torch) });
            if (Config.jupiter) CircuitFabricatorRecipes.addRecipe(
                new ItemStack(ExtraPlanets_Items.wafers, 3, 3),
                new ItemStack[] { new ItemStack(ExtraPlanets_Items.tier5Items, 1, 8), silicon, silicon,
                    new ItemStack(Items.redstone), new ItemStack(Items.repeater) });
            if (Config.neptune) {
                if (Config.morePlanetsCompatibilityAdv143) CircuitFabricatorRecipes.addRecipe(
                    new ItemStack(ExtraPlanets_Items.wafers, 3, 4),
                    new ItemStack[] { new ItemStack(ExtraPlanets_Items.tier8Items, 1, 2), silicon, silicon,
                        new ItemStack(Items.redstone), new ItemStack(Items.repeater) });
                else CircuitFabricatorRecipes.addRecipe(
                    new ItemStack(ExtraPlanets_Items.wafers, 3, 4),
                    new ItemStack[] { new ItemStack(ExtraPlanets_Items.tier8Items, 1, 6), silicon, silicon,
                        new ItemStack(Items.redstone), new ItemStack(Items.repeater) });
            }
            if (Config.uranus) {
                if (Config.morePlanetsCompatibilityAdv143) CircuitFabricatorRecipes.addRecipe(
                    new ItemStack(ExtraPlanets_Items.wafers, 3, 5),
                    new ItemStack[] { new ItemStack(ExtraPlanets_Items.tier7Items, 1, 3), silicon, silicon,
                        new ItemStack(Items.redstone), new ItemStack(Items.repeater) });
                else CircuitFabricatorRecipes.addRecipe(
                    new ItemStack(ExtraPlanets_Items.wafers, 3, 5),
                    new ItemStack[] { new ItemStack(ExtraPlanets_Items.tier7Items, 1, 7), silicon, silicon,
                        new ItemStack(Items.redstone), new ItemStack(Items.repeater) });
            }
        }
    }
}
