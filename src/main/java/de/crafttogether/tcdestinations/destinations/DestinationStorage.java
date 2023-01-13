package de.crafttogether.tcdestinations.destinations;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import de.crafttogether.TCDestinations;
import de.crafttogether.common.mysql.MySQLAdapter;
import de.crafttogether.common.mysql.MySQLConnection;
import de.crafttogether.common.mysql.MySQLConnection.Consumer;
import de.crafttogether.common.NetworkLocation;
import de.crafttogether.tcdestinations.util.DynmapMarker;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings("unused")
public class DestinationStorage {
    private final TreeMap<Integer, Destination> destinations = new TreeMap<>();

    public DestinationStorage() {
        MySQLConnection MySQL = MySQLAdapter.getConnection();

        // Create Tables if missing
        try {
            ResultSet result = MySQL.query("SHOW TABLES LIKE '%sdestinations';", MySQL.getTablePrefix());

            if (!result.next()) {
                TCDestinations.plugin.getLogger().info("[MySQL]: Create Table '" + MySQL.getTablePrefix() + "destinations' ...");

                MySQL.execute("""
                    CREATE TABLE `%sdestinations` (
                      `id` int(11) NOT NULL,
                      `name` varchar(24) NOT NULL,
                      `type` varchar(24) NOT NULL,
                      `server` varchar(24) NOT NULL,
                      `world` varchar(24) NOT NULL,
                      `loc_x` double NOT NULL,
                      `loc_y` double NOT NULL,
                      `loc_z` double NOT NULL,
                      `owner` varchar(36) NOT NULL,
                      `participants` longtext DEFAULT NULL,
                      `public` tinyint(1) NOT NULL,
                      `tp_x` double DEFAULT NULL,
                      `tp_y` double DEFAULT NULL,
                      `tp_z` double DEFAULT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """, MySQL.getTablePrefix());

                MySQL.execute("""
                    ALTER TABLE `%sdestinations`
                      ADD PRIMARY KEY (`id`);
                """, MySQL.getTablePrefix());

                MySQL.execute("""
                    ALTER TABLE `%sdestinations`
                      MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
                """, MySQL.getTablePrefix());
            }
        }
        catch (SQLException ex) {
            TCDestinations.plugin.getLogger().warning("[MySQL]: " + ex.getMessage());
        }
        finally {
            MySQL.close();
        }

        // Load all destinations from database into our cache
        Bukkit.getServer().getScheduler().runTask(TCDestinations.plugin, () -> loadAll((err, destinations) -> {
            TCDestinations.plugin.getLogger().info("Loaded " + destinations.size() + " destinations");

            // Add Dynmmap-Markers
            DynmapMarker.setupMarkers(TCDestinations.plugin.getDestinationStorage().getDestinations());
        }));
    }

