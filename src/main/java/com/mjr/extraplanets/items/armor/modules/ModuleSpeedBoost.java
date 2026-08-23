package com.mjr.extraplanets.items.armor.modules;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Speed Boost module for leggings. Increases movement speed by 10% (T1) or 20% (T2).
 * Uses a potion effect with ambient=true to suppress visible particles.
 * Provides a clean visual without the swirling potion particles.
 */
public class ModuleSpeedBoost extends Module {

    private final int tier;
    private static final int REFRESH_INTERVAL = 200;

    public ModuleSpeedBoost(String name, int tier) {
        super(
            name,
            2, // 2 = legs
            new ItemStack(Items.sugar),
            true,
            5 * tier,
            0);
        this.tier = tier;
        List<ItemStack> reqs = new ArrayList<ItemStack>();
        reqs.add(new ItemStack(Items.sugar, 4));
        if (tier >= 2) {
            reqs.add(new ItemStack(Items.blaze_powder, 2));
        }
        this.setRequirements(reqs);
    }

    public int getTier() {
        return tier;
    }

    @Override
    public void tickServer(EntityPlayerMP player) {
        if ((player.ticksExisted - 1) % REFRESH_INTERVAL == 0) {
            int amplifier = tier - 1; // T1 = speed I, T2 = speed II
            player.addPotionEffect(new PotionEffect(Potion.moveSpeed.getId(), REFRESH_INTERVAL + 20, amplifier, true));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void tickClient(EntityPlayer player) {
        // The potion effect is applied server-side and synced to the client.
        // The 'isAmbient' flag (4th arg) hides the swirling particles.
    }
}
