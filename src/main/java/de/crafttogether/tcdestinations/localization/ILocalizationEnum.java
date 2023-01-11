package de.crafttogether.tcdestinations.localization;

/*
  Copyright (C) 2013-2022 bergerkiller
 */

import com.bergerkiller.bukkit.common.utils.LogicUtil;
import de.crafttogether.TCDestinations;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

/**
 * Interface for a LocalizationEnum. Can be implemented by
 * an actual enum to provide localization constants and defaults.
 */
public interface ILocalizationEnum extends ILocalizationDefault {
    /**
     * Sends this Localization message to the sender specified
     *
     * @param sender to send to
     * @param arguments for the node
     */
    default void message(CommandSender sender, PlaceholderResolver... arguments) {
        TCDestinations.plugin.adventure().sender(sender).sendMessage(deserialize(arguments));
    }

    /**
     * Returns the deserialized Localization message to the sender specified
     *
     * @param resolvers for the node
     */
    default Component deserialize(List<PlaceholderResolver> resolvers) {
        String text = get();

        if (LogicUtil.nullOrEmpty(text))
            return null;

        resolvers.addAll(LocalizationManager.getGlobalPlaceholders());

        for (PlaceholderResolver resolver : resolvers)
            text = resolver.resolve(text);

        return TCDestinations.plugin.getMiniMessageParser().deserialize(text);
    }

    /**
     * Returns the deserialized Localization message to the sender specified
     *
     * @param resolvers for the node
     */
    default Component deserialize(PlaceholderResolver... resolvers) {
        return deserialize(Arrays.stream(resolvers).toList());
    }

    /**
     * Gets the locale value for this Localization node
     *
     * @return Locale value
     */
    String get();
}
