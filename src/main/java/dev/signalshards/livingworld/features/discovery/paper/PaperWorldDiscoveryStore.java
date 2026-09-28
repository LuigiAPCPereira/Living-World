package dev.signalshards.livingworld.features.discovery.paper;

import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryScope;
import dev.signalshards.livingworld.features.discovery.persistence.WorldDiscoveryStore;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Descobertas coletivas no PDC do mundo carregado.
 *
 * <p>Gravar em mundo não carregado falha fechada: o PDC vive no objeto do mundo e
 * perder a gravação significaria uma memória do mundo que nunca existiu. Ler um mundo
 * não carregado devolve vazio, e é a única leitura tolerante do par de stores.
 */
public final class PaperWorldDiscoveryStore implements WorldDiscoveryStore {
    private static final String KEY_NAME = "discovery_learned";
    private static final DiscoveryScope SCOPE = DiscoveryScope.WORLD;

    private final Server server;
    private final NamespacedKey key;

    public PaperWorldDiscoveryStore(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        this.server = plugin.getServer();
        this.key = new NamespacedKey(plugin, KEY_NAME);
    }

    PaperWorldDiscoveryStore(Server server, NamespacedKey key) {
        this.server = Objects.requireNonNull(server, "servidor");
        this.key = Objects.requireNonNull(key, "chave");
    }

    @Override
    public Set<DiscoveryRecord> load(UUID worldId) {
        Objects.requireNonNull(worldId, "mundo");
        World world = server.getWorld(worldId);
        if (world == null) {
            return Set.of();
        }
        return read(world.getPersistentDataContainer(), worldId);
    }

    @Override
    public boolean add(UUID worldId, DiscoveryRecord record) {
        Objects.requireNonNull(record, "registro de descoberta");
        Objects.requireNonNull(worldId, "mundo");
        requireSameOwner(record, worldId);

        PersistentDataContainer container = containerFor(worldId);
        LinkedHashSet<DiscoveryRecord> records = new LinkedHashSet<>(read(container, worldId));
        boolean added = records.add(record);
        if (added) {
            container.set(key, DiscoveryEntryCodec.ENTRY_TYPE, DiscoveryEntryCodec.encode(records));
        }
        return added;
    }

    private Set<DiscoveryRecord> read(PersistentDataContainer container, UUID worldId) {
        return DiscoveryEntryCodec.decode(
                container.getOrDefault(key, DiscoveryEntryCodec.ENTRY_TYPE, List.of()),
                SCOPE,
                worldId
        );
    }

    private PersistentDataContainer containerFor(UUID worldId) {
        World world = server.getWorld(worldId);
        if (world == null) {
            throw new IllegalStateException(
                    "As descobertas do mundo só podem ser alteradas com o mundo carregado"
            );
        }
        return world.getPersistentDataContainer();
    }

    private void requireSameOwner(DiscoveryRecord record, UUID worldId) {
        if (record.scope() != SCOPE) {
            throw new IllegalArgumentException(
                    "Uma descoberta " + record.scope() + " não pertence ao armazenamento do mundo"
            );
        }
        if (!record.owner().equals(worldId)) {
            throw new IllegalArgumentException(
                    "A descoberta não pode ser gravada em um mundo diferente do dono"
            );
        }
    }
}
