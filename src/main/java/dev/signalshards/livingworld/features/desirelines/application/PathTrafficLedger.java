package dev.signalshards.livingworld.features.desirelines.application;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contador esparso e limitado de tráfego dentro de um chunk.
 */
public final class PathTrafficLedger {
    private final Map<Integer, Integer> visits;
    private final Set<Integer> touchedSinceDecay = new HashSet<>();
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

        touchedSinceDecay.add(positionKey);
        int next = Math.min(maxScore, (current == null ? 0 : current) + 1);
        if (!Integer.valueOf(next).equals(current)) {
            visits.put(positionKey, next);
            dirty = true;
        }
        return next;
    }

    public boolean contains(int positionKey) {
        return visits.containsKey(positionKey);
    }

    public List<PathTrafficDecay> decayUntouched(long days) {
        if (days < 0) {
            throw new IllegalArgumentException("A quantidade de dias para recuperação não pode ser negativa");
        }
        if (days == 0 || visits.isEmpty()) {
            touchedSinceDecay.clear();
            return List.of();
        }

        List<PathTrafficDecay> changes = new ArrayList<>();
        for (var entry : Map.copyOf(visits).entrySet()) {
            int key = entry.getKey();
            int previous = entry.getValue();
            long protectedDays = touchedSinceDecay.contains(key) ? 1L : 0L;
            long decayDays = Math.max(0L, days - protectedDays);
            int current = (int) Math.max(0L, previous - Math.min(previous, decayDays));

            if (current == previous) {
                continue;
            }

            if (current == 0) {
                visits.remove(key);
            } else {
                visits.put(key, current);
            }
            dirty = true;
            changes.add(new PathTrafficDecay(key, previous, current));
        }

        touchedSinceDecay.clear();
        return List.copyOf(changes);
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
