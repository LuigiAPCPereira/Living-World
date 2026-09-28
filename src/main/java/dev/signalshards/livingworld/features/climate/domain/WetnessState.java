package dev.signalshards.livingworld.features.climate.domain;

/**
 * Nível acumulado de umidade corporal/equipamento relevante ao conforto térmico.
 *
 * <p>Zero representa seco e um representa completamente molhado. O estado é
 * separado de temperatura: estar molhado altera troca térmica, não a
 * temperatura ambiental.</p>
 */
public record WetnessState(double level) {
    public static final double DRY = 0.0D;
    public static final double SATURATED = 1.0D;

    public WetnessState {
        if (!Double.isFinite(level)) {
            throw new IllegalArgumentException("O nível de umidade deve ser finito");
        }
        if (level < DRY || level > SATURATED) {
            throw new IllegalArgumentException("O nível de umidade deve ficar entre 0 e 1");
        }
    }

    public static WetnessState dry() {
        return new WetnessState(DRY);
    }

    public WetnessState adjustedBy(double delta) {
        if (!Double.isFinite(delta)) {
            throw new IllegalArgumentException("A variação de umidade deve ser finita");
        }
        return new WetnessState(Math.clamp(level + delta, DRY, SATURATED));
    }
}
