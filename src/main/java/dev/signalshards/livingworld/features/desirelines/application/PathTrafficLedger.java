package dev.signalshards.livingworld.features.desirelines.application;

import java.util.HashMap;
import java.util.Map;

/**
 * Contador esparso e limitado de tráfego dentro de um chunk.
 */
public final class PathTrafficLedger {
    private final Map<Integer, Integer> visits;
    private final int maxEntries;
    private final int maxScore;
    private boolean dirty;

    public PathTrafficLedger(Map<Integer, Integer> initialVisits, int maxEntries, int maxScore) {
        if (maxEntries <= 0 || maxScore <= 0) {
            throw new IllegalArgumentException("Os limites do ledger devem ser positivos");
        }
        if (initialVisits.size() > maxEntries) {
            throw new IllegalArgumentException("O estado inicial excede o limite de posições do chunk");
        }

        this.visits = new HashMap<>(initialVisits);
        this.maxEntries = maxEntries;
        this.maxScore = maxScore;
    }

    public static PathTrafficLedger empty(int maxEntries, int maxScore) {
        return new PathTrafficLedger(Map.of(), maxEntries, maxScore);
    }

    /**
     * @return novo score, ou 0 quando uma nova posição não pode ser rastreada
     * porque o ledger atingiu seu limite.
     */
    public int record(int positionKey) {
        Integer current = visits.get(positionKey);
        if (current == null && visits.size() >= maxEntries) {
            return 0;
        }

        int next = Math.min(maxScore, (current == null ? 0 : current) + 1);
        if (!Integer.valueOf(next).equals(current)) {
            visits.put(positionKey, next);
            dirty = true;
        }
        return next;
    }

    public Map<Integer, Integer> snapshot() {
        return Map.copyOf(visits);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markClean() {
        dirty = false;
    }
}
