package de.crafttogether.tcdestinations.speedometer;

import com.bergerkiller.bukkit.common.utils.EntityUtil;
import com.bergerkiller.bukkit.common.utils.PacketUtil;
import com.bergerkiller.bukkit.common.wrappers.ChatText;
import com.bergerkiller.bukkit.common.wrappers.DataWatcher;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.generated.net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLivingHandle;
import com.bergerkiller.generated.net.minecraft.world.entity.EntityHandle;
import de.crafttogether.TCDestinations;
import de.crafttogether.common.dep.net.kyori.adventure.text.Component;
import de.crafttogether.tcdestinations.util.TCHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class Speedometer implements Runnable {
    private List<SpeedData> trains;
    private List<DebugParticle> debugParticles;
    private BukkitTask task;

    public static class DebugParticle {
        public String trainName;
        public Location location;
        public Particle particle;
        public Object data;

        public DebugParticle(String trainName, Location location, Particle particle, Object data) {
            this.trainName = trainName;
            this.location = location;
            this.particle = particle;
            this.data = data;
        }

        public static void createArmorStand(Location location, String name) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("tcdestinations.debug"))continue;

                DataWatcher metadata = new DataWatcher();
                metadata.set(EntityHandle.DATA_NO_GRAVITY, true);
                metadata.set(EntityHandle.DATA_CUSTOM_NAME_VISIBLE, true);
                metadata.set(EntityHandle.DATA_CUSTOM_NAME, ChatText.fromMessage(name));
                metadata.set(EntityHandle.DATA_FLAGS, (byte) EntityHandle.DATA_FLAG_INVISIBLE);

                PacketPlayOutSpawnEntityLivingHandle spawnPacket = PacketPlayOutSpawnEntityLivingHandle.T.newHandleNull();
                spawnPacket.setEntityId(EntityUtil.getUniqueEntityId());
                spawnPacket.setEntityUUID(UUID.randomUUID());
                spawnPacket.setEntityType(EntityType.ARMOR_STAND);
                spawnPacket.setPosX(location.getX());
                spawnPacket.setPosY(location.getY());
                spawnPacket.setPosZ(location.getZ());
                spawnPacket.setMotX(0.0);
                spawnPacket.setMotY(0.0);
                spawnPacket.setMotZ(0.0);
                spawnPacket.setPitch(0.0f);
                spawnPacket.setYaw(0.0f);
                PacketUtil.sendEntityLivingSpawnPacket(player, spawnPacket, metadata);
            }
        }
    }

    public Speedometer() {
        this.trains = new ArrayList<>();
        this.debugParticles = new ArrayList<>();
        this.task = Bukkit.getScheduler().runTaskTimer(TCDestinations.plugin, this, 20L, 5L);
    }

    @Override
    public void run() {
        if (debugParticles.isEmpty())
            return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("tcdestinations.debug")) continue;

            for (DebugParticle particle : debugParticles) {
                if (TCHelper.getTrain(particle.trainName) == null)
                    debugParticles.remove(particle);

                else if (particle.location.getChunk().isLoaded())
                    p.spawnParticle(particle.particle, particle.location, 1, particle.data);
            }
        }

        if (trains.isEmpty())
            return;

        updateData();
        sendActionBars();
    }

    public void stop() {
        if (this.task != null)
            this.task.cancel();
    }

    public void add(String trainName) {
        if (get(trainName) != null) return;
        trains.add(new SpeedData(trainName));
    }

    public SpeedData get(String trainName) {
        List<SpeedData> data = trains.stream()
                .filter(speedData -> speedData.getTrainName().equals(trainName))
                .distinct()
                .toList();

        if (data.isEmpty())
            return null;

        return data.get(0);
    }

    public void remove(String trainName) {
        SpeedData data = get(trainName);
        if (data == null) return;

        // Clear actionbar for all players
        MinecartGroup train = TCHelper.getTrain(trainName);

        if (train != null)
            TCHelper.sendActionbar(train, Component.empty());

        trains.remove(data);
    }

    public void sendActionBars() {
        for (SpeedData data : trains) {
            MinecartGroup group = TCHelper.getTrain(data.getTrainName());

            if (group == null)
                return;
            
            Component message;
            String destinationName = data.getDestinationName();

            double realVelocity = data.getVelocity();
            double smoothedVelocity = data.getSmoothVelocity();
            double distance = data.getDistance();

            if (realVelocity > 0) {
                int minutes, seconds;
                int time = (int) (distance / smoothedVelocity);

                seconds = time % 60;
                minutes = (time-seconds) / 60;

                if (distance > 5) {
                    if (time > 3)
                        message = Component.text(String.format("§e%.1f §6Blöcke/s §8| §e%.0f §6Blöcke bis \"§e%s\" §8| §6ETA: §e%d:%02d", realVelocity, distance, destinationName, minutes, seconds));
                    else
                        message = Component.text(String.format("§e%.1f §6Blöcke/s §8| §e%.0f §6Blöcke bis \"§e%s\"", realVelocity, distance, destinationName));
                }

                else
                    message = Component.text(String.format("§e%.1f §6Blöcke/s", realVelocity));
            }

            else if (distance > 5)
                message = Component.text(String.format("§e%.0f §6Blöcke bis \"%s\"", distance, destinationName));
            else
                message = Component.text("");

            TCHelper.sendActionbar(group, "tcdestinations.speedometer", message);
        }
    }

    private void updateData() {
        for (SpeedData data : trains)
            data.update();
    }

    public List<DebugParticle> getDebugParticles() {
        return debugParticles;
    }
}