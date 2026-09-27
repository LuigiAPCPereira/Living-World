package dev.signalshards.livingworld.features.waystones.paper;

import dev.signalshards.livingworld.core.i18n.MessageCatalog;
import dev.signalshards.livingworld.core.status.LivingWorldStatusProvider;
import dev.signalshards.livingworld.features.waystones.application.WaystoneService;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@NullMarked
public final class PaperWaystoneCommand implements BasicCommand {
    private final WaystoneService waystones;
    private final PaperWaystoneTravelService travel;
    private final MessageCatalog messages;
    private final LivingWorldStatusProvider statusProvider;
    private final double renameMaxDistance;
    private final PaperWaystoneMenu menu;

    public PaperWaystoneCommand(
            WaystoneService waystones,
            PaperWaystoneTravelService travel,
            MessageCatalog messages,
            LivingWorldStatusProvider statusProvider,
            double renameMaxDistance,
            PaperWaystoneMenu menu
    ) {
        this.waystones = Objects.requireNonNull(waystones, "serviço de waystones");
        this.travel = Objects.requireNonNull(travel, "viagem de waystones");
        this.messages = Objects.requireNonNull(messages, "catálogo de mensagens");
        this.statusProvider = Objects.requireNonNull(
                statusProvider,
                "diagnóstico do Living World"
        );
        if (!Double.isFinite(renameMaxDistance) || renameMaxDistance <= 0.0D) {
            throw new IllegalArgumentException(
                    "A distância para renomear waystones deve ser positiva"
            );
        }
        this.renameMaxDistance = renameMaxDistance;
        this.menu = Objects.requireNonNull(menu, "menu de waystones");
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("status")) {
            status(source.getSender());
            return;
        }

        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendMessage(messages.component(
                    NamedTextColor.RED,
                    "waystone.player-only"
            ));
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
        if (args[0].equalsIgnoreCase("rename")) {
            rename(player, args);
            return;
        }
        if (args[0].equalsIgnoreCase("menu")) {
            menu.open(player);
            return;
        }

