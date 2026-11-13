package com.mjr.extraplanets;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Covers the server -&gt; client config synchronisation added for the "client and server config are never
 * synchronised" finding.
 *
 * <p>
 * The mechanism is a name list plus reflection: {@code SYNC_CONFIG_FIELD_NAMES} drives both the packet's decode
 * class array ({@link Config#getConfigSyncDecodeClasses()}) and the ordered value list
 * ({@link Config#getServerConfigOverride()}). The client applies it positionally
 * ({@link Config#setConfigOverride(List)}), so the three lists must stay exactly aligned. They are only kept in
 * sync by hand, and a single mistyped or renamed field silently shifts every value after it onto the wrong
 * config key - a corruption that only shows up as "my planet is the wrong dimension" in game.
 * </p>
 *
 * <p>
 * That alignment is what these tests pin down.
 * </p>
 */
public class ConfigSyncContractTest {

    private ConfigState state;

    @Before
    public void captureConfig() {
        state = ConfigState.snapshot();
    }

    @After
    public void restoreConfig() {
        state.restore();
    }

    // ---------------------------------------------------------------------------------------------------------
    // The name list must resolve
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void everySyncedNameIsAPublicStaticInt() {
        List<String> broken = new ArrayList<String>();
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            Field field;
            try {
                field = Config.class.getField(name);
            } catch (NoSuchFieldException e) {
                broken.add(name + " (no such field)");
                continue;
            }
            if (!Modifier.isStatic(field.getModifiers())) {
                broken.add(name + " (not static)");
            } else if (field.getType() != int.class) {
                // getServerConfigOverride uses Field.getInt, which throws for any other type, so the value
                // would be silently skipped and the positional list would shift.
                broken.add(
                    name + " ("
                        + field.getType()
                            .getSimpleName()
                        + ", must be int)");
            }
        }
        Assert.assertEquals("synced config keys that cannot be read reflectively: " + broken, 0, broken.size());
    }

    @Test
    public void theValueListIsAsLongAsTheDecodeClassArray() {
        // If these disagree, NetworkUtil.decodeData reads the wrong number of ints and the positional mapping
        // in setConfigOverride is off: a shorter value list means every key after the gap gets its neighbour's
        // value.
        Assert.assertEquals(
            Config.getConfigSyncDecodeClasses().length,
            Config.getServerConfigOverride()
                .size());
    }

    @Test
    public void theDecodeClassArrayOnlyAsksForIntegers() {
        for (Class<?> type : Config.getConfigSyncDecodeClasses()) {
            Assert.assertEquals(Integer.class, type);
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // The round trip
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void everySyncedValueSurvivesAServerToClientRoundTrip() {
        // Give each key a distinct value so a shifted mapping cannot accidentally pass.
        int value = 1000;
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            ConfigState.setInt(name, value++);
        }

        List<Object> packet = Config.getServerConfigOverride();

        // Simulate the client's own, different, local config.
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            ConfigState.setInt(name, -1);
        }

        Config.setConfigOverride(packet);

        int expected = 1000;
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            Assert.assertEquals("wrong value applied for " + name, expected, ConfigState.getInt(name));
            expected++;
        }
    }

    @Test
    public void applyingAShortPacketLeavesTheRemainingKeysAlone() {
        // Forward/backward compatibility: a shorter list (older server) must not throw.
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            ConfigState.setInt(name, -7);
        }
        List<Object> shortPacket = new ArrayList<Object>();
        shortPacket.add(Integer.valueOf(42));

        Assert.assertNull(applyAndReturnThrowable(shortPacket));
        Assert.assertEquals(42, ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[0]));
        Assert.assertEquals(-7, ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[1]));
    }

    @Test
    public void applyingANullPacketChangesNothing() {
        ConfigState.setInt(ConfigState.SYNC_FIELD_NAMES[0], -7);
        Assert.assertNull(applyAndReturnThrowable(null));
        Assert.assertEquals(-7, ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[0]));
    }

    @Test
    public void anEmptyPacketChangesNothing() {
        ConfigState.setInt(ConfigState.SYNC_FIELD_NAMES[0], -7);
        Assert.assertNull(applyAndReturnThrowable(new ArrayList<Object>()));
        Assert.assertEquals(-7, ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[0]));
    }

    @Test
    public void saveThenRestoreRoundTripsTheLocallyConfiguredValues() {
        // The client saves its own config before applying the server's, then restores it on disconnect.
        ConfigState.setInt(ConfigState.SYNC_FIELD_NAMES[0], 11);
        ConfigState.setInt(ConfigState.SYNC_FIELD_NAMES[1], 22);
        Config.saveClientConfigOverrideable();

        // The server's values are whatever is configured there; simulate a different set.
        ConfigState.setInt(ConfigState.SYNC_FIELD_NAMES[0], 999);
        ConfigState.setInt(ConfigState.SYNC_FIELD_NAMES[1], 888);
        List<Object> serverPacket = Config.getServerConfigOverride();

        // The client's own values are back in place while it waits for the packet.
        ConfigState.setInt(ConfigState.SYNC_FIELD_NAMES[0], 11);
        ConfigState.setInt(ConfigState.SYNC_FIELD_NAMES[1], 22);

        Config.setConfigOverride(serverPacket);
        Assert.assertEquals(
            "the client must adopt the server's value",
            999,
            ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[0]));
        Assert.assertEquals(888, ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[1]));

        Config.restoreClientConfigOverrideable();
        Assert.assertEquals(
            "disconnecting must restore the client's own value",
            11,
            ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[0]));
        Assert.assertEquals(22, ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[1]));

        // Restoring twice is a no-op (the save slot is cleared).
        Config.restoreClientConfigOverrideable();
        Assert.assertEquals(11, ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[0]));
    }

    private static Throwable applyAndReturnThrowable(List<Object> packet) {
        try {
            Config.setConfigOverride(packet);
            return null;
        } catch (Throwable t) {
            return t;
        }
    }
}
