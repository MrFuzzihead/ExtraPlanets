package com.mjr.extraplanets.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import com.mjr.extraplanets.handlers.MainHandler;

/**
 * Debug command for the radiation system.
 * Usage:
 * /ep getradiation — shows current accumulated radiation
 * /ep setradiation <amt> — sets radiation level (0-100)
 */
public class CommandEP extends CommandBase {

    @Override
    public String getCommandName() {
        return "ep";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/ep <getradiation|setradiation> [amount]";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            return;
        }
        if (!(sender instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) sender;

        if (args[0].equalsIgnoreCase("getradiation")) {
            double level = MainHandler.getRadiationLevel(player);
            player.addChatMessage(
                new ChatComponentText("\u00a7eCurrent radiation level: \u00a7f" + String.format("%.1f", level) + "%"));
        } else if (args[0].equalsIgnoreCase("setradiation")) {
            if (args.length < 2) {
                player.addChatMessage(new ChatComponentText("\u00a7cUsage: /ep setradiation <amount>"));
                return;
            }
            try {
                double amount = Double.parseDouble(args[1]);
                MainHandler.setRadiationLevel(player, amount);
                player.addChatMessage(
                    new ChatComponentText(
                        "\u00a7eRadiation level set to: \u00a7f" + String.format("%.1f", amount) + "%"));
            } catch (NumberFormatException e) {
                player.addChatMessage(new ChatComponentText("\u00a7cInvalid number: " + args[1]));
            }
        }
    }
}
