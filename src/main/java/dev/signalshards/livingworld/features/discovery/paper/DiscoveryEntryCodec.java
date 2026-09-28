package dev.signalshards.livingworld.features.discovery.paper;

import dev.signalshards.livingworld.features.discovery.domain.DiscoveryId;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryRecord;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryScope;
import dev.signalshards.livingworld.features.discovery.domain.DiscoveryType;
import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Codifica a identidade persistida de uma descoberta como pares
 * {@code (tipo, id)} em uma lista de textos.
 *
 * <p>Pares evitam qualquer ambiguidade de separador entre o tipo, que é um token
 * curto, e o id, que pode conter {@code ':'} como acontece com chaves de bioma. O
 * escopo e o dono não são gravados: eles vêm do contêiner que guarda o fato, e por
 * isso a mesma identidade nunca pode ser gravada em outro escopo.
 */
final class DiscoveryEntryCodec {
    static final ListPersistentDataType<String, String> ENTRY_TYPE =
            PersistentDataType.LIST.strings();

    private DiscoveryEntryCodec() {
    }

    static List<String> encode(Collection<DiscoveryRecord> records) {
        List<String> entries = new ArrayList<>(records.size() * 2);
        for (DiscoveryRecord record : records) {
            entries.add(record.type().value());
            entries.add(record.id().value());
        }
        return entries;
    }

    static Set<DiscoveryRecord> decode(List<String> entries, DiscoveryScope scope, UUID owner) {
        if ((entries.size() & 1) != 0) {
            throw new IllegalStateException("As descobertas persistidas estão malformadas");
        }

        Set<DiscoveryRecord> records = new LinkedHashSet<>();
        for (int index = 0; index < entries.size(); index += 2) {
            records.add(new DiscoveryRecord(
                    typeOf(entries.get(index)),
                    idOf(entries.get(index + 1)),
                    scope,
                    owner
            ));
        }
        return records;
    }

    private static DiscoveryType typeOf(String raw) {
        try {
            return new DiscoveryType(raw);
        } catch (IllegalArgumentException malformed) {
            throw new IllegalStateException(
                    "As descobertas persistidas contêm um tipo inválido: " + raw,
                    malformed
            );
        }
    }

    private static DiscoveryId idOf(String raw) {
        try {
            return new DiscoveryId(raw);
        } catch (IllegalArgumentException malformed) {
            throw new IllegalStateException(
                    "As descobertas persistidas contêm um identificador inválido: " + raw,
                    malformed
            );
        }
    }
}
