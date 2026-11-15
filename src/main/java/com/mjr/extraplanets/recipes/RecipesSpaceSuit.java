package com.mjr.extraplanets.recipes;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.armor.ExtraPlanets_Armor;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Space suit, thermal padding and suit-module recipes.
 *
 * <p>
 * Split out of the single 2,844-line recipe class (§6.1 of {@code CODE_REVIEW.md}); the methods inside are unchanged
 * and are still called in the same order, because crafting recipes are matched by position.
 * </p>
 */
public class RecipesSpaceSuit {

    private RecipesSpaceSuit() {}

    static void addRecipes() {
        registerSpaceSuitRecipes();
    }

    static void registerSpaceSuitRecipes() {
        if (Config.pressure || Config.radiation) {
            Item helmet = ExtraPlanets_Armor.spaceSuitHelmet;
            Item chest = ExtraPlanets_Armor.spaceSuitChest;
            Item leggings = ExtraPlanets_Armor.spaceSuitLegings;
            Item boots = ExtraPlanets_Armor.spaceSuitBoots;
            if (helmet == null || chest == null || leggings == null || boots == null) {
                return;
            }

            // Central iron piece surrounded by an aluminum ring: "III", "IAI", "III"
            // (white = aluminum, A = center iron piece). ShapedOreRecipe is used because it is the
            // Forge recipe type that understands ore-dictionary string ingredients ("ingotAluminum");
            // vanilla CraftingManager cannot handle raw String keys and would NullPointerException.
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(helmet),
                    new Object[] { "III", "IAI", "III", 'I', "ingotAluminum", 'A', new ItemStack(Items.iron_helmet) }));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(chest),
                    new Object[] { "III", "IAI", "III", 'I', "ingotAluminum", 'A',
                        new ItemStack(Items.iron_chestplate) }));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(leggings),
                    new Object[] { "III", "IAI", "III", 'I', "ingotAluminum", 'A',
                        new ItemStack(Items.iron_leggings) }));
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(boots),
                    new Object[] { "III", "IAI", "III", 'I', "ingotAluminum", 'A', new ItemStack(Items.iron_boots) }));
        }
    }
}
