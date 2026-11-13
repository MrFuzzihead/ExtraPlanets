package com.mjr.extraplanets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Covers the server -&gt; client config synchronisation.
 *
 * <p>
 * A client that disagrees with the server about the dimension, space-station or biome ids cannot reach the
 * server's planets: its rockets point at dimensions that do not exist there. So the server pushes its ids on
 * login and the client adopts them.
 * </p>
 *
 * <p>
 * The wire format is a list of {@code (name, value)} pairs, and {@link Config#setConfigOverride(List)} looks every
 * key up <em>by name</em>. That is the property these tests exist to protect. The previous implementation sent a
 * bare positional list, so a single field that was renamed, retyped or removed made the server send 110 values
 * where the client expected 111 - and every value from the gap onwards landed on its neighbour's config key,
 * silently mapping (for example) a biome id onto a schematic GUI id. Nothing crashed; planets simply pointed at
 * the wrong dimension.
 * </p>
 *
 * <p>
 * The suite therefore pins four things: the name list matches the real fields, every field round-trips through
 * its own getter/setter, order and gaps cannot move a value onto another key, and no future {@code ...ID} field
 * can be added without being synced.
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
    // The name list matches the real fields
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void everySyncedNameIsAPublicStaticInt() {
        List<String> broken = new ArrayList<String>();
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            try {
                java.lang.reflect.Field field = Config.class.getField(name);
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    broken.add(name + " (not static)");
                } else if (field.getType() != int.class) {
                    broken.add(
                        name + " ("
                            + field.getType()
                                .getSimpleName()
                            + ", must be int)");
                }
            } catch (NoSuchFieldException e) {
                broken.add(name + " (no such field)");
            }
        }
        Assert.assertEquals("synced config keys that are not public static ints: " + broken, 0, broken.size());
    }

    @Test
    public void noNameIsSyncedTwice() {
        Set<String> seen = new HashSet<String>();
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            Assert.assertTrue("'" + name + "' is synced more than once", seen.add(name));
        }
    }

    @Test
    public void everyIntFieldThatLooksLikeAnIdIsSynced() {
        // Self-maintaining guard: adding `fooPlanetID` to Config without adding it to buildSyncEntries() would
        // silently leave clients on a different dimension, so require every ...ID field to be in the payload.
        // The ints that are deliberately not synced (rocket tiers, cloud speed, radiation/pressure amounts) do
        // not end in "ID"; see the comment above buildSyncEntries().
        Set<String> synced = new HashSet<String>();
        Collections.addAll(synced, ConfigState.SYNC_FIELD_NAMES);

        List<String> missing = new ArrayList<String>();
        for (String field : ConfigState.intFieldNames()) {
            if (field.endsWith("ID") && !synced.contains(field)) {
                missing.add(field);
            }
        }
        Assert.assertEquals("ID fields missing from the config sync payload: " + missing, 0, missing.size());
    }

    // ---------------------------------------------------------------------------------------------------------
    // The payload shape
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void theDecodeClassArrayIsNameValuePairs() {
        Class<?>[] types = Config.getConfigSyncDecodeClasses();
        int fields = ConfigState.SYNC_FIELD_NAMES.length;
        Assert.assertEquals("one class per name and per value", fields * 2, types.length);
        for (int i = 0; i < types.length; i += 2) {
            Assert.assertEquals("pair " + (i / 2) + " must start with a key", String.class, types[i]);
            Assert.assertEquals("pair " + (i / 2) + " must end with a value", Integer.class, types[i + 1]);
        }
    }

    @Test
    public void thePayloadIsNameValuePairsInTableOrder() {
        List<Object> packet = Config.getServerConfigOverride();
        Assert.assertEquals(
            "one name and one value per synced field",
            ConfigState.SYNC_FIELD_NAMES.length * 2,
            packet.size());

        for (int i = 0; i < packet.size(); i += 2) {
            Object key = packet.get(i);
            Assert.assertEquals("pair " + (i / 2) + " must be keyed by name", String.class, key.getClass());
            Assert.assertEquals("pair " + (i / 2) + " is out of order", ConfigState.SYNC_FIELD_NAMES[i / 2], key);
            Assert.assertTrue("pair " + (i / 2) + " must carry a number", packet.get(i + 1) instanceof Integer);
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Every field round-trips through its own entry
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void everySyncedFieldRoundTripsThroughItsOwnGetterAndSetter() {
        // A copied-and-pasted table entry (`() -> mercuryID, v -> venusID = v`) would pass every other test in
        // this class, so check each entry against the real field, one at a time.
        int value = 1000;
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            ConfigState.setInt(name, value);
            Assert.assertEquals(
                "getServerConfigOverride() reads the wrong field for '" + name + "'",
                Integer.valueOf(value),
                Config.getConfigSyncValue(name));

            List<Object> packet = Config.getServerConfigOverride();
            ConfigState.setInt(name, -1);
            Config.setConfigOverride(packet);
            Assert.assertEquals(
                "setConfigOverride() writes the wrong field for '" + name + "'",
                value,
                ConfigState.getInt(name));
            value++;
        }
    }

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

    // ---------------------------------------------------------------------------------------------------------
    // Order and gaps cannot move a value onto another key
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void pairOrderDoesNotMatterBecauseKeysAreLookedUpByName() {
        int value = 1000;
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            ConfigState.setInt(name, value++);
        }
        List<Object> packet = Config.getServerConfigOverride();

        // Rotate by an even number of entries: the pairs stay intact but arrive in a different order. The old
        // positional implementation would assign every value to the wrong key here.
        Collections.rotate(packet, 2 * 37);

        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            ConfigState.setInt(name, -1);
        }
        Config.setConfigOverride(packet);

        int expected = 1000;
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            Assert.assertEquals("reordered packet mis-assigned " + name, expected, ConfigState.getInt(name));
            expected++;
        }
    }

    @Test
    public void aMissingKeyCannotShiftTheRemainingValues() {
        // The exact failure mode of the positional format: the server drops one entry, and every later value
        // lands on its neighbour's key.
        int value = 1000;
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            ConfigState.setInt(name, value++);
        }
        List<Object> packet = Config.getServerConfigOverride();

        String dropped = ConfigState.SYNC_FIELD_NAMES[0];
        String next = ConfigState.SYNC_FIELD_NAMES[1];
        packet.remove(0); // remove the key of the first pair
        packet.remove(0); // and its value

        // Client-side values, distinct from the server's.
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            ConfigState.setInt(name, -5);
        }
        Config.setConfigOverride(packet);

        Assert.assertEquals("the dropped key must keep the client's own value", -5, ConfigState.getInt(dropped));
        Assert.assertEquals(
            "the next key must still get its own value, not its neighbour's",
            1001,
            ConfigState.getInt(next));

        int expected = 1002;
        for (int i = 2; i < ConfigState.SYNC_FIELD_NAMES.length; i++) {
            Assert.assertEquals(
                "value for " + ConfigState.SYNC_FIELD_NAMES[i] + " shifted",
                expected,
                ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[i]));
            expected++;
        }
    }

    @Test
    public void aServerThatSendsOnlySomeKeysLeavesTheRestAlone() {
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            ConfigState.setInt(name, -7);
        }
        List<Object> packet = new ArrayList<Object>();
        packet.add(ConfigState.SYNC_FIELD_NAMES[0]);
        packet.add(Integer.valueOf(42));

        Assert.assertNull(applyAndReturnThrowable(packet));
        Assert.assertEquals(42, ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[0]));
        Assert.assertEquals(-7, ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[1]));
    }

    @Test
    public void anUnknownKeyIsIgnoredWithoutAffectingTheKnownOnes() {
        // A newer server syncing a field this client has never heard of: adopt what we understand, ignore the
        // rest, and never let the unknown key disturb a known one.
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            ConfigState.setInt(name, -7);
        }
        List<Object> packet = new ArrayList<Object>();
        packet.add("aFieldFromAFutureVersion");
        packet.add(Integer.valueOf(12345));
        packet.add(ConfigState.SYNC_FIELD_NAMES[2]);
        packet.add(Integer.valueOf(99));

        Assert.assertNull(applyAndReturnThrowable(packet));
        Assert.assertEquals(99, ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[2]));
        Assert.assertEquals(-7, ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[1]));
        Assert.assertEquals(-7, ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[3]));
    }

    @Test
    public void aNonNumericValueIsSkippedWithoutAffectingAnything() {
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            ConfigState.setInt(name, -7);
        }
        List<Object> packet = new ArrayList<Object>();
        packet.add(ConfigState.SYNC_FIELD_NAMES[0]);
        packet.add("not a number");
        packet.add(ConfigState.SYNC_FIELD_NAMES[1]);
        packet.add(Integer.valueOf(5));

        Assert.assertNull(applyAndReturnThrowable(packet));
        Assert.assertEquals(
            "a non-numeric value must not be applied",
            -7,
            ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[0]));
        Assert.assertEquals(
            "the following pair must still apply",
            5,
            ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[1]));
    }

    @Test
    public void aPositionalPayloadFromAnOlderServerIsRejectedOutright() {
        // The pre-pair format was a bare list of ints. There is no safe way to interpret it positionally any
        // more, so it must be ignored completely rather than half-applied.
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            ConfigState.setInt(name, -7);
        }
        List<Object> legacyPacket = new ArrayList<Object>();
        legacyPacket.add(Integer.valueOf(1));
        legacyPacket.add(Integer.valueOf(2));

        Assert.assertNull(applyAndReturnThrowable(legacyPacket));
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            Assert.assertEquals("a legacy positional payload must change nothing", -7, ConfigState.getInt(name));
        }
    }

    @Test
    public void anOddLengthPayloadChangesNothing() {
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            ConfigState.setInt(name, -7);
        }
        List<Object> packet = new ArrayList<Object>();
        packet.add(ConfigState.SYNC_FIELD_NAMES[0]);
        // the value is missing

        Assert.assertNull(applyAndReturnThrowable(packet));
        Assert.assertEquals(-7, ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[0]));
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

    // ---------------------------------------------------------------------------------------------------------
    // Save / restore around a remote-server session
    // ---------------------------------------------------------------------------------------------------------

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

    @Test
    public void savingTwiceDoesNotOverwriteTheOriginalSnapshot() {
        // saveClientConfigOverrideable() runs when the C_UPDATE_CONFIGS packet arrives. A second packet (a
        // server sending it twice, or a respawn) must not replace the snapshot with the overridden values, or
        // the client would restore the server's ids and never get its own back.
        ConfigState.setInt(ConfigState.SYNC_FIELD_NAMES[0], 11);
        Config.saveClientConfigOverrideable();

        ConfigState.setInt(ConfigState.SYNC_FIELD_NAMES[0], 999);
        Config.saveClientConfigOverrideable();
        Config.restoreClientConfigOverrideable();

        Assert.assertEquals(11, ConfigState.getInt(ConfigState.SYNC_FIELD_NAMES[0]));
    }

    // ---------------------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------------------

    private static Throwable applyAndReturnThrowable(List<Object> packet) {
        try {
            Config.setConfigOverride(packet);
            return null;
        } catch (Throwable t) {
            return t;
        }
    }

    /** The payload as a name -&gt; value map, for tests that want to talk about keys rather than indices. */
    static Map<String, Integer> payloadAsMap(List<Object> packet) {
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < packet.size(); i += 2) {
            map.put((String) packet.get(i), (Integer) packet.get(i + 1));
        }
        return map;
    }
}
