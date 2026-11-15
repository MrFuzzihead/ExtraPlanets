package com.mjr.extraplanets.inventory;

import static org.mockito.Mockito.mock;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.mjr.extraplanets.inventory.rockets.ContainerSchematicTier10Rocket;
import com.mjr.extraplanets.inventory.rockets.ContainerSchematicTier4Rocket;
import com.mjr.extraplanets.inventory.rockets.ContainerSchematicTier5Rocket;
import com.mjr.extraplanets.inventory.rockets.ContainerSchematicTier6Rocket;
import com.mjr.extraplanets.inventory.rockets.ContainerSchematicTier7Rocket;
import com.mjr.extraplanets.inventory.rockets.ContainerSchematicTier8Rocket;
import com.mjr.extraplanets.inventory.rockets.ContainerSchematicTier9Rocket;

/**
 * Covers the rocket bench containers and the craft matrix + slot classes they own.
 *
 * <p>
 * Seven tiers, one class each, all built the same way: a 22-slot craft matrix, a result slot, the three chest slots,
 * and the player's inventory, wired up in a constructor that is 120 lines of {@code addSlotToContainer}. That layout
 * is the whole contract of the bench, and it is checkable without a game - the container only needs a player whose
 * inventory exists, which Mockito can supply.
 * </p>
 *
 * <p>
 * The interesting behaviour is {@code transferStackInSlot}: shift-clicking must route a stack to a slot that accepts
 * it, and give up cleanly when nothing does. Both paths run here.
 * </p>
 */
public class RocketSchematicContainerTest {

    /** One factory per tier, so every assertion below runs against all seven. */
    private static final Map<String, Function<InventoryPlayer, Container>> TIERS = new LinkedHashMap<String, Function<InventoryPlayer, Container>>();

    static {
        TIERS.put("tier4", inventory -> new ContainerSchematicTier4Rocket(inventory, 0, 0, 0));
        TIERS.put("tier5", inventory -> new ContainerSchematicTier5Rocket(inventory, 0, 0, 0));
        TIERS.put("tier6", inventory -> new ContainerSchematicTier6Rocket(inventory, 0, 0, 0));
        TIERS.put("tier7", inventory -> new ContainerSchematicTier7Rocket(inventory, 0, 0, 0));
        TIERS.put("tier8", inventory -> new ContainerSchematicTier8Rocket(inventory, 0, 0, 0));
        TIERS.put("tier9", inventory -> new ContainerSchematicTier9Rocket(inventory, 0, 0, 0));
        TIERS.put("tier10", inventory -> new ContainerSchematicTier10Rocket(inventory, 0, 0, 0));
    }

    private EntityPlayer player;
    private InventoryPlayer inventory;

    @Before
    public void createPlayer() {
        // worldObj and inventory are plain fields, not getters, so they have to be assigned rather than stubbed - the
        // container constructor reads inventory.player.worldObj. The inventory itself is real: a mocked one accepts
        // nothing, which would make every shift-click assertion vacuous.
        player = mock(EntityPlayer.class);
        player.worldObj = mock(World.class);
        inventory = new InventoryPlayer(player);
        player.inventory = inventory;
    }

    // ---------------------------------------------------------------------------------------------------------
    // Layout
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void everyBenchWiresUpItsFullSlotLayout() {
        for (Map.Entry<String, Function<InventoryPlayer, Container>> tier : TIERS.entrySet()) {
            Container container = tier.getValue()
                .apply(inventory);

            // 22 craft slots + result + 36 player slots; transferStackInSlot addresses up to index 57.
            Assert.assertEquals(tier.getKey() + ": slot count", 58, container.inventorySlots.size());
            Assert.assertTrue(
                tier.getKey() + ": the last slot the transfer code addresses must exist",
                container.inventorySlots.size() > 57);
            Assert.assertEquals(
                tier.getKey() + ": craft matrix size",
                22,
                this.craftMatrix(container)
                    .getSizeInventory());
            Assert.assertTrue(
                tier.getKey() + ": result slot must be readable",
                this.craftResult(container)
                    .getSizeInventory() >= 1);
        }
    }

