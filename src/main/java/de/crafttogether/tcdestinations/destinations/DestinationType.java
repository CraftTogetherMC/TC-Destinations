package de.crafttogether.tcdestinations.destinations;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DestinationType {
    private static final List<DestinationType> types = new ArrayList<>();

    private final String name;
    private final String displayName;
    private final NamedTextColor displayNameColor;
    private final String icon;
    private final boolean showOwnerInformations;

    DestinationType(String name, String displayName, NamedTextColor displayNameColor, String icon, boolean showOwnerInformations) {
        this.name = name;
        this.displayName = displayName;
        this.displayNameColor = displayNameColor;
        this.icon = icon;
        this.showOwnerInformations = showOwnerInformations;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public NamedTextColor getDisplayNameColor() {
        return displayNameColor;
    }

    public boolean showOwnerInformations() {
        return showOwnerInformations;
    }

    public String getIcon() {
        return icon;
    }

    public static List<DestinationType> getTypes() {
        return types;
    }

    public static void registerTypes(FileConfiguration config) {
        List<?> typesList = config.getList("DestinationTypes");

        for (Object object : typesList) {
            LinkedHashMap<String, Object> typeData = (LinkedHashMap<String, Object>) object;

            String name = (String) typeData.get("Name");
            String displayName = (String) typeData.get("DisplayName");
            NamedTextColor displayNameColor = NamedTextColor.NAMES.value((String) typeData.get("DisplayNameColor"));
            String icon = (String) typeData.get("DynmapIcon");
            Boolean showOwnerInformations = (Boolean) typeData.get("ShowOwnerInformations");

            DestinationType type = new DestinationType(name, displayName, displayNameColor, icon, showOwnerInformations);
            types.add(type);
        }
    }

    public static DestinationType getFromName(String name) {
        List<DestinationType> result = DestinationType.getTypes().stream()
                .filter(destinationType -> destinationType.getName().equals(name))
                .toList();
        return result.size() > 0 ? result.get(0) : null;
    }

    public static DestinationType getFromDisplayName(String displayName) {
        List<DestinationType> result = DestinationType.getTypes().stream()
                .filter(destinationType -> destinationType.getDisplayName().equals(displayName))
                .toList();
        return result.size() > 0 ? result.get(0) : null;
    }
}