package com.mjr.extraplanets.handlers;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.mjr.extraplanets.api.item.IModularArmor;
import com.mjr.extraplanets.api.world.IPressureWorld;
import com.mjr.extraplanets.api.world.ISolarRadiationWorld;
import com.mjr.extraplanets.items.ExtraPlanets_Items;
import com.mjr.extraplanets.items.armor.modules.Module;
import com.mjr.extraplanets.items.armor.modules.ModuleHelper;
import com.mjr.extraplanets.items.armor.modules.ModulePressureSeal;
import com.mjr.extraplanets.items.armor.modules.ModuleRadiationShield;
import com.mjr.extraplanets.util.DamageSourceEP;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerHandler.ThermalArmorEvent;
import micdoodle8.mods.galacticraft.planets.asteroids.items.AsteroidsItems;

public class MainHandler {

    /**
     * Per-player cache of the last known step height state, keyed by player.
     * Avoids NBT parsing every tick — only re-evaluates when the leggings slot changes.
     */
    private static final Map<EntityPlayer, StepState> stepCache = new WeakHashMap<EntityPlayer, StepState>();

    private static final class StepState {

        /** The leggings ItemStack from the previous tick (identity check). */
        ItemStack lastLeggings;
        /** Whether the Step Assist module was active the last time we checked. */
        boolean moduleActive;
        /**
         * Whether we were the ones that set stepHeight to 1.0F. If true, we must
         * reset it when the module deactivates. If false, we leave it alone — some
         * other mod may have set it.
         */
        boolean weSetIt;
        /** The tick number when we last verified the module state. */
        long lastCheckTick;
    }

    @SubscribeEvent
    public void onThermalArmorEvent(ThermalArmorEvent event) {
        if (event.armorStack == null) {
            event.setArmorAddResult(ThermalArmorEvent.ArmorAddResult.REMOVE);
            return;
        }
        if (event.armorStack.getItem() == AsteroidsItems.thermalPadding
            && event.armorStack.getItemDamage() == event.armorIndex) {
            event.setArmorAddResult(ThermalArmorEvent.ArmorAddResult.ADD);
            return;
        }
        if (event.armorStack.getItem() == ExtraPlanets_Items.tier2ThermalPadding
            && event.armorStack.getItemDamage() == event.armorIndex) {
            event.setArmorAddResult(ThermalArmorEvent.ArmorAddResult.ADD);
            return;
        }
        if (event.armorStack.getItem() == ExtraPlanets_Items.tier3ThermalPadding
            && event.armorStack.getItemDamage() == event.armorIndex) {
            event.setArmorAddResult(ThermalArmorEvent.ArmorAddResult.ADD);
            return;
        }
        if (event.armorStack.getItem() == ExtraPlanets_Items.tier4ThermalPadding
            && event.armorStack.getItemDamage() == event.armorIndex) {
            event.setArmorAddResult(ThermalArmorEvent.ArmorAddResult.ADD);
            return;
        }
        event.setArmorAddResult(ThermalArmorEvent.ArmorAddResult.NOTHING);
    }

    /**
     * Manages the Step Assist module's step height. Re-evaluates the module state
     * only when the leggings stack changes or once per second, avoiding per-tick NBT
     * parsing while still covering the "all pieces removed" edge case.
     */
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (player == null || event.phase != TickEvent.Phase.END) {
            return;
        }

        ItemStack leggings = player.inventory.armorItemInSlot(1);
        StepState state = stepCache.get(player);

        if (state == null) {
            state = new StepState();
            stepCache.put(player, state);
        }

        // Detect change: different stack in the slot, or periodic re-check (every 20 ticks)
        boolean changed = (leggings != state.lastLeggings) || (player.ticksExisted - state.lastCheckTick >= 20);

        if (changed) {
            state.lastLeggings = leggings;
            state.lastCheckTick = player.ticksExisted;
            state.moduleActive = leggings != null && leggings.getItem() instanceof IModularArmor
                && ModuleHelper.isModuleActive(leggings, "step_assist");
        }

