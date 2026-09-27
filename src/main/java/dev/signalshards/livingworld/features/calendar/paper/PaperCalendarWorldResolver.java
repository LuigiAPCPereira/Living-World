package dev.signalshards.livingworld.features.calendar.paper;

import org.bukkit.Server;
import org.bukkit.World;

import java.util.Objects;

public final class PaperCalendarWorldResolver {
    private PaperCalendarWorldResolver() {
    }

    public static World resolve(Server server, String configuredWorldName) {
        Objects.requireNonNull(server, "servidor");

        if (configuredWorldName != null && !configuredWorldName.isBlank()) {
            World configured = server.getWorld(configuredWorldName);
            if (configured == null) {
                throw new IllegalStateException(
                        "O mundo configurado para o calendário não foi encontrado: " + configuredWorldName
                );
            }
            ensureOverworld(configured);
            return configured;
        }

        return server.getWorlds().stream()
                .filter(world -> world.getEnvironment() == World.Environment.NORMAL)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Nenhum mundo do Overworld está disponível para o calendário"
                ));
    }

    private static void ensureOverworld(World world) {
        if (world.getEnvironment() != World.Environment.NORMAL) {
            throw new IllegalStateException(
                    "O calendário precisa usar um mundo do Overworld: " + world.getName()
            );
        }
    }
}
