package com.mjr.extraplanets.proxy;

import net.minecraft.world.World;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {}

    public void init(FMLInitializationEvent event) {}

    public void postInit(FMLPostInitializationEvent event) {}

    /**
     * The world the local client is in, or null on a dedicated server.
     *
     * <p>
     * Tooltips are the one place common code used to reach for the client directly: several item classes built a
     * throw-away {@code FMLClientHandler.instance().getWorldClient()} world to read a tank capacity for a tooltip
     * line. A tooltip is never shown on a dedicated server, but the reference still sat in a class the server loads.
     * Routing it through here keeps {@code FMLClientHandler} out of the common class entirely.
     * </p>
     */
    public World getClientWorld() {
        return null;
    }

    /**
     * The display name of the sneak key, for the "hold X for more info" tooltip line.
     *
     * <p>
     * Same reason as {@link #getClientWorld()}: the client resolves it from the actual key binding, and the dedicated
     * server falls back to the untranslated default, which is never displayed there.
     * </p>
     */
    public String getSneakKeyName() {
        return "Shift";
    }
}
