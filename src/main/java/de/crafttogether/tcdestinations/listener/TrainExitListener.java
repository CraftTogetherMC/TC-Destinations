package de.crafttogether.tcdestinations.listener;

import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;

import de.crafttogether.TCDestinations;
import de.crafttogether.common.util.AudienceUtil;
import de.crafttogether.tcdestinations.speedometer.Speedometer;
import de.crafttogether.tcdestinations.util.TCHelper;

import de.crafttogether.common.shaded.net.kyori.adventure.text.Component;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class TrainExitListener implements Listener {

    @EventHandler
    public void onVehicleExit(VehicleExitEvent e) {
        Speedometer speedometer = TCDestinations.plugin.getSpeedometer();

        if (!(e.getExited() instanceof Player player) || speedometer == null)
            return;

        MinecartMember<?> member = MinecartMemberStore.getFromEntity(e.getVehicle());
        if (member == null)
            return;

        // Check if train has no more passengers
        if (TCHelper.getPlayerPassengers(member.getGroup()).size() <= 1) {
            // Remove Speedometer if activated
            speedometer.remove(member.getGroup().getProperties().getTrainName());
        }

        // Clear ActionBar
        AudienceUtil.Bukkit.audiences.player(player).sendActionBar(Component.empty());
    }
}