package de.crafttogether.tcdestinations.destinations;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import de.crafttogether.TCDestinations;
import de.crafttogether.common.mysql.MySQLAdapter;
import de.crafttogether.common.mysql.MySQLConnection;
import de.crafttogether.common.mysql.MySQLConnection.Consumer;
import de.crafttogether.common.NetworkLocation;
import de.crafttogether.common.platform.bukkit.util.BukkitNetworkLocationAdapter;
import de.crafttogether.tcdestinations.util.DynmapMarker;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class DestinationStorage {
    private final TCDestinations plugin = TCDestinations.plugin;

    private MySQLAdapter mySQLAdapter;
    private  Map<Integer, Destination> destinations = new ConcurrentHashMap<>();


    public DestinationStorage() {
        this.connect();
    }

    public void connect() {
        if (this.isActive())
            return;

        this.destinations.clear();

        // Initialize MySQLAdapter
        this.mySQLAdapter = new MySQLAdapter(plugin.getPlatformLayer(),
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
                      `tp_z` double DEFAULT NULL
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

        for (UUID uuid : destination.getParticipants()) {
            participants.add(uuid.toString());
        }

        String locX = (loc != null) ? String.valueOf(loc.getX()) : "NULL";
        String locY = (loc != null) ? String.valueOf(loc.getY()) : "NULL";
        String locZ = (loc != null) ? String.valueOf(loc.getZ()) : "NULL";

        String tpX = (tpLoc != null) ? String.valueOf(tpLoc.getX()) : "NULL";
        String tpY = (tpLoc != null) ? String.valueOf(tpLoc.getY()) : "NULL";
        String tpZ = (tpLoc != null) ? String.valueOf(tpLoc.getZ()) : "NULL";

        connection.updateAsync("UPDATE `%sdestinations` SET " +
                        "`name`         = '" + escapeSql(destination.getName()) + "', " +
                        "`type`         = '" + escapeSql(destination.getType().getName()) + "', " +
                        "`server`       = '" + escapeSql(destination.getServer()) + "', " +
                        "`world`        = '" + escapeSql(destination.getWorld()) + "', " +
                        "`loc_x`        = " + locX + ", " +
                        "`loc_y`        = " + locY + ", " +
                        "`loc_z`        = " + locZ + ", " +
                        "`owner`        = '" + destination.getOwner() + "', " +
                        "`participants` = '" + escapeSql(participants.toString()) + "', " +
                        "`public`       = " + (destination.isPublic() ? 1 : 0) + ", " +
                        "`tp_x`         = " + tpX + ", " +
                        "`tp_y`         = " + tpY + ", " +
                        "`tp_z`         = " + tpZ + " " +
                        "WHERE `%sdestinations`.`id` = %s;",
                (err, affectedRows) -> {
                    try {
                        if (err != null) {
                            this.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
                            consumer.operation(err, null);
                            return;
                        }

                        destinations.put(destination.getId(), destination);
                        consumer.operation(null, affectedRows);
                    } finally {
                        connection.close();
                    }
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
            try {
                if (err != null) {
                    this.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
                    consumer.operation(err, null);
                    return;
                }

                this.destinations.remove(destinationId);
                consumer.operation(null, affectedRows);
            } finally {
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
        return new ArrayList<>(this.destinations.values());
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
        NetworkLocation ctLoc = BukkitNetworkLocationAdapter.fromBukkitLocation(loc, serverName);

        Destination dest = new Destination(name, serverName, Objects.requireNonNull(loc.getWorld()).getName(), owner, new ArrayList<>(), type, ctLoc, ctLoc, isPublic);
        insert(dest, consumer);
    }

    private Destination setupDestination(ResultSet result) {
        try {
            Integer id = result.getInt("id");
            String name = result.getString("name");
            String server = result.getString("server");
            String world = result.getString("world");

            UUID owner;
            try {
                owner = UUID.fromString(result.getString("owner"));
            } catch (Exception ex) {
                this.plugin.getLogger().warning("Invalid owner UUID for destination '" + name + "'");
                return null;
            }

            NetworkLocation loc = null;
            double locX = result.getDouble("loc_x");
            boolean locXNull = result.wasNull();
            double locY = result.getDouble("loc_y");
            boolean locYNull = result.wasNull();
            double locZ = result.getDouble("loc_z");
            boolean locZNull = result.wasNull();

            if (!locXNull && !locYNull && !locZNull) {
                loc = new NetworkLocation(server, world, locX, locY, locZ);
            }

            NetworkLocation tpLoc = null;
            double tpX = result.getDouble("tp_x");
            boolean tpXNull = result.wasNull();
            double tpY = result.getDouble("tp_y");
            boolean tpYNull = result.wasNull();
            double tpZ = result.getDouble("tp_z");
            boolean tpZNull = result.wasNull();

            if (!tpXNull && !tpYNull && !tpZNull) {
                tpLoc = new NetworkLocation(server, world, tpX, tpY, tpZ);
            }

            List<UUID> participants = new ArrayList<>();
            String participantsJson = result.getString("participants");
            if (participantsJson != null && !participantsJson.isBlank()) {
                try {
                    Type listType = new TypeToken<List<String>>() {}.getType();
                    List<String> uuids = new Gson().fromJson(participantsJson, listType);
                    if (uuids != null) {
                        for (String uuid : uuids) {
                            try {
                                participants.add(UUID.fromString(uuid));
                            } catch (Exception ignored) {
                            }
                        }
                    }
                } catch (Exception ex) {
                    this.plugin.getLogger().warning("Unable to read participants for '" + name + "'");
                }
            }

            String typeName = result.getString("type");
            DestinationType destinationType = DestinationType.getFromName(typeName);
            if (destinationType == null) {
                this.plugin.getLogger().warning("DestinationType '" + typeName + "' was not found at config.yml");
                return null;
            }

            Destination dest = new Destination(name, id);
            dest.setServer(server);
            dest.setWorld(world);
            dest.setOwner(owner);
            dest.setParticipants(participants);
            dest.setType(destinationType);
            dest.setLocation(loc);
            dest.setTeleportLocation(tpLoc);
            dest.setPublic(result.getBoolean("public"));

            return dest;
        } catch (Exception err) {
            this.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
            err.printStackTrace();
            return null;
        }
    }
    private String escapeSql(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("\\", "\\\\").replace("'", "''");
    }

}
