package dev.signalshards.livingworld.features.discovery.application;

/**
 * Único ponto de saída do Discovery para fora.
 *
 * <p>Um listener reage a uma descoberta; ele não muda o fato, não escreve estado e não
 * pode impedir os demais listeners de rodar.
 */
@FunctionalInterface
public interface DiscoveryListener {
    void onDiscovery(DiscoveryUnlocked unlocked);
}
