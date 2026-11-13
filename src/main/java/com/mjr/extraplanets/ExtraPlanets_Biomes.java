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
 * Some modpacks use {@code EndlessIDs} (or similar ASM patchers) to extend the biome array well
 * beyond the vanilla 256 slots. This registry detects that at runtime by reading
 * {@link BiomeGenBase#getBiomeGenArray() getBiomeGenArray()}{@code .length} and uses the actual
 * dynamic bound instead of the hardcoded 255, so it works seamlessly with or without an array
 * extender.
 * <p>
 * Because of that, {@link #getBiome} deliberately never remaps an existing config value:
 * <ul>
 * <li>It KEEPS the configured ID whenever it is within the live array's range - even if it collides
 * with another mod's biome - so existing configs and worlds are never remapped.</li>
 * <li>It DETECTS and loudly LOGS any collision with another mod's biome, naming the slot and the
 * biome currently occupying it, so the user can fix the value in their config by hand.</li>
 * <li>It only relocates an ID that would otherwise corrupt the world: one that is OUT of the live
 * range (which would throw {@link ArrayIndexOutOfBoundsException} inside the {@link BiomeGenBase}
 * constructor) and ID 0, which is the vanilla ocean biome's slot. Relocation is a last resort to keep
 * the game running, and it always logs a clear message to fix the config.</li>
 * <li>It never constructs a biome until the first time it is actually requested, which for
 * ExtraPlanets only happens when a planet/moon's dimension is used. Disabled bodies therefore
 * never claim a biome slot.</li>
 * </ul>
 */
public class ExtraPlanets_Biomes {

    /** Creates the biome instance for a resolved (safe) numeric ID. */
    public interface BiomeFactory {

        BiomeGenBase create(int biomeID);
    }

    /** Registry keyed by the biome name used in the config, e.g. {@code "venus"}. */
    private static final Map<String, BiomeGenBase> BIOMES = new HashMap<String, BiomeGenBase>();

    private ExtraPlanets_Biomes() {}

    /**
     * Returns (creating it on first use) the biome registered for {@code biomeName}.
     *
     * @param biomeName    config biome name, e.g. {@code "venus"}
     * @param configuredID the numeric ID read from the config; the valid upper bound is whatever
     *                     {@link BiomeGenBase#getBiomeGenArray()} reports at runtime (255 vanilla, far higher with an
     *                     ID extender), never a hardcoded cap
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
        final int maxID = BiomeGenBase.getBiomeGenArray().length - 1;
        if (configuredID < 0 || configuredID > maxID) {
            int fallbackID = findFreeBiomeID();
            GCLog.severe(
                "[ExtraPlanets] The Biome ID for '" + biomeName
                    + "' is "
                    + configuredID
                    + ", which is outside the valid 0-"
                    + maxID
                    + " range on this instance"
                    + (maxID == 255 ? "" : " (EndlessIDs or similar extends the array)")
                    + ", so it would crash. "
                    + "Using the unused ID "
                    + fallbackID
                    + " instead. Please set the '"
                    + biomeName
                    + " Biome ID' value in config/ExtraPlanets.cfg to an unused number in the range 0-"
                    + maxID
                    + " and restart.");
            return fallbackID;
        }

        // ID 0 is vanilla's ocean biome. BiomeGenBase writes every constructed biome into biomeList[id], so
        // accepting 0 here would replace the ocean for the rest of the session: every ocean in every world becomes
        // this planet's biome. The slot is used, so treat it exactly like a collision - but relocate instead of just
        // warning, because keeping the configured value is what corrupts the world.
        //
        // Note that 0 is *not* treated as out of range: with an ID extender (EndlessIDs and friends) the valid range
        // runs to the extender's real array length, and this check stays out of the way of that.
        if (configuredID == 0) {
            int fallbackID = findFreeBiomeID();
            GCLog.severe(
                "[ExtraPlanets] The Biome ID for '" + biomeName
                    + "' is 0 in config/ExtraPlanets.cfg, but 0 is the vanilla ocean biome's ID. Using it would replace"
                    + " the ocean with this biome in every world. Using the unused ID "
                    + fallbackID
                    + " instead. Please set the '"
                    + biomeName
                    + " Biome ID' value in config/ExtraPlanets.cfg to an unused number and restart.");
            return fallbackID;
        }

        // BiomeGenBase.getBiome returns the biome occupying a slot when that ID is in range, or null
        // if the slot is empty (only out-of-bounds IDs fall back to ocean). A null occupant therefore
        // means the slot is genuinely unused -> no conflict, use the configured ID as-is.
        BiomeGenBase occupant = BiomeGenBase.getBiome(configuredID);
        if (occupant != null && occupant != BiomeGenBase.ocean) {
            if (BIOMES.containsValue(occupant)) {
                String owner = findOwnerName(occupant);
                GCLog.severe(
                    "[ExtraPlanets] BIOME ID CONFLICT: the '" + biomeName
                        + "' biome is set to ID "
                        + configuredID
                        + " in config/ExtraPlanets.cfg, but that slot is already used by the ExtraPlanets biome '"
                        + owner
                        + "'. Both share the same ID, which will corrupt generated terrain. "
                        + "To resolve this, change one of the two '... Biome ID' values in config/ExtraPlanets.cfg "
                        + "to an unused number in the range 0-"
                        + maxID
                        + " and restart.");
            } else {
                String occupantName = occupant.getClass()
                    .getSimpleName();
                GCLog.severe(
                    "[ExtraPlanets] BIOME ID CONFLICT: the '" + biomeName
                        + "' biome is set to ID "
                        + configuredID
                        + " in config/ExtraPlanets.cfg, but that slot is already used by the biome '"
                        + occupantName
                        + "'. Both share the same ID, which can corrupt generated terrain. "
                        + "To resolve this, change the '"
                        + biomeName
                        + " Biome ID' value in config/ExtraPlanets.cfg to an unused number in the range 0-"
                        + maxID
                        + " and restart.");
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
        final int arrayLen = BiomeGenBase.getBiomeGenArray().length;
        // Skip slot 0 (vanilla ocean biome). A slot is free only when biomeList[id] is null
        // (BiomeGenBase.getBiome returns null for an in-range empty slot). After EndlessIDs (or
        // similar extenders) patching the array, there may be thousands of free slots > 255 - the bound is always
        // read from the live array, never hardcoded.
        for (int id = 1; id < arrayLen; id++) {
            if (BiomeGenBase.getBiome(id) == null) {
                return id;
            }
        }
        // Nothing left. Taking slot 0 would replace the ocean in every world, which is a far worse outcome than a
        // crash on the one planet that needed a biome, so fail loudly instead of corrupting the save.
        throw new IllegalStateException(
            "[ExtraPlanets] No free biome IDs remain in the whole 0-" + (arrayLen - 1)
                + " range, so this biome cannot be registered. "
                + "Another mod has claimed every slot; remove a few biome-adding mods or install an ID extender such "
                + "as EndlessIDs, then restart.");
    }
}
