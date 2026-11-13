package com.mjr.extraplanets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Loads the real config file (into the build directory, not the repository) and checks the defaults it ships.
 *
 * <p>
 * Dimension, station, biome and schematic IDs are all user-visible numbers that must be unique. Two bodies
 * sharing a dimension ID is a hard crash on travel; two bodies sharing a biome ID quietly corrupts generated
 * terrain and is only visible much later. Both are pure config-file mistakes, so they belong in a headless test
 * rather than in a manual play-test.
 * </p>
 *
 * <p>
 * {@code Config.init()} parses {@code Constants.CONFIG_FILE} relative to the working directory; the build sets
 * the test working directory to {@code build/test-workdir}, so this class never writes into the checkout.
 * </p>
 */
public class ConfigDefaultsTest {

    /** The dimension IDs of the real planets. Space-station IDs are checked separately. */
    private static final String[] PLANET_DIMENSION_KEYS = { "mercuryID", "venusID", "ceresID", "jupiterID", "saturnID",
        "uranusID", "neptuneID", "plutoID", "erisID", "tritonID", "europaID", "ioID", "deimosID", "phobosID",
        "callistoID", "ganymedeID", "rheaID", "titanID", "oberonID", "titaniaID", "iapetusID", "kepler22bID" };

    /** Every biome ID the mod can register (49 of them). */
    private static final String[] BIOME_KEYS = { "mercuryBiomeID", "venusBiomeID", "ceresBiomeID", "jupiterBiomeID",
        "jupiterSeaBiomeID", "jupiterSandsBiomeID", "saturnBiomeID", "saturnHydrocarbonSeaBiomeID",
        "saturnNuclearLandBiomeID", "uranusBiomeID", "uranusFrozenSeaBiomeID", "uranusSnowLandsBiomeID",
        "neptuneBiomeID", "neptuneRadioActiveWaterSeaBiomeID", "neptuneLayeredHillsBiomeID", "plutoBiomeID",
        "erisBiomeID", "tritonBiomeID", "tritonIceLandsBiomeID", "tritonIceSeaBiomeID", "europaBiomeID", "ioBiomeID",
        "ioAshLandsBiomeID", "ioBurningPlainsBiomeID", "deimosBiomeID", "phobosBiomeID", "callistoBiomeID",
        "ganymedeBiomeID", "rheaBiomeID", "titanBiomeID", "titanSeaBiomeID", "titanMethaneHillsBiomeID",
        "oberonBiomeID", "oberonValleysBiomeID", "oberonLargeMountainBiomeID", "titaniaBiomeID", "iapetusBiomeID",
        "kepler22bPlainsBiomeID", "kepler22bBlueForestBiomeID", "kepler22bPurpleForestBiomeID",
        "kepler22bRedForestBiomeID", "kepler22bYellowForestBiomeID", "kepler22bRedDesertBiomeID",
        "kepler22bWasteLandsBiomeID", "kepler22bDiamondPlainsBiomeID", "kepler22bCoalPlainsBiomeID",
        "kepler22bIronPlainsBiomeID", "kepler22bGoldPlainsBiomeID", "kepler22bEmeraldPlainsBiomeID" };

    /** The GUI/page IDs used by the schematic pages (they must not collide with GC's own). */
    private static final String[] SCHEMATIC_GUI_KEYS = { "schematicTier4GUIID", "schematicTier5GUIID",
        "schematicTier6GUIID", "schematicTier7GUIID", "schematicTier8GUIID", "schematicTier9GUIID",
        "schematicTier10GUIID", "schematicMarsRoverGUIID", "schematicVenusRoverGUIID" };

    private static final String[] SCHEMATIC_PAGE_KEYS = { "schematicTier4PageID", "schematicTier5PageID",
        "schematicTier6PageID", "schematicTier7PageID", "schematicTier8PageID", "schematicTier9PageID",
        "schematicTier10PageID", "schematicMarsRoverPageID", "schematicVenusRoverPageID" };

    private ConfigState state;

    @BeforeClass
    public static void loadTheRealConfig() {
        // Config.init() -> new Configuration(...) needs FML's injected home directory, which only exists inside a
        // real launch. Stub it so the real config file can be parsed in a plain JVM.
        FmlTestEnvironment.install();
        Config.init();
    }

