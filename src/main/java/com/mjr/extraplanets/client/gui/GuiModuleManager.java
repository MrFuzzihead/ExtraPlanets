package com.mjr.extraplanets.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.StatCollector;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.SimpleGuiFactory;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.scroll.ScrollData;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
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

        ModularPanel panel = ModularPanel.defaultPanel("module_manager", 240, 200);
        panel.bindPlayerInventory();

        // Title
        panel.child(
            IKey.str(
                player.getHeldItem()
                    .getDisplayName())
                .asWidget()
                .pos(8, 6)
                .color(0xFF404040));

        // Energy bar + usage estimate
        if (player.getHeldItem()
            .getItem() instanceof ElectricArmorBase) {
            ElectricArmorBase electric = (ElectricArmorBase) player.getHeldItem()
                .getItem();
            final float maxE = electric.getMaxElectricityStored(player.getHeldItem());
            final float storedE = electric.getElectricityStored(player.getHeldItem());
            float drainPerSec = 0.25F / 9F * 20F;
            for (Module mod : ModuleHelper.getModules(player.getHeldItem())) {
                if (mod.isActive()) drainPerSec += mod.getPassivePowerCost();
            }
            final float drain = drainPerSec;
            panel.child(
                IKey.str(
                    String.format("\u00a7e%.0f\u00a77/\u00a7e%.0f\u00a77 gJ  \u00a78~%.1f gJ/s", storedE, maxE, drain))
                    .asWidget()
                    .pos(8, 22));
        }

        // Module data
        List<Module> installed = ModuleHelper.getModules(player.getHeldItem());
        final List<String> installedNames = new ArrayList<String>();
        for (Module m : installed) installedNames.add(
            m.getName()
                .toLowerCase());

        // Installed list
        panel.child(
            IKey.str("\u00a7nInstalled")
                .asWidget()
                .pos(8, 40));
        ListWidget installedList = new ListWidget();
        installedList.pos(8, 52)
            .size(115, 60);
        installedList.scrollDirection(ScrollData.of(GuiAxis.Y));
        for (Module m : installed) {
            final boolean active = m.isActive();
            final String mName = m.getName();

            InteractionSyncHandler toggleHandler = makeHandler(
                () -> ModuleHelper.updateModuleActiveState(
                    player.getHeldItem(),
                    ExtraPlanets_Modules.getModuleByName(mName)
                        .copy(),
                    !active),
                player);
            InteractionSyncHandler removeHandler = makeHandler(
                () -> ModuleHelper.uninstallModule(
                    player.getHeldItem(),
                    ExtraPlanets_Modules.getModuleByName(mName)
                        .copy(),
                    player),
                player);

            Flow row = new Row().height(16)
                .margin(0, 2);
            row.child(
                IKey.str((active ? "\u00a7a" : "\u00a77") + localizeModule(mName))
                    .asWidget()
                    .width(72)
                    .alignment(Alignment.CenterLeft));
            row.child(
                new ButtonWidget<>().size(22, 12)
                    .overlay(IKey.str(active ? "ON" : "OFF"))
                    .onMouseTapped(b -> {
                        ModuleHelper.updateModuleActiveState(
                            player.getHeldItem(),
                            ExtraPlanets_Modules.getModuleByName(mName)
                                .copy(),
                            !active);
                        return false;
                    })
                    .syncHandler(toggleHandler));
            row.child(
                new ButtonWidget<>().size(12, 12)
                    .overlay(GuiTextures.REMOVE)
                    .onMouseTapped(b -> {
                        ModuleHelper.uninstallModule(
                            player.getHeldItem(),
                            ExtraPlanets_Modules.getModuleByName(mName)
                                .copy(),
                            player);
                        return false;
                    })
                    .syncHandler(removeHandler));
            installedList.child(row);
        }
        panel.child(installedList);

        // Available list
        panel.child(
            IKey.str("\u00a7nAvailable")
                .asWidget()
                .pos(130, 40));
        ListWidget availList = new ListWidget();
        availList.pos(130, 52)
            .size(105, 60);
        availList.scrollDirection(ScrollData.of(GuiAxis.Y));
        for (Module m : ExtraPlanets_Modules.getModules()) {
            if (installedNames.contains(
                m.getName()
                    .toLowerCase()))
                continue;
            if (m.getSlotType() != -1 && m.getSlotType() != slotType) continue;
            final String mName = m.getName();

            InteractionSyncHandler installHandler = makeHandler(
                () -> ModuleHelper.installModule(player.getHeldItem(), m.copy(), player),
                player);

            Flow row = new Row().height(16)
                .margin(0, 2);
            row.child(
                IKey.str(localizeModule(mName))
                    .asWidget()
                    .width(68)
                    .alignment(Alignment.CenterLeft));
            row.child(
                new ButtonWidget<>().size(14, 12)
                    .overlay(GuiTextures.ADD)
                    .onMouseTapped(b -> {
                        ModuleHelper.installModule(player.getHeldItem(), m.copy(), player);
                        return false;
                    })
                    .syncHandler(installHandler));
            availList.child(row);
        }
        panel.child(availList);

        panel.child(ButtonWidget.panelCloseButton());
        return panel;
    }
}
