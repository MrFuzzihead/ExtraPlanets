package com.mjr.extraplanets;

import net.minecraft.world.biome.BiomeGenBase;

import org.junit.Assert;
import org.junit.Test;

/**
 * Covers {@link ExtraPlanets_Biomes#getBiome(String, int, ExtraPlanets_Biomes.BiomeFactory)}.
 *
 * <p>
 * The registry is deliberately conservative: on 1.7.10 a biome's numeric ID is written into every generated
 * chunk, so remapping a value that is merely *colliding* would corrupt existing worlds. Only an ID that would
 * corrupt the world anyway is relocated: one outside the array (which would throw
 * {@code ArrayIndexOutOfBoundsException} inside {@code BiomeGenBase}'s constructor) and ID 0, which belongs to the
 * vanilla ocean biome.
 * </p>
 *
 * <p>
 * Biomes register themselves into the global {@code BiomeGenBase.biomeList} when constructed, so every test uses
 * its own ID and its own registry name and asserts only relative facts.
 * </p>
 */
public class ExtraPlanets_BiomesTest {

    /** BiomeGenBase is abstract only to force a subclass; it has no abstract members. */
    private static final class PlainBiome extends BiomeGenBase {

        PlainBiome(int biomeID) {
            super(biomeID);
        }
    }

    private static final ExtraPlanets_Biomes.BiomeFactory PLAIN_BIOME = new ExtraPlanets_Biomes.BiomeFactory() {

        @Override
        public BiomeGenBase create(int biomeID) {
            return new PlainBiome(biomeID);
        }
    };

    // ---------------------------------------------------------------------------------------------------------
    // Happy path
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void anInRangeIdIsKeptExactlyAsConfigured() {
        BiomeGenBase biome = ExtraPlanets_Biomes.getBiome("test_kept", 190, PLAIN_BIOME);
        Assert.assertNotNull(biome);
        Assert.assertEquals(190, biome.biomeID);
    }

    @Test
    public void theFactoryReceivesTheResolvedId() {
        final int[] received = new int[] { -1 };
        BiomeGenBase biome = ExtraPlanets_Biomes.getBiome("test_factory", 191, new ExtraPlanets_Biomes.BiomeFactory() {

            @Override
            public BiomeGenBase create(int biomeID) {
                received[0] = biomeID;
                return new PlainBiome(biomeID);
            }
        });
        Assert.assertEquals(biome.biomeID, received[0]);
    }

    @Test
    public void aSecondRequestForTheSameNameReturnsTheSameInstance() {
        BiomeGenBase first = ExtraPlanets_Biomes.getBiome("test_cached", 192, PLAIN_BIOME);
        BiomeGenBase second = ExtraPlanets_Biomes.getBiome("test_cached", 193, PLAIN_BIOME);
        Assert.assertSame("the registry must cache by name", first, second);
    }

    @Test
    public void aSecondRequestWithADifferentIdDoesNotReallocate() {
        BiomeGenBase first = ExtraPlanets_Biomes.getBiome("test_cached_id", 194, PLAIN_BIOME);
        ExtraPlanets_Biomes.getBiome("test_cached_id", 195, PLAIN_BIOME);
        Assert.assertEquals(194, first.biomeID);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Out-of-range values are relocated
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void anIdAboveTheArrayLengthIsRelocated() {
        int max = BiomeGenBase.getBiomeGenArray().length - 1;
        BiomeGenBase biome = ExtraPlanets_Biomes.getBiome("test_too_high", max + 1, PLAIN_BIOME);
        Assert.assertNotNull(biome);
        Assert.assertNotEquals(max + 1, biome.biomeID);
        Assert.assertTrue("relocated id must be inside the array", biome.biomeID >= 0 && biome.biomeID <= max);
    }

    @Test
    public void aNegativeIdIsRelocated() {
        BiomeGenBase biome = ExtraPlanets_Biomes.getBiome("test_negative", -5, PLAIN_BIOME);
        Assert.assertNotNull(biome);
        Assert.assertTrue("relocated id must be inside the array", biome.biomeID >= 1);
    }

    @Test
    public void aRelocatedIdIsOnTheBiomeList() {
        BiomeGenBase biome = ExtraPlanets_Biomes.getBiome("test_registered", 4000, PLAIN_BIOME);
        Assert.assertSame(
            "the relocated biome must actually be registered, not just returned",
            biome,
            BiomeGenBase.getBiome(biome.biomeID));
    }

    // ---------------------------------------------------------------------------------------------------------
    // Collisions are reported but never remapped
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void aCollisionWithAnotherModsBiomeStillUsesTheConfiguredId() {
        // Simulate another mod owning slot 196 and then loading ExtraPlanets with the same value.
        BiomeGenBase foreign = new PlainBiome(196);
        BiomeGenBase ours = ExtraPlanets_Biomes.getBiome("test_collision", 196, PLAIN_BIOME);

        Assert.assertNotSame(foreign, ours);
        Assert.assertEquals(
            "an in-range collision must not be silently remapped (that would corrupt existing worlds)",
            196,
            ours.biomeID);
    }

    @Test
    public void anExtraPlanetsInternalDuplicateIsReportedButFits() {
        ExtraPlanets_Biomes.getBiome("test_dup_first", 197, PLAIN_BIOME);
        BiomeGenBase second = ExtraPlanets_Biomes.getBiome("test_dup_second", 197, PLAIN_BIOME);
        Assert.assertNotNull(second);
        Assert.assertEquals(197, second.biomeID);
    }

    // ---------------------------------------------------------------------------------------------------------
    // ID 0 belongs to the vanilla ocean
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void idZeroIsRelocatedInsteadOfReplacingTheOcean() {
        // BiomeGenBase writes every constructed biome into biomeList[id], so accepting 0 would make this biome *the*
        // ocean for the rest of the session, in every world. The slot is occupied, but unlike a plain collision the
        // configured value cannot be kept either.
        BiomeGenBase ocean = BiomeGenBase.ocean;
        BiomeGenBase biome = ExtraPlanets_Biomes.getBiome("test_zero", 0, PLAIN_BIOME);

        Assert.assertNotEquals("ID 0 must not be handed to the factory", 0, biome.biomeID);
        Assert.assertTrue("relocated id must be inside the array", biome.biomeID >= 1);
        Assert.assertSame("the vanilla ocean must still own slot 0", ocean, BiomeGenBase.getBiome(0));
    }

    @Test
    public void aRelocatedIdIsNeverZero() {
        // findFreeBiomeID skips slot 0, so even a relocation triggered by a negative id stays clear of the ocean.
        BiomeGenBase biome = ExtraPlanets_Biomes.getBiome("test_never_zero", -1, PLAIN_BIOME);
        Assert.assertTrue(biome.biomeID >= 1);
        Assert.assertNotSame(BiomeGenBase.ocean, biome);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Sanity
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void theVanillaArrayIsStill256Entries() {
        // If an array extender such as EndlessIDs is installed this becomes larger, and the registry is expected
        // to use the real bound; the assertion is only here to make the assumption explicit.
        int length = BiomeGenBase.getBiomeGenArray().length;
        Assert.assertTrue("biome list length must be at least 256", length >= 256);
    }
}
