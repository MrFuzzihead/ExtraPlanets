package com.mjr.extraplanets.network;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.client.gui.vehicles.GuiPoweredVehicleBase;
import com.mjr.extraplanets.client.gui.vehicles.GuiVehicleBase;
import com.mjr.extraplanets.entities.vehicles.EntityPoweredVehicleBase;
import com.mjr.extraplanets.entities.vehicles.EntityVehicleBase;
import com.mjr.extraplanets.util.ExtraPlanetsUtli;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStatsClient;
import micdoodle8.mods.galacticraft.core.network.IPacket;
import micdoodle8.mods.galacticraft.core.network.NetworkUtil;
import micdoodle8.mods.galacticraft.core.util.GCLog;
import micdoodle8.mods.galacticraft.core.util.PlayerUtil;

public class PacketSimple extends Packet implements IPacket {

    public static enum EnumSimplePacket {

        // SERVER
        S_OPEN_FUEL_GUI(Side.SERVER, String.class),
        S_OPEN_POWER_GUI(Side.SERVER, String.class),

        // CLIENT
        C_OPEN_PARACHEST_GUI(Side.CLIENT, Integer.class, Integer.class, Integer.class),
        // P1: server pushes its authoritative Dimension/Biome/Schematic GUI/Page IDs
        C_UPDATE_CONFIGS(Side.CLIENT, Config.getConfigSyncDecodeClasses());

        private Side targetSide;
        private Class<?>[] decodeAs;

        private EnumSimplePacket(Side targetSide, Class<?>... decodeAs) {
            this.targetSide = targetSide;
            this.decodeAs = decodeAs;
        }

        public Side getTargetSide() {
            return this.targetSide;
        }

        public Class<?>[] getDecodeClasses() {
            return this.decodeAs;
        }
    }

    private EnumSimplePacket type;
    private List<Object> data;

    public PacketSimple() {}

    public PacketSimple(EnumSimplePacket packetType, Object[] data) {
        this(packetType, Arrays.asList(data));
    }

    public PacketSimple(EnumSimplePacket packetType, List<Object> data) {
        if (packetType.getDecodeClasses().length != data.size()) {
            GCLog.info("[ExtraPlanets] Simple Packet Core found data length different than packet type");
            new RuntimeException().printStackTrace();
        }

        this.type = packetType;
        this.data = data;
    }

    @Override
    public void encodeInto(ChannelHandlerContext context, ByteBuf buffer) {
        buffer.writeInt(this.type.ordinal());

        try {
            NetworkUtil.encodeData(buffer, this.data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext context, ByteBuf buffer) {
        if (buffer.readableBytes() < 4) {
            GCLog.severe(
                "[ExtraPlanets] Received a simple packet too short to contain a packet type. Discarding it.");
            return;
        }
        int typeOrdinal = buffer.readInt();
        EnumSimplePacket[] types = EnumSimplePacket.values();
        if (typeOrdinal < 0 || typeOrdinal >= types.length) {
            GCLog.severe(
                "[ExtraPlanets] Received a simple packet with invalid type ordinal " + typeOrdinal
                    + " (valid range is 0-" + (types.length - 1) + "). Discarding the malformed packet.");
            return;
        }
        this.type = types[typeOrdinal];

        try {
            if (this.type.getDecodeClasses().length > 0) {
                this.data = NetworkUtil.decodeData(this.type.getDecodeClasses(), buffer);
            }
            if (buffer.readableBytes() > 0) {
                GCLog.severe("ExtraPlanets packet length problem for packet type " + this.type.toString());
            }
        } catch (Exception e) {
            System.err.println(
                "[ExtraPlanets] Error handling simple packet type: " + this.type.toString() + " " + buffer.toString());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    @SideOnly(Side.CLIENT)
    @Override
    public void handleClientSide(EntityPlayer player) {
        if (this.type == null) return; // a malformed packet was discarded during decoding
        EntityClientPlayerMP playerBaseClient = null;
        GCPlayerStatsClient stats = null;

        if (player instanceof EntityClientPlayerMP) {
            playerBaseClient = (EntityClientPlayerMP) player;
            stats = GCPlayerStatsClient.get(playerBaseClient);
        }

        switch (this.type) {
            case C_OPEN_PARACHEST_GUI:
                switch ((Integer) this.data.get(1)) {
                    case 0:
                        if (player.ridingEntity instanceof EntityVehicleBase) {
                            FMLClientHandler.instance()
                                .getClient()
                                .displayGuiScreen(
                                    new GuiVehicleBase(
                                        player.inventory,
                                        (EntityVehicleBase) player.ridingEntity,
                                        ((EntityVehicleBase) player.ridingEntity).getType()));
                            player.openContainer.windowId = (Integer) this.data.get(0);
                        } else if (player.ridingEntity instanceof EntityPoweredVehicleBase) {
                            FMLClientHandler.instance()
                                .getClient()
                                .displayGuiScreen(
                                    new GuiPoweredVehicleBase(
                                        player.inventory,
                                        (EntityPoweredVehicleBase) player.ridingEntity,
                                        ((EntityPoweredVehicleBase) player.ridingEntity).getType()));
                            player.openContainer.windowId = (Integer) this.data.get(0);
                        }
                        break;
                }
                break;
            case C_UPDATE_CONFIGS:
                Config.saveClientConfigOverrideable();
                Config.setConfigOverride(this.data);
                break;
            default:
                break;
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player) {
        if (this.type == null) return; // a malformed packet was discarded during decoding
        EntityPlayerMP playerBase = PlayerUtil.getPlayerBaseServerFromPlayer(player, false);

        if (playerBase == null) {
            return;
        }

        switch (this.type) {
            case S_OPEN_FUEL_GUI:
                if (player.ridingEntity instanceof EntityVehicleBase) {
                    ExtraPlanetsUtli.openFuelVehicleInv(
                        playerBase,
                        (EntityVehicleBase) player.ridingEntity,
                        ((EntityVehicleBase) player.ridingEntity).getType());
                }
                break;
            case S_OPEN_POWER_GUI:
                if (player.ridingEntity instanceof EntityPoweredVehicleBase) {
                    ExtraPlanetsUtli.openPowerVehicleInv(
                        playerBase,
                        (EntityPoweredVehicleBase) player.ridingEntity,
                        ((EntityPoweredVehicleBase) player.ridingEntity).getType());
                }
                break;
            default:
                break;
        }
    }

    /*
     * BEGIN "net.minecraft.network.Packet" IMPLEMENTATION
     * This is for handling server->client packets before the player has joined the world
     */

    @Override
    public void readPacketData(PacketBuffer var1) {
        this.decodeInto(null, var1);
    }

    @Override
    public void writePacketData(PacketBuffer var1) {
        this.encodeInto(null, var1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void processPacket(INetHandler var1) {
        if (FMLCommonHandler.instance()
            .getEffectiveSide() == Side.CLIENT) {
            this.handleClientSide(
                FMLClientHandler.instance()
                    .getClientPlayerEntity());
        }
    }

    /*
     * END "net.minecraft.network.Packet" IMPLEMENTATION
     * This is for handling server->client packets before the player has joined the world
     */
}
