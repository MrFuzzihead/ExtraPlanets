package com.mjr.extraplanets.proxy;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * Guards the proxy seam that keeps client-only classes out of the common code.
 *
 * <p>
 * Several item classes used to reach for {@code FMLClientHandler} to build a tooltip: the rockets constructed a
 * throw-away entity in {@code getWorldClient()} to read a tank capacity, and the space suit read the sneak key
 * binding. A tooltip is never drawn on a dedicated server, but the reference still sat in classes the server loads -
 * the same latent {@code NoClassDefFoundError} shape as the vehicle code that was fixed earlier.
 * </p>
 *
 * <p>
 * {@link CommonProxy#getClientWorld()} and {@link CommonProxy#getSneakKeyName()} replace them. This test pins both
 * ends: the server-side defaults must be safe, and no item or armour class may reach for the client again. The
 * remaining uses in common code are listed with the guard that makes each one safe, so a new one has to be added
 * deliberately - and the check is a file list rather than a per-line analysis, so a *new unguarded use inside an
 * already-listed file* would still slip through.
 * </p>
 */
public class ClientOnlySeamTest {

    /** Remaining {@code FMLClientHandler} uses in common code, each with the guard that makes it safe. */
    private static final Set<String> ALLOWED = new LinkedHashSet<String>(
        Arrays.asList(
            // worldObj.isRemote && ... on the same line.
            "entities/vehicles/EntityVehicleBase.java",
            "entities/vehicles/EntityPoweredVehicleBase.java",
            "tileEntities/machines/TileEntitySolar.java",
            "tileEntities/machines/TileEntityPoweredChargingPad.java",
            "tileEntities/machines/TileEntityTier2LandingPad.java",
            "tileEntities/machines/TileEntityTier3LandingPad.java",
            // Inside @SideOnly(Side.CLIENT) methods, which the side transformer strips.
            "network/PacketSimple.java",
            "planets/KuiperBelt/KuiperBeltEvents.java"));

    @Test
    public void theServerSideProxyDefaultsAreSafe() {
        // These are what a dedicated server actually calls, so they must not touch anything client-only.
        CommonProxy proxy = new CommonProxy();
        Assert.assertNull("a dedicated server has no client world", proxy.getClientWorld());
        Assert.assertNotNull("the tooltip fallback must still render", proxy.getSneakKeyName());
        Assert.assertFalse(
            "the tooltip fallback must not be empty",
            proxy.getSneakKeyName()
                .isEmpty());
    }

    @Test
    public void noItemOrArmourClassReachesForTheClient() {
        List<String> offenders = new ArrayList<String>();
        for (File file : javaSources()) {
            String path = relativise(file);
            if (!path.startsWith("items/") && !path.startsWith("armor/")) {
                continue;
            }
            String source = stripComments(read(file));
            for (String marker : new String[] { "FMLClientHandler", "getWorldClient", "Minecraft.getMinecraft" }) {
                if (source.contains(marker)) {
                    offenders.add(path + " uses " + marker);
                }
            }
        }
        Assert.assertEquals(
            "Items and armour must go through CommonProxy for client-only state:\n  " + String.join("\n  ", offenders),
            0,
            offenders.size());
    }

    @Test
    public void theRemainingClientCallsInCommonCodeAreTheDocumentedOnes() {
        Set<String> found = new LinkedHashSet<String>();
        for (File file : javaSources()) {
            String path = relativise(file);
            if (path.startsWith("client/") || path.contains("SkyProvider")) {
                continue;
            }
            if (stripComments(read(file)).contains("FMLClientHandler")) {
                found.add(path);
            }
        }

        Set<String> unexpected = new LinkedHashSet<String>(found);
        unexpected.removeAll(ALLOWED);
        Set<String> stale = new LinkedHashSet<String>(ALLOWED);
        stale.removeAll(found);

        Assert.assertEquals(
            "common code that reaches for the client without a documented guard: " + unexpected,
            0,
            unexpected.size());
        Assert.assertEquals("entries in the allowlist that no longer use the client: " + stale, 0, stale.size());
    }

    // ---------------------------------------------------------------------------------------------------------
    // Source access - shared style with NoStrayTracesTest
    // ---------------------------------------------------------------------------------------------------------

    private static String read(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new IllegalStateException("cannot read " + file, e);
        }
    }

    private static String stripComments(String source) {
        return source.replaceAll("(?s)/\\*.*?\\*/", "")
            .replaceAll("//[^\\n]*", "");
    }

    private static List<File> javaSources() {
        String dir = System.getProperty("extraplanets.projectDir");
        Assert.assertNotNull("-Dextraplanets.projectDir must be set by addon.gradle.kts", dir);
        File root = new File(dir, "src/main/java");
        Assert.assertTrue("missing " + root.getAbsolutePath(), root.isDirectory());

        List<File> sources = new ArrayList<File>();
        collect(root, sources);
        Assert.assertTrue("expected a few hundred sources", sources.size() > 200);
        return sources;
    }

    private static void collect(File dir, List<File> out) {
        File[] children = dir.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                collect(child, out);
            } else if (child.getName()
                .endsWith(".java")) {
                    out.add(child);
                }
        }
    }

    private static String relativise(File file) {
        String path = file.getPath()
            .replace('\\', '/');
        int index = path.indexOf("com/mjr/extraplanets/");
        return index < 0 ? path : path.substring(index + "com/mjr/extraplanets/".length());
    }
}
