package com.mjr.extraplanets.items;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.mjr.extraplanets.Constants;
import com.mjr.extraplanets.ExtraPlanets;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Module item that stores its module name and optional tier data in NBT.
 * Used for installing modules into space suit pieces via the GUI.
 */
public class ItemModule extends Item {

    public ItemModule() {
        super();
        this.setMaxDamage(0);
        this.setHasSubtypes(false);
        this.setUnlocalizedName("module_item");
        this.setTextureName(Constants.TEXTURE_PREFIX + "module_item");
        this.setCreativeTab(ExtraPlanets.ItemsTab);
    }

    /**
     * Creates a module ItemStack with the given module name.
     */
    public static ItemStack createModule(String moduleName) {
        ItemStack stack = new ItemStack(ExtraPlanets_Items.moduleItem, 1, 0);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("module_name", moduleName);
        stack.setTagCompound(nbt);
        return stack;
    }

    /**
     * Creates a module ItemStack with the given module name and tier data.
     */
    public static ItemStack createModule(String moduleName, int data) {
        ItemStack stack = createModule(moduleName);
        stack.getTagCompound()
            .setInteger("module_data", data);
        return stack;
    }

    /**
     * @return the module name stored in the ItemStack's NBT, or null if invalid.
     */
    public static String getModuleName(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemModule)) return null;
        if (!stack.hasTagCompound() || !stack.getTagCompound()
            .hasKey("module_name")) return null;
        return stack.getTagCompound()
            .getString("module_name");
    }

    /**
     * @return the tier data stored in the ItemStack's NBT, defaulting to 0.
     */
    public static int getModuleData(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemModule)) return 0;
        if (!stack.hasTagCompound() || !stack.getTagCompound()
            .hasKey("module_data")) return 0;
        return stack.getTagCompound()
            .getInteger("module_data");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        String moduleName = getModuleName(stack);
        if (moduleName != null) {
            int data = getModuleData(stack);
            list.add("\u00a77Module: \u00a7f" + moduleName);
            if (data > 0) {
                list.add("\u00a77Tier: \u00a7e" + data);
            }
        } else {
            list.add("\u00a78Empty module");
        }
        super.addInformation(stack, player, list, advanced);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String moduleName = getModuleName(stack);
        if (moduleName != null) {
            // Capitalize and replace underscores
            StringBuilder sb = new StringBuilder();
            for (String part : moduleName.split("_")) {
                if (sb.length() > 0) sb.append(" ");
                if (part.length() > 0) {
                    sb.append(Character.toUpperCase(part.charAt(0)));
                    if (part.length() > 1) sb.append(part.substring(1));
                }
            }
            return sb.toString() + " Module";
        }
        return "Blank Module";
    }
}
