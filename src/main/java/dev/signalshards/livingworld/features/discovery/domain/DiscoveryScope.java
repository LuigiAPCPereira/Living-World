package dev.signalshards.livingworld.features.discovery.domain;

/**
 * Dono do fato de uma descoberta.
 *
 * <p>O escopo é declarado pelo tipo da descoberta, nunca escolhido pelo chamador, e
 * decide onde o fato é persistido: {@code PERSONAL} no PDC do jogador e {@code WORLD}
 * no PDC do mundo.
 */
public enum DiscoveryScope {
    PERSONAL,
    WORLD
}
