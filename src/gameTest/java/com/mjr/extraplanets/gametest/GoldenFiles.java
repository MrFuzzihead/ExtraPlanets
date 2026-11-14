package com.mjr.extraplanets.gametest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * The golden-file machinery shared by the snapshot checks.
 *
 * <p>
 * A golden is a map of stable key to hash. Checks compare their freshly computed map against the committed file and
 * report the keys that changed, appeared or disappeared; {@code -PrecordGoldens} rewrites the file instead. Recording
 * is deliberately a separate, explicit act: the only thing that makes a golden meaningful is that it was produced by
 * code that was known good.
 * </p>
 */
final class GoldenFiles {

    private GoldenFiles() {}

    static void verify(Map<String, Long> actual, String fileName, Predicate<String> belongsToThisFamily,
        String whatChanged) {
        File file = file(fileName);
        if (Boolean.getBoolean("extraplanets.recordGoldens")) {
            Map<String, Long> merged = file.isFile() ? read(file) : new TreeMap<String, Long>();
            merged.putAll(actual);
            write(file, merged);
            TestReporter.assertTrue("recorded " + merged.size() + " hashes to " + file, file.isFile());
            return;
        }

        TestReporter
            .assertTrue("missing " + file + "; record it with ./gradlew gameTestServer -PrecordGoldens", file.isFile());

        Map<String, Long> expected = read(file);
        List<String> failures = new ArrayList<String>();
        for (String key : actual.keySet()) {
            Long was = expected.get(key);
            if (was == null) {
                failures.add(key + ": no golden for this case (record goldens deliberately)");
            } else if (!was.equals(actual.get(key))) {
                failures.add(key + " changed: " + Long.toHexString(was) + " -> " + Long.toHexString(actual.get(key)));
            }
        }
        for (String key : expected.keySet()) {
            if (belongsToThisFamily.test(key) && !actual.containsKey(key)) {
                failures.add(key + ": no longer produced");
            }
        }

        TestReporter.assertEquals(whatChanged + ":\n  " + String.join("\n  ", failures), 0, failures.size());
    }

    static File file(String name) {
        String dir = System.getProperty("extraplanets.projectDir");
        TestReporter.assertNotNull("-Dextraplanets.projectDir must be set by addon.gradle.kts", dir);
        return new File(dir, "src/gameTest/resources/" + name);
    }

    private static void write(File file, Map<String, Long> hashes) {
        StringBuilder text = new StringBuilder();
        text.append("# Golden hashes - see gametest/GoldenFiles.java\n");
        text.append("# key<TAB>hash. Regenerate only with ./gradlew gameTestServer -PrecordGoldens.\n");
        for (Map.Entry<String, Long> entry : hashes.entrySet()) {
            text.append(entry.getKey())
                .append('\t')
                .append(Long.toHexString(entry.getValue()))
                .append('\n');
        }
        try {
            file.getParentFile()
                .mkdirs();
            Files.write(
                file.toPath(),
                text.toString()
                    .getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            throw new IllegalStateException("cannot write " + file, e);
        }
    }

    private static Map<String, Long> read(File file) {
        Map<String, Long> hashes = new LinkedHashMap<String, Long>();
        try {
            for (String line : Files.readAllLines(file.toPath(), Charset.forName("UTF-8"))) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\t");
                hashes.put(parts[0], Long.parseUnsignedLong(parts[1], 16));
            }
        } catch (IOException e) {
            throw new IllegalStateException("cannot read " + file, e);
        }
        return hashes;
    }
}
