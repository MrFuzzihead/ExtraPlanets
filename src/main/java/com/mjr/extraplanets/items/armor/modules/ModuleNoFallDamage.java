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
 * No Fall Damage module for boots. Negates all fall damage while the module is active.
 * Power is drained per fall tick (100 gJ per tick while falling).
 * <p>
 * Ported from upstream ExtraPlanets.
 */
public class ModuleNoFallDamage extends Module {

    public ModuleNoFallDamage(String name) {
        super(
            name,
            3, // 3 = boots
            new ItemStack(Items.feather),
            true,
            0,
            100);
        List<ItemStack> reqs = new ArrayList<ItemStack>();
        reqs.add(new ItemStack(Items.feather, 8));
        reqs.add(new ItemStack(Items.iron_boots, 1));
        this.setRequirements(reqs);
    }

    @Override
    public void tickServer(EntityPlayerMP player) {
        if (player.fallDistance != 0.0F) {
            ItemStack boots = player.inventory.armorItemInSlot(0);
            // Only negate fall damage if we have power to drain
            if (boots != null && ModuleHelper.hasPower(boots, this.getUsePowerCost())) {
                // Drain power every 20 ticks (1 second) while falling
                if ((player.ticksExisted - 1) % 20 == 0) {
                    ModuleHelper.takeArmourPower(boots, this.getUsePowerCost());
                }
                player.fallDistance = 0.0F;
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void tickClient(EntityPlayer player) {}
}
