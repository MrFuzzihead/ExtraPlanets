package com.mjr.extraplanets.planets.Jupiter;

import org.junit.Assert;
import org.junit.Test;

/**
 * Covers the throttle that decides when the server spawns a Jupiter bolt.
 *
 * <p>
 * The rest of {@link JupiterStorms} needs a real world (it places an entity), but this part is a pure function, and it
 * is the part with the interesting conditions: the config key, the dimension, the interval and the roll. Without it
 * the server hook could only be tested by loading the Jupiter dimension.
 * </p>
 */
public class JupiterStormsTest {

    private static final long ON_INTERVAL = JupiterStorms.CHECK_INTERVAL * 3L;
    private static final long OFF_INTERVAL = ON_INTERVAL + 1;

    @Test
    public void aStrikeHappensOnTheIntervalWhenEverythingLinesUp() {
        Assert.assertTrue(JupiterStorms.shouldStrike(ON_INTERVAL, true, true, 0));
    }

    @Test
    public void theConfigKeyTurnsItOff() {
        Assert.assertFalse(JupiterStorms.shouldStrike(ON_INTERVAL, false, true, 0));
    }

    @Test
    public void anotherWorldNeverStrikes() {
        Assert.assertFalse(JupiterStorms.shouldStrike(ON_INTERVAL, true, false, 0));
    }

    @Test
    public void ticksBetweenChecksAreSilent() {
        Assert.assertFalse(JupiterStorms.shouldStrike(OFF_INTERVAL, true, true, 0));
        for (long tick = ON_INTERVAL + 1; tick < ON_INTERVAL + JupiterStorms.CHECK_INTERVAL; tick++) {
            Assert.assertFalse("tick " + tick, JupiterStorms.shouldStrike(tick, true, true, 0));
        }
    }

    @Test
    public void theRollThinsTheStrikesOut() {
        for (int roll = 1; roll < JupiterStorms.ONE_IN; roll++) {
            Assert.assertFalse("roll " + roll, JupiterStorms.shouldStrike(ON_INTERVAL, true, true, roll));
        }
    }

    @Test
    public void theAverageRateIsOneStrikePerTwentySeconds() {
        // Walk a full cycle of the interval/roll pair and count: 4 considerations per 400 ticks, one of which fires.
        int strikes = 0;
        for (long tick = JupiterStorms.CHECK_INTERVAL; tick <= 20 * JupiterStorms.CHECK_INTERVAL; tick++) {
            for (int roll = 0; roll < JupiterStorms.ONE_IN; roll++) {
                if (JupiterStorms.shouldStrike(tick, true, true, roll)) strikes++;
            }
        }
        Assert.assertEquals(20, strikes);
    }

    @Test
    public void tickZeroNeverStrikes() {
        // World time starts at 0, which is divisible by the interval; the guard keeps the first tick quiet so the
        // test is explicit about it rather than relying on a coincidence.
        Assert.assertFalse(JupiterStorms.shouldStrike(0, true, true, 0));
    }
}
