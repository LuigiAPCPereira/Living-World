package dev.signalshards.livingworld.features.hud.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.features.calendar.application.CalendarView;
import dev.signalshards.livingworld.features.calendar.domain.CalendarDate;
import dev.signalshards.livingworld.features.climate.application.ThermalRuntimeReadoutProvider;
import dev.signalshards.livingworld.features.hud.domain.HeadingPolicy;
import dev.signalshards.livingworld.features.seasons.domain.Season;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class PaperHudModule implements LivingWorldModule, Listener {
    private final JavaPlugin plugin;
    private final PaperHudSettings settings;
    private final CalendarView calendar;
    private final MessageCatalog messages;
    private final PaperHudActionBarRenderer actionBarRenderer;
    private final Map<UUID, PlayerHudSession> sessions = new HashMap<>();

    private BukkitTask updateTask;

    public PaperHudModule(
            JavaPlugin plugin,
            PaperHudSettings settings,
            CalendarView calendar,
            MessageCatalog messages,
            HeadingPolicy headingPolicy,
            ThermalRuntimeReadoutProvider thermalReadoutProvider,
            TemperatureColorPolicy temperatureColorPolicy
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.settings = Objects.requireNonNull(settings, "configuração de HUD");
        this.calendar = Objects.requireNonNull(calendar, "calendário");
        this.messages = Objects.requireNonNull(messages, "catálogo de mensagens");
        this.actionBarRenderer = new PaperHudActionBarRenderer(
                settings,
                messages,
                headingPolicy,
                thermalReadoutProvider,
                temperatureColorPolicy
        );
    }

    @Override
    public void enable() {
        if (!settings.enabled()) {
            return;
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            attach(player);
        }

        updateTask = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                this::updateAll,
                settings.updatePeriodTicks(),
                settings.updatePeriodTicks()
        );
    }

    @Override
    public void disable() {
        if (!settings.enabled()) {
            return;
        }

        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            detach(player);
        }
        sessions.clear();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        attach(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        detach(event.getPlayer());
    }

    public int activeSessionCount() {
        return sessions.size();
    }

    private void attach(Player player) {
        if (sessions.containsKey(player.getUniqueId())) {
            return;
        }

        BossBar calendarBar = null;
        if (settings.calendarBossBarEnabled()) {
            calendarBar = BossBar.bossBar(
                    Component.empty(),
                    0.0F,
                    BossBar.Color.GREEN,
                    BossBar.Overlay.PROGRESS
            );
            player.showBossBar(calendarBar);
        }

        PlayerHudSession session = new PlayerHudSession(calendarBar);
        sessions.put(player.getUniqueId(), session);
        update(player, session);
    }

    private void detach(Player player) {
        PlayerHudSession session = sessions.remove(player.getUniqueId());
        if (session != null && session.calendarBar != null) {
            player.hideBossBar(session.calendarBar);
        }
    }

    private void updateAll() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerHudSession session = sessions.computeIfAbsent(
                    player.getUniqueId(),
                    ignored -> new PlayerHudSession(null)
            );
            update(player, session);
        }
    }

    private void update(Player player, PlayerHudSession session) {
        updateCalendarBar(session);
        if (settings.actionBarEnabled()) {
            player.sendActionBar(actionBarRenderer.render(player));
        }
    }

    private void updateCalendarBar(PlayerHudSession session) {
        if (session.calendarBar == null) {
            return;
        }

        CalendarDate date = calendar.currentDate();
        Season season = calendar.currentSeason();
        CalendarBarKey key = new CalendarBarKey(date, season, calendar.daysPerMonth());
        if (key.equals(session.lastCalendarKey)) {
            return;
        }

        Component title = Component.text(
                seasonText(season),
                textColorFor(season)
        ).append(Component.text("  •  ", NamedTextColor.DARK_GRAY))
                .append(Component.text(
                        messages.text(
                                "hud.calendar.date",
                                date.year(),
                                date.month(),
                                date.day()
                        ),
                        NamedTextColor.GOLD
                ));
        session.calendarBar.name(title);
        session.calendarBar.progress(Math.clamp(
                (float) date.day() / key.daysPerMonth(),
                0.0F,
                1.0F
        ));
        session.calendarBar.color(colorFor(season));
        session.lastCalendarKey = key;
    }

    private String seasonText(Season season) {
        return messages.text(switch (season) {
            case PRIMAVERA -> "season.spring";
            case VERAO -> "season.summer";
            case OUTONO -> "season.autumn";
            case INVERNO -> "season.winter";
        });
    }

    private BossBar.Color colorFor(Season season) {
        return switch (season) {
            case PRIMAVERA -> BossBar.Color.GREEN;
            case VERAO -> BossBar.Color.YELLOW;
            case OUTONO -> BossBar.Color.RED;
            case INVERNO -> BossBar.Color.BLUE;
        };
    }

    private NamedTextColor textColorFor(Season season) {
        return switch (season) {
            case PRIMAVERA -> NamedTextColor.LIGHT_PURPLE;
            case VERAO -> NamedTextColor.YELLOW;
            case OUTONO -> NamedTextColor.GOLD;
            case INVERNO -> NamedTextColor.AQUA;
        };
    }

    private static final class PlayerHudSession {
        private final BossBar calendarBar;
        private CalendarBarKey lastCalendarKey;

        private PlayerHudSession(BossBar calendarBar) {
            this.calendarBar = calendarBar;
        }
    }

    private record CalendarBarKey(
            CalendarDate date,
            Season season,
            int daysPerMonth
    ) {
    }
}
