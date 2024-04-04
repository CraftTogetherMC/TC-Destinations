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
    private final TCDestinations plugin = TCDestinations.plugin;

    private MySQLAdapter mySQLAdapter;
    private TreeMap<Integer, Destination> destinations;

    public DestinationStorage() {
        this.connect();
    }

    public void connect() {
        if (this.isActive())
            return;

        this.destinations = new TreeMap<>();

        // Initialize MySQLAdapter
        this.mySQLAdapter = new MySQLAdapter(plugin,
                this.plugin.getConfig().getString("MySQL.Host"),
                this.plugin.getConfig().getInt("MySQL.Port"),
                this.plugin.getConfig().getString("MySQL.Username"),
                this.plugin.getConfig().getString("MySQL.Password"),
                this.plugin.getConfig().getString("MySQL.Database"),
                this.plugin.getConfig().getString("MySQL.TablePrefix"),
                this.plugin.getConfig().getString("MySQL.JDBCArgs"));

        // Register DestinationTypes from config.yml
        DestinationType.registerTypes(this.plugin.getConfig());

        // Create Tables if missing
        MySQLConnection connection = this.mySQLAdapter.getConnection();
        if (connection == null)
            return;

        try (ResultSet result = connection.query("SHOW TABLES LIKE '%sdestinations';", connection.getTablePrefix())) {

            if (result != null && !result.next()) {
                this.plugin.getLogger().info("[MySQL]: Create Table '" + connection.getTablePrefix() + "destinations' ...");

                connection.execute("""
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
                              `tp_z` double DEFAULT NULL,
                              `tp_yaw` float DEFAULT NULL,
                              `tp_pitch` float DEFAULT NULL
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                        """, connection.getTablePrefix());

                connection.execute("""
                    ALTER TABLE `%sdestinations`
                      ADD PRIMARY KEY (`id`);
                """, connection.getTablePrefix());

                connection.execute("""
                    ALTER TABLE `%sdestinations`
                      MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
                """, connection.getTablePrefix());
            }
        }
        catch (SQLException ex) {
            this.plugin.getLogger().warning("[MySQL]: " + ex.getMessage());
        }
        finally {
            connection.close();
        }

        // Load all destinations from database into our cache
        Bukkit.getServer().getScheduler().runTask(this.plugin, () -> loadAll((err, destinations) -> {
            if (err == null)
                this.plugin.getLogger().info("Loaded " + destinations.size() + " destinations");

            // Add Dynmmap-Markers
            DynmapMarker.setupMarkers(this.plugin.getDestinationStorage().getDestinations());
        }));
    }

    public boolean isActive() {
        if (this.mySQLAdapter == null)
            return false;

        return this.mySQLAdapter.isActive();
    }

    public void disconnect() {
        if (this.mySQLAdapter == null)
            return;

        this.mySQLAdapter.disconnect();
        this.mySQLAdapter = null;
    }

    private void insert(Destination destination, MySQLConnection.Consumer<SQLException, Destination> consumer) {
        MySQLConnection connection = this.mySQLAdapter.getConnection();

        NetworkLocation loc = destination.getLocation();
        NetworkLocation tpLoc = destination.getTeleportLocation();
        JsonArray participants = new JsonArray();

        for (UUID uuid : destination.getParticipants())
            participants.add(uuid.toString());

        connection.insertAsync("INSERT INTO `%sdestinations` " +
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
            "`tp_z`," +
            "`tp_yaw`, " +
            "`tp_pitch` " +
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
            (tpLoc != null ? tpLoc.getZ() : null) + ", " +
            (tpLoc != null ? tpLoc.getYaw() : null) + ", " +
            (tpLoc != null ? tpLoc.getPitch() : null) +
        ");",

        (err, lastInsertedId) -> {
            if (err != null) {
                this.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
                consumer.operation(err, null);
            }
            else {
                // Add to cache
                destination.setId(lastInsertedId);
                this.destinations.put(lastInsertedId, destination);
                consumer.operation(null, destination);
            }

            connection.close();
        }, connection.getTablePrefix());
    }

    public void update(Destination destination, MySQLConnection.Consumer<SQLException, Integer> consumer) {
        MySQLConnection connection = this.mySQLAdapter.getConnection();

        NetworkLocation loc = destination.getLocation();
        NetworkLocation tpLoc = destination.getTeleportLocation();
        JsonArray participants = new JsonArray();

        for (UUID uuid : destination.getParticipants())
            participants.add(uuid.toString());

        connection.updateAsync("UPDATE `%sdestinations` SET " +
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
            "`tp_z`         = " + tpLoc.getZ() + ", " +
            "`tp_yaw`       = " + tpLoc.getYaw() + ", " +
            "`tp_pitch`     = " + tpLoc.getPitch() + " " +
        "WHERE `%sdestinations`.`id` = %s;",

        (err, affectedRows) -> {
            if (err != null) {
                this.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
                consumer.operation(err, null);
            }
            else {
                // Update cache
                destinations.put(destination.getId(), destination);
                consumer.operation(null, affectedRows);
            }
            connection.close();
        }, connection.getTablePrefix(), connection.getTablePrefix(), destination.getId());
    }

    // TODO: Trigger if other server updates a destination
    public void load(int destinationId, Consumer<SQLException, Destination> consumer) {
        MySQLConnection connection = this.mySQLAdapter.getConnection();

        connection.queryAsync("SELECT * FROM `%sdestinations` WHERE `id` = %s", (err, result) -> {
            if (err != null) {
                this.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
                consumer.operation(err, null);
            }

            else {
                Destination dest = null;

                try {
                    if (result.next()) {
                        dest = setupDestination(result);

                        // Update cache
                        if (dest != null)
                            this.destinations.put(dest.getId(), dest);
                    }
                } catch (SQLException ex) {
                    err = ex;
                    this.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
                }
                finally {
                    connection.close();
                }

                consumer.operation(err, dest);
            }
        }, connection.getTablePrefix(), destinationId);
    }

    public void delete(int destinationId, Consumer<SQLException, Integer> consumer) {
        MySQLConnection connection = this.mySQLAdapter.getConnection();

        connection.updateAsync("DELETE FROM `%sdestinations` WHERE `id` = %s", (err, affectedRows) -> {
            if (err != null) {
                this.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
                consumer.operation(err, null);
            }
            else {
                // Update cache
                this.destinations.remove(destinationId);

                consumer.operation(null, affectedRows);
                connection.close();
            }
        }, connection.getTablePrefix(), destinationId);
    }

    public void loadAll(Consumer<SQLException, Collection<Destination>> consumer) {
        MySQLConnection connection = this.mySQLAdapter.getConnection();

        this.destinations = new TreeMap<>();

        connection.queryAsync("SELECT * FROM `%sdestinations`", (err, result) -> {
            if (err != null) {
                this.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
                consumer.operation(err, null);
            }

            else {
                try {
                    while (result.next()) {
                        Destination dest = setupDestination(result);

                        // Update cache
                        if (dest != null)
                            this.destinations.put(dest.getId(), dest);
                    }
                } catch (SQLException ex) {
                    err = ex;
                    this.plugin.getLogger().warning("[MySQL]: Error: " + ex.getMessage());
                }
                finally {
                    connection.close();
                }

                consumer.operation(err, this.destinations.values());
            }
        }, connection.getTablePrefix());
    }

    public Collection<Destination> getDestinations() {
        return this.destinations.values();
    }

    public Collection<Destination> getDestinations(String name) {
        List<Destination> list = new ArrayList<>();

        for (Destination dest : this.destinations.values()) {
            if (dest.getName().equalsIgnoreCase(name))
                list.add(dest);
        }

        return list;
    }

    public Destination getDestination(int id) {
        for (Destination dest : this.destinations.values())
            if (dest.getId() == id) return dest;

        return null;
    }

    public Destination getDestination(String destinationName, String serverName) {
        for (Destination dest : this.destinations.values())
            if (dest.getName().equalsIgnoreCase(destinationName) && dest.getServer().equalsIgnoreCase(serverName)) return dest;

        return null;
    }

    public void addDestination(String name, UUID owner, DestinationType type, Location loc, Boolean isPublic, Consumer<SQLException, Destination> consumer) {
        String serverName = this.plugin.getServerName();
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
            NetworkLocation tpLoc = new NetworkLocation(server, world, result.getDouble("tp_x"), result.getDouble("tp_y"), result.getDouble("tp_z"), result.getFloat("tp_yaw"), result.getFloat("tp_pitch"));
            List<UUID> participants = new ArrayList<>();

            try {
                Type listType = new TypeToken<List<String>>() {}.getType();
                List<String> uuids = new Gson().fromJson(result.getString("participants"), listType);
                for (String uuid : uuids) participants.add(UUID.fromString(uuid));
            } catch (Exception e) {
                e.printStackTrace();
                this.plugin.getLogger().warning("Error: Unable to read participants for '" + name + "'");
            }

            String type = result.getString("type");
            DestinationType destinationType = DestinationType.getFromName(type);

            if (destinationType == null) {
                this.plugin.getLogger().warning("DestinationType '" + type + "' was not found at config.yml");
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
            this.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
            err.printStackTrace();
        }

        return dest;
    }
}
