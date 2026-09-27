package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.features.waystones.domain.Waystone;
import dev.signalshards.livingworld.features.waystones.domain.WaystoneId;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

final class WaystoneBinaryCodec {
    private static final int VERSION = 1;
    private static final int MAX_NAME_BYTES = 192;

    byte[] encode(Waystone waystone) {
        byte[] nameBytes = waystone.name().getBytes(StandardCharsets.UTF_8);
        if (nameBytes.length > MAX_NAME_BYTES) {
            throw new IllegalArgumentException("O nome codificado da waystone é grande demais");
        }

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(buffer)) {
                output.writeByte(VERSION);
                output.writeInt(waystone.x());
                output.writeInt(waystone.y());
                output.writeInt(waystone.z());
                output.writeShort(nameBytes.length);
                output.write(nameBytes);
            }
            return buffer.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível codificar a waystone", exception);
        }
    }

    Waystone decode(WaystoneId id, UUID worldId, byte[] data) {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(data))) {
            int version = input.readUnsignedByte();
            if (version != VERSION) {
                throw new IllegalStateException(
                        "Versão de dados de waystone não suportada: " + version
                );
            }

            int x = input.readInt();
            int y = input.readInt();
            int z = input.readInt();
            int nameLength = input.readUnsignedShort();
            if (nameLength > MAX_NAME_BYTES || nameLength > input.available()) {
                throw new IllegalStateException("Nome persistido de waystone está malformado");
            }

            byte[] nameBytes = input.readNBytes(nameLength);
            if (input.available() != 0) {
                throw new IllegalStateException("Dados persistidos de waystone contêm bytes inesperados");
            }

            return new Waystone(
                    id,
                    new String(nameBytes, StandardCharsets.UTF_8),
                    worldId,
                    x,
                    y,
                    z
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível ler a waystone persistida", exception);
        }
    }
}
