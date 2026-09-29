package dev.signalshards.livingworld.features.biomes.application;

import dev.signalshards.livingworld.features.discovery.application.DiscoveryOutcome;
import dev.signalshards.livingworld.features.discovery.application.DiscoveryService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Suprime trabalho repetido enquanto um jogador permanece no mesmo biome.
 *
 * <p>Não decide se uma descoberta é inédita: essa responsabilidade continua em
 * {@link DiscoveryService}. O cache existe somente para evitar reenviar a mesma
 * observação a cada mudança de bloco dentro do mesmo biome.</p>
 */
public final class BiomeEntryTracker {
    private final DiscoveryService discovery;
    private final Map<UUID, String> lastBiomeByPlayer = new HashMap<>();

    public BiomeEntryTracker(DiscoveryService discovery) {
        this.discovery = Objects.requireNonNull(discovery, "serviço de descobertas");
    }

    public Optional<DiscoveryOutcome> observe(UUID playerId, String biomeKey) {
        Objects.requireNonNull(playerId, "jogador");
        String normalizedBiomeKey = BiomeDiscovery.idFor(biomeKey).value();

        if (normalizedBiomeKey.equals(lastBiomeByPlayer.get(playerId))) {
            return Optional.empty();
        }

        DiscoveryOutcome outcome = BiomeDiscovery.recordEncounter(
                discovery,
                playerId,
                normalizedBiomeKey
        );
        lastBiomeByPlayer.put(playerId, normalizedBiomeKey);
        return Optional.of(outcome);
    }

    public void forget(UUID playerId) {
        lastBiomeByPlayer.remove(Objects.requireNonNull(playerId, "jogador"));
    }

    public void clear() {
        lastBiomeByPlayer.clear();
    }

    int trackedPlayerCount() {
        return lastBiomeByPlayer.size();
    }
}
