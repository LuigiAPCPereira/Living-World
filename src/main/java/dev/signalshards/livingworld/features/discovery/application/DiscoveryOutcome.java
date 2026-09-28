package dev.signalshards.livingworld.features.discovery.application;

import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;

import java.util.Objects;

/**
 * Resultado de uma tentativa de descoberta.
 *
 * @param record        fato durável resultante
 * @param firstDiscovery {@code true} apenas quando o fato acabou de ser registrado
 */
public record DiscoveryOutcome(
        DiscoveryRecord record,
        boolean firstDiscovery
) {
    public DiscoveryOutcome {
        Objects.requireNonNull(record, "registro de descoberta");
    }
}