    @Before
    public void captureConfig() {
        state = ConfigState.snapshot();
    }

    @After
    public void restoreConfig() {
        state.restore();
    }

    // ---------------------------------------------------------------------------------------------------------
    // Dimension IDs
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void planetDimensionIdsAreUnique() {
        assertNoDuplicateInts("dimension", PLANET_DIMENSION_KEYS);
    }

    @Test
    public void spaceStationDimensionIdsAreUnique() {
        assertNoDuplicateInts("space station", spaceStationKeys());
    }

    @Test
    public void aStationNeverSharesItsOwnPlanetsDimensionId() {
        for (String key : spaceStationKeys()) {
            if (!key.endsWith("SpaceStationID")) continue;
            String planetKey = key.substring(0, key.length() - "SpaceStationID".length()) + "ID";
            if (!contains(PLANET_DIMENSION_KEYS, planetKey)) continue;
            Assert.assertNotEquals(
                "station and planet share a dimension ID: " + key + " / " + planetKey,
                ConfigState.getInt(planetKey),
                ConfigState.getInt(key));
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Biome IDs
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void allFortyNineBiomeIdsAreDeclared() {
        Assert.assertEquals(49, BIOME_KEYS.length);
        // Guards the list above against a biome being added to Config without being added here.
        int synced = 0;
        for (String name : ConfigState.SYNC_FIELD_NAMES) if (name.endsWith("BiomeID")) synced++;
        Assert.assertEquals("Config syncs a different number of biome IDs than this test knows about", 49, synced);
    }

    @Test
    public void biomeIdsAreUnique() {
        assertNoDuplicateInts("biome", BIOME_KEYS);
    }

    @Test
    public void biomeIdsFitTheVanillaBiomeArray() {
        // BiomeGenBase.biomeList is a fixed 256-entry array on 1.7.10; anything outside 0-255 throws
        // ArrayIndexOutOfBoundsException in the BiomeGenBase constructor.
        for (String key : BIOME_KEYS) {
            int id = ConfigState.getInt(key);
            Assert.assertTrue(
                "biome ID for " + key + " is " + id + ", outside the valid 0-255 range",
                id >= 0 && id <= 255);
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Schematic GUI / page IDs
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void schematicGuiIdsAreUnique() {
        assertNoDuplicateInts("schematic GUI", SCHEMATIC_GUI_KEYS);
    }

    @Test
    public void schematicPageIdsAreUnique() {
        assertNoDuplicateInts("schematic page", SCHEMATIC_PAGE_KEYS);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Config file hygiene
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void theConfigFileIsWrittenOnceAndReused() {
        // load() must be idempotent: calling it a second time has to produce the same values, otherwise a
        // /reload or a second preInit would silently re-randomise IDs.
        Map<String, Integer> before = new HashMap<String, Integer>();
        for (String key : ConfigState.SYNC_FIELD_NAMES) before.put(key, Integer.valueOf(ConfigState.getInt(key)));

        Config.init();

        for (String key : ConfigState.SYNC_FIELD_NAMES) {
            Assert.assertEquals(
                "second Config.init() changed " + key,
                before.get(key)
                    .intValue(),
                ConfigState.getInt(key));
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------------------

    private static String[] spaceStationKeys() {
        Set<String> keys = new HashSet<String>();
        for (String name : ConfigState.SYNC_FIELD_NAMES) {
            if (name.endsWith("SpaceStationID") || name.endsWith("SpaceStationStaticID")) keys.add(name);
        }
        String[] result = keys.toArray(new String[0]);
        java.util.Arrays.sort(result);
        return result;
    }

    private static boolean contains(String[] array, String value) {
        for (String element : array) if (element.equals(value)) return true;
        return false;
    }

    private static void assertNoDuplicateInts(String kind, String[] keys) {
        Map<Integer, String> seen = new HashMap<Integer, String>();
        for (String key : keys) {
            Integer value = Integer.valueOf(ConfigState.getInt(key));
            String previous = seen.put(value, key);
            Assert.assertNull(
                "duplicate " + kind + " ID " + value + ": both '" + previous + "' and '" + key + "' use it",
                previous);
        }
    }
}
