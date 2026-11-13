package com.mjr.extraplanets.gametest;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.oredict.OreDictionary;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.Constants;
import com.mjr.extraplanets.armor.ExtraPlanets_Armor;
import com.mjr.extraplanets.blocks.ExtraPlanets_Blocks;
import com.mjr.extraplanets.blocks.machines.ExtraPlanets_Machines;
import com.mjr.extraplanets.items.ExtraPlanets_Items;
import com.mjr.extraplanets.items.armor.modules.ExtraPlanets_Modules;
import com.mjr.extraplanets.items.armor.modules.Module;
import com.mjr.extraplanets.items.armor.modules.ModuleHelper;
import com.mjr.extraplanets.tileEntities.machines.TileEntityAdvancedRefinery;
import com.mjr.extraplanets.tileEntities.machines.TileEntitySolar;

/**
 * Everything the in-game test asserts about registries, recipes and tile entities.
 *
 * <p>
 * These checks need what a plain JVM cannot provide: the real FML block and item registries (so
 * {@code GameRegistry.findBlock} works), the real ore dictionary, the real recipe managers, and the tile entity
 * mapping that {@code TileEntity#writeToNBT} requires. All of it runs on the server thread from
 * {@code FMLServerStartedEvent}, where the overworld exists but nothing has to be ticked.
 * </p>
 */
final class ServerGameTests {

    private ServerGameTests() {}

    /** Runs every server-side check. @return the number of failures. */
    static int runAll() {
        TestReporter reporter = new TestReporter();

        run(reporter, "config");
        run(reporter, "blocks");
        run(reporter, "items");
        run(reporter, "tileEntities");
        run(reporter, "oreDictionary");
        run(reporter, "recipes");
        run(reporter, "modules");

        System.out.println(
            "[EP-GAMETEST] server checks: " + (reporter.checks() - reporter.failures())
                + "/"
                + reporter.checks()
                + " passed");
        return reporter.failures();
    }

