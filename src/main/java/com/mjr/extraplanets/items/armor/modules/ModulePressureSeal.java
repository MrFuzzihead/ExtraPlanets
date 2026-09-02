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
 * Pressure seal module. A single module whose tier (1-2) is stored in the {@code data} field.
 * Higher tiers protect against more extreme pressure environments.
 * <p>
 * Tier 1 — lead (basic pressure protection)<br>
 * Tier 2 — tungsten (extreme pressure protection)
 */
public class ModulePressureSeal extends Module {

    public ModulePressureSeal(String name) {
        super(name, -1, new ItemStack(Items.iron_ingot), true, 5, 0);
        this.setSubType(1);
        List<ItemStack> reqs = new ArrayList<ItemStack>();
        reqs.add(new ItemStack(ExtraPlanets_Items.ingotLead, 1));
        this.setRequirements(reqs);
    }

    @Override
    public void tickServer(EntityPlayerMP player) {
        setPassivePowerCost(5 * getSubType());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void tickClient(EntityPlayer player) {}
}
