package com.mjr.extraplanets.compatibility.ic2;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.mjr.extraplanets.items.ExtraPlanets_Items;

import cpw.mods.fml.common.Loader;
import ic2.api.recipe.RecipeInputOreDict;
import ic2.api.recipe.Recipes;

/**
 * IndustrialCraft 2 machine recipes for the aluminum/titanium ore processing chain.
 *
 * <p>
 * This mirrors the Galacticraft IC2 ore processing flow, but using this mod's own items (crushed /
 * purified / dust / tiny dust). Recipes are registered through IC2's public API:
 * <ul>
 * <li>Macerator: ore -&gt; 2x crushed ore</li>
 * <li>Ore Washer: crushed ore -&gt; purified ore, 2x tiny (primary) dust, stone dust</li>
 * <li>Thermal Centrifuge: purified ore -&gt; metal dust, tiny (secondary) dust, stone dust</li>
 * </ul>
 * Aluminum is the secondary tiny dust of titanium, and titanium is the secondary tiny dust of
 * aluminum.
 *
 * <p>
 * All IC2 API references are confined to this class, which is only invoked when IC2 is loaded, so
 * this adds no hard runtime dependency.
 */
public class IC2MachineRecipes {

    /** Mod id of IndustrialCraft 2. */
    private static final String MOD_ID_IC2 = "IC2";

    public static void init() {
        if (!Loader.isModLoaded(MOD_ID_IC2)) {
            return;
        }

        addMaceratorRecipes();
        addOreWashingRecipes();
        addCentrifugeRecipes();
    }

    private static void addMaceratorRecipes() {
        // Aluminum ore (Galacticraft: GCBlocks.basicBlock meta 7, oredict "oreAluminum") -> 2x crushed
        addMaceratorRecipe("oreAluminum", ExtraPlanets_Items.crushedAluminumOre);

        // Titanium ore. Galacticraft's own titanium-bearing ore (ilmenite) is registered as
        // "oreIlmenite", while other mods commonly expose "oreTitanium", so accept both.
        addMaceratorRecipe("oreIlmenite", ExtraPlanets_Items.crushedTitaniumOre);
        addMaceratorRecipe("oreTitanium", ExtraPlanets_Items.crushedTitaniumOre);
    }

    private static void addMaceratorRecipe(String oreDict, Item crushedOre) {
        Recipes.macerator.addRecipe(new RecipeInputOreDict(oreDict, 1), null, new ItemStack(crushedOre, 2));
    }

    private static void addOreWashingRecipes() {
        addOreWashingRecipe(
            "crushedAluminum",
            ExtraPlanets_Items.purifiedAluminumOre,
            ExtraPlanets_Items.aluminumTinyDust);
        addOreWashingRecipe(
            "crushedTitanium",
            ExtraPlanets_Items.purifiedTitaniumOre,
            ExtraPlanets_Items.titaniumTinyDust);
    }

    private static void addOreWashingRecipe(String input, Item purifiedOre, Item primaryTinyDust) {
        final ItemStack stoneDust = stoneDust();
        if (stoneDust == null) {
            Recipes.oreWashing.addRecipe(
                new RecipeInputOreDict(input, 1),
                null,
                new ItemStack(purifiedOre),
                new ItemStack(primaryTinyDust, 2));
        } else {
            Recipes.oreWashing.addRecipe(
                new RecipeInputOreDict(input, 1),
                null,
                new ItemStack(purifiedOre),
                new ItemStack(primaryTinyDust, 2),
                stoneDust);
        }
    }

    private static void addCentrifugeRecipes() {
        // Aluminum: purified aluminum -> aluminum dust + tiny titanium dust (secondary) + stone dust
        addCentrifugeRecipe(
            "crushedPurifiedAluminum",
            ExtraPlanets_Items.aluminumDust,
            ExtraPlanets_Items.titaniumTinyDust);
        // Titanium: purified titanium -> titanium dust + tiny aluminum dust (secondary) + stone dust
        addCentrifugeRecipe(
            "crushedPurifiedTitanium",
            ExtraPlanets_Items.titaniumDust,
            ExtraPlanets_Items.aluminumTinyDust);
    }

    private static void addCentrifugeRecipe(String input, Item dust, Item secondaryTinyDust) {
        final ItemStack stoneDust = stoneDust();
        if (stoneDust == null) {
            Recipes.centrifuge.addRecipe(
                new RecipeInputOreDict(input, 1),
                null,
                new ItemStack(dust),
                new ItemStack(secondaryTinyDust, 1));
        } else {
            Recipes.centrifuge.addRecipe(
                new RecipeInputOreDict(input, 1),
                null,
                new ItemStack(dust),
                new ItemStack(secondaryTinyDust, 1),
                stoneDust);
        }
    }

    /**
     * Returns IC2's crushed stone dust (byproducts of the Ore Washer / Thermal Centrifuge), or
     * {@code null} if it is unavailable.
     */
    private static ItemStack stoneDust() {
        final List<ItemStack> ores = OreDictionary.getOres("dustStone");
        return ores.isEmpty() ? null
            : ores.get(0)
                .copy();
    }
}
