package de.crafttogether.tcdestinations.listener;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import de.crafttogether.TCDestinations;
import de.crafttogether.common.dep.net.kyori.adventure.text.Component;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.util.PluginUtil;
import de.crafttogether.tcdestinations.util.Util;
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
        for (String tag : enterMessages.getValues().keySet()) {
            Util.debug("found message: " + tag);
            if (!member.getProperties().getTags().contains(tag))
                continue;

            List<Placeholder> resolvers = new ArrayList<>();
            resolvers.add(Placeholder.set("train", member.getGroup().getProperties().getTrainName()));

            String message = (String) enterMessages.get(tag);
            for (Placeholder resolver : resolvers)
                message = resolver.resolve(message);

            Component messageComponent = plugin.getLocalizationManager().miniMessage().deserialize(message);
            PluginUtil.adventure().player(player).sendMessage(messageComponent);
        }
    }
}
