package com.mjr.extraplanets.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;

import com.mjr.extraplanets.armor.Tier1SpaceSuitArmor;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import micdoodle8.mods.galacticraft.core.client.render.entities.RenderPlayerGC;

/**
 * Hides Galacticraft's Thermal Padding layer when a player is wearing a full ExtraPlanets space
 * suit. GC renders the thermal padding (an animated, semi-transparent 0.25F-expanded ModelBiped)
 * over the player body - either underneath the standard armor pass (mixin path) or on top of it
 * (RenderPlayerAPI path) - and its boxes do not match the suit's custom OBJ geometry, so they
 * poke through the suit's separate body/arm/leg meshes at the limb joints as the limbs swing.
 * Polygon-offset and uniform-scale fixes both failed because the clip is a coverage gap at the
 * rotational seams, not coplanar Z-fighting. Since the suit fully covers the body when all four
 * pieces are worn, the cleanest robust fix is to suppress GC's thermal-padding render for that
 * player. GC exposes {@link RenderPlayerGC#flagThermalOverride} (checked in both render paths)
 * for exactly this kind of override; it is toggled per-render via RenderPlayerEvent.Pre/Post so
 * it only affects players wearing the full suit and is restored immediately after.
 */
public class SpaceSuitRenderHandler {

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new SpaceSuitRenderHandler());
    }

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (wearingFullSpaceSuit(event.entityPlayer)) {
            RenderPlayerGC.flagThermalOverride = true;
        }
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        // Always restore so the override only lasts for this player's render.
        RenderPlayerGC.flagThermalOverride = false;
    }

    private static boolean wearingFullSpaceSuit(EntityPlayer player) {
        if (player == null || player.inventory == null) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            ItemStack stack = player.inventory.armorItemInSlot(i);
            if (stack == null || !(stack.getItem() instanceof Tier1SpaceSuitArmor)) {
                return false;
            }
        }
        return true;
    }
}
