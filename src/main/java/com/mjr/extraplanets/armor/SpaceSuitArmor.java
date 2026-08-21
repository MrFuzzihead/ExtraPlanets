package com.mjr.extraplanets.armor;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import com.mjr.extraplanets.Constants;
import com.mjr.extraplanets.ExtraPlanets;
import com.mjr.extraplanets.api.item.IPressureSuit;
import com.mjr.extraplanets.api.item.IRadiationSuit;
import com.mjr.extraplanets.armor.bases.ElectricArmorBase;
import com.mjr.extraplanets.client.model.ArmorSpaceSuitModel;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import micdoodle8.mods.galacticraft.api.item.IArmorGravity;
import micdoodle8.mods.galacticraft.api.item.IBreathableArmor;
import micdoodle8.mods.galacticraft.api.item.IItemElectricBase;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import micdoodle8.mods.galacticraft.core.util.OxygenUtil;

/**
 * Single-player space suit powered by electricity (gJ / RF / EU / J). Extends
 * {@link ElectricArmorBase} so its durability is backed by stored charge instead of
 * fixed damage values, and it can be recharged through GC machines, IC2, CoFH/RF, or
 * Mekanism energy systems.
 *
 * <p>
 * Provides pressure, radiation, breathing, and gravity-override protection when the
 * full 4-piece set is worn. Gravity boots are a separate item slot that adds gravity
 * compensation on low/high-G worlds.
 */
