package com.mjr.extraplanets.items.armor.modules;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.mjr.extraplanets.items.ExtraPlanets_Items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Pressure seal module. A single module whose tier (1-4) is stored in the {@code data} field
 * and determined by which pressure layer item is used when installing.
 * <p>
 * Higher tiers provide better pressure protection but cost more power.
 * <p>
 * Without this module (or with it inactive), the suit piece provides zero pressure protection.
 * <p>
 * Requirements (crafting items used to install):
 * <ul>
 * <li>Tier 1 — Tier 1 Pressure Layer</li>
 * <li>Tier 2 — Tier 2 Pressure Layer</li>
 * <li>Tier 3 — Tier 3 Pressure Layer</li>
 * <li>Tier 4 — Tier 4 Pressure Layer</li>
 * </ul>
 */
public class ModulePressureSeal extends Module {

    public ModulePressureSeal(String name) {
        super(name, -1, new ItemStack(Items.iron_ingot), true, 5, 0);
        this.setSubType(1); // default tier
        // Default requirement — tier 1. The GUI will accept any tier's item and set the subType accordingly.
        List<ItemStack> reqs = new ArrayList<ItemStack>();
        reqs.add(new ItemStack(ExtraPlanets_Items.tier1PressureLayer, 1));
        this.setRequirements(reqs);
    }

    @Override
    public void tickServer(EntityPlayerMP player) {
        // Passive power cost scales with tier
        setPassivePowerCost(5 * getSubType());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void tickClient(EntityPlayer player) {}
}
