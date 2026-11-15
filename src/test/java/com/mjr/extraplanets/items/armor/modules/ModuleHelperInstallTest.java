package com.mjr.extraplanets.items.armor.modules;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.mjr.extraplanets.api.item.IModularArmor;

/**
 * Covers module installation and removal, which is the part a player actually drives.
 *
 * <p>
 * These helpers take an {@link EntityPlayer}, so the tests use Mockito for the player and its inventory rather than
 * constructing one: {@code EntityPlayer}'s inventory is a public field, so a mock plus a plain field assignment
 * reproduces exactly the state the helpers touch, without a world, a connection or a dimension.
 * </p>
 */
public class ModuleHelperInstallTest {

    private static final class TestModule extends Module {

        TestModule(String name, int slotType, List<ItemStack> requirements) {
            super(name, requirements, slotType, null, true, 0, 0);
        }

        TestModule(String name, int slotType) {
            this(name, slotType, new ArrayList<ItemStack>());
        }

        @Override
        public void tickServer(net.minecraft.entity.player.EntityPlayerMP player) {}

        @Override
        public void tickClient(net.minecraft.entity.player.EntityPlayer player) {}
    }

    /** A chest slot (armorType 1) modular item. */
    private static final class TestChest extends ItemArmor implements IModularArmor {

        TestChest() {
            super(ArmorMaterial.IRON, 2, 1);
        }
    }

    private static final class TestNonModularItem extends Item {
    }

    private final List<Module> refundables = new ArrayList<Module>();
    private TestModule universal;
    private TestModule chestOnly;
    private TestModule bootsOnly;

    @Before
    public void registerModules() {
        universal = new TestModule("install_test_universal", -1);
        chestOnly = new TestModule("install_test_chest", 1);
        bootsOnly = new TestModule("install_test_boots", 3);
        ExtraPlanets_Modules.addModule(universal);
        ExtraPlanets_Modules.addModule(chestOnly);
        ExtraPlanets_Modules.addModule(bootsOnly);
    }

    @After
    public void unregisterModules() {
        ExtraPlanets_Modules.removeModule(universal);
        ExtraPlanets_Modules.removeModule(chestOnly);
        ExtraPlanets_Modules.removeModule(bootsOnly);
        for (Module module : refundables) ExtraPlanets_Modules.removeModule(module);
        refundables.clear();
    }

    // ---------------------------------------------------------------------------------------------------------
    // installModule
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void installingOnANonModularItemFails() {
        ItemStack stack = new ItemStack(new TestNonModularItem(), 1, 0);
        Assert.assertFalse(ModuleHelper.installModule(stack, universal, mockPlayer(0)));
    }

    @Test
    public void installingAModuleInTheWrongSlotFails() {
        ItemStack chest = new ItemStack(new TestChest(), 1, 0);
        Assert.assertFalse(
            "a boots module must not install on a chestplate",
            ModuleHelper.installModule(chest, bootsOnly, mockPlayer(0)));
        Assert.assertFalse(ModuleHelper.hasModule(chest, bootsOnly));
    }

    @Test
    public void installingAModuleInTheRightSlotSucceeds() {
        ItemStack chest = new ItemStack(new TestChest(), 1, 0);
        Assert.assertTrue(ModuleHelper.installModule(chest, chestOnly, mockPlayer(0)));
        Assert.assertTrue(ModuleHelper.hasModule(chest, chestOnly));
    }

    @Test
    public void installingAUniversalModuleSucceeds() {
        ItemStack chest = new ItemStack(new TestChest(), 1, 0);
        Assert.assertTrue(ModuleHelper.installModule(chest, universal, mockPlayer(0)));
    }

    @Test
    public void installingTheSameModuleTwiceFails() {
        ItemStack chest = new ItemStack(new TestChest(), 1, 0);
        Assert.assertTrue(ModuleHelper.installModule(chest, universal, mockPlayer(0)));
        Assert.assertFalse(
            "a second install must be refused rather than duplicating the entry",
            ModuleHelper.installModule(chest, universal, mockPlayer(0)));
        Assert.assertEquals(
            1,
            ModuleHelper.getModules(chest)
                .size());
    }

