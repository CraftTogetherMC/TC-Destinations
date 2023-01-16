package de.crafttogether.tcdestinations;

import de.crafttogether.TCDestinations;
import de.crafttogether.common.localization.LocalizationEnum;
import de.crafttogether.common.localization.LocalizationManager;

public class Localization extends LocalizationEnum {
    public static final Localization PREFIX = new Localization("prefix", "<gold>TCDestinations </gold><dark_gray>» </dark_gray>");
    public static final Localization HEADER = new Localization("header", "<yellow>-------------- <red><bold>TCDestinations</bold></red> --------------<yellow/>");
    public static final Localization FOOTER = new Localization("footer", "<yellow>----------------------------------------</yellow>");

    public static final Localization CONFIG_RELOADED = new Localization("config.reloaded", "<prefix/><green>Configuration reloaded.</green>");

    public static final Localization COMMAND_NOPERM = new Localization("command.noPerm", "<hover:show_text:'<red>{permission}'><red>You do not have permission, ask an admin to do this for you.</red></hover>");
    public static final Localization COMMAND_NOTRAIN = new Localization("command.noTrain", "<prefix/><red>Please enter a train first.</red>");

    public static final Localization COMMAND_DESTINATION_NOTEXIST = new Localization("command.destination.notExist", "<prefix/><red>No destination named <yellow>{input}</yellow> was found.</red>");
    public static final Localization COMMAND_DESTINATION_NOPERMISSION = new Localization("command.destination.noPermission", "<prefix/><red>You don't have access to this destination.</red>");
    public static final Localization COMMAND_DESTINATION_MULTIPLEDEST = new Localization("command.destination.multiple", "<prefix/><red>Several possible destinations were found.</red>");
    public static final Localization COMMAND_DESTINATION_APPLIED = new Localization("command.destination.applied", "<prefix/><green>This train now tries to reach the destination <yellow>{destination}</yellow>.</green>");
    public static final Localization COMMAND_DESTINATION_INFO = new Localization("command.destination.info",
            """
                <header/>

                <prefix/><gold><bold>Please choose your destination:</bold></gold>
                <prefix/><hover:show_text:'<green>List available destinations</green>'><click:run_command:{cmd_destinations}><red>{cmd_destinations}</red></click></hover>
                <prefix/><yellow>or</yellow>
                <prefix/><click:suggest_command:{cmd_destination} NAME><red>{cmd_destination}</red></click> <gray>\\<name></gray>
                
                <footer/>""");

    public static final Localization COMMAND_DESTINATIONS_HEAD_CONTENT = new Localization("command.destinations.head.content",
            """
                <header/>

                <prefix/><gold><bold>Possible destinations:</bold></gold>
                <prefix/>
                <types/>""");
    public static final Localization COMMAND_DESTINATIONS_HEAD_DESTINATIONTYPE = new Localization("command.destinations.head.destinationType", "<prefix/><hover:show_text:'<green>{cmd_destinations} {displayName}</green>'><click:run_command:{cmd_destinations} {displayName}><green> > </green>{displayName}</click></hover>");

