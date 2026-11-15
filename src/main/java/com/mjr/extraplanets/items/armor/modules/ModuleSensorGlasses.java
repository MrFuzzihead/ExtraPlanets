package com.mjr.extraplanets.items.armor.modules;

import java.util.Collections;

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
        this.setRequirementSupplier(() -> Collections.singletonList(new ItemStack(Items.ender_eye, 1)));
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
