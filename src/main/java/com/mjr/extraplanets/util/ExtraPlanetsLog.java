package com.mjr.extraplanets.util;

import org.apache.logging.log4j.Level;

import com.mjr.extraplanets.Config;
import com.mjr.extraplanets.Constants;

import cpw.mods.fml.relauncher.FMLRelaunchLog;

/**
 * The mod's own log front end.
 *
 * <p>
 * {@code GCLog} is fine for a one-line message but has no {@code (String, Throwable)} overload, so every
 * {@code catch} block in this mod used {@code e.printStackTrace()} instead - which writes to {@code System.err},
 * bypassing the log file, the log level and the log formatter. A crash report from a user then contains no trace at
 * all. These helpers log through Forge's relaunch log (the same sink {@code GCLog} uses, under this mod's own name)
 * and accept the throwable so the stack trace lands in {@code logs/fml-server-latest.log} at the right level.
 * </p>
 *
 * <p>
 * Rules this class exists to enforce (see {@code CODE_REVIEW.md} §6.2):
 * </p>
 * <ul>
 * <li>Never call {@code printStackTrace()}; log the throwable instead.</li>
 * <li>Never write to {@code System.out}/{@code System.err} from game code; use one of these methods. The only
 * permitted exception is {@code SmokeTestHook}, whose stdout marker is a CI contract.</li>
 * <li>Anything that would be noisy per-tick goes through {@link #debug(String)}, which is gated on
 * {@code Config.debugMode}.</li>
 * </ul>
 */
public final class ExtraPlanetsLog {

    /** Forge resolves the log target by mod id, so this puts our lines under "ExtraPlanets". */
    private static final String TARGET = Constants.modID;

    private ExtraPlanetsLog() {}

    public static void info(String message) {
        FMLRelaunchLog.log(TARGET, Level.INFO, message);
    }

    public static void warn(String message) {
        FMLRelaunchLog.log(TARGET, Level.WARN, message);
    }

    public static void severe(String message) {
        FMLRelaunchLog.log(TARGET, Level.ERROR, message);
    }

    /** Logs an error with the throwable's stack trace attached. */
    public static void severe(String message, Throwable throwable) {
        FMLRelaunchLog.log(TARGET, Level.ERROR, throwable, message);
    }

    public static void warn(String message, Throwable throwable) {
        FMLRelaunchLog.log(TARGET, Level.WARN, throwable, message);
    }

    /** Development-only logging; silently discarded unless {@code B:debugMode} is enabled in the config. */
    public static void debug(String message) {
        if (Config.debugMode) {
            FMLRelaunchLog.log(TARGET, Level.INFO, "Debug: " + message);
        }
    }
}
