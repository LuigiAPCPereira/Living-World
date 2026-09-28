package dev.signalshards.livingworld.features.discovery.persistence;

import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;

import java.util.Set;
import java.util.UUID;

/**
 * Descobertas coletivas de um mundo.
 *
 * <p>As implementações só podem gravar em mundo carregado, porque o PDC vive no objeto
 * do mundo. Leitura de mundo não carregado devolve vazio: esse dado só existe enquanto
 * o mundo está disponível.
 */
public interface WorldDiscoveryStore {
    Set<DiscoveryRecord> load(UUID worldId);

    /**
     * @return {@code true} apenas quando o fato ainda não existia para o mundo
     */
    boolean add(UUID worldId, DiscoveryRecord record);
}
