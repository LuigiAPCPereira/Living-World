package dev.signalshards.livingworld.features.calendar.domain;

/**
 * Representa um instante absoluto do relógio do mundo em ticks do Minecraft.
 *
 * <p>Este tipo é propositalmente independente de Paper, scheduler e estado mutável
 * do servidor. A integração com o mundo real do servidor deve acontecer em uma
 * camada externa.</p>
 *
 * @param totalTicks quantidade absoluta de ticks observada no mundo
 */
public record WorldTime(long totalTicks) {
    public static final long TICKS_PER_DAY = 24_000L;

    /**
     * Índice de dia baseado em zero.
     */
    public long dayIndex() {
        return Math.floorDiv(totalTicks, TICKS_PER_DAY);
    }

    /**
     * Tick dentro do dia, sempre entre 0 e 23.999.
     */
    public long tickOfDay() {
        return Math.floorMod(totalTicks, TICKS_PER_DAY);
    }
}