        if (state.moduleActive) {
            // We're the active source — set stepHeight and remember it.
            player.stepHeight = 1.0F;
            state.weSetIt = true;
        } else if (state.weSetIt) {
            // We previously set stepHeight but the module is gone — reset to default.
            player.stepHeight = 0.5F;
            state.weSetIt = false;
        }
        // else: we never set stepHeight, so leave it alone (other mod may have set it)
    }

    // ===========================================================================
    // Radiation system (ported from upstream 1.12.2)
    // ===========================================================================

    /** NBT key on the player's persistent entity data for the radiation level. */
    private static final String RADIATION_NBT_KEY = "ExtraPlanetsRadiationLevel";

    public static double getRadiationLevel(EntityPlayer player) {
        if (!player.getEntityData()
            .hasKey(RADIATION_NBT_KEY)) {
            return 0;
        }
        return player.getEntityData()
            .getDouble(RADIATION_NBT_KEY);
    }

    public static void setRadiationLevel(EntityPlayer player, double level) {
        if (level < 0) {
            level = 0;
        }
        player.getEntityData()
            .setDouble(RADIATION_NBT_KEY, level);
    }

    /**
     * @return the radiation shield tier of the given armor piece, based on installed
     *         Radiation Shield modules. Returns 0 if no active module is installed.
     */
    public static int getArmorPieceRadiationTier(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof IModularArmor)) {
            return 0;
        }
        for (Module m : ModuleHelper.getModules(stack)) {
            if (m instanceof ModuleRadiationShield && m.isActive()) {
                return m.getSubType();
            }
        }
        return 0;
    }

    // ===========================================================================
    // Pressure system
    // ===========================================================================

    /** NBT key on the player's persistent entity data for the pressure level. */
    private static final String PRESSURE_NBT_KEY = "ExtraPlanetsPressureLevel";

    public static double getPressureLevel(EntityPlayer player) {
        if (!player.getEntityData()
            .hasKey(PRESSURE_NBT_KEY)) {
            return 0;
        }
        return player.getEntityData()
            .getDouble(PRESSURE_NBT_KEY);
    }

    public static void setPressureLevel(EntityPlayer player, double level) {
        if (level < 0) {
            level = 0;
        }
        player.getEntityData()
            .setDouble(PRESSURE_NBT_KEY, level);
    }

    /**
     * @return the pressure seal tier of the given armor piece, based on installed
     *         Pressure Seal modules. Returns 0 if no active module is installed.
     */
    public static int getArmorPiecePressureTier(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof IModularArmor)) {
            return 0;
        }
        for (Module m : ModuleHelper.getModules(stack)) {
            if (m instanceof ModulePressureSeal && m.isActive()) {
                return m.getSubType();
            }
        }
        return 0;
    }

    /**
     * Gets the environmental pressure level for the world the given provider is in.
     * Returns null (unknown) if the world has no pressure hazard.
     */
    private static Integer getWorldPressureLevel(net.minecraft.world.WorldProvider provider) {
        if (provider instanceof IPressureWorld) {
            int level = ((IPressureWorld) provider).getPressureLevel();
            return level <= 0 ? null : level;
        }
        // Fall back to known GC world providers
        if (provider instanceof micdoodle8.mods.galacticraft.core.dimension.WorldProviderMoon) {
            int level = com.mjr.extraplanets.Config.moonPressureAmount;
            return level <= 0 ? null : level;
        }
        if (provider instanceof micdoodle8.mods.galacticraft.planets.mars.dimension.WorldProviderMars) {
            int level = com.mjr.extraplanets.Config.marsPressureAmount;
            return level <= 0 ? null : level;
        }
        if (provider instanceof micdoodle8.mods.galacticraft.planets.asteroids.dimension.WorldProviderAsteroids) {
            int level = com.mjr.extraplanets.Config.asteroidsPressureAmount;
            return level <= 0 ? null : level;
        }
        if (provider instanceof micdoodle8.mods.galacticraft.core.dimension.WorldProviderSpaceStation) {
            int level = com.mjr.extraplanets.Config.spaceStationPressureAmount;
            return level <= 0 ? null : level;
        }
        return null;
    }

    /**
     * Checks whether the player has a full space suit set equipped with pressure protection.
     */
    private static boolean hasFullPressureSuit(EntityPlayer player) {
        ItemStack helmet = player.inventory.armorItemInSlot(3);
        ItemStack chest = player.inventory.armorItemInSlot(2);
        ItemStack leggings = player.inventory.armorItemInSlot(1);
        ItemStack boots = player.inventory.armorItemInSlot(0);
        return helmet != null && chest != null
            && leggings != null
            && boots != null
            && helmet.getItem() instanceof com.mjr.extraplanets.api.item.IPressureSuit
            && chest.getItem() instanceof com.mjr.extraplanets.api.item.IPressureSuit
            && leggings.getItem() instanceof com.mjr.extraplanets.api.item.IPressureSuit
            && boots.getItem() instanceof com.mjr.extraplanets.api.item.IPressureSuit;
    }

    @SubscribeEvent
    public void onPressureTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player == null) {
            return;
        }
        // Only run on the server
        if (event.player.worldObj.isRemote) {
            return;
        }
        EntityPlayer player = event.player;
        if (player.capabilities.isCreativeMode || player.isDead) {
            return;
        }
        if (!com.mjr.extraplanets.Config.pressure) {
            return;
        }

        // Check if the player is inside a sealed oxygen sealer room
        boolean inSealedPressureSafeRoom = false;
        if (player.worldObj.provider instanceof IGalacticraftWorldProvider) {
            micdoodle8.mods.galacticraft.core.tile.TileEntityOxygenSealer sealer = micdoodle8.mods.galacticraft.core.tile.TileEntityOxygenSealer
                .getNearestSealer(player.worldObj, player.posX, player.posY, player.posZ);
            if (sealer != null && sealer.sealed) {
                double dx = sealer.xCoord + 0.5 - player.posX;
                double dy = sealer.yCoord + 0.5 - player.posY;
                double dz = sealer.zCoord + 0.5 - player.posZ;
                if (dx * dx + dy * dy + dz * dz < 16.0 * 16.0) {
                    inSealedPressureSafeRoom = true;
                }
            }
        }

        int tick = player.ticksExisted - 1;
        Integer pressureAmount = null;

        if (player.worldObj.provider instanceof IGalacticraftWorldProvider) {
            pressureAmount = getWorldPressureLevel(player.worldObj.provider);
        }

        // Pressure decay over time when NOT in a hazardous area or inside a sealed room
        if (pressureAmount == null || inSealedPressureSafeRoom) {
            if (tick % 30 == 0) {
                if (com.mjr.extraplanets.Config.pressureOvertimeReduceAmount != 0) {
                    double temp = getPressureLevel(player);
                    double level = (temp * com.mjr.extraplanets.Config.pressureOvertimeReduceAmount) / 100;
                    if (level <= 0) {
                        setPressureLevel(player, 0);
                    } else {
                        setPressureLevel(player, getPressureLevel(player) - level);
                    }
                }
            }
        }

        // Pressure exposure check
        if (pressureAmount != null && pressureAmount > 0 && !inSealedPressureSafeRoom) {
            checkPressure(event, player, pressureAmount);
        }

        // Sync pressure level to client for HUD display (every 10 ticks)
        if (tick % 10 == 0 && !player.worldObj.isRemote) {
            double currentPressure = getPressureLevel(player);
            com.mjr.extraplanets.network.PacketSimple pkt = new com.mjr.extraplanets.network.PacketSimple(
                com.mjr.extraplanets.network.PacketSimple.EnumSimplePacket.C_UPDATE_PRESSURE,
                new Object[] { currentPressure });
            com.mjr.extraplanets.ExtraPlanets.packetPipeline.sendTo(pkt, (EntityPlayerMP) player);
        }
    }

    private void checkPressure(TickEvent.PlayerTickEvent event, EntityPlayer player, int amount) {
        // Skip if riding a vehicle
        if (player.ridingEntity != null) {
            return;
        }

        boolean doDamage = false;
        boolean doArmorCheck = false;
        double damageToTake = 0;
        double damageModifer = 0;
        int tierValue = 0;

        if (!hasFullPressureSuit(player)) {
            damageModifer = 0.1;
            doDamage = true;
        } else {
            doArmorCheck = true;
            doDamage = false;
        }

        if (doArmorCheck) {
            double helmetTier = getArmorPiecePressureTier(player.inventory.armorItemInSlot(3));
            double chestTier = getArmorPiecePressureTier(player.inventory.armorItemInSlot(2));
            double legginsTier = getArmorPiecePressureTier(player.inventory.armorItemInSlot(1));
            double bootsTier = getArmorPiecePressureTier(player.inventory.armorItemInSlot(0));

            tierValue = (int) ((helmetTier + chestTier + legginsTier + bootsTier) / 2);
            damageToTake = 0.005 * tierValue;
            doDamage = true;
        }

        if (doDamage) {
            double stats = getPressureLevel(player);
            if (stats >= 100) {
                if ((player.ticksExisted - 1) % 50 == 0) {
                    player.attackEntityFrom(DamageSourceEP.pressure, 3F);
                }
            } else if (stats >= 0) {
                double tempLevel;
                if (amount < 10) {
                    damageModifer = 0.005625 - (damageToTake / 2) / 10;
                    tempLevel = (damageModifer * amount) / 100;
                } else {
                    damageModifer = 0.001875 - (damageToTake / 2) / 10;
                    if (damageModifer < 0) {
                        damageModifer = 0.000225;
                    }
                    tempLevel = damageModifer * (amount / 10) / 6;
                }
                setPressureLevel(player, getPressureLevel(player) + tempLevel);
            } else {
                setPressureLevel(player, 0);
            }
        }
    }

    /**
     * Reduces pressure when the player wakes up from sleeping.
     */
    @SubscribeEvent
    public void onSleepInBedPressure(net.minecraftforge.event.entity.player.PlayerWakeUpEvent event) {
        if (event.entityPlayer == null || event.entityPlayer.worldObj.isRemote) {
            return;
        }
        EntityPlayer player = event.entityPlayer;
        if (!com.mjr.extraplanets.Config.pressure) {
            return;
        }
        int reduceAmount = com.mjr.extraplanets.Config.pressureSleepingReduceAmount;
        if (reduceAmount != 0) {
            double current = getPressureLevel(player);
            if (current > 0) {
                double level = (current * reduceAmount) / 100;
                if (level <= 0) {
                    setPressureLevel(player, 0);
                } else {
                    setPressureLevel(player, current - level);
                }
                player.addChatMessage(
                    new net.minecraft.util.ChatComponentText(
                        StatCollector.translateToLocal("gui.pressure.slept.message")));
                player.addChatMessage(
                    new net.minecraft.util.ChatComponentText(
                        StatCollector.translateToLocal("gui.pressure.current.message") + ": "
                            + (int) getPressureLevel(player)
                            + "/100"));
            }
        }
    }

    /**
     * Warns the player about pressure levels when entering a hazardous dimension.
     */
    @SubscribeEvent
    public void onWorldChangePressure(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player == null || event.player.worldObj.isRemote) {
            return;
        }
        if (!com.mjr.extraplanets.Config.pressure) {
            return;
        }
        EntityPlayer player = event.player;
        Integer amount = getWorldPressureLevel(player.worldObj.provider);
        if (amount != null) {
            player.addChatMessage(
                new net.minecraft.util.ChatComponentText(
                    StatCollector.translateToLocal("gui.pressure.subject.message") + " "
                        + amount
                        + "/100% "
                        + StatCollector.translateToLocal("gui.pressure.type.message")));
            player.addChatMessage(
                new net.minecraft.util.ChatComponentText(
                    StatCollector.translateToLocal("gui.pressure.reverse.message") + "!"));
            player.addChatMessage(
                new net.minecraft.util.ChatComponentText(
                    StatCollector.translateToLocal("gui.pressure.cancel.message") + "!"));
        }
    }

    /**
     * Gets the solar radiation level for the world the given provider is in.
     * Returns null (unknown) if the world has no radiation.
     */
    private static Integer getWorldRadiationLevel(net.minecraft.world.WorldProvider provider) {
        if (provider instanceof ISolarRadiationWorld) {
            int level = ((ISolarRadiationWorld) provider).getSolarRadiationLevel();
            return level <= 0 ? null : level;
        }
        // Fall back to known GC world providers
        if (provider instanceof micdoodle8.mods.galacticraft.core.dimension.WorldProviderMoon) {
            int level = com.mjr.extraplanets.Config.moonRadiationAmount;
            return level <= 0 ? null : level;
        }
        if (provider instanceof micdoodle8.mods.galacticraft.planets.mars.dimension.WorldProviderMars) {
            int level = com.mjr.extraplanets.Config.marsRadiationAmount;
            return level <= 0 ? null : level;
        }
        if (provider instanceof micdoodle8.mods.galacticraft.planets.asteroids.dimension.WorldProviderAsteroids) {
            int level = com.mjr.extraplanets.Config.asteroidsRadiationAmount;
            return level <= 0 ? null : level;
        }
        if (provider instanceof micdoodle8.mods.galacticraft.core.dimension.WorldProviderSpaceStation) {
            int level = com.mjr.extraplanets.Config.spaceStationRadiationAmount;
            return level <= 0 ? null : level;
        }
        return null;
    }

    /**
     * Checks whether the player has a full space suit set equipped. Returns true if all
     * four armor slots are filled with IRadiationSuit items.
     */
    private static boolean hasFullSpaceSuit(EntityPlayer player) {
        ItemStack helmet = player.inventory.armorItemInSlot(3);
        ItemStack chest = player.inventory.armorItemInSlot(2);
        ItemStack leggings = player.inventory.armorItemInSlot(1);
        ItemStack boots = player.inventory.armorItemInSlot(0);
        return helmet != null && chest != null
            && leggings != null
            && boots != null
            && helmet.getItem() instanceof com.mjr.extraplanets.api.item.IRadiationSuit
            && chest.getItem() instanceof com.mjr.extraplanets.api.item.IRadiationSuit
            && leggings.getItem() instanceof com.mjr.extraplanets.api.item.IRadiationSuit
            && boots.getItem() instanceof com.mjr.extraplanets.api.item.IRadiationSuit;
    }

    @SubscribeEvent
    public void onRadiationTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player == null) {
            return;
        }
        // Only run on the server — client-side NBT changes wouldn't get saved anyway
        if (event.player.worldObj.isRemote) {
            return;
        }
        EntityPlayer player = event.player;
        if (player.capabilities.isCreativeMode || player.isDead) {
            return;
        }
        if (!com.mjr.extraplanets.Config.radiation) {
            return;
        }

        // Check if the player is inside a sealed oxygen sealer room with a thermal regulator.
        // We check if the nearest active sealer within 16 blocks is sealed and has a thermal
        // regulator installed. This is a reasonable approximation for "inside a sealed room".
        boolean inSealedRadiationSafeRoom = false;
        if (player.worldObj.provider instanceof IGalacticraftWorldProvider) {
            micdoodle8.mods.galacticraft.core.tile.TileEntityOxygenSealer sealer = micdoodle8.mods.galacticraft.core.tile.TileEntityOxygenSealer
                .getNearestSealer(player.worldObj, player.posX, player.posY, player.posZ);
            if (sealer != null && sealer.sealed && sealer.thermalControlEnabled()) {
                // Verify the player is close enough to actually be inside the sealed area
                double dx = sealer.xCoord + 0.5 - player.posX;
                double dy = sealer.yCoord + 0.5 - player.posY;
                double dz = sealer.zCoord + 0.5 - player.posZ;
                double distSq = dx * dx + dy * dy + dz * dz;
                if (distSq < 16.0 * 16.0) {
                    inSealedRadiationSafeRoom = true;
                }
            }
            if (player.ticksExisted % 200 == 0 && sealer != null) {
                System.out.println(
                    "[EP] Sealer check: found=" + (sealer != null)
                        + " sealed="
                        + sealer.sealed
                        + " thermal="
                        + sealer.thermalControlEnabled()
                        + " safeRoom="
                        + inSealedRadiationSafeRoom);
            }
        }

        int tick = player.ticksExisted - 1;
        Integer radiationAmount = null;

        if (player.worldObj.provider instanceof IGalacticraftWorldProvider) {
            radiationAmount = getWorldRadiationLevel(player.worldObj.provider);
        }

        // Radiation decay over time when NOT in a radioactive area or inside a sealed safe room
        if (radiationAmount == null || inSealedRadiationSafeRoom) {
            if (tick % 30 == 0) {
                if (com.mjr.extraplanets.Config.radiationOvertimeReduceAmount != 0) {
                    double temp = getRadiationLevel(player);
                    double level = (temp * com.mjr.extraplanets.Config.radiationOvertimeReduceAmount) / 100;
                    if (level <= 0) {
                        setRadiationLevel(player, 0);
                    } else {
                        setRadiationLevel(player, getRadiationLevel(player) - level);
                    }
                }
            }
        }

        // Radiation exposure check every tick (matching upstream frequency)
        // Skip if in a sealed safe room with thermal regulator
        if (radiationAmount != null && radiationAmount > 0 && !inSealedRadiationSafeRoom) {
            checkRadiation(event, player, radiationAmount);
        }

        // Sync radiation level to client for HUD display (every 10 ticks)
        if (tick % 10 == 0 && !player.worldObj.isRemote) {
            double currentRad = getRadiationLevel(player);
            com.mjr.extraplanets.network.PacketSimple pkt = new com.mjr.extraplanets.network.PacketSimple(
                com.mjr.extraplanets.network.PacketSimple.EnumSimplePacket.C_UPDATE_RADIATION,
                new Object[] { currentRad });
            com.mjr.extraplanets.ExtraPlanets.packetPipeline.sendTo(pkt, (EntityPlayerMP) player);
        }
    }

    private void checkRadiation(TickEvent.PlayerTickEvent event, EntityPlayer player, int amount) {
        // Skip if riding a vehicle (lander/rocket)
        if (player.ridingEntity != null) {
            return;
        }

        boolean doDamage = false;
        boolean doArmorCheck = false;
        double damageToTake = 0;
        double damageModifer = 0;
        int tierValue = 0;

        if (!hasFullSpaceSuit(player)) {
            damageModifer = 0.1;
            doDamage = true;
        } else {
            doArmorCheck = true;
            doDamage = false;
        }

        if (doArmorCheck) {
            double helmetTier = getArmorPieceRadiationTier(player.inventory.armorItemInSlot(3));
            double chestTier = getArmorPieceRadiationTier(player.inventory.armorItemInSlot(2));
            double legginsTier = getArmorPieceRadiationTier(player.inventory.armorItemInSlot(1));
            double bootsTier = getArmorPieceRadiationTier(player.inventory.armorItemInSlot(0));

            tierValue = (int) ((helmetTier + chestTier + legginsTier + bootsTier) / 2);
            damageToTake = 0.005 * tierValue;
            doDamage = true;
        }

        if (doDamage) {
            double stats = getRadiationLevel(player);
            if (stats >= 100) {
                if ((player.ticksExisted - 1) % 50 == 0) {
                    player.attackEntityFrom(DamageSourceEP.radiation, 3F);
                }
            } else if (stats >= 0) {
                double tempLevel;
                if (amount < 10) {
                    damageModifer = 0.005625 - (damageToTake / 2) / 10;
                    tempLevel = (damageModifer * amount) / 100;
                } else {
                    damageModifer = 0.001875 - (damageToTake / 2) / 10;
                    if (damageModifer < 0) {
                        damageModifer = 0.000225;
                    }
                    tempLevel = damageModifer * (amount / 10) / 6;
                }
                setRadiationLevel(player, getRadiationLevel(player) + tempLevel);
            } else {
                setRadiationLevel(player, 0);
            }
        }
    }

    /**
     * Reduces radiation when the player wakes up from sleeping.
     */
    @SubscribeEvent
    public void onSleepInBed(net.minecraftforge.event.entity.player.PlayerWakeUpEvent event) {
        if (event.entityPlayer == null || event.entityPlayer.worldObj.isRemote) {
            return;
        }
        EntityPlayer player = event.entityPlayer;
        if (!com.mjr.extraplanets.Config.radiation) {
            return;
        }
        int reduceAmount = com.mjr.extraplanets.Config.radiationSleepingReduceAmount;
        if (reduceAmount != 0) {
            double current = getRadiationLevel(player);
            if (current > 0) {
                double level = (current * reduceAmount) / 100;
                if (level <= 0) {
                    setRadiationLevel(player, 0);
                } else {
                    setRadiationLevel(player, current - level);
                }
                player.addChatMessage(
                    new net.minecraft.util.ChatComponentText(
                        StatCollector.translateToLocal("gui.radiation.slept.message")));
                player.addChatMessage(
                    new net.minecraft.util.ChatComponentText(
                        StatCollector.translateToLocal("gui.radiation.current.message") + ": "
                            + (int) getRadiationLevel(player)
                            + "/100"));
            }
        }
    }

    /**
     * Warns the player about radiation levels when entering a radioactive dimension.
     */
    @SubscribeEvent
    public void onWorldChange(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player == null || event.player.worldObj.isRemote) {
            return;
        }
        if (!com.mjr.extraplanets.Config.radiation) {
            return;
        }
        EntityPlayer player = event.player;
        Integer amount = getWorldRadiationLevel(player.worldObj.provider);
        if (amount != null) {
            player.addChatMessage(
                new net.minecraft.util.ChatComponentText(
                    StatCollector.translateToLocal("gui.radiation.subject.message") + " "
                        + amount
                        + "/100% "
                        + StatCollector.translateToLocal("gui.radiation.type.message")));
            player.addChatMessage(
                new net.minecraft.util.ChatComponentText(
                    StatCollector.translateToLocal("gui.radiation.reverse.message") + "!"));
            player.addChatMessage(
                new net.minecraft.util.ChatComponentText(
                    StatCollector.translateToLocal("gui.radiation.cancel.message") + "!"));
        }
    }

    // ===========================================================================
    // Debug commands — registered in ExtraPlanets.serverStarting via CommandEP
    // ===========================================================================
}
