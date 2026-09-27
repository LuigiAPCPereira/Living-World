package dev.signalshards.livingworld.features.calendar.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.core.module.LivingWorldModule;
import dev.signalshards.livingworld.features.calendar.application.CalendarProgress;
import dev.signalshards.livingworld.features.calendar.application.CalendarProgressListener;
import dev.signalshards.livingworld.features.calendar.application.CalendarProgressTracker;
import dev.signalshards.livingworld.features.calendar.application.CalendarRuntime;
import dev.signalshards.livingworld.features.calendar.application.CalendarTimeSkipCause;
import dev.signalshards.livingworld.features.calendar.domain.CalendarRules;
import dev.signalshards.livingworld.features.seasons.domain.SeasonCycle;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class PaperCalendarModule implements LivingWorldModule, Listener {
    private static final long SAMPLE_PERIOD_TICKS = 20L;

    private final JavaPlugin plugin;
    private final World world;
    private final MessageCatalog messages;
    private final List<CalendarProgressListener> progressListeners;

    private CalendarRuntime runtime;
    private CalendarProgressTracker tracker;
    private BukkitTask samplerTask;

    public PaperCalendarModule(
            JavaPlugin plugin,
            World world,
            MessageCatalog messages,
            CalendarProgressListener... progressListeners
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.world = Objects.requireNonNull(world, "mundo");
        this.messages = Objects.requireNonNull(messages, "catálogo de mensagens");
        this.progressListeners = List.of(progressListeners);
    }

    @Override
    public void enable() {
        PaperCalendarStateStore store = PaperCalendarStateStore.forWorld(plugin, world);
        runtime = CalendarRuntime.load(
                CalendarRules.livingWorldDefaults(),
                SeasonCycle.livingWorldDefaults(),
                store
        );
        tracker = new CalendarProgressTracker(world.getFullTime());

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        samplerTask = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                this::sampleWorldTime,
                SAMPLE_PERIOD_TICKS,
                SAMPLE_PERIOD_TICKS
        );

        var date = runtime.currentDate();
        plugin.getLogger().info(messages.text(
                "calendar.runtime-enabled",
                world.getName(),
                date.year(),
                date.month(),
                date.day()
        ));
    }

    @Override
    public void disable() {
        if (samplerTask != null) {
            samplerTask.cancel();
            samplerTask = null;
        }
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    @SuppressWarnings("UnstableApiUsage")
    public void onTimeSkip(TimeSkipEvent event) {
        if (!event.getWorld().equals(world)) {
            return;
        }

        CalendarTimeSkipCause cause = switch (event.getSkipReason()) {
            case NIGHT_SKIP -> CalendarTimeSkipCause.SONO;
            case COMMAND -> CalendarTimeSkipCause.COMANDO;
            case CUSTOM -> CalendarTimeSkipCause.PLUGIN;
        };

        applyAdvance(tracker.observeSkip(
                cause,
                world.getFullTime(),
                event.getSkipAmount()
        ));
    }

    private void sampleWorldTime() {
        applyAdvance(tracker.sample(world.getFullTime()));
    }

    private void applyAdvance(long days) {
        runtime.advanceDays(days).ifPresent(progress -> {
            logProgress(progress);
            notifyProgress(progress);
        });
    }

    private void logProgress(CalendarProgress progress) {
        progress.seasonTransition().ifPresent(transition ->
                plugin.getLogger().info(messages.text(
                        "calendar.season-changed",
                        transition.current()
                ))
        );
    }

    private void notifyProgress(CalendarProgress progress) {
        for (CalendarProgressListener progressListener : progressListeners) {
            try {
                progressListener.onProgress(progress);
            } catch (RuntimeException exception) {
                plugin.getLogger().log(
                        Level.SEVERE,
                        messages.text("calendar.progress-listener-failed", exception.getMessage()),
                        exception
                );
            }
        }
    }
}
