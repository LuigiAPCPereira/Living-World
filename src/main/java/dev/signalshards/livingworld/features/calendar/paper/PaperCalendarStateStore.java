package dev.signalshards.livingworld.features.calendar.paper;

import dev.signalshards.livingworld.features.calendar.domain.CalendarState;
import dev.signalshards.livingworld.features.calendar.persistence.CalendarStateStore;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public final class PaperCalendarStateStore implements CalendarStateStore {
    private static final String KEY_NAME = "calendar_elapsed_days";

    private final PersistentDataContainer container;
    private final NamespacedKey key;

    public PaperCalendarStateStore(PersistentDataContainer container, NamespacedKey key) {
        this.container = Objects.requireNonNull(container, "container de dados persistentes");
        this.key = Objects.requireNonNull(key, "chave do calendário");
    }

    public static PaperCalendarStateStore forWorld(Plugin plugin, World world) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(world, "mundo");
        return new PaperCalendarStateStore(
                world.getPersistentDataContainer(),
                new NamespacedKey(plugin, KEY_NAME)
        );
    }

    @Override
    public CalendarState load() {
        long elapsedDays = container.getOrDefault(key, PersistentDataType.LONG, 0L);
        return new CalendarState(elapsedDays);
    }

    @Override
    public void save(CalendarState state) {
        Objects.requireNonNull(state, "estado do calendário");
        container.set(key, PersistentDataType.LONG, state.elapsedDays());
    }
}
