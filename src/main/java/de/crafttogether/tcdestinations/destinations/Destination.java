package de.crafttogether.tcdestinations.destinations;

import de.crafttogether.tcdestinations.util.CTLocation;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Destination {
    private String name;
    private Integer id = null;
    private String server = null;
    private String world = null;
    private UUID owner = null;
    private List<UUID> participants = new ArrayList<>();
    private DestinationType type = null;
    private CTLocation location = null;
    private CTLocation teleportLocation = null;
    private Boolean isPublic = null;

    public Destination(String name, Integer id) {
        this.id = id;
        this.name = name;
    }

    public Destination(String name, String server, String world, UUID owner, List<UUID> participants, DestinationType type, CTLocation location, CTLocation teleportLocation, Boolean isPublic) {
        this.name = name;
        this.server = server;
        this.world = world;
        this.owner = owner;
        this.participants = participants;
        this.type = type;
        this.location = location;
        this.teleportLocation = teleportLocation;
        this.isPublic = isPublic;
    }

    public Integer getId() { return id; }

    public String getName() {
        return this.name;
    }

    public String getServer() {
        return this.server;
    }

    public String getWorld() {
        return this.world;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public List<UUID> getParticipants() { return participants; }

    public DestinationType getType() {
        return this.type;
    }

    public CTLocation getLocation() { return this.location; }

    public CTLocation getTeleportLocation() {
        return teleportLocation;
    }

    public Boolean isPublic() {
        return this.isPublic;
    }

    public void setId(Integer id) { this.id = id; }

    public void setName(String name) {
        this.name = name;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void setParticipants(List<UUID> participants) { this.participants = participants; }

    public void addParticipant(UUID uuid) { this.participants.add(uuid); }

    public void removeParticipant(UUID uuid) { this.participants.remove(uuid); }

    public void setType(DestinationType type) {
        this.type = type;
    }

    public void setLocation(CTLocation location) {
        this.location = location;
    }

    public void setTeleportLocation(CTLocation teleportLocation) {
        this.teleportLocation = teleportLocation;
    }

    public void setPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String toString() {
        StringBuilder strParticipants = new StringBuilder();
        for (UUID participant : participants) strParticipants.append(participant.toString()).append(",");
        if (strParticipants.length() > 1) strParticipants = new StringBuilder(strParticipants.substring(0, strParticipants.length() - 1));
        return "id=" + id + ", name=" + name + ", server=" + server + ", world=" + world + ", type=" + (type == null ? null : type.toString()) + ", owner=" + owner + ", participants=[" + strParticipants + "], isPrivate=" + isPublic + ", location=[" + (location == null ? null : location.toString()) + "], teleportLocation=[" + (teleportLocation == null ? null : teleportLocation.toString()) + "]";
    }
}
