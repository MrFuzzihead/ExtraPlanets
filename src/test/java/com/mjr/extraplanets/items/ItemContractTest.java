package com.mjr.extraplanets.items;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.Assert;
import org.junit.Test;

import com.mjr.extraplanets.items.keys.ItemKeyT10;
import com.mjr.extraplanets.items.keys.ItemKeyT4;
import com.mjr.extraplanets.items.keys.ItemKeyT5;
import com.mjr.extraplanets.items.keys.ItemKeyT6;
import com.mjr.extraplanets.items.keys.ItemKeyT7;
import com.mjr.extraplanets.items.keys.ItemKeyT8;
import com.mjr.extraplanets.items.keys.ItemKeyT9;
import com.mjr.extraplanets.items.noseCones.Tier10NoseCone;
import com.mjr.extraplanets.items.noseCones.Tier4NoseCone;
import com.mjr.extraplanets.items.noseCones.Tier5NoseCone;
import com.mjr.extraplanets.items.noseCones.Tier6NoseCone;
import com.mjr.extraplanets.items.noseCones.Tier7NoseCone;
import com.mjr.extraplanets.items.noseCones.Tier8NoseCone;
import com.mjr.extraplanets.items.noseCones.Tier9NoseCone;
import com.mjr.extraplanets.items.planetAndMoonItems.ItemTier10Items;
import com.mjr.extraplanets.items.planetAndMoonItems.ItemTier4Items;
import com.mjr.extraplanets.items.planetAndMoonItems.ItemTier5Items;
import com.mjr.extraplanets.items.planetAndMoonItems.ItemTier6Items;
import com.mjr.extraplanets.items.planetAndMoonItems.ItemTier7Items;
import com.mjr.extraplanets.items.planetAndMoonItems.ItemTier8Items;
import com.mjr.extraplanets.items.planetAndMoonItems.ItemTier9Items;
import com.mjr.extraplanets.items.rockets.Tier10Rocket;
import com.mjr.extraplanets.items.rockets.Tier4Rocket;
import com.mjr.extraplanets.items.rockets.Tier5Rocket;
import com.mjr.extraplanets.items.rockets.Tier6Rocket;
import com.mjr.extraplanets.items.rockets.Tier7Rocket;
import com.mjr.extraplanets.items.rockets.Tier8Rocket;
import com.mjr.extraplanets.items.rockets.Tier9Rocket;
import com.mjr.extraplanets.items.thermalPadding.ItemThermalCloth;
import com.mjr.extraplanets.items.thermalPadding.ItemTier2ThermalPadding;
import com.mjr.extraplanets.items.thermalPadding.ItemTier3ThermalPadding;
import com.mjr.extraplanets.items.thermalPadding.ItemTier4ThermalPadding;

/**
 * The item contract, swept across the families that are built the same way.
 *
 * <p>
 * Seven tiers of "planet items", seven rocket keys, seven nose cones, the wafers and the canned food: all of them are
 * an {@code Item} with a subtype table and an icon per metadata value. None of that needs a world, and all of it is
 * what breaks when a new tier is copied from the one below and one constant is missed.
 * </p>
 */
public class ItemContractTest {

    /** Every item that can be built without a game, keyed by what it is. */
    private static final Map<String, Supplier<Item>> ITEMS = new LinkedHashMap<String, Supplier<Item>>();

