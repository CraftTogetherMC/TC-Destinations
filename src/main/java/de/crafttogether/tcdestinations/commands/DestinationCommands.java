package de.crafttogether.tcdestinations.commands;

import de.crafttogether.common.platform.bukkit.util.BukkitNetworkLocationAdapter;
import de.crafttogether.common.util.AudienceUtil;
import org.incendo.cloud.annotations.*;
import com.bergerkiller.bukkit.common.utils.LogicUtil;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import net.kyori.adventure.text.Component;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.TCDestinations;
import de.crafttogether.tcdestinations.Localization;
import de.crafttogether.tcdestinations.destinations.Destination;
import de.crafttogether.tcdestinations.destinations.DestinationList;
import de.crafttogether.tcdestinations.destinations.DestinationType;
import de.crafttogether.tcdestinations.util.DynmapMarker;
import de.crafttogether.tcdestinations.util.TCHelper;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public class DestinationCommands {
    private final TCDestinations plugin = TCDestinations.plugin;

    public DestinationCommands() { }

    @Command(value="${command.destination}", requiredSender=Player.class)
    @CommandDescription("Informationen zum /fahrziel Befehl")
    @Permission("craftbahn.command.destination")
    public void fahrziel_info(
            final Player sender
    ) {
        Localization.COMMAND_DESTINATION_INFO.message(sender.getUniqueId());
    }

    @Command(value="${command.destination} <destination> [server]", requiredSender=Player.class)
    @CommandDescription("Setzt dem aktuell ausgewähltem Zug ein Fahrziel")
    @Permission("craftbahn.command.destination")
    public void fahrziel(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="server", suggestions="serverName") String server,
            final @Flag(value="page") Integer page
    ) {
        ArrayList<Destination> result = new ArrayList<>();
        if (server == null || server.isEmpty())
            result = new ArrayList<>(plugin.getDestinationStorage().getDestinations(name));
        else
            result.add(plugin.getDestinationStorage().getDestination(name, server));

        // No destination was found
        if (result.size() < 1 || result.get(0) == null) {
            Localization.COMMAND_DESTINATION_NOTEXIST.message(sender.getUniqueId(),
                Placeholder.set("input", name)
            );
        }

        // Multiple destinations have been found
        else if (result.size() > 1) {
            DestinationList list = new DestinationList(result);
            list.setCommand("/" + plugin.getCommandManager().getConfig().get("commands.destination"));
            list.setRowsPerPage(12);
            list.showContentsPage(false);
            list.showFooterLine(true);
            list.showOwner(true);
            list.showLocation(true);

            if (!LogicUtil.nullOrEmpty(Localization.HEADER.get()))
                AudienceUtil.getPlayer(sender.getUniqueId()).sendMessage(Localization.HEADER.deserialize().append(Component.newline()));

            if (page == null) {
                Localization.COMMAND_DESTINATION_MULTIPLEDEST.message(sender.getUniqueId());
            }

            list.sendPage(sender, (page == null ? 1 : page));
        }

        // A single destination was found
        else {
            Destination destination = result.get(0);

            // Check permission and if destination is public
            if (!destination.isPublic() && !sender.hasPermission("craftbahn.destination.see.private")) {
                Localization.COMMAND_DESTINATION_NOPERMISSION.message(sender.getUniqueId());
                return;
            }

            // Find train
            MinecartGroup train = TCHelper.getTrain(sender);
            if (train == null) {
                Localization.COMMAND_NOTRAIN.message(sender.getUniqueId());
                return;
            }

            // Apply destination
            train.getProperties().setDestination("train destination " + destination.getServer());

            if (!TCDestinations.plugin.getServerName().equalsIgnoreCase(destination.getServer())) {
                // Create a route first
                List<String> route = new ArrayList<>();
                route.add(destination.getServer());
                route.add(destination.getName());

                train.getProperties().setDestinationRoute(route);
                train.getProperties().setDestination(destination.getServer());
            } else {
                train.getProperties().setDestination(destination.getName());
            }

            Localization.COMMAND_DESTINATION_APPLIED.message(sender.getUniqueId(),
                Placeholder.set("destination", destination.getName())
            );
        }
    }

    @Command(value="${command.destinations} [type]", requiredSender=Player.class)
    @CommandDescription("Zeigt eine Liste mit möglichen Fahrzielen")
    @Permission("craftbahn.command.destination")
    public void fahrziele(
            final Player sender,
            final @Argument(value="type", suggestions="destinationType") String type,
            final @Flag(value="player", suggestions="onlinePlayers", permission = "craftbahn.command.destination.filter") String player,
            final @Flag(value="server", suggestions="serverName", permission = "craftbahn.command.destination.filter") String server,
            final @Flag(value="page") Integer page
    ) {
        List<Destination> result = new ArrayList<>(TCDestinations.plugin.getDestinationStorage().getDestinations());
        String commandFlags;
        boolean showContentsPage = true;

        // Filter: destinationType
        if (type != null && !type.isEmpty() && !type.equalsIgnoreCase(plugin.getConfig().getString("DestinationTypeAll.DisplayName"))) {
            commandFlags = " " + type;
            showContentsPage = false;
            result = result.stream()
                    .filter(d -> d.getType().getDisplayName().equalsIgnoreCase(type))
                    .toList();
        }
        else
            commandFlags = " " + plugin.getConfig().getString("DestinationTypeAll.DisplayName");

        if (type != null && type.equalsIgnoreCase(plugin.getConfig().getString("DestinationTypeAll.DisplayName")))
            showContentsPage = false;

        // Filter: player
        if (player != null && !player.isEmpty()) {
            OfflinePlayer targetPlayer = resolveOfflinePlayer(player);
            if (targetPlayer == null) {
                Localization.COMMAND_DESTEDIT_UNKOWNPLAYER.message(sender.getUniqueId(),
                        Placeholder.set("input", player));
                return;
            }

            UUID targetUuid = targetPlayer.getUniqueId();

            result = result.stream()
                    .filter(d -> d.getOwner().equals(targetUuid) || d.getParticipants().contains(targetUuid))
                    .toList();
        }

        // Filter: server
        if (server != null && !server.isEmpty()) {
            commandFlags += " --server " + server;
            showContentsPage = false;
            result = result.stream()
                    .filter(d -> d.getServer().equalsIgnoreCase(server))
                    .toList();
        }

        if (result.size() < 1) {
            Localization.COMMAND_DESTINATIONS_LIST_EMPTY.message(sender.getUniqueId());
            return;
        }

        DestinationList list = new DestinationList(result);
        list.setCommand("/" + plugin.getCommandManager().getConfig().get("commands.destinations"));
        list.setCommandFlags(commandFlags);
        list.showContentsPage(showContentsPage);
        list.showFooterLine(true);
        list.showOwner(true);
        list.showLocation(true);

        list.sendPage(sender, (page == null ? 1 : page));
    }

    @Command(value="${command.destedit} info <destination> [server]", requiredSender=Player.class)
    @CommandDescription("Zeigt Informationen zum angegebenen Fahrziel an")
    @Permission("craftbahn.command.destedit.info")
    public void fahrzieledit_info(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="server", suggestions="serverName") String server,
            final @Flag(value="page") Integer page
    ) {
        Destination destination = findDestination(sender, name, server);
        if (destination == null) {
            return;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(destination.getOwner());
        String unkown = Localization.COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_OWNERUNKOWN.get();
        String ownerName = (owner.hasPlayedBefore() ? owner.getName() : unkown);

        StringBuilder participants = new StringBuilder();
        for (UUID uuid : destination.getParticipants()) {
            OfflinePlayer participant = Bukkit.getOfflinePlayer(uuid);
            if (!participant.hasPlayedBefore()) continue;
            participants.append(participant.getName()).append(", ");
        }

        Localization.COMMAND_DESTEDIT_INFO.message(sender.getUniqueId(),
                Placeholder.set("name", destination.getName()),
                Placeholder.set("id", destination.getId().toString()),
                Placeholder.set("type", destination.getType().toString()),
                Placeholder.set("owner", ownerName),
                Placeholder.set("participants", participants.isEmpty() ? "" : participants.substring(0, participants.length()-2)),
                Placeholder.set("server", destination.getServer()),
                Placeholder.set("world", destination.getWorld()),
                Placeholder.set("x", destination.getLocation().getX()),
                Placeholder.set("y", destination.getLocation().getY()),
                Placeholder.set("z", destination.getLocation().getZ()));
    }

    @Command(value="${command.destedit} tp <destination> [server]", requiredSender=Player.class)
    @CommandDescription("Teleportiert den Spieler zum angegebenen Fahrziel")
    @Permission("craftbahn.command.destedit.teleport")
    public void fahrzieledit_teleport(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        Destination destination = findDestination(sender, name, server);
        if (destination == null) {
            return;
        }
        if (!destination.getServer().equalsIgnoreCase(plugin.getServerName())) {
            Localization.COMMAND_DESTEDIT_TELEPORT_OTHERSERVER.message(sender.getUniqueId(),
                    Placeholder.set("server", destination.getServer()));
            return;
        }

        // Teleport player
        sender.teleport(BukkitNetworkLocationAdapter.toBukkitLocation(destination.getTeleportLocation()));

        Localization.COMMAND_DESTEDIT_TELEPORT.message(sender.getUniqueId(),
                Placeholder.set("destination", destination.getName()));
    }

    @Command(value="${command.destedit} add <destination> <type>", requiredSender=Player.class)
    @CommandDescription("Fügt ein neues Fahrziel der Liste hinzu")
    @Permission("craftbahn.command.destedit.add")
    public void fahrzieledit_add(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="type", suggestions="destinationType") String type
    ) {
        DestinationType destinationType = DestinationType.getFromName(type);

        if (destinationType == null)
            destinationType = DestinationType.getFromDisplayName(type);

        if (destinationType == null) {
            Localization.COMMAND_DESTEDIT_ADD_INVALIDTYPE.message(sender.getUniqueId());
            return;
        }

        plugin.getDestinationStorage().addDestination(name, sender.getUniqueId(), destinationType, sender.getLocation(), true, (err, destination) -> {
            if (err != null)
                Localization.COMMAND_DESTEDIT_SAVEFAILED.message(sender.getUniqueId(),
                        Placeholder.set("error", err.getMessage()));
            else {
                Localization.COMMAND_DESTEDIT_ADD_SUCCESS.message(sender.getUniqueId(),
                        Placeholder.set("destination", destination.getName()),
                        Placeholder.set("id", String.valueOf(destination.getId())));

                DynmapMarker.addMarker(destination);
            }
        });
    }

    @Command(value="${command.destedit} remove <destination> [server]", requiredSender=Player.class)
    @CommandDescription("Entfernt das angegebene Fahrziel aus der Liste")
    @Permission("craftbahn.command.destedit.remove")
    public void fahrzieledit_remove(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        Destination destination = findDestination(sender, name, server);
        if (destination == null) {
            return;
        }

        plugin.getDestinationStorage().delete(destination.getId(), (err, affectedRows) -> {
            if (err != null) {
                Localization.COMMAND_DESTEDIT_SAVEFAILED.message(sender.getUniqueId(),
                        Placeholder.set("error", err.getMessage()));
                return;
            }
                DynmapMarker.deleteMarker(destination);
                Localization.COMMAND_DESTEDIT_REMOVE.message(sender.getUniqueId(),
                        Placeholder.set("destination", destination.getName()));
        });
    }

    @Command(value="${command.destedit} addmember <destination> <player> [server]", requiredSender=Player.class)
    @CommandDescription("Fügt dem angegebene Fahrziel einen sekundären Besitzer hinzu")
    @Permission("craftbahn.command.destedit.addmember")
    public void fahrzieledit_addmember(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="player", suggestions="onlinePlayers") String player,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        OfflinePlayer participant = resolveOfflinePlayer(player);
        if (!participant.hasPlayedBefore()) {
            Localization.COMMAND_DESTEDIT_UNKOWNPLAYER.message(sender.getUniqueId(),
                    Placeholder.set("input", player));
            return;
        }

        Destination destination = findDestination(sender, name, server);
        if (destination == null) {
            return;
        }
        if (destination.getParticipants().contains(participant.getUniqueId())) {
            Localization.COMMAND_DESTEDIT_ADDMEMBER_FAILED.message(sender.getUniqueId(),
                    Placeholder.set("input", player));
            return;
        }

        destination.addParticipant(participant.getUniqueId());

        plugin.getDestinationStorage().update(destination, (err, affectedRows) -> {
            if (err != null)
                Localization.COMMAND_DESTEDIT_SAVEFAILED.message(sender.getUniqueId(),
                        Placeholder.set("error", err.getMessage()));
            else
                Localization.COMMAND_DESTEDIT_ADDMEMBER_SUCCESS.message(sender.getUniqueId(),
                        Placeholder.set("destination", destination.getName()),
                        Placeholder.set("player", participant.getName()));
        });
    }

    @Command(value="${command.destedit} removemember <destination> <player> [server]", requiredSender=Player.class)
    @CommandDescription("Entfernt dem angegebene Fahrziel einen sekundären Besitzer")
    @Permission("craftbahn.command.destedit.removemember")
    public void fahrzieledit_removemember(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="player", suggestions="onlinePlayers") String player,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        OfflinePlayer participant = resolveOfflinePlayer(player);
        if (!participant.hasPlayedBefore()) {
            Localization.COMMAND_DESTEDIT_UNKOWNPLAYER.message(sender.getUniqueId(),
                    Placeholder.set("input", player));
            return;
        }

        Destination destination = findDestination(sender, name, server);
        if (destination == null) {
            return;
        }
        if (!destination.getParticipants().contains(participant.getUniqueId())) {
            Localization.COMMAND_DESTEDIT_REMOVEMEMBER_FAILED.message(sender.getUniqueId(),
                    Placeholder.set("input", player));
            return;
        }

        destination.removeParticipant(participant.getUniqueId());

        plugin.getDestinationStorage().update(destination, (err, affectedRows) -> {
            if (err != null)
                Localization.COMMAND_DESTEDIT_SAVEFAILED.message(sender.getUniqueId(),
                        Placeholder.set("error", err.getMessage()));
            else
                Localization.COMMAND_DESTEDIT_REMOVEMEMBER_SUCCESS.message(sender.getUniqueId(),
                        Placeholder.set("destination", destination.getName()),
                        Placeholder.set("player", participant.getName()));
        });
    }

    @Command(value="${command.destedit} setowner <destination> <player> [server]", requiredSender=Player.class)
    @CommandDescription("Legt den primären Besitzer des angegebenen Fahrziel fest")
    @Permission("craftbahn.command.destedit.setowner")
    public void fahrzieledit_setowner(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="player", suggestions="onlinePlayers") String player,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        OfflinePlayer owner = resolveOfflinePlayer(player);
        if (!owner.hasPlayedBefore()) {
            Localization.COMMAND_DESTEDIT_UNKOWNPLAYER.message(sender.getUniqueId(),
                    Placeholder.set("input", player));
            return;
        }

        Destination destination = findDestination(sender, name, server);
        if (destination == null) {
            return;
        }
        destination.setOwner(owner.getUniqueId());

        plugin.getDestinationStorage().update(destination, (err, affectedRows) -> {
            if (err != null)
                Localization.COMMAND_DESTEDIT_SAVEFAILED.message(sender.getUniqueId(),
                        Placeholder.set("error", err.getMessage()));
            else
                Localization.COMMAND_DESTEDIT_SETOWNER_SUCCESS.message(sender.getUniqueId(),
                        Placeholder.set("destination", destination.getName()),
                        Placeholder.set("owner", owner.getName()));
        });
    }

    @Command(value="${command.destedit} setpublic <destination> [server]", requiredSender=Player.class)
    @CommandDescription("Macht das angegebene Fahrziel öffentlich")
    @Permission("craftbahn.command.destedit.setpublic")
    public void fahrzieledit_setpublic(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        Destination destination = findDestination(sender, name, server);
        if (destination == null) {
            return;
        }
        destination.setPublic(true);

        plugin.getDestinationStorage().update(destination, (err, affectedRows) -> {
            if (err != null)
                Localization.COMMAND_DESTEDIT_SAVEFAILED.message(sender.getUniqueId(),
                        Placeholder.set("error", err.getMessage()));
            else {
                Localization.COMMAND_DESTEDIT_SETPUBLIC_SUCCESS.message(sender.getUniqueId(),
                        Placeholder.set("destination", destination.getName()));
            }
        });
    }

    @Command(value="${command.destedit} setprivate <destination> [server]", requiredSender=Player.class)
    @CommandDescription("Macht das angegebene Fahrziel privat")
    @Permission("craftbahn.command.destedit.setprivate")
    public void fahrzieledit_setprivate(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        Destination destination = findDestination(sender, name, server);
        if (destination == null) {
            return;
        }
        destination.setPublic(false);

        plugin.getDestinationStorage().update(destination, (err, affectedRows) -> {
            if (err != null)
                Localization.COMMAND_DESTEDIT_SAVEFAILED.message(sender.getUniqueId(),
                        Placeholder.set("error", err.getMessage()));
            else {
                Localization.COMMAND_DESTEDIT_SETPRIVATE_SUCCESS.message(sender.getUniqueId(),
                        Placeholder.set("destination", destination.getName()));
            }
        });
    }

    @Command(value="${command.destedit} setlocation <destination> [server]", requiredSender=Player.class)
    @CommandDescription("Legt die Marker-Position (Dynmap) des aktuellen Fahrziel fest")
    @Permission("craftbahn.command.destedit.setlocation")
    public void fahrzieledit_setlocation(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        Destination destination = findDestination(sender, name, server);
        if (destination == null) {
            return;
        }
        destination.setLocation(BukkitNetworkLocationAdapter.fromBukkitLocation(sender.getLocation(), plugin.getServerName()));

        plugin.getDestinationStorage().update(destination, (err, affectedRows) -> {
            if (err != null)
                Localization.COMMAND_DESTEDIT_SAVEFAILED.message(sender.getUniqueId(),
                        Placeholder.set("error", err.getMessage()));
            else {
                Localization.COMMAND_DESTEDIT_SETLOCATION_SUCCESS.message(sender.getUniqueId(),
                        Placeholder.set("destination", destination.getName()));
            }
        });
    }

    @Command(value="${command.destedit} settype <destination> <type> [server]", requiredSender=Player.class)
    @CommandDescription("Legt den Typ des angegebenen Fahrziel fest")
    @Permission("craftbahn.command.destedit.settype")
    public void fahrzieledit_settype(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="type", suggestions="destinationType") String type,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        DestinationType destinationType = DestinationType.getFromName(type);

        if (destinationType == null)
            destinationType = DestinationType.getFromDisplayName(type);

        if (destinationType == null) {
            Localization.COMMAND_DESTEDIT_ADD_INVALIDTYPE.message(sender.getUniqueId());
            return;
        }

        Destination destination = findDestination(sender, name, server);
        if (destination == null) {
            return;
        }
        destination.setType(destinationType);

        DestinationType finalDestinationType = destinationType;
        plugin.getDestinationStorage().update(destination, (err, affectedRows) -> {
            if (err != null)
                Localization.COMMAND_DESTEDIT_SAVEFAILED.message(sender.getUniqueId(),
                        Placeholder.set("error", err.getMessage()));
            else {
                Localization.COMMAND_DESTEDIT_SETTYPE_SUCCESS.message(sender.getUniqueId(),
                        Placeholder.set("destination", destination.getName()),
                        Placeholder.set("type", finalDestinationType.getDisplayName()));
            }
        });
    }

    @Command(value="${command.destedit} setwarp <destination> [server]", requiredSender=Player.class)
    @CommandDescription("Legt die Warp-Position des aktuellen Fahrziel fest")
    @Permission("craftbahn.command.destedit.setwarp")
    public void fahrzieledit_setwarp(
            final Player sender,
            final @Argument(value="destination", suggestions="destinationName") String name,
            final @Argument(value="server", suggestions="serverName") String server
    ) {
        Destination destination = findDestination(sender, name, server);
        if (destination == null) {
            return;
        }
        destination.setTeleportLocation(BukkitNetworkLocationAdapter.fromBukkitLocation(sender.getLocation(), plugin.getServerName()));

        plugin.getDestinationStorage().update(destination, (err, affectedRows) -> {
            if (err != null)
                Localization.COMMAND_DESTEDIT_SAVEFAILED.message(sender.getUniqueId(),
                        Placeholder.set("error", err.getMessage()));
            else {
                Localization.COMMAND_DESTEDIT_SETWARP_SUCCESS.message(sender.getUniqueId(),
                        Placeholder.set("destination", destination.getName()));
            }
        });
    }

    @Command(value="${command.destedit} updatemarker", requiredSender=Player.class)
    @CommandDescription("Alle Dynmap-Marker werden neu geladen")
    @Permission("craftbahn.command.destedit.updatemarker")
    public void fahrzieledit_updatemarker(
            final Player sender
    ) {
        if (plugin.getDynmap() == null) {
            Localization.DYNMAP_NOTINSTALLED.message(sender.getUniqueId());
            return;
        }

        int markersCreated = DynmapMarker.setupMarkers(TCDestinations.plugin.getDestinationStorage().getDestinations());
        Localization.COMMAND_DESTEDIT_UPDATEMARKER_SUCCESS.message(sender.getUniqueId(),
                Placeholder.set("amount", String.valueOf(markersCreated)));
    }

    @Command(value="${command.destedit} reload", requiredSender=Player.class)
    @CommandDescription("Konfiguration wird neu geladen")
    @Permission("craftbahn.command.destedit.reload")
    public void fahrzieledit_reload(
            final Player sender
    ) {
        sender.sendMessage("Coming soon");
    }

    private Destination findDestination(Player sender, String name, String server) {
        List<Destination> result = plugin.getDestinationStorage().getDestinations().stream()
                .filter(d -> d.getName().equalsIgnoreCase(name))
                .filter(d -> server == null || server.isBlank() || d.getServer().equalsIgnoreCase(server))
                .toList();

        if (result.isEmpty()) {
            Localization.COMMAND_DESTINATION_NOTEXIST.message(sender.getUniqueId(),
                    Placeholder.set("input", name));
            return null;
        }

        if (result.size() > 1) {
            Localization.COMMAND_DESTEDIT_MULTIPLEDEST.message(sender.getUniqueId(),
                    Placeholder.set("input", name));
            return null;
        }

        return result.get(0);
    }


        private OfflinePlayer resolveOfflinePlayer(String input) {
            if (input == null || input.isBlank()) {
                return null;
            }

            Player online = Bukkit.getPlayerExact(input);
            if (online != null) {
                return online;
            }

            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                String name = offlinePlayer.getName();
                if (name != null && name.equalsIgnoreCase(input)) {
                    return offlinePlayer;
                }
            }

            return null;
        }
}
