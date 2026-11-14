package com.mjr.extraplanets.handlers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import micdoodle8.mods.galacticraft.core.dimension.WorldProviderMoon;
import micdoodle8.mods.galacticraft.core.dimension.WorldProviderSpaceStation;
import micdoodle8.mods.galacticraft.planets.asteroids.dimension.WorldProviderAsteroids;
import micdoodle8.mods.galacticraft.planets.mars.dimension.WorldProviderMars;

/**
 * Works out where a world's pressure and radiation come from, once per provider type.
 *
 * <p>
 * The hazard level is needed every tick for the HUD and for the damage checks, and it used to be a chain of
 * {@code instanceof} tests that ran again on every call. The <em>classification</em> depends only on the provider
 * class, so it is memoised; the <em>level</em> comes from {@code Config} and is read fresh each time, because the
 * server syncs those values at runtime.
 * </p>
 *
 * <p>
 * Taking the class rather than the instance also makes this testable: {@code SomeProvider.class} loads the type
 * without initialising it, so a plain JVM can check the classification without a world.
 * </p>
 */
final class WorldHazards {

    /** Where a world's pressure value comes from. */
    enum PressureSource {
        /** The provider implements {@code IPressureWorld} and reports its own level. */
        FROM_PROVIDER,
        MOON,
        MARS,
        ASTEROIDS,
        SPACE_STATION,
        NONE
    }

    /** Where a world's solar radiation value comes from. */
    enum RadiationSource {
        /** The provider implements {@code ISolarRadiationWorld} and reports its own level. */
        FROM_PROVIDER,
        MOON,
        MARS,
        ASTEROIDS,
        SPACE_STATION,
        NONE
    }

    private static final Map<Class<?>, PressureSource> PRESSURE_CACHE = new ConcurrentHashMap<Class<?>, PressureSource>();
    private static final Map<Class<?>, RadiationSource> RADIATION_CACHE = new ConcurrentHashMap<Class<?>, RadiationSource>();

    private WorldHazards() {}

    static PressureSource pressureSource(Class<?> providerType) {
        PressureSource cached = PRESSURE_CACHE.get(providerType);
        if (cached != null) {
            return cached;
        }
        PressureSource computed = classifyPressure(providerType);
        PRESSURE_CACHE.put(providerType, computed);
        return computed;
    }

    static RadiationSource radiationSource(Class<?> providerType) {
        RadiationSource cached = RADIATION_CACHE.get(providerType);
        if (cached != null) {
            return cached;
        }
        RadiationSource computed = classifyRadiation(providerType);
        RADIATION_CACHE.put(providerType, computed);
        return computed;
    }

    private static PressureSource classifyPressure(Class<?> providerType) {
        if (com.mjr.extraplanets.api.world.IPressureWorld.class.isAssignableFrom(providerType)) {
            return PressureSource.FROM_PROVIDER;
        }
        if (WorldProviderMoon.class.isAssignableFrom(providerType)) {
            return PressureSource.MOON;
        }
        if (WorldProviderMars.class.isAssignableFrom(providerType)) {
            return PressureSource.MARS;
        }
        if (WorldProviderAsteroids.class.isAssignableFrom(providerType)) {
            return PressureSource.ASTEROIDS;
        }
        if (WorldProviderSpaceStation.class.isAssignableFrom(providerType)) {
            return PressureSource.SPACE_STATION;
        }
        return PressureSource.NONE;
    }

    private static RadiationSource classifyRadiation(Class<?> providerType) {
        if (com.mjr.extraplanets.api.world.ISolarRadiationWorld.class.isAssignableFrom(providerType)) {
            return RadiationSource.FROM_PROVIDER;
        }
        if (WorldProviderMoon.class.isAssignableFrom(providerType)) {
            return RadiationSource.MOON;
        }
        if (WorldProviderMars.class.isAssignableFrom(providerType)) {
            return RadiationSource.MARS;
        }
        if (WorldProviderAsteroids.class.isAssignableFrom(providerType)) {
            return RadiationSource.ASTEROIDS;
        }
        if (WorldProviderSpaceStation.class.isAssignableFrom(providerType)) {
            return RadiationSource.SPACE_STATION;
        }
        return RadiationSource.NONE;
    }
}
