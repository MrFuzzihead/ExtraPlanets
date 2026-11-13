package com.mjr.extraplanets.items.armor.modules;

import java.util.Collections;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Step Assist module for leggings. Allows the player to automatically step up 1-block
 * heights without jumping. This is implemented by setting the player's step height to 1.0
 * while the module is active.
 */
public class ModuleStepAssist extends Module {

    public ModuleStepAssist(String name) {
        super(
            name,
            2, // 2 = legs
            new ItemStack(Blocks.piston),
            true,
            2,
            0);
        this.setRequirementSupplier(() -> Collections.singletonList(new ItemStack(Blocks.piston, 1)));
    }

    @Override
    public void tickServer(EntityPlayerMP player) {
        player.stepHeight = 1.0F;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void tickClient(EntityPlayer player) {
        player.stepHeight = 1.0F;
    }
}