    private void insert(Destination destination, MySQLConnection.Consumer<SQLException, Destination> consumer) {
        MySQLConnection MySQL = MySQLAdapter.getConnection();

        NetworkLocation loc = destination.getLocation();
        NetworkLocation tpLoc = destination.getTeleportLocation();
        JsonArray participants = new JsonArray();

        for (UUID uuid : destination.getParticipants())
            participants.add(uuid.toString());

        MySQL.insertAsync("INSERT INTO `%sdestinations` " +
        "(" +
            "`name`, " +
            "`type`, " +
            "`server`, " +
            "`world`, " +
            "`loc_x`, " +
            "`loc_y`, " +
            "`loc_z`, " +
            "`owner`, " +
            "`participants`, " +
            "`public`, " +
            "`tp_x`, " +
            "`tp_y`, " +
            "`tp_z`" +
        ") " +

        "VALUES (" +
            "'" + destination.getName() + "', " +
            "'" + destination.getType().getName() + "', " +
            "'" + destination.getServer() + "', " +
            "'" + destination.getWorld() + "', " +
            (loc != null ? loc.getX() : null) + ", " +
            (loc != null ? loc.getY() : null) + ", " +
            (loc != null ? loc.getZ() : null) + ", " +
            "'" + destination.getOwner().toString() + "', " +
            "'" + participants + "', " +
            (destination.isPublic() ? 1 : 0) + ", " +
            (tpLoc != null ? tpLoc.getX() : null) + ", " +
            (tpLoc != null ? tpLoc.getY() : null) + ", " +
            (tpLoc != null ? tpLoc.getZ() : null) +
        ");",

        (err, lastInsertedId) -> {
            if (err != null)
                TCDestinations.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());

            // Add to cache
            destination.setId(lastInsertedId);
            destinations.put(lastInsertedId, destination);

            consumer.operation(err, destination);
            MySQL.close();
        }, MySQL.getTablePrefix());
    }

    public void update(Destination destination, MySQLConnection.Consumer<SQLException, Integer> consumer) {
        MySQLConnection MySQL = MySQLAdapter.getConnection();

        NetworkLocation loc = destination.getLocation();
        NetworkLocation tpLoc = destination.getTeleportLocation();
        JsonArray participants = new JsonArray();

        for (UUID uuid : destination.getParticipants())
            participants.add(uuid.toString());

        MySQL.updateAsync("UPDATE `%sdestinations` SET " +
            "`name`         = '" + destination.getName() + "', " +
            "`type`         = '" + destination.getType().getName() + "', " +
            "`server`       = '" + destination.getServer() + "', " +
            "`world`        = '" + destination.getWorld() + "', " +
            "`loc_x`        = " + loc.getX() + ", " +
            "`loc_y`        = " + loc.getY() + ", " +
            "`loc_z`        = " + loc.getZ() + ", " +
            "`owner`        = '" + destination.getOwner().toString() + "', " +
            "`participants` = '" + participants + "', " +
            "`public`       = " + (destination.isPublic() ? 1 : 0) + ", " +
            "`tp_x`         = " + tpLoc.getX() + ", " +
            "`tp_y`         = " + tpLoc.getY() + ", " +
            "`tp_z`         = " + tpLoc.getZ() + " " +
        "WHERE `%sdestinations`.`id` = %s;",

        (err, affectedRows) -> {
            if (err != null)
                TCDestinations.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());

            consumer.operation(err, affectedRows);
            MySQL.close();
        }, MySQL.getTablePrefix(), MySQL.getTablePrefix(), destination.getId());
    }

    // TODO: Trigger if other server updates a destination
    public void load(int destinationId, Consumer<SQLException, Destination> consumer) {
        MySQLConnection MySQL = MySQLAdapter.getConnection();

        MySQL.queryAsync("SELECT * FROM `%sdestinations` WHERE `id` = %s", (err, result) -> {
            if (err != null) {
                TCDestinations.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
            }

            else {
                Destination dest = null;

                try {
                    if (result.next()) {
                        dest = setupDestination(result);

                        // Update cache
                        if (dest != null)
                            destinations.put(dest.getId(), dest);
                    }
                } catch (SQLException ex) {
                    err = ex;
                    TCDestinations.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
                }
                finally {
                    MySQL.close();
                }

                consumer.operation(err, dest);
            }
        }, MySQL.getTablePrefix(), destinationId);
    }

    public void delete(int destinationId, Consumer<SQLException, Integer> consumer) {
        MySQLConnection MySQL = MySQLAdapter.getConnection();

        MySQL.updateAsync("DELETE FROM `%sdestinations` WHERE `id` = %s", (err, affectedRows) -> {
            if (err != null) {
                TCDestinations.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
                consumer.operation(err, null);
            }
            else {
                // Update cache
                destinations.remove(destinationId);

                consumer.operation(null, affectedRows);
                MySQL.close();
            }
        }, MySQL.getTablePrefix(), destinationId);
    }

    public void loadAll(Consumer<SQLException, Collection<Destination>> consumer) {
        MySQLConnection MySQL = MySQLAdapter.getConnection();

        MySQL.queryAsync("SELECT * FROM `%sdestinations`", (err, result) -> {
            if (err != null) {
                TCDestinations.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
                consumer.operation(err, null);
            }

            else {
                try {
                    while (result.next()) {
                        Destination dest = setupDestination(result);

                        // Update cache
                        if (dest != null)
                            destinations.put(dest.getId(), dest);
                    }
                } catch (SQLException ex) {
                    err = ex;
                    TCDestinations.plugin.getLogger().warning("[MySQL]: Error: " + ex.getMessage());
                }
                finally {
                    MySQL.close();
                }

                consumer.operation(err, destinations.values());
            }
        }, MySQL.getTablePrefix());
    }

    public Collection<Destination> getDestinations() {
        return destinations.values();
    }

    public Collection<Destination> getDestinations(String name) {
        List<Destination> list = new ArrayList<>();

        for (Destination dest : destinations.values()) {
            if (dest.getName().equalsIgnoreCase(name))
                list.add(dest);
        }

        return list;
    }

    public Destination getDestination(int id) {
        for (Destination dest : destinations.values())
            if (dest.getId() == id) return dest;

        return null;
    }

    public Destination getDestination(String destinationName, String serverName) {
        for (Destination dest : destinations.values())
            if (dest.getName().equalsIgnoreCase(destinationName) && dest.getServer().equalsIgnoreCase(serverName)) return dest;

        return null;
    }

    public void addDestination(String name, UUID owner, DestinationType type, Location loc, Boolean isPublic, Consumer<SQLException, Destination> consumer) {
        String serverName = TCDestinations.plugin.getServerName();
        NetworkLocation ctLoc = NetworkLocation.fromBukkitLocation(loc, serverName);

        Destination dest = new Destination(name, serverName, Objects.requireNonNull(loc.getWorld()).getName(), owner, new ArrayList<>(), type, ctLoc, ctLoc, isPublic);
        insert(dest, consumer);
    }

    private Destination setupDestination(ResultSet result) {
        Destination dest = null;

        try {
            Integer id = result.getInt("id");
            String name = result.getString("name");
            String server = result.getString("server");
            String world = result.getString("world");

            NetworkLocation loc = new NetworkLocation(server, world, result.getDouble("loc_x"), result.getDouble("loc_y"), result.getDouble("loc_z"));
            NetworkLocation tpLoc = new NetworkLocation(server, world, result.getDouble("tp_x"), result.getDouble("tp_y"), result.getDouble("tp_z"));
            List<UUID> participants = new ArrayList<>();

            try {
                Type listType = new TypeToken<List<String>>() {}.getType();
                List<String> uuids = new Gson().fromJson(result.getString("participants"), listType);
                for (String uuid : uuids) participants.add(UUID.fromString(uuid));
            } catch (Exception e) {
                e.printStackTrace();
                TCDestinations.plugin.getLogger().warning("Error: Unable to read participants for '" + name + "'");
            }

            String type = result.getString("type");
            DestinationType destinationType = DestinationType.getFromName(type);

            if (destinationType == null) {
                TCDestinations.plugin.getLogger().warning("DestinationType '" + type + "' was not found at config.yml");
                return null;
            }

            dest = new Destination(name, id);
            dest.setServer(server);
            dest.setWorld(world);
            dest.setOwner(UUID.fromString(result.getString("owner")));
            dest.setParticipants(participants);
            dest.setType(destinationType);
            dest.setLocation(loc);
            dest.setTeleportLocation(tpLoc);
            dest.setPublic(result.getBoolean("public"));
        }
        catch (Exception err) {
            TCDestinations.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
            err.printStackTrace();
        }

        return dest;
    }
}
