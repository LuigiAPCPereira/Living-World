package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

/**
 * Aplica resistência à água da armadura apenas a taxas positivas de wetness.
 *
 * <p>Nenhum material é impermeável. Secagem negativa não é acelerada nem
 * retardada neste slice; retenção de água por material pode ser adicionada
 * depois se playtest justificar.</p>
 */
public final class ArmorWetnessPolicy {
    private final ArmorThermalCatalog catalog;
    private final ArmorWetnessSettings settings;

    public ArmorWetnessPolicy() {
        this(
                ArmorThermalCatalog.livingWorldDefaults(),
                ArmorWetnessSettings.livingWorldDefaults()
        );
    }

    public ArmorWetnessPolicy(
            ArmorThermalCatalog catalog,
            ArmorWetnessSettings settings
    ) {
        this.catalog = Objects.requireNonNull(catalog, "catálogo térmico");
        this.settings = Objects.requireNonNull(settings, "configuração de wetness");
    }

    public double effectiveWaterResistance(ArmorThermalLoadout loadout) {
        Objects.requireNonNull(loadout, "armadura equipada");
        double total = 0.0D;
        for (EquippedArmorPiece piece : loadout.pieces()) {
            ArmorMaterialThermalProfile profile = catalog.profileFor(piece.material());
            total += piece.slot().coverageWeight() * profile.waterResistance();
        }
        return Math.clamp(total, 0.0D, 1.0D);
    }

    public WetnessRate apply(WetnessRate baseRate, ArmorThermalLoadout loadout) {
        Objects.requireNonNull(baseRate, "taxa base de wetness");
        if (baseRate.levelPerSecond() <= 0.0D) {
            return baseRate;
        }
        double resistance = effectiveWaterResistance(loadout);
        double multiplier = 1.0D - (
                resistance * settings.maximumWetnessIngressReduction()
        );
        return new WetnessRate(baseRate.levelPerSecond() * multiplier);
    }
}
