package dev.signalshards.livingworld.features.desirelines.paper;

import dev.signalshards.livingworld.features.desirelines.application.PathTrafficLedger;
import dev.signalshards.livingworld.features.desirelines.domain.PathWearSettings;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class PaperChunkTrafficStore {
    private static final String KEY_NAME = "desire_lines_traffic";

    private final NamespacedKey key;
    private final PathWearSettings settings;

    public PaperChunkTrafficStore(Plugin plugin, PathWearSettings settings) {
        this.key = new NamespacedKey(Objects.requireNonNull(plugin, "plugin"), KEY_NAME);
        this.settings = Objects.requireNonNull(settings, "configuração de desgaste");
    }

    PaperChunkTrafficStore(NamespacedKey key, PathWearSettings settings) {
        this.key = Objects.requireNonNull(key, "chave");
        this.settings = Objects.requireNonNull(settings, "configuração de desgaste");
    }

    public PathTrafficLedger load(Chunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        int[] raw = pdc.getOrDefault(key, PersistentDataType.INTEGER_ARRAY, new int[0]);

        if ((raw.length & 1) != 0) {
            throw new IllegalStateException("Os dados persistidos de Desire Lines estão malformados");
        }

        Map<Integer, Integer> visits = new HashMap<>();
        for (int index = 0; index < raw.length; index += 2) {
            if (visits.size() >= settings.maxTrackedPositionsPerChunk()) {
                break;
            }

            int score = Math.max(0, Math.min(settings.dirtToPathVisits(), raw[index + 1]));
            if (score > 0) {
                visits.put(raw[index], score);
            }
        }

        return new PathTrafficLedger(
                visits,
                settings.maxTrackedPositionsPerChunk(),
                settings.dirtToPathVisits()
        );
    }

    public void save(Chunk chunk, PathTrafficLedger ledger) {
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(ledger, "ledger");

        Map<Integer, Integer> snapshot = ledger.snapshot();
        int[] raw = new int[snapshot.size() * 2];
        int index = 0;
        for (var entry : snapshot.entrySet()) {
            raw[index++] = entry.getKey();
            raw[index++] = entry.getValue();
        }

        chunk.getPersistentDataContainer().set(key, PersistentDataType.INTEGER_ARRAY, raw);
        ledger.markClean();
    }
}
