package com.mjr.extraplanets.tileentities;

import static org.mockito.Mockito.mock;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.junit.Assert;
import org.junit.Test;

import com.mjr.extraplanets.tileEntities.treasureChest.TileEntityT10TreasureChest;
import com.mjr.extraplanets.tileEntities.treasureChest.TileEntityT4TreasureChest;
import com.mjr.extraplanets.tileEntities.treasureChest.TileEntityT5TreasureChest;
import com.mjr.extraplanets.tileEntities.treasureChest.TileEntityT6TreasureChest;
import com.mjr.extraplanets.tileEntities.treasureChest.TileEntityT7TreasureChest;
import com.mjr.extraplanets.tileEntities.treasureChest.TileEntityT8TreasureChest;
import com.mjr.extraplanets.tileEntities.treasureChest.TileEntityT9TreasureChest;

import micdoodle8.mods.galacticraft.api.item.IKeyable;

/**
 * Covers the treasure chest tile entities - seven tiers, one class each, all the same 27-slot chest with a key lock.
 *
 * <p>
 * The lid animation needs a world, and {@code writeToNBT} needs a block position, but the part a player actually
 * interacts with is plain data handling: the slot array, its clamping, the lock, and the key contract. Every
 * assertion here runs against all seven tiers, which is the point - the classes are copies, and a fix applied to one
 * of them is exactly the kind of thing that gets missed.
 * </p>
 */
public class TreasureChestTest {

    private static final Map<String, Supplier<TileEntity>> TIERS = new LinkedHashMap<String, Supplier<TileEntity>>();

    static {
        TIERS.put("tier4", TileEntityT4TreasureChest::new);
        TIERS.put("tier5", TileEntityT5TreasureChest::new);
        TIERS.put("tier6", TileEntityT6TreasureChest::new);
        TIERS.put("tier7", TileEntityT7TreasureChest::new);
        TIERS.put("tier8", TileEntityT8TreasureChest::new);
        TIERS.put("tier9", TileEntityT9TreasureChest::new);
        TIERS.put("tier10", TileEntityT10TreasureChest::new);
    }