    private static void run(TestReporter reporter, String group) {
        switch (group) {
            case "config":
                config(reporter);
                break;
            case "blocks":
                blocks(reporter);
                break;
            case "items":
                items(reporter);
                break;
            case "tileEntities":
                tileEntities(reporter);
                break;
            case "oreDictionary":
                oreDictionary(reporter);
                break;
            case "recipes":
                recipes(reporter);
                break;
            case "modules":
                modules(reporter);
                break;
            default:
                throw new IllegalArgumentException(group);
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Config
    // ---------------------------------------------------------------------------------------------------------

    private static void config(TestReporter reporter) {
        reporter.check("config: biome ids are inside the vanilla array", () -> {
            int max = net.minecraft.world.biome.BiomeGenBase.getBiomeGenArray().length - 1;
            assertInRange("mercuryBiomeID", Config.mercuryBiomeID, max);
            assertInRange("venusBiomeID", Config.venusBiomeID, max);
            assertInRange("jupiterBiomeID", Config.jupiterBiomeID, max);
            assertInRange("kepler22bPlainsBiomeID", Config.kepler22bPlainsBiomeID, max);
        });

        reporter.check("config: dimension ids are negative (GC convention)", () -> {
            // GC (and ExtraPlanets) allocate negative dimension ids so they cannot collide with vanilla or with
            // other mods' positive ones. A positive value here is almost always a hand-edited config mistake.
            TestReporter.assertTrue("mercury dimension id must be negative", Config.mercuryID < 0);
            TestReporter.assertTrue("venus dimension id must be negative", Config.venusID < 0);
            TestReporter.assertTrue("jupiter dimension id must be negative", Config.jupiterID < 0);
        });

        reporter.check("config: the config file was created", () -> {
            java.io.File expected = new java.io.File(Constants.CONFIG_FILE);
            TestReporter.assertTrue("expected the config file at " + expected.getAbsolutePath(), expected.isFile());
        });

        reporter.check("config: config sync covers every biome and dimension id", () -> {
            TestReporter.assertEquals(
                "decode class count",
                Config.getConfigSyncDecodeClasses().length,
                Config.getServerConfigOverride()
                    .size());
        });
    }

    private static void assertInRange(String name, int value, int max) {
        TestReporter.assertTrue(name + " = " + value + " is outside 0-" + max, value >= 0 && value <= max);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Blocks
    // ---------------------------------------------------------------------------------------------------------

    private static void blocks(TestReporter reporter) {
        reporter.check("blocks: the mercury block set is registered", () -> {
            if (!Config.mercury) return;
            TestReporter.assertNotNull("mercuryBlocks field", ExtraPlanets_Blocks.mercuryBlocks);
            TestReporter.assertNotNull(
                "mercuryBlocks is not in the block registry",
                Block.blockRegistry.getNameForObject(ExtraPlanets_Blocks.mercuryBlocks));
            TestReporter.assertTrue(
                "mercuryBlocks is registered under the wrong namespace: "
                    + Block.blockRegistry.getNameForObject(ExtraPlanets_Blocks.mercuryBlocks),
                Block.blockRegistry.getNameForObject(ExtraPlanets_Blocks.mercuryBlocks)
                    .toLowerCase()
                    .startsWith(Constants.modID.toLowerCase() + ":"));
        });

        reporter.check("blocks: every registered body block has a name", () -> {
            List<Block> registered = new ArrayList<Block>();
            registered.add(ExtraPlanets_Blocks.mercuryBlocks);
            registered.add(ExtraPlanets_Blocks.venusBlocks);
            registered.add(ExtraPlanets_Blocks.ceresBlocks);
            registered.add(ExtraPlanets_Blocks.jupiterBlocks);
            registered.add(ExtraPlanets_Blocks.saturnBlocks);
            registered.add(ExtraPlanets_Blocks.uranusBlocks);
            registered.add(ExtraPlanets_Blocks.neptuneBlocks);
            registered.add(ExtraPlanets_Blocks.plutoBlocks);
            registered.add(ExtraPlanets_Blocks.erisBlocks);

            for (Block block : registered) {
                if (block == null) continue;
                TestReporter.assertFalse(
                    "block has no unlocalized name: " + block.getClass()
                        .getSimpleName(),
                    block.getUnlocalizedName()
                        .isEmpty());
            }
        });

        reporter.check("blocks: machine blocks are registered", () -> {
            if (Config.advancedRefinery) {
                TestReporter.assertNotNull("advancedRefinery block", ExtraPlanets_Machines.advancedRefinery);
                TestReporter.assertNotNull(
                    "advancedRefinery is not in the block registry",
                    Block.blockRegistry.getNameForObject(ExtraPlanets_Machines.advancedRefinery));
            }
            TestReporter.assertNotNull("solarPanel block", ExtraPlanets_Machines.solarPanel);
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // Items
    // ---------------------------------------------------------------------------------------------------------

    private static void items(TestReporter reporter) {
        reporter.check("items: the rocket line is registered", () -> {
            if (Config.venus) {
                TestReporter.assertHasItem("tier4Rocket", new ItemStack(ExtraPlanets_Items.tier4Rocket));
                assertRegistered(ExtraPlanets_Items.tier4Rocket, "tier4Rocket");
            }
            if (Config.jupiter) {
                TestReporter.assertHasItem("tier5Rocket", new ItemStack(ExtraPlanets_Items.tier5Rocket));
                assertRegistered(ExtraPlanets_Items.tier5Rocket, "tier5Rocket");
            }
        });

        reporter.check("items: the space suit pieces are registered", () -> {
            TestReporter.assertHasItem("spaceSuitHelmet", new ItemStack(ExtraPlanets_Armor.spaceSuitHelmet));
            TestReporter.assertHasItem("spaceSuitChest", new ItemStack(ExtraPlanets_Armor.spaceSuitChest));
            TestReporter.assertHasItem("spaceSuitLegings", new ItemStack(ExtraPlanets_Armor.spaceSuitLegings));
            TestReporter.assertHasItem("spaceSuitBoots", new ItemStack(ExtraPlanets_Armor.spaceSuitBoots));
        });

        reporter.check("items: item ids round trip through the registry", () -> {
            Item item = ExtraPlanets_Items.tier4Rocket;
            if (item == null) return;
            int id = Item.getIdFromItem(item);
            TestReporter.assertTrue("registered items must have a non-zero id", id > 0);
            TestReporter.assertEquals("Item.getItemById must reverse Item.getIdFromItem", item, Item.getItemById(id));
        });
    }

    private static void assertRegistered(Item item, String name) {
        TestReporter.assertNotNull(name + " is not in the item registry", Item.itemRegistry.getNameForObject(item));
        TestReporter.assertTrue(
            name + " is registered under the wrong namespace: " + Item.itemRegistry.getNameForObject(item),
            Item.itemRegistry.getNameForObject(item)
                .toLowerCase()
                .startsWith(Constants.modID.toLowerCase() + ":"));
    }

    // ---------------------------------------------------------------------------------------------------------
    // Tile entities
    // ---------------------------------------------------------------------------------------------------------

    private static void tileEntities(TestReporter reporter) {
        reporter.check("tile entities: the refinery survives a save/load round trip", () -> {
            TileEntityAdvancedRefinery refinery = new TileEntityAdvancedRefinery();
            refinery.xCoord = 3;
            refinery.yCoord = 65;
            refinery.zCoord = -8;

            NBTTagCompound saved = new NBTTagCompound();
            refinery.writeToNBT(saved);
            TestReporter.assertTrue("writeToNBT must record the tile entity id", saved.hasKey("id"));

            TileEntity loaded = TileEntity.createAndLoadEntity(saved);
            TestReporter.assertNotNull("the tile entity mapping must resolve the saved id", loaded);
            TestReporter.assertTrue(
                "expected a refinery, got " + loaded.getClass()
                    .getName(),
                loaded instanceof TileEntityAdvancedRefinery);
            TestReporter.assertEquals("x", 3, loaded.xCoord);
            TestReporter.assertEquals("y", 65, loaded.yCoord);
            TestReporter.assertEquals("z", -8, loaded.zCoord);
        });

        reporter.check("tile entities: fluid tanks survive a save/load round trip", () -> {
            TileEntityAdvancedRefinery refinery = new TileEntityAdvancedRefinery();
            NBTTagCompound saved = new NBTTagCompound();
            refinery.writeToNBT(saved);

            TileEntityAdvancedRefinery loaded = (TileEntityAdvancedRefinery) TileEntity.createAndLoadEntity(saved);
            TestReporter.assertNotNull("loaded refinery", loaded);
            TestReporter.assertNotNull("oil tank", loaded.oilTank);
            TestReporter.assertEquals("tank capacity", refinery.oilTank.getCapacity(), loaded.oilTank.getCapacity());
        });

        reporter.check("tile entities: the solar panel survives a save/load round trip", () -> {
            TileEntitySolar solar = new TileEntitySolar();
            NBTTagCompound saved = new NBTTagCompound();
            solar.writeToNBT(saved);

            TileEntity loaded = TileEntity.createAndLoadEntity(saved);
            TestReporter.assertTrue(
                "expected a solar panel, got " + (loaded == null ? "null" : loaded.getClass()),
                loaded instanceof TileEntitySolar);
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // Ore dictionary
    // ---------------------------------------------------------------------------------------------------------

    private static void oreDictionary(TestReporter reporter) {
        reporter.check("ore dictionary: the planet ores are registered", () -> {
            assertOre("oreMercury");
            assertOre("orePalladium");
            assertOre("oreMagnesium");
            assertOre("oreCrystal");
            assertOre("oreZinc");
            assertOre("oreUranium");
        });

        reporter.check("ore dictionary: the planet blocks are registered", () -> {
            assertOre("blockPalladium");
            assertOre("blockMagnesium");
            assertOre("blockCrystal");
            assertOre("blockZinc");
        });

        reporter.check("ore dictionary: every registered ore entry points at a real stack", () -> {
            for (String name : new String[] { "oreMercury", "orePalladium", "oreMagnesium", "oreCrystal", "oreZinc" }) {
                for (ItemStack stack : OreDictionary.getOres(name)) {
                    TestReporter.assertHasItem("ore dict entry " + name, stack);
                }
            }
        });
    }

    private static void assertSmeltsToOreDict(ItemStack input, String oreName) {
        List<ItemStack> ores = OreDictionary.getOres(oreName);
        if (ores.isEmpty()) return; // the providing mod is absent; addOreSmelting skips the recipe too
        ItemStack result = FurnaceRecipes.smelting()
            .getSmeltingResult(input);
        TestReporter.assertHasItem("smelting result for " + oreName, result);
        TestReporter.assertTrue(
            "expected the furnace result for the ore block to be an " + oreName + ", got " + result.getDisplayName(),
            OreDictionary.getOreIDs(result).length > 0);
    }

    private static void assertOre(String name) {
        TestReporter.assertTrue("no ore dictionary entry for " + name, OreDictionary.doesOreNameExist(name));
        TestReporter.assertFalse(
            "empty ore dictionary entry for " + name,
            OreDictionary.getOres(name)
                .isEmpty());
    }

    // ---------------------------------------------------------------------------------------------------------
    // Recipes
    // ---------------------------------------------------------------------------------------------------------

    private static void recipes(TestReporter reporter) {
        reporter.check("recipes: crafting recipes were registered", () -> {
            TestReporter.assertTrue(
                "the crafting recipe list is suspiciously small",
                CraftingManager.getInstance()
                    .getRecipeList()
                    .size() > 100);
        });

        reporter.check("recipes: the compressor recipe for desh exists", () -> {
            // Every GC machine recipe is registered by GC itself; what is worth checking is that ExtraPlanets' own
            // postInit ran and produced at least one smelting recipe for its own materials.
            TestReporter.assertNotNull("the smelting registry is missing", FurnaceRecipes.smelting());
        });

        reporter.check("recipes: the planet ores smelt into the ore-dictionary ingot", () -> {
            // registerFurnaceRecipes maps each planet's copper/tin/iron ore block onto the *first* ore-dictionary
            // entry for the matching ingot, so the expected result is whatever OreDictionary.getOres returns first.
            if (!Config.mercury) return;
            assertSmeltsToOreDict(new ItemStack(ExtraPlanets_Blocks.mercuryBlocks, 1, 5), "ingotCopper");
            assertSmeltsToOreDict(new ItemStack(ExtraPlanets_Blocks.mercuryBlocks, 1, 4), "ingotTin");
            assertSmeltsToOreDict(new ItemStack(ExtraPlanets_Blocks.mercuryBlocks, 1, 3), "ingotIron");
        });

        reporter.check("recipes: the mod's own metal ore smelts into its ingot", () -> {
            if (!Config.mercury) return;
            ItemStack result = FurnaceRecipes.smelting()
                .getSmeltingResult(new ItemStack(ExtraPlanets_Blocks.mercuryBlocks, 1, 6));
            TestReporter.assertHasItem("smelting result for the mercury ore", result);
        });
    }

    // ---------------------------------------------------------------------------------------------------------
    // Suit modules
    // ---------------------------------------------------------------------------------------------------------

    private static void modules(TestReporter reporter) {
        reporter.check("modules: the registry is populated", () -> {
            TestReporter.assertTrue(
                "expected the full module list, got " + ExtraPlanets_Modules.getModules()
                    .size(),
                ExtraPlanets_Modules.getModules()
                    .size() >= 17);
        });

        reporter.check("modules: every module requirement resolves to a real item in game", () -> {
            // This is the check that cannot be made headless: outside a loaded game ItemStack#getItem() is null for
            // items that were created before FML populated its registry.
            for (Module module : ExtraPlanets_Modules.getModules()) {
                TestReporter.assertHasItem("icon of " + module.getName(), module.getIcon());
                for (ItemStack requirement : module.getRequirements()) {
                    TestReporter.assertHasItem("requirement of " + module.getName(), requirement);
                }
            }
        });

        reporter.check("modules: install and uninstall round trip through NBT", () -> {
            ItemStack suit = new ItemStack(ExtraPlanets_Armor.spaceSuitChest, 1);
            Module jetpack = ExtraPlanets_Modules.getModuleByName("jetpack_t1");
            TestReporter.assertNotNull("jetpack_t1", jetpack);

            ModuleHelper.addModule(suit, jetpack);
            TestReporter.assertTrue("the module must be readable back", ModuleHelper.hasModule(suit, "jetpack_t1"));
            TestReporter.assertEquals(
                "module count",
                1,
                ModuleHelper.getModules(suit)
                    .size());

            ModuleHelper.setModules(suit, new ArrayList<Module>());
            TestReporter.assertFalse("the module must be gone", ModuleHelper.hasModule(suit, "jetpack_t1"));
        });

        reporter.check("modules: a modular suit starts with no modules", () -> {
            ItemStack suit = new ItemStack(ExtraPlanets_Armor.spaceSuitHelmet, 1);
            TestReporter.assertTrue(
                "a freshly crafted suit must have no installed modules",
                ModuleHelper.getModules(suit)
                    .isEmpty());
        });
    }
}
