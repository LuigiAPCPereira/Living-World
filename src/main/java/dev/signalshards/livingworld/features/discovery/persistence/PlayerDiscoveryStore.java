package dev.signalshards.livingworld.features.discovery.persistence;

import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;

import java.util.Set;
import java.util.UUID;

/**
 * Descobertas pessoais de um jogador.
 *
 * <p>As implementações só podem ser usadas com jogador online: o PDC de um jogador
 * offline não é seguro de alterar e a leitura pode envolver I/O bloqueante.
 */
public interface PlayerDiscoveryStore {
    Set<DiscoveryRecord> load(UUID playerId);

    /**
     * @return {@code true} apenas quando o fato ainda não existia para o jogador
     */
    boolean add(UUID playerId, DiscoveryRecord record);
}
