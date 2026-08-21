package com.mjr.extraplanets.armor.bases;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;

import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import cpw.mods.fml.common.Optional.Method;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import mekanism.api.energy.IEnergizedItem;
import micdoodle8.mods.galacticraft.api.item.ElectricItemHelper;
import micdoodle8.mods.galacticraft.api.item.IItemElectricBase;
import micdoodle8.mods.galacticraft.core.energy.EnergyConfigHandler;
import micdoodle8.mods.galacticraft.core.energy.EnergyDisplayHelper;
import micdoodle8.mods.galacticraft.core.energy.item.ElectricItemManagerIC2_1710;
import micdoodle8.mods.galacticraft.core.items.ItemBatteryInfinite;

/**
 * Base class for electric armour items that take their durability from power (gJ / RF / EU / J),
 * drawing from GC, CoFH/RF, IC2, and Mekanism power systems.
 *
 * <p>
 * Extends {@link ItemArmor} and manually wires up all electric interfaces because forge energy is
 * not unified in 1.7.10 and GC's own {@code ItemElectricBase} extends {@link Item}, not
 * {@link ItemArmor}. Derived from both the upstream 1.12.2 ExtraPlanets
 * {@code ElectricArmorBase} and GC 1.7.10's {@code ItemElectricBase}.
 *
 * <p>
 * Energy is stored in NBT (key {@code "electricity"}, in Galacticraft gJ). The item damage bar
 * is repurposed as an energy bar (0 damage = full, 100 damage = empty).
 *
 * <p>
 * Armour protection is {@value #DAMAGE_ABSORB_RATIO} per piece with {@code Integer.MAX_VALUE}
 * virtual durability (the actual durability is the charge). Incoming damage discharges power at
 * {@value #POWER_PER_DAMAGE} gJ per damage point.
 *
 * <p>
 * Subclasses must override {@link #getMaxElectricityStored(ItemStack)} to set the capacity.
 */
@InterfaceList({ @Interface(modid = "CoFHAPI|energy", iface = "cofh.api.energy.IEnergyContainerItem"),
    @Interface(modid = "IC2API", iface = "ic2.api.item.ISpecialElectricItem"),
    @Interface(modid = "MekanismAPI|energy", iface = "mekanism.api.energy.IEnergizedItem") })
