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
 * Night Vision module. Grants permanent night vision while the helmet is worn.
 * <p>
 * The effect is given a very long duration and refreshed well before expiry so the
 * vanilla darkening vignette never appears.
 */
public class ModuleNightVision extends Module {

    /**
     * How often (ticks) to re-apply the night vision effect.
     * 150 ticks = 7.5 seconds — well below the 1-second vignette threshold.
     */
    private static final int REFRESH_INTERVAL = 150;

    /**
     * Duration of the applied effect, in ticks. Must be longer than REFRESH_INTERVAL.
     * 600 ticks = 30 seconds.
     */
    private static final int EFFECT_DURATION = 600;

    public ModuleNightVision(String name) {
        super(
            name,
            0, // 0 = head (helmet)
            new ItemStack(Items.golden_carrot),
            true,
            1,
            0);
        List<ItemStack> reqs = new ArrayList<ItemStack>();
        reqs.add(new ItemStack(Items.golden_carrot, 1));
        reqs.add(new ItemStack(Items.glowstone_dust, 4));
        this.setRequirements(reqs);
    }

    @Override
    public void tickServer(EntityPlayerMP player) {
        if ((player.ticksExisted - 1) % REFRESH_INTERVAL == 0) {
            player.addPotionEffect(new PotionEffect(Potion.nightVision.getId(), EFFECT_DURATION, 0, true));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void tickClient(EntityPlayer player) {}
}
