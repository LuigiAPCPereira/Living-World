package dev.signalshards.livingworld.features.climate.domain;

/**
 * Taxa líquida de molhamento/secagem por segundo.
 *
 * <p>Valor positivo molha, negativo seca e zero mantém o estado. Chuva,
 * contato com água, abrigo, calor e equipamento irão compor essa taxa
 * posteriormente.</p>
 */
public record WetnessRate(double levelPerSecond) {
    public WetnessRate {
        if (!Double.isFinite(levelPerSecond)) {
            throw new IllegalArgumentException("A taxa de umidade deve ser finita");
        }
    }

    public static WetnessRate neutral() {
        return new WetnessRate(0.0D);
    }
}