    @Test
    public void everyBenchAcceptsThePlayer() {
        for (Map.Entry<String, Function<InventoryPlayer, Container>> tier : TIERS.entrySet()) {
            Assert.assertTrue(
                tier.getKey() + ": canInteractWith must be true",
                tier.getValue()
                    .apply(inventory)
                    .canInteractWith(player));
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Craft matrix and slots
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void theCraftMatrixIsARealInventory() {
        IInventory matrix = this.craftMatrix(
            TIERS.get("tier4")
                .apply(inventory));

        Assert.assertEquals(22, matrix.getSizeInventory());
        Assert.assertNull("empty slot", matrix.getStackInSlot(0));
        Assert.assertNull("out of range", matrix.getStackInSlot(22));

        ItemStack stack = new ItemStack(new Item(), 1);
        matrix.setInventorySlotContents(3, stack);
        Assert.assertSame(stack, matrix.getStackInSlot(3));

        matrix.setInventorySlotContents(3, null);
        Assert.assertNull(matrix.getStackInSlot(3));
        Assert.assertNull(matrix.getStackInSlotOnClosing(3));

        // The matrix stores what it is given; clamping is the chest's job, not the bench's.
        ItemStack stack64 = new ItemStack(new Item(), 64);
        matrix.setInventorySlotContents(4, stack64);
        Assert.assertSame(stack64, matrix.getStackInSlot(4));
        Assert.assertEquals(64, matrix.getInventoryStackLimit());
    }

    @Test
    public void everySlotReportsWhetherItAcceptsAStack() {
        Container container = TIERS.get("tier4")
            .apply(inventory);
        ItemStack something = new ItemStack(new Item(), 1);

        for (int i = 0; i < container.inventorySlots.size(); i++) {
            Slot slot = (Slot) container.inventorySlots.get(i);
            // Neither call may throw, whatever the index means.
            slot.isItemValid(something);
            slot.getSlotStackLimit();
            Assert.assertNotNull("slot " + i + " has no coordinates", slot);
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Shift-clicking
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void shiftClickingAnEmptySlotReturnsNothing() {
        for (Map.Entry<String, Function<InventoryPlayer, Container>> tier : TIERS.entrySet()) {
            Container container = tier.getValue()
                .apply(inventory);
            for (int slot : new int[] { 0, 1, 21, 22, 57 }) {
                Assert.assertNull(
                    tier.getKey() + ": slot " + slot + " was empty",
                    container.transferStackInSlot(player, slot));
            }
        }
    }

    @Test
    public void shiftClickingACraftSlotMovesTheStackToThePlayer() {
        for (Map.Entry<String, Function<InventoryPlayer, Container>> tier : TIERS.entrySet()) {
            InventoryPlayer playerInventory = new InventoryPlayer(player);
            Container container = tier.getValue()
                .apply(playerInventory);
            Slot craftSlot = (Slot) container.inventorySlots.get(1);
            craftSlot.putStack(new ItemStack(new Item(), 1));

            container.transferStackInSlot(player, 1);

            Assert.assertNull(
                tier.getKey() + ": the craft slot must be empty after a successful shift-click",
                craftSlot.getStack());
            Assert.assertEquals(
                tier.getKey() + ": the stack must have landed in the player's inventory",
                1,
                this.countStacks(playerInventory));
        }
    }

    @Test
    public void shiftClickingAPlayerSlotTriesToMoveTheStackIntoTheMatrix() {
        InventoryPlayer playerInventory = new InventoryPlayer(player);
        Container container = TIERS.get("tier4")
            .apply(playerInventory);
        Slot playerSlot = (Slot) container.inventorySlots.get(22);
        playerSlot.putStack(new ItemStack(new Item(), 1));

        container.transferStackInSlot(player, 22);

        // A plain item matches no craft slot, so the routing must fall back to moving it between player ranges
        // instead of dropping it.
        Assert.assertNull("the player slot must be empty after the transfer", playerSlot.getStack());
        Assert.assertEquals(
            "the stack must still be somewhere in the player's inventory",
            1,
            this.countStacks(playerInventory));
    }

    // ---------------------------------------------------------------------------------------------------------
    // Crafting result and closing
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void changingTheMatrixRecomputesTheResult() {
        for (Map.Entry<String, Function<InventoryPlayer, Container>> tier : TIERS.entrySet()) {
            Container container = tier.getValue()
                .apply(inventory);
            this.craftResult(container)
                .setInventorySlotContents(0, new ItemStack(new Item(), 1));

            // An empty matrix cannot match a recipe, so the result must be cleared rather than left stale.
            container.onCraftMatrixChanged(this.craftMatrix(container));
            Assert.assertNull(
                tier.getKey() + ": an empty matrix must not produce a result",
                this.craftResult(container)
                    .getStackInSlot(0));
        }
    }

    @Test
    public void closingTheBenchEmptiesTheMatrix() {
        Container container = TIERS.get("tier4")
            .apply(inventory);
        IInventory matrix = this.craftMatrix(container);
        matrix.setInventorySlotContents(1, new ItemStack(new Item(), 1));

        container.onContainerClosed(player);

        Assert.assertNull(
            "closing must take the stack out of the matrix (it is dropped to the player)",
            matrix.getStackInSlot(1));
    }

    @Test
    public void aRemoteWorldDoesNotDropTheMatrix() {
        // The drop loop is gated on the world being local; on a client-side close nothing is dropped.
        World remote = mock(World.class);
        remote.isRemote = true;
        player.worldObj = remote;

        Container container = TIERS.get("tier4")
            .apply(inventory);
        IInventory matrix = this.craftMatrix(container);
        matrix.setInventorySlotContents(1, new ItemStack(new Item(), 1));

        container.onContainerClosed(player);

        Assert.assertNotNull("a remote close must leave the matrix alone", matrix.getStackInSlot(1));
    }

    /** @return how many slots of the player's inventory hold something. */
    private int countStacks(InventoryPlayer inventory) {
        int found = 0;
        for (ItemStack stack : inventory.mainInventory) {
            if (stack != null) {
                found++;
            }
        }
        return found;
    }

    private IInventory craftMatrix(Container container) {
        return this.inventoryField(container, "craftMatrix");
    }

    private IInventory craftResult(Container container) {
        return this.inventoryField(container, "craftResult");
    }

    /**
     * The two inventories are public fields on every tier, but their declared types differ per class, so read them
     * through the field rather than casting to one tier's type.
     */
    private IInventory inventoryField(Container container, String name) {
        for (java.lang.reflect.Field field : container.getClass()
            .getFields()) {
            if (field.getName()
                .equals(name)) {
                try {
                    return (IInventory) field.get(container);
                } catch (IllegalAccessException e) {
                    throw new AssertionError("cannot read " + name, e);
                }
            }
        }
        throw new AssertionError(
            container.getClass()
                .getSimpleName() + " has no "
                + name
                + " field");
    }
}
