package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.features.climate.domain.ClimateProfile;
import dev.signalshards.livingworld.features.climate.domain.ClimateProfileClassifier;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

/**
 * Produz um perfil climático representativo do mundo sem percorrer chunks.
 *
 * <p>Quando existem jogadores, amostra no máximo 32 posições ativas e calcula
 * a média de temperatura/umidade exposta pela API do Paper. Sem jogadores,
 * usa apenas o spawn do mundo como fallback.</p>
 */
public final class PaperClimateSampler {
    static final int MAX_PLAYER_SAMPLES = 32;

    private final ClimateProfileClassifier classifier;

    public PaperClimateSampler(ClimateProfileClassifier classifier) {
        this.classifier = Objects.requireNonNull(classifier, "classificador climático");
    }

    public ClimateProfile sample(World world) {
        Objects.requireNonNull(world, "mundo");

        List<Player> players = world.getPlayers();
        if (players.isEmpty()) {
            return sampleLocations(world, List.of(world.getSpawnLocation()));
        }

        return sampleLocations(
                world,
                players.stream()
                        .limit(MAX_PLAYER_SAMPLES)
                        .map(Player::getLocation)
                        .toList()
        );
    }

    private ClimateProfile sampleLocations(World world, List<Location> locations) {
        double temperatureSum = 0;
        double humiditySum = 0;

        for (Location location : locations) {
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            temperatureSum += world.getTemperature(x, y, z);
            humiditySum += world.getHumidity(x, y, z);
        }

        double count = locations.size();
        return classifier.classify(
                temperatureSum / count,
                humiditySum / count
        );
    }
}
