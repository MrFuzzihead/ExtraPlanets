package com.mjr.extraplanets;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

/**
 * Static guard over {@code world/features}: every structure generator must be called by something.
 *
 * <p>
 * A {@code WorldGenerator} subclass is only ever reached from a biome decorator (or {@code WorldGenHelper}), never
 * from a registry, so an orphan is completely silent: the class compiles, the mod loads, and the structure simply
 * never appears in any world. That is exactly what happened to {@code WorldGenDeshTower} - added to
 * {@code BiomeDecoratorCeres} in c1192dc, then left behind when that decorator was rewritten, which removed the
 * Tower of Desh from every world generated since without a single error.
 * </p>
 *
 * <p>
 * This test is deliberately generic rather than a list of known structures: any new generator that nothing calls
 * fails the build, and it is checked by reading the sources because a decorator call is not observable from a plain
 * JVM.
 * </p>
 */
public class StructureWiringTest {

    @Test
    public void everyStructureGeneratorIsCalledFromSomewhere() throws IOException {
        List<String> orphaned = new ArrayList<String>();
        for (String generator : generators()) {
            int references = 0;
            for (String source : otherSources(generator)) {
                if (source.contains(generator)) {
                    references++;
                }
            }
            if (references == 0) {
                orphaned.add(generator);
            }
        }

        Assert.assertEquals(
            "structure generators nothing calls (they will never appear in a world):\n  "
                + String.join("\n  ", orphaned),
            0,
            orphaned.size());
    }

    @Test
    public void theScanActuallyFindsTheGenerators() throws IOException {
        // Guards the guard: an empty generator list would make the test above pass forever. There are ~19 of them.
        Assert.assertTrue("expected the world/features package", generators().size() >= 15);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------------------

    /** @return the simple class names of every file in {@code world/features}. */
    private static List<String> generators() throws IOException {
        File dir = new File(projectDir(), "src/main/java/com/mjr/extraplanets/world/features");
        Assert.assertTrue("missing " + dir.getAbsolutePath(), dir.isDirectory());
        try (Stream<Path> files = Files.list(dir.toPath())) {
            return files.map(
                path -> path.getFileName()
                    .toString())
                .filter(name -> name.endsWith(".java"))
                .map(name -> name.substring(0, name.length() - ".java".length()))
                .sorted()
                .collect(Collectors.toList());
        }
    }

    /** @return the contents of every production source except the definition of {@code generator}. */
    private static List<String> otherSources(String generator) throws IOException {
        File root = new File(projectDir(), "src/main/java");
        try (Stream<Path> files = Files.walk(root.toPath())) {
            return files.filter(
                path -> path.toString()
                    .endsWith(".java"))
                .filter(
                    path -> !path.getFileName()
                        .toString()
                        .equals(generator + ".java"))
                .map(StructureWiringTest::readQuietly)
                .map(StructureWiringTest::stripCommentsAndImports)
                .collect(Collectors.toList());
        }
    }

    /**
     * A mention is only evidence of a call site if it is code: an unused {@code import} or a comment saying "this
     * class was orphaned" must not satisfy the check. (The first version of this test counted imports, and happily
     * passed with the generator deleted from its decorator.)
     */
    private static String stripCommentsAndImports(String source) {
        return source.replaceAll("(?m)^\\s*import [^;]+;\\s*$", "")
            .replaceAll("(?m)^\\s*(?:/\\*|\\*|//).*$", "")
            .replaceAll("//[^\\n]*", "");
    }

    private static String readQuietly(Path path) {
        try {
            return new String(Files.readAllBytes(path), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new IllegalStateException("cannot read " + path, e);
        }
    }

    private static String projectDir() {
        String dir = System.getProperty("extraplanets.projectDir");
        Assert.assertNotNull("-Dextraplanets.projectDir must be set by addon.gradle.kts", dir);
        return dir;
    }
}