public abstract class ElectricArmorBase extends ItemArmor
    implements IItemElectricBase, ISpecialArmor, IEnergyContainerItem, ISpecialElectricItem, IEnergizedItem {

    /** Maximum transfer rate per operation, in gJ. */
    public float transferMax = 200;

    /** Number of damage/metadata ticks that map to the full energy range. */
    public static final int DAMAGE_RANGE = 100;

    /** Armour damage absorption ratio (20 % per piece). */
    private static final double DAMAGE_ABSORB_RATIO = 0.20D;

    /** Power consumed per point of incoming damage, in gJ. */
    private static final float POWER_PER_DAMAGE = 2.5F;

    /** IC2 item manager bridge. */
    private static Object itemManagerIC2;

    /**
     * @param material    the armour material (used for equip sound, enchantability, etc.).
     * @param renderIndex the render index for the vanilla armour model (3 = diamond chain, etc.;
     *                    not used when a custom model is provided via {@code getArmorModel}).
     * @param armorType   0 = helmet, 1 = chest, 2 = legs, 3 = boots.
     */
    public ElectricArmorBase(ArmorMaterial material, int renderIndex, int armorType) {
        super(material, renderIndex, armorType);
        this.setMaxStackSize(1);
        this.setMaxDamage(DAMAGE_RANGE);
        this.setNoRepair();

        if (EnergyConfigHandler.isIndustrialCraft2Loaded()) {
            itemManagerIC2 = new ElectricItemManagerIC2_1710();
        }
    }

    // ---------------------------------------------------------------------------
    // Durability bar — repurposed as energy bar
    // ---------------------------------------------------------------------------

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    // ---------------------------------------------------------------------------
    // IItemElectric / IItemElectricBase — GC power API
    // ---------------------------------------------------------------------------

    @Override
    public float getMaxTransferGC(ItemStack itemStack) {
        return this.transferMax;
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4) {
        String color;
        final float joules = this.getElectricityStored(itemStack);

        if (joules <= this.getMaxElectricityStored(itemStack) / 3) {
            color = "\u00a74"; // red
        } else if (joules > this.getMaxElectricityStored(itemStack) * 2 / 3) {
            color = "\u00a72"; // green
        } else {
            color = "\u00a76"; // gold
        }

        list.add(
            color + EnergyDisplayHelper.getEnergyDisplayS(joules)
                + "/"
                + EnergyDisplayHelper.getEnergyDisplayS(this.getMaxElectricityStored(itemStack)));
    }

    /**
     * Start uncharged when crafted.
     */
    @Override
    public void onCreated(ItemStack itemStack, World par2World, EntityPlayer par3EntityPlayer) {
        this.setElectricity(itemStack, 0);
    }

    @Override
    public float recharge(ItemStack itemStack, float energy, boolean doReceive) {
        final float rejectedElectricity = Math
            .max(this.getElectricityStored(itemStack) + energy - this.getMaxElectricityStored(itemStack), 0);
        float energyToReceive = energy - rejectedElectricity;
        if (energyToReceive > this.transferMax) {
            energyToReceive = this.transferMax;
        }

        if (doReceive) {
            this.setElectricity(itemStack, this.getElectricityStored(itemStack) + energyToReceive);
        }

        return energyToReceive;
    }

    @Override
    public float discharge(ItemStack itemStack, float energy, boolean doTransfer) {
        final float energyToTransfer = Math
            .min(Math.min(this.getElectricityStored(itemStack), energy), this.transferMax);

        if (doTransfer) {
            this.setElectricity(itemStack, this.getElectricityStored(itemStack) - energyToTransfer);
        }

        return energyToTransfer;
    }

    @Override
    public int getTierGC(ItemStack itemStack) {
        return 1;
    }

    @Override
    public void setElectricity(ItemStack itemStack, float joules) {
        if (itemStack.getTagCompound() == null) {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        final float electricityStored = Math.max(Math.min(joules, this.getMaxElectricityStored(itemStack)), 0);
        itemStack.getTagCompound()
            .setFloat("electricity", electricityStored);

        // Map charge to item damage for the built-in durability bar
        itemStack.setItemDamage(
            DAMAGE_RANGE - (int) (electricityStored / this.getMaxElectricityStored(itemStack) * DAMAGE_RANGE));
    }

    @Override
    public float getTransfer(ItemStack itemStack) {
        return Math
            .min(this.transferMax, this.getMaxElectricityStored(itemStack) - this.getElectricityStored(itemStack));
    }

    @Override
    public float getElectricityStored(ItemStack itemStack) {
        if (itemStack.getTagCompound() == null) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        float energyStored = 0f;
        if (itemStack.getTagCompound()
            .hasKey("electricity")) {
            final NBTBase obj = itemStack.getTagCompound()
                .getTag("electricity");
            if (obj instanceof NBTTagDouble) {
                energyStored = ((NBTTagDouble) obj).func_150288_h();
            } else if (obj instanceof NBTTagFloat) {
                energyStored = ((NBTTagFloat) obj).func_150288_h();
            }
        }

        // Keep the damage bar in sync
        itemStack.setItemDamage(
            DAMAGE_RANGE - (int) (energyStored / this.getMaxElectricityStored(itemStack) * DAMAGE_RANGE));
        return energyStored;
    }

    @Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
        par3List.add(ElectricItemHelper.getUncharged(new ItemStack(this)));
        par3List.add(
            ElectricItemHelper.getWithCharge(new ItemStack(this), this.getMaxElectricityStored(new ItemStack(this))));
    }

    // ---------------------------------------------------------------------------
    // ISpecialArmor — custom armour behaviour (charge-as-durability)
    // ---------------------------------------------------------------------------

    @Override
    public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage,
        int slot) {
        // Integer.MAX_VALUE virtual durability means the armour never breaks from wear —
        // it only becomes non-functional when out of charge.
        return new ArmorProperties(1, DAMAGE_ABSORB_RATIO, Integer.MAX_VALUE);
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
        return 5;
    }

    @Override
    public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {
        this.discharge(stack, POWER_PER_DAMAGE * damage, true);
    }

    // ---------------------------------------------------------------------------
    // Static helpers for checking electric items
    // ---------------------------------------------------------------------------

    public static boolean isElectricItem(Item item) {
        if (item instanceof IItemElectricBase) {
            return true;
        }
        if (EnergyConfigHandler.isIndustrialCraft2Loaded()) {
            return item instanceof ISpecialElectricItem;
        }
        return false;
    }

    public static boolean isElectricItemEmpty(ItemStack itemstack) {
        if (itemstack == null) {
            return false;
        }
        final Item item = itemstack.getItem();

        if (item instanceof IItemElectricBase) {
            return ((IItemElectricBase) item).getElectricityStored(itemstack) <= 0;
        }

        if (EnergyConfigHandler.isIndustrialCraft2Loaded() && item instanceof ISpecialElectricItem) {
            return !((ISpecialElectricItem) item).canProvideEnergy(itemstack);
        }

        return false;
    }

    public static boolean isElectricItemCharged(ItemStack itemstack) {
        if (itemstack == null) {
            return false;
        }
        final Item item = itemstack.getItem();

        if (item instanceof IItemElectricBase) {
            return ((IItemElectricBase) item).getElectricityStored(itemstack) > 0;
        }

        if (EnergyConfigHandler.isIndustrialCraft2Loaded() && item instanceof ISpecialElectricItem) {
            return ((ISpecialElectricItem) item).canProvideEnergy(itemstack);
        }

        return false;
    }

    // ---------------------------------------------------------------------------
    // IEnergyContainerItem — CoFH / RedstoneFlux compatibility
    // ---------------------------------------------------------------------------

    @Override
    @Method(modid = "CoFHAPI|energy")
    public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
        return (int) (this.recharge(container, maxReceive * EnergyConfigHandler.RF_RATIO, !simulate)
            / EnergyConfigHandler.RF_RATIO);
    }

    @Override
    @Method(modid = "CoFHAPI|energy")
    public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
        return (int) (this.discharge(container, maxExtract / EnergyConfigHandler.TO_RF_RATIO, !simulate)
            * EnergyConfigHandler.TO_RF_RATIO);
    }

    @Override
    @Method(modid = "CoFHAPI|energy")
    public int getEnergyStored(ItemStack container) {
        return (int) (this.getElectricityStored(container) * EnergyConfigHandler.TO_RF_RATIO);
    }

    @Override
    @Method(modid = "CoFHAPI|energy")
    public int getMaxEnergyStored(ItemStack container) {
        return (int) (this.getMaxElectricityStored(container) * EnergyConfigHandler.TO_RF_RATIO);
    }

    // ---------------------------------------------------------------------------
    // IEnergizedItem — Mekanism compatibility
    // ---------------------------------------------------------------------------

    @Override
    @Method(modid = "MekanismAPI|energy")
    public double getEnergy(ItemStack itemStack) {
        return this.getElectricityStored(itemStack) * EnergyConfigHandler.TO_MEKANISM_RATIO;
    }

    @Override
    @Method(modid = "MekanismAPI|energy")
    public void setEnergy(ItemStack itemStack, double amount) {
        this.setElectricity(itemStack, (float) amount * EnergyConfigHandler.MEKANISM_RATIO);
    }

    @Override
    @Method(modid = "MekanismAPI|energy")
    public double getMaxEnergy(ItemStack itemStack) {
        return this.getMaxElectricityStored(itemStack) * EnergyConfigHandler.TO_MEKANISM_RATIO;
    }

    @Override
    @Method(modid = "MekanismAPI|energy")
    public double getMaxTransfer(ItemStack itemStack) {
        return this.transferMax * EnergyConfigHandler.TO_MEKANISM_RATIO;
    }

    @Override
    @Method(modid = "MekanismAPI|energy")
    public boolean canReceive(ItemStack itemStack) {
        return itemStack != null && !(itemStack.getItem() instanceof ItemBatteryInfinite);
    }

    @Override
    @Method(modid = "MekanismAPI|energy")
    public boolean canSend(ItemStack itemStack) {
        return true;
    }

    // ---------------------------------------------------------------------------
    // ISpecialElectricItem — IC2 compatibility
    // ---------------------------------------------------------------------------

    @Override
    @Method(modid = "IC2API")
    public IElectricItemManager getManager(ItemStack itemstack) {
        return (IElectricItemManager) ElectricArmorBase.itemManagerIC2;
    }

    @Override
    @Method(modid = "IC2API")
    public boolean canProvideEnergy(ItemStack itemStack) {
        return true;
    }

    @Override
    @Method(modid = "IC2API")
    public Item getChargedItem(ItemStack itemStack) {
        return itemStack.getItem();
    }

    @Override
    @Method(modid = "IC2API")
    public Item getEmptyItem(ItemStack itemStack) {
        return itemStack.getItem();
    }

    @Override
    @Method(modid = "IC2API")
    public int getTier(ItemStack itemStack) {
        return 1;
    }

    @Override
    @Method(modid = "IC2API")
    public double getMaxCharge(ItemStack itemStack) {
        return this.getMaxElectricityStored(itemStack) / EnergyConfigHandler.IC2_RATIO;
    }

    @Override
    @Method(modid = "IC2API")
    public double getTransferLimit(ItemStack itemStack) {
        return this.transferMax * EnergyConfigHandler.TO_IC2_RATIO;
    }
}
