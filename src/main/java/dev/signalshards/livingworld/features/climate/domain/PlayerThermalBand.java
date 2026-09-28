package dev.signalshards.livingworld.features.climate.domain;

/**
 * Faixa qualitativa do estado térmico corporal do jogador.
 *
 * <p>As faixas são uma linguagem de gameplay. Elas não representam
 * diagnóstico médico nem temperatura corporal real.</p>
 */
public enum PlayerThermalBand {
    EXTREME_COLD,
    FREEZING,
    VERY_COLD,
    COLD,
    COOL,
    COMFORTABLE,
    WARM,
    HOT,
    OVERHEATING,
    EXTREME_HEAT
}
