package com.mjr.extraplanets;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

/**
 * Static guard over {@code SkyProviderHandler}: every body must render its own sky.
 *
 * <p>
 * The handler is a long chain of {@code world.provider instanceof WorldProviderX} blocks, each constructing a
 * sky renderer. Two of them were wrong in exactly the same way and neither was visible anywhere - the code
 * compiled, ran, and simply drew the wrong planets: Europa constructed {@code SkyProviderGanymede} and Titan
 * constructed {@code SkyProviderEris}, which put Pluto, Dysnomia and Earth in Titan's sky while the two correct
 * renderers sat unused in the same packages. Nothing could catch it, because the handler needs a live client.
 * </p>
 *
 * <p>
 * So this test reads the source instead and requires the mapping to be the identity: the block for
 * {@code WorldProviderX} must construct {@code SkyProviderX}. Orbit providers are exempt because they legitimately
 * all share {@code SkyProviderOrbit}.
 * </p>
 */
public class SkyProviderWiringTest {

    private static final Pattern BRANCH = Pattern.compile("world\\.provider instanceof WorldProvider(\\w+)\\)");
    private static final Pattern RENDERER = Pattern.compile("new SkyProvider(\\w+)\\(");

    @Test
    public void everyBodyRendersItsOwnSky() throws IOException {
        Map<String, String> wiring = readWiring();
        Assert.assertFalse("no sky-provider branches were found; the parser is out of date", wiring.isEmpty());

        List<String> wrong = new ArrayList<String>();
        for (Map.Entry<String, String> entry : wiring.entrySet()) {
            String provider = entry.getKey();
            String renderer = entry.getValue();
            if (provider.endsWith("Orbit")) {
                Assert.assertEquals(provider + " must use the generic orbital sky", "Orbit", renderer);
                continue;
            }
            if (!provider.equals(renderer)) {
                wrong.add("WorldProvider" + provider + " -> SkyProvider" + renderer);
            }
        }

        Assert.assertEquals(
            "each celestial body must render its own sky; a copy-paste here is invisible in game:\n"
                + String.join("\n", wrong),
            0,
            wrong.size());
    }

    @Test
    public void everyBodyHasASkyAtAll() throws IOException {
        // The other half of the same bug: a body with no branch at all silently gets the default sky.
        Map<String, String> wiring = readWiring();
        List<String> missing = new ArrayList<String>();
        for (String provider : expectedProviders()) {
            if (!wiring.containsKey(provider)) {
                missing.add(provider);
            }
        }
        Assert.assertEquals("celestial bodies with no sky provider: " + missing, 0, missing.size());
    }

    // ---------------------------------------------------------------------------------------------------------
    // Parsing
    // ---------------------------------------------------------------------------------------------------------

    /** @return provider suffix (e.g. {@code Titan}) to renderer suffix (e.g. {@code Titan}). */
    private static Map<String, String> readWiring() throws IOException {
        File file = new File(
            projectDir(),
            "src/main/java/com/mjr/extraplanets/client/handlers/SkyProviderHandler.java");
        Assert.assertTrue("missing " + file.getAbsolutePath(), file.isFile());
        String source = new String(Files.readAllBytes(file.toPath()), Charset.forName("UTF-8"));

        Map<String, String> wiring = new LinkedHashMap<String, String>();
        Matcher branches = BRANCH.matcher(source);
        List<int[]> ranges = new ArrayList<int[]>();
        List<String> names = new ArrayList<String>();
        while (branches.find()) {
            ranges.add(new int[] { branches.start(), branches.end() });
            names.add(branches.group(1));
        }

        for (int i = 0; i < ranges.size(); i++) {
            // A branch runs until the next one starts.
            int start = ranges.get(i)[1];
            int end = i + 1 < ranges.size() ? ranges.get(i + 1)[0] : source.length();
            Matcher renderer = RENDERER.matcher(source.substring(start, end));
            Assert.assertTrue("WorldProvider" + names.get(i) + " never sets a sky renderer", renderer.find());
            Assert.assertNull(
                "WorldProvider" + names.get(i) + " is wired twice",
                wiring.put(names.get(i), renderer.group(1)));
        }
        return wiring;
    }

    /** The bodies this mod ships, taken from the world providers on disk rather than from a second hand list. */
    private static List<String> expectedProviders() throws IOException {
        File dir = new File(projectDir(), "src/main/java/com/mjr/extraplanets");
        List<String> providers = new ArrayList<String>();
        Files.walk(dir.toPath())
            .filter(
                path -> path.getFileName()
                    .toString()
                    .startsWith("WorldProvider"))
            .forEach(path -> {
                String name = path.getFileName()
                    .toString()
                    .replace("WorldProvider", "")
                    .replace(".java", "");
                // Space stations are orbit providers and are covered by the same identity rule.
                providers.add(name);
            });
        Assert.assertFalse("no world providers found", providers.isEmpty());
        return providers;
    }

    private static String projectDir() {
        String dir = System.getProperty("extraplanets.projectDir");
        Assert.assertNotNull("-Dextraplanets.projectDir must be set by addon.gradle.kts", dir);
        return dir;
    }
}
