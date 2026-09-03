package com.mjr.extraplanets.items.armor.modules;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import com.mjr.extraplanets.armor.bases.ElectricArmorBase;
import com.mjr.extraplanets.items.ExtraPlanets_Items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Battery Expansion module. Increases the max energy storage of the suit piece by 25%.
 * Stackable up to 4 modules (100% extra = double capacity).
 */
public class ModuleBatteryExpansion extends Module {

    /** Additional capacity multiplier per module installed: 0.25 (25 %). */
    public static final double CAPACITY_BOOST_PER_MODULE = 0.25;
    /** Hard limit on the number of battery expansion modules per piece. */
    public static final int MAX_MODULES = 4;

    public ModuleBatteryExpansion(String name) {
        super(name, -1, new ItemStack(ExtraPlanets_Items.advancedBattery), true, 0, 0);
        List<ItemStack> reqs = new ArrayList<ItemStack>();
        reqs.add(new ItemStack(ExtraPlanets_Items.advancedBattery, 1));
        this.setRequirements(reqs);
    }

    /**
     * Computes the boosted max electricity for a given item by counting installed
     * Battery Expansion modules and applying the per-module multiplier to the base capacity.
     *
     * @param baseCapacity the unboosted capacity (from the item's normal getMaxElectricityStored)
     * @param item         the armour item
     * @return the boosted max electricity
     */
    public static float getBoostedCapacity(float baseCapacity, ItemStack item) {
        if (!(item.getItem() instanceof ElectricArmorBase)) {
            return baseCapacity;
        }
        int count = 0;
        for (Module m : ModuleHelper.getModules(item)) {
            if (m instanceof ModuleBatteryExpansion && m.isActive()) {
                count++;
            }
        }
        count = Math.min(count, MAX_MODULES);
        return baseCapacity * (1.0F + (float) (CAPACITY_BOOST_PER_MODULE * count));
    }

    @Override
    public void tickServer(EntityPlayerMP player) {}

    @Override
    @SideOnly(Side.CLIENT)
    public void tickClient(EntityPlayer player) {}
}
