package com.mjr.extraplanets.items.armor.modules;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Abstract base for a single suit module. Each module has a name, an allowed armour slot type,
 * optional crafting requirements, an icon, an active state, and passive/use power costs.
 * <p>
 * {@link #tickServer} and {@link #tickClient} are called every tick while the module is active
 * and the suit piece has enough power for the passive cost.
 */
public abstract class Module implements Cloneable {

    private String name;
    private final List<ItemStack> requirements = new ArrayList<ItemStack>();
    private final int slotType;
    private ItemStack icon;
    private boolean active;
    private int passivePowerCost;
    private int usePowerCost;
    /** Optional integer data specific to the module type (e.g. radiation tier, speed tier). */
    private int subType;

    /**
     * @param name             unique module key (used for NBT and lang keys).
     * @param requirements     items consumed on install.
     * @param slotType         armour slot this module is valid for (0=feet, 1=legs, 2=chest, 3=head,
     *                         or -1 for any slot).
     * @param icon             item stack used as the module icon in GUIs.
     * @param active           default active state when first installed.
     * @param passivePowerCost power drained every tick while active (gJ).
     * @param usePowerCost     power drained on each use action (gJ).
     */
    public Module(String name, List<ItemStack> requirements, int slotType, ItemStack icon, boolean active,
        int passivePowerCost, int usePowerCost) {
        this.name = name;
        this.requirements.addAll(requirements);
        this.slotType = slotType;
        this.icon = icon;
        this.active = active;
        this.passivePowerCost = passivePowerCost;
        this.usePowerCost = usePowerCost;
    }

    /**
     * Convenience constructor for modules with no requirements.
     */
    public Module(String name, int slotType, ItemStack icon, boolean active, int passivePowerCost, int usePowerCost) {
        this(name, new ArrayList<ItemStack>(), slotType, icon, active, passivePowerCost, usePowerCost);
    }

    // -- Getters / setters ----------------------------------------------------------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ItemStack> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<ItemStack> requirements) {
        this.requirements.clear();
        this.requirements.addAll(requirements);
    }

    public int getSlotType() {
        return slotType;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return getName() + ".desc";
    }

    public int getPassivePowerCost() {
        return passivePowerCost;
    }

    public void setPassivePowerCost(int passivePowerCost) {
        this.passivePowerCost = passivePowerCost;
    }

    public int getUsePowerCost() {
        return usePowerCost;
    }

    public void setUsePowerCost(int usePowerCost) {
        this.usePowerCost = usePowerCost;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    // -- Tick methods ---------------------------------------------------------------

    /** Called every tick on the server side while the module is active. */
    public abstract void tickServer(EntityPlayerMP player);

    /** Called every tick on the client side while the module is active. */
    @SideOnly(Side.CLIENT)
    public abstract void tickClient(EntityPlayer player);

    /**
     * Creates a shallow copy of this module with its own active state.
     */
    public Module copy() {
        try {
            Module copy = (Module) super.clone();
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Module) {
            return ((Module) obj).getName()
                .equals(this.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getName()
            .hashCode();
    }
}
