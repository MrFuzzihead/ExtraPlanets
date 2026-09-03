package com.mjr.extraplanets.items.armor.modules;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.mjr.extraplanets.api.item.IModularArmor;
import com.mjr.extraplanets.armor.bases.ElectricArmorBase;

/**
 * Utility methods for reading, writing, installing and uninstalling modules on
 * {@link IModularArmor} items. Modules are stored in NBT as a list of
 * <code>{module: "name", active: 1|0}</code> entries under the {@code "modules"} tag.
 */
public class ModuleHelper {

    /**
     * Ensures the ItemStack has the {@code "modules"} NBT tag list initialised.
     */
    public static void setupModulesNBT(ItemStack item) {
        if (!(item.getItem() instanceof IModularArmor)) {
            return;
        }
        if (!item.hasTagCompound()) {
            item.setTagCompound(new NBTTagCompound());
        }
        if (!item.getTagCompound()
            .hasKey("modules")) {
            item.getTagCompound()
                .setTag("modules", new NBTTagList());
        }
    }

    /**
     * @return the list of {@link Module} instances currently installed on this item, with their
     *         active states populated from NBT. Returns an empty list if the item is not modular
     *         or has no modules tag.
     */
    public static List<Module> getModules(ItemStack item) {
        List<Module> result = new ArrayList<Module>();
        if (!(item.getItem() instanceof IModularArmor)) {
            return result;
        }
        if (!item.hasTagCompound() || !item.getTagCompound()
            .hasKey("modules")) {
            return result;
        }
        NBTTagList tagList = item.getTagCompound()
            .getTagList("modules", 10);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound entry = tagList.getCompoundTagAt(i);
            String moduleName = entry.getString("module");
            boolean active;
            active = entry.hasKey("active") ? entry.getInteger("active") != 0 : false;
            int data = entry.hasKey("data") ? entry.getInteger("data") : 0;
            for (Module mod : ExtraPlanets_Modules.getModules()) {
                if (mod.getName()
                    .equalsIgnoreCase(moduleName)) {
                    Module copy = mod.copy();
                    copy.setActive(active);
                    copy.setSubType(data);
                    result.add(copy);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Completely replaces the module list on the item with the given list.
     */
    public static void setModules(ItemStack item, List<Module> modules) {
        if (!(item.getItem() instanceof IModularArmor)) {
            return;
        }
        NBTTagCompound nbt = item.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            item.setTagCompound(nbt);
        }
        NBTTagList tagList = new NBTTagList();
        for (Module mod : modules) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("module", mod.getName());
            entry.setBoolean("active", mod.isActive());
            if (mod.getSubType() != 0) {
                entry.setInteger("data", mod.getSubType());
            }
            tagList.appendTag(entry);
        }
        nbt.setTag("modules", tagList);
    }

    /**
     * Appends a single module (with its current active state) to the item's module list.
     */
    public static void addModule(ItemStack item, Module module) {
        if (!(item.getItem() instanceof IModularArmor)) {
            return;
        }
        setupModulesNBT(item);
        NBTTagCompound nbt = item.getTagCompound();
        NBTTagList tagList = nbt.getTagList("modules", 10);
        NBTTagCompound entry = new NBTTagCompound();
        entry.setString("module", module.getName());
        entry.setBoolean("active", module.isActive());
        if (module.getSubType() != 0) {
            entry.setInteger("data", module.getSubType());
        }
        tagList.appendTag(entry);
        nbt.setTag("modules", tagList);
    }

    /**
     * Updates the active state of a single module in NBT without touching the rest.
     */
    public static void updateModuleActiveState(ItemStack item, Module module, boolean active) {
        List<Module> modules = getModules(item);
        for (Module m : modules) {
            if (m.getName()
                .equalsIgnoreCase(module.getName())) {
                m.setActive(active);
            }
        }
        setModules(item, modules);
    }

    /**
     * Removes a single module from the item's NBT list.
     */
    public static void removeModule(ItemStack item, Module module) {
        List<Module> modules = getModules(item);
        List<Module> remaining = new ArrayList<Module>();
        for (Module m : modules) {
            if (!m.getName()
                .equalsIgnoreCase(module.getName())) {
                remaining.add(m);
            }
        }
        setModules(item, remaining);
    }

    /**
     * Checks whether the module is compatible with the given item's slot, and whether the
     * player has the required items in their inventory. If both conditions are satisfied,
     * the requirements are consumed and the module is added.
     *
     * @return true if the module was successfully installed.
     */
    public static boolean installModule(ItemStack item, Module module, EntityPlayer player) {
        if (!(item.getItem() instanceof IModularArmor)) {
            return false;
        }
        if (!checkSlotCompatibility(item, module)) {
            return false;
        }
        if (hasModule(item, module)) {
            return false; // already installed
        }

        // Check and consume requirements
        for (ItemStack required : module.getRequirements()) {
            if (!consumeFromInventory(player, required)) {
                return false; // missing items — roll back nothing was consumed yet
            }
        }
        // If we reach here, all requirements were consumed; install the module
        addModule(item, module);
        return true;
    }

    /**
     * Uninstalls a module and returns its crafting ingredients to the player's inventory
     * (or drops them if the inventory is full).
     */
    public static void uninstallModule(ItemStack item, Module module, EntityPlayer player) {
        if (!(item.getItem() instanceof IModularArmor)) {
            return;
        }
        removeModule(item, module);
        for (ItemStack req : module.getRequirements()) {
            if (!player.inventory.addItemStackToInventory(req.copy())) {
                player.entityDropItem(req.copy(), 0.0F);
            }
        }
    }

    /**
     * @return true if the module's slot type matches the armour slot of the given item.
     *         Slot types use the 1.7.10 ItemArmor convention: 0=head, 1=chest, 2=legs, 3=boots.
     *         A slot type of -1 means universal (compatible with any slot).
     */
    public static boolean checkSlotCompatibility(ItemStack item, Module module) {
        if (!(item.getItem() instanceof IModularArmor)) {
            return false;
        }
        if (module.getSlotType() == -1) {
            return true;
        }
        if (item.getItem() instanceof net.minecraft.item.ItemArmor) {
            return module.getSlotType() == ((net.minecraft.item.ItemArmor) item.getItem()).armorType;
        }
        return false;
    }

    public static boolean hasModule(ItemStack item, Module module) {
        return hasModule(item, module.getName());
    }

    public static boolean hasModule(ItemStack item, String moduleName) {
        if (!(item.getItem() instanceof IModularArmor)) {
            return false;
        }
        for (Module m : getModules(item)) {
            if (m.getName()
                .equalsIgnoreCase(moduleName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isModuleActive(ItemStack item, Module module) {
        return isModuleActive(item, module.getName());
    }

    public static boolean isModuleActive(ItemStack item, String moduleName) {
        if (!(item.getItem() instanceof IModularArmor)) {
            return false;
        }
        for (Module m : getModules(item)) {
            if (m.getName()
                .equalsIgnoreCase(moduleName)) {
                return m.isActive();
            }
        }
        return false;
    }

    /**
     * @return the stored power in gJ of the given modular armour item.
     */
    public static int getArmourStoredPower(ItemStack item) {
        if (!(item.getItem() instanceof IModularArmor)) {
            return 0;
        }
        return (int) ((ElectricArmorBase) item.getItem()).getElectricityStored(item);
    }

    /**
     * Discharges {@code power} gJ from the given modular armour item.
     */
    public static void takeArmourPower(ItemStack item, int power) {
        if (!(item.getItem() instanceof IModularArmor)) {
            return;
        }
        ((ElectricArmorBase) item.getItem()).discharge(item, power, true);
    }

    /**
     * @return true if the item has at least {@code power} gJ of stored energy.
     */
    public static boolean hasPower(ItemStack item, int power) {
        return getArmourStoredPower(item) >= power;
    }

    // -- Internal helpers ------------------------------------------------------------

    /**
     * Tries to remove one {@code required} stack from the player's inventory.
     *
     * @return true if the item was found and consumed.
     */
    public static boolean consumeFromInventory(EntityPlayer player, ItemStack required) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.isItemEqual(required) && stack.stackSize >= required.stackSize) {
                stack.stackSize -= required.stackSize;
                if (stack.stackSize <= 0) {
                    player.inventory.setInventorySlotContents(i, null);
                }
                return true;
            }
        }
        return false;
    }
}
