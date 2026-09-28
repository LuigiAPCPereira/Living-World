package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

/**
 * Uma peça equipada relevante ao cálculo térmico.
 */
public record EquippedArmorPiece(
        ArmorSlot slot,
        ArmorThermalMaterial material
) {
    public EquippedArmorPiece {
        Objects.requireNonNull(slot, "slot");
        Objects.requireNonNull(material, "material");
    }
}
