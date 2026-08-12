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
            // FLAT-COLOR SUBSTITUTE (no-UV model) - if the Blender model is ever re-exported with
            // UV coordinates, restore the original texture bind: blank_rocket_dark_red.png
            renderFlatOnly(GROUP_TANKS, 0.714F, 0.294F, 0.271F); // was blank_rocket_dark_red.png
            // FLAT TRANSLUCENT GLASS (no-UV model + additive-blend fix): the visor used to render
            // with GL_ONE, GL_ONE additive blending, which let the head glow through as a bright
            // overlay. It is now a normal alpha-blended tinted pane. If the Blender model is ever
            // re-exported WITH UV coordinates, restore the original textured+additive render:
            // bindTexture(TEXTURE_PREFIX + "textures/model/blank_rocket_blue_textured.png")
            // GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            renderFlatGlass(GROUP_HEAD_GLASS, 0.451F, 0.659F, 0.851F, 0.6F); // was blank_rocket_blue_textured.png
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
            // FLAT-COLOR SUBSTITUTE (see renderFlatPart()/renderFlatOnly()): space_suit.obj has no UVs,
            // so these parts can't be textured in 1.7.10 and used to render as shimmering fuzz.
            // Colors are the average pixel color of each part's original texture (see helper javadoc).
            // IF THE BLENDER MODEL IS EVER RE-EXPORTED WITH UV COORDINATES, replace each flat-color
            // call below with its original texture bind (texture filename noted on each line).
            renderFlatOnly(GROUP_PIPES, 0.180F, 0.180F, 0.180F); // was blank_rocket_textured.png
            renderFlatPart(GROUP_BODY_TANK1, 0.043F, 0.043F, 0.996F); // was blank_rocket_blue.png
            renderFlatPart(GROUP_BODY_TANK2, 0.714F, 0.294F, 0.271F); // was blank_rocket_dark_red.png
            renderFlatPart(GROUP_BODY_TANK3, 0.451F, 0.659F, 0.851F); // was blank_rocket_blue_textured.png
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
            // FLAT-COLOR SUBSTITUTE (no-UV model) - if the Blender model is ever re-exported with
            // UV coordinates, restore the original texture bind: blank_rocket_textured.png
            renderFlatOnly(GROUP_RIGHT_ARM_SPRING, 0.180F, 0.180F, 0.180F);
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
            // FLAT-COLOR SUBSTITUTE (no-UV model) - if the Blender model is ever re-exported with
            // UV coordinates, restore the original texture bind: blank_rocket_textured.png
            renderFlatOnly(GROUP_LEFT_ARM_SPRING, 0.180F, 0.180F, 0.180F);
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
            // FLAT-COLOR SUBSTITUTE (no-UV model) - if the Blender model is ever re-exported with
            // UV coordinates, restore the original texture bind: blank_rocket_textured.png
            renderFlatOnly(GROUP_LEFT_LEG_PIPES, 0.180F, 0.180F, 0.180F); // was blank_rocket_textured.png
            // FLAT-COLOR SUBSTITUTE (no-UV model) - if the Blender model is ever re-exported with
            // UV coordinates, restore the original texture bind: blank_rocket_dark_grey.png
            renderFlatPart(GROUP_LEFT_BOOT, 0.522F, 0.522F, 0.522F); // was blank_rocket_dark_grey.png
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
            // FLAT-COLOR SUBSTITUTE (no-UV model) - if the Blender model is ever re-exported with
            // UV coordinates, restore the original texture bind: blank_rocket_textured.png
            renderFlatOnly(GROUP_RIGHT_LEG_PIPES, 0.180F, 0.180F, 0.180F); // was blank_rocket_textured.png
            // FLAT-COLOR SUBSTITUTE (no-UV model) - if the Blender model is ever re-exported with
            // UV coordinates, restore the original texture bind: blank_rocket_dark_grey.png
            renderFlatPart(GROUP_RIGHT_BOOT, 0.522F, 0.522F, 0.522F); // was blank_rocket_dark_grey.png
        }
    }

    /**
     * FLAT-COLOR SUBSTITUTE - renders an OBJ group as a solid untextured color.
     *
     * <p>
     * WHY: {@code space_suit.obj} was exported without UV coordinates (no {@code vt} entries; it
     * is the only OBJ in the mod without them). The 1.7.10 {@code WavefrontObject} therefore leaves
     * GL texture-coordinate state undefined for these faces, so the parts sample their
     * {@code blank_rocket_*} textures at uncontrolled coordinates and render as shimmering "fuzz"
     * that shifts with camera angle. Drawing them as flat colors sidesteps texture sampling entirely
     * and is stable from every angle.
     *
     * <p>
     * IF THE BLENDER MODEL IS EVER RE-EXPORTED WITH UV COORDINATES: replace each
     * {@code renderFlatPart/renderFlatOnly} call with the original texture bind noted in its
     * comment (e.g. {@code bindTexture(TEXTURE_PREFIX + "textures/model/blank_rocket_textured.png")}).
     * The RGB values passed here are the average pixel colors of the original textures, so they match
     * the intended look until then.
     */
    private void renderFlatPart(String group, float r, float g, float b) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor3f(r, g, b);
        spaceSuitModel.renderPart(group);
        GL11.glColor3f(1F, 1F, 1F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void renderFlatOnly(String[] groups, float r, float g, float b) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor3f(r, g, b);
        spaceSuitModel.renderOnly(groups);
        GL11.glColor3f(1F, 1F, 1F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * FLAT-COLOR SUBSTITUTE for {@code GROUP_HEAD_GLASS} - renders the helmet visor as a flat
     * translucent tinted pane using standard alpha blending.
     *
     * <p>
     * The visor previously rendered with {@code GL_ONE, GL_ONE} additive blending over a no-UV
     * texture, which produced a glowing noisy overlay (the head showed through as a bright region
     * instead of being tinted). This replaces it with a deterministic alpha-blended tint.
     *
     * <p>
     * {@code a} is opacity: 0 = invisible, 1 = opaque, 0.6 reads as tinted glass. If the Blender
     * model is ever re-exported WITH UV coordinates, restore the original textured additive render
     * (see the comment in {@code partHead()}).
     */
    private void renderFlatGlass(String group, float r, float g, float b, float a) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(r, g, b, a);
        spaceSuitModel.renderPart(group);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
