package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.features.waystones.application.WaystoneService;
import dev.signalshards.livingworld.features.waystones.application.WaystoneTravelResult;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@NullMarked
public final class PaperWaystoneCommand implements BasicCommand {
    private final WaystoneService waystones;
    private final PaperWaystoneTravelService travel;
    private final MessageCatalog messages;

    public PaperWaystoneCommand(
            WaystoneService waystones,
            PaperWaystoneTravelService travel,
            MessageCatalog messages
    ) {
        this.waystones = Objects.requireNonNull(waystones, "serviço de waystones");
        this.travel = Objects.requireNonNull(travel, "viagem de waystones");
        this.messages = Objects.requireNonNull(messages, "catálogo de mensagens");
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendPlainMessage(messages.text("waystone.player-only"));
            return;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("list")
                || args[0].equalsIgnoreCase("waystones")) {
            list(player);
            return;
        }

        if (args[0].equalsIgnoreCase("travel")) {
            travel(player, args);
            return;
        }

        player.sendPlainMessage(messages.text("waystone.command-usage"));
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (!(source.getSender() instanceof Player player)) {
            return List.of();
        }

        if (args.length <= 1) {
            return List.of("list", "travel");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("travel")) {
            int size = waystones.activatedWaystones(player.getUniqueId()).size();
            List<String> suggestions = new ArrayList<>(size);
            for (int index = 1; index <= size; index++) {
                suggestions.add(Integer.toString(index));
            }
            return suggestions;
        }

        return List.of();
    }

    private void list(Player player) {
        var activated = waystones.activatedWaystones(player.getUniqueId());
        if (activated.isEmpty()) {
            player.sendPlainMessage(messages.text("waystone.list-empty"));
            return;
        }

        player.sendPlainMessage(messages.text("waystone.list-header", activated.size()));
        for (int index = 0; index < activated.size(); index++) {
            player.sendPlainMessage(messages.text(
                    "waystone.list-entry",
                    index + 1,
                    activated.get(index).name()
            ));
        }
    }

    private void travel(Player player, String[] args) {
        if (args.length != 2) {
            player.sendPlainMessage(messages.text("waystone.travel-usage"));
            return;
        }

        int selection;
        try {
            selection = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            player.sendPlainMessage(messages.text("waystone.travel-invalid"));
            return;
        }

        var activated = waystones.activatedWaystones(player.getUniqueId());
        if (selection < 1 || selection > activated.size()) {
            player.sendPlainMessage(messages.text("waystone.travel-invalid"));
            return;
        }

        var target = activated.get(selection - 1);
        player.sendPlainMessage(messages.text("waystone.travel-start", target.name()));
        travel.travel(player, target.id()).thenAccept(result -> sendResult(player, target.name(), result));
    }

    private void sendResult(Player player, String name, WaystoneTravelResult result) {
        if (!player.isOnline()) {
            return;
        }

        String key = switch (result) {
            case SUCCESS -> "waystone.travel-success";
            case NOT_ACTIVATED -> "waystone.travel-not-activated";
            case WORLD_UNAVAILABLE -> "waystone.travel-world-unavailable";
            case DESTINATION_UNSAFE -> "waystone.travel-unsafe";
            case PLAYER_OFFLINE -> null;
            case TELEPORT_REJECTED -> "waystone.travel-rejected";
        };
        if (key != null) {
            player.sendPlainMessage(messages.text(key, name));
        }
    }
}