        player.sendMessage(messages.component(
                NamedTextColor.YELLOW,
                "waystone.command-usage"
        ));
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length <= 1) {
            if (source.getSender() instanceof Player) {
                return List.of("status", "list", "travel", "rename", "menu");
            }
            return List.of("status");
        }

        if (!(source.getSender() instanceof Player player)) {
            return List.of();
        }

        if (args.length == 2 && (
                args[0].equalsIgnoreCase("travel")
                        || args[0].equalsIgnoreCase("rename")
        )) {
            int size = waystones.activatedWaystones(player.getUniqueId()).size();
            List<String> suggestions = new ArrayList<>(size);
            for (int index = 1; index <= size; index++) {
                suggestions.add(Integer.toString(index));
            }
            return suggestions;
        }

        return List.of();
    }

    private void rename(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(messages.component(
                    NamedTextColor.YELLOW,
                    "waystone.rename-usage"
            ));
            return;
        }

        int selection;
        try {
            selection = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            player.sendMessage(messages.component(
                    NamedTextColor.RED,
                    "waystone.rename-invalid-selection"
            ));
            return;
        }

        var activated = waystones.activatedWaystones(player.getUniqueId());
        if (selection < 1 || selection > activated.size()) {
            player.sendMessage(messages.component(
                    NamedTextColor.RED,
                    "waystone.rename-invalid-selection"
            ));
            return;
        }

        var target = activated.get(selection - 1);
        if (!player.getWorld().getUID().equals(target.worldId())
                || distanceSquaredToAnchor(player, target) >
                renameMaxDistance * renameMaxDistance) {
            player.sendMessage(messages.component(
                    NamedTextColor.YELLOW,
                    "waystone.rename-too-far",
                    renameMaxDistance
            ));
            return;
        }

        String newName = String.join(
                " ",
                Arrays.copyOfRange(args, 2, args.length)
        );
        try {
            var renamed = waystones.renameActivated(
                    player.getUniqueId(),
                    target.id(),
                    newName
            );
            if (renamed.isEmpty()) {
                player.sendMessage(messages.component(
                        NamedTextColor.RED,
                        "waystone.rename-not-activated"
                ));
                return;
            }

            player.sendMessage(messages.component(
                    NamedTextColor.GREEN,
                    "waystone.renamed",
                    target.name(),
                    renamed.orElseThrow().name()
            ));
        } catch (IllegalArgumentException exception) {
            player.sendMessage(messages.component(
                    NamedTextColor.RED,
                    "waystone.rename-invalid-name"
            ));
        }
    }

    private double distanceSquaredToAnchor(
            Player player,
            dev.signalshards.livingworld.features.waystones.domain.Waystone waystone
    ) {
        var location = player.getLocation();
        double dx = location.getX() - (waystone.x() + 0.5D);
        double dy = location.getY() - waystone.y();
        double dz = location.getZ() - (waystone.z() + 0.5D);
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    private void status(CommandSender sender) {
        var snapshot = statusProvider.snapshot();
        sender.sendMessage(messages.component(
                NamedTextColor.GOLD,
                "status.header",
                snapshot.version()
        ));
        sender.sendMessage(messages.component(
                NamedTextColor.GRAY,
                "status.world",
                snapshot.worldName()
        ));
        sender.sendMessage(messages.component(
                NamedTextColor.AQUA,
                "status.calendar",
                snapshot.seasonName(),
                snapshot.year(),
                snapshot.month(),
                snapshot.day()
        ));
        sender.sendMessage(messages.component(
                NamedTextColor.GRAY,
                "status.players",
                snapshot.onlinePlayers()
        ));
        sender.sendMessage(messages.component(
                NamedTextColor.GRAY,
                "status.hud",
                snapshot.hudSessions()
        ));
        sender.sendMessage(messages.component(
                NamedTextColor.GRAY,
                "status.desire-lines",
                snapshot.desireLineCachedChunks()
        ));
        sender.sendMessage(messages.component(
                NamedTextColor.GRAY,
                "status.waystones",
                snapshot.registeredWaystones()
        ));
        sender.sendMessage(messages.component(
                NamedTextColor.GREEN,
                "status.performance",
                snapshot.tpsOneMinute(),
                snapshot.averageTickTimeMillis()
        ));
        sender.sendMessage(messages.component(
                NamedTextColor.YELLOW,
                "status.restart-required"
        ));
    }

    private void list(Player player) {
        var activated = waystones.activatedWaystones(player.getUniqueId());
        if (activated.isEmpty()) {
            player.sendMessage(messages.component(
                    NamedTextColor.YELLOW,
                    "waystone.list-empty"
            ));
            return;
        }

        player.sendMessage(messages.component(
                NamedTextColor.GOLD,
                "waystone.list-header",
                activated.size()
        ));
        for (int index = 0; index < activated.size(); index++) {
            player.sendMessage(messages.component(
                    NamedTextColor.AQUA,
                    "waystone.list-entry",
                    index + 1,
                    activated.get(index).name()
            ));
        }
    }

    private void travel(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(messages.component(
                    NamedTextColor.YELLOW,
                    "waystone.travel-usage"
            ));
            return;
        }

        int selection;
        try {
            selection = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            player.sendMessage(messages.component(
                    NamedTextColor.RED,
                    "waystone.travel-invalid"
            ));
            return;
        }

        var activated = waystones.activatedWaystones(player.getUniqueId());
        if (selection < 1 || selection > activated.size()) {
            player.sendMessage(messages.component(
                    NamedTextColor.RED,
                    "waystone.travel-invalid"
            ));
            return;
        }

        var target = activated.get(selection - 1);
        PaperWaystoneTravelFeedback.sendStart(
                player,
                messages,
                target.name()
        );
        travel.travel(player, target.id()).thenAccept(result ->
                PaperWaystoneTravelFeedback.sendResult(
                        player,
                        messages,
                        target.name(),
                        result
                )
        );
    }
}
