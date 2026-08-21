package com.mjr.extraplanets.handlers;

import com.mjr.extraplanets.items.ExtraPlanets_Items;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerHandler.ThermalArmorEvent;
import micdoodle8.mods.galacticraft.planets.asteroids.items.AsteroidsItems;

public class MainHandler {

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
}