    @Test
    public void installingWithoutTheRequirementsFails() {
        List<ItemStack> requirements = new ArrayList<ItemStack>();
        requirements.add(new ItemStack(Items.diamond, 2));
        TestModule expensive = new TestModule("install_test_expensive", -1, requirements);
        ExtraPlanets_Modules.addModule(expensive);
        try {
            ItemStack chest = new ItemStack(new TestChest(), 1, 0);
            Assert.assertFalse(ModuleHelper.installModule(chest, expensive, mockPlayer(0)));
            Assert.assertFalse(
                "nothing may be installed when the cost cannot be paid",
                ModuleHelper.hasModule(chest, expensive));
        } finally {
            ExtraPlanets_Modules.removeModule(expensive);
        }
    }

    @Test
    public void installingWithTheRequirementsConsumesThem() {
        List<ItemStack> requirements = new ArrayList<ItemStack>();
        requirements.add(new ItemStack(Items.diamond, 2));
        TestModule expensive = new TestModule("install_test_affordable", -1, requirements);
        ExtraPlanets_Modules.addModule(expensive);
        try {
            ItemStack chest = new ItemStack(new TestChest(), 1, 0);
            ItemStack payment = new ItemStack(Items.diamond, 3);
            EntityPlayer player = mockPlayer(1);
            when(player.inventory.getStackInSlot(0)).thenReturn(payment);

            Assert.assertTrue(ModuleHelper.installModule(chest, expensive, player));
            Assert.assertTrue(ModuleHelper.hasModule(chest, expensive));
            Assert.assertEquals("only the required amount is taken", 1, payment.stackSize);
        } finally {
            ExtraPlanets_Modules.removeModule(expensive);
        }
    }

