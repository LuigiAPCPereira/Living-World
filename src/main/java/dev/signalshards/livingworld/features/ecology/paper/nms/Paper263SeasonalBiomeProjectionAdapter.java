package dev.signalshards.livingworld.features.ecology.paper.nms;

import io.netty.buffer.Unpooled;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

/**
 * Adapter Paper 26.3 para provar projeção visual de biome sem alterar a verdade
 * server-side do chunk.
 *
 * <p>O caller continua responsável por escolher somente chunks que façam sentido
 * para o jogador. Este adapter nunca carrega chunk, nunca chama setBiome e nunca
 * modifica os containers reais.</p>
 */
public final class Paper263SeasonalBiomeProjectionAdapter {
    private static final int BIOME_CELLS_PER_AXIS = 4;

    public ProjectionResult projectWholeChunk(
            Player player,
            Chunk chunk,
            NamespacedKey targetBiomeKey
    ) {
        Objects.requireNonNull(player, "jogador");
        Objects.requireNonNull(chunk, "chunk");
        Objects.requireNonNull(targetBiomeKey, "bioma visual");

        if (!Bukkit.isPrimaryThread()) {
            return ProjectionResult.WRONG_THREAD;
        }
        if (!player.isOnline()) {
            return ProjectionResult.PLAYER_OFFLINE;
        }
        if (player.getWorld() != chunk.getWorld()) {
            return ProjectionResult.WORLD_MISMATCH;
        }
        if (!chunk.isLoaded()) {
            return ProjectionResult.CHUNK_NOT_LOADED;
        }
        if (!(player instanceof CraftPlayer craftPlayer)
                || !(chunk.getWorld() instanceof CraftWorld craftWorld)) {
            return ProjectionResult.UNSUPPORTED_RUNTIME;
        }

        ServerLevel level = craftWorld.getHandle();
        LevelChunk levelChunk = level.getChunkSource().getChunkNow(
                chunk.getX(),
                chunk.getZ()
        );
        if (levelChunk == null) {
            return ProjectionResult.CHUNK_NOT_LOADED;
        }

        Registry<Biome> biomeRegistry = CraftRegistry.getMinecraftRegistry(
                Registries.BIOME
        );
        Biome targetBiome = biomeRegistry
                .getOptional(CraftNamespacedKey.toMinecraft(targetBiomeKey))
                .orElse(null);
        if (targetBiome == null) {
            return ProjectionResult.TARGET_BIOME_UNAVAILABLE;
        }

        Holder<Biome> targetHolder = biomeRegistry.wrapAsHolder(targetBiome);
        byte[] payload = serializeBiomes(levelChunk, targetHolder);
        var packet = new ClientboundChunksBiomesPacket(List.of(
                new ClientboundChunksBiomesPacket.ChunkBiomeData(
                        levelChunk.getPos(),
                        payload
                )
        ));

        if (craftPlayer.getHandle().connection == null) {
            return ProjectionResult.CONNECTION_UNAVAILABLE;
        }
        craftPlayer.getHandle().connection.send(packet);
        return ProjectionResult.SENT;
    }

    private byte[] serializeBiomes(
            LevelChunk chunk,
            Holder<Biome> targetBiome
    ) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            for (LevelChunkSection section : chunk.getSections()) {
                PalettedContainer<Holder<Biome>> copy =
                        section.getBiomes().copy();
                replaceAllBiomeCells(copy, targetBiome);
                copy.write(buffer);
            }

            byte[] payload = new byte[buffer.writerIndex()];
            buffer.getBytes(0, payload);
            return payload;
        } finally {
            buffer.release();
        }
    }

    private void replaceAllBiomeCells(
            PalettedContainer<Holder<Biome>> biomes,
            Holder<Biome> targetBiome
    ) {
        for (int x = 0; x < BIOME_CELLS_PER_AXIS; x++) {
            for (int y = 0; y < BIOME_CELLS_PER_AXIS; y++) {
                for (int z = 0; z < BIOME_CELLS_PER_AXIS; z++) {
                    biomes.set(x, y, z, targetBiome);
                }
            }
        }
    }

    public enum ProjectionResult {
        SENT,
        WRONG_THREAD,
        PLAYER_OFFLINE,
        WORLD_MISMATCH,
        CHUNK_NOT_LOADED,
        TARGET_BIOME_UNAVAILABLE,
        CONNECTION_UNAVAILABLE,
        UNSUPPORTED_RUNTIME
    }
}
