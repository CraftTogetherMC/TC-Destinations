package de.crafttogether.tcdestinations.util;

import de.crafttogether.TCDestinations;
import de.crafttogether.tcdestinations.Localization;
import de.crafttogether.tcdestinations.destinations.Destination;
import de.crafttogether.tcdestinations.localization.LocalizationManager;
import de.crafttogether.tcdestinations.localization.PlaceholderResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class DynmapMarker {
    private static final TCDestinations plugin = TCDestinations.plugin;

    public static int setupMarkers(Collection<Destination> destinations) {
        if (plugin.getDynmap() == null)
            return 0;

        plugin.getLogger().info("Setup Markers...");

        int markersCreated = 0;
        for (Destination destination : destinations) {
            if (!plugin.getServerName().equalsIgnoreCase(destination.getServer())) continue;
            if(addMarker(destination)) markersCreated++;
        }

        plugin.getLogger().info("Created " + markersCreated + " markers.");
        plugin.getLogger().info("Marker-Setup completed.");
        return markersCreated;
    }

    public static void deleteMarker(Destination dest) {
        if (plugin.getDynmap() == null)
            return;

        if (!dest.getServer().equalsIgnoreCase(plugin.getServerName()))
            return;

        MarkerSet set = plugin.getDynmap().getMarkerAPI().getMarkerSet("TC_" + dest.getType().getDisplayName());
        if (set == null)
            return;

        Marker marker = set.findMarker(dest.getName());
        if (marker != null)
            marker.deleteMarker();
    }

    public static boolean addMarker(Destination destination) {
        if (plugin.getDynmap() == null)
            return false;

        if (!destination.getServer().equalsIgnoreCase(plugin.getServerName()))
            return false;

        DynmapAPI dynmap = plugin.getDynmap();

        if (dynmap == null)
            return false;

        MarkerAPI markers = dynmap.getMarkerAPI();
        MarkerSet set = markers.getMarkerSet("TC_" + destination.getType().getName());

        // Create MarkerSet if not exists
        if (set == null)
            set = dynmap.getMarkerAPI().createMarkerSet("TC_" + destination.getType().getName(), destination.getType().getDisplayName(), null, true);

        // Delete Marker if already exists
        Marker marker = set.findMarker(destination.getName());
        if (marker != null)
            marker.deleteMarker();

        if (Bukkit.getServer().getWorld(destination.getLocation().getWorld()) == null) {
            plugin.getLogger().warning("Error: Unable to create marker for '" + destination.getName() + "'. World '" + destination.getWorld() + "' is not loaded");
            return false;
        }

        if (destination.getLocation() == null) {
            plugin.getLogger().warning("Error: Destination '" + destination.getName() + "' has no location set!");
            return false;
        }

        // Load icon
        MarkerIcon markerIcon = getIcon(markers, destination);

        if (markerIcon == null) {
            plugin.getLogger().warning("Error: Unable to create marker for  '" + destination.getName() + "'. File '" + destination.getType().getIcon() + "' could not be loaded");
            return false;
        }

        // List owners & participants
        String owner;
        StringBuilder participants = new StringBuilder(Bukkit.getOfflinePlayer(destination.getOwner()).getName() + ", ");
        for (UUID uuid : destination.getParticipants()) {
            OfflinePlayer participant = Bukkit.getOfflinePlayer(uuid);
            if (!participant.hasPlayedBefore()) continue;
            participants.append(participant.getName()).append(", ");
        }
        owner = participants.isEmpty() ? "" : participants.substring(0, participants.length() - 2);

        String markerHTML = Localization.DYNMAP_MARKER.get();
        List<PlaceholderResolver> resolvers = new ArrayList<>();

        resolvers.add(PlaceholderResolver.resolver("id", String.valueOf(destination.getId())));
        resolvers.add(PlaceholderResolver.resolver("name", destination.getName()));
        resolvers.add(PlaceholderResolver.resolver("type", destination.getType().getDisplayName()));
        resolvers.add(PlaceholderResolver.resolver("owner", owner));
        resolvers.add(PlaceholderResolver.resolver("displayOwner", destination.getType().showOwnerInformations() ? "inline" : "none"));
        resolvers.add(PlaceholderResolver.resolver("color", destination.getType().getDisplayNameColor().asHexString()));
        resolvers.add(PlaceholderResolver.resolver("world", destination.getWorld()));
        resolvers.add(PlaceholderResolver.resolver("server", destination.getServer()));
        resolvers.addAll(LocalizationManager.getGlobalPlaceholders());

        for (PlaceholderResolver resolver : resolvers)
            markerHTML = resolver.parse(markerHTML);

        Location location = destination.getLocation().getBukkitLocation();
        set.createMarker(destination.getName(), markerHTML, true, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), markerIcon, false);
        return true;
    }

    public static MarkerIcon getIcon(MarkerAPI markers, Destination destination) {
        MarkerIcon markerIcon;
        InputStream iconFile;

        try {
            iconFile = new FileInputStream(plugin.getDataFolder() + File.separator + destination.getType().getIcon());
        } catch (FileNotFoundException e) {
            plugin.getLogger().info(e.getMessage());
            return null;
        }

            markerIcon = markers.getMarkerIcon(destination.getType().getName());
            if (markerIcon == null)
                markerIcon = markers.createMarkerIcon(destination.getType().getName(), destination.getType().getDisplayName(), iconFile);

        return markerIcon;
    }
}