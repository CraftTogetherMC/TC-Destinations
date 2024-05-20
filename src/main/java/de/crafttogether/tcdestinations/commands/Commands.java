package de.crafttogether.tcdestinations.commands;

import com.google.common.collect.ImmutableMap;

import com.bergerkiller.bukkit.common.config.FileConfiguration;
import com.bergerkiller.bukkit.common.utils.EntityUtil;
import com.bergerkiller.bukkit.common.utils.WorldUtil;
import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.pathfinding.*;

import de.crafttogether.TCDestinations;
import de.crafttogether.common.commands.CloudSimpleHandler;
import de.crafttogether.common.commands.CommandSender;
import de.crafttogether.common.platform.bukkit.CloudBukkitHandler;
import de.crafttogether.common.shaded.org.incendo.cloud.annotations.Command;
import de.crafttogether.common.shaded.org.incendo.cloud.annotations.CommandDescription;
import de.crafttogether.common.shaded.org.incendo.cloud.annotations.Permission;
import de.crafttogether.common.shaded.org.incendo.cloud.annotations.string.PropertyReplacingStringProcessor;
import de.crafttogether.common.util.AudienceUtil;
import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.tcdestinations.Localization;
import de.crafttogether.tcdestinations.destinations.Destination;
import de.crafttogether.tcdestinations.destinations.DestinationType;
import de.crafttogether.tcdestinations.util.TCHelper;

import de.crafttogether.common.shaded.org.incendo.cloud.CommandManager;
import de.crafttogether.common.shaded.net.kyori.adventure.text.Component;
import de.crafttogether.common.shaded.net.kyori.adventure.text.TextComponent;
import de.crafttogether.common.shaded.net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Range;
import org.incendo.cloud.annotations.Argument;

