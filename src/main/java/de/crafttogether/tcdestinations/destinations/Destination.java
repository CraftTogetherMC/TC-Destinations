package de.crafttogether.tcdestinations.destinations;

import de.crafttogether.common.NetworkLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public class Destination {
    private String name;
    private Integer id = null;
    private String server = null;
    private String world = null;
    private UUID owner = null;
    private List<UUID> participants = new ArrayList<>();
    private DestinationType type = null;
    private NetworkLocation location = null;
    private NetworkLocation teleportLocation = null;
    private Boolean isPublic = null;

    public Destination(String name, Integer id) {
        this.id = id;
        this.name = name;
    }

    public Destination(String name, String server, String world, UUID owner, List<UUID> participants, DestinationType type, NetworkLocation location, NetworkLocation teleportLocation, Boolean isPublic) {
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

    public Integer getId() {
        return id;
    }

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

    public List<UUID> getParticipants() {
        return participants;
    }

    public DestinationType getType() {
        return this.type;
    }

    public NetworkLocation getLocation() {
        return this.location;
    }

    public NetworkLocation getTeleportLocation() {
        return teleportLocation;
    }

    public Boolean isPublic() {
        return this.isPublic;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public void setParticipants(List<UUID> participants) {
        this.participants = participants;
    }

    public void addParticipant(UUID uuid) {
        this.participants.add(uuid);
    }

    public void removeParticipant(UUID uuid) {
        this.participants.remove(uuid);
    }

    public void setType(DestinationType type) {
        this.type = type;
    }

    public void setLocation(NetworkLocation location) {
        this.location = location;
    }

    public void setTeleportLocation(NetworkLocation teleportLocation) {
        this.teleportLocation = teleportLocation;
    }

    public void setPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String toString() {
        StringBuilder strParticipants = new StringBuilder();
        for (UUID participant : participants) strParticipants.append(participant.toString()).append(",");
        if (strParticipants.length() > 1) strParticipants = new StringBuilder(strParticipants.substring(0, strParticipants.length() - 1));
        return "CTDestination{id=" + id + ", name=" + name + ", server=" + server + ", world=" + world + ", type=" + (type == null ? null : type.toString()) + ", owner=" + owner + ", participants=[" + strParticipants + "], isPrivate=" + isPublic + ", location=[" + (location == null ? null : location.toString()) + "], teleportLocation=[" + (teleportLocation == null ? null : teleportLocation.toString()) + "]}";
    }
}
