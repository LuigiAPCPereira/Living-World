package dev.signalshards.livingworld.features.climate.domain;

public enum ThermalBand {
    CONGELANTE,
    FRIO,
    TEMPERADO,
    QUENTE,
    ESCALDANTE;

    public ThermalBand shift(int amount) {
        int target = Math.max(0, Math.min(values().length - 1, ordinal() + amount));
        return values()[target];
    }
}