    static {
        ITEMS.put("tier4Items", () -> new ItemTier4Items("tier4_items"));
        ITEMS.put("tier5Items", () -> new ItemTier5Items("tier5_items"));
        ITEMS.put("tier6Items", () -> new ItemTier6Items("tier6_items"));
        ITEMS.put("tier7Items", () -> new ItemTier7Items("tier7_items"));
        ITEMS.put("tier8Items", () -> new ItemTier8Items("tier8_items"));
        ITEMS.put("tier9Items", () -> new ItemTier9Items("tier9_items"));
        ITEMS.put("tier10Items", () -> new ItemTier10Items("tier10_items"));

        ITEMS.put("keyT4", ItemKeyT4::new);
        ITEMS.put("keyT5", ItemKeyT5::new);
        ITEMS.put("keyT6", ItemKeyT6::new);
        ITEMS.put("keyT7", ItemKeyT7::new);
        ITEMS.put("keyT8", ItemKeyT8::new);
        ITEMS.put("keyT9", ItemKeyT9::new);
        ITEMS.put("keyT10", ItemKeyT10::new);

        ITEMS.put("noseConeT4", () -> new Tier4NoseCone("nose_cone_t4"));
        ITEMS.put("noseConeT5", () -> new Tier5NoseCone("nose_cone_t5"));
        ITEMS.put("noseConeT6", () -> new Tier6NoseCone("nose_cone_t6"));
        ITEMS.put("noseConeT7", () -> new Tier7NoseCone("nose_cone_t7"));
        ITEMS.put("noseConeT8", () -> new Tier8NoseCone("nose_cone_t8"));
        ITEMS.put("noseConeT9", () -> new Tier9NoseCone("nose_cone_t9"));
        ITEMS.put("noseConeT10", () -> new Tier10NoseCone("nose_cone_t10"));

        ITEMS.put("electricParts", () -> new ItemElectricParts("electric_parts"));
        ITEMS.put("wafers", () -> new ItemWafers("wafers"));
        ITEMS.put("cannedFood", () -> new ItemCannedFood("canned_food"));
        ITEMS.put("thermalCloth", () -> new ItemThermalCloth("thermal_cloth"));
        ITEMS.put("thermalPaddingT2", () -> new ItemTier2ThermalPadding("thermal_padding_t2"));
        ITEMS.put("thermalPaddingT3", () -> new ItemTier3ThermalPadding("thermal_padding_t3"));
        ITEMS.put("thermalPaddingT4", () -> new ItemTier4ThermalPadding("thermal_padding_t4"));
        ITEMS.put("rocketT4", () -> new Tier4Rocket("rocket_t4"));
        ITEMS.put("rocketT5", () -> new Tier5Rocket("rocket_t5"));
        ITEMS.put("rocketT6", () -> new Tier6Rocket("rocket_t6"));
        ITEMS.put("rocketT7", () -> new Tier7Rocket("rocket_t7"));
        ITEMS.put("rocketT8", () -> new Tier8Rocket("rocket_t8"));
        ITEMS.put("rocketT9", () -> new Tier9Rocket("rocket_t9"));
        ITEMS.put("rocketT10", () -> new Tier10Rocket("rocket_t10"));

    }

    @Test
    public void everyItemIsConstructibleWithSaneDefaults() {
        for (Map.Entry<String, Supplier<Item>> entry : ITEMS.entrySet()) {
            Item item = entry.getValue()
                .get();

            Assert.assertNotNull(entry.getKey() + ": item", item);
            Assert.assertNotNull(entry.getKey() + ": unlocalized name", item.getUnlocalizedName());
            Assert.assertFalse(
                entry.getKey() + ": an item without a name cannot be translated",
                item.getUnlocalizedName()
                    .isEmpty());
            Assert.assertTrue(
                entry.getKey() + ": stack size " + item.getItemStackLimit(),
                item.getItemStackLimit() >= 1 && item.getItemStackLimit() <= 64);
            Assert.assertTrue(entry.getKey() + ": negative max damage", item.getMaxDamage() >= 0);
            Assert.assertTrue(entry.getKey() + ": negative enchantability", item.getItemEnchantability() >= 0);
        }
    }

    @Test
    public void everyItemCanRegisterItsIcons() {
        // The register pass is where an item's icon array is sized; a mismatch shows up as an empty slot in the
        // creative tab rather than as an exception.
        for (Map.Entry<String, Supplier<Item>> entry : ITEMS.entrySet()) {
            Item item = entry.getValue()
                .get();
            IIconRegister register = org.mockito.Mockito.mock(IIconRegister.class);
            item.registerIcons(register);
        }
    }

    @Test
    public void everyItemResolvesAnIconForEachOfItsSubtypes() {
        for (Map.Entry<String, Supplier<Item>> entry : ITEMS.entrySet()) {
            Item item = entry.getValue()
                .get();
            item.registerIcons(org.mockito.Mockito.mock(IIconRegister.class));

            // Every subtyped item in this mod uses metadata 0..n-1; asking for a few of them must not throw even
            // though a mocked registry hands back null icons.
            for (int meta = 0; meta < 4; meta++) {
                item.getIconFromDamage(meta);
                item.getIconIndex(new ItemStack(item, 1, meta));
            }
        }
    }

    @Test
    public void everyItemNamesItsFirstSubtype() {
        // Metadata 0 is the only value every family has - the keys and nose cones have exactly one, and their
        // getUnlocalizedName indexes the name table directly, so asking for metadata 1 there throws (which is why
        // this only asks for the first).
        for (Map.Entry<String, Supplier<Item>> entry : ITEMS.entrySet()) {
            Item item = entry.getValue()
                .get();
            String name = item.getUnlocalizedName(new ItemStack(item, 1, 0));
            Assert.assertNotNull(entry.getKey() + ": name", name);
            Assert.assertFalse(entry.getKey() + ": empty name", name.isEmpty());
        }
    }
}
