package de.crafttogether.tcdestinations.destinations;

import de.crafttogether.common.dep.net.kyori.adventure.text.format.NamedTextColor;
import de.crafttogether.common.dep.net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

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

    public @Nullable TextColor getDisplayNameColor() {
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

    @SuppressWarnings("unchecked")
    public static void registerTypes(FileConfiguration config) {
        List<?> typesList = config.getList("DestinationTypes");

        for (Object object : Objects.requireNonNull(typesList)) {
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