package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

/**
 * Resolve isolamento efetivo e aplica-o à transferência térmica com o ar.
 *
 * <p>Armadura não cria temperatura. Ela reduz a magnitude da troca com o ar
 * em ambas as direções: ajuda a reter calor no frio, mas também retém calor
 * corporal quando o jogador precisa dissipá-lo.</p>
 */
public final class ArmorInsulationPolicy {
    private final ArmorThermalCatalog catalog;
    private final ArmorInsulationSettings settings;

    public ArmorInsulationPolicy() {
        this(
                ArmorThermalCatalog.livingWorldDefaults(),
                ArmorInsulationSettings.livingWorldDefaults()
        );
    }

    public ArmorInsulationPolicy(
            ArmorThermalCatalog catalog,
            ArmorInsulationSettings settings
    ) {
        this.catalog = Objects.requireNonNull(catalog, "catálogo térmico");
        this.settings = Objects.requireNonNull(settings, "configuração de isolamento");
    }

    public double effectiveInsulation(ArmorThermalLoadout loadout) {
        Objects.requireNonNull(loadout, "armadura equipada");
        double total = 0.0D;
        for (EquippedArmorPiece piece : loadout.pieces()) {
            ArmorMaterialThermalProfile profile = catalog.profileFor(piece.material());
            total += piece.slot().coverageWeight() * profile.insulation();
        }
        return Math.clamp(total, 0.0D, 1.0D);
    }

    public AirThermalTransferFactor apply(
            AirThermalTransferFactor baseFactor,
            ArmorThermalLoadout loadout
    ) {
        return apply(baseFactor, loadout, WetnessState.dry());
    }

    public AirThermalTransferFactor apply(
            AirThermalTransferFactor baseFactor,
            ArmorThermalLoadout loadout,
            WetnessState wetness
    ) {
        Objects.requireNonNull(baseFactor, "fator base do ar");
        Objects.requireNonNull(wetness, "wetness");
        double dryInsulation = effectiveInsulation(loadout);
        double wetLoss = wetness.level() * settings.maximumWetInsulationLoss();
        double insulation = dryInsulation * (1.0D - wetLoss);
        double multiplier = 1.0D - (
                insulation * settings.maximumAirTransferReduction()
        );
        return new AirThermalTransferFactor(baseFactor.value() * multiplier);
    }
}
