package dev.signalshards.livingworld.features.waystones.paper;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;

import java.lang.reflect.Proxy;
import java.util.Map;

final class PersistentDataContainerTestDouble {
    private PersistentDataContainerTestDouble() {
    }

    static PersistentDataContainer create(Map<NamespacedKey, Object> data) {
        return (PersistentDataContainer) Proxy.newProxyInstance(
                PersistentDataContainer.class.getClassLoader(),
                new Class<?>[]{PersistentDataContainer.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "set" -> {
                        data.put((NamespacedKey) args[0], args[2]);
                        yield null;
                    }
                    case "get" -> data.get((NamespacedKey) args[0]);
                    case "getOrDefault" -> data.getOrDefault((NamespacedKey) args[0], args[2]);
                    case "has" -> data.containsKey((NamespacedKey) args[0]);
                    case "remove" -> {
                        data.remove((NamespacedKey) args[0]);
                        yield null;
                    }
                    case "getKeys" -> data.keySet();
                    case "isEmpty" -> data.isEmpty();
                    case "getSize" -> data.size();
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
