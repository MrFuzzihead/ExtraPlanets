package com.mjr.extraplanets.items.armor.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.mjr.extraplanets.items.ExtraPlanets_Items;

/**
 * Guards the deferred requirement declaration on {@link Module}.
 *
 * <p>
 * {@link ExtraPlanets_Modules#init()} runs immediately after {@code ExtraPlanets_Items.init()} in preInit and builds
 * its prototypes out of {@code ExtraPlanets_Items} fields - {@code tier1RadiationLayer}, {@code tier1PressureLayer}
 * and {@code advancedBattery} - that are assigned <em>inside</em> that method. Building the requirement stacks in the
 * constructors made the registry depend on that call order: reorder preInit, or disable those three items by config,
 * and every requirement became an {@code ItemStack} with a null item. Nothing threw. The module simply cost nothing
 * in the GUI and could never be installed, because {@code ModuleHelper.installModule} cannot match a null item.
 * </p>
 *
 * <p>
 * The declarations are now deferred to the first {@link Module#getRequirements()}, which happens long after preInit.
 * These tests exercise that contract directly, without touching the global registry, so they do not depend on which
 * other test class ran first.
 * </p>
 */
public class ModuleRequirementOrderTest {

    private Item originalRadiationLayer;
    private Item originalPressureLayer;
    private Item originalAdvancedBattery;

    @Before
    public void captureItemFields() {
        // Another class may already have left these null (ModuleRegistryTest clears its stubs); capture, do not
        // assume.
        originalRadiationLayer = ExtraPlanets_Items.tier1RadiationLayer;
        originalPressureLayer = ExtraPlanets_Items.tier1PressureLayer;
        originalAdvancedBattery = ExtraPlanets_Items.advancedBattery;
    }

    @After
    public void restoreItemFields() {
        ExtraPlanets_Items.tier1RadiationLayer = originalRadiationLayer;
        ExtraPlanets_Items.tier1PressureLayer = originalPressureLayer;
        ExtraPlanets_Items.advancedBattery = originalAdvancedBattery;
    }

    @Test
    public void requirementsResolveAfterTheItemTheyReferenceIsRegistered() {
        // The exact preInit shape that used to break: construction while the layer item does not exist yet.
        ExtraPlanets_Items.tier1RadiationLayer = null;
        Module module = new ModuleRadiationShield("order_test_radiation_shield");

        // ExtraPlanets_Items.init() runs, assigning the item.
        ExtraPlanets_Items.tier1RadiationLayer = new Item().setUnlocalizedName("order_test_radiation_layer");

        Assert.assertEquals(
            "a requirement declared before its item existed must resolve once the item does",
            1,
            module.getRequirements()
                .size());
        Assert.assertNotNull(
            "the resolved requirement must hold a real item",
            module.getRequirements()
                .get(0)
                .getItem());
    }

    @Test
    public void everyExtraPlanetsItemRequirementResolvesLate() {
        // The three prototypes that reference ExtraPlanets_Items, not vanilla blocks.
        ExtraPlanets_Items.tier1RadiationLayer = null;
        ExtraPlanets_Items.tier1PressureLayer = null;
        ExtraPlanets_Items.advancedBattery = null;

        List<Module> modules = new ArrayList<Module>();
        modules.add(new ModuleRadiationShield("order_test_radiation_shield"));
        modules.add(new ModulePressureSeal("order_test_pressure_seal"));
        modules.add(new ModuleBatteryExpansion("order_test_battery_expansion"));

        ExtraPlanets_Items.tier1RadiationLayer = new Item().setUnlocalizedName("order_test_radiation_layer");
        ExtraPlanets_Items.tier1PressureLayer = new Item().setUnlocalizedName("order_test_pressure_layer");
        ExtraPlanets_Items.advancedBattery = new Item().setUnlocalizedName("order_test_battery");

        for (Module module : modules) {
            Assert.assertEquals(
                "module '" + module.getName() + "' lost its requirement to the preInit ordering",
                1,
                module.getRequirements()
                    .size());
        }
    }

    @Test
    public void aRequirementWhoseItemIsDisabledIsDroppedRatherThanKeptAsANullStack() {
        // Disabled by config means the item never exists at all, so there is no later point at which it resolves.
        // A null stack is not an acceptable answer: it renders as an empty slot and makes the module uninstallable.
        ExtraPlanets_Items.tier1RadiationLayer = null;
        Module module = new ModuleRadiationShield("order_test_disabled_shield");

        Assert.assertTrue(
            "a null-item requirement must be dropped, not kept",
            module.getRequirements()
                .isEmpty());
        // Reading again is stable, not a second attempt.
        Assert.assertTrue(
            module.getRequirements()
                .isEmpty());
    }

    @Test
    public void aDeclarationIsEvaluatedAtMostOnce() {
        Module module = new ModuleJetpack("order_test_once_jetpack", 1);
        final int[] evaluations = { 0 };
        module.setRequirementSupplier(() -> {
            evaluations[0]++;
            return Collections.singletonList(new ItemStack(Blocks.piston, 1));
        });

        module.getRequirements();
        module.getRequirements();
        module.copy()
            .getRequirements();

        Assert.assertEquals("the declaration must run once, not on every read", 1, evaluations[0]);
    }

    @Test
    public void setRequirementsOverridesADeclaration() {
        Module module = new ModulePressureSeal("order_test_override_pressure_seal");
        List<ItemStack> explicit = new ArrayList<ItemStack>();
        explicit.add(new ItemStack(Blocks.stone, 1));

        module.setRequirements(explicit);

        Assert.assertEquals(
            1,
            module.getRequirements()
                .size());
        Assert.assertEquals(
            "the explicit list must win over the deferred declaration",
            explicit.get(0),
            module.getRequirements()
                .get(0));
    }

    @Test
    public void copyingAnUnresolvedModuleCarriesItsRequirements() {
        ExtraPlanets_Items.tier1PressureLayer = null;
        Module module = new ModulePressureSeal("order_test_copy_pressure_seal");
        ExtraPlanets_Items.tier1PressureLayer = new Item().setUnlocalizedName("order_test_pressure_layer");

        Module copy = module.copy();

        Assert.assertEquals(
            "a copy must carry the resolved requirement, not an empty list",
            1,
            copy.getRequirements()
                .size());
    }
}