    /**
     * {@code TileEntity#worldObj} is protected, so it takes a field write. The chest's unlock path reads
     * {@code worldObj.isRemote} to decide whether to send a packet or to change the lock.
     */
    private static void setWorld(TileEntity tile, World world) {
        try {
            java.lang.reflect.Field field = TileEntity.class.getDeclaredField("worldObj");
            field.setAccessible(true);
            field.set(tile, world);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("cannot set TileEntity.worldObj", e);
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Inventory contract
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void everyChestIsATwentySevenSlotInventoryThatStartsEmpty() {
        for (Map.Entry<String, Supplier<TileEntity>> tier : TIERS.entrySet()) {
            net.minecraft.inventory.IInventory chest = (net.minecraft.inventory.IInventory) tier.getValue()
                .get();

            Assert.assertEquals(tier.getKey() + ": slot count", 27, chest.getSizeInventory());
            Assert.assertEquals(tier.getKey() + ": stack limit", 64, chest.getInventoryStackLimit());
            for (int slot = 0; slot < chest.getSizeInventory(); slot++) {
                Assert.assertNull(tier.getKey() + ": slot " + slot + " should start empty", chest.getStackInSlot(slot));
            }
        }
    }

    @Test
    public void aChestClampsOversizedStacks() {
        for (Map.Entry<String, Supplier<TileEntity>> tier : TIERS.entrySet()) {
            net.minecraft.inventory.IInventory chest = (net.minecraft.inventory.IInventory) tier.getValue()
                .get();
            ItemStack oversized = new ItemStack(Blocks.stone, 200);

            chest.setInventorySlotContents(0, oversized);

            Assert.assertEquals(
                tier.getKey() + ": an oversized stack must be clamped to the inventory limit",
                64,
                chest.getStackInSlot(0).stackSize);
        }
    }

    @Test
    public void takingPartOfAStackSplitsIt() {
        for (Map.Entry<String, Supplier<TileEntity>> tier : TIERS.entrySet()) {
            net.minecraft.inventory.IInventory chest = (net.minecraft.inventory.IInventory) tier.getValue()
                .get();
            chest.setInventorySlotContents(2, new ItemStack(Blocks.stone, 10));

            ItemStack taken = chest.decrStackSize(2, 4);

            Assert.assertEquals(tier.getKey() + ": taken", 4, taken.stackSize);
            Assert.assertEquals(tier.getKey() + ": left behind", 6, chest.getStackInSlot(2).stackSize);
        }
    }

    @Test
    public void takingTheWholeStackEmptiesTheSlot() {
        for (Map.Entry<String, Supplier<TileEntity>> tier : TIERS.entrySet()) {
            net.minecraft.inventory.IInventory chest = (net.minecraft.inventory.IInventory) tier.getValue()
                .get();
            chest.setInventorySlotContents(2, new ItemStack(Blocks.stone, 4));

            ItemStack taken = chest.decrStackSize(2, 9);

            Assert.assertEquals(tier.getKey() + ": taken", 4, taken.stackSize);
            Assert.assertNull(tier.getKey() + ": slot must be empty", chest.getStackInSlot(2));
        }
    }

    @Test
    public void takingFromAnEmptySlotReturnsNull() {
        for (Map.Entry<String, Supplier<TileEntity>> tier : TIERS.entrySet()) {
            net.minecraft.inventory.IInventory chest = (net.minecraft.inventory.IInventory) tier.getValue()
                .get();
            Assert.assertNull(tier.getKey(), chest.decrStackSize(0, 1));
            Assert.assertNull(tier.getKey(), chest.getStackInSlotOnClosing(0));
        }
    }

    @Test
    public void closingASlotHandsTheStackOver() {
        for (Map.Entry<String, Supplier<TileEntity>> tier : TIERS.entrySet()) {
            net.minecraft.inventory.IInventory chest = (net.minecraft.inventory.IInventory) tier.getValue()
                .get();
            chest.setInventorySlotContents(5, new ItemStack(Blocks.stone, 3));

            ItemStack closing = chest.getStackInSlotOnClosing(5);

            Assert.assertEquals(tier.getKey() + ": stack", 3, closing.stackSize);
            Assert.assertNull(tier.getKey() + ": the slot must be empty", chest.getStackInSlot(5));
        }
    }

    @Test
    public void everyChestHasANameAndAcceptsAnything() {
        for (Map.Entry<String, Supplier<TileEntity>> tier : TIERS.entrySet()) {
            net.minecraft.inventory.IInventory chest = (net.minecraft.inventory.IInventory) tier.getValue()
                .get();
            Assert.assertNotNull(tier.getKey() + ": name", chest.getInventoryName());
            Assert.assertTrue(tier.getKey() + ": custom name flag", chest.hasCustomInventoryName());
            Assert.assertTrue(
                tier.getKey() + ": an empty chest accepts anything",
                chest.isItemValidForSlot(0, new ItemStack(Blocks.stone, 1)));
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // The lock and the key
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void everyChestStartsLockedAndNeedsItsOwnTierOfKey() {
        for (Map.Entry<String, Supplier<TileEntity>> tier : TIERS.entrySet()) {
            IKeyable chest = (IKeyable) tier.getValue()
                .get();
            int expectedTier = Integer.parseInt(
                tier.getKey()
                    .replace("tier", ""));

            Assert.assertEquals(tier.getKey() + ": key tier", expectedTier, chest.getTierOfKeyRequired());
            Assert.assertFalse(tier.getKey() + ": a chest must not be breakable", chest.canBreak());
            // These two come from Galacticraft's TileEntityAdvanced, not from the key contract.
            Assert.assertTrue(
                tier.getKey() + ": networked",
                ((micdoodle8.mods.galacticraft.core.tile.TileEntityAdvanced) chest).isNetworkedTile());
            Assert.assertTrue(
                tier.getKey() + ": packet range",
                ((micdoodle8.mods.galacticraft.core.tile.TileEntityAdvanced) chest).getPacketRange() > 0);
        }
    }

    @Test
    public void aValidKeyUnlocksTheChestAndConsumesOneKey() {
        for (Map.Entry<String, Supplier<TileEntity>> tier : TIERS.entrySet()) {
            TileEntity tile = tier.getValue()
                .get();
            // The unlock path is server-side and reads the player's held item; both are plain fields.
            EntityPlayer player = mock(EntityPlayer.class);
            World world = mock(World.class);
            player.worldObj = world;
            player.inventory = new InventoryPlayer(player);
            world.isRemote = false;
            setWorld(tile, world);
            player.inventory.mainInventory[0] = new ItemStack(Blocks.stone, 2);
            player.inventory.currentItem = 0;

            boolean unlocked = ((IKeyable) tile).onValidKeyActivated(player, player.inventory.getCurrentItem(), 1);

            Assert.assertTrue(tier.getKey() + ": the chest must unlock", unlocked);
            Assert.assertEquals(
                tier.getKey() + ": one key must be consumed",
                1,
                player.inventory.getStackInSlot(0).stackSize);
            // A second activation finds it already unlocked and reports that it did nothing.
            Assert.assertFalse(
                tier.getKey() + ": unlocking twice must be a no-op",
                ((IKeyable) tile).onValidKeyActivated(player, player.inventory.getCurrentItem(), 1));
        }
    }

    @Test
    public void theLastKeyInTheStackIsConsumed() {
        for (Map.Entry<String, Supplier<TileEntity>> tier : TIERS.entrySet()) {
            TileEntity tile = tier.getValue()
                .get();
            EntityPlayer player = mock(EntityPlayer.class);
            World world = mock(World.class);
            player.worldObj = world;
            player.inventory = new InventoryPlayer(player);
            world.isRemote = false;
            setWorld(tile, world);
            player.inventory.mainInventory[0] = new ItemStack(Blocks.stone, 1);
            player.inventory.currentItem = 0;

            ((IKeyable) tile).onValidKeyActivated(player, player.inventory.getCurrentItem(), 1);

            Assert.assertNull(
                tier.getKey() + ": the emptied stack must be cleared from the slot",
                player.inventory.getStackInSlot(0));
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Loading from NBT
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void loadingFromNbtRestoresTheLockAndTheTier() {
        for (Map.Entry<String, Supplier<TileEntity>> tier : TIERS.entrySet()) {
            TileEntity tile = tier.getValue()
                .get();
            int expectedTier = Integer.parseInt(
                tier.getKey()
                    .replace("tier", ""));

            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean("isLocked", false);
            tag.setInteger("tier", 99);
            tag.setTag("Items", new NBTTagList());

            tile.readFromNBT(tag);

            Assert.assertEquals(
                tier.getKey() + ": the tier is part of the save format",
                99,
                ((IKeyable) tile).getTierOfKeyRequired());
            // The lock is restored from the tag, so a chest that was unlocked stays unlocked across a reload.
            EntityPlayer player = mock(EntityPlayer.class);
            World world = mock(World.class);
            player.worldObj = world;
            player.inventory = new InventoryPlayer(player);
            world.isRemote = false;
            setWorld(tile, world);
            Assert.assertFalse(
                tier.getKey() + ": the unlocked state must survive the load",
                ((IKeyable) tile).onValidKeyActivated(player, new ItemStack(Blocks.stone, 1), 1));
        }
    }

    @Test
    public void loadingFromNbtToleratesATagWithNoItems() {
        // Older saves and hand-written tags may not carry the item list at all; the chest must come up empty rather
        // than throwing.
        for (Map.Entry<String, Supplier<TileEntity>> tier : TIERS.entrySet()) {
            TileEntity tile = tier.getValue()
                .get();
            tile.readFromNBT(new NBTTagCompound());

            net.minecraft.inventory.IInventory chest = (net.minecraft.inventory.IInventory) tile;
            for (int slot = 0; slot < chest.getSizeInventory(); slot++) {
                Assert.assertNull(tier.getKey() + ": slot " + slot, chest.getStackInSlot(slot));
            }
        }
    }

    @Test
    public void aChestWithNoPlayersUsingItReportsZero() {
        for (Map.Entry<String, Supplier<TileEntity>> tier : TIERS.entrySet()) {
            TileEntity tile = tier.getValue()
                .get();
            // The client event is how the lid animation is synchronised; event 1 carries the user count.
            Assert.assertTrue(tier.getKey() + ": event 1 must be handled", tile.receiveClientEvent(1, 3));
            // numUsingPlayers is a public field on each tier, but the declared type differs, so read it reflectively.
            try {
                java.lang.reflect.Field field = tile.getClass()
                    .getField("numUsingPlayers");
                Assert.assertEquals(tier.getKey() + ": user count", 3, field.getInt(tile));
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(tier.getKey() + " has no numUsingPlayers field", e);
            }
        }
    }
}
