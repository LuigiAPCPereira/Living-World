package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.ecology.application.WinterSurfaceOwnershipLedger;
import dev.signalshards.livingworld.features.ecology.domain.PhysicalWinterSettings;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfaceKind;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfacePosition;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Ownership persistente de gelo/neve física criada pelo Living World.
 *
 * <p>Formato: pares {@code packedPosition, kindId} em INTEGER_ARRAY.</p>
 */
public final class PaperWinterSurfaceOwnershipStore {
    private static final String KEY_NAME = "physical_winter_ownership";

    private final NamespacedKey key;
    private final PhysicalWinterSettings settings;

    public PaperWinterSurfaceOwnershipStore(
            Plugin plugin,
            PhysicalWinterSettings settings
    ) {
        this(
                new NamespacedKey(
                        Objects.requireNonNull(plugin, "plugin"),
                        KEY_NAME
                ),
                settings
        );
    }

    PaperWinterSurfaceOwnershipStore(
            NamespacedKey key,
            PhysicalWinterSettings settings
    ) {
        this.key = Objects.requireNonNull(key, "chave");
        this.settings = Objects.requireNonNull(
                settings,
                "configuração de inverno físico"
        );
    }

    public WinterSurfaceOwnershipLedger load(Chunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        int[] raw = pdc.getOrDefault(
                key,
                PersistentDataType.INTEGER_ARRAY,
                new int[0]
        );
        if ((raw.length & 1) != 0) {
            throw new IllegalStateException(
                    "Ownership de inverno persistido está malformado"
            );
        }

        Map<WinterSurfacePosition, WinterSurfaceKind> owned =
                new LinkedHashMap<>();
        for (int index = 0;
             index < raw.length
                     && owned.size() < settings.maxOwnedPositionsPerChunk();
             index += 2) {
            WinterSurfacePosition position =
                    WinterSurfacePosition.unpack(raw[index]);
            final WinterSurfaceKind kind;
            try {
                kind = WinterSurfaceKind.fromPersistentId(raw[index + 1]);
            } catch (IllegalArgumentException exception) {
                throw new IllegalStateException(
                        "Ownership de inverno contém tipo desconhecido",
                        exception
                );
            }
            owned.put(position, kind);
        }

        return new WinterSurfaceOwnershipLedger(
                owned,
                settings.maxOwnedPositionsPerChunk()
        );
    }

    public void save(
            Chunk chunk,
            WinterSurfaceOwnershipLedger ledger
    ) {
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(ledger, "ledger");

        var entries = ledger.snapshot().entrySet().stream()
                .sorted(Comparator.comparingInt(
                        entry -> entry.getKey().packed()
                ))
                .toList();
        int[] raw = new int[entries.size() * 2];
        int index = 0;
        for (var entry : entries) {
            raw[index++] = entry.getKey().packed();
            raw[index++] = entry.getValue().persistentId();
        }

        chunk.getPersistentDataContainer().set(
                key,
                PersistentDataType.INTEGER_ARRAY,
                raw
        );
        ledger.markClean();
    }
}
