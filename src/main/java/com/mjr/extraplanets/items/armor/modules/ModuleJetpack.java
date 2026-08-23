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
 * Jetpack module for the chestplate. Provides vertical thrust while holding the jump key
 * in mid-air.
 * <p>
 * T1: 0.15 accel, 0.5 max speed, 35 gJ/tick.
 * T2: 0.25 accel, 0.7 max speed, 50 gJ/tick.
 * <p>
 * The actual jetpack key handling (space-in-mid-air) is managed by the client tick.
 */
public class ModuleJetpack extends Module {

    private final int tier;
    private final double accelSpeed;
    private final double maxAccel;

    public ModuleJetpack(String name, int tier) {
        super(
            name,
            1, // 1 = chest
            new ItemStack(Blocks.piston),
            false,
            0,
            tier == 1 ? 35 : 50);
        this.tier = tier;
        this.accelSpeed = tier == 1 ? 0.15 : 0.25;
        this.maxAccel = tier == 1 ? 0.5 : 0.7;
        List<ItemStack> reqs = new ArrayList<ItemStack>();
        reqs.add(new ItemStack(Blocks.piston, 2));
        this.setRequirements(reqs);
    }

    public int getTier() {
        return tier;
    }

    public double getAccelSpeed() {
        return accelSpeed;
    }

    public double getMaxAccelSpeed() {
        return maxAccel;
    }

    @Override
    public void tickServer(EntityPlayerMP player) {
        // Thrust handling is done on the client side via key binding.
        // The server-side tick just deducts power when the player is thrusting.
        // The actual thrust state is synced via a packet or detected by checking
        // the player's motionY change.
        // For now, we rely on the client to send a packet or the server to detect
        // the player holding space. This is a placeholder that works with the
        // client tickServer calling the thrust logic.
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void tickClient(EntityPlayer player) {
        // Client-side thrust is handled by a key handler, not here.
    }
}
