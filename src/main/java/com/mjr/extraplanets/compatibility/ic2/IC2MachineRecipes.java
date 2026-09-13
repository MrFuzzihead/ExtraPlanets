package com.mjr.extraplanets.compatibility.ic2;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import com.mjr.extraplanets.items.ExtraPlanets_Items;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import ic2.api.recipe.RecipeInputOreDict;
import ic2.api.recipe.Recipes;
import micdoodle8.mods.galacticraft.core.util.GCLog;

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
 * IC2's ore washer and thermal centrifuge read per-recipe values from the recipe metadata NBT
 * ("amount" = water usage in mB, "minHeat" = required heat). The centrifuge dereferences the
 * metadata without a null check in {@code updateEntityServer()}, so both recipe types must always
 * be registered with metadata set, mirroring IC2's own recipes.
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
        addSmeltingRecipes();
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
        // IC2's ore washer reads the water usage (in mB) from the recipe metadata ("amount"); with
        // null metadata its getOutput() rejects the recipe, so the machine would never process it.
        final NBTTagCompound metadata = new NBTTagCompound();
        metadata.setInteger("amount", 1000);
        final ItemStack stoneDust = stoneDust();
        if (stoneDust == null) {
            Recipes.oreWashing.addRecipe(
                new RecipeInputOreDict(input, 1),
                metadata,
                new ItemStack(purifiedOre),
                new ItemStack(primaryTinyDust, 2));
        } else {
            Recipes.oreWashing.addRecipe(
                new RecipeInputOreDict(input, 1),
                metadata,
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
            ExtraPlanets_Items.titaniumTinyDust,
            1500);
        // Titanium: purified titanium -> titanium dust + tiny aluminum dust (secondary) + stone dust
        addCentrifugeRecipe(
            "crushedPurifiedTitanium",
            ExtraPlanets_Items.titaniumDust,
            ExtraPlanets_Items.aluminumTinyDust,
            2000);
    }

    /**
     * Registers furnace recipes smelting the IC2 ore processing intermediates back into ingots
     * (1:1). Inputs are resolved through the OreDictionary ("crushedAluminum", "crushedPurifiedAluminum",
     * "crushedTitanium", "crushedPurifiedTitanium") so any mod's items registered under those names
     * smelt. Furnace recipes only accept concrete ItemStacks (no oredict outputs), so the output is
     * resolved once at registration time to the first OreDictionary entry of
     * "ingotAluminum"/"ingotTitanium" (Galacticraft's ingots). Recipes are skipped (with a one-time
     * log message) when an ore name has no entries, mirroring
     * {@code ExtraPlanets_Recipes#addOreSmelting}.
     */
    private static void addSmeltingRecipes() {
        addOreInputSmelting("crushedAluminum", "ingotAluminum");
        addOreInputSmelting("crushedPurifiedAluminum", "ingotAluminum");
        addOreInputSmelting("crushedTitanium", "ingotTitanium");
        addOreInputSmelting("crushedPurifiedTitanium", "ingotTitanium");
    }

    private static void addOreInputSmelting(String inputOre, String outputOre) {
        final List<ItemStack> inputs = OreDictionary.getOres(inputOre);
        final List<ItemStack> outputs = OreDictionary.getOres(outputOre);
        if (inputs.isEmpty() || outputs.isEmpty()) {
            if (inputs.isEmpty()) {
                GCLog.severe(
                    "[ExtraPlanets] The OreDictionary has no entries for '" + inputOre
                        + "', so the furnace recipe smelting it into '"
                        + outputOre
                        + "' was skipped.");
            }
            if (outputs.isEmpty()) {
                GCLog.severe(
                    "[ExtraPlanets] The OreDictionary has no entries for '" + outputOre
                        + "', so the furnace recipe smelting '"
                        + inputOre
                        + "' into it was skipped.");
            }
            return;
        }
        for (final ItemStack input : inputs) {
            GameRegistry.addSmelting(input, outputs.get(0), 0.0F);
        }
    }

    private static void addCentrifugeRecipe(String input, Item dust, Item secondaryTinyDust, int minHeat) {
        // IC2's thermal centrifuge reads the required heat from the recipe metadata ("minHeat") in
        // updateEntityServer() without a null check, so a null metadata crashes the game with an NPE
        // as soon as the machine starts processing. IC2's own recipes always set it (e.g. iron 1500,
        // gold 2000); use comparable values here.
        final NBTTagCompound metadata = new NBTTagCompound();
        metadata.setInteger("minHeat", minHeat);
        final ItemStack stoneDust = stoneDust();
        if (stoneDust == null) {
            Recipes.centrifuge.addRecipe(
                new RecipeInputOreDict(input, 1),
                metadata,
                new ItemStack(dust),
                new ItemStack(secondaryTinyDust, 1));
        } else {
            Recipes.centrifuge.addRecipe(
                new RecipeInputOreDict(input, 1),
                metadata,
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
