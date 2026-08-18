package com.mjr.extraplanets.armor;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import com.mjr.extraplanets.Constants;
import com.mjr.extraplanets.ExtraPlanets;
import com.mjr.extraplanets.api.item.IPressureSuit;
import com.mjr.extraplanets.api.item.IRadiationSuit;
import com.mjr.extraplanets.client.model.ArmorSpaceSuitModel;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import micdoodle8.mods.galacticraft.api.item.IArmorGravity;
import micdoodle8.mods.galacticraft.api.item.IBreathableArmor;
import micdoodle8.mods.galacticraft.core.util.EnumColor;

public class Tier1SpaceSuitArmor extends ItemArmor
    implements IPressureSuit, IRadiationSuit, IArmorGravity, IBreathableArmor {

    /**
     * Per-entity armor models, keyed by the living entity wearing the suit (weakly held so they are
     * collected once the entity unloads). Each entity owns its own set of 4 models (one per armor
     * slot), so the mutable {@code isSneak}/{@code isRiding}/{@code isChild}/{@code heldItem} and
     * pose state on an {@link ArmorSpaceSuitModel} is never shared across players.
     */
    private static final Map<EntityLivingBase, ArmorSpaceSuitModel[]> entityModels =
        new WeakHashMap<EntityLivingBase, ArmorSpaceSuitModel[]>();
    public String name;

    public Tier1SpaceSuitArmor(String name, ArmorMaterial material, int placement) {
        super(material, 0, placement);
        this.setCreativeTab(ExtraPlanets.ArmorTab); // ArmorTab, not ItemsTab
        if (placement == 0) {
            this.setTextureName(Constants.TEXTURE_PREFIX + name + "_helmet");
        } else if (placement == 1) {
            this.setTextureName(Constants.TEXTURE_PREFIX + name + "_chest");
        } else if (placement == 2) {
            this.setTextureName(Constants.TEXTURE_PREFIX + name + "_legings");
        } else if (placement == 3) {
            this.setTextureName(Constants.TEXTURE_PREFIX + name + "_boots");
        }
        this.name = name;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
        if (stack.getItem() == ExtraPlanets_Armor.tier1SpaceSuitHelmet
            || stack.getItem() == ExtraPlanets_Armor.tier1SpaceSuitChest
            || stack.getItem() == ExtraPlanets_Armor.tier1SpaceSuitBoots) {
            return Constants.TEXTURE_PREFIX + "textures/model/armor/tier1_space_suit_layer_1.png";
        } else if (stack.getItem() == ExtraPlanets_Armor.tier1SpaceSuitLegings) {
            return Constants.TEXTURE_PREFIX + "textures/model/armor/tier1_space_suit_layer_2.png";
        }
        // Never return null: RenderBiped binds whatever this returns, and a null can NPE / hit the
        // missing-texture path. This branch is effectively unreachable for the four suit pieces
        // (they are all matched above), so fall back to the correct layer for the render slot
        // (2 = leggings use layer 2; every other slot uses layer 1).
        return slot == 2
            ? Constants.TEXTURE_PREFIX + "textures/model/armor/tier1_space_suit_layer_2.png"
            : Constants.TEXTURE_PREFIX + "textures/model/armor/tier1_space_suit_layer_1.png";
    }

    @Override
    public int getArmorTier() {
        return 1;
    }

    @Override
    public int gravityOverrideIfLow(EntityPlayer p) {
        // TODO: Gravity boots variant (5th piece) not ported yet - check
        // p.inventory.armorInventory[i] for tier1SpaceSuitGravityBoots and return 55 when worn.
        return 0;
    }

    @Override
    public int gravityOverrideIfHigh(EntityPlayer p) {
        // TODO: Gravity boots variant (5th piece) not ported yet - check
        // p.inventory.armorInventory[i] for tier1SpaceSuitGravityBoots and return 75 when worn.
        return 0;
    }

    @Override
    public boolean handleGearType(IBreathableArmor.EnumGearType gearType) {
        // Which GC oxygen component this suit piece replaces. Each piece only declares the
        // component(s) it actually carries, so a single stray piece cannot satisfy the whole
        // oxygen setup (and cannot replace the mask/gear/tanks on its own).
        // ItemArmor.armorType: 0 = helmet, 1 = chest, 2 = legs, 3 = boots.
        switch (gearType) {
            case HELMET:
                // The helmet replaces the oxygen mask.
                return this.armorType == 0;
            case GEAR:
            case TANK1:
            case TANK2:
                // The chestpiece carries the oxygen harness and the integrated tanks.
                return this.armorType == 1;
            default:
                return false;
        }
    }

    @Override
    public boolean canBreathe(ItemStack helmetInSlot, EntityPlayer playerWearing, IBreathableArmor.EnumGearType type) {
        // The suit is a single, sealed pressure garment. Breathing is only possible while the
        // full four-piece set is worn; a lone helmet (or any single piece) has no sealed body
        // to supply air to.
        return isFullSuitWorn(playerWearing);
    }

    private static boolean isFullSuitWorn(EntityPlayer player) {
        // 1.7.10 armor slots: 0 = boots, 1 = legs, 2 = chest, 3 = head.
        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = player.inventory.armorItemInSlot(slot);
            if (stack == null || !(stack.getItem() instanceof Tier1SpaceSuitArmor)) {
                return false;
            }
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4Boolean) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            list.add(EnumColor.AQUA + StatCollector.translateToLocal("space.suit.information"));
            list.add(EnumColor.AQUA + StatCollector.translateToLocal("space.suit.information.2"));
            list.add(EnumColor.YELLOW + StatCollector.translateToLocal("space.suit.information.extra"));
            list.add(EnumColor.YELLOW + StatCollector.translateToLocal("space.suit.information.extra.2"));
            list.add(EnumColor.AQUA + StatCollector.translateToLocal("space.suit.information.extra.3"));
            list.add(EnumColor.AQUA + StatCollector.translateToLocal("space.suit.information.extra.4"));
        } else {
            list.add(
                EnumColor.YELLOW + StatCollector.translateToLocalFormatted(
                    "item_desc.spacesuit.shift.name",
                    GameSettings.getKeyDisplayString(
                        FMLClientHandler.instance()
                            .getClient().gameSettings.keyBindSneak.getKeyCode())));
        }
        super.addInformation(itemStack, player, list, par4Boolean);
    }

    public static ModelBiped fillingArmorModel(ModelBiped model, EntityLivingBase entityLiving) {
        if (model == null) {
            return model;
        }
        model.bipedHead.showModel = model.bipedHeadwear.showModel = model.bipedBody.showModel = model.bipedRightArm.showModel = model.bipedLeftArm.showModel = model.bipedRightLeg.showModel = model.bipedLeftLeg.showModel = false;
        model.isSneak = entityLiving.isSneaking();
        model.isRiding = entityLiving.isRiding();
        model.isChild = entityLiving.isChild();

        // Propagate the held-item pose so the suit arms track the player's skin arms.
        // ModelBiped.setRotationAngles reads heldItemRight to bend the arm forward when holding an
        // item; without it the suit arm stays at rest while the real arm (driven by the main player
        // model) bends, so the skin arm protrudes through the suit. 1.7.10 has no offhand slot.
        ItemStack heldItem = entityLiving.getHeldItem();
        model.heldItemRight = heldItem != null ? 1 : 0;
        model.heldItemLeft = 0;
        return model;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int armorSlot) {
        ArmorSpaceSuitModel[] slots = entityModels.get(entityLiving);
        if (slots == null) {
            slots = new ArmorSpaceSuitModel[4];
            entityModels.put(entityLiving, slots);
        }
        if (slots[armorSlot] == null) {
            slots[armorSlot] = new ArmorSpaceSuitModel(armorSlot);
        }
        ModelBiped armorModel = slots[armorSlot];
        if (itemStack.getItem() instanceof Tier1SpaceSuitArmor) {
            armorModel = fillingArmorModel(armorModel, entityLiving);
        }
        return armorModel;
    }
}
