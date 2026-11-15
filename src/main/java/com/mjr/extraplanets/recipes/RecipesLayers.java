package com.mjr.extraplanets.recipes;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.items.ExtraPlanets_Items;

import cpw.mods.fml.common.registry.GameRegistry;
import micdoodle8.mods.galacticraft.api.recipe.CompressorRecipes;
import micdoodle8.mods.galacticraft.core.items.GCItems;

/**
 * Radiation and pressure layer recipes, including their compressor upgrades.
 *
 * <p>
 * Split out of the single 2,844-line recipe class (§6.1 of {@code CODE_REVIEW.md}); the methods inside are unchanged
 * and are still called in the same order, because crafting recipes are matched by position.
 * </p>
 */
public class RecipesLayers {

    private RecipesLayers() {}

    static void addRecipes() {
        registerRadiationLayerRecipes();
        registerPressureLayerRecipes();
    }

    static void registerRadiationLayerRecipes() {
        if (!Config.radiation) {
            return;
        }

        // Cloth from 4 wool (any color) — 2x2 shaped, wildcard for any wool color
        GameRegistry.addRecipe(
            new net.minecraftforge.oredict.ShapedOreRecipe(
                new ItemStack(ExtraPlanets_Items.cloth),
                new Object[] { "WW", "WW", 'W',
                    new ItemStack(Blocks.wool, 1, net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE) }));

        // Tier 1: 6 lead (oreDict) + 3 cloth
        // Shapeless (ShapelessOreRecipe) is the only GC compressor recipe type that
        // supports oreDict — shaped "addRecipe" builds a plain ShapedRecipes with
        // exact ItemStack matching, so only ExtraPlanets lead would be accepted.
        CompressorRecipes.addShapelessRecipe(
            new ItemStack(ExtraPlanets_Items.tier1RadiationLayer),
            "ingotLead",
            "ingotLead",
            "ingotLead",
            "ingotLead",
            "ingotLead",
            "ingotLead",
            new ItemStack(ExtraPlanets_Items.cloth),
            new ItemStack(ExtraPlanets_Items.cloth),
            new ItemStack(ExtraPlanets_Items.cloth));

        // Tier 2-4: upgrade previous tier
        addRadiationLayerCompressorUpgrade(
            ExtraPlanets_Items.tier1RadiationLayer,
            ExtraPlanets_Items.tier2RadiationLayer);
        addRadiationLayerCompressorUpgrade(
            ExtraPlanets_Items.tier2RadiationLayer,
            ExtraPlanets_Items.tier3RadiationLayer);
        addRadiationLayerCompressorUpgrade(
            ExtraPlanets_Items.tier3RadiationLayer,
            ExtraPlanets_Items.tier4RadiationLayer);
    }

    static void registerPressureLayerRecipes() {
        if (!Config.pressure) {
            return;
        }

        // Tier 1: 1 Oxygen Concentrator + 6 cloth
        // Upstream 1.12.2 uses oxygen concentrators (not lead) for pressure layers — an
        // oxygen concentrator pressurizes air, which is what a pressure-proof layer needs.
        CompressorRecipes.addShapelessRecipe(
            new ItemStack(ExtraPlanets_Items.tier1PressureLayer),
            new ItemStack(GCItems.oxygenConcentrator),
            new ItemStack(ExtraPlanets_Items.cloth),
            new ItemStack(ExtraPlanets_Items.cloth),
            new ItemStack(ExtraPlanets_Items.cloth),
            new ItemStack(ExtraPlanets_Items.cloth),
            new ItemStack(ExtraPlanets_Items.cloth),
            new ItemStack(ExtraPlanets_Items.cloth));

        // Tier 2-4: upgrade previous tier
        addPressureLayerCompressorUpgrade(ExtraPlanets_Items.tier1PressureLayer, ExtraPlanets_Items.tier2PressureLayer);
        addPressureLayerCompressorUpgrade(ExtraPlanets_Items.tier2PressureLayer, ExtraPlanets_Items.tier3PressureLayer);
        addPressureLayerCompressorUpgrade(ExtraPlanets_Items.tier3PressureLayer, ExtraPlanets_Items.tier4PressureLayer);
    }

    static void addRadiationLayerCompressorUpgrade(net.minecraft.item.Item prev, net.minecraft.item.Item next) {
        // 6 lead (oreDict) + 2 cloth + previous tier
        CompressorRecipes.addShapelessRecipe(
            new ItemStack(next),
            new ItemStack(prev),
            "ingotLead",
            "ingotLead",
            "ingotLead",
            "ingotLead",
            "ingotLead",
            "ingotLead",
            new ItemStack(ExtraPlanets_Items.cloth),
            new ItemStack(ExtraPlanets_Items.cloth));
    }

    static void addPressureLayerCompressorUpgrade(net.minecraft.item.Item prev, net.minecraft.item.Item next) {
        // 2 Oxygen Concentrators + 6 cloth + previous tier
        CompressorRecipes.addShapelessRecipe(
            new ItemStack(next),
            new ItemStack(prev),
            new ItemStack(GCItems.oxygenConcentrator),
            new ItemStack(GCItems.oxygenConcentrator),
            new ItemStack(ExtraPlanets_Items.cloth),
            new ItemStack(ExtraPlanets_Items.cloth),
            new ItemStack(ExtraPlanets_Items.cloth),
            new ItemStack(ExtraPlanets_Items.cloth),
            new ItemStack(ExtraPlanets_Items.cloth),
            new ItemStack(ExtraPlanets_Items.cloth));
    }
}
