package com.mjr.extraplanets.tileentities;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.Assert;
import org.junit.Test;

import com.mjr.extraplanets.tileEntities.machines.TileEntityAdvancedFuelLoader;
import com.mjr.extraplanets.tileEntities.machines.TileEntityAdvancedOxygenCompressor;
import com.mjr.extraplanets.tileEntities.machines.TileEntityAdvancedRefinery;
import com.mjr.extraplanets.tileEntities.machines.TileEntitySolar;
import com.mjr.extraplanets.tileEntities.machines.TileEntityUltimateFuelLoader;
import com.mjr.extraplanets.tileEntities.machines.TileEntityUltimateOxygenCompressor;
import com.mjr.extraplanets.tileEntities.machines.TileEntityUltimateRefinery;

/**
 * Constructs every machine without a world and checks the parts that are observable without one.
 *
 * <p>
 * The save/load path itself cannot be tested here: {@code TileEntity.writeToNBT} looks the class up in FML's tile
 * entity registry ({@code TileEntity.addMapping}) and throws "is missing a mapping!" outside a loaded game. The NBT
 * round trip is covered by the in-game {@code gameTest} mod instead, which runs against the real registry.
 * </p>
 *
 * <p>
 * What is worth pinning headless is the per-instance state: a machine that shared its tank or its inventory array
 * through a static would corrupt every other machine of the same type, and {@code readFromNBT} has to tolerate a
 * tag written by an older version that is missing keys.
 * </p>
 */
public class MachineTileEntityTest {

    @Test
    public void everyMachineCanBeConstructedWithoutAWorld() {
        Assert.assertNotNull(new TileEntityAdvancedRefinery());
        Assert.assertNotNull(new TileEntityUltimateRefinery());
        Assert.assertNotNull(new TileEntityAdvancedFuelLoader());
        Assert.assertNotNull(new TileEntityUltimateFuelLoader());
        Assert.assertNotNull(new TileEntityAdvancedOxygenCompressor());
        Assert.assertNotNull(new TileEntityUltimateOxygenCompressor());
        Assert.assertNotNull(new TileEntitySolar());
    }

    @Test
    public void refineryTanksArePresentAndIndependent() {
        TileEntityAdvancedRefinery first = new TileEntityAdvancedRefinery();
        TileEntityAdvancedRefinery second = new TileEntityAdvancedRefinery();

        Assert.assertNotNull(first.oilTank);
        Assert.assertNotNull(first.fuelTank);
        Assert.assertNotSame("tank state must not be shared through a static", first.oilTank, second.oilTank);
        Assert.assertNotSame(first.fuelTank, second.fuelTank);
        Assert.assertEquals(0, first.oilTank.getFluidAmount());
        Assert.assertEquals(first.oilTank.getCapacity(), first.fuelTank.getCapacity());
        Assert.assertTrue("the refinery tank should hold more than a bucket", first.oilTank.getCapacity() > 1000);
    }

    @Test
    public void readingAnEmptyTagLeavesTheRefineryUsable() {
        // Chunk data from an older version of the mod may be missing keys the current class expects; readFromNBT
        // must not leave the tanks null.
        TileEntityAdvancedRefinery refinery = new TileEntityAdvancedRefinery();
        refinery.readFromNBT(new NBTTagCompound());
        Assert.assertNotNull(refinery.oilTank);
        Assert.assertNotNull(refinery.fuelTank);
        Assert.assertEquals(0, refinery.oilTank.getFluidAmount());
    }

    @Test
    public void readingAnEmptyTagLeavesTheOtherMachinesUsable() {
        new TileEntityUltimateRefinery().readFromNBT(new NBTTagCompound());
        new TileEntityAdvancedFuelLoader().readFromNBT(new NBTTagCompound());
        new TileEntityUltimateFuelLoader().readFromNBT(new NBTTagCompound());
        new TileEntityAdvancedOxygenCompressor().readFromNBT(new NBTTagCompound());
        new TileEntityUltimateOxygenCompressor().readFromNBT(new NBTTagCompound());
        new TileEntitySolar().readFromNBT(new NBTTagCompound());
    }
}
