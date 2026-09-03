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
 * Radiation shield module. A single module whose tier (1-3) is stored in the {@code data} field
 * and determined by the item used when installing. Higher tiers provide better radiation
 * protection but cost more power.
 * <p>
 * Tier 1 — lead ingots (basic)<br>
 * Tier 2 — uranium ingot (moderate)<br>
 * Tier 3 — dark iron ingot (full)
 */
public class ModuleRadiationShield extends Module {

    public ModuleRadiationShield(String name) {
        super(name, -1, new ItemStack(Items.iron_ingot), true, 10, 0);
        this.setSubType(1); // default tier
        // Default requirements — tier 1. Install logic checks inventory to pick the highest
        // tier the player can afford.
        List<ItemStack> reqs = new ArrayList<ItemStack>();
        reqs.add(new ItemStack(ExtraPlanets_Items.ingotLead, 1));
        this.setRequirements(reqs);
    }

    @Override
    public void tickServer(EntityPlayerMP player) {
        // Passive power cost scales with tier
        setPassivePowerCost(10 * getSubType());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void tickClient(EntityPlayer player) {}
}