    public static final Localization COMMAND_DESTINATIONS_LIST_INVALIDPAGE = new Localization("command.destinations.list.invalidPage", "<prefix/><red>Invalid page number.</red>");
    public static final Localization COMMAND_DESTINATIONS_LIST_UNKOWNPAGE = new Localization("command.destinations.list.unkownPage", "<prefix/><red>There are only {pages} page(s).</red>");
    public static final Localization COMMAND_DESTINATIONS_LIST_EMPTY = new Localization("command.destinations.list.empty", "<prefix/><red>No destinations were found for this selection.</red>");
    public static final Localization COMMAND_DESTINATIONS_LIST_INDICATOR = new Localization("command.destinations.list.indicator", " <yellow>{actual}</yellow> <green>/</green> <yellow>{total}</yellow> ");
    public static final Localization COMMAND_DESTINATIONS_LIST_CAPTION = new Localization("command.destinations.list.caption", "<prefix/><gray> # </gray><gold><bold>{server}</bold></gold>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_CAPTION = new Localization("command.destinations.list.entry.hover.caption", "<green>{command}</green>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_TYPE = new Localization("command.destinations.list.entry.hover.type", "<gold>Station type:</gold> <yellow>{type}</yellow>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_OWNER = new Localization("command.destinations.list.entry.hover.owner", "<gold>Owner:</gold> <yellow>{owner}</yellow>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_OWNERUNKOWN = new Localization("command.destinations.list.entry.hover.ownerUnkown", "Unknown");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_LOCATION = new Localization("command.destinations.list.entry.hover.location", "<gold>Coordinates:</gold> <yellow>{location}</yellow>");
    public static final Localization COMMAND_DESTINATIONS_LIST_ENTRY_HOVER_WORLD = new Localization("command.destinations.list.entry.hover.world", "<gold>World:</gold> <yellow>{world}</yellow>");
    public static final Localization COMMAND_DESTINATIONS_BTN_FORWARDS_ON = new Localization("command.destinations.btn.forwards.on", "<hover:show_text:'<green>Next page</green>'><click:run_command:{command}><gold>Next</gold> <green>>>----</green></click></hover>");
    public static final Localization COMMAND_DESTINATIONS_BTN_FORWARDS_OFF = new Localization("command.destinations.btn.forwards.off", "<gray>Next</gray> <green>>>----</green>");
    public static final Localization COMMAND_DESTINATIONS_BTN_BACKWARDS_ON = new Localization("command.destinations.btn.backwards.on", "<hover:show_text:'<green>Previous page</green>'><click:run_command:{command}><green>----<<</green> <gold>Previous</gold></click></hover>");
    public static final Localization COMMAND_DESTINATIONS_BTN_BACKWARDS_OFF = new Localization("command.destinations.btn.backwards.off", "<green>----<<</green> <gray>Previous</gray>");
    public static final Localization COMMAND_DESTINATIONS_BTN_TELEPORT = new Localization("command.destinations.btn.teleport", "<hover:show_text:'<green>Teleport to the station</green>'><click:run_command:{command_destedit} tp {destination}> <gray>[</gray><white>TP</white><gray>]</gray></click></hover>");

    public static final Localization COMMAND_DESTEDIT_INFO = new Localization("command.destedit.info", """
            <header/>
            
            <prefix/><gold>Destination:</gold> <yellow>{name}</yellow>
            <prefix/><gold>ID:</gold> <yellow>{id}</yellow>
            <prefix/><gold>Type:</gold> <yellow>{type}</yellow>
            <prefix/><gold>Owner:</gold> <yellow>{owner}</yellow>
            <prefix/><gold>Participants:</gold> <yellow>{participants}</yellow>
            <prefix/><gold>Server:</gold> <yellow>{server}</yellow>
            <prefix/><gold>World:</gold> <yellow>{world}</yellow>
            <prefix/><gold>Coordinates:</gold> <yellow>{x} {x} {z}</yellow>
            
            <footer/>""");
    public static final Localization COMMAND_DESTEDIT_NONAME = new Localization("command.destedit.noName", "<prefix/><red>Please enter the name of the destination.</red>");
    public static final Localization COMMAND_DESTEDIT_MULTIPLEDEST = new Localization("command.destedit.multiple", "<prefix/><red>There are several destinations with this name.<newLine><prefix/>Please also specify the server name.</red>");
    public static final Localization COMMAND_DESTEDIT_UNKOWNPLAYER = new Localization("command.destedit.unkownPlayer", "<prefix/><red>A player with the name <yellow>{input}</yellow> could not be found.</red>");
    public static final Localization COMMAND_DESTEDIT_SAVEFAILED = new Localization("command.destedit.saveFailed", "<prefix/><hover:show_text:'<white>{error}'><red>An error occurred while saving the destination. Please contact an administrator.</red></hover>");
    public static final Localization COMMAND_DESTEDIT_TELEPORT = new Localization("command.destedit.teleport", "<prefix/><gold>You have been teleported to the destination <yellow>{destination}</yellow>.");
    public static final Localization COMMAND_DESTEDIT_TELEPORT_OTHERSERVER = new Localization("command.destedit.teleport.otherServer", "<prefix/><red>The destination is on the server: <yellow>{server}</yellow></red>.");
    public static final Localization COMMAND_DESTEDIT_ADD_INVALIDTYPE = new Localization("command.destedit.teleport.add.invalidType", "<prefix/><red>Invalid station type.</red>");
    public static final Localization COMMAND_DESTEDIT_ADD_SUCCESS = new Localization("command.destedit.teleport.add.success", "<prefix/><green>Destination <yellow>{destination}</yellow> ID: <yellow>{id}</yellow>.</green>");
    public static final Localization COMMAND_DESTEDIT_REMOVE = new Localization("command.destedit.teleport.remove", "<prefix/><green>The destination <yellow>{destination}</yellow> has been deleted.</green>");
    public static final Localization COMMAND_DESTEDIT_ADDMEMBER_SUCCESS = new Localization("command.destedit.addmember.success", "<prefix/><green>You have added <yellow>{player}</yellow> as participant for the destination <yellow>{destination}</yellow>.</green>");
    public static final Localization COMMAND_DESTEDIT_ADDMEMBER_FAILED = new Localization("command.destedit.addmember.failed", "<prefix/><red><yellow>{input}</yellow> is already participant of the destination <yellow>{destination}</yellow>.</red>");
    public static final Localization COMMAND_DESTEDIT_REMOVEMEMBER_SUCCESS = new Localization("command.destedit.removemember.success", "<prefix/><green>You have removed <yellow>{player}</yellow> as participant for the destination <yellow>{destination}</yellow>.</green>");
    public static final Localization COMMAND_DESTEDIT_REMOVEMEMBER_FAILED = new Localization("command.destedit.removemember.failed", "<prefix/><red><yellow>{input}</yellow> is no participant of the destination</red> <yellow>{destination}</yellow>.</red>");
    public static final Localization COMMAND_DESTEDIT_SETOWNER_SUCCESS = new Localization("command.destedit.setowner.success", "<prefix/><green>You have set <yellow>{player}</yellow> as the owner for the destination <yellow>{destination}</yellow>.</green>");
    public static final Localization COMMAND_DESTEDIT_SETPUBLIC_SUCCESS = new Localization("command.destedit.setpublic.success", "<prefix/><green>The destination <yellow>{destination}</yellow> is now <dark_green>public</dark_green>.</green>");
    public static final Localization COMMAND_DESTEDIT_SETPRIVATE_SUCCESS = new Localization("command.destedit.setprivate.success", "<prefix/><green>The destination</green> <yellow>{destination}</yellow> is now <dark_green>private</dark_green>.</green>");
    public static final Localization COMMAND_DESTEDIT_SETLOCATION_SUCCESS = new Localization("command.destedit.setlocation.success", "<prefix/><green>You have updated the position of the destination <yellow>{destination}</yellow>.</green>");
    public static final Localization COMMAND_DESTEDIT_SETWARP_SUCCESS = new Localization("command.destedit.setwarp.success", "<prefix/><green>You have updated the teleport position of the destination <yellow>{destination}</yellow>.</green>");
    public static final Localization COMMAND_DESTEDIT_SETTYPE_SUCCESS = new Localization("command.destedit.settype.success", "<prefix/><green>You have changed the station type of the destination <yellow>{destination}</yellow> to <yellow>{type}</yellow></green>");
    public static final Localization COMMAND_DESTEDIT_UPDATEMARKER_SUCCESS = new Localization("command.destedit.updatemarker.success", "<prefix/><green>Dynmap markers updated. <yellow>{amount}</yellow> Markers were created.</green>");

    public static final Localization COMMAND_MOBENTER_SUCCESS = new Localization("command.mobenter.success", "<prefix/><green><yellow>{amount}</yellow> animals were placed in your train.</green>");
    public static final Localization COMMAND_MOBENTER_FAILED = new Localization("command.mobenter.failed", "<prefix/><red>No animals were found within <yellow>{radius}</yellow> blocks or the train is already full.</red>");
    public static final Localization COMMAND_MOBEJECT_SUCCESS = new Localization("command.mobeject.success", "<prefix/><green>All animals have been ejected from the train.</green>");

    public static final Localization DYNMAP_NOTINSTALLED = new Localization("dynmap.notInstalled", "<red>Dynmap is not loaded.</red>");
    public static final Localization DYNMAP_MARKER = new Localization("dynmap.marker", """
            <div style="z-index:100">
                <div style="padding:6px">
                    <h3 style="padding:0px; margin:0px; color:{color}">{name}</h3>
                    <span style="font-weight:bold; color:#aaaaaa;">Station type: </span>{type}<br>
                    <span style="display: {displayOwner}"><span style="font-weight:bold; color:#aaaaaa;">Owner: </span>{owner}<br></span>
                    <span style="font-style:italic; font-weight:bold; color:#00AA00">{cmd_destination} {name}</span>
                </div>
            </div>""");

    public static final Localization UPDATE_LASTBUILD = new Localization("update.lastBuild", "<prefix/><green>Your installed version is up to date</green>");
    public static final Localization UPDATE_RELEASE = new Localization("update.devBuild", """
            <hover:show_text:'<green>Click here to download this version'><click:open_url:'{url}'><prefix/><green>A new full version was released!</green>
            <prefix/><green>Version: </green><yellow>{version} #{build}</yellow>
            <prefix/><green>FileName: </green><yellow>{fileName}</yellow>
            <prefix/><green>FileSize: </green><yellow>{fileSize}</yellow>
            <prefix/><red>You are on version: </red><yellow>{currentVersion} #{currentBuild}</yellow></click></hover>""");
    public static final Localization UPDATE_DEVBUILD = new Localization("update.release", """
            <hover:show_text:'<green>Click here to download this version'><click:open_url:'{url}'><prefix/><green>A new snapshot build is available!</green>
            <prefix/><green>Version: </green><yellow>{version} #{build}</yellow>
            <prefix/><green>FileName: </green><yellow>{fileName}</yellow>
            <prefix/><green>FileSize: </green><yellow>{fileSize}</yellow>
            <prefix/><red>You are on version: </red><yellow>{currentVersion} #{currentBuild}</yellow></click></hover>""");
    public static final Localization UPDATE_ERROR = new Localization("update.error", "<prefix/><hover:show_text:'<white>{error}'><red>Unable to retrieve update informations</red></hover>");

    private Localization(String name, String defValue) {
        super(name, defValue);
    }

    @Override
    public LocalizationManager getManager() {
        return TCDestinations.plugin.getLocalizationManager();
    }
}
