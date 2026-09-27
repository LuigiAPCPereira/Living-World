package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.features.waystones.application.WaystoneRegistry;
import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PaperWaystoneRegistry implements WaystoneRegistry {
    private static final String KEY_PREFIX = "waystone_";

    private final Server server;
    private final String namespace;
    private final WaystoneBinaryCodec codec = new WaystoneBinaryCodec();

    public PaperWaystoneRegistry(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        this.server = plugin.getServer();
        this.namespace = new NamespacedKey(plugin, "namespace_probe").getNamespace();
    }

    @Override
    public void register(Waystone waystone) {
        Objects.requireNonNull(waystone, "waystone");
        World world = server.getWorld(waystone.worldId());
        if (world == null) {
            throw new IllegalStateException(
                    "Não é possível registrar uma waystone em um mundo não carregado"
            );
        }

        world.getPersistentDataContainer().set(
                keyFor(waystone.id()),
                PersistentDataType.BYTE_ARRAY,
                codec.encode(waystone)
        );
    }

    @Override
    public Optional<Waystone> find(WaystoneId id) {
        Objects.requireNonNull(id, "waystone");
        NamespacedKey key = keyFor(id);

        for (World world : server.getWorlds()) {
            byte[] data = world.getPersistentDataContainer().get(
                    key,
                    PersistentDataType.BYTE_ARRAY
            );
            if (data != null) {
                return Optional.of(codec.decode(id, world.getUID(), data));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Waystone> findAt(UUID worldId, int x, int y, int z) {
        Objects.requireNonNull(worldId, "mundo");

        return all().stream()
                .filter(waystone -> waystone.worldId().equals(worldId))
                .filter(waystone -> waystone.x() == x)
                .filter(waystone -> waystone.y() == y)
                .filter(waystone -> waystone.z() == z)
                .findFirst();
    }

    @Override
    public List<Waystone> all() {
        List<Waystone> result = new ArrayList<>();
        for (World world : server.getWorlds()) {
            PersistentDataContainer pdc = world.getPersistentDataContainer();
            for (NamespacedKey key : pdc.getKeys()) {
                WaystoneId id = idFromKey(key);
                if (id == null) {
                    continue;
                }

                byte[] data = pdc.get(key, PersistentDataType.BYTE_ARRAY);
                if (data != null) {
                    result.add(codec.decode(id, world.getUID(), data));
                }
            }
        }
        return List.copyOf(result);
    }

    @Override
    public boolean remove(WaystoneId id) {
        Objects.requireNonNull(id, "waystone");
        NamespacedKey key = keyFor(id);
        boolean removed = false;

        for (World world : server.getWorlds()) {
            PersistentDataContainer pdc = world.getPersistentDataContainer();
            if (pdc.has(key, PersistentDataType.BYTE_ARRAY)) {
                pdc.remove(key);
                removed = true;
            }
        }
        return removed;
    }

    private NamespacedKey keyFor(WaystoneId id) {
        return new NamespacedKey(
                namespace,
                KEY_PREFIX + id.value().toString().toLowerCase(Locale.ROOT)
        );
    }

    private WaystoneId idFromKey(NamespacedKey key) {
        if (!key.getNamespace().equals(namespace) || !key.getKey().startsWith(KEY_PREFIX)) {
            return null;
        }

        try {
            return new WaystoneId(UUID.fromString(key.getKey().substring(KEY_PREFIX.length())));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
