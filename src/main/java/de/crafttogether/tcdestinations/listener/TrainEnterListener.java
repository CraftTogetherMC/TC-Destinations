package de.crafttogether.tcdestinations.listener;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import de.crafttogether.TCDestinations;
import de.crafttogether.common.dep.net.kyori.adventure.text.Component;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.util.PluginUtil;
import de.crafttogether.tcdestinations.util.TCHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import java.util.ArrayList;
import java.util.List;

public class TrainEnterListener implements Listener {
    private final TCDestinations plugin = TCDestinations.plugin;

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player player))
            return;

        MinecartMember<?> member = MinecartMemberStore.getFromEntity(event.getVehicle());
        if (member == null)
            return;

        ConfigurationNode enterMessages = plugin.getEnterMessages().getNode("enterMessages");

        for (String tag : member.getProperties().getTags()) {
            if (!enterMessages.getValues().containsKey(tag))
                continue;

            if (enterMessages.getValues().containsKey(tag + "_with_destination") && member.getProperties().hasDestination())
                tag = tag + "_with_destination";

            List<String> destinationRoute = member.getGroup().getProperties().getDestinationRoute();
            String current_destination = member.getGroup().getProperties().getDestination();
            String finalDestination = destinationRoute.isEmpty() ? current_destination : destinationRoute.get(destinationRoute.size() -1);

            List<Placeholder> resolvers = new ArrayList<>(plugin.getLocalizationManager().getPlaceholders());
            resolvers.add(Placeholder.set("train_name", member.getGroup().getProperties().getTrainName()));
            resolvers.add(Placeholder.set("display_name", member.getGroup().getProperties().getDisplayNameOrEmpty()));
            resolvers.add(Placeholder.set("speed_limit", member.getGroup().getProperties().getSpeedLimit()));
            resolvers.add(Placeholder.set("route", TCHelper.stringifyRoute(destinationRoute)));
            resolvers.add(Placeholder.set("current_destination", member.getGroup().getProperties().getDestination()));
            resolvers.add(Placeholder.set("next_destination", member.getGroup().getProperties().getNextDestinationOnRoute()));
            resolvers.add(Placeholder.set("final_destination", finalDestination));

            String message = (String) enterMessages.get(tag);
            for (Placeholder resolver : resolvers)
                message = resolver.resolve(message);

            Component messageComponent = plugin.getLocalizationManager().miniMessage().deserialize(message);
            PluginUtil.adventure().player(player).sendMessage(messageComponent);
        }
    }
}
