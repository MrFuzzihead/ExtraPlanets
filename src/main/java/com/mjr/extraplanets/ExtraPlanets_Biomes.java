package com.mjr.extraplanets;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.biome.BiomeGenBase;

import micdoodle8.mods.galacticraft.core.util.GCLog;

/**
 * Central registry for ExtraPlanets biomes.
 * <p>
 * In Minecraft 1.7.10, {@link BiomeGenBase} keeps every biome in a fixed 256-entry array (IDs 0-255)
 * and registers a biome into that array the moment it is constructed. Generated chunks also store the
 * numeric biome ID in their biome data, so a biome's ID is effectively part of every world's save
 * format.
 * <p>
 * Because of that, {@link #getBiome} deliberately never remaps an existing config value:
 * <ul>
 * <li>It KEEPS the configured ID whenever it is within the valid 0-255 range - even if it collides
 *     with another mod's biome - so existing configs and worlds are never remapped.</li>
 * <li>It DETECTS and loudly LOGS any collision with another mod's biome, naming the slot and the
 *     biome currently occupying it, so the user can fix the value in their config by hand.</li>
 * <li>It only relocates an ID that is OUT of the 0-255 range (which would otherwise crash
 *     <code>biomeList[id] = ...</code> with an {@link ArrayIndexOutOfBoundsException}), as a last
 *     resort to keep the game running, and logs a clear message to fix the config.</li>
 * <li>It never constructs a biome until the first time it is actually requested, which for
 *     ExtraPlanets only happens when a planet/moon's dimension is used. Disabled bodies therefore
 *     never claim a biome slot.</li>
 * </ul>
 */
public class ExtraPlanets_Biomes {

    /** Creates the biome instance for a resolved (safe) numeric ID. */
    public interface BiomeFactory {

        BiomeGenBase create(int biomeID);
    }

    /** Registry keyed by the biome name used in the config, e.g. {@code "venus"}. */
    private static final Map<String, BiomeGenBase> BIOMES = new HashMap<String, BiomeGenBase>();

    private ExtraPlanets_Biomes() {
    }

    /**
     * Returns (creating it on first use) the biome registered for {@code biomeName}.
     *
     * @param biomeName    config biome name, e.g. {@code "venus"}
     * @param configuredID the numeric ID read from the config (0-255 is the valid range on 1.7.10)
     * @param factory      creates the biome instance for the given numeric ID
     * @return the created (or already registered) biome instance
     */
    public static BiomeGenBase getBiome(String biomeName, int configuredID, BiomeFactory factory) {
        BiomeGenBase existing = BIOMES.get(biomeName);
        if (existing != null) {
            return existing;
        }
        int resolvedID = resolveBiomeID(biomeName, configuredID);
        BiomeGenBase biome = factory.create(resolvedID);
        BIOMES.put(biomeName, biome);
        return biome;
    }

    private static int resolveBiomeID(String biomeName, int configuredID) {
        if (configuredID < 0 || configuredID > 255) {
            int fallbackID = findFreeBiomeID();
            GCLog.severe(
                "[ExtraPlanets] The Biome ID for '" + biomeName
                    + "' is " + configuredID
                    + ", which is outside the valid 0-255 range for Minecraft 1.7.10, so it would crash. "
                    + "Using the unused ID " + fallbackID
                    + " instead. Please set the '" + biomeName
                    + " Biome ID' value in config/ExtraPlanets.cfg to an unused number in the range 0-255 and restart.");
            return fallbackID;
        }

        // BiomeGenBase.getBiome returns the biome occupying a slot when that ID is in range, or null
        // if the slot is empty (only out-of-bounds IDs fall back to ocean). A null occupant therefore
        // means the slot is genuinely unused -> no conflict, use the configured ID as-is.
        // The ocean biome itself owns slot 0, so ID 0 is never treated as a conflict here either.
        BiomeGenBase occupant = BiomeGenBase.getBiome(configuredID);
        if (configuredID != 0 && occupant != null && occupant != BiomeGenBase.ocean) {
            if (BIOMES.containsValue(occupant)) {
                String owner = findOwnerName(occupant);
                GCLog.severe(
                    "[ExtraPlanets] BIOME ID CONFLICT: the '" + biomeName + "' biome is set to ID " + configuredID
                        + " in config/ExtraPlanets.cfg, but that slot is already used by the ExtraPlanets biome '"
                        + owner + "'. Both share the same ID, which will corrupt generated terrain. "
                        + "To resolve this, change one of the two '... Biome ID' values in config/ExtraPlanets.cfg "
                        + "to an unused number in the range 0-255 and restart.");
            } else {
                String occupantName = occupant.getClass().getSimpleName();
                GCLog.severe(
                    "[ExtraPlanets] BIOME ID CONFLICT: the '" + biomeName + "' biome is set to ID " + configuredID
                        + " in config/ExtraPlanets.cfg, but that slot is already used by the biome '"
                        + occupantName + "'. Both share the same ID, which can corrupt generated terrain. "
                        + "To resolve this, change the '" + biomeName
                        + " Biome ID' value in config/ExtraPlanets.cfg to an unused number in the range 0-255 and restart.");
            }
        }
        return configuredID;
    }

    private static String findOwnerName(BiomeGenBase biome) {
        for (Map.Entry<String, BiomeGenBase> entry : BIOMES.entrySet()) {
            if (entry.getValue() == biome) {
                return entry.getKey();
            }
        }
        return "unknown";
    }

    private static int findFreeBiomeID() {
        // IDs 1-255; slot 0 always belongs to the vanilla ocean biome. A slot is free only when
        // biomeList[id] is null (BiomeGenBase.getBiome returns null for an in-range empty slot).
        // Slots that already hold a real biome - including ocean, which owns slot 0 - are taken.
        for (int id = 1; id <= 255; id++) {
            if (BiomeGenBase.getBiome(id) == null) {
                return id;
            }
        }
        GCLog.severe(
            "[ExtraPlanets] No free biome IDs remain in the entire 0-255 range! This indicates a severe "
                + "biome conflict with other mods. Falling back to ID 0 (the ocean biome) so the game can continue, "
                + "but this world will have corrupted biomes.");
        return 0;
    }
}