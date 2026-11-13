package com.mjr.extraplanets.items.armor.modules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.Item;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mjr.extraplanets.items.ExtraPlanets_Items;

/**
 * Guards the module registry contract.
 *
 * <p>
 * Modules are looked up by name from NBT, so the registry is effectively a persistence format: a duplicated or
 * renamed module silently orphans every suit in the world that already has it installed. The slot-type values are
 * just as load-bearing - they must use the {@link net.minecraft.item.ItemArmor#armorType} convention
 * (0 = helmet, 1 = chestplate, 2 = leggings, 3 = boots) because {@code ModuleHelper#checkSlotCompatibility}
 * compares them directly against {@code armorType}.
 * </p>
 */
public class ModuleRegistryTest {

    @BeforeClass
    public static void initialise() {
        // The prototypes declare their requirements lazily (see ModuleRequirementOrderTest), so init() itself no
        // longer touches these items. They are still stubbed here because this class reads every module's
        // requirement list, and a requirement whose item does not exist is deliberately dropped at that point.
        stubItem("tier1RadiationLayer");
        stubItem("tier1PressureLayer");
        stubItem("advancedBattery");

        ExtraPlanets_Modules.init();
    }

    @AfterClass
    public static void clearStubs() {
        setItem("tier1RadiationLayer", null);
        setItem("tier1PressureLayer", null);
        setItem("advancedBattery", null);
    }

    private static void stubItem(String fieldName) {
        setItem(fieldName, new Item().setUnlocalizedName("stub_" + fieldName));
    }

    private static void setItem(String fieldName, Item item) {
        try {
            java.lang.reflect.Field field = ExtraPlanets_Items.class.getField(fieldName);
            field.set(null, item);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot stub ExtraPlanets_Items." + fieldName, e);
        }
    }

    @Test
    public void initIsIdempotent() {
        int size = ExtraPlanets_Modules.getModules()
            .size();
        ExtraPlanets_Modules.init();
        Assert.assertEquals(
            "a second init() must not duplicate every module",
            size,
            ExtraPlanets_Modules.getModules()
                .size());
    }

    @Test
    public void theExpectedNumberOfModulesIsRegistered() {
        Assert.assertEquals(
            17,
            ExtraPlanets_Modules.getModules()
                .size());
    }

    @Test
    public void moduleNamesAreUnique() {
        Set<String> names = new HashSet<String>();
        for (Module module : ExtraPlanets_Modules.getModules()) {
            Assert.assertTrue(
                "duplicate module name: " + module.getName(),
                names.add(
                    module.getName()
                        .toLowerCase()));
        }
    }

    @Test
    public void moduleNamesAreLowerSnakeCase() {
        // Names are used verbatim in translation keys and NBT; whitespace or upper case would break both.
        for (Module module : ExtraPlanets_Modules.getModules()) {
            Assert.assertTrue(
                "module name '" + module.getName() + "' is not lower_snake_case",
                module.getName()
                    .matches("[a-z0-9_]+"));
        }
    }

    @Test
    public void slotTypesUseTheArmorTypeConvention() {
        for (Module module : ExtraPlanets_Modules.getModules()) {
            int slot = module.getSlotType();
            Assert.assertTrue(
                "module '" + module.getName() + "' has slot type " + slot + ", outside {-1, 0, 1, 2, 3}",
                slot >= -1 && slot <= 3);
        }
    }

    @Test
    public void theSlotTypeOfEveryModuleMatchesTheArmourPieceItBelongsTo() {
        // Pins the convention documented on Module: 0 = helmet, 1 = chestplate, 2 = leggings, 3 = boots,
        // -1 = any slot. A module installed on the wrong piece is not caught by anything at runtime; the player
        // simply gets a helmet jetpack that never fires.
        assertSlot("night_vision", 0);
        assertSlot("sensor_glasses", 0);
        assertSlot("basic_solar_panel", 0);
        assertSlot("advanced_solar_panel", 0);
        assertSlot("jetpack_t1", 1);
        assertSlot("jetpack_t2", 1);
        assertSlot("speed_boost_t1", 2);
        assertSlot("speed_boost_t2", 2);
        assertSlot("step_assist", 2);
        assertSlot("gravity_controller", 3);
        assertSlot("enhanced_gravity", 3);
        assertSlot("anti_gravity_jump", 3);
        assertSlot("no_fall_damage", 3);
        assertSlot("radiation_shield", -1);
        assertSlot("pressure_seal", -1);
        assertSlot("protection_plus_diamond", -1);
        assertSlot("battery_expansion", -1);
    }

    @Test
    public void everyModuleHasAnIcon() {
        for (Module module : ExtraPlanets_Modules.getModules()) {
            Assert.assertNotNull("module '" + module.getName() + "' has no icon", module.getIcon());
        }
    }

    @Test
    public void everyModuleHasAtLeastOneRequirement() {
        for (Module module : ExtraPlanets_Modules.getModules()) {
            Assert.assertFalse(
                "module '" + module.getName() + "' costs nothing to install",
                module.getRequirements()
                    .isEmpty());
        }
    }

    @Test
    public void everyRequirementIsANonNullStack() {
        // Deliberately does not assert on ItemStack#getItem(): in a plain JVM the RegistryDelegate of an item that
        // was created before FML populated its registry is null, so getItem() is null even for vanilla items. What
        // can be asserted headless is that no prototype holds a null stack.
        for (Module module : ExtraPlanets_Modules.getModules()) {
            for (Object requirement : module.getRequirements()) {
                Assert.assertNotNull("module '" + module.getName() + "' has a null requirement", requirement);
            }
        }
    }

    @Test
    public void lookupIsCaseInsensitiveAndNullSafe() {
        Assert.assertNotNull(ExtraPlanets_Modules.getModuleByName("JETPACK_T1"));
        Assert.assertNull(ExtraPlanets_Modules.getModuleByName("no_such_module"));
        Assert.assertNull(ExtraPlanets_Modules.getModuleByName(null));
    }

    @Test
    public void addAndRemoveAreVisible() {
        Module extra = ExtraPlanets_Modules.getModuleByName("jetpack_t1");
        List<Module> modules = ExtraPlanets_Modules.getModules();
        int before = modules.size();

        ExtraPlanets_Modules.removeModule(extra);
        Assert.assertEquals(before - 1, modules.size());
        Assert.assertNull(ExtraPlanets_Modules.getModuleByName("jetpack_t1"));

        ExtraPlanets_Modules.addModule(extra);
        Assert.assertEquals(before, modules.size());
        Assert.assertNotNull(ExtraPlanets_Modules.getModuleByName("jetpack_t1"));
    }

    @Test
    public void copiesDoNotShareTheirRequirementList() {
        // Module#copy is a shallow clone; if it keeps sharing the requirement list, a GUI that edits a copy's
        // requirements silently edits the registry prototype for every player in the session.
        Module original = ExtraPlanets_Modules.getModuleByName("jetpack_t1");
        int originalRequirementCount = original.getRequirements()
            .size();

        Module copy = original.copy();
        copy.getRequirements()
            .clear();

        Assert.assertEquals(
            "copy() must not share the prototype's requirement list",
            originalRequirementCount,
            original.getRequirements()
                .size());
    }

    private static void assertSlot(String moduleName, int expected) {
        Module module = ExtraPlanets_Modules.getModuleByName(moduleName);
        Assert.assertNotNull("missing module: " + moduleName, module);
        Assert.assertEquals("slot type of " + moduleName, expected, module.getSlotType());
    }
}