    @Test
    public void installingConsumesADeferredRequirement() {
        // The real modules declare their cost lazily (see ModuleRequirementOrderTest), so the install and refund
        // paths have to work on a module whose requirement list is built on first read rather than at construction.
        final List<ItemStack> cost = new ArrayList<ItemStack>();
        cost.add(new ItemStack(Items.diamond, 2));
        TestModule deferred = new TestModule("install_test_deferred", -1);
        deferred.setRequirementSupplier(() -> cost);
        ExtraPlanets_Modules.addModule(deferred);
        try {
            ItemStack chest = new ItemStack(new TestChest(), 1, 0);
            ItemStack payment = new ItemStack(Items.diamond, 3);
            EntityPlayer player = mockPlayer(1);
            when(player.inventory.getStackInSlot(0)).thenReturn(payment);
            when(player.inventory.addItemStackToInventory(any(ItemStack.class))).thenReturn(true);

            Assert.assertTrue(ModuleHelper.installModule(chest, deferred, player));
            Assert.assertEquals("only the required amount is taken", 1, payment.stackSize);

            ModuleHelper.uninstallModule(chest, deferred, player);
            Assert.assertFalse(ModuleHelper.hasModule(chest, deferred));
            verify(player.inventory).addItemStackToInventory(any(ItemStack.class));
        } finally {
            ExtraPlanets_Modules.removeModule(deferred);
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // uninstallModule
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void uninstallingReturnsTheRequirementsToTheInventory() {
        TestModule refundable = refundableModule();
        ItemStack chest = new ItemStack(new TestChest(), 1, 0);
        ModuleHelper.addModule(chest, refundable);

        EntityPlayer player = mockPlayer(0);
        when(player.inventory.addItemStackToInventory(any(ItemStack.class))).thenReturn(true);

        ModuleHelper.uninstallModule(chest, refundable, player);

        Assert.assertFalse(ModuleHelper.hasModule(chest, refundable));
        verify(player.inventory).addItemStackToInventory(any(ItemStack.class));
        verify(player, never()).entityDropItem(any(ItemStack.class), anyFloat());
    }

    @Test
    public void uninstallingDropsTheRequirementsWhenTheInventoryIsFull() {
        TestModule refundable = refundableModule();
        ItemStack chest = new ItemStack(new TestChest(), 1, 0);
        ModuleHelper.addModule(chest, refundable);

        EntityPlayer player = mockPlayer(0);
        when(player.inventory.addItemStackToInventory(any(ItemStack.class))).thenReturn(false);

        ModuleHelper.uninstallModule(chest, refundable, player);

        Assert.assertFalse(ModuleHelper.hasModule(chest, refundable));
        verify(player).entityDropItem(any(ItemStack.class), anyFloat());
    }

    @Test
    public void uninstallingANonModularItemIsANoOp() {
        TestModule refundable = refundableModule();
        ItemStack stack = new ItemStack(new TestNonModularItem(), 1, 0);
        EntityPlayer player = mockPlayer(0);
        ModuleHelper.uninstallModule(stack, refundable, player);
        verify(player, never()).entityDropItem(any(ItemStack.class), anyFloat());
    }

    /** A module with one requirement, so uninstalling has something to give back. */
    private TestModule refundableModule() {
        List<ItemStack> requirements = new ArrayList<ItemStack>();
        requirements.add(new ItemStack(Items.iron_ingot, 1));
        TestModule module = new TestModule("install_test_refundable", -1, requirements);
        ExtraPlanets_Modules.addModule(module);
        refundables.add(module);
        return module;
    }

    // ---------------------------------------------------------------------------------------------------------
    // consumeFromInventory
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void consumingAnAbsentRequirementFails() {
        EntityPlayer player = mockPlayer(2);
        Assert.assertFalse(ModuleHelper.consumeFromInventory(player, new ItemStack(Items.diamond, 1)));
    }

    @Test
    public void consumingAnInsufficientStackFails() {
        EntityPlayer player = mockPlayer(1);
        ItemStack one = new ItemStack(Items.diamond, 1);
        when(player.inventory.getStackInSlot(0)).thenReturn(one);

        Assert.assertFalse(
            "a stack smaller than the requirement must not be consumed",
            ModuleHelper.consumeFromInventory(player, new ItemStack(Items.diamond, 2)));
        Assert.assertEquals(1, one.stackSize);
    }

    @Test
    public void consumingTheWholeStackClearsTheSlot() {
        EntityPlayer player = mockPlayer(1);
        ItemStack two = new ItemStack(Items.diamond, 2);
        when(player.inventory.getStackInSlot(0)).thenReturn(two);

        Assert.assertTrue(ModuleHelper.consumeFromInventory(player, new ItemStack(Items.diamond, 2)));
        verify(player.inventory).setInventorySlotContents(0, null);
    }

    @Test
    public void consumingPrefersTheMatchingStack() {
        // Note: a headless test cannot prove that a *different* item is rejected, because ItemStack#isItemEqual
        // compares registry delegates and those are null outside a loaded game (see CODE_REVIEW.md section 8.3), so
        // two stacks of different items compare equal here. The in-game suite covers the negative case.
        EntityPlayer player = mockPlayer(1);
        when(player.inventory.getStackInSlot(0)).thenReturn(new ItemStack(Blocks.dirt, 64));

        Assert.assertTrue(
            "consumption is decided by isItemEqual + stackSize, which the in-game suite verifies",
            ModuleHelper.consumeFromInventory(player, new ItemStack(Blocks.dirt, 1)));
    }

    // ---------------------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------------------

    private static EntityPlayer mockPlayer(int inventorySize) {
        EntityPlayer player = mock(EntityPlayer.class);
        InventoryPlayer inventory = mock(InventoryPlayer.class);
        when(inventory.getSizeInventory()).thenReturn(inventorySize);
        player.inventory = inventory;
        return player;
    }
}
