package com.mjr.extraplanets.client.gui.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.opengl.GL11;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.api.world.ISolarRadiationWorld;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.util.ClientUtil;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;

/**
 * Radiation level indicator HUD overlay.
 * <p>
 * Renders a vertical bar + text showing the player's accumulated radiation level.
 * Positioned on the opposite side from the GC oxygen/thermal indicator.
 * <p>
 * Triggers:
 * <ul>
 * <li>Player is in a Galacticraft world provider with radiation</li>
 * <li>Config.radiation is enabled</li>
 * <li>Player has accumulated radiation > 0</li>
 * </ul>
 */
@SideOnly(Side.CLIENT)
public class OverlayRadiation {

    private static final Minecraft mc = Minecraft.getMinecraft();

    /** Client-side radiation level, updated by C_UPDATE_RADIATION packet. */
    public static double clientRadiationLevel = 0;

    private static int getWorldRadiation() {
        if (mc.thePlayer.worldObj.provider instanceof ISolarRadiationWorld) {
            return ((ISolarRadiationWorld) mc.thePlayer.worldObj.provider).getSolarRadiationLevel();
        }
        if (mc.thePlayer.worldObj.provider instanceof micdoodle8.mods.galacticraft.core.dimension.WorldProviderMoon) {
            return Config.moonRadiationAmount;
        }
        if (mc.thePlayer.worldObj.provider instanceof micdoodle8.mods.galacticraft.planets.mars.dimension.WorldProviderMars) {
            return Config.marsRadiationAmount;
        }
        if (mc.thePlayer.worldObj.provider instanceof micdoodle8.mods.galacticraft.planets.asteroids.dimension.WorldProviderAsteroids) {
            return Config.asteroidsRadiationAmount;
        }
        if (mc.thePlayer.worldObj.provider instanceof micdoodle8.mods.galacticraft.core.dimension.WorldProviderSpaceStation) {
            return Config.spaceStationRadiationAmount;
        }
        return 0;
    }

    public static void render() {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        if (!Config.radiation) {
            return;
        }
        if (!(mc.thePlayer.worldObj.provider instanceof IGalacticraftWorldProvider)) {
            return;
        }
        if (mc.gameSettings.showDebugInfo) {
            return;
        }

        int worldRadiation = getWorldRadiation();

        double accumulated = clientRadiationLevel;

        // Only show when we have accumulated radiation to display
        if (accumulated <= 0) {
            return;
        }

        // Position: on the same side as the oxygen indicator (top-right by default)
        // oxygenIndicatorLeft defaults to false (oxygen is on the right)
        // When true, oxygen moves to the left and radiation follows
        boolean right = !ConfigManagerCore.oxygenIndicatorLeft;
        boolean top = !ConfigManagerCore.oxygenIndicatorBottom;

        final ScaledResolution res = ClientUtil.getScaledRes(mc, mc.displayWidth, mc.displayHeight);
        final int screenWidth = res.getScaledWidth();
        final int screenHeight = res.getScaledHeight();

        mc.entityRenderer.setupOverlayRendering();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Bar dimensions
        int barWidth = 12;
        int barHeight = 46;
        int barX;
        int barY;

        if (right) {
            barX = screenWidth - 50;
        } else {
            barX = 10;
        }

        if (top) {
            barY = 10 + 50;
        } else {
            barY = screenHeight - 107;
        }

        // Draw background frame (dark)
        GuiScreen.drawRect(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, 0xFF333333);
        GuiScreen.drawRect(barX, barY, barX + barWidth, barY + barHeight, 0xFF111111);

        // Draw fill bar based on accumulated radiation
        int fillHeight = (int) ((accumulated / 100.0) * barHeight);
        fillHeight = Math.min(fillHeight, barHeight);

        // Color: green -> yellow -> red as radiation increases
        int fillColor;
        if (accumulated < 33) {
            fillColor = 0xFF00FF00;
        } else if (accumulated < 66) {
            fillColor = 0xFFFFAA00;
        } else {
            fillColor = 0xFFFF0000;
        }

        int fillY = barY + barHeight - fillHeight;
        GuiScreen.drawRect(barX + 1, fillY, barX + barWidth - 1, fillY + fillHeight, fillColor);

        // Determine warning label based on accumulated radiation (not world level)
        String label;
        int labelColor;
        if (accumulated >= 80) {
            label = "\u00a7cCritical";
            labelColor = 0xFFFF0A0A;
        } else if (accumulated >= 50) {
            label = "\u00a76Warning";
            labelColor = 0xFFFF960A;
        } else if (accumulated >= 20) {
            label = "\u00a7eExposed";
            labelColor = 0xFFFFFF0A;
        } else {
            label = "\u00a7aExposed";
            labelColor = 0xFF0AFF0A;
        }

        // Position label text next to the bar
        int labelX = right ? barX - mc.fontRenderer.getStringWidth(label) - 5 : barX + barWidth + 5;
        int labelY = barY + 12;
        mc.fontRenderer.drawString(label, labelX, labelY, labelColor);

        // Draw accumulated percentage
        String pct = String.format("%.0f%%", accumulated);
        int pctX = right ? barX - mc.fontRenderer.getStringWidth(pct) - 5 : barX + barWidth + 5;
        mc.fontRenderer.drawString(pct, pctX, labelY + 12, 0xFFFFFFFF);

        // Draw world radiation level
        String worldStr = "\u00a77Rad: " + worldRadiation + "%";
        int worldX = right ? barX - mc.fontRenderer.getStringWidth(worldStr) - 5 : barX + barWidth + 5;
        mc.fontRenderer.drawString(worldStr, worldX, labelY + 24, 0xFFB4B4B4);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
