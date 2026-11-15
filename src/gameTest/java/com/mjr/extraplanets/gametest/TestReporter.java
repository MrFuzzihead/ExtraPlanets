package com.mjr.extraplanets.gametest;

import net.minecraft.item.ItemStack;

/**
 * The assertion and reporting vocabulary used by the in-game tests.
 *
 * <p>
 * The game test is not run by JUnit; it is a plain {@code @Mod} that runs inside a real dedicated server. It counts
 * its own results so that one failed check does not abort the rest of the run - a game-test boot costs a minute, and
 * knowing about three failures at once is worth much more than knowing about the first one.
 * </p>
 *
 * <p>
 * It deliberately depends on nothing but the game itself, so that the gameTest classpath stays identical to the mod's
 * own runtime classpath.
 * </p>
 */
final class TestReporter {

    private int checks;
    private int failures;

    /** Runs a named check. A thrown exception is recorded as a failure, not propagated. */
    void check(String name, Runnable body) {
        checks++;
        try {
            body.run();
            System.out.println("[EP-GAMETEST] PASS " + name);
        } catch (Throwable t) {
            failures++;
            System.out.println("[EP-GAMETEST] FAIL " + name + ": " + t);
            t.printStackTrace(System.out);
        }
    }

    /** @return the number of checks that failed. */
    int failures() {
        return failures;
    }

    /** @return the number of checks that ran. */
    int checks() {
        return checks;
    }

    // ---------------------------------------------------------------------------------------------------------
    // Assertions
    // ---------------------------------------------------------------------------------------------------------

    static void assertTrue(String message, boolean condition) {
        if (!condition) throw new AssertionError(message);
    }

    static void assertFalse(String message, boolean condition) {
        if (condition) throw new AssertionError(message);
    }

    static void assertNotNull(String message, Object value) {
        if (value == null) throw new AssertionError(message + " was null");
    }

    static void assertEquals(String message, Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }

    static void assertNull(String message, Object value) {
        if (value != null) throw new AssertionError(message + " but was <" + value + ">");
    }

    static void assertEquals(String message, float expected, float actual, float delta) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError(
                message + ": expected <" + expected + "> +/- " + delta + " but was <" + actual + ">");
        }
    }

    static void assertEquals(String message, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }

    static void assertNotEquals(String message, Object unexpected, Object actual) {
        if (unexpected == null ? actual == null : unexpected.equals(actual)) {
            throw new AssertionError(message + ": did not expect <" + actual + ">");
        }
    }

    /**
     * Asserts that a stack has a real item. In game the item registry is fully populated, so unlike in the headless
     * suite, {@code ItemStack#getItem()} must never be null here.
     */
    static void assertHasItem(String message, ItemStack stack) {
        assertNotNull(message + " (stack is null)", stack);
        assertNotNull(message + " (item is null)", stack.getItem());
    }
}
