package de.crafttogether.tcdestinations.util;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import com.bergerkiller.bukkit.tc.properties.TrainPropertiesStore;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

public class TCHelper {

    public static MinecartGroup getTrain(Player p) {
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
}