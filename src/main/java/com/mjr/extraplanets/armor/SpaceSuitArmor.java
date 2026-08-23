package com.mjr.extraplanets.armor;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import com.mjr.extraplanets.Constants;
import com.mjr.extraplanets.ExtraPlanets;
import com.mjr.extraplanets.api.item.IModularArmor;
import com.mjr.extraplanets.api.item.IPressureSuit;
import com.mjr.extraplanets.api.item.IRadiationSuit;
import com.mjr.extraplanets.armor.bases.ElectricArmorBase;
import com.mjr.extraplanets.client.model.ArmorSpaceSuitModel;
import com.mjr.extraplanets.items.armor.modules.Module;
import com.mjr.extraplanets.items.armor.modules.ModuleBatteryExpansion;
import com.mjr.extraplanets.items.armor.modules.ModuleEnhancedGravity;
import com.mjr.extraplanets.items.armor.modules.ModuleGravityController;
import com.mjr.extraplanets.items.armor.modules.ModuleHelper;
import com.mjr.extraplanets.items.armor.modules.ModulePressureSeal;
import com.mjr.extraplanets.items.armor.modules.ModuleRadiationShield;

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
 * <p>
 * Provides pressure, radiation, breathing, and gravity-override protection via modular
 * upgrades. Modules are installed per-piece and provide passive/active effects.
 */
