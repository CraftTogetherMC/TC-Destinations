package de.crafttogether.tcdestinations.listener;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import de.crafttogether.TCDestinations;
import de.crafttogether.tcdestinations.localization.LocalizationManager;
import de.crafttogether.tcdestinations.localization.PlaceholderResolver;
import de.crafttogether.tcdestinations.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TrainEnterListener implements Listener {
    private TCDestinations plugin = TCDestinations.plugin;

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player player))
            return;

        MinecartMember<?> member = MinecartMemberStore.getFromEntity(event.getVehicle());
        if (member == null)
            return;

        ConfigurationNode enterMessages = plugin.getEnterMessages().getNode("enterMessages");
        for (String tag : enterMessages.getValues().keySet()) {
            Util.debug("found message: " + tag);
            if (!member.getProperties().getTags().contains(tag))
                continue;

            List<PlaceholderResolver> resolvers = new ArrayList<>();
            resolvers.add(PlaceholderResolver.resolver("train", member.getGroup().getProperties().getTrainName()));
            resolvers.addAll(LocalizationManager.getGlobalPlaceholders());

            String message = (String) enterMessages.get(tag);
            for (PlaceholderResolver resolver : resolvers)
                message = resolver.resolve(message);

            Component messageComponent = TCDestinations.plugin.getMiniMessageParser().deserialize(message);
            plugin.adventure().player(player).sendMessage(messageComponent);
        }
    }
}
