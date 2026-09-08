package com.mjr.extraplanets.items.armor.modules;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Sensor Glasses module. Shows the GC sensor glasses overlay (valuable blocks, player entities).
 * <p>
 * The overlay rendering is wired into the client HUD render tick; this module simply marks the
 * feature as active.
 */
public class ModuleSensorGlasses extends Module {

    public ModuleSensorGlasses(String name) {
        super(
            name,
            0, // 0 = head (helmet)
            new ItemStack(Items.ender_eye),
            true,
            0,
            5);
        List<ItemStack> reqs = new ArrayList<ItemStack>();
        reqs.add(new ItemStack(Items.ender_eye, 1));
        this.setRequirements(reqs);
    }

    @Override
    public void tickServer(EntityPlayerMP player) {
        // Power cost is handled by the passive drain; no per-tick server logic needed.
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void tickClient(EntityPlayer player) {
        // Overlay rendering is handled in the client HUD event handler.
    }
}
