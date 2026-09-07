package com.mjr.extraplanets.items.armor.modules;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.mjr.extraplanets.items.ExtraPlanets_Items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Radiation shield module. A single module whose tier (1-4) is stored in the {@code data} field
 * and determined by which radiation layer item is used when installing.
 * <p>
 * Higher tiers provide better radiation protection but cost more power.
 * <p>
 * Without this module (or with it inactive), the suit piece provides zero radiation protection.
 * <p>
 * Requirements (crafting items used to install):
 * <ul>
 * <li>Tier 1 — Tier 1 Radiation Layer</li>
 * <li>Tier 2 — Tier 2 Radiation Layer</li>
 * <li>Tier 3 — Tier 3 Radiation Layer</li>
 * <li>Tier 4 — Tier 4 Radiation Layer</li>
 * </ul>
 */
public class ModuleRadiationShield extends Module {

    public ModuleRadiationShield(String name) {
        super(name, -1, new ItemStack(Items.iron_ingot), true, 10, 0);
        this.setSubType(1); // default tier
        // Default requirement — tier 1. The GUI will accept any tier's item and set the subType accordingly.
        List<ItemStack> reqs = new ArrayList<ItemStack>();
        reqs.add(new ItemStack(ExtraPlanets_Items.tier1RadiationLayer, 1));
        this.setRequirements(reqs);
    }

    @Override
    public void tickServer(EntityPlayerMP player) {
        // Passive power cost scales with tier
        setPassivePowerCost(10 * getSubType());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void tickClient(EntityPlayer player) {}
}
