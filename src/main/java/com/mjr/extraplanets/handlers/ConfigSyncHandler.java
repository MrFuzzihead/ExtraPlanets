package com.mjr.extraplanets.handlers;

import net.minecraft.entity.player.EntityPlayerMP;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.ExtraPlanets;
import com.mjr.extraplanets.network.PacketSimple;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

/**
 * Keeps client/server config in sync (P1 - IDs only).
 *
 * Server side: when a player logs in, push this server's authoritative dimension /
 * space-station / biome / schematic GUI/page IDs to that player.
 *
 * Client side: when the player later leaves a <em>remote</em> server, restore the
 * client's own locally-configured values so connecting to a different server starts
 * from a clean state. Single-player (integrated) servers never override, so nothing
 * is restored for them.
 */
public class ConfigSyncHandler {

    private static boolean clientConnectedToRemoteServer = false;

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            ExtraPlanets.packetPipeline.sendTo(
                new PacketSimple(PacketSimple.EnumSimplePacket.C_UPDATE_CONFIGS, Config.getServerConfigOverride()),
                (EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public void onConnectionOpened(ClientConnectedToServerEvent event) {
        if (!event.isLocal) {
            ConfigSyncHandler.clientConnectedToRemoteServer = true;
        }
    }

    @SubscribeEvent
    public void onConnectionClosed(ClientDisconnectionFromServerEvent event) {
        if (ConfigSyncHandler.clientConnectedToRemoteServer) {
            ConfigSyncHandler.clientConnectedToRemoteServer = false;
            Config.restoreClientConfigOverrideable();
        }
    }
}