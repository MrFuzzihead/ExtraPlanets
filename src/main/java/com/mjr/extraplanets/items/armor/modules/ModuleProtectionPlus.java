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
 * Protection + module. Bumps the armour's protection by one vanilla tier per level.
 * <p>
 * Tier 1 (Diamond) — requires the same vanilla armour piece of the target tier as a crafting
 * ingredient (e.g. diamond chestplate for a chestplate protection module).
 * <p>
 * Future tiers could use Netherite-equivalent materials.
 */
public class ModuleProtectionPlus extends Module {

    private final int tier;
    private static final int[][] TIER_PROTECTION = { {}, // index 0 unused
        { 3, 8, 6, 3 }, // tier 1 = Diamond {helm, chest, legs, boots}
    };

    public ModuleProtectionPlus(String name, int tier) {
        super(name, -1, new ItemStack(Items.diamond_chestplate), true, 5, 0);
        this.tier = tier;
        // The requirement is the vanilla armour piece matching the item's slot, but we can't
        // know the slot at construction time. We just require a generic diamond chestplate
        // as a placeholder and will validate slot compatibility at install time.
        List<ItemStack> reqs = new ArrayList<ItemStack>();
        reqs.add(new ItemStack(Items.diamond_chestplate, 1));
        this.setRequirements(reqs);
    }

    public int getTier() {
        return tier;
    }

    /**
     * Returns the protection array for this tier, indexed by armour slot (0=feet, 1=legs,
     * 2=chest, 3=head).
     */
    public static int[] getProtectionForTier(int tier) {
        if (tier >= 1 && tier < TIER_PROTECTION.length) {
            return TIER_PROTECTION[tier];
        }
        return TIER_PROTECTION[TIER_PROTECTION.length - 1];
    }

    @Override
    public void tickServer(EntityPlayerMP player) {}

    @Override
    @SideOnly(Side.CLIENT)
    public void tickClient(EntityPlayer player) {}
}
