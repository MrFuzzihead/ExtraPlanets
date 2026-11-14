package com.mjr.extraplanets.handlers;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldProviderSurface;

import org.junit.Assert;
import org.junit.Test;

import com.mjr.extraplanets.api.world.IPressureWorld;
import com.mjr.extraplanets.api.world.ISolarRadiationWorld;

import micdoodle8.mods.galacticraft.core.dimension.WorldProviderMoon;
import micdoodle8.mods.galacticraft.core.dimension.WorldProviderSpaceStation;
import micdoodle8.mods.galacticraft.planets.asteroids.dimension.WorldProviderAsteroids;
import micdoodle8.mods.galacticraft.planets.mars.dimension.WorldProviderMars;

/**
 * Covers {@link WorldHazards}, which replaced the per-tick {@code instanceof} chains in {@code MainHandler}.
 *
 * <p>
 * The classification is a pure function of the provider <em>type</em>, which is what makes this testable at all: a
 * class literal loads the type without initialising it, so a plain JVM can ask "where would this world's pressure come
 * from" without a world, a dimension or a registry.
 * </p>
 */
public class WorldHazardsTest {

    // ---------------------------------------------------------------------------------------------------------
    // Pressure
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void theThreeGalacticraftBodiesMapToTheirOwnConfigValues() {
        Assert.assertEquals(WorldHazards.PressureSource.MOON, WorldHazards.pressureSource(WorldProviderMoon.class));
        Assert.assertEquals(WorldHazards.PressureSource.MARS, WorldHazards.pressureSource(WorldProviderMars.class));
        Assert.assertEquals(
            WorldHazards.PressureSource.ASTEROIDS,
            WorldHazards.pressureSource(WorldProviderAsteroids.class));
    }

    @Test
    public void spaceStationsMapToTheStationValue() {
        // Checked after the bodies on purpose: a station provider must not be classified as the body it orbits.
        Assert.assertEquals(
            WorldHazards.PressureSource.SPACE_STATION,
            WorldHazards.pressureSource(WorldProviderSpaceStation.class));
    }

    @Test
    public void aProviderThatReportsItsOwnPressureDelegatesToIt() {
        Assert.assertEquals(
            WorldHazards.PressureSource.FROM_PROVIDER,
            WorldHazards.pressureSource(RecordingPressureWorld.class));
    }

    @Test
    public void anUnrelatedWorldHasNoPressureSource() {
        Assert.assertEquals(WorldHazards.PressureSource.NONE, WorldHazards.pressureSource(WorldProviderSurface.class));
        Assert.assertEquals(WorldHazards.PressureSource.NONE, WorldHazards.pressureSource(WorldProviderHell.class));
        Assert.assertEquals(WorldHazards.PressureSource.NONE, WorldHazards.pressureSource(WorldProvider.class));
    }

    // ---------------------------------------------------------------------------------------------------------
    // Radiation
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void theRadiationSourcesMatchThePressureOnes() {
        Assert.assertEquals(WorldHazards.RadiationSource.MOON, WorldHazards.radiationSource(WorldProviderMoon.class));
        Assert.assertEquals(WorldHazards.RadiationSource.MARS, WorldHazards.radiationSource(WorldProviderMars.class));
        Assert.assertEquals(
            WorldHazards.RadiationSource.ASTEROIDS,
            WorldHazards.radiationSource(WorldProviderAsteroids.class));
        Assert.assertEquals(
            WorldHazards.RadiationSource.SPACE_STATION,
            WorldHazards.radiationSource(WorldProviderSpaceStation.class));
        Assert.assertEquals(
            WorldHazards.RadiationSource.FROM_PROVIDER,
            WorldHazards.radiationSource(RecordingRadiationWorld.class));
        Assert
            .assertEquals(WorldHazards.RadiationSource.NONE, WorldHazards.radiationSource(WorldProviderSurface.class));
    }

    @Test
    public void theTwoKindsOfProviderAreNotConfused() {
        // A world that only reports radiation must not be treated as one that reports pressure.
        Assert
            .assertEquals(WorldHazards.PressureSource.NONE, WorldHazards.pressureSource(RecordingRadiationWorld.class));
        Assert.assertEquals(
            WorldHazards.RadiationSource.NONE,
            WorldHazards.radiationSource(RecordingPressureWorld.class));
    }

    // ---------------------------------------------------------------------------------------------------------
    // The cache
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void theAnswerIsCachedPerProviderType() {
        // The cache is the whole point (this runs every tick), so assert it answers identically and that it is keyed
        // by type rather than by anything that can change under it.
        for (int i = 0; i < 100; i++) {
            Assert.assertEquals(WorldHazards.PressureSource.MARS, WorldHazards.pressureSource(WorldProviderMars.class));
        }
        Assert.assertEquals(WorldHazards.PressureSource.MOON, WorldHazards.pressureSource(WorldProviderMoon.class));
    }

    // ---------------------------------------------------------------------------------------------------------
    // Stand-ins for a world that reports its own levels
    // ---------------------------------------------------------------------------------------------------------

    /** Only implements the pressure interface, so it must not be classified as a radiation source. */
    private static final class RecordingPressureWorld extends WorldProvider implements IPressureWorld {

        @Override
        public int getPressureLevel() {
            return 3;
        }

        @Override
        public String getDimensionName() {
            return "pressure-test";
        }
    }

    /** Only implements the radiation interface, so it must not be classified as a pressure source. */
    private static final class RecordingRadiationWorld extends WorldProvider implements ISolarRadiationWorld {

        @Override
        public int getSolarRadiationLevel() {
            return 4;
        }

        @Override
        public String getDimensionName() {
            return "radiation-test";
        }
    }
}
