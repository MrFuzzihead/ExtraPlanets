package com.mjr.extraplanets.items.armor.modules;

import java.util.Collections;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Anti-Gravity Jump module for boots. Increases jump height by 1.5× using a constant
 * jump boost potion effect.
 */
public class ModuleAntiGravityJump extends Module {

    private static final int REFRESH_INTERVAL = 200;

    public ModuleAntiGravityJump(String name) {
        super(
            name,
            3, // 3 = boots
            new ItemStack(Blocks.sticky_piston),
            true,
            5,
            0);
        this.setRequirementSupplier(() -> Collections.singletonList(new ItemStack(Blocks.sticky_piston, 1)));
    }

    @Override
    public void tickServer(EntityPlayerMP player) {
        if ((player.ticksExisted - 1) % REFRESH_INTERVAL == 0) {
            player.addPotionEffect(new PotionEffect(Potion.jump.getId(), REFRESH_INTERVAL + 20, 0, true));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void tickClient(EntityPlayer player) {}
}
