package com.mjr.extraplanets.client.gui;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.SimpleGuiFactory;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.scroll.ScrollData;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;
import com.mjr.extraplanets.armor.bases.ElectricArmorBase;
import com.mjr.extraplanets.items.armor.modules.ExtraPlanets_Modules;
import com.mjr.extraplanets.items.armor.modules.Module;
import com.mjr.extraplanets.items.armor.modules.ModuleHelper;

public class GuiModuleManager implements IGuiHolder<GuiData> {

    private static final SimpleGuiFactory FACTORY = new SimpleGuiFactory(
        "extraplanets:module_manager",
        GuiModuleManager::new);

    /**
     * Opens the module GUI. Bypasses the GuiManager's openedContainers guard so the GUI
     * can be reopened from within a server-side handler in the same tick.
     */
    public static void openForPlayer(EntityPlayerMP player) {
        // Remove from GuiManager's openedContainers so the guard doesn't block us
        unguard(player);
        FACTORY.open(player);
    }

    private static final Field GUARD_LIST;
    static {
        Field f = null;
        try {
            f = GuiManager.class.getDeclaredField("openedContainers");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // ignore — openForPlayer will fall back to plain FACTORY.open
        }
        GUARD_LIST = f;
    }

    @SuppressWarnings("unchecked")
    private static void unguard(EntityPlayerMP player) {
        if (GUARD_LIST != null) {
            try {
                List<EntityPlayer> list = (List<EntityPlayer>) GUARD_LIST.get(null);
                list.remove(player);
            } catch (Exception e) {
                // ignore
            }
        }
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

    private static InteractionSyncHandler clickHandler(Runnable action, EntityPlayer player) {
        InteractionSyncHandler handler = new InteractionSyncHandler();
        handler.setOnMouseTapped(mouseData -> {
            action.run();
            if (player instanceof EntityPlayerMP) {
                openForPlayer((EntityPlayerMP) player);
            }
        });
        return handler;
    }

    private static ItemStack getDisplayStack(Module m) {
        List<ItemStack> reqs = m.getRequirements();
        if (reqs != null && !reqs.isEmpty()) {
            return reqs.get(0)
                .copy();
        }
        return null;
    }

    private static ItemStack getDisplayStack(String name) {
        Module m = ExtraPlanets_Modules.getModuleByName(name);
        if (m != null) return getDisplayStack(m);
        return null;
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

        // --- Installed (active) modules ---
        for (Module m : active) {
            final String mName = m.getName();
            final ItemStack displayStack = getDisplayStack(mName);

            InteractionSyncHandler removeHandler = clickHandler(
                () -> {
                    ModuleHelper.uninstallModule(
                        player.getHeldItem(),
                        ExtraPlanets_Modules.getModuleByName(mName)
                            .copy(),
                        player);
                },
                player);

            InteractionSyncHandler toggleHandler = clickHandler(() -> {
                ModuleHelper.updateModuleActiveState(
                    player.getHeldItem(),
                    ExtraPlanets_Modules.getModuleByName(mName)
                        .copy(),
                    false);
            }, player);

            Flow row = new Row().height(18)
                .margin(0, 2);
            row.child(
                new ButtonWidget<>().size(18, 18)
                    .overlay(displayStack != null ? new ItemStackDrawable(displayStack) : IKey.str("?"))
                    .syncHandler(removeHandler));
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
                    .syncHandler(toggleHandler));
            moduleList.child(row);
        }

        // --- Installed (inactive) modules ---
        for (Module m : inactive) {
            final String mName = m.getName();
            final ItemStack displayStack = getDisplayStack(mName);

            InteractionSyncHandler removeHandler = clickHandler(
                () -> {
                    ModuleHelper.uninstallModule(
                        player.getHeldItem(),
                        ExtraPlanets_Modules.getModuleByName(mName)
                            .copy(),
                        player);
                },
                player);

            InteractionSyncHandler toggleHandler = clickHandler(() -> {
                ModuleHelper.updateModuleActiveState(
                    player.getHeldItem(),
                    ExtraPlanets_Modules.getModuleByName(mName)
                        .copy(),
                    true);
            }, player);

            Flow row = new Row().height(18)
                .margin(0, 2);
            row.child(
                new ButtonWidget<>().size(18, 18)
                    .overlay(displayStack != null ? new ItemStackDrawable(displayStack) : IKey.str("?"))
                    .syncHandler(removeHandler));
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
                    .syncHandler(toggleHandler));
            moduleList.child(row);
        }

        // --- Not installed modules ---
        for (Module m : notInstalled) {
            final String mName = m.getName();
            final List<ItemStack> reqs = m.getRequirements();
            final ItemStack reqItem = (reqs != null && !reqs.isEmpty()) ? reqs.get(0) : null;

            StringBuilder tip = new StringBuilder();
            if (reqItem != null) {
                tip.append("Drag ")
                    .append(reqItem.getDisplayName())
                    .append(" here to install");
            } else {
                tip.append("Use /give to obtain a module item");
            }
            final String tooltip = tip.toString();

            ItemStackHandler installHandler = new ItemStackHandler(1);
            ModularSlot installSlot = new ModularSlot(installHandler, 0);
            installSlot.filter(stack -> {
                if (stack == null) return false;
                for (ItemStack req : m.getRequirements()) {
                    if (req.isItemEqual(stack)) return true;
                }
                return false;
            });
            installSlot.changeListener((stack, a, b, c) -> {
                if (stack != null && !player.worldObj.isRemote) {
                    Module mod = m.copy();
                    if (!ModuleHelper.hasModule(player.getHeldItem(), mod)) {
                        ModuleHelper.addModule(player.getHeldItem(), mod);
                        // Clear the phantom handler and server cursor
                        installHandler.setStackInSlot(0, null);
                        player.inventory.setItemStack(null);
                        openForPlayer((EntityPlayerMP) player);
                    }
                }
            });

            Flow row = new Row().height(18)
                .margin(0, 2);
            row.child(
                new PhantomItemSlot().slot(installSlot)
                    .size(18, 18)
                    .addTooltipLine(tooltip));
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
        panel.child(ButtonWidget.panelCloseButton());
        return panel;
    }

    /** Draws an ItemStack on a button. */
    private static class ItemStackDrawable implements IDrawable {

        private final ItemStack stack;
        private static final RenderItem RENDER_ITEM = new RenderItem();

        ItemStackDrawable(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
            if (stack != null) {
                Minecraft mc = Minecraft.getMinecraft();
                RenderHelper.enableGUIStandardItemLighting();
                GuiScreen.drawRect(x, y, x + width, y + height, 0x44FFFFFF);
                int slotX = x + (width - 16) / 2;
                int slotY = y + (height - 16) / 2;
                RENDER_ITEM.renderItemIntoGUI(mc.fontRenderer, mc.getTextureManager(), stack, slotX, slotY);
                RenderHelper.disableStandardItemLighting();
            }
        }
    }
}
