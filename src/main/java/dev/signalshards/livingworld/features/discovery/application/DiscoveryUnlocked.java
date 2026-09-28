package dev.signalshards.livingworld.features.discovery.application;

import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;

import java.util.Objects;
import java.util.UUID;

/**
 * Descoberta registrada pela primeira vez.
 *
 * <p>Publicada apenas em registros novos: reencontrar um lugar conhecido é um retorno
 * agradável, não uma conquista.
 *
 * @param record       fato durável
 * @param discoveredBy jogador que descobriu agora
 * @param label        nome visível da coisa descoberta no momento da descoberta
 */
public record DiscoveryUnlocked(
        DiscoveryRecord record,
        UUID discoveredBy,
        String label
) {
    public DiscoveryUnlocked {
        Objects.requireNonNull(record, "registro de descoberta");
        Objects.requireNonNull(discoveredBy, "jogador que descobriu");
        Objects.requireNonNull(label, "rótulo da descoberta");
    }
}
