package de.crafttogether.tcdestinations.util;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import com.bergerkiller.bukkit.tc.properties.TrainPropertiesStore;
import de.crafttogether.common.util.AudienceUtil;

import de.crafttogether.common.shaded.net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TCHelper {
    public static String stringifyRoute(List<String> route) {
        StringBuilder result = new StringBuilder();
        for(String destination : route) {
            result.append(destination);
            result.append(" -> ");
        }
        return !result.isEmpty() ? result.substring(0, result.length() - 4): "";
    }

    public static MinecartGroup getTrain(UUID uuid) {
        Player p = Bukkit.getServer().getPlayer(uuid);

        if (p == null)
            return null;

        Entity entity = p.getVehicle();
        MinecartMember<?> member = null;

        if (entity == null)
            return null;

        if (entity instanceof Minecart)
            member = MinecartMemberStore.getFromEntity(entity);

        if (member != null)
            return member.getGroup();

        return null;
    }

    public static MinecartGroup getTrain(String trainName) {
        TrainProperties properties = TrainPropertiesStore.get(trainName);
        return (properties != null && properties.hasHolder()) ? properties.getHolder() : null;
    }

    public static BlockFace getDirection(String junctionName) {
        return switch (junctionName) {
            default -> null;
            case "n" -> BlockFace.NORTH;
            case "e" -> BlockFace.EAST;
            case "s" -> BlockFace.SOUTH;
            case "w" -> BlockFace.WEST;
        };
    }

    public static List<Player> getPlayerPassengers(MinecartMember<?> member) {
        List<Player> passengers = new ArrayList<>();
        for (Entity passenger : member.getEntity().getEntity().getPassengers())
            if (passenger instanceof Player) passengers.add((Player) passenger);

        return passengers;
    }

    public static List<Player> getPlayerPassengers(MinecartGroup group) {
        List<Player> passengers = new ArrayList<>();
        for (MinecartMember<?> member : group)
            passengers.addAll(getPlayerPassengers(member));

        return passengers;
    }

    // Send actionbar to all passengers of a train
    public static void sendActionbar(MinecartGroup group, Component message) {
        for (MinecartMember<?> member : group)
            sendActionbar(member, message);
    }

    // Send permission-based actionbar to all passengers of a train
    public static void sendActionbar(MinecartGroup group, String permission, Component message) {
        for (MinecartMember<?> member : group)
            sendActionbar(member, permission, message);
    }

    // Send actionBar to all passengers of a cart
    public static void sendActionbar(MinecartMember<?> member, Component message) {
        for (Object passenger : getPlayerPassengers(member)) {
            if (passenger instanceof Player player)
                AudienceUtil.Bukkit.audiences.player(player).sendActionBar(message);
        }
    }

    // Send permission-based actionbar to all passengers of a cart
    public static void sendActionbar(MinecartMember<?> member, String permission, Component message) {
        for (Object passenger : getPlayerPassengers(member)) {
            if (passenger instanceof Player player && player.hasPermission(permission))
                AudienceUtil.Bukkit.audiences.player(player).sendActionBar(message);
        }
    }
}