package com.mjr.extraplanets.client.gui;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.SimpleGuiFactory;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.scroll.ScrollData;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.mjr.extraplanets.armor.bases.ElectricArmorBase;
import com.mjr.extraplanets.items.ItemModule;
import com.mjr.extraplanets.items.armor.modules.ExtraPlanets_Modules;
import com.mjr.extraplanets.items.armor.modules.Module;
import com.mjr.extraplanets.items.armor.modules.ModuleHelper;

public class GuiModuleManager implements IGuiHolder<GuiData> {

    private static final SimpleGuiFactory FACTORY = new SimpleGuiFactory(
        "extraplanets:module_manager",
        GuiModuleManager::new);

    public static void openForPlayer(EntityPlayerMP player) {
        FACTORY.open(player);
    }

    private static String localizeModule(String name) {
        StringBuilder sb = new StringBuilder();
        for (String part : name.split("_")) {
            if (sb.length() > 0) sb.append(" ");
            if (part.length() > 0) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    private static InteractionSyncHandler makeHandler(Runnable action, EntityPlayer player) {
        InteractionSyncHandler handler = new InteractionSyncHandler();
        handler.setOnMouseTapped(mouseData -> {
            action.run();
            if (player instanceof EntityPlayerMP) {
                openForPlayer((EntityPlayerMP) player);
            }
        });
        return handler;
    }

    @Override
    public ModularPanel buildUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        final EntityPlayer player = data.getPlayer();
        final int slotType = (player.getHeldItem() != null && player.getHeldItem()
            .getItem() instanceof ItemArmor)
                ? ((ItemArmor) player.getHeldItem()
                    .getItem()).armorType
                : -1;

        ModularPanel panel = ModularPanel.defaultPanel("module_manager", 240, 225);
        panel.bindPlayerInventory();

        // Title
        panel.child(
            IKey.str(
                player.getHeldItem()
                    .getDisplayName())
                .asWidget()
                .pos(8, 6)
                .color(0xFF404040));

        // Energy bar
        if (player.getHeldItem()
            .getItem() instanceof ElectricArmorBase) {
            ElectricArmorBase electric = (ElectricArmorBase) player.getHeldItem()
                .getItem();
            final float maxE = electric.getMaxElectricityStored(player.getHeldItem());
            final float storedE = electric.getElectricityStored(player.getHeldItem());
            float drain = 0.25F / 9F * 20F;
            for (Module mod : ModuleHelper.getModules(player.getHeldItem())) {
                if (mod.isActive()) drain += mod.getPassivePowerCost();
            }
            final float d = drain;
            panel.child(
                IKey.str(String.format("\u00a7e%.0f\u00a77/\u00a7e%.0f\u00a77 gJ  \u00a78~%.1f gJ/s", storedE, maxE, d))
                    .asWidget()
                    .pos(8, 22));
        }

        // Module data
        List<Module> installed = ModuleHelper.getModules(player.getHeldItem());
        final List<String> installedNames = new java.util.ArrayList<String>();
        for (Module m : installed) installedNames.add(
            m.getName()
                .toLowerCase());

        // Consolidated module list — sorted: active > installed-off > not-installed
        panel.child(
            IKey.str("\u00a7nModules")
                .asWidget()
                .pos(8, 40));

        ListWidget moduleList = new ListWidget();
        moduleList.pos(8, 52)
            .size(224, 85);
        moduleList.scrollDirection(ScrollData.of(GuiAxis.Y));

        // Build sorted list
        java.util.List<Module> active = new java.util.ArrayList<Module>();
        java.util.List<Module> inactive = new java.util.ArrayList<Module>();
        java.util.List<Module> notInstalled = new java.util.ArrayList<Module>();

        for (Module m : installed) {
            if (m.isActive()) active.add(m);
            else inactive.add(m);
        }
        for (Module m : ExtraPlanets_Modules.getModules()) {
            if (!installedNames.contains(
                m.getName()
                    .toLowerCase())) {
                if (m.getSlotType() == -1 || m.getSlotType() == slotType) {
                    notInstalled.add(m);
                }
            }
        }

        // Active modules
        for (Module m : active) {
            final String mName = m.getName();
            final ItemStack moduleStack = ItemModule.createModule(mName);

            ItemStackHandler slotHandler = new ItemStackHandler(1);
            slotHandler.setStackInSlot(0, moduleStack.copy());
            ModularSlot modSlot = new ModularSlot(slotHandler, 0);
            modSlot.accessibility(true, true);
            modSlot.changeListener((stack, a, b, c) -> {
                if (stack == null && !player.worldObj.isRemote) {
                    ModuleHelper.uninstallModule(
                        player.getHeldItem(),
                        ExtraPlanets_Modules.getModuleByName(mName)
                            .copy(),
                        player);
                }
            });

            InteractionSyncHandler toggleHandler = makeHandler(
                () -> ModuleHelper.updateModuleActiveState(
                    player.getHeldItem(),
                    ExtraPlanets_Modules.getModuleByName(mName)
                        .copy(),
                    false),
                player);

            Flow row = new Row().height(18)
                .margin(0, 2);
            row.child(
                new ItemSlot().slot(modSlot)
                    .size(18, 18));
            row.child(
                IKey.str(" ")
                    .asWidget()
                    .width(4));
            row.child(
                IKey.str("\u00a7a" + localizeModule(mName))
                    .asWidget()
                    .width(154)
                    .alignment(Alignment.CenterLeft));
            row.child(
                IKey.str(" ")
                    .asWidget()
                    .width(2));
            row.child(
                new ButtonWidget<>().size(22, 12)
                    .overlay(IKey.str("ON"))
                    .onMouseTapped(b -> false)
                    .syncHandler(toggleHandler));
            moduleList.child(row);
        }

        // Installed but inactive modules
        for (Module m : inactive) {
            final String mName = m.getName();
            final ItemStack moduleStack = ItemModule.createModule(mName);

            ItemStackHandler slotHandler = new ItemStackHandler(1);
            slotHandler.setStackInSlot(0, moduleStack.copy());
            ModularSlot modSlot = new ModularSlot(slotHandler, 0);
            modSlot.accessibility(true, true);
            modSlot.changeListener((stack, a, b, c) -> {
                if (stack == null && !player.worldObj.isRemote) {
                    ModuleHelper.uninstallModule(
                        player.getHeldItem(),
                        ExtraPlanets_Modules.getModuleByName(mName)
                            .copy(),
                        player);
                }
            });

            InteractionSyncHandler toggleHandler = makeHandler(
                () -> ModuleHelper.updateModuleActiveState(
                    player.getHeldItem(),
                    ExtraPlanets_Modules.getModuleByName(mName)
                        .copy(),
                    true),
                player);

            Flow row = new Row().height(18)
                .margin(0, 2);
            row.child(
                new ItemSlot().slot(modSlot)
                    .size(18, 18));
            row.child(
                IKey.str(" ")
                    .asWidget()
                    .width(4));
            row.child(
                IKey.str("\u00a77" + localizeModule(mName))
                    .asWidget()
                    .width(154)
                    .alignment(Alignment.CenterLeft));
            row.child(
                IKey.str(" ")
                    .asWidget()
                    .width(2));
            row.child(
                new ButtonWidget<>().size(22, 12)
                    .overlay(IKey.str("OFF"))
                    .onMouseTapped(b -> false)
                    .syncHandler(toggleHandler));
            moduleList.child(row);
        }

        // Not installed modules — functional slot: drop a module item in to install
        for (Module m : notInstalled) {
            final String mName = m.getName();
            final int modSlotType = m.getSlotType();

            StringBuilder tip = new StringBuilder();
            List<ItemStack> reqs = m.getRequirements();
            if (reqs != null && !reqs.isEmpty()) {
                ItemStack r = reqs.get(0);
                tip.append("Drop ")
                    .append(r.getDisplayName())
                    .append(" here to install");
            } else {
                tip.append("Use /give to obtain a module item");
            }
            final String tooltip = tip.toString();

            ItemStackHandler installHandler = new ItemStackHandler(1);
            ModularSlot installSlot = new ModularSlot(installHandler, 0);
            installSlot.filter(stack -> {
                if (stack == null) return false;
                // Accept matching ItemModule items
                if (stack.getItem() instanceof ItemModule) {
                    String name = ItemModule.getModuleName(stack);
                    return mName.equalsIgnoreCase(name);
                }
                // Accept vanilla items that match the module's crafting requirements
                for (ItemStack req : m.getRequirements()) {
                    if (req.isItemEqual(stack)) return true;
                }
                return false;
            });
            installSlot.changeListener((stack, a, b, c) -> {
                if (stack != null && !player.worldObj.isRemote) {
                    // Check if this is a module item
                    if (stack.getItem() instanceof ItemModule) {
                        String name = ItemModule.getModuleName(stack);
                        int moduleData = ItemModule.getModuleData(stack);
                        if (name != null && mName.equalsIgnoreCase(name)) {
                            Module mod = m.copy();
                            if (moduleData > 0) mod.setSubType(moduleData);
                            if (!ModuleHelper.hasModule(player.getHeldItem(), mod)) {
                                ModuleHelper.addModule(player.getHeldItem(), mod);
                                installHandler.setStackInSlot(0, null);
                            }
                        }
                    } else {
                        // Vanilla crafting item — check if it matches a requirement
                        for (ItemStack req : m.getRequirements()) {
                            if (req.isItemEqual(stack)) {
                                Module mod = m.copy();
                                if (!ModuleHelper.hasModule(player.getHeldItem(), mod)) {
                                    ModuleHelper.addModule(player.getHeldItem(), mod);
                                    installHandler.setStackInSlot(0, null);
                                }
                                break;
                            }
                        }
                    }
                }
            });

            Flow row = new Row().height(18)
                .margin(0, 2);
            row.child(
                new ItemSlot().slot(installSlot)
                    .size(18, 18));
            row.child(
                IKey.str(" ")
                    .asWidget()
                    .width(4));
            row.child(
                IKey.str("\u00a78" + localizeModule(mName))
                    .asWidget()
                    .width(182)
                    .alignment(Alignment.CenterLeft)
                    .addTooltipLine(tooltip));
            moduleList.child(row);
        }

        panel.child(moduleList);

        // Close button
        panel.child(ButtonWidget.panelCloseButton());
        return panel;
    }
}
