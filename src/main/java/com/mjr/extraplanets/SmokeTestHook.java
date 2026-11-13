package com.mjr.extraplanets;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;

/**
 * Development/CI only. When the {@code extraplanets.smokeTest} system property is set, the game logs a
 * machine-readable marker once the overworld has loaded and then exits. This lets a CI job boot the real dedicated
 * server (or the client) without an operator having to type {@code stop}.
 *
 * <p>
 * The hook is a no-op in normal gameplay: the property is only set by the {@code -PsmokeTest} Gradle property (see
 * {@code addon.gradle.kts}).
 * </p>
 *
 * <p>
 * It exits at world load rather than at load-complete: on a dedicated server, load-complete fires before the spawn
 * chunk has been generated, and exiting there races the server's "Done" line and hides worldgen crashes.
 * </p>
 */
public final class SmokeTestHook {

    public static final String ENABLE_PROPERTY = "extraplanets.smokeTest";
    public static final String DELAY_PROPERTY = "extraplanets.smokeTest.delay";
    public static final String SUCCESS_MARKER = "EXTRAPLANETS_SMOKE_TEST_OK";

    private static final CountDownLatch WORLD_LOADED = new CountDownLatch(1);

    private static volatile boolean scheduled;

    private SmokeTestHook() {}

    /**
     * Called from {@link ExtraPlanets#loadComplete(cpw.mods.fml.common.event.FMLLoadCompleteEvent)}. Schedules the exit
     * on a daemon thread.
     */
    public static void scheduleExitIfRequested() {
        if (!Boolean.getBoolean(ENABLE_PROPERTY)) return;
        synchronized (SmokeTestHook.class) {
            if (scheduled) return;
            scheduled = true;
        }

        final boolean serverSide = isServerSide();
        if (serverSide) MinecraftForge.EVENT_BUS.register(new WorldListener());

        final long delay = Long.getLong(DELAY_PROPERTY, 2000L);
        final Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (serverSide) {
                        // A generous window: a missing world should fail the CI job through its own timeout rather
                        // than exit early and mask the real problem.
                        WORLD_LOADED.await(10, TimeUnit.MINUTES);
                    }
                    Thread.sleep(Math.max(0L, delay));
                } catch (InterruptedException e) {
                    Thread.currentThread()
                        .interrupt();
                    return;
                }

                System.out.println(SUCCESS_MARKER + ": FML load complete and a world is loaded, shutting down");
                FMLCommonHandler.instance()
                    .exitJava(0, false);
            }
        }, Constants.modName + " smoke test");
        thread.setDaemon(true);
        thread.start();
    }

    /** @return true when one of the CI boot checks is running. */
    public static boolean isEnabled() {
        return Boolean.getBoolean(ENABLE_PROPERTY);
    }

    private static boolean isServerSide() {
        try {
            return FMLCommonHandler.instance()
                .getSide() == Side.SERVER;
        } catch (Throwable t) {
            // Outside FML (unit tests, early class loading): treat as a client so no latch wait is introduced.
            return false;
        }
    }

    /** Releases {@link #WORLD_LOADED} the first time a world is loaded. */
    public static final class WorldListener {

        private WorldListener() {}

        @SubscribeEvent
        public void onWorldLoad(WorldEvent.Load event) {
            WORLD_LOADED.countDown();
        }
    }
}
