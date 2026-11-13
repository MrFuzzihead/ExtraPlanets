package com.mjr.extraplanets;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test helper: snapshot/restore every {@code public static int} field on {@link Config}.
 *
 * <p>
 * {@code Config} is a 118 KB bag of mutable public statics that every other class reads directly. A test that
 * calls {@link Config#init()} (which reads and writes {@code config/ExtraPlanets.cfg}) therefore changes global
 * state for every test that runs afterwards. Restoring the snapshot in {@code @After} keeps the suite
 * order-independent without having to fork a JVM per class.
 * </p>
 */
public final class ConfigState {

    /** The (reflection-order) names of the synced fields, as declared by Config itself. */
    public static final String[] SYNC_FIELD_NAMES = readPrivateSyncNameArray();

    private final Map<String, Integer> intValues = new HashMap<String, Integer>();
    private final Map<String, Object> otherValues = new HashMap<String, Object>();

    private ConfigState() {}

    /** Captures the current value of every public static field of {@link Config}. */
    public static ConfigState snapshot() {
        ConfigState state = new ConfigState();
        for (Field field : Config.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            try {
                if (field.getType() == int.class) {
                    state.intValues.put(field.getName(), field.getInt(null));
                } else {
                    state.otherValues.put(field.getName(), field.get(null));
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("cannot read Config." + field.getName(), e);
            }
        }
        return state;
    }

    /** Puts every captured value back. */
    public void restore() {
        for (Map.Entry<String, Integer> entry : intValues.entrySet()) {
            try {
                Config.class.getField(entry.getKey())
                    .setInt(null, entry.getValue());
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("cannot restore Config." + entry.getKey(), e);
            }
        }
        for (Map.Entry<String, Object> entry : otherValues.entrySet()) {
            try {
                Config.class.getField(entry.getKey())
                    .set(null, entry.getValue());
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("cannot restore Config." + entry.getKey(), e);
            }
        }
    }

    /** @return the current value of the named {@code public static int} field. */
    public static int getInt(String name) {
        try {
            return Config.class.getField(name)
                .getInt(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot read Config." + name, e);
        }
    }

    /** Sets the named {@code public static int} field. */
    public static void setInt(String name, int value) {
        try {
            Config.class.getField(name)
                .setInt(null, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot write Config." + name, e);
        }
    }

    /**
     * The sync field list is private; the contract tests need the names in the exact order the packets use.
     */
    @SuppressWarnings("unchecked")
    private static String[] readPrivateSyncNameArray() {
        try {
            Field field = Config.class.getDeclaredField("SYNC_CONFIG_FIELD_NAMES");
            field.setAccessible(true);
            String[] names = (String[]) field.get(null);
            return names.clone();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Config.SYNC_CONFIG_FIELD_NAMES is gone", e);
        }
    }

    /** @return every {@code public static int} field name on Config, in declaration order. */
    public static List<String> intFieldNames() {
        List<String> names = new ArrayList<String>();
        for (Field field : Config.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == int.class) names.add(field.getName());
        }
        return names;
    }
}