public class SpaceSuitArmor extends ElectricArmorBase
    implements IPressureSuit, IRadiationSuit, IArmorGravity, IBreathableArmor, IModularArmor {

    private static final Map<EntityLivingBase, ArmorSpaceSuitModel[]> entityModels = new WeakHashMap<EntityLivingBase, ArmorSpaceSuitModel[]>();

    public String name;

    /** Base protection values per slot: {boots, legs, chest, head}. */
    private static final int[] BASE_PROTECTION = { 2, 6, 5, 2 };

    // ===========================================================================
    // Construction
    // ===========================================================================

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

    /**
     * Opens the module manager GUI when the player shift+right-clicks with this item in hand.
     */
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (player.isSneaking() && !world.isRemote) {
            com.mjr.extraplanets.client.gui.GuiModuleManager.openForPlayer((EntityPlayerMP) player);
        }
        return itemStack;
    }

    // ===========================================================================
    // Electricity capacity — modified by Battery Expansion modules
    // ===========================================================================

    @Override
    public float getMaxElectricityStored(ItemStack theItem) {
        float base = 50000; // 10000 * 5 — matches upstream Tier1 capacity
        return ModuleBatteryExpansion.getBoostedCapacity(base, theItem);
    }

    // ===========================================================================
    // Texture
    // ===========================================================================

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
        if (stack.getItem() == ExtraPlanets_Armor.spaceSuitHelmet
            || stack.getItem() == ExtraPlanets_Armor.spaceSuitChest
            || stack.getItem() == ExtraPlanets_Armor.spaceSuitBoots) {
            return Constants.TEXTURE_PREFIX + "textures/model/armor/space_suit_layer_1.png";
        } else if (stack.getItem() == ExtraPlanets_Armor.spaceSuitLegings) {
            return Constants.TEXTURE_PREFIX + "textures/model/armor/space_suit_layer_2.png";
        }
        return slot == 2 ? Constants.TEXTURE_PREFIX + "textures/model/armor/space_suit_layer_2.png"
            : Constants.TEXTURE_PREFIX + "textures/model/armor/space_suit_layer_1.png";
    }

    // ===========================================================================
    // Radiation / pressure tier — computed from installed modules
    // ===========================================================================

    @Override
    public int getArmorTier() {
        // Returns the highest radiation shield tier installed across all pieces.
        // This is called by the IRadiationSuit interface; the actual check per-piece
        // happens at the radiation system level.
        return 1; // base tier; upgraded by modules
    }

    /**
     * Scans all 4 suit pieces for the highest installed Radiation Shield module tier.
     */
    public static int getMaxRadiationTier(EntityPlayer player) {
        int maxTier = 0;
        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = player.inventory.armorItemInSlot(slot);
            if (stack == null || !(stack.getItem() instanceof IModularArmor)) {
                continue;
            }
            for (Module m : ModuleHelper.getModules(stack)) {
                if (m instanceof ModuleRadiationShield && m.isActive()) {
                    maxTier = Math.max(maxTier, m.getSubType());
                }
            }
        }
        return maxTier;
    }

    /**
     * Scans all 4 suit pieces for the highest installed Pressure Seal module tier.
     */
    public static int getMaxPressureTier(EntityPlayer player) {
        int maxTier = 0;
        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = player.inventory.armorItemInSlot(slot);
            if (stack == null || !(stack.getItem() instanceof IModularArmor)) {
                continue;
            }
            for (Module m : ModuleHelper.getModules(stack)) {
                if (m instanceof ModulePressureSeal && m.isActive()) {
                    maxTier = Math.max(maxTier, m.getSubType());
                }
            }
        }
        return maxTier;
    }

    // ===========================================================================
    // Gravity override — only the boots piece contributes
    // ===========================================================================

    @Override
    public int gravityOverrideIfLow(EntityPlayer p) {
        // Only the boots piece (armorType == 3) should contribute, or GC will sum
        // the override 4 times (once per armor piece) and produce 4x the intended value.
        if (this.armorType != 3) {
            return 0;
        }
        ItemStack boots = p.inventory.armorItemInSlot(0);
        if (boots != null && boots.getItem() instanceof IModularArmor) {
            if (ModuleEnhancedGravity.isActiveOn(boots)) {
                return ModuleEnhancedGravity.OVERRIDE_LOW;
            }
            if (ModuleGravityController.isActiveOn(boots)) {
                return ModuleGravityController.OVERRIDE_LOW;
            }
        }
        return 0;
    }

    @Override
    public int gravityOverrideIfHigh(EntityPlayer p) {
        if (this.armorType != 3) {
            return 0;
        }
        ItemStack boots = p.inventory.armorItemInSlot(0);
        if (boots != null && boots.getItem() instanceof IModularArmor) {
            if (ModuleEnhancedGravity.isActiveOn(boots)) {
                return ModuleEnhancedGravity.OVERRIDE_HIGH;
            }
            if (ModuleGravityController.isActiveOn(boots)) {
                return ModuleGravityController.OVERRIDE_HIGH;
            }
        }
        return 0;
    }

    // ===========================================================================
    // IBreathableArmor
    // ===========================================================================

    @Override
    public boolean handleGearType(IBreathableArmor.EnumGearType gearType) {
        return true;
    }

    @Override
    public boolean canBreathe(ItemStack helmetInSlot, EntityPlayer playerWearing, IBreathableArmor.EnumGearType type) {
        return isFullSuitWorn(playerWearing) && allPiecesHavePower(playerWearing);
    }

    private static boolean isFullSuitWorn(EntityPlayer player) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = player.inventory.armorItemInSlot(slot);
            if (stack == null || !(stack.getItem() instanceof SpaceSuitArmor)) {
                return false;
            }
        }
        return true;
    }

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

    // ===========================================================================
    // onArmorTick — oxygen drain + module passive power drain + module tick
    // ===========================================================================

    private static final int OXYGEN_HEIGHT_LIMIT = 450;
    private static final int OXYGEN_DRAIN_INTERVAL = 9;
    /** How often (in ticks) passive power is drained for active modules. */
    private static final int MODULE_PASSIVE_DRAIN_INTERVAL = 20;

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {

        if (world.isRemote) {
            tickClientModules(player, itemStack);
            return;
        }

        // --- Server-side ---

        // Passive power drain for active modules (once per second)
        if ((player.ticksExisted - 1) % MODULE_PASSIVE_DRAIN_INTERVAL == 0) {
            drainPassivePower(itemStack, player);
        }

        // Call tickServer for each active module
        tickServerModules(player, itemStack);

        // --- Oxygen drain (every piece drains at 0.25 gJ per 9 ticks) ---
        tickOxygenDrain(world, player, itemStack);
    }

    /**
     * Deducts passive power costs for all active modules on this piece.
     */
    private void drainPassivePower(ItemStack stack, EntityPlayer player) {
        if (!(stack.getItem() instanceof IModularArmor)) {
            return;
        }
        for (Module m : ModuleHelper.getModules(stack)) {
            if (m.isActive() && m.getPassivePowerCost() > 0) {
                if (ModuleHelper.hasPower(stack, m.getPassivePowerCost())) {
                    ModuleHelper.takeArmourPower(stack, m.getPassivePowerCost());
                } else {
                    // Not enough power — deactivate the module
                    m.setActive(false);
                    ModuleHelper.updateModuleActiveState(stack, m, false);
                }
            }
        }
    }

    /**
     * Calls {@link Module#tickServer(EntityPlayerMP)} for each active module on this piece.
     */
    private void tickServerModules(EntityPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof IModularArmor) || !(player instanceof EntityPlayerMP)) {
            return;
        }
        for (Module m : ModuleHelper.getModules(stack)) {
            if (m.isActive()) {
                m.tickServer((EntityPlayerMP) player);
            }
        }
    }

    /**
     * Calls {@link Module#tickClient(EntityPlayer)} for each active module on this piece.
     */
    @SideOnly(Side.CLIENT)
    private void tickClientModules(EntityPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof IModularArmor)) {
            return;
        }
        for (Module m : ModuleHelper.getModules(stack)) {
            if (m.isActive()) {
                m.tickClient(player);
            }
        }
    }

    private void tickOxygenDrain(World world, EntityPlayer player, ItemStack itemStack) {
        if (!isFullSuitWorn(player) || !allPiecesHavePower(player)) {
            return;
        }
        if (player.capabilities.isCreativeMode) {
            return;
        }

        boolean needsOxygen = false;
        if (player.dimension == 0) {
            needsOxygen = player.posY > OXYGEN_HEIGHT_LIMIT;
        } else if (world.provider instanceof IGalacticraftWorldProvider) {
            needsOxygen = !((IGalacticraftWorldProvider) world.provider).hasBreathableAtmosphere();
        }
        if (!needsOxygen) {
            return;
        }

        if (OxygenUtil.isAABBInBreathableAirBlock(player)) {
            return;
        }

        if ((player.ticksExisted - 1) % OXYGEN_DRAIN_INTERVAL == 0) {
            this.discharge(itemStack, 0.25F, true);
        }
    }

    // ===========================================================================
    // Tooltip
    // ===========================================================================

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
        // Show installed modules
        List<Module> modules = ModuleHelper.getModules(itemStack);
        if (!modules.isEmpty()) {
            list.add(EnumColor.GREY + StatCollector.translateToLocal("gui.module_list.name") + ":");
            for (Module m : modules) {
                String color = m.isActive() ? EnumColor.BRIGHT_GREEN.toString() : EnumColor.GREY.toString();
                list.add(color + "  " + StatCollector.translateToLocal("gui.module." + m.getName() + ".name"));
            }
        }
        // Let ElectricArmorBase append the energy bar
        super.addInformation(itemStack, player, list, par4Boolean);
    }

    // ===========================================================================
    // Custom armour model (OBJ-based)
    // ===========================================================================

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
