package dev.signalshards.livingworld.features.calendar.paper;

import dev.signalshards.livingworld.features.calendar.domain.CalendarState;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperCalendarStateStoreTest {
    @Test
    void iniciaEmZeroEPersisteNoPdc() {
        Map<NamespacedKey, Object> data = new HashMap<>();
        PersistentDataContainer container = fakeContainer(data);
        NamespacedKey key = new NamespacedKey("livingworld", "calendar_elapsed_days");
        PaperCalendarStateStore store = new PaperCalendarStateStore(container, key);

        assertEquals(CalendarState.initial(), store.load());

        store.save(new CalendarState(42));

        assertEquals(new CalendarState(42), store.load());
    }

    private PersistentDataContainer fakeContainer(Map<NamespacedKey, Object> data) {
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
