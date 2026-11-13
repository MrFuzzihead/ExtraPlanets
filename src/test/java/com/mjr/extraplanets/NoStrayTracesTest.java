package com.mjr.extraplanets;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Static guard over {@code src/main/java}: no {@code printStackTrace()} and no direct {@code System.out}/
 * {@code System.err} writes.
 *
 * <p>
 * This is a lint test, not a behavioural one, and it exists because the rule is easy to break and expensive to
 * notice: a {@code printStackTrace()} in a {@code catch} block writes to the console, so the trace never reaches
 * {@code logs/fml-*.log} and a user crash report contains no evidence of what went wrong. All seven of them were
 * removed in the P2 pass (see {@code CODE_REVIEW.md} §6.2); this test makes sure they do not come back.
 * </p>
 *
 * <p>
 * Uses {@code extraplanets.projectDir}, which {@code addon.gradle.kts} passes to the test JVM, because the tests run
 * with their working directory redirected to {@code build/test-workdir}.
 * </p>
 */
public class NoStrayTracesTest {

    /**
     * The one permitted stdout write: {@code SmokeTestHook}'s marker is the CI contract for
     * {@code ./gradlew runServer -PsmokeTest}, so it has to be greppable from the process's own output.
     */
    private static final String ALLOWED_FILE = "SmokeTestHook.java";

    @Test
    public void noSourceFilePrintsAStackTraceOrWritesToStdout() throws IOException {
        List<File> sources = javaSources();
        Assert.assertFalse("the source tree was not found; is extraplanets.projectDir set?", sources.isEmpty());

        List<String> offences = new ArrayList<String>();
        for (File file : sources) {
            List<String> lines = Files.readAllLines(file.toPath(), Charset.forName("UTF-8"));
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String trimmed = line.trim();
                if (trimmed.startsWith("*") || trimmed.startsWith("//") || trimmed.startsWith("/*")) {
                    continue; // javadoc and comments may name the thing they forbid
                }
                if (line.contains("printStackTrace(")) {
                    offences.add(relativise(file) + ":" + (i + 1) + "  printStackTrace()");
                }
                boolean stdout = line.contains("System.out.print") || line.contains("System.err.print");
                if (stdout && !file.getName()
                    .equals(ALLOWED_FILE)) {
                    offences.add(relativise(file) + ":" + (i + 1) + "  writes to stdout/stderr");
                }
            }
        }

        Assert.assertEquals(
            "Use ExtraPlanetsLog (info/warn/severe, with the throwable) instead of writing to the console:\n"
                + String.join("\n", offences),
            0,
            offences.size());
    }

    @Test
    public void theSourceScanActuallySeesTheSources() throws IOException {
        // Guards the guard: a silently empty file list would make the test above pass forever.
        Assert.assertTrue("expected a few hundred production sources", javaSources().size() > 200);
    }

    private static List<File> javaSources() throws IOException {
        String projectDir = System.getProperty("extraplanets.projectDir");
        Assert.assertNotNull("-Dextraplanets.projectDir must be set by addon.gradle.kts", projectDir);

        File root = new File(projectDir, "src/main/java");
        Assert.assertTrue("missing " + root.getAbsolutePath(), root.isDirectory());

        List<File> sources = new ArrayList<File>();
        Files.walk(root.toPath())
            .filter(
                path -> path.toString()
                    .endsWith(".java"))
            .forEach(path -> sources.add(path.toFile()));
        return sources;
    }

    private static String relativise(File file) {
        String path = file.getPath()
            .replace('\\', '/');
        int index = path.indexOf("src/main/java/");
        return index < 0 ? path : path.substring(index + "src/main/java/".length());
    }
}
