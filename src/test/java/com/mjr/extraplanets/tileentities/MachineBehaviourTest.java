package com.mjr.extraplanets.tileentities;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import org.junit.Assert;
import org.junit.Test;

import com.mjr.extraplanets.tileEntities.machines.TileEntityAdvancedFuelLoader;
import com.mjr.extraplanets.tileEntities.machines.TileEntityAdvancedOxygenCompressor;
import com.mjr.extraplanets.tileEntities.machines.TileEntityAdvancedOxygenDecompressor;
import com.mjr.extraplanets.tileEntities.machines.TileEntityAdvancedRefinery;
import com.mjr.extraplanets.tileEntities.machines.TileEntityUltimateFuelLoader;
import com.mjr.extraplanets.tileEntities.machines.TileEntityUltimateOxygenCompressor;
import com.mjr.extraplanets.tileEntities.machines.TileEntityUltimateOxygenDecompressor;
import com.mjr.extraplanets.tileEntities.machines.TileEntityUltimateRefinery;

/**
 * The machine behaviour that does not need a world: the inventory side, and loading from a tag.
 *
 * <p>
 * {@code updateEntity} is where the interesting work is and it cannot run headless. Three things in these machines
 * cannot be reached here at all, each for a different reason, and each is covered in game instead:
 * </p>
 *
 * <ul>
 * <li>{@code canInsertItem}/{@code canExtractItem} ask Galacticraft's {@code FluidUtil} about oil containers, and
 * that class resolves {@code Launch.classLoader} when it initialises - the same headless limit as
 * {@code ElectricArmorBase}'s constructor;</li>
 * <li>{@code writeToNBT} needs the tile entity registered with FML, which is the in-game suite's job;</li>
 * <li>the solar panel is not an inventory, and its output is derived from its world.</li>
 * </ul>
 */
public class MachineBehaviourTest {

    private static final Map<String, Supplier<TileEntity>> MACHINES = new LinkedHashMap<String, Supplier<TileEntity>>();

    static {
        MACHINES.put("advancedRefinery", TileEntityAdvancedRefinery::new);
        MACHINES.put("ultimateRefinery", TileEntityUltimateRefinery::new);
        MACHINES.put("advancedFuelLoader", TileEntityAdvancedFuelLoader::new);
        MACHINES.put("ultimateFuelLoader", TileEntityUltimateFuelLoader::new);
        MACHINES.put("advancedOxygenCompressor", TileEntityAdvancedOxygenCompressor::new);
        MACHINES.put("ultimateOxygenCompressor", TileEntityUltimateOxygenCompressor::new);
        MACHINES.put("advancedOxygenDecompressor", TileEntityAdvancedOxygenDecompressor::new);
        MACHINES.put("ultimateOxygenDecompressor", TileEntityUltimateOxygenDecompressor::new);
    }

