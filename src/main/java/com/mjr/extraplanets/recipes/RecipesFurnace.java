package com.mjr.extraplanets.recipes;

import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.blocks.ExtraPlanets_Blocks;
import com.mjr.extraplanets.items.ExtraPlanets_Items;

import cpw.mods.fml.common.registry.GameRegistry;
import micdoodle8.mods.galacticraft.core.util.GCLog;

/**
 * Recipes for smelting this mod's ores.
 *
 * <p>
 * Split out of the single 2,844-line recipe class (§6.1 of {@code CODE_REVIEW.md}); the methods inside are unchanged
 * and are still called in the same order, because crafting recipes are matched by position.
 * </p>
 */
public class RecipesFurnace {

    private RecipesFurnace() {}

    /** Ore names already reported as missing, so each missing entry is only logged once. */
    private static final java.util.Set<String> MISSING_ORE_DICT_WARNED = new java.util.HashSet<String>();

    static void addRecipes() {
        registerFurnaceRecipes();
    }

    static void registerFurnaceRecipes() {
        if (Config.mercury) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.mercuryBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.mercuryBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.mercuryBlocks, 1, 3), "ingotIron", 0.0F);
            GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.mercuryBlocks, 1, 6),
                new ItemStack(ExtraPlanets_Items.ingotMercury),
                0.0F);
        }
        if (Config.venus) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.venusBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.venusBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.venusBlocks, 1, 3), "ingotIron", 0.0F);
            GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.venusBlocks, 1, 6),
                new ItemStack(ExtraPlanets_Items.tier4Items, 1, 5),
                0.0F);

            GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.volcanicRock),
                new ItemStack(ExtraPlanets_Items.tier4Items, 1, 11),
                0.0F);
        }
        if (Config.ceres) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.ceresBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.ceresBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.ceresBlocks, 1, 3), "ingotIron", 0.0F);
            GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.ceresBlocks, 1, 6),
                new ItemStack(ExtraPlanets_Items.ingotUranium, 1),
                0.0F);
        }
        if (Config.jupiter) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.jupiterBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.jupiterBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.jupiterBlocks, 1, 3), "ingotIron", 0.0F);
            GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.jupiterBlocks, 1, 6),
                new ItemStack(ExtraPlanets_Items.tier5Items, 1, 5),
                0.0F);
            GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.jupiterBlocks, 1, 7),
                new ItemStack(ExtraPlanets_Items.tier5Items, 1, 7),
                0.0F);
            GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.jupiterBlocks, 1, 10),
                new ItemStack(ExtraPlanets_Items.tier5Items, 1, 8),
                0.0F);
        }
        if (Config.saturn) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.saturnBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.saturnBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.saturnBlocks, 1, 3), "ingotIron", 0.0F);
            if (Config.morePlanetsCompatibilityAdv143) GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.saturnBlocks, 1, 6),
                new ItemStack(ExtraPlanets_Items.tier6Items, 1, 1),
                0.0F);
            else GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.saturnBlocks, 1, 6),
                new ItemStack(ExtraPlanets_Items.tier6Items, 1, 5),
                0.0F);
        }
        if (Config.uranus) {
            if (Config.morePlanetsCompatibilityAdv143) {
                GameRegistry.addSmelting(
                    new ItemStack(ExtraPlanets_Blocks.uranusBlocks, 1, 3),
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 1),
                    0.0F);
                GameRegistry.addSmelting(
                    new ItemStack(ExtraPlanets_Blocks.uranusBlocks, 1, 6),
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 3),
                    0.0F);
            } else {
                GameRegistry.addSmelting(
                    new ItemStack(ExtraPlanets_Blocks.uranusBlocks, 1, 3),
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 5),
                    0.0F);
                GameRegistry.addSmelting(
                    new ItemStack(ExtraPlanets_Blocks.uranusBlocks, 1, 6),
                    new ItemStack(ExtraPlanets_Items.tier7Items, 1, 7),
                    0.0F);
            }
        }
        if (Config.neptune) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.neptuneBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.neptuneBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.neptuneBlocks, 1, 3), "ingotIron", 0.0F);
            if (Config.morePlanetsCompatibilityAdv143) {
                GameRegistry.addSmelting(
                    new ItemStack(ExtraPlanets_Blocks.neptuneBlocks, 1, 6),
                    new ItemStack(ExtraPlanets_Items.tier8Items, 1, 1),
                    0.0F);
                GameRegistry.addSmelting(
                    new ItemStack(ExtraPlanets_Blocks.neptuneBlocks, 1, 9),
                    new ItemStack(ExtraPlanets_Items.tier8Items, 1, 2),
                    0.0F);
            } else {
                GameRegistry.addSmelting(
                    new ItemStack(ExtraPlanets_Blocks.neptuneBlocks, 1, 6),
                    new ItemStack(ExtraPlanets_Items.tier8Items, 1, 5),
                    0.0F);
                GameRegistry.addSmelting(
                    new ItemStack(ExtraPlanets_Blocks.neptuneBlocks, 1, 9),
                    new ItemStack(ExtraPlanets_Items.tier8Items, 1, 6),
                    0.0F);
            }
        }
        if (Config.pluto) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.plutoBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.plutoBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.plutoBlocks, 1, 3), "ingotIron", 0.0F);
            GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.plutoBlocks, 1, 6),
                new ItemStack(ExtraPlanets_Items.tier9Items, 1, 5),
                0.0F);
        }
        if (Config.eris) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.erisBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.erisBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.erisBlocks, 1, 3), "ingotIron", 0.0F);
            if (Config.morePlanetsCompatibilityAdv143) GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.erisBlocks, 1, 7),
                new ItemStack(ExtraPlanets_Items.tier10Items, 1, 1),
                0.0F);
            else GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.erisBlocks, 1, 7),
                new ItemStack(ExtraPlanets_Items.tier10Items, 1, 5),
                0.0F);
        }
        if (Config.callisto) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.callistoBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.callistoBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.callistoBlocks, 1, 3), "ingotIron", 0.0F);
        }
        if (Config.deimos) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.deimosBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.deimosBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.deimosBlocks, 1, 3), "ingotIron", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.deimosBlocks, 1, 6), "ingotLead", 0.0F);
        }
        if (Config.europa) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.europaBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.europaBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.europaBlocks, 1, 3), "ingotIron", 0.0F);
        }
        if (Config.ganymede) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.ganymedeBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.ganymedeBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.ganymedeBlocks, 1, 3), "ingotIron", 0.0F);
        }
        if (Config.io) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.ioBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.ioBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.ioBlocks, 1, 3), "ingotIron", 0.0F);
        }
        if (Config.phobos) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.phobosBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.phobosBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.phobosBlocks, 1, 3), "ingotIron", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.phobosBlocks, 1, 6), "ingotLead", 0.0F);
        }
        if (Config.triton) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.tritonBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.tritonBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.tritonBlocks, 1, 3), "ingotIron", 0.0F);
        }
        if (Config.rhea) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.rheaBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.rheaBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.rheaBlocks, 1, 3), "ingotIron", 0.0F);
        }
        if (Config.titan) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.titanBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.titanBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.titanBlocks, 1, 3), "ingotIron", 0.0F);
        }
        if (Config.oberon) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.oberonBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.oberonBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.oberonBlocks, 1, 3), "ingotIron", 0.0F);
        }
        if (Config.titania) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.titaniaBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.titaniaBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.titaniaBlocks, 1, 3), "ingotIron", 0.0F);
        }
        if (Config.iapetus) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.iapetusBlocks, 1, 5), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.iapetusBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.iapetusBlocks, 1, 3), "ingotIron", 0.0F);
        }
        if (Config.kepler22b && Config.keplerSolarSystems) {
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.kepler22bBlocks, 1, 3), "ingotCopper", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.kepler22bBlocks, 1, 4), "ingotTin", 0.0F);
            addOreSmelting(new ItemStack(ExtraPlanets_Blocks.kepler22bBlocks, 1, 2), "ingotIron", 0.0F);
            GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.kepler22bBlocks, 1, 12),
                new ItemStack(ExtraPlanets_Blocks.kepler22bBlocks, 1, 1),
                0.0F);
            GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.kepler22bBlocks, 1, 13),
                new ItemStack(ExtraPlanets_Items.tier11Items, 1, 11),
                0.0F);

            GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.kepler22bBlocks, 1, 5),
                new ItemStack(Blocks.coal_block, 1, 0),
                0.0F);

            GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.kepler22bBlocks, 1, 6),
                new ItemStack(ExtraPlanets_Items.tier11Items, 1, 0),
                0.0F);
            GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.kepler22bBlocks, 1, 7),
                new ItemStack(ExtraPlanets_Items.tier11Items, 1, 1),
                0.0F);
            GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.kepler22bBlocks, 1, 8),
                new ItemStack(ExtraPlanets_Items.tier11Items, 1, 2),
                0.0F);
            GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.kepler22bBlocks, 1, 9),
                new ItemStack(ExtraPlanets_Items.tier11Items, 1, 3),
                0.0F);
            GameRegistry.addSmelting(
                new ItemStack(ExtraPlanets_Blocks.kepler22bBlocks, 1, 10),
                new ItemStack(ExtraPlanets_Items.tier11Items, 1, 4),
                0.0F);
        }
    }

    static void addOreSmelting(ItemStack input, String oreName, float xp) {
        List<ItemStack> ores = OreDictionary.getOres(oreName);
        if (ores.isEmpty()) {
            if (MISSING_ORE_DICT_WARNED.add(oreName)) {
                GCLog.severe(
                    "[ExtraPlanets] The OreDictionary has no entries for '" + oreName
                        + "', so the furnace recipe for "
                        + input.getDisplayName()
                        + " was skipped. Is the mod that provides this ore (e.g. Galacticraft) installed "
                        + "with its ore registration enabled?");
            }
            return;
        }
        GameRegistry.addSmelting(input, ores.get(0), xp);
    }
}
