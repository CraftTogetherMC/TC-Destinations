![](https://img.shields.io/github/license/CraftTogetherMC/TC-Destinations?style=flat-square)
![](https://img.shields.io/github/last-commit/CraftTogetherMC/TC-Destinations?style=flat-square)
![](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.craft-together-mc.de%2Fjob%2FTC%2520Destinations&style=flat-square)
![](https://shields-io-visitor-counter.herokuapp.com/badge?page=CraftTogetherMC.TC-Destinations&style=flat-square)
![](https://img.shields.io/spiget/download-size/107441?style=flat-square)
![](https://img.shields.io/spiget/downloads/107441?style=flat-square)
  
[Development Builds](https://ci.craft-together-mc.de/job/TC-Destinations) / 
[Modrinth](https://modrinth.com/plugin/tc-destinations) / 
[SpigotMC](https://www.spigotmc.org/resources/tc-destinations.107441/)
  
# TC-Destinations
#### Requires: [TrainCarts](https://github.com/bergerhealer/TrainCarts), [CTCommons](https://github.com/CraftTogetherMC/CTCommons) and a MySQL-Database
**Java-Version:** 17 or above  
**Minecraft-Version:** 1.17.1 or above  
  
TC-Destinations is a plugin for Minecraft servers using [SpigotMC](https://www.spigotmc.org), [PaperMC](https://papermc.io) or forks of these projects.   
It serves as an add-on for the [TrainCarts](https://github.com/bergerhealer/TrainCarts) plugin and comes along with a bunch of features to manage destinations in a more advanced way.  
It is very lightweight as database queries and network connections run completely asynchronously.
   
This plugin was developed for the [CraftTogetherMC](https://github.com/CraftTogetherMC) Minecraft server, see also: [TC-Portals](https://github.com/CraftTogetherMC/TC-Portals)!  
   
### A big thank you and lots of love go out to [TeamBergerhealer](https://github.com/bergerhealer)  
Also, a lot of appreciation goes to the people behind [Cloud](https://github.com/Incendo/cloud) and [Adventure](https://github.com/KyoriPowered/adventure)!  
  
## Features:
- Fancy paginated list of destinations (Works cross-server in a BungeeCord network)
- Manage your destinations in a more advanced way
- Teleport to your destinations
- Set up multiple enter-messages with clickable texts using [MiniMessage](https://docs.adventure.kyori.net/minimessage)
- Dynmap integration *(A marker is created on the map for each destination)*
- Commands to get mobs on/off train(s).
- All root-commands renameable
- All texts can be customized

#### Choose your destination with `/destination`
![](https://i.imgur.com/vSkjgU3.png)

#### Clickable paginated list of all your saved destinations `/destinations`
![](https://i.imgur.com/a6VdIq0.png)
![](https://i.imgur.com/mUETMfv.png)

#### Set up multiple customized enter-messages *(enterMessages.yml)*
![](https://i.imgur.com/8qRGMM4.png)

## Commands & Permissions:

#### Select a Destination
| Command                         | Permission                            | Description                                                                       |
|:--------------------------------|:--------------------------------------|:----------------------------------------------------------------------------------|
| `/destination`                  | craftbahn.command.destination         | Shows basic information about using the command                                   |
| `/destination <name>`           | craftbahn.command.destination         | Sets the specified destination to the currently selected train                    |
| `/destinations [type]`          | craftbahn.command.destinations        | Shows a list of all destinations                                                  |
| `/destinations [type] [filter]` | craftbahn.command.destinations.filter | Shows a filtered list of all destinations **Filter flags:** `--server` `--player` |  

#### Manage your destinations
| Command                                                  | Permissions                             | Description                                                                                                                |
|:---------------------------------------------------------|:----------------------------------------|:---------------------------------------------------------------------------------------------------------------------------|
| `/destedit info <destination> [server]`                  | craftbahn.command.destedit.info         | Displays detailed information about the specified destination                                                              |
| `/destedit tp <destination> [server]`                    | craftbahn.command.destedit.teleport     | Teleports the player to the specified destination                                                                          |
| `/destedit add <destination> <type> `                    | craftbahn.command.destedit.add          | Adds a new destination with the specified station type                                                                     |
| `/destedit remove <destination> [server] `               | craftbahn.command.destedit.remove       | Removes an existing destination                                                                                            |
| `/destedit addmember <destination> <player> [server]`    | craftbahn.command.destedit.addmember    | Adds a secondary owner to the specified destination                                                                        |
| `/destedit removemember <destination> <player> [server]` | craftbahn.command.destedit.removemember | Removes a secondary owner of the specified destination                                                                     |
| `/destedit settype <destination> <player> [server]`      | craftbahn.command.destedit.settype      | Specifies the type of specified destination                                                                                |
| `/destedit setowner <destination> <player> [server]`     | craftbahn.command.destedit.setowner     | Sets the owner of a destination                                                                                            |
| `/destedit setprivate <destination> <player> [server]`   | craftbahn.command.destedit.setprivate   | Specifies that this target is private. Only players with permission `craftbahn.destination.see.private` can use and see it |
| `/destedit setpublic <destination> <player> [server]`    | craftbahn.command.destedit.setpublic    | Sets this game to be viewable by all players. (This is the default setting for newly created destinations)                 |                                                
| `/destedit setwarp <destination> <player> [server]`      | craftbahn.command.destedit.setwarp      | Sets the teleport point of this target for players                                                                         |
| `/destedit setlocation <destination> <player> [server]`  | craftbahn.command.destedit.setlocation  | Specifies the position of the destination (Mainly used to locate dynmap markers)                                           |
| `/destedit updatemarker <destination> <player> [server]` | craftbahn.command.destedit.updatemarker | Renews all markers to be displayed on the dynmap                                                                           |

#### Additional commands
| Command               | Permission                 | Description                                                 |
|:----------------------|:---------------------------|:------------------------------------------------------------|
| `/mobenter [radius]`  | craftbahn.command.mobenter | Allows animals around the selected train to board the train |
| `/mobeject`           | craftbahn.command.mobeject | Ejects all animals from the selected train                  |

## Pathfinding across servers (BungeeCord)
If you're using [TC-Portals](https://github.com/CraftTogetherMC/TC-Portals) to create cross-server portals, with a little extra work,  
it is possible to reach destinations on another server.  
  
For example, if you want to drive from server1 to a destination on server2,  
you need to create a [destination](https://wiki.traincarts.net/p/TrainCarts/Signs/Destination) on server1 that leads to the portal, which leads to server2.  
Let's name it server2 as well.  
  
If you now run `/destination` to set a destination, for your train and the destination is  
on another server, TC-Destinations will create a [route](https://wiki.traincarts.net/p/TrainCarts/PathFinding#Route_Manager) for the train.  
The route then firstly contain the destination: `server2` and then afterwards,  
the destination you want to reach.   
  
Thats it!  

## F.A.Q
<details>
    <summary>Can I use this plugin without Bungeecord?</summary>
    Yes you can.
</details>

<details>
    <summary>I don't want other servers' destinations to be listed. What can I do?</summary>
    Just use separate databases or table-prefixes for each server
</details>
  
## Libraries used
- [CTCommons](https://github.com/CraftTogetherMC/CTCommons) (CraftTogether's plugin library)
- [BKCommonLib](https://github.com/bergerhealer) (Extensive plugin library)
- [Adventure](https://docs.adventure.kyori.net) (Very neat UI-Framework for Bukkit/Bungeecord and more)
- [MiniMessage](https://docs.adventure.kyori.net/minimessage) (Text format to represent chat components)
- [Cloud](https://github.com/Incendo/cloud) (Command Framework)
- [HikariCP](https://github.com/brettwooldridge/HikariCP) (High-performance, JDBC connection pool)
- [MySQL Connector/J](https://github.com/mysql/mysql-connector-j) (JDBC-Driver)
  
### MySQL Table-structure:

``` sql
CREATE TABLE `ct_destinations` (
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

ALTER TABLE `ct_destinations`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `ct_destinations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

COMMIT;
```


