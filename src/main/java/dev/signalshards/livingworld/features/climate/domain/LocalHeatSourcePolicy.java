package dev.signalshards.livingworld.features.climate.domain;

import java.util.Collection;
import java.util.Objects;

/**
 * Converte fontes térmicas já observadas em uma contribuição positiva bounded.
 *
 * <p>O falloff é quadrático até o alcance efetivo. Múltiplas fontes somam,
 * mas o resultado final possui cap. Localizar as fontes é responsabilidade
 * de adapters/cache Paper futuros, nunca desta policy.</p>
 */
public final class LocalHeatSourcePolicy {
    private final LocalHeatSourceCatalog catalog;
    private final LocalHeatSettings settings;

    public LocalHeatSourcePolicy() {
        this(LocalHeatSourceCatalog.livingWorldDefaults(), LocalHeatSettings.livingWorldDefaults());
    }

    public LocalHeatSourcePolicy(
            LocalHeatSourceCatalog catalog,
            LocalHeatSettings settings
    ) {
        this.catalog = Objects.requireNonNull(catalog, "catálogo de fontes");
        this.settings = Objects.requireNonNull(settings, "configuração de calor local");
    }

    public ThermalExchangeRate exchangeRate(Collection<LocalHeatExposure> exposures) {
        Objects.requireNonNull(exposures, "fontes térmicas observadas");
        double total = 0.0D;
        for (LocalHeatExposure exposure : exposures) {
            Objects.requireNonNull(exposure, "fonte térmica observada");
            total += contribution(exposure);
        }
        return new ThermalExchangeRate(Math.min(total, settings.maximumCombinedLoadPerSecond()));
    }

    private double contribution(LocalHeatExposure exposure) {
        LocalHeatSourceProfile profile = catalog.profileFor(exposure.sourceType());
        if (exposure.distanceBlocks() >= profile.effectiveRangeBlocks()) {
            return 0.0D;
        }
        double normalizedDistance = exposure.distanceBlocks() / profile.effectiveRangeBlocks();
        double falloff = 1.0D - normalizedDistance;
        falloff *= falloff;
        return profile.peakLoadPerSecond()
                * falloff
                * exposure.exposureFactor()
                * exposure.lineOfSightFactor();
    }
}
