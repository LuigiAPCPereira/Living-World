package dev.signalshards.livingworld.features.desirelines.paper;

import dev.signalshards.livingworld.features.desirelines.application.PathTrafficLedger;
import dev.signalshards.livingworld.features.desirelines.domain.PathWearSettings;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PaperChunkTrafficStoreTest {
    @Test
    void persisteLedgerComoArrayCompactoNoPdcDoChunk() {
        Map<NamespacedKey, Object> data = new HashMap<>();
        PersistentDataContainer pdc = fakePdc(data);
        Chunk chunk = fakeChunk(pdc);
        PathWearSettings settings = PathWearSettings.defaults();
        PaperChunkTrafficStore store = new PaperChunkTrafficStore(
                new NamespacedKey("livingworld", "desire_lines_traffic"),
                settings
        );

        PathTrafficLedger ledger = store.load(chunk);
        ledger.record(10);
        ledger.record(10);
        ledger.record(20);
        store.save(chunk, ledger);

        PathTrafficLedger restored = store.load(chunk);
        assertEquals(2, restored.snapshot().get(10));
        assertEquals(1, restored.snapshot().get(20));
        assertFalse(restored.isDirty());
    }

    private Chunk fakeChunk(PersistentDataContainer pdc) {
        return (Chunk) Proxy.newProxyInstance(
                Chunk.class.getClassLoader(),
                new Class<?>[]{Chunk.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPersistentDataContainer" -> pdc;
                    case "toString" -> "ChunkFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }

    private PersistentDataContainer fakePdc(Map<NamespacedKey, Object> data) {
        return (PersistentDataContainer) Proxy.newProxyInstance(
                PersistentDataContainer.class.getClassLoader(),
                new Class<?>[]{PersistentDataContainer.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "set" -> {
                        data.put((NamespacedKey) args[0], args[2]);
                        yield null;
                    }
                    case "getOrDefault" -> data.getOrDefault((NamespacedKey) args[0], args[2]);
                    case "toString" -> "PdcFake";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(
                            "Método não esperado no teste: " + method.getName()
                    );
                }
        );
    }
}
