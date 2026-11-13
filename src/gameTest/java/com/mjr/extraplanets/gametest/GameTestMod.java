package com.mjr.extraplanets.gametest;

import net.minecraft.server.MinecraftServer;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;

/**
 * Test-only mod: a separate, never-shipped {@code @Mod} that asserts ExtraPlanets works against the real game.
 *
 * <p>
 * It lives in the {@code gameTest} source set, which is only added to the run classpath by
 * {@code ./gradlew gameTestServer} (or {@code -PgameTest}), so it can never reach the published jar.
 * </p>
 *
 * <pre>
 * echo "eula=true" &gt; run/eula.txt
 * ./gradlew gameTestServer
 * </pre>
 *
 * <p>
 * The assertions run on the server thread at {@link FMLServerStartedEvent}: that is the earliest point where the
 * block/item/tile-entity registries, the ore dictionary and the recipe managers are all final, and where a world
 * exists. They do not tick the world, so no chunk is loaded and no extra worldgen is triggered by the test itself.
 * </p>
 *
 * <p>
 * The result is reported as a marker line ({@value #OK_MARKER} / {@value #FAIL_MARKER}) and the JVM exits with a
 * matching status, so a CI job can boot a real dedicated server, run the checks and fail loudly. If the mod under
 * test fails to load at all, the server never reaches this point and the job times out - which is also a failure.
 * </p>
 */
@Mod(
    modid = GameTestMod.MOD_ID,
    name = "ExtraPlanets Game Tests",
    version = "1.0",
    acceptableRemoteVersions = "*",
    dependencies = "required-after:ExtraPlanets")
public class GameTestMod {

    public static final String MOD_ID = "extraplanetsgametest";
    static final String OK_MARKER = "EXTRAPLANETS_GAMETEST_OK";
    static final String FAIL_MARKER = "EXTRAPLANETS_GAMETEST_FAILED";

    private boolean ran;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        System.out.println("[EP-GAMETEST] test mod loaded; assertions run at server start");
    }

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        if (ran) return;
        ran = true;

        if (MinecraftServer.getServer() == null) {
            System.out.println(FAIL_MARKER + ": no server instance at FMLServerStartedEvent");
            exit(1);
            return;
        }

        int failures;
        try {
            failures = ServerGameTests.runAll();
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println(FAIL_MARKER + ": " + t);
            exit(1);
            return;
        }

        if (failures > 0) {
            System.out.println(FAIL_MARKER + ": " + failures + " check(s) failed, see the PASS/FAIL lines above");
            exit(1);
        } else {
            System.out.println(OK_MARKER);
            exit(0);
        }
    }

    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        // Nothing to clean up: the test never changes world state.
    }

    private static void exit(int status) {
        FMLCommonHandler.instance()
            .exitJava(status, false);
    }
}
