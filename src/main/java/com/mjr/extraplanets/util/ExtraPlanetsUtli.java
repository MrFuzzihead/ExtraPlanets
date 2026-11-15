package com.mjr.extraplanets.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;

import com.mjr.extraplanets.ExtraPlanets;
import com.mjr.extraplanets.inventory.vehicles.ContainerPoweredVehicleBase;
import com.mjr.extraplanets.inventory.vehicles.ContainerVehicleBase;
import com.mjr.extraplanets.network.PacketSimple;
import com.mjr.extraplanets.network.PacketSimple.EnumSimplePacket;

/**
 * "Utli" is a typo for "Utils". Unlike the misspelled fields elsewhere in this mod this one is *not* part of any save
 * format - it is a class name and nothing persists it - so it could be renamed safely. It has not been, because the
 * rename touches every caller and buys nothing a comment cannot.
 */
public class ExtraPlanetsUtli {

    public static void openFuelVehicleInv(EntityPlayerMP player, IInventory buggyInv, int type) {
        player.getNextWindowId();
        player.closeContainer();
        int id = player.currentWindowId;
        ExtraPlanets.packetPipeline
            .sendTo(new PacketSimple(EnumSimplePacket.C_OPEN_PARACHEST_GUI, new Object[] { id, 0, 0 }), player);
        player.openContainer = new ContainerVehicleBase(player.inventory, buggyInv, type);
        player.openContainer.windowId = id;
        player.openContainer.addCraftingToCrafters(player);
    }

    public static void openPowerVehicleInv(EntityPlayerMP player, IInventory buggyInv, int type) {
        player.getNextWindowId();
        player.closeContainer();
        int id = player.currentWindowId;
        ExtraPlanets.packetPipeline
            .sendTo(new PacketSimple(EnumSimplePacket.C_OPEN_PARACHEST_GUI, new Object[] { id, 0, 0 }), player);
        player.openContainer = new ContainerPoweredVehicleBase(player.inventory, buggyInv, type);
        player.openContainer.windowId = id;
        player.openContainer.addCraftingToCrafters(player);
    }
}
