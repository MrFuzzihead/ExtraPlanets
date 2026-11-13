package com.mjr.extraplanets.items.armor.modules;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.mjr.extraplanets.api.item.IModularArmor;

/**
 * Covers {@link ModuleHelper}: the NBT format that stores installed suit modules.
 *
 * <p>
 * This is the one part of the armour system that is pure data handling - no energy, no rendering, no world - and
 * it is also the part everything else depends on: the GUI writes it, the tick handlers read it every tick, and it
 * is the only thing that survives a save/reload. So it is worth pinning precisely.
 * </p>
 */
public class ModuleHelperTest {

    /** Minimal module used by the tests; avoids dragging in the real registry's item requirements. */
    private static final class TestModule extends Module {

        TestModule(String name, int slotType) {
            super(name, slotType, null, true, 0, 0);
        }

        @Override
        public void tickServer(EntityPlayerMP player) {}

        @Override
        public void tickClient(net.minecraft.entity.player.EntityPlayer player) {}
    }

    /** A modular item that is deliberately *not* an ElectricArmorBase, to prove the helpers do not cast blindly. */
    private static final class TestModularItem extends Item implements IModularArmor {

        private final int armorType;

        TestModularItem(int armorType) {
            this.armorType = armorType;
        }
    }

    private static final class TestNonModularItem extends Item {
    }

    private TestModule moduleA;
    private TestModule moduleB;

    @Before
    public void registerTestModules() {
        moduleA = new TestModule("test_module_a", -1);
        moduleB = new TestModule("test_module_b", 2);
        ExtraPlanets_Modules.addModule(moduleA);
        ExtraPlanets_Modules.addModule(moduleB);
    }

    @After
    public void unregisterTestModules() {
        ExtraPlanets_Modules.removeModule(moduleA);
        ExtraPlanets_Modules.removeModule(moduleB);
    }

    private static ItemStack modularStack() {
        return new ItemStack(new TestModularItem(2), 1, 0);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Non-modular items are inert
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void aNonModularItemNeverGetsTheModulesTag() {
        ItemStack stack = new ItemStack(new TestNonModularItem(), 1, 0);
        ModuleHelper.setupModulesNBT(stack);
        Assert.assertFalse("a non-modular item must not be touched", stack.hasTagCompound());
    }

    @Test
    public void aNonModularItemHasNoModules() {
        ItemStack stack = new ItemStack(new TestNonModularItem(), 1, 0);
        Assert.assertTrue(
            ModuleHelper.getModules(stack)
                .isEmpty());
        Assert.assertFalse(ModuleHelper.hasModule(stack, moduleA));
        Assert.assertFalse(ModuleHelper.isModuleActive(stack, moduleA));
    }

    // ---------------------------------------------------------------------------------------------------------
    // NBT format
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void setupCreatesAnEmptyModulesList() {
        ItemStack stack = modularStack();
        ModuleHelper.setupModulesNBT(stack);

        Assert.assertTrue(stack.hasTagCompound());
        NBTTagList list = stack.getTagCompound()
            .getTagList("modules", 10);
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.tagCount());
    }

    @Test
    public void setupKeepsTheExistingList() {
        ItemStack stack = modularStack();
        ModuleHelper.addModule(stack, moduleA);
        int count = stack.getTagCompound()
            .getTagList("modules", 10)
            .tagCount();

        ModuleHelper.setupModulesNBT(stack);

        Assert.assertEquals(
            "setup must not wipe installed modules",
            count,
            stack.getTagCompound()
                .getTagList("modules", 10)
                .tagCount());
    }

    @Test
    public void setupKeepsOtherNbtKeys() {
        ItemStack stack = modularStack();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setFloat("electricity", 1234.5F);
        stack.setTagCompound(tag);

        ModuleHelper.setupModulesNBT(stack);

        Assert.assertEquals(
            1234.5F,
            stack.getTagCompound()
                .getFloat("electricity"),
            0.0001F);
    }

    @Test
    public void addedModulesAreStoredByName() {
        ItemStack stack = modularStack();
        ModuleHelper.addModule(stack, moduleA);

        NBTTagList list = stack.getTagCompound()
            .getTagList("modules", 10);
        Assert.assertEquals(1, list.tagCount());
        Assert.assertEquals(
            "test_module_a",
            list.getCompoundTagAt(0)
                .getString("module"));
    }

