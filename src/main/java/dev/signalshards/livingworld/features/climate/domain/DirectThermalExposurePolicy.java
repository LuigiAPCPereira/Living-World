package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

/**
 * Converte contato físico direto em contribuição térmica.
 *
 * <p>Não aplica dano, efeitos ou freeze ticks; esses continuam em owners próprios.</p>
 */
public final class DirectThermalExposurePolicy {
    private final DirectThermalExposureSettings settings;

    public DirectThermalExposurePolicy() {
        this(DirectThermalExposureSettings.livingWorldDefaults());
    }

    public DirectThermalExposurePolicy(DirectThermalExposureSettings settings) {
        this.settings = Objects.requireNonNull(
                settings,
                "configuração de exposição térmica direta"
        );
    }

    public ThermalExchangeRate exchangeRate(DirectThermalExposure exposure) {
        Objects.requireNonNull(exposure, "exposição térmica direta");
        return new ThermalExchangeRate(switch (exposure) {
            case NONE -> 0.0D;
            case FIRE -> settings.fireLoadPerSecond();
            case LAVA -> settings.lavaLoadPerSecond();
            case POWDER_SNOW -> settings.powderSnowLoadPerSecond();
        });
    }
}
