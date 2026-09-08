package com.mjr.extraplanets.items.armor.modules;

import java.util.ArrayList;
import java.util.List;

/**
 * Central registry for all suit module prototypes. Modules are registered here and referenced
 * by their {@link Module#getName()} key from ItemStack NBT.
 */
public class ExtraPlanets_Modules {

    public static final List<Module> modules = new ArrayList<Module>();

    public static void init() {
        // -- Universal ----------------------------------------------------------
        modules.add(new ModuleRadiationShield("radiation_shield"));
        modules.add(new ModulePressureSeal("pressure_seal"));
        modules.add(new ModuleProtectionPlus("protection_plus_diamond", 1));
        modules.add(new ModuleBatteryExpansion("battery_expansion"));

        // -- Helmet (slot 3) ----------------------------------------------------
        modules.add(new ModuleNightVision("night_vision"));
        modules.add(new ModuleSensorGlasses("sensor_glasses"));
        modules.add(new ModuleSolarPanel("basic_solar_panel", 0));
        modules.add(new ModuleSolarPanel("advanced_solar_panel", 1));

        // -- Chestplate (slot 2) ------------------------------------------------
        modules.add(new ModuleJetpack("jetpack_t1", 1));
        modules.add(new ModuleJetpack("jetpack_t2", 2));

        // -- Leggings (slot 1) --------------------------------------------------
        modules.add(new ModuleSpeedBoost("speed_boost_t1", 1));
        modules.add(new ModuleSpeedBoost("speed_boost_t2", 2));
        modules.add(new ModuleStepAssist("step_assist"));

        // -- Boots (slot 0) -----------------------------------------------------
        modules.add(new ModuleGravityController("gravity_controller"));
        modules.add(new ModuleEnhancedGravity("enhanced_gravity"));
        modules.add(new ModuleAntiGravityJump("anti_gravity_jump"));
        modules.add(new ModuleNoFallDamage("no_fall_damage"));
    }

    public static List<Module> getModules() {
        return modules;
    }

    public static void addModule(Module module) {
        modules.add(module);
    }

    public static void removeModule(Module module) {
        modules.remove(module);
    }

    /**
     * Looks up a module prototype by its name key.
     *
     * @return the module instance, or null if not found.
     */
    public static Module getModuleByName(String name) {
        for (Module m : modules) {
            if (m.getName()
                .equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }
}
