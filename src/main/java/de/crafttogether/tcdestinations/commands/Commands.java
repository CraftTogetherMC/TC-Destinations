package de.crafttogether.tcdestinations.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.*;
import cloud.commandframework.annotations.specifier.Range;
import com.bergerkiller.bukkit.common.cloud.CloudSimpleHandler;
import com.bergerkiller.bukkit.common.config.FileConfiguration;
import com.bergerkiller.bukkit.common.utils.EntityUtil;
import com.bergerkiller.bukkit.common.utils.WorldUtil;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.google.common.collect.ImmutableMap;
import de.crafttogether.TCDestinations;
import de.crafttogether.common.dep.net.kyori.adventure.text.Component;
import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.common.util.PluginUtil;
import de.crafttogether.tcdestinations.Localization;
import de.crafttogether.tcdestinations.destinations.Destination;
import de.crafttogether.tcdestinations.destinations.DestinationType;
import de.crafttogether.tcdestinations.util.TCHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Commands {
    private static final TCDestinations plugin = TCDestinations.plugin;
    private static final CloudSimpleHandler cloud = new CloudSimpleHandler();

    private FileConfiguration config;

    public void enable(TCDestinations plugin) {
        cloud.enable(plugin);

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

    @CommandMethod("tcdestinations")
    @CommandDescription("Zeigt die aktuelle Version des Plugin")
    public void tcdestinations(
            final CommandSender sender
    ) {
        new UpdateChecker(plugin).checkUpdatesAsync((err, build, currentVersion, currentBuild) -> {
            if (err != null)
                err.printStackTrace();

            List<Placeholder> resolvers = new ArrayList<>();
            Component message;

            if (build == null) {
                resolvers.add(Placeholder.set("currentVersion", currentVersion));
                resolvers.add(Placeholder.set("currentBuild", currentBuild));

                message = plugin.getLocalizationManager().miniMessage()
                        .deserialize("<prefix/><gold>" + plugin.getName() + " version: </gold><yellow>" + currentVersion + " #" + currentBuild + "</yellow><newLine/>");

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
                resolvers.add(Placeholder.set("currentVersion", currentVersion));
                resolvers.add(Placeholder.set("currentBuild", currentBuild));

                if (build.getType().equals(BuildType.RELEASE))
                    message = Localization.UPDATE_RELEASE.deserialize(resolvers);
                else
                    message = Localization.UPDATE_DEVBUILD.deserialize(resolvers);
            }

            PluginUtil.adventure().sender(sender).sendMessage(message);
        }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));
    }

    @CommandMethod("tcdestinations reload")
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
        PluginUtil.adventure().sender(sender).sendMessage(Localization.CONFIG_RELOADED.deserialize());
    }

    @CommandMethod(value="${command.mobenter} [radius]", requiredSender=Player.class)
    @CommandDescription("Lässt Tiere in der nahen Umgebung in den ausgewählten Zug einsteigen.")
    @CommandPermission("craftbahn.command.mobenter")
    public void mobenter(
            final Player sender,
            @Argument(value="radius") @Range(min = "1", max = "16") Integer radius
    ) {
        MinecartGroup group = TCHelper.getTrain(sender);

        if (group == null) {
            Localization.COMMAND_NOTRAIN.message(sender);
            return;
        }

        if (radius == null)
            radius = 8;

        Location center = sender.getLocation();
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
            Localization.COMMAND_MOBENTER_SUCCESS.message(sender,
                    Placeholder.set("amount", String.valueOf(entered)));
        else
            Localization.COMMAND_MOBENTER_FAILED.message(sender,
                    Placeholder.set("radius", String.valueOf(radius)));
    }

    @CommandMethod(value="${command.mobeject}", requiredSender=Player.class)
    @CommandDescription("Lässt alle Tiere aus dem ausgewählten Zug aussteigen.")
    @CommandPermission("craftbahn.command.mobeject")
    public void mobeject(
            final Player sender
    ) {
        MinecartGroup group = TCHelper.getTrain(sender);

        if (group == null) {
            Localization.COMMAND_NOTRAIN.message(sender);
            return;
        }

        for (MinecartMember<?> member : group) {
            if (!member.getEntity().hasPassenger()) continue;

            for (Entity passenger : member.getEntity().getPassengers()) {
                if (EntityUtil.isMob(passenger))
                    member.eject();
            }
        }

        Localization.COMMAND_MOBEJECT_SUCCESS.message(sender);
    }

    public static CloudSimpleHandler getCloud() {
        return cloud;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public CommandManager<?> getManager() {
        return cloud.getManager();
    }
}