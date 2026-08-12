package com.mjr.extraplanets.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;

import com.mjr.extraplanets.armor.Tier1SpaceSuitArmor;
import com.mjr.extraplanets.client.model.ArmorCustomModel;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import micdoodle8.mods.galacticraft.core.proxy.ClientProxyCore;
import micdoodle8.mods.galacticraft.core.wrappers.PlayerGearData;

/**
 * Hides Galacticraft gear that would otherwise poke through the ExtraPlanets space suit.
 *
 * <p>
 * GC renders its gear through a substituted player model and only auto-hides it based on the
 * gear item itself (a translucency enchant or the global {@code disableGearRender} config) - it
 * never checks the worn ExtraPlanets suit, so the gear stays visible and clips through (or is
 * larger than) the suit's custom OBJ geometry. GC's direct render path honors the per-player
 * {@code render*} flags on {@link PlayerGearData}, so we switch off the relevant flag(s) while the
 * corresponding EP suit piece is worn. Each flag is captured in {@code RenderPlayerEvent.Pre} and
 * restored in {@code Post}, so the suppression only affects the suit-wearing player for the
 * duration of that single render and self-corrects when the suit comes off. Hiding a render never
 * affects the underlying gameplay (oxygen supply, thermal resistance, etc.) - the gear simply
 * isn't drawn.
 *
 * <ul>
 * <li>Oxygen Mask ({@code renderMask}) - hidden while the EP <b>helmet</b> is worn.</li>
 * <li>Oxygen Gear + both Oxygen Tanks ({@code renderGear}, {@code renderLeftTank},
 * {@code renderRightTank}) - hidden while the EP <b>chestplate</b> is worn.</li>
 * <li>Thermal Padding ({@code renderThermalPadding[]}) - each padding piece is hidden while its
 * own EP suit piece is worn (thermal helm &harr; helmet, chest &harr; chestplate,
 * legs &harr; leggings, boots &harr; boots).</li>
 * <li>Frequency Module - intentionally left rendering (cosmetic decision under review).</li>
 * </ul>
 *
 * <p>
 * NOTE: the RenderPlayerAPI render path ({@code ModelPlayerBaseGC}) ignores the {@code render*}
 * flags - for thermal padding it only honours the global {@code RenderPlayerGC.flagThermalOverride}.
 * The per-piece suppression here therefore applies only in the direct (non-RenderPlayerAPI) path.
 * If RenderPlayerAPI is ever installed, either a PlayerAPI model/render layer, or re-introducing
 * {@code flagThermalOverride} (at the cost of reverting to all-or-nothing thermal hides), would be
 * required.
 */
public class SpaceSuitRenderHandler {

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new SpaceSuitRenderHandler());
    }

    /** Gear render flags for the player currently being rendered (captured in Pre, restored in Post). */
    private boolean prevRenderMask;
    private boolean prevRenderGear;
    private boolean prevRenderLeftTank;
    private boolean prevRenderRightTank;
    private final boolean[] prevRenderThermal = new boolean[4];

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.entityPlayer;
        // Capture the player's main model so the suit armor model can mirror its exact pose.
        ArmorCustomModel.poseSource = event.renderer.modelBipedMain;
        PlayerGearData gearData = gearDataFor(player);
        if (gearData == null) {
            return;
        }

        // Capture the current render flags so Post can restore them exactly.
        this.prevRenderMask = gearData.getRenderMask();
        this.prevRenderGear = gearData.getRenderGear();
        this.prevRenderLeftTank = gearData.getRenderLeftTank();
        this.prevRenderRightTank = gearData.getRenderRightTank();
        for (int slot = 0; slot < 4; slot++) {
            this.prevRenderThermal[slot] = gearData.getRenderThermalPadding(slot);
        }

        // Mask: face-mounted, covered by the helmet (1.7.10 armor slot 3 = HEAD).
        if (wearingSpaceSuitPart(player, 3)) {
            gearData.setRenderMask(false);
        }

        // Gear + tanks: torso/back harness, covered by the chestplate (armor slot 2 = CHEST).
        if (wearingSpaceSuitPart(player, 2)) {
            gearData.setRenderGear(false);
            gearData.setRenderLeftTank(false);
            gearData.setRenderRightTank(false);
        }

        // Thermal padding: one padding piece per EP suit piece. GC's thermal slot index (0=helm,
        // 1=chest, 2=legs, 3=boots) differs from the 1.7.10 armor-slot index (0=feet, 1=legs,
        // 2=chest, 3=head), so map each explicitly to its covering suit piece.
        if (wearingSpaceSuitPart(player, 3)) { // EP helmet covers thermal helm (slot 0)
            gearData.setRenderThermalPadding(0, false);
        }
        if (wearingSpaceSuitPart(player, 2)) { // EP chestplate covers thermal chest (slot 1)
            gearData.setRenderThermalPadding(1, false);
        }
        if (wearingSpaceSuitPart(player, 1)) { // EP leggings cover thermal legs (slot 2)
            gearData.setRenderThermalPadding(2, false);
        }
        if (wearingSpaceSuitPart(player, 0)) { // EP boots cover thermal boots (slot 3)
            gearData.setRenderThermalPadding(3, false);
        }
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        // Pose source is only valid for the duration of this player's render.
        ArmorCustomModel.poseSource = null;
        PlayerGearData gearData = gearDataFor(event.entityPlayer);
        if (gearData == null) {
            return;
        }
        gearData.setRenderMask(this.prevRenderMask);
        gearData.setRenderGear(this.prevRenderGear);
        gearData.setRenderLeftTank(this.prevRenderLeftTank);
        gearData.setRenderRightTank(this.prevRenderRightTank);
        for (int slot = 0; slot < 4; slot++) {
            gearData.setRenderThermalPadding(slot, this.prevRenderThermal[slot]);
        }
    }

    private static PlayerGearData gearDataFor(EntityPlayer player) {
        if (player == null) {
            return null;
        }
        return ClientProxyCore.playerItemData.get(player.getCommandSenderName());
    }

    /** True if the given 1.7.10 armor slot (0=feet, 1=legs, 2=chest, 3=head) holds an EP suit piece. */
    private static boolean wearingSpaceSuitPart(EntityPlayer player, int armorSlot) {
        if (player == null || player.inventory == null) {
            return false;
        }
        ItemStack stack = player.inventory.armorItemInSlot(armorSlot);
        return stack != null && stack.getItem() instanceof Tier1SpaceSuitArmor;
    }
}
