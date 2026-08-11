package com.mjr.extraplanets.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import com.mjr.extraplanets.Constants;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ArmorSpaceSuitModel extends ArmorCustomModel {

    private static IModelCustom spaceSuitModel;

    // Head (solid helmet) groups
    private static final String[] GROUP_HEAD = new String[] { "HelmetPart1", "HelmetPart2", "HelmetPart4",
        "HelmetPart5", "HelmetPart6", "HelmetPart7", "HelmetPart8", "HelmetPart9", "HelmetPart10", "HelmetPart11",
        "MainPartHelmet", "SpacerAntenna2" };
    // Head oxygen capsules
    private static final String[] GROUP_TANKS = new String[] { "HelmetOxygenCapsule1", "HelmetOxygenCapsule2" };
    // Head glass visor
    private static final String GROUP_HEAD_GLASS = "HelmetPart3";
    // Body pipes
    private static final String[] GROUP_PIPES = new String[] { "BodyPart1", "BodyPart10", "BodyPart11", "BodyPart12",
        "BodyPart13", "BodyPart14", "BodyPart15", "BodyPart16", "BodyPart17", "BodyPart18", "BodyPart3", "BodyPart4",
        "BodyPart5", "BodyPart6", "BodyPart7", "BodyPart8", "BodyPart9" };
    private static final String GROUP_BODY = "BodyPart2";
    private static final String GROUP_BODY_TANK1 = "NitrogenTank";
    private static final String GROUP_BODY_TANK2 = "OxygenTank";
    private static final String GROUP_BODY_TANK3 = "HydrogenTank";
    private static final String GROUP_LEFT_ARM = "LeftHandPart1";
    private static final String GROUP_RIGHT_ARM = "RightHandPart4";
    private static final String[] GROUP_LEFT_ARM_SPRING = new String[] { "LeftHandPart2", "LeftHandPart3",
        "LeftHandPart4" };
    private static final String[] GROUP_RIGHT_ARM_SPRING = new String[] { "RightHandPart1", "RightHandPart2",
        "RightHandPart3" };
    private static final String GROUP_LEFT_LEG = "LeftLegPart1";
    private static final String GROUP_RIGHT_LEG = "RightLegPart3";
    private static final String[] GROUP_LEFT_LEG_PIPES = new String[] { "LeftLegPart2", "LeftLegPart3" };
    private static final String[] GROUP_RIGHT_LEG_PIPES = new String[] { "RightLegPart2", "RightLegPart4" };
    private static final String GROUP_LEFT_BOOT = "left_boot";
    private static final String GROUP_RIGHT_BOOT = "right_boot";

    private final int partType;

    public ArmorSpaceSuitModel(int armorSlot) {
        this.partType = armorSlot;
        updateModel();
    }

    @Override
    public void pre() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH); // was GlStateManager.shadeModel in 1.12.2
        updateModel();
    }

    @Override
    public void post() {
        GL11.glDisable(GL11.GL_BLEND); // restore blend state - critical to not leak blend state
    }

    private void updateModel() {
        if (spaceSuitModel == null) {
            spaceSuitModel = AdvancedModelLoader
                .loadModel(new ResourceLocation(Constants.ASSET_PREFIX, "models/space_suit.obj"));
        }
    }

    @Override
    public void partHead() {
        if (this.partType == 0) { // 0 = helmet
            // The -0.005F Z offset prevents the visor from Z-fighting the head
            GL11.glTranslatef(0F, -1.525F, -0.005F);
            // TODO: Tier0 light-blue texture switch (Tier0SpaceSuitArmor) not ported yet
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_white.png"));
            spaceSuitModel.renderOnly(GROUP_HEAD);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(
                    new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_dark_red.png"));
            spaceSuitModel.renderOnly(GROUP_TANKS);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(
                    new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_blue_textured.png"));
            // Additive blend for the glass visor - doesn't compete in the depth buffer with the frame
            GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            spaceSuitModel.renderPart(GROUP_HEAD_GLASS);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // restore
        }
    }

    @Override
    public void partBody() {
        if (this.partType == 1) { // 1 = chest
            GL11.glScalef(1.0F, 1.0F, 1.0F);
            GL11.glTranslatef(0F, -1.50F, 0F);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_white.png"));
            spaceSuitModel.renderPart(GROUP_BODY);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(
                    new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_textured.png"));
            spaceSuitModel.renderOnly(GROUP_PIPES);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_blue.png"));
            spaceSuitModel.renderPart(GROUP_BODY_TANK1);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(
                    new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_dark_red.png"));
            spaceSuitModel.renderPart(GROUP_BODY_TANK2);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(
                    new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_blue_textured.png"));
            spaceSuitModel.renderPart(GROUP_BODY_TANK3);
            // TODO: Jetpack rendering (JetpackArmorBase) not ported yet - re-enable when jetpacks are ported
        }
    }

    @Override
    public void partRightArm() {
        if (this.partType == 1) { // 1 = chest
            if (this.isSneak) {
                GL11.glScalef(1.1F, 1F, 1.3F);
            } else {
                GL11.glScalef(1F, 1F, 1.0F);
            }
            GL11.glTranslatef(-0.3525F, -1.375F, 0F);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_white.png"));
            spaceSuitModel.renderPart(GROUP_RIGHT_ARM);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(
                    new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_textured.png"));
            spaceSuitModel.renderOnly(GROUP_RIGHT_ARM_SPRING);
        }
    }

    @Override
    public void partLeftArm() {
        if (this.partType == 1) { // 1 = chest
            if (this.isSneak) {
                GL11.glScalef(1.1F, 1F, 1.3F);
            } else {
                GL11.glScalef(1F, 1F, 1.0F);
            }
            GL11.glTranslatef(0.3525F, -1.375F, 0F);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_white.png"));
            spaceSuitModel.renderPart(GROUP_LEFT_ARM);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(
                    new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_textured.png"));
            spaceSuitModel.renderOnly(GROUP_LEFT_ARM_SPRING);
        }
    }

    @Override
    public void partRightLeg() {
        if (this.partType == 2) { // 2 = legs
            GL11.glScalef(1F, 1F, 1.5F);
            if (this.isSneak) {
                GL11.glTranslatef(-0.100F, -0.7F, 0.04F);
            } else {
                GL11.glTranslatef(-0.100F, -0.7F, -0.01F);
            }
            // NOTE: The OBJ geometry is mirrored vs. the player's sides - the mesh named "Left*"
            // sits at negative X and is used for the player's RIGHT leg (this matches 1.12.2).
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_white.png"));
            spaceSuitModel.renderPart(GROUP_LEFT_LEG);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(
                    new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_textured.png"));
            spaceSuitModel.renderOnly(GROUP_LEFT_LEG_PIPES);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(
                    new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_dark_grey.png"));
            spaceSuitModel.renderPart(GROUP_LEFT_BOOT);
        }
    }

    @Override
    public void partLeftLeg() {
        if (this.partType == 2) { // 2 = legs
            GL11.glScalef(1F, 1F, 1.5F);
            if (this.isSneak) {
                GL11.glTranslatef(0.100F, -0.7F, 0.04F);
            } else {
                GL11.glTranslatef(0.100F, -0.7F, -0.01F);
            }
            // NOTE: The OBJ geometry is mirrored vs. the player's sides - the mesh named "Right*"
            // sits at positive X and is used for the player's LEFT leg (this matches 1.12.2).
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_white.png"));
            spaceSuitModel.renderPart(GROUP_RIGHT_LEG);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(
                    new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_textured.png"));
            spaceSuitModel.renderOnly(GROUP_RIGHT_LEG_PIPES);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(
                    new ResourceLocation(Constants.TEXTURE_PREFIX + "textures/model/blank_rocket_dark_grey.png"));
            spaceSuitModel.renderPart(GROUP_RIGHT_BOOT);
        }
    }
}
