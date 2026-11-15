package com.mjr.extraplanets;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import cpw.mods.fml.relauncher.FMLInjectionData;

/**
 * Test helper: fills in the static state FML normally provides at launch, so that Forge classes which read it can be
 * used in a plain JVM.
 *
 * <p>
 * {@code net.minecraftforge.common.config.Configuration}'s constructor derives a relative path from
 * {@code FMLInjectionData.data()[6]} - the Minecraft home directory - which is {@code null} outside a launch handler
 * and throws a {@link NullPointerException}. Installing a temporary directory here makes {@link Config#init()} (and
 * therefore the whole config file) testable headless.
 * </p>
 *
 * <p>
 * This only stubs the injection data; it does not start FML, so {@code GameRegistry} and everything else that needs
 * the mod loader is still unavailable (that is what the in-game {@code gameTest} source set is for).
 * </p>
 */
public final class FmlTestEnvironment {

    private static boolean installed;

    private FmlTestEnvironment() {}

    /** Installs the fake injection data once per JVM. */
    public static synchronized void install() {
        if (installed) return;

        File home = new File("minecraft-home");
        home.mkdirs();

        set("major", "1");
        set("minor", "7");
        set("rev", "10");
        set("build", "0");
        set("mccversion", "1.7.10");
        set("mcpversion", "stable_12");
        set("minecraftHome", home);
        set("deobfuscationDataHash", "0000000000000000");

        if (FMLInjectionData.containers == null) FMLInjectionData.containers = new ArrayList<String>();

        installed = true;
    }

    /** @return true once {@link #install()} has run. */
    public static boolean isInstalled() {
        return installed;
    }

    private static void set(String fieldName, Object value) {
        try {
            Field field = FMLInjectionData.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot stub FMLInjectionData." + fieldName, e);
        }
    }
}
