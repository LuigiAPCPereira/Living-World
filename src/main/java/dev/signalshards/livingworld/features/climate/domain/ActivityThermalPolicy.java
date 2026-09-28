package dev.signalshards.livingworld.features.climate.domain;

import java.util.Objects;

/**
 * Produz a contribuição térmica positiva de atividade física.
 *
 * <p>RESTING representa o baseline corporal já embutido no modelo e, por
 * isso, não adiciona calor. As demais atividades geram pequenas taxas
 * positivas que podem reduzir resfriamento ou aumentar sobreaquecimento,
 * mas não substituem abrigo/calor em condições severas.</p>
 */
public final class ActivityThermalPolicy {
    private final ActivityThermalSettings settings;

    public ActivityThermalPolicy() {
        this(ActivityThermalSettings.livingWorldDefaults());
    }

    public ActivityThermalPolicy(ActivityThermalSettings settings) {
        this.settings = Objects.requireNonNull(settings, "configuração metabólica");
    }

    public ThermalExchangeRate exchangeRate(PlayerActivity activity) {
        Objects.requireNonNull(activity, "atividade");
        double rate = switch (activity) {
            case RESTING -> 0.0D;
            case WALKING -> settings.walkingLoadPerSecond();
            case SPRINTING -> settings.sprintingLoadPerSecond();
            case SWIMMING -> settings.swimmingLoadPerSecond();
            case CLIMBING -> settings.climbingLoadPerSecond();
            case GLIDING -> settings.glidingLoadPerSecond();
        };
        return new ThermalExchangeRate(Math.min(rate, settings.maximumLoadPerSecond()));
    }
}
