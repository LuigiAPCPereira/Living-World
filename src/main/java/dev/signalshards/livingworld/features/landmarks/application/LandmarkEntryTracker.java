package dev.signalshards.livingworld.features.landmarks.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Mantém apenas quais landmarks contêm cada jogador neste momento.
 *
 * <p>O cache não decide se um landmark é uma descoberta inédita. Ele somente
 * evita repetir o caso de uso enquanto o jogador continua dentro da mesma
 * instância. Sair e reentrar volta a produzir uma entrada; DiscoveryService
 * continua sendo o dono da idempotência durável.</p>
 */
public final class LandmarkEntryTracker {
    private final Map<UUID, Set<UUID>> currentByPlayer = new HashMap<>();

    public List<ResolvedLandmark> observe(
            UUID playerId,
            Collection<ResolvedLandmark> currentLandmarks
    ) {
        Objects.requireNonNull(playerId, "jogador");
        Objects.requireNonNull(currentLandmarks, "landmarks atuais");

        Map<UUID, ResolvedLandmark> unique = new LinkedHashMap<>();
        for (ResolvedLandmark landmark : currentLandmarks) {
            ResolvedLandmark value = Objects.requireNonNull(
                    landmark,
                    "landmark atual"
            );
            unique.putIfAbsent(value.id(), value);
        }

        Set<UUID> previous = currentByPlayer.getOrDefault(
                playerId,
                Set.of()
        );
        List<ResolvedLandmark> entered = new ArrayList<>();
        for (ResolvedLandmark landmark : unique.values()) {
            if (!previous.contains(landmark.id())) {
                entered.add(landmark);
            }
        }

        if (unique.isEmpty()) {
            currentByPlayer.remove(playerId);
        } else {
            currentByPlayer.put(playerId, Set.copyOf(unique.keySet()));
        }
        return List.copyOf(entered);
    }

    public void forget(UUID playerId) {
        currentByPlayer.remove(Objects.requireNonNull(playerId, "jogador"));
    }

    public void clear() {
        currentByPlayer.clear();
    }

    int trackedPlayerCount() {
        return currentByPlayer.size();
    }
}
