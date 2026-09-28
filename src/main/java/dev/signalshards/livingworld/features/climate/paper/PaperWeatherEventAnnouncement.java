package dev.signalshards.livingworld.features.climate.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.features.climate.application.WeatherEventPlan;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Objects;

public final class PaperWeatherEventAnnouncement {
    private final MessageCatalog messages;
    private final boolean enabled;

    public PaperWeatherEventAnnouncement(
            MessageCatalog messages,
            boolean enabled
    ) {
        this.messages = Objects.requireNonNull(messages, "catálogo de mensagens");
        this.enabled = enabled;
    }

    public void announce(World world, WeatherEventPlan plan) {
        Objects.requireNonNull(world, "mundo");
        Objects.requireNonNull(plan, "plano climático");
        if (!enabled) {
            return;
        }

        long seconds = plan.duration().toSeconds();
        StyledWeather styled = switch (plan.tendency()) {
            case TEMPO_LIMPO -> new StyledWeather(
                    "climate.weather-event.clear",
                    NamedTextColor.YELLOW
            );
            case ESTAVEL -> throw new IllegalArgumentException(
                    "Estado estável não deve produzir anúncio de evento climático"
            );
            case PRECIPITACAO -> new StyledWeather(
                    "climate.weather-event.precipitation",
                    NamedTextColor.AQUA
            );
            case TEMPESTADE -> new StyledWeather(
                    "climate.weather-event.storm",
                    NamedTextColor.LIGHT_PURPLE
            );
        };

        for (Player player : world.getPlayers()) {
            player.sendMessage(messages.component(
                    styled.color(),
                    styled.key(),
                    seconds
            ));
        }
    }

    private record StyledWeather(String key, NamedTextColor color) {
    }
}
