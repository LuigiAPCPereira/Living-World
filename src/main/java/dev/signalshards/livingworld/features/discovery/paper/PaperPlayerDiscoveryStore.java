package dev.signalshards.livingworld.features.discovery.paper;

import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryScope;
import dev.signalshards.livingworld.features.discovery.persistence.PlayerDiscoveryStore;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Descobertas pessoais no PDC do jogador online.
 *
 * <p>Segue a política já usada pelas ativações de waystone: nada de PDC de jogador
 * offline, porque a leitura pode envolver I/O bloqueante e o offline é somente leitura.
 */
public final class PaperPlayerDiscoveryStore implements PlayerDiscoveryStore {
    private static final String KEY_NAME = "discovery_learned";
    private static final DiscoveryScope SCOPE = DiscoveryScope.PERSONAL;

    private final Server server;
    private final NamespacedKey key;

    public PaperPlayerDiscoveryStore(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        this.server = plugin.getServer();
        this.key = new NamespacedKey(plugin, KEY_NAME);
    }

    PaperPlayerDiscoveryStore(Server server, NamespacedKey key) {
        this.server = Objects.requireNonNull(server, "servidor");
        this.key = Objects.requireNonNull(key, "chave");
    }

    @Override
    public Set<DiscoveryRecord> load(UUID playerId) {
        return read(containerFor(playerId), playerId);
    }

    @Override
    public boolean add(UUID playerId, DiscoveryRecord record) {
        Objects.requireNonNull(record, "registro de descoberta");
        requireSameOwner(record, playerId, "jogador");

        PersistentDataContainer container = containerFor(playerId);
        LinkedHashSet<DiscoveryRecord> records = new LinkedHashSet<>(read(container, playerId));
        boolean added = records.add(record);
        if (added) {
            container.set(key, DiscoveryEntryCodec.ENTRY_TYPE, DiscoveryEntryCodec.encode(records));
        }
        return added;
    }

    private Set<DiscoveryRecord> read(PersistentDataContainer container, UUID playerId) {
        return DiscoveryEntryCodec.decode(
                container.getOrDefault(key, DiscoveryEntryCodec.ENTRY_TYPE, List.of()),
                SCOPE,
                playerId
        );
    }

    private PersistentDataContainer containerFor(UUID playerId) {
        Objects.requireNonNull(playerId, "jogador");
        Player player = server.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            throw new IllegalStateException(
                    "As descobertas só podem ser alteradas para jogador online"
            );
        }
        return player.getPersistentDataContainer();
    }

    private void requireSameOwner(DiscoveryRecord record, UUID owner, String ownerKind) {
        if (record.scope() != SCOPE) {
            throw new IllegalArgumentException(
                    "Uma descoberta " + record.scope() + " não pertence ao armazenamento do "
                            + ownerKind
            );
        }
        if (!record.owner().equals(owner)) {
            throw new IllegalArgumentException(
                    "A descoberta não pode ser gravada em um " + ownerKind + " diferente do dono"
            );
        }
    }
}
