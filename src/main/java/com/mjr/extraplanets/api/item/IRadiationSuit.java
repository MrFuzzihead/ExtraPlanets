package com.mjr.extraplanets.api.item;

/**
 * Interface for armor that protects against radiation. The returned tier is used by the
 * radiation system to determine the level of protection. Identical to the 1.12.2 interface.
 */
public interface IRadiationSuit {

    public int getArmorTier();
}
