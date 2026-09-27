package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.features.waystones.application.WaystoneAccessStore;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class PaperPlayerWaystoneAccessStore implements WaystoneAccessStore {
    private static final String KEY_NAME = "waystone_activations";

    private final Server server;
    private final NamespacedKey key;

    public PaperPlayerWaystoneAccessStore(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        this.server = plugin.getServer();
        this.key = new NamespacedKey(plugin, KEY_NAME);
    }

    PaperPlayerWaystoneAccessStore(Server server, NamespacedKey key) {
        this.server = Objects.requireNonNull(server, "servidor");
        this.key = Objects.requireNonNull(key, "chave");
    }

    @Override
    public Set<WaystoneId> load(UUID playerId) {
        return decode(containerFor(playerId).getOrDefault(
                key,
                PersistentDataType.LONG_ARRAY,
                new long[0]
        ));
    }

    @Override
    public boolean add(UUID playerId, WaystoneId waystoneId) {
        PersistentDataContainer container = containerFor(playerId);
        Set<WaystoneId> ids = new LinkedHashSet<>(decode(container.getOrDefault(
                key,
                PersistentDataType.LONG_ARRAY,
                new long[0]
        )));

        boolean added = ids.add(waystoneId);
        if (added) {
            container.set(key, PersistentDataType.LONG_ARRAY, encode(ids));
        }
        return added;
    }

    private PersistentDataContainer containerFor(UUID playerId) {
        Player player = server.getPlayer(Objects.requireNonNull(playerId, "jogador"));
        if (player == null || !player.isOnline()) {
            throw new IllegalStateException(
                    "O acesso de waystones só pode ser alterado para jogador online"
            );
        }
        return player.getPersistentDataContainer();
    }

    private Set<WaystoneId> decode(long[] raw) {
        if ((raw.length & 1) != 0) {
            throw new IllegalStateException("Os acessos persistidos de waystones estão malformados");
        }

        Set<WaystoneId> result = new LinkedHashSet<>();
        for (int index = 0; index < raw.length; index += 2) {
            result.add(new WaystoneId(new UUID(raw[index], raw[index + 1])));
        }
        return Set.copyOf(result);
    }

    private long[] encode(Set<WaystoneId> ids) {
        long[] raw = new long[ids.size() * 2];
        int index = 0;
        for (WaystoneId id : ids) {
            raw[index++] = id.value().getMostSignificantBits();
            raw[index++] = id.value().getLeastSignificantBits();
        }
        return raw;
    }
}