public class SpaceSuitArmor extends ElectricArmorBase
    implements IPressureSuit, IRadiationSuit, IArmorGravity, IBreathableArmor {

    private static final Map<EntityLivingBase, ArmorSpaceSuitModel[]> entityModels = new WeakHashMap<EntityLivingBase, ArmorSpaceSuitModel[]>();

    public String name;

    public SpaceSuitArmor(String name, ArmorMaterial material, int placement) {
        super(material, 0, placement);
        this.setCreativeTab(ExtraPlanets.ArmorTab);
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

    // ---------------------------------------------------------------------------
    // Electricity capacity
    // ---------------------------------------------------------------------------

    @Override
    public float getMaxElectricityStored(ItemStack theItem) {
        return 50000; // 10000 * 5 — matches upstream Tier1 capacity
    }

    // ---------------------------------------------------------------------------
    // Texture
    // ---------------------------------------------------------------------------

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
        if (stack.getItem() == ExtraPlanets_Armor.spaceSuitHelmet
            || stack.getItem() == ExtraPlanets_Armor.spaceSuitChest
            || stack.getItem() == ExtraPlanets_Armor.spaceSuitBoots
            || stack.getItem() == ExtraPlanets_Armor.spaceSuitGravityBoots) {
            return Constants.TEXTURE_PREFIX + "textures/model/armor/space_suit_layer_1.png";
        } else if (stack.getItem() == ExtraPlanets_Armor.spaceSuitLegings) {
            return Constants.TEXTURE_PREFIX + "textures/model/armor/space_suit_layer_2.png";
        }
        return slot == 2 ? Constants.TEXTURE_PREFIX + "textures/model/armor/space_suit_layer_2.png"
            : Constants.TEXTURE_PREFIX + "textures/model/armor/space_suit_layer_1.png";
    }

    // ---------------------------------------------------------------------------
    // Radiation / pressure tier
    // ---------------------------------------------------------------------------

    @Override
    public int getArmorTier() {
        return 1;
    }

    // ---------------------------------------------------------------------------
    // IArmorGravity — gravity boots check
    // ---------------------------------------------------------------------------

    @Override
    public int gravityOverrideIfLow(EntityPlayer p) {
        // 1.7.10 armor slots: 0 = feet, 1 = legs, 2 = chest, 3 = head
        for (int i = 0; i < 4; i++) {
            ItemStack stack = p.inventory.armorItemInSlot(i);
            if (stack != null && stack.getItem() == ExtraPlanets_Armor.spaceSuitGravityBoots) {
                return 55;
            }
        }
        return 0;
    }

    @Override
    public int gravityOverrideIfHigh(EntityPlayer p) {
        for (int i = 0; i < 4; i++) {
            ItemStack stack = p.inventory.armorItemInSlot(i);
            if (stack != null && stack.getItem() == ExtraPlanets_Armor.spaceSuitGravityBoots) {
                return 75;
            }
        }
        return 0;
    }

    // ---------------------------------------------------------------------------
    // IBreathableArmor
    // ---------------------------------------------------------------------------

    @Override
    public boolean handleGearType(IBreathableArmor.EnumGearType gearType) {
        // The suit replaces all GC oxygen gear components when the full set is worn.
        return true;
    }

    @Override
    public boolean canBreathe(ItemStack helmetInSlot, EntityPlayer playerWearing, IBreathableArmor.EnumGearType type) {
        return isFullSuitWorn(playerWearing) && allPiecesHavePower(playerWearing);
    }

    private static boolean isFullSuitWorn(EntityPlayer player) {
        // 1.7.10 armor slots: 0 = boots, 1 = legs, 2 = chest, 3 = head.
        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = player.inventory.armorItemInSlot(slot);
            if (stack == null || !(stack.getItem() instanceof SpaceSuitArmor)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return false if any suit piece has zero or less electricity, meaning the suit cannot sustain
     *         breathing. The player must rely on GC oxygen gear as fallback until the suit is recharged.
     */
    private static boolean allPiecesHavePower(EntityPlayer player) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = player.inventory.armorItemInSlot(slot);
            if (stack != null && stack.getItem() instanceof IItemElectricBase) {
                if (((IItemElectricBase) stack.getItem()).getElectricityStored(stack) <= 0) {
                    return false;
                }
            }
        }
        return true;
    }

    // ---------------------------------------------------------------------------
    // Oxygen-consumption tick — drain power at the same rate as GC oxygen tanks
    // ---------------------------------------------------------------------------

    /** GC's height above which the overworld itself requires oxygen. */
    private static final int OXYGEN_HEIGHT_LIMIT = 450;

    /**
     * Drain interval matching GC's oxygen tank drain spacing (1 unit per 9 ticks).
     *
     * @see micdoodle8.mods.galacticraft.core.util.OxygenUtil#getDrainSpacing
     */
    private static final int OXYGEN_DRAIN_INTERVAL = 9;

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
        if (world.isRemote) {
            return; // server-side only
        }

        // Only drain when the suit is actually providing breathable air.
        if (!isFullSuitWorn(player) || !allPiecesHavePower(player)) {
            return;
        }

        // Creative players don't need oxygen.
        if (player.capabilities.isCreativeMode) {
            return;
        }

        // GC's oxygen-need condition: overworld above height limit, or a GC dimension without
        // breathable atmosphere.
        boolean needsOxygen = false;
        if (player.dimension == 0) {
            needsOxygen = player.posY > OXYGEN_HEIGHT_LIMIT;
        } else if (world.provider instanceof IGalacticraftWorldProvider) {
            needsOxygen = !((IGalacticraftWorldProvider) world.provider).hasBreathableAtmosphere();
        }
        if (!needsOxygen) {
            return;
        }

        // Only drain when the player is NOT inside a breathable air pocket (sealed base, oxygen
        // bubble, etc.).
        if (OxygenUtil.isAABBInBreathableAirBlock(player)) {
            return;
        }

        // Each piece contributes 1/4 of the total oxygen drain every 9 ticks.
        // With 4 pieces, the total drain is 1 gJ per 9 ticks — matching GC's oxygen tank rate.
        if ((player.ticksExisted - 1) % OXYGEN_DRAIN_INTERVAL == 0) {
            this.discharge(itemStack, 0.25F, true);
        }
    }

    // ---------------------------------------------------------------------------
    // Tooltip
    // ---------------------------------------------------------------------------

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
        // Let ElectricArmorBase append the energy bar
        super.addInformation(itemStack, player, list, par4Boolean);
    }

    // ---------------------------------------------------------------------------
    // Custom armour model (OBJ-based)
    // ---------------------------------------------------------------------------

    public static ModelBiped fillingArmorModel(ModelBiped model, EntityLivingBase entityLiving) {
        if (model == null) {
            return model;
        }
        model.bipedHead.showModel = model.bipedHeadwear.showModel = model.bipedBody.showModel = model.bipedRightArm.showModel = model.bipedLeftArm.showModel = model.bipedRightLeg.showModel = model.bipedLeftLeg.showModel = false;
        model.isSneak = entityLiving.isSneaking();
        model.isRiding = entityLiving.isRiding();
        model.isChild = entityLiving.isChild();

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
        if (itemStack.getItem() instanceof SpaceSuitArmor) {
            armorModel = fillingArmorModel(armorModel, entityLiving);
        }
        return armorModel;
    }
}
