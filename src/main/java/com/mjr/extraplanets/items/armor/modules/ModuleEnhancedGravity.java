package com.mjr.extraplanets.items.armor.modules;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Enhanced Gravity module for boots. Stronger version of the Gravity Controller,
 * with <code>gravityOverrideIfLow = 80</code> and <code>gravityOverrideIfHigh = 90</code>.
 */
public class ModuleEnhancedGravity extends Module {

    public static final int OVERRIDE_LOW = 80;
    public static final int OVERRIDE_HIGH = 90;

    public ModuleEnhancedGravity(String name) {
        super(
            name,
            3, // 3 = boots
            new ItemStack(Blocks.obsidian),
            true,
            10,
            0);
        List<ItemStack> reqs = new ArrayList<ItemStack>();
        reqs.add(new ItemStack(Blocks.obsidian, 1));
        this.setRequirements(reqs);
    }

    public static boolean isActiveOn(ItemStack boots) {
        return ModuleHelper.isModuleActive(boots, "enhanced_gravity");
    }

    @Override
    public void tickServer(EntityPlayerMP player) {}

    @Override
    @SideOnly(Side.CLIENT)
    public void tickClient(EntityPlayer player) {}
}
