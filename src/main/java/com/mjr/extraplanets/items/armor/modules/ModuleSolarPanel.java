package com.mjr.extraplanets.items.armor.modules;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import com.mjr.extraplanets.armor.bases.ElectricArmorBase;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Solar Panel module. Recharges all 4 suit pieces in sunlight.
 * <p>
 * Basic (tier 0): 5 gJ per 60 ticks per piece (~1.4 hours to fully charge a 50k suit).
 * Advanced (tier 1): 10 gJ per 60 ticks per piece.
 * <p>
 * Ported from upstream ExtraPlanets.
 */
public class ModuleSolarPanel extends Module {

    private final int tier;

    public ModuleSolarPanel(String name, int tier) {
        super(
            name,
            0, // 0 = head (helmet)
            new ItemStack(Blocks.daylight_detector),
            true,
            0,
            0);
        this.tier = tier;
        List<ItemStack> reqs = new ArrayList<ItemStack>();
        reqs.add(tier == 0 ? new ItemStack(Blocks.daylight_detector, 1) : new ItemStack(Blocks.daylight_detector, 2));
        this.setRequirements(reqs);
    }

    @Override
    public void tickServer(EntityPlayerMP player) {
        if (!player.worldObj.isDaytime()) {
            return;
        }
        // Recharge every 60 ticks (3 seconds)
        if ((player.ticksExisted - 1) % 60 != 0) {
            return;
        }

        float basePower = tier == 0 ? 5F : 10F;

        for (int slot = 0; slot < 4; slot++) {
            ItemStack piece = player.inventory.armorItemInSlot(slot);
            if (piece != null && piece.getItem() instanceof ElectricArmorBase) {
                ((ElectricArmorBase) piece.getItem()).recharge(piece, basePower, true);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void tickClient(EntityPlayer player) {}
}
