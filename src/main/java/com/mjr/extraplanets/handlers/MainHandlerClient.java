package com.mjr.extraplanets.handlers;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiOpenEvent;

import org.lwjgl.opengl.GL11;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.client.gui.screen.CustomCelestialSelection;
import com.mjr.extraplanets.planets.ExtraPlanets_Planets;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import micdoodle8.mods.galacticraft.api.event.client.CelestialBodyRenderEvent;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection;
import micdoodle8.mods.galacticraft.core.tick.KeyHandlerClient;

/**
 * Client-only event handlers that must never be registered on a dedicated server's event bus.
 *
 * <p>
 * These were previously part of {@link MainHandler}, which is registered on the common
 * {@code MinecraftForge.EVENT_BUS} in {@code ExtraPlanets.preInit} on <em>both</em> sides. Splitting
 * them out lets them be registered only on the client (in {@code ClientProxy.postInit}), so these
 * entity-view fog / GUI / celestial-rendering handlers and the client Jupiter ambiance tick can
 * never be registered on - or load client classes from - a vanilla server.
 */
@SideOnly(Side.CLIENT)
public class MainHandlerClient {

    /** Cached RNG for the client-only Jupiter fake lighting, allocated once instead of every tick. */
    private final Random rand = new Random();

    @SubscribeEvent
    public void onPlayer(PlayerTickEvent event) {
        // The fake Jupiter lightning is purely client-side ambiance; never spawn real server bolts.
        if (event.side == Side.SERVER) return;
        if (Config.jupiterLightingClient && event.player.worldObj.provider.dimensionId == Config.jupiterID) {
            int addX = this.rand.nextInt(64);
            int addZ = this.rand.nextInt(64);
            if (this.rand.nextInt(2) == 1) addX = -addX;
            if (this.rand.nextInt(2) == 1) addZ = -addZ;
            if (addX <= 3) addX = 5;
            if (addZ <= 3) addZ = 5;
            if (this.rand.nextInt(100) == 10) event.player.worldObj.addWeatherEffect(
                new EntityLightningBolt(
                    event.player.worldObj,
                    event.player.posX + addX,
                    event.player.worldObj
                        .getHeightValue((int) event.player.posX + addX, (int) event.player.posZ + addZ),
                    event.player.posZ + addZ));
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onGuiOpenEvent(GuiOpenEvent event) {
        if (Config.useCustomCelestialSelection) {
            if (((event.gui instanceof GuiCelestialSelection))) {
                if (GameSettings.isKeyDown(KeyHandlerClient.galaxyMap)) {
                    event.gui = new CustomCelestialSelection(
                        GuiCelestialSelection.MapMode.VIEW,
                        ((GuiCelestialSelection) event.gui).possibleBodies);
                } else {
                    event.gui = new CustomCelestialSelection(
                        GuiCelestialSelection.MapMode.TRAVEL,
                        ((GuiCelestialSelection) event.gui).possibleBodies);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onRenderFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (Config.customFog) {
            if (event.entity.worldObj.provider.dimensionId == Config.jupiterID) {
                event.density = 0.02f;
                GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
                event.setCanceled(true);
            } else if (event.entity.worldObj.provider.dimensionId == Config.uranusID) {
                event.density = 0.008f;
                GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
                event.setCanceled(true);
            } else if (event.entity.worldObj.provider.dimensionId == Config.saturnID) {
                event.density = 0.015f;
                GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
                event.setCanceled(true);
            } else if (event.entity.worldObj.provider.dimensionId == Config.neptuneID) {
                event.density = 0.01f;
                GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
                event.setCanceled(true);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderPlanetPost(CelestialBodyRenderEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen instanceof GuiCelestialSelection) {
            GuiCelestialSelection screen = ((GuiCelestialSelection) mc.currentScreen);
            if (event.celestialBody == ExtraPlanets_Planets.saturn) {
                mc.renderEngine.bindTexture(
                    new ResourceLocation(
                        GalacticraftCore.ASSET_PREFIX,
                        "textures/gui/celestialbodies/saturnRings.png"));
                float size = GuiCelestialSelection.getWidthForCelestialBodyStatic(event.celestialBody) / 6.0F;
                screen.drawTexturedModalRect(
                    -7.5F * size,
                    -1.75F * size,
                    15.0F * size,
                    3.5F * size,
                    0,
                    0,
                    30,
                    7,
                    false,
                    false,
                    32,
                    32);
            } else if (event.celestialBody == ExtraPlanets_Planets.uranus) {
                mc.renderEngine.bindTexture(
                    new ResourceLocation(
                        GalacticraftCore.ASSET_PREFIX,
                        "textures/gui/celestialbodies/uranusRings.png"));
                float size = GuiCelestialSelection.getWidthForCelestialBodyStatic(event.celestialBody) / 6.0F;
                screen.drawTexturedModalRect(
                    -1.75F * size,
                    -7.0F * size,
                    3.5F * size,
                    14.0F * size,
                    0,
                    0,
                    7,
                    28,
                    false,
                    false,
                    32,
                    32);
            }
        }
    }
}