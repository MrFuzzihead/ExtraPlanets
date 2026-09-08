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
        List<ItemStack> reqs = new ArrayList<ItemStack>();
        reqs.add(new ItemStack(Blocks.piston, 1));
        this.setRequirements(reqs);
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
