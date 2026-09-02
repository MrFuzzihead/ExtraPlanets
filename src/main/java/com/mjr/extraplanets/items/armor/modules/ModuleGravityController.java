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
 * Gravity Controller module for boots. Provides GC gravity override on low-G and high-G worlds.
 * <p>
 * <code>gravityOverrideIfLow = 55</code> — makes the player feel heavier on low-G worlds,
 * counteracting the floaty movement.
 * <code>gravityOverrideIfHigh = 75</code> — makes the player feel lighter on high-G worlds,
 * counteracting the heaviness.
 * <p>
 * Replaces the old separate {@code spaceSuitGravityBoots} item.
 */
public class ModuleGravityController extends Module {

    public static final int OVERRIDE_LOW = 55;
    public static final int OVERRIDE_HIGH = 75;

    public ModuleGravityController(String name) {
        super(
            name,
            3, // 3 = boots
            new ItemStack(Blocks.iron_block),
            true,
            5,
            0);
        List<ItemStack> reqs = new ArrayList<ItemStack>();
        reqs.add(new ItemStack(Blocks.iron_block, 1));
        this.setRequirements(reqs);
    }

    /**
     * @return true if the boots item has an active Gravity Controller module.
     */
    public static boolean isActiveOn(ItemStack boots) {
        return ModuleHelper.isModuleActive(boots, "gravity_controller");
    }

    @Override
    public void tickServer(EntityPlayerMP player) {}

    @Override
    @SideOnly(Side.CLIENT)
    public void tickClient(EntityPlayer player) {}
}
