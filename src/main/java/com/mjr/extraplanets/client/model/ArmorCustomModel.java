package com.mjr.extraplanets.client.model;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class ArmorCustomModel extends ModelBiped {

    public ArmorCustomModel() {
        super(1.0F);
    }

    public int color = -1;

    /**
     * Per-entity pose sources, captured in {@code RenderPlayerEvent.Pre} and cleared in
     * {@code Post} (see {@code SpaceSuitRenderHandler}). {@link #setRotationAngles} mirrors the
     * captured main {@link ModelBiped}'s exact limb angles so the suit geometry stays attached to
     * the body in every pose (held item, bow aim, blocking, sword swing, sneak, ride, etc.). When
     * no pose source is registered for the entity being rendered, the model falls back to
     * computing its pose from its own fields.
     *
     * <p>
     * The poses are keyed by the <em>entity being rendered</em> rather than a single process-global
     * so that a {@code Pre} which never gets a matching {@code Post} (e.g. after a render
     * exception) can only ever influence a later re-render of that same entity - never a different
     * entity's render. Entries are weakly held so they are collected once the entity unloads.
     */
    private static final Map<Entity, ModelBiped> POSE_SOURCES = new WeakHashMap<Entity, ModelBiped>();

    /** Records the main model for {@code entity} to mirror for the duration of its render. */
    public static void setPoseSource(Entity entity, ModelBiped source) {
        if (entity != null && source != null) {
            POSE_SOURCES.put(entity, source);
        }
    }

    /** Clears the mirrored model for {@code entity} once its render has finished. */
    public static void clearPoseSource(Entity entity) {
        if (entity != null) {
            POSE_SOURCES.remove(entity);
        }
    }

    private static ModelBiped getPoseSource(Entity entity) {
        return entity == null ? null : POSE_SOURCES.get(entity);
    }

    public abstract void pre();

    public abstract void post();

    public abstract void partHead();

    public abstract void partBody();

    public abstract void partRightArm();

    public abstract void partLeftArm();

    public abstract void partRightLeg();

    public abstract void partLeftLeg();

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
        float headPitch, float scale, Entity entity) {
        final ModelBiped source = getPoseSource(entity);
        if (source != null) {
            // Mirror the exact pose of the player's main model so the suit stays attached to the
            // body in every pose (held item, bow aim, blocking, swing, sneak, ride, etc.).
            copyModelAngles(source.bipedHead, this.bipedHead);
            copyModelAngles(source.bipedHeadwear, this.bipedHeadwear);
            copyModelAngles(source.bipedBody, this.bipedBody);
            copyModelAngles(source.bipedRightArm, this.bipedRightArm);
            copyModelAngles(source.bipedLeftArm, this.bipedLeftArm);
            copyModelAngles(source.bipedRightLeg, this.bipedRightLeg);
            copyModelAngles(source.bipedLeftLeg, this.bipedLeftLeg);

            // State flags used by partXxx()/render() for sneak/ride offsets etc.
            this.isSneak = source.isSneak;
            this.isRiding = source.isRiding;
            this.isChild = source.isChild;
            this.aimedBow = source.aimedBow;
            this.heldItemRight = source.heldItemRight;
            this.heldItemLeft = source.heldItemLeft;
            return;
        }
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
    }

    private static void copyModelAngles(ModelRenderer src, ModelRenderer dst) {
        dst.rotateAngleX = src.rotateAngleX;
        dst.rotateAngleY = src.rotateAngleY;
        dst.rotateAngleZ = src.rotateAngleZ;
        dst.rotationPointX = src.rotationPointX;
        dst.rotationPointY = src.rotationPointY;
        dst.rotationPointZ = src.rotationPointZ;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
        float headPitch, float scale) {
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GL11.glPushMatrix();
        if (this.color != -1) {
            float red = (this.color >> 16 & 255) / 255F;
            float blue = (this.color >> 8 & 255) / 255F;
            float green = (this.color & 255) / 255F;
            GL11.glColor3f(red, blue, green);
        }

        pre();
        // THE primary anti-Z-fighting measure: scale the whole armor 10% larger than the body
        // so armor surfaces never coincide with the skin surfaces.
        GL11.glScalef(1.1F, 1.1F, 1.1F);
        float f6 = 4.0F;
        {// partHead
            GL11.glPushMatrix();
            if (this.isChild) {
                GL11.glScalef(1.5F / f6, 1.5F / f6, 1.5F / f6);
                GL11.glTranslatef(0.0F, 16.0F * scale, 0.0F);
            }
            GL11.glTranslatef(
                this.bipedHead.rotationPointX * scale,
                this.bipedHead.rotationPointY * scale,
                this.bipedHead.rotationPointZ * scale);
            GL11.glRotatef(this.bipedHead.rotateAngleZ * (180F / (float) Math.PI), 0F, 0F, 1F);
            GL11.glRotatef(this.bipedHead.rotateAngleY * (180F / (float) Math.PI), 0F, 1F, 0F);
            GL11.glRotatef(this.bipedHead.rotateAngleX * (180F / (float) Math.PI), 1F, 0F, 0F);
            GL11.glRotatef(180F, 1F, 0F, 0F);
            // Note: 1.12.2 had an EntityArmorStand check here; EntityArmorStand does not exist
            // in 1.7.10, so the extra 180F Y rotation is always applied.
            GL11.glRotatef(180F, 0F, 1F, 0F);
            if (this.isSneak) {
                GL11.glTranslatef(0F, -0.125F, 0F);
                if (this.bipedHead.rotateAngleX < 0.48) {
                    GL11.glRotatef(-this.bipedHead.rotateAngleX * 2, 1F, 0F, 0F);
                } else if (this.bipedHead.rotateAngleX > 0.48) {
                    GL11.glTranslatef(0F, this.bipedHead.rotateAngleX / 10, -this.bipedHead.rotateAngleX / 10);
                }
            }
            partHead();
            GL11.glPopMatrix();
        }

        if (this.isChild) {
            GL11.glPushMatrix();
            GL11.glScalef(1.0F / f6, 1.0F / f6, 1.0F / f6);
            GL11.glTranslatef(0.0F, 24.0F * scale, 0.0F);
        }

        {// partBody
            GL11.glPushMatrix();
            GL11.glTranslatef(
                this.bipedBody.rotationPointX * scale,
                this.bipedBody.rotationPointY * scale,
                this.bipedBody.rotationPointZ * scale);
            GL11.glRotatef(this.bipedBody.rotateAngleZ * (180F / (float) Math.PI), 0F, 0F, 1F);
            GL11.glRotatef(this.bipedBody.rotateAngleY * (180F / (float) Math.PI), 0F, 1F, 0F);
            GL11.glRotatef(this.bipedBody.rotateAngleX * (180F / (float) Math.PI), 1F, 0F, 0F);
            GL11.glRotatef(180F, 1F, 0F, 0F);
            GL11.glRotatef(180F, 0F, 1F, 0F);
            if (this.isSneak) {
                GL11.glTranslatef(0F, -0.20F, -0.1F);
            }
            partBody();
            GL11.glPopMatrix();
        }

        {// partRightArm
            GL11.glPushMatrix();
            GL11.glTranslatef(
                this.bipedRightArm.rotationPointX * scale,
                this.bipedRightArm.rotationPointY * scale,
                this.bipedRightArm.rotationPointZ * scale);
            GL11.glRotatef(this.bipedRightArm.rotateAngleZ * (180F / (float) Math.PI), 0F, 0F, 1F);
            GL11.glRotatef(this.bipedRightArm.rotateAngleY * (180F / (float) Math.PI), 0F, 1F, 0F);
            GL11.glRotatef(this.bipedRightArm.rotateAngleX * (180F / (float) Math.PI), 1F, 0F, 0F);
            GL11.glRotatef(180F, 1F, 0F, 0F);
            GL11.glRotatef(180F, 0F, 1F, 0F);
            if (this.isSneak) {
                GL11.glTranslatef(0.02F, -0.1F, -0.05F + (-0.02F + limbSwingAmount / 10));
            }
            partRightArm();
            GL11.glPopMatrix();
        }

        {// partLeftArm
            GL11.glPushMatrix();
            GL11.glTranslatef(
                this.bipedLeftArm.rotationPointX * scale,
                this.bipedLeftArm.rotationPointY * scale,
                this.bipedLeftArm.rotationPointZ * scale);
            GL11.glRotatef(this.bipedLeftArm.rotateAngleZ * (180F / (float) Math.PI), 0F, 0F, 1F);
            GL11.glRotatef(this.bipedLeftArm.rotateAngleY * (180F / (float) Math.PI), 0F, 1F, 0F);
            GL11.glRotatef(this.bipedLeftArm.rotateAngleX * (180F / (float) Math.PI), 1F, 0F, 0F);
            GL11.glRotatef(180F, 1F, 0F, 0F);
            GL11.glRotatef(180F, 0F, 1F, 0F);
            if (this.isSneak) {
                GL11.glTranslatef(-0.02F, -0.1F, -0.05F + (-0.02F + limbSwingAmount / 10));
            }
            partLeftArm();
            GL11.glPopMatrix();
        }

        {// partRightLeg
            GL11.glPushMatrix();
            GL11.glTranslatef(
                this.bipedRightLeg.rotationPointX * scale,
                this.bipedRightLeg.rotationPointY * scale,
                this.bipedRightLeg.rotationPointZ * scale);
            GL11.glRotatef(-this.bipedRightLeg.rotateAngleZ * (180F / (float) Math.PI), 0F, 0F, 1F);
            GL11.glRotatef(-this.bipedRightLeg.rotateAngleY * (180F / (float) Math.PI), 0F, 1F, 0F);
            GL11.glRotatef(-this.bipedRightLeg.rotateAngleX * (180F / (float) Math.PI), 1F, 0F, 0F);
            GL11.glRotatef(180F, 1F, 0F, 0F);
            GL11.glRotatef(180F, 0F, 1F, 0F);
            if (this.isSneak) {
                GL11.glTranslatef(0F, -0.225F, -0.10F);
            }
            partRightLeg();
            GL11.glPopMatrix();
        }

        {// partLeftLeg
            GL11.glPushMatrix();
            GL11.glTranslatef(
                this.bipedLeftLeg.rotationPointX * scale,
                this.bipedLeftLeg.rotationPointY * scale,
                this.bipedLeftLeg.rotationPointZ * scale);
            GL11.glRotatef(-this.bipedLeftLeg.rotateAngleZ * (180F / (float) Math.PI), 0F, 0F, 1F);
            GL11.glRotatef(-this.bipedLeftLeg.rotateAngleY * (180F / (float) Math.PI), 0F, 1F, 0F);
            GL11.glRotatef(-this.bipedLeftLeg.rotateAngleX * (180F / (float) Math.PI), 1F, 0F, 0F);
            GL11.glRotatef(180F, 1F, 0F, 0F);
            GL11.glRotatef(180F, 0F, 1F, 0F);
            if (this.isSneak) {
                GL11.glTranslatef(0F, -0.225F, -0.10F);
            }
            partLeftLeg();
            GL11.glPopMatrix();
        }

        if (this.isChild) {
            GL11.glPopMatrix();
        }
        post();
        // GL11.glColor3f(1F, 1F, 1F);
        GL11.glPopMatrix();
    }
}
