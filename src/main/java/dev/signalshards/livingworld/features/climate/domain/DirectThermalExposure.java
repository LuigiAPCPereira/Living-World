package dev.signalshards.livingworld.features.climate.domain;

/**
 * Exposição térmica física direta já resolvida pelo adapter.
 *
 * <p>Água permanece em WaterExposure porque possui fração/profundidade/wetness próprios.</p>
 */
public enum DirectThermalExposure {
    NONE,
    FIRE,
    LAVA,
    POWDER_SNOW
}
