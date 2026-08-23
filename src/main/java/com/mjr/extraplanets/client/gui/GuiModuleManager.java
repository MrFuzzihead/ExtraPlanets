package com.mjr.extraplanets.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.StatCollector;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.SimpleGuiFactory;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.DoubleValue;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.mjr.extraplanets.armor.bases.ElectricArmorBase;
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
        String key = "gui.module." + name + ".name";
        String translated = StatCollector.translateToLocal(key);
        if (!translated.equals(key)) return translated;
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

    @Override
    public ModularPanel buildUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        final EntityPlayer player = data.getPlayer();
        final int slotType = (player.getHeldItem() != null && player.getHeldItem()
            .getItem() instanceof ItemArmor)
                ? ((ItemArmor) player.getHeldItem()
                    .getItem()).armorType
                : -1;

        ModularPanel panel = ModularPanel.defaultPanel("module_manager", 240, 185);
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
            panel.child(
                new ProgressWidget().value(new DoubleValue(maxE > 0 ? storedE / maxE : 0))
                    .direction(ProgressWidget.Direction.RIGHT)
                    .texture(GuiTextures.PROGRESS_ARROW, 20)
                    .pos(8, 22)
                    .size(120, 10)
                    .background(GuiTextures.SLOT_FLUID)
                    .overlay(
                        IKey.str(String.format("%.0f/%.0f gJ", storedE, maxE))
                            .alignment(Alignment.Center)));
        }

        // Module data
        List<Module> installed = ModuleHelper.getModules(player.getHeldItem());
        List<String> installedNames = new ArrayList<String>();
        for (Module m : installed) installedNames.add(
            m.getName()
                .toLowerCase());

        // Installed column
        panel.child(
            IKey.str("\u00a7nInstalled")
                .asWidget()
                .pos(8, 40));

        Flow installedCol = new Column().pos(8, 52)
            .size(115, 100);
        for (Module m : installed) {
            final boolean active = m.isActive();
            final String mName = m.getName();

            InteractionSyncHandler toggleHandler = new InteractionSyncHandler();
            toggleHandler.setOnMouseTapped(mouseData -> {
                if (player.worldObj.isRemote) return;
                ModuleHelper.updateModuleActiveState(
                    player.getHeldItem(),
                    ExtraPlanets_Modules.getModuleByName(mName)
                        .copy(),
                    !active);
                openForPlayer((EntityPlayerMP) player);
            });

            InteractionSyncHandler removeHandler = new InteractionSyncHandler();
            removeHandler.setOnMouseTapped(mouseData -> {
                if (player.worldObj.isRemote) return;
                ModuleHelper.uninstallModule(
                    player.getHeldItem(),
                    ExtraPlanets_Modules.getModuleByName(mName)
                        .copy(),
                    player);
                openForPlayer((EntityPlayerMP) player);
            });

            installedCol.child(
                new Row().height(14)
                    .margin(0, 1)
                    .child(
                        IKey.str((active ? "\u00a7a" : "\u00a77") + localizeModule(mName))
                            .asWidget()
                            .width(76)
                            .alignment(Alignment.CenterLeft))
                    .child(
                        new ButtonWidget<>().size(22, 12)
                            .overlay(IKey.str(active ? "ON" : "OFF"))
                            .onMouseTapped(b -> false)
                            .syncHandler(toggleHandler))
                    .child(
                        new ButtonWidget<>().size(12, 12)
                            .overlay(GuiTextures.REMOVE)
                            .onMouseTapped(b -> false)
                            .syncHandler(removeHandler)));
        }
        panel.child(installedCol);

        // Available column
        panel.child(
            IKey.str("\u00a7nAvailable")
                .asWidget()
                .pos(130, 40));
        Flow availCol = new Column().pos(130, 52)
            .size(105, 100);
        for (Module m : ExtraPlanets_Modules.getModules()) {
            if (installedNames.contains(
                m.getName()
                    .toLowerCase()))
                continue;
            if (m.getSlotType() != -1 && m.getSlotType() != slotType) continue;
            final String mName = m.getName();

            InteractionSyncHandler installHandler = new InteractionSyncHandler();
            installHandler.setOnMouseTapped(mouseData -> {
                if (player.worldObj.isRemote) return;
                ModuleHelper.installModule(player.getHeldItem(), m.copy(), player);
                openForPlayer((EntityPlayerMP) player);
            });

            availCol.child(
                new Row().height(14)
                    .margin(0, 1)
                    .child(
                        IKey.str(localizeModule(mName))
                            .asWidget()
                            .width(72)
                            .alignment(Alignment.CenterLeft))
                    .child(
                        new ButtonWidget<>().size(14, 12)
                            .overlay(GuiTextures.ADD)
                            .onMouseTapped(b -> false)
                            .syncHandler(installHandler)));
        }
        panel.child(availCol);

        panel.child(ButtonWidget.panelCloseButton());
        return panel;
    }
}