    @Test
    public void anInactiveModuleIsStoredAsInactive() {
        ItemStack stack = modularStack();
        moduleA.setActive(false);
        ModuleHelper.addModule(stack, moduleA);

        Assert.assertFalse(
            stack.getTagCompound()
                .getTagList("modules", 10)
                .getCompoundTagAt(0)
                .getBoolean("active"));
    }

    @Test
    public void aZeroSubTypeIsNotWritten() {
        ItemStack stack = modularStack();
        moduleA.setSubType(0);
        ModuleHelper.addModule(stack, moduleA);

        Assert.assertFalse(
            "the default subtype is omitted to keep the tag small",
            stack.getTagCompound()
                .getTagList("modules", 10)
                .getCompoundTagAt(0)
                .hasKey("data"));
    }

    // ---------------------------------------------------------------------------------------------------------
    // Read back
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void modulesRoundTripWithTheirActiveState() {
        ItemStack stack = modularStack();
        moduleA.setActive(true);
        moduleB.setActive(false);
        ModuleHelper.setModules(stack, java.util.Arrays.<Module>asList(moduleA, moduleB));

        List<Module> read = ModuleHelper.getModules(stack);

        Assert.assertEquals(2, read.size());
        Assert.assertEquals(
            "test_module_a",
            read.get(0)
                .getName());
        Assert.assertTrue(
            read.get(0)
                .isActive());
        Assert.assertEquals(
            "test_module_b",
            read.get(1)
                .getName());
        Assert.assertFalse(
            read.get(1)
                .isActive());
    }

    @Test
    public void modulesRoundTripWithTheirSubType() {
        ItemStack stack = modularStack();
        TestModule tiered = new TestModule("test_module_tiered", -1);
        ExtraPlanets_Modules.addModule(tiered);
        try {
            tiered.setSubType(3);
            ModuleHelper.addModule(stack, tiered);

            List<Module> read = ModuleHelper.getModules(stack);
            Assert.assertEquals(1, read.size());
            Assert.assertEquals(
                3,
                read.get(0)
                    .getSubType());
        } finally {
            ExtraPlanets_Modules.removeModule(tiered);
        }
    }

    @Test
    public void readingReturnsCopiesNotNullTheRegistryPrototype() {
        ItemStack stack = modularStack();
        moduleA.setActive(true);
        ModuleHelper.addModule(stack, moduleA);

        List<Module> read = ModuleHelper.getModules(stack);
        Assert.assertEquals(1, read.size());
        Assert.assertNotSame("the registry prototype must not be handed out", moduleA, read.get(0));
        Assert.assertTrue(
            "the copy must not change the prototype",
            ExtraPlanets_Modules.getModuleByName("test_module_a")
                .isActive());
    }

    @Test
    public void anUnknownModuleNameIsIgnored() {
        ItemStack stack = modularStack();
        NBTTagCompound entry = new NBTTagCompound();
        entry.setString("module", "this_module_does_not_exist");
        NBTTagList list = new NBTTagList();
        list.appendTag(entry);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("modules", list);
        stack.setTagCompound(tag);

        Assert.assertTrue(
            ModuleHelper.getModules(stack)
                .isEmpty());
        Assert.assertFalse(ModuleHelper.hasModule(stack, "this_module_does_not_exist"));
    }

    @Test
    public void aMissingModulesTagYieldsAnEmptyList() {
        ItemStack stack = modularStack();
        stack.setTagCompound(new NBTTagCompound());

        Assert.assertTrue(
            ModuleHelper.getModules(stack)
                .isEmpty());
    }

    @Test
    public void theWrongTagTypeForModulesDoesNotThrow() {
        // getTagList returns an empty list for a mistyped tag; a save written by an older/newer version must not
        // crash the tick handler.
        ItemStack stack = modularStack();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("modules", "not a list");
        stack.setTagCompound(tag);

        Assert.assertTrue(
            ModuleHelper.getModules(stack)
                .isEmpty());
    }

    // ---------------------------------------------------------------------------------------------------------
    // Mutation helpers
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void hasModuleIsCaseInsensitive() {
        ItemStack stack = modularStack();
        ModuleHelper.addModule(stack, moduleA);

        Assert.assertTrue(ModuleHelper.hasModule(stack, "TEST_MODULE_A"));
        Assert.assertTrue(ModuleHelper.hasModule(stack, "test_module_a"));
        Assert.assertFalse(ModuleHelper.hasModule(stack, "test_module_c"));
    }