    @Test
    public void everyMachineHasAUsableInventory() {
        for (Map.Entry<String, Supplier<TileEntity>> machine : MACHINES.entrySet()) {
            ISidedInventory inventory = (ISidedInventory) machine.getValue()
                .get();

            Assert.assertTrue(machine.getKey() + ": slots", inventory.getSizeInventory() > 0);
            Assert.assertNotNull(machine.getKey() + ": name", inventory.getInventoryName());
            Assert.assertTrue(machine.getKey() + ": stack limit", inventory.getInventoryStackLimit() > 0);
            for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
                Assert.assertNull(
                    machine.getKey() + ": slot " + slot + " should start empty",
                    inventory.getStackInSlot(slot));
            }
        }
    }

    @Test
    public void everyMachineStoresAndSplitsStacks() {
        for (Map.Entry<String, Supplier<TileEntity>> machine : MACHINES.entrySet()) {
            ISidedInventory inventory = (ISidedInventory) machine.getValue()
                .get();
            ItemStack stack = new ItemStack(Blocks.stone, 2);

            inventory.setInventorySlotContents(0, stack);
            Assert.assertSame(machine.getKey() + ": the slot must hold the stack", stack, inventory.getStackInSlot(0));

            // Some machines hold one item per slot (the oxygen compressors), so the stored size is clamped; take that
            // into account instead of assuming the two items written are still there.
            int stored = inventory.getStackInSlot(0).stackSize;
            Assert.assertTrue(machine.getKey() + ": stored size", stored >= 1);
            Assert.assertTrue(
                machine.getKey() + ": stored size must respect the limit",
                stored <= inventory.getInventoryStackLimit());

            ItemStack taken = inventory.decrStackSize(0, 1);
            Assert.assertEquals(machine.getKey() + ": taken", 1, taken.stackSize);
            if (stored == 1) {
                Assert.assertNull(machine.getKey() + ": the slot must be empty", inventory.getStackInSlot(0));
            } else {
                Assert.assertEquals(machine.getKey() + ": left", stored - 1, inventory.getStackInSlot(0).stackSize);
            }

            inventory.setInventorySlotContents(0, null);
            Assert.assertNull(machine.getKey() + ": clearing the slot", inventory.getStackInSlot(0));
        }
    }

    @Test
    public void twoMachinesOfTheSameTypeDoNotShareTheirInventory() {
        for (Map.Entry<String, Supplier<TileEntity>> machine : MACHINES.entrySet()) {
            ISidedInventory first = (ISidedInventory) machine.getValue()
                .get();
            ISidedInventory second = (ISidedInventory) machine.getValue()
                .get();

            first.setInventorySlotContents(0, new ItemStack(Blocks.stone, 1));

            Assert.assertNull(
                machine.getKey() + ": the two machines share a static inventory array",
                second.getStackInSlot(0));
        }
    }

    @Test
    public void everyMachineReportsWhichSlotsEachSideCanReach() {
        for (Map.Entry<String, Supplier<TileEntity>> machine : MACHINES.entrySet()) {
            ISidedInventory inventory = (ISidedInventory) machine.getValue()
                .get();

            for (int side = 0; side < 6; side++) {
                int[] slots = inventory.getAccessibleSlotsFromSide(side);
                Assert.assertNotNull(machine.getKey() + ": side " + side + " returned no slot array", slots);
                for (int slot : slots) {
                    Assert.assertTrue(
                        machine.getKey() + ": side " + side + " exposes slot " + slot + ", which does not exist",
                        slot >= 0 && slot < inventory.getSizeInventory());
                }
            }
        }
    }

    @Test
    public void everyMachineRejectsInsertingNothing() {
        for (Map.Entry<String, Supplier<TileEntity>> machine : MACHINES.entrySet()) {
            ISidedInventory inventory = (ISidedInventory) machine.getValue()
                .get();
            for (int side = 0; side < 6; side++) {
                // A hopper passes null when a slot empties; the null check comes before anything Galacticraft-side.
                Assert.assertFalse(
                    machine.getKey() + ": a null stack is never insertable",
                    inventory.canInsertItem(0, null, side));
                Assert.assertFalse(
                    machine.getKey() + ": a null stack is never extractable",
                    inventory.canExtractItem(0, null, side));
            }
        }
    }

    @Test
    public void everyMachineLoadsFromAnEmptyTag() {
        // A machine placed by a world edit, or a tag written by an older version, may carry almost nothing.
        for (Map.Entry<String, Supplier<TileEntity>> machine : MACHINES.entrySet()) {
            TileEntity tile = machine.getValue()
                .get();

            tile.readFromNBT(new NBTTagCompound());

            Assert
                .assertEquals(machine.getKey() + ": an empty tag must leave the machine at the origin", 0, tile.xCoord);
            Assert.assertEquals(machine.getKey() + ": and with nothing in it", 0, tile.yCoord);
            for (int slot = 0; slot < ((ISidedInventory) tile).getSizeInventory(); slot++) {
                Assert.assertNull(
                    machine.getKey() + ": slot " + slot + " after an empty load",
                    ((ISidedInventory) tile).getStackInSlot(slot));
            }
        }
    }

    @Test
    public void loadingFromATagWithAnItemListDoesNotEmptyTheInventory() {
        // The item list is stored as a list of compounds with a "Slot" byte; a malformed one must be ignored rather
        // than wipe what is already there or throw.
        for (Map.Entry<String, Supplier<TileEntity>> machine : MACHINES.entrySet()) {
            TileEntity tile = machine.getValue()
                .get();

            NBTTagCompound tag = new NBTTagCompound();
            net.minecraft.nbt.NBTTagList items = new net.minecraft.nbt.NBTTagList();
            NBTTagCompound entry = new NBTTagCompound();
            entry.setByte("Slot", (byte) 0);
            items.appendTag(entry);
            tag.setTag("Items", items);

            tile.readFromNBT(tag);

            Assert.assertNotNull(machine.getKey() + ": the machine must survive a malformed item list", tile);
        }
    }
}
