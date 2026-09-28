package dev.signalshards.livingworld.features.ecology.paper;

import dev.signalshards.livingworld.features.ecology.domain.PhysicalWinterSettings;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfaceKind;
import dev.signalshards.livingworld.features.ecology.domain.WinterSurfacePosition;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaperWinterSurfaceOwnershipStoreTest {
    private final NamespacedKey key = new NamespacedKey(
            "livingworld",
            "physical_winter_ownership"
    );
    private final PhysicalWinterSettings settings =
            PhysicalWinterSettings.defaults();

    @Test
    void persisteOwnershipCompactoNoPdcDoChunk() {
        Map<NamespacedKey, Object> data = new HashMap<>();
        Chunk chunk = fakeChunk(fakePdc(data));
        PaperWinterSurfaceOwnershipStore store =
                new PaperWinterSurfaceOwnershipStore(key, settings);
        var ledger = store.load(chunk);
        WinterSurfacePosition ice =
                WinterSurfacePosition.fromWorld(-1, -20, 16);
        WinterSurfacePosition snow =
                WinterSurfacePosition.fromWorld(4, 95, 7);

        ledger.claim(ice, WinterSurfaceKind.ICE);
        ledger.claim(snow, WinterSurfaceKind.SNOW);
        store.save(chunk, ledger);

        var restored = store.load(chunk);
        assertEquals(
                WinterSurfaceKind.ICE,
                restored.kindAt(ice).orElseThrow()
        );
        assertEquals(
                WinterSurfaceKind.SNOW,
                restored.kindAt(snow).orElseThrow()
        );
        assertEquals(2, restored.size());
        assertFalse(restored.isDirty());
    }

    @Test
    void rejeitaArrayImparETipoDesconhecido() {
        Map<NamespacedKey, Object> malformed = new HashMap<>();
        malformed.put(key, new int[]{10});
        PaperWinterSurfaceOwnershipStore store =
                new PaperWinterSurfaceOwnershipStore(key, settings);

        assertThrows(
                IllegalStateException.class,
                () -> store.load(fakeChunk(fakePdc(malformed)))
        );

        Map<NamespacedKey, Object> unknownKind = new HashMap<>();
        unknownKind.put(
                key,
                new int[]{
                        new WinterSurfacePosition(1, 64, 2).packed(),
                        999
                }
        );
        assertThrows(
                IllegalStateException.class,
                () -> store.load(fakeChunk(fakePdc(unknownKind)))
        );
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

    private PersistentDataContainer fakePdc(
            Map<NamespacedKey, Object> data
    ) {
        return (PersistentDataContainer) Proxy.newProxyInstance(
                PersistentDataContainer.class.getClassLoader(),
                new Class<?>[]{PersistentDataContainer.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "set" -> {
                        data.put((NamespacedKey) args[0], args[2]);
                        yield null;
                    }
                    case "getOrDefault" -> data.getOrDefault(
                            (NamespacedKey) args[0],
                            args[2]
                    );
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
