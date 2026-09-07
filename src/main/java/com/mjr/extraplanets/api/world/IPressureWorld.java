package com.mjr.extraplanets.api.world;

/**
 * Extended for world providers that have a pressure hazard level.
 * <p>
 * The returned value represents the atmospheric/environmental pressure hazard
 * on a scale of 0-100, where 0 = no pressure hazard (safe) and 100 = extreme
 * pressure (requires maximum protection).
 * <p>
 * High pressure (gas giants, Venus): the suit must resist compressive forces.<br>
 * Low pressure (vacuum, thin atmospheres): handled inversely — the suit must
 * maintain internal pressure against vacuum.
 */
public interface IPressureWorld {

    public int getPressureLevel();
}
