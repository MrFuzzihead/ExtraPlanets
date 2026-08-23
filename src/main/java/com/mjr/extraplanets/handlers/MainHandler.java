package com.mjr.extraplanets.handlers;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.mjr.extraplanets.api.item.IModularArmor;
import com.mjr.extraplanets.items.ExtraPlanets_Items;
import com.mjr.extraplanets.items.armor.modules.ModuleHelper;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerHandler.ThermalArmorEvent;
import micdoodle8.mods.galacticraft.planets.asteroids.items.AsteroidsItems;

public class MainHandler {

    /**
     * Per-player cache of the last known step height state, keyed by player.
     * Avoids NBT parsing every tick — only re-evaluates when the leggings slot changes.
     */
    private static final Map<EntityPlayer, StepState> stepCache = new WeakHashMap<EntityPlayer, StepState>();

    private static final class StepState {

        /** The leggings ItemStack from the previous tick (identity check). */
        ItemStack lastLeggings;
        /** Whether the Step Assist module was active the last time we checked. */
        boolean moduleActive;
        /**
         * Whether we were the ones that set stepHeight to 1.0F. If true, we must
         * reset it when the module deactivates. If false, we leave it alone — some
         * other mod may have set it.
         */
        boolean weSetIt;
        /** The tick number when we last verified the module state. */
        long lastCheckTick;
    }

    @SubscribeEvent
    public void onThermalArmorEvent(ThermalArmorEvent event) {
        if (event.armorStack == null) {
            event.setArmorAddResult(ThermalArmorEvent.ArmorAddResult.REMOVE);
            return;
        }
        if (event.armorStack.getItem() == AsteroidsItems.thermalPadding
            && event.armorStack.getItemDamage() == event.armorIndex) {
            event.setArmorAddResult(ThermalArmorEvent.ArmorAddResult.ADD);
            return;
        }
        if (event.armorStack.getItem() == ExtraPlanets_Items.tier2ThermalPadding
            && event.armorStack.getItemDamage() == event.armorIndex) {
            event.setArmorAddResult(ThermalArmorEvent.ArmorAddResult.ADD);
            return;
        }
        if (event.armorStack.getItem() == ExtraPlanets_Items.tier3ThermalPadding
            && event.armorStack.getItemDamage() == event.armorIndex) {
            event.setArmorAddResult(ThermalArmorEvent.ArmorAddResult.ADD);
            return;
        }
        if (event.armorStack.getItem() == ExtraPlanets_Items.tier4ThermalPadding
            && event.armorStack.getItemDamage() == event.armorIndex) {
            event.setArmorAddResult(ThermalArmorEvent.ArmorAddResult.ADD);
            return;
        }
        event.setArmorAddResult(ThermalArmorEvent.ArmorAddResult.NOTHING);
    }

    /**
     * Manages the Step Assist module's step height. Re-evaluates the module state
     * only when the leggings stack changes or once per second, avoiding per-tick NBT
     * parsing while still covering the "all pieces removed" edge case.
     */
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (player == null || event.phase != TickEvent.Phase.END) {
            return;
        }

        ItemStack leggings = player.inventory.armorItemInSlot(1);
        StepState state = stepCache.get(player);

        if (state == null) {
            state = new StepState();
            stepCache.put(player, state);
        }

        // Detect change: different stack in the slot, or periodic re-check (every 20 ticks)
        boolean changed = (leggings != state.lastLeggings) || (player.ticksExisted - state.lastCheckTick >= 20);

        if (changed) {
            state.lastLeggings = leggings;
            state.lastCheckTick = player.ticksExisted;
            state.moduleActive = leggings != null && leggings.getItem() instanceof IModularArmor
                && ModuleHelper.isModuleActive(leggings, "step_assist");
        }

        if (state.moduleActive) {
            // We're the active source — set stepHeight and remember it.
            player.stepHeight = 1.0F;
            state.weSetIt = true;
        } else if (state.weSetIt) {
            // We previously set stepHeight but the module is gone — reset to default.
            player.stepHeight = 0.5F;
            state.weSetIt = false;
        }
        // else: we never set stepHeight, so leave it alone (other mod may have set it)
    }
}