import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Commands {
    private static final TCDestinations plugin = TCDestinations.plugin;
    private static final CloudSimpleHandler cloud = new CloudBukkitHandler();

    private FileConfiguration config;

    public void enable(TCDestinations plugin) {
        cloud.enable();

        // Command handlers
        DestinationCommands commands_destination = new DestinationCommands();

        config = new FileConfiguration(plugin.getDataFolder() + File.separator + "commands.yml");
        config.load();

        cloud.getParser().stringProcessor(
                new PropertyReplacingStringProcessor(
                        s -> ImmutableMap.of(
                                "command.destination", (String) config.get("commands.destination"),
                                "command.destinations", (String) config.get("commands.destinations"),
                                "command.destedit", (String) config.get("commands.destedit"),
                                "command.mobenter", (String) config.get("commands.mobenter"),
                                "command.mobeject", (String) config.get("commands.mobeject")
                        ).get(s)
                )
        );
        
        // Register command placeholders
        LocalizationManager localization = plugin.getLocalizationManager();
        localization.addPlaceholder("cmd_destination", "/" + config.get("commands.destination"));
        localization.addPlaceholder("cmd_destinations", "/" + config.get("commands.destinations"));
        localization.addPlaceholder("cmd_destedit", "/" + config.get("commands.destedit"));
        localization.addPlaceholder("cmd_mobenter", "/" + config.get("commands.mobenter"));
        localization.addPlaceholder("cmd_mobeject", "/" + config.get("commands.mobeject"));

        // Suggestions
        cloud.suggest("onlinePlayers", (context, input) -> Bukkit.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList()));

        cloud.suggest("serverName", (context, input) -> plugin.getDestinationStorage().getDestinations().stream().distinct()
                .map(Destination::getServer)
                .collect(Collectors.toList()));

        cloud.suggest("destinationName", (context, input) -> plugin.getDestinationStorage().getDestinations().stream()
                .map(Destination::getName)
                .collect(Collectors.toList()));

        cloud.suggest("destinationType", (context, input) -> {
            List<String> result = DestinationType.getTypes().stream()
                    .map(DestinationType::getDisplayName)
                    .collect(Collectors.toList());
            result.add(plugin.getConfig().getString("DestinationTypeAll.DisplayName"));
            return result;
        });

        // Register Annotations
        cloud.annotations(this);
        cloud.annotations(commands_destination);
    }

    @Command("tcdestinations")
    @CommandDescription("Zeigt die aktuelle Version des Plugin")
    public void tcdestinations(
            final CommandSender sender
    ) {
        new UpdateChecker(TCDestinations.platformLayer).checkUpdatesAsync((err, installedVersion, installedBuild, build) -> {
            if (err != null) {
                plugin.getLogger().warning("An error occurred while receiving update information.");
                plugin.getLogger().warning("Error: " + err.getMessage());
            }

            List<Placeholder> resolvers = new ArrayList<>();
            Component message;

            if (build == null) {
                resolvers.add(Placeholder.set("currentVersion", installedVersion));
                resolvers.add(Placeholder.set("currentBuild", installedBuild));

                message = plugin.getLocalizationManager().miniMessage()
                        .deserialize("<prefix/><gold>" + plugin.getName() + " version: </gold><yellow>" + installedVersion + " #" + installedBuild + "</yellow><newLine/>");

                if (err == null)
                    message = message.append(Localization.UPDATE_LASTBUILD.deserialize(resolvers));
                else
                    message = message.append(Localization.UPDATE_ERROR.deserialize(
                            Placeholder.set("error", err.getMessage())));
            }

            else {
                resolvers.add(Placeholder.set("version", build.getVersion()));
                resolvers.add(Placeholder.set("build", build.getNumber()));
                resolvers.add(Placeholder.set("fileName", build.getFileName()));
                resolvers.add(Placeholder.set("fileSize", build.getHumanReadableFileSize()));
                resolvers.add(Placeholder.set("url", build.getUrl()));
                resolvers.add(Placeholder.set("currentVersion", installedVersion));
                resolvers.add(Placeholder.set("currentBuild", installedBuild));

                if (build.getType().equals(BuildType.RELEASE))
                    message = Localization.UPDATE_RELEASE.deserialize(resolvers);
                else
                    message = Localization.UPDATE_DEVBUILD.deserialize(resolvers);
            }

            sender.sendMessage(message);
        }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));
    }

    @Command("tcdestinations reload")
    @CommandDescription("This command reloads the configuration of the plugin")
    public void tcdestinations_reload(
            final CommandSender sender
    ) {
        plugin.getLogger().info("Disconnecting destination-storage...");
        plugin.getDestinationStorage().disconnect();

        plugin.getLogger().info("Reloading config.yml...");
        plugin.reloadConfig();

        plugin.getLogger().info("Reloading enterMessages.yml...");
        plugin.getEnterMessages().load();

        plugin.getLogger().info("Reloading localization...");
        plugin.getLocalizationManager().loadLocalization(plugin.getConfig().getString("Settings.Language"));

        plugin.getLogger().info("Reconnecting destination-storage...");
        plugin.getDestinationStorage().connect();

        plugin.getLogger().info("Reload completed...");
        sender.sendMessage(Localization.CONFIG_RELOADED.deserialize());
    }

    @Command("tcdestinations debug")
    @CommandDescription("This command helps debugging destinations")
    public void tcdestinations_debug(
            final CommandSender sender
    ) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                PathNode randomNode = null;
                Collection<PathWorld> worlds;
                List<PathNode> destinations = new ArrayList<>();
                Player player = (Player) sender;
                World playerWorld = player.getWorld();
                worlds = TrainCarts.plugin.getPathProvider().getWorlds();

                for (PathWorld world : worlds) {
                    for (PathNode node : world.getNodes()) {
                        if (!node.containsOnlySwitcher()) {
                            destinations.add(node);
                        }

                        else if (randomNode == null) {
                            randomNode = node;
                        }
                    }
                }

                if (randomNode == null) {
                    TextComponent output = Component.text("Es konnte keine Weiche gefunden werden.")
                            .color(NamedTextColor.RED);
                    AudienceUtil.Bukkit.audiences.player(player).sendMessage(output);
                    return;
                }

                TextComponent output = Component.text("Überprüfe " + destinations.size() + " Verbindungen...")
                        .color(NamedTextColor.YELLOW)
                        .append(Component.newline());
                AudienceUtil.Bukkit.audiences.player(player).sendMessage(output);

                int errors = 0, connections = 0;

                for (PathNode destination : destinations) {
                    output = Component.empty();

                    output = output.append(Component.text("Überprüfe Verbindung zu " + destination + "... ")
                            .color(NamedTextColor.YELLOW));

                    PathConnection[] route = randomNode.findRoute(destination);

                    if (route.length > 0) {
                        output = output.append(Component.text("Verbindung gefunden! (" + route.length + " Nodes)")
                                .color(NamedTextColor.GREEN));
                        connections++;
                    }
                    else {
                        output = output.append(Component.text("Es konnte keine Verbindung gefunden werden!")
                                .color(NamedTextColor.RED));
                        errors++;
                    }

                    AudienceUtil.Bukkit.audiences.player(player).sendMessage(output);
                }
            }
        });
    }

    @Command(value="${command.mobenter} [radius]", requiredSender=Player.class) // TODO: Check
    @CommandDescription("Lässt Tiere in der nahen Umgebung in den ausgewählten Zug einsteigen.")
    @Permission("craftbahn.command.mobenter")
    public void mobenter(
            final Player player,
            @Argument(value="radius") @Range(min = "1", max = "16") Integer radius
    ) {
        MinecartGroup group = TCHelper.getTrain(player.getUniqueId());

        if (group == null) {
            Localization.COMMAND_NOTRAIN.message(player.getUniqueId());
            return;
        }

        if (radius == null)
            radius = 8;

        Location center = player.getLocation();
        int entered = 0;
        for (Entity entity : WorldUtil.getNearbyEntities(center, radius, radius, radius)) {
            if (entity.getVehicle() != null)
                continue;

            if (EntityUtil.isMob(entity)) {
                for (MinecartMember<?> member : group) {
                    if (member.getAvailableSeatCount(entity) > 0 && member.addPassengerForced(entity)) {
                        entered++;
                        break;
                    }
                }
            }
        }

        if (entered > 0)
            Localization.COMMAND_MOBENTER_SUCCESS.message(player.getUniqueId(),
                    Placeholder.set("amount", String.valueOf(entered)));
        else
            Localization.COMMAND_MOBENTER_FAILED.message(player.getUniqueId(),
                    Placeholder.set("radius", String.valueOf(radius)));
    }

    @Command(value="${command.mobeject}", requiredSender=Player.class)
    @CommandDescription("Lässt alle Tiere aus dem ausgewählten Zug aussteigen.")
    @Permission("craftbahn.command.mobeject")
    public void mobeject(
            final Player player
    ) {
        MinecartGroup group = TCHelper.getTrain(player.getUniqueId());

        if (group == null) {
            Localization.COMMAND_NOTRAIN.message(player.getUniqueId());
            return;
        }

        for (MinecartMember<?> member : group) {
            if (!member.getEntity().hasPassenger()) continue;

            for (Entity passenger : member.getEntity().getPassengers()) {
                if (EntityUtil.isMob(passenger))
                    member.eject();
            }
        }

        Localization.COMMAND_MOBEJECT_SUCCESS.message(player.getUniqueId());
    }

    public static CloudSimpleHandler getCloud() {
        return cloud;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public CommandManager<de.crafttogether.common.commands.CommandSender> getManager() {
        return cloud.getManager();
    }
}