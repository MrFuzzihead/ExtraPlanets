package com.mjr.extraplanets.items.armor.modules;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import micdoodle8.mods.galacticraft.core.util.GCLog;

/**
 * Abstract base for a single suit module. Each module has a name, an allowed armour slot type,
 * optional crafting requirements, an icon, an active state, and passive/use power costs.
 * <p>
 * {@link #tickServer} and {@link #tickClient} are called every tick while the module is active
 * and the suit piece has enough power for the passive cost.
 */
public abstract class Module implements Cloneable {

    private String name;
    /** Not final: {@link #copy()} gives each copy its own list instead of sharing the prototype's. */
    private List<ItemStack> requirements = new ArrayList<ItemStack>();
    /** Built on first read; see {@link #setRequirementSupplier(RequirementSource)}. Null once resolved. */
    private RequirementSource requirementSource;
    private boolean requirementsResolved;
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
     * @param slotType         armour slot this module is valid for, using the {@link net.minecraft.item.ItemArmor}
     *                         convention that {@code ModuleHelper.checkSlotCompatibility} compares against
     *                         (0=helmet, 1=chestplate, 2=leggings, 3=boots), or -1 for any slot.
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

    /**
     * Builds a module's requirement list on first use.
     * <p>
     * Requirements are declared this way so that a module can be constructed before the items it costs are
     * registered. {@link com.mjr.extraplanets.items.armor.modules.ExtraPlanets_Modules#init()} runs immediately
     * after {@code ExtraPlanets_Items.init()} in preInit, and three of the prototypes cost an ExtraPlanets item that
     * is only assigned inside that method. Building the stacks in the constructor meant a reordered preInit - or an
     * item disabled by config - silently produced an {@code ItemStack} with a null item: the module became free in
     * the GUI and impossible to install, because {@code ModuleHelper.installModule} can never match a null item.
     */
    public interface RequirementSource {

        List<ItemStack> get();
    }

    /**
     * One requirement, or nothing when the item does not exist on this side.
     * <p>
     * An item that a config option disables is never assigned, so the module would otherwise build an
     * {@code ItemStack} with a null item: free to install in the GUI and impossible to satisfy, because
     * {@code ModuleHelper.installModule} can never match a null item. Costs are therefore declared through this
     * helper, which checks the item <em>reference</em> (reliable: null means never assigned) rather than
     * {@code ItemStack#getItem()} (which is null for every stack outside a loaded game).
     *
     * @param item  the required item, or null when it is disabled.
     * @param count how many are consumed.
     * @return a one-entry list, or an empty list if the item is disabled.
     */
    protected static List<ItemStack> requirementOf(Item item, int count) {
        if (item == null) {
            GCLog.info("[ExtraPlanets] A suit-module requirement is disabled by config and was skipped.");
            return new ArrayList<ItemStack>();
        }
        List<ItemStack> requirements = new ArrayList<ItemStack>(1);
        requirements.add(new ItemStack(item, count));
        return requirements;
    }

    // -- Getters / setters ----------------------------------------------------------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the items consumed when this module is installed, resolving a
     *         {@linkplain #setRequirementSupplier(RequirementSource) deferred declaration} on first call.
     *         <p>
     *         Synchronised because a single-player client and its integrated server share these prototypes across two
     *         threads: without it, both could run the declaration at once and one could observe the list mid-update.
     */
    public synchronized List<ItemStack> getRequirements() {
        if (!this.requirementsResolved) {
            resolveRequirements();
        }
        return requirements;
    }

    /**
     * Declares how to build this module's requirements, invoked at most once, on the first
     * {@link #getRequirements()}. Anything the source reads is read then, not now.
     */
    public synchronized void setRequirementSupplier(RequirementSource source) {
        this.requirementSource = source;
        this.requirementsResolved = false;
        this.requirements = new ArrayList<ItemStack>();
    }

    /**
     * Replaces the requirement list outright and cancels any deferred declaration.
     */
    public synchronized void setRequirements(List<ItemStack> requirements) {
        this.requirementSource = null;
        this.requirementsResolved = true;
        this.requirements = new ArrayList<ItemStack>(requirements);
    }

    /** Runs the deferred declaration, once. Called with the lock held. */
    private void resolveRequirements() {
        // One-shot: a source that throws must not be retried on every tick (the HUD path reads this list).
        this.requirementsResolved = true;
        RequirementSource source = this.requirementSource;
        this.requirementSource = null;
        if (source == null) {
            return;
        }

        List<ItemStack> resolved = source.get();
        List<ItemStack> kept = new ArrayList<ItemStack>(resolved == null ? 0 : resolved.size());
        if (resolved != null) {
            for (ItemStack stack : resolved) {
                // Deliberately does not look at stack.getItem(): Forge resolves that through the item's registry
                // delegate, which is null outside a loaded game (so even a vanilla stack reports a null item in a
                // unit test). "Is this item available?" is decided by requirementOf(Item, int) instead, on the
                // item reference itself, where null unambiguously means "never assigned".
                if (stack == null) {
                    continue;
                }
                kept.add(stack);
            }
        }
        // Swap the reference rather than clearing in place, so a concurrent reader never sees a half-built list.
        this.requirements = kept;
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
     * Creates a copy of this module with its own active state, subtype and requirement list.
     * <p>
     * The requirement list has to be copied as well: {@link ModuleHelper#getModules(net.minecraft.item.ItemStack)}
     * hands out copies, and callers that edit a copy's requirements (or clear them) must never mutate the registry
     * prototype that every other installed suit of the same type looks up. A deferred declaration is resolved here,
     * so a copy is always complete.
     */
    public Module copy() {
        try {
            Module copy = (Module) super.clone();
            copy.requirements = new ArrayList<ItemStack>(this.getRequirements());
            copy.requirementSource = null;
            copy.requirementsResolved = true;
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
