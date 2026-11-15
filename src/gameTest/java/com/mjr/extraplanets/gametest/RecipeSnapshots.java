package com.mjr.extraplanets.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;

/**
 * Golden snapshot of the registered recipes.
 *
 * <p>
 * The recipe file is a single 2,844-line class that is being split up, and crafting recipes are matched by
 * <em>position</em> in the list - so a dropped recipe does not merely remove itself, it lets an earlier vague recipe
 * match something it never used to. Nothing about that shows up in a compile, so the whole list is fingerprinted in
 * order (output, stack size, damage and recipe class per index) and compared against a golden.
 * </p>
 */
final class RecipeSnapshots {

    private RecipeSnapshots() {}

    static void run(TestReporter reporter) {
        reporter.check("recipes: the crafting recipe list is unchanged (golden)", () -> {
            List<IRecipe> recipes = CraftingManager.getInstance()
                .getRecipeList();
            TestReporter.assertTrue("no crafting recipes at all", recipes.size() > 100);

            // Only this mod's recipes are fingerprinted: the list holds every mod's (40k+ in a dev pack), and another
            // mod's changes are not ours to fail on. Relative order among our own recipes is what carries behaviour.
            int deadRecipes = 0;
            List<Long> fingerprints = new ArrayList<Long>(recipes.size());
            for (IRecipe recipe : recipes) {
                if (recipe == null) {
                    // a recipe list with holes in it: fingerprint the hole rather than blowing up
                    fingerprints.add(Long.valueOf(0));
                    continue;
                }
                ItemStack output = recipe.getRecipeOutput();
                long fingerprint = recipe.getClass()
                    .getSimpleName()
                    .hashCode();
                // Some recipes produce a stack whose item was never registered (a body's block disabled by config).
                // For those, even getItemDamage() throws - it goes through the item's registry delegate - so a null
                // item has to short-circuit both calls.
                if (output != null) {
                    Item item = output.getItem();
                    String name = item == null ? null : Item.itemRegistry.getNameForObject(item);
                    if (name != null && !name.toLowerCase(java.util.Locale.ROOT)
                        .startsWith("extraplanets:")) {
                        continue;
                    }
                    int itemId = item == null ? 0 : Item.getIdFromItem(item);
                    int damage = item == null ? 0 : output.getItemDamage();
                    fingerprint = fingerprint * 31 + itemId * 16L + damage;
                    fingerprint = fingerprint * 31 + output.stackSize;
                    if (item == null) {
                        deadRecipes++;
                    }
                }
                if (output == null) {
                    continue;
                }
                fingerprints.add(Long.valueOf(fingerprint));
            }

            // Order matters and a single recipe is too small a unit to key by, so hash fixed-size windows: a failure
            // then says roughly where in the list the change is.
            final int window = 32;
            Map<String, Long> windows = new TreeMap<String, Long>();
            for (int start = 0; start < fingerprints.size(); start += window) {
                long hash = 1125899906842597L;
                for (int i = start; i < Math.min(start + window, fingerprints.size()); i++) {
                    hash = hash * 31 + fingerprints.get(i)
                        .longValue();
                }
                windows.put("crafting/" + start, Long.valueOf(hash));
            }
            windows.put("crafting/size", Long.valueOf(fingerprints.size()));

            if (deadRecipes > 0) {
                System.out.println(
                    "[EP-GAMETEST] " + deadRecipes
                        + " of "
                        + recipes.size()
                        + " crafting recipes produce a stack whose item is not registered, so they cannot be crafted");
            }

            GoldenFiles.verify(
                windows,
                "recipe-golden.txt",
                key -> key.startsWith("crafting/"),
                "the crafting recipe list changed (recipes are matched by position, so this is a behaviour change)");
        });

        reporter.check("recipes: the furnace recipe set is unchanged (golden)", () -> {
            Map<ItemStack, ItemStack> smelting = FurnaceRecipes.smelting()
                .getSmeltingList();
            TestReporter.assertTrue("no furnace recipes at all", smelting.size() > 20);

            // A HashMap has no order, so key by the input stack and hash the output.
            Map<String, Long> byInput = new TreeMap<String, Long>();
            for (Map.Entry<ItemStack, ItemStack> entry : smelting.entrySet()) {
                ItemStack input = entry.getKey();
                // Only this mod's furnace recipes: the map holds every mod's and another mod's changes are not ours
                // to fail on.
                String inputName = input == null || input.getItem() == null ? null
                    : Item.itemRegistry.getNameForObject(input.getItem());
                if (inputName == null || !inputName.toLowerCase(java.util.Locale.ROOT)
                    .startsWith("extraplanets:")) {
                    continue;
                }
                ItemStack output = entry.getValue();
                String key = "smelting/" + Item.getIdFromItem(input.getItem()) + "@" + input.getItemDamage();
                long hash = output == null ? 0 : Item.getIdFromItem(output.getItem()) * 16L + output.getItemDamage();
                byInput.put(key, Long.valueOf(hash));
            }
            GoldenFiles.verify(
                byInput,
                "recipe-golden.txt",
                key -> key.startsWith("smelting/"),
                "the furnace recipe set changed");
        });
    }
}
