package de.crafttogether.tcdestinations.speedometer;

import com.bergerkiller.bukkit.common.utils.EntityUtil;
import com.bergerkiller.bukkit.common.utils.PacketUtil;
import com.bergerkiller.bukkit.common.wrappers.ChatText;
import com.bergerkiller.bukkit.common.wrappers.DataWatcher;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.generated.net.minecraft.network.protocol.game.ClientboundAddMobPacketHandle;
import com.bergerkiller.generated.net.minecraft.world.entity.EntityHandle;
import de.crafttogether.TCDestinations;
import de.crafttogether.tcdestinations.util.TCHelper;

import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
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

    }

    public Speedometer() {
        this.trains = new ArrayList<>();
        this.debugParticles = new ArrayList<>();
        this.task = null;
        this.task = Bukkit.getScheduler().runTaskTimer(TCDestinations.plugin, this, 20L, 5L);
    }

    @Override
    public void run() {
        if (trains.isEmpty()) {
            stopTask();
            return;
        }

        if (!debugParticles.isEmpty()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.hasPermission("tcdestinations.debug")) continue;
                debugParticles.removeIf(pa -> TCHelper.getTrain(pa.trainName) == null);
                for (DebugParticle particle : debugParticles) {
                    if (particle.location.getChunk().isLoaded())
                        p.spawnParticle(particle.particle, particle.location, 1, particle.data);
                }
            }
        }

        updateData();
        sendActionBars();
    }


    public void add(String trainName) {
        if (get(trainName) != null) return;

        boolean wasEmpty = trains.isEmpty();
        trains.add(new SpeedData(trainName));

        if (wasEmpty) {
            startTask();
        }
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
        SpeedData speedData = get(trainName);
        if (speedData == null) {
            return;
        }

        // Clear actionbar for all players
        MinecartGroup train = TCHelper.getTrain(trainName);

        if (train != null)
            TCHelper.sendActionbar(train,Component.empty());
        trains.remove(speedData);

        if (trains.isEmpty()) {
            stopTask();
        }
    }

    public void sendActionBars() {
        for (SpeedData data : trains) {
            MinecartGroup group = TCHelper.getTrain(data.getTrainName());

            if (group == null)
                continue;
            
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

    private void startTask() {
        if (task != null && !task.isCancelled()) {
            return;
        }

        task = Bukkit.getScheduler().runTaskTimer(TCDestinations.plugin, this, 0L, 10L);
    }

    private void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}