    @Test
    public void removingAModuleLeavesTheRestAlone() {
        ItemStack stack = modularStack();
        ModuleHelper.setModules(stack, java.util.Arrays.<Module>asList(moduleA, moduleB));

        ModuleHelper.removeModule(stack, moduleA);

        List<Module> remaining = ModuleHelper.getModules(stack);
        Assert.assertEquals(1, remaining.size());
        Assert.assertEquals(
            "test_module_b",
            remaining.get(0)
                .getName());
    }

    @Test
    public void removingAModuleThatIsNotInstalledChangesNothing() {
        ItemStack stack = modularStack();
        ModuleHelper.addModule(stack, moduleA);

        ModuleHelper.removeModule(stack, moduleB);

        Assert.assertTrue(ModuleHelper.hasModule(stack, moduleA));
        Assert.assertEquals(
            1,
            ModuleHelper.getModules(stack)
                .size());
    }

    @Test
    public void updatingTheActiveStateTouchesOnlyTheNamedModule() {
        ItemStack stack = modularStack();
        moduleA.setActive(true);
        moduleB.setActive(true);
        ModuleHelper.setModules(stack, java.util.Arrays.<Module>asList(moduleA, moduleB));

        ModuleHelper.updateModuleActiveState(stack, moduleB, false);

        Assert.assertTrue(ModuleHelper.isModuleActive(stack, "test_module_a"));
        Assert.assertFalse(ModuleHelper.isModuleActive(stack, "test_module_b"));
    }

    @Test
    public void setModulesReplacesRatherThanAppends() {
        ItemStack stack = modularStack();
        ModuleHelper.setModules(stack, java.util.Arrays.<Module>asList(moduleA, moduleB));
        ModuleHelper.setModules(stack, java.util.Arrays.<Module>asList(moduleB));

        List<Module> modules = ModuleHelper.getModules(stack);
        Assert.assertEquals(1, modules.size());
        Assert.assertEquals(
            "test_module_b",
            modules.get(0)
                .getName());
    }

    @Test
    public void setModulesWithAnEmptyListClearsTheTag() {
        ItemStack stack = modularStack();
        ModuleHelper.addModule(stack, moduleA);

        ModuleHelper.setModules(stack, new ArrayList<Module>());

        Assert.assertTrue(
            ModuleHelper.getModules(stack)
                .isEmpty());
    }

    // ---------------------------------------------------------------------------------------------------------
    // Slot compatibility
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void aUniversalModuleFitsAnyModularItem() {
        Assert.assertTrue(ModuleHelper.checkSlotCompatibility(modularStack(), moduleA));
    }

    @Test
    public void aNonModularItemIsNeverCompatible() {
        ItemStack stack = new ItemStack(new TestNonModularItem(), 1, 0);
        Assert.assertFalse(ModuleHelper.checkSlotCompatibility(stack, moduleA));
    }

    @Test
    public void aSlotBoundModuleDoesNotFitANonArmorItem() {
        // TestModularItem is not an ItemArmor, so a module with a concrete slot type cannot be installed on it.
        Assert.assertFalse(ModuleHelper.checkSlotCompatibility(modularStack(), moduleB));
    }

    @Test
    public void powerReadsOnANonElectricModularItemReturnZero() {
        // IModularArmor is a marker interface: it promises module slots, not an energy store. Any addon suit can
        // implement it, and getArmourStoredPower is on the HUD path, so an unchecked cast here threw
        // ClassCastException every frame for that player. It must read as "no power" instead.
        Assert.assertEquals(0, ModuleHelper.getArmourStoredPower(modularStack()));
    }

    @Test
    public void hasPowerIsFalseForANonElectricModularItem() {
        Assert.assertFalse(ModuleHelper.hasPower(modularStack(), 1));
    }

    @Test
    public void takingPowerFromANonElectricModularItemIsANoOp() {
        // Used by the module tick and use paths; it has to survive a suit it cannot discharge.
        ModuleHelper.takeArmourPower(modularStack(), 100);
    }

    @Test
    public void powerReadsOnANonModularItemReturnZero() {
        Assert.assertEquals(0, ModuleHelper.getArmourStoredPower(new ItemStack(new TestNonModularItem(), 1)));
        Assert.assertFalse(ModuleHelper.hasPower(new ItemStack(new TestNonModularItem(), 1), 1));
        ModuleHelper.takeArmourPower(new ItemStack(new TestNonModularItem(), 1), 100);
    }
}
