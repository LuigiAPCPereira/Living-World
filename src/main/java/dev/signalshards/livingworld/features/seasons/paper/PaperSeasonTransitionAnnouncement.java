package dev.signalshards.livingworld.features.seasons.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.features.calendar.application.CalendarProgress;
import dev.signalshards.livingworld.features.calendar.application.CalendarProgressListener;
import dev.signalshards.livingworld.features.seasons.domain.Season;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Objects;

public final class PaperSeasonTransitionAnnouncement
        implements CalendarProgressListener {
    private static final Title.Times TITLE_TIMES = Title.Times.times(
            Duration.ofMillis(500),
            Duration.ofSeconds(3),
            Duration.ofMillis(750)
    );

    private final Server server;
    private final MessageCatalog messages;
    private final boolean enabled;

    public PaperSeasonTransitionAnnouncement(
            Server server,
            MessageCatalog messages,
            boolean enabled
    ) {
        this.server = Objects.requireNonNull(server, "servidor");
        this.messages = Objects.requireNonNull(messages, "catálogo de mensagens");
        this.enabled = enabled;
    }

    @Override
    public void onProgress(CalendarProgress progress) {
        Objects.requireNonNull(progress, "avanço do calendário");
        if (!enabled) {
            return;
        }

        progress.seasonTransition().ifPresent(transition -> {
            Season season = transition.current();
            var date = progress.currentDate();
            Title title = Title.title(
                    Component.text(
                            seasonText(season),
                            textColorFor(season)
                    ),
                    Component.text(
                            messages.text(
                                    "season.transition-subtitle",
                                    date.year(),
                                    date.month(),
                                    date.day()
                            ),
                            NamedTextColor.GOLD
                    ),
                    TITLE_TIMES
            );
            for (Player player : server.getOnlinePlayers()) {
                player.showTitle(title);
            }
        });
    }

    private String seasonText(Season season) {
        return messages.text(switch (season) {
            case PRIMAVERA -> "season.spring";
            case VERAO -> "season.summer";
            case OUTONO -> "season.autumn";
            case INVERNO -> "season.winter";
        });
    }

    private NamedTextColor textColorFor(Season season) {
        return switch (season) {
            case PRIMAVERA -> NamedTextColor.LIGHT_PURPLE;
            case VERAO -> NamedTextColor.YELLOW;
            case OUTONO -> NamedTextColor.GOLD;
            case INVERNO -> NamedTextColor.AQUA;
        };
    }
}
