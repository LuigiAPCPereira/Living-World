package dev.signalshards.livingworld.features.climate.domain;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

/**
 * Conjunto imutável de peças térmicas equipadas, no máximo uma por slot.
 */
public record ArmorThermalLoadout(List<EquippedArmorPiece> pieces) {
    public ArmorThermalLoadout {
        Objects.requireNonNull(pieces, "peças equipadas");
        pieces = List.copyOf(pieces);
        EnumSet<ArmorSlot> occupied = EnumSet.noneOf(ArmorSlot.class);
        for (EquippedArmorPiece piece : pieces) {
            Objects.requireNonNull(piece, "peça equipada");
            if (!occupied.add(piece.slot())) {
                throw new IllegalArgumentException(
                        "Apenas uma peça pode ocupar cada slot de armadura"
                );
            }
        }
    }

    public static ArmorThermalLoadout empty() {
        return new ArmorThermalLoadout(List.of());
    }
}
