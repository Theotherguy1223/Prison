package tech.mcprison.prison.spigot.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.mines.PrisonMines;
import tech.mcprison.prison.mines.data.Mine;
import tech.mcprison.prison.modules.Module;
import tech.mcprison.prison.ranks.PrisonRanks;
import tech.mcprison.prison.ranks.data.Rank;
import tech.mcprison.prison.ranks.data.RankLadder;
import tech.mcprison.prison.spigot.SpigotPrison;
import tech.mcprison.prison.spigot.gui.autoFeatures.SpigotAutoBlockGUI;
import tech.mcprison.prison.spigot.gui.autoFeatures.SpigotAutoFeaturesGUI;
import tech.mcprison.prison.spigot.gui.autoFeatures.SpigotAutoPickupGUI;
import tech.mcprison.prison.spigot.gui.autoFeatures.SpigotAutoSmeltGUI;
import tech.mcprison.prison.spigot.gui.mine.SpigotMineInfoGUI;
import tech.mcprison.prison.spigot.gui.mine.SpigotMineNotificationRadiusGUI;
import tech.mcprison.prison.spigot.gui.mine.SpigotMineNotificationsGUI;
import tech.mcprison.prison.spigot.gui.mine.SpigotMineResetTimeGUI;
import tech.mcprison.prison.spigot.gui.mine.SpigotMinesBlocksGUI;
import tech.mcprison.prison.spigot.gui.mine.SpigotMinesConfirmGUI;
import tech.mcprison.prison.spigot.gui.mine.SpigotMinesGUI;
import tech.mcprison.prison.spigot.gui.rank.SpigotLaddersGUI;
import tech.mcprison.prison.spigot.gui.rank.SpigotRankManagerGUI;
import tech.mcprison.prison.spigot.gui.rank.SpigotRankPriceGUI;
import tech.mcprison.prison.spigot.gui.rank.SpigotRankUPCommandsGUI;
import tech.mcprison.prison.spigot.gui.rank.SpigotRanksGUI;


/**
 * @author GABRYCA
 * @author RoyalBlueRanger
 */
public class ListenersPrisonManager implements Listener {

    SpigotPrison plugin;
    public List <String> activeGui = new ArrayList<>();
    public boolean isChatEventActive = false;
    public int id;
    public String rankNameOfChat;

    public ListenersPrisonManager(){}
    public ListenersPrisonManager(SpigotPrison instance){
        plugin = instance;
    }

    @EventHandler
    public void onGuiClosing(InventoryCloseEvent e){

        Player p = (Player) e.getPlayer();

        activeGui.remove(p.getName());
    }

    public void addToGUIBlocker(Player p){
        if(!activeGui.contains(p.getName()))
            activeGui.add(p.getName());
    }

    @EventHandler
    public void onOpenInventory(InventoryOpenEvent e) {

        // Get the player
        Player p = (Player) e.getPlayer();

        // Array with all the Prison titles of Inventories
        String[] titleNames = new String[21];
        titleNames[0] = "AutoFeatures -> AutoBlock";
        titleNames[1] = "PrisonManager -> AutoFeatures";
        titleNames[2] = "AutoFeatures -> AutoPickup";
        titleNames[3] = "AutoFeatures -> AutoSmelt";
        titleNames[4] = "Mines -> MineInfo";
        titleNames[5] = "MineNotifications -> Radius";
        titleNames[6] = "MineInfo -> MineNotifications";
        titleNames[7] = "MinesInfo -> ResetTime";
        titleNames[8] = "MineInfo -> Blocks";
        titleNames[9] = "Mines -> Delete";
        titleNames[10] = "MinesManager -> Mines";
        titleNames[11] = "Mines -> PlayerMines";
        titleNames[12] = "Prestige -> Confirmation";
        titleNames[13] = "RanksManager -> Ladders";
        titleNames[14] = "Prestiges -> PlayerPrestiges";
        titleNames[15] = "Ranks -> PlayerRanks";
        titleNames[16] = "Ranks -> RankManager";
        titleNames[17] = "RankManager -> RankPrice";
        titleNames[18] = "Ladders -> Ranks";
        titleNames[19] = "RankManager -> RankUPCommands";
        titleNames[20] = "PrisonManager";

        // For every title check if equals, the if true add it to the GuiBlocker
        for (String title : titleNames){
            if (e.getView().getTitle().substring(2).equalsIgnoreCase(title)) {

                // Add the player to the list of those who can't move items in the inventory
                addToGUIBlocker(p);

            }
        }
    }

    // On chat event to rename the a Rank Tag
    @EventHandler (priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (isChatEventActive){
            Player p = e.getPlayer();
            String message = e.getMessage();
            Bukkit.getScheduler().cancelTask(id);
            if (message.equalsIgnoreCase("close")){
                isChatEventActive = false;
                p.sendMessage(SpigotPrison.format("&cRename tag closed, nothing got changed"));
                e.setCancelled(true);
            } else {
                Bukkit.getScheduler().runTask(SpigotPrison.getInstance(), () -> Bukkit.getServer().dispatchCommand(p, "ranks set tag " + rankNameOfChat + " " + message));
                e.setCancelled(true);
                isChatEventActive = false;
            }
        }
    }

    // Cancel the events of the active GUI opened from the player
    private void activeGuiEventCanceller(Player p, InventoryClickEvent e){
        if(activeGui.contains(p.getName())) {
            e.setCancelled(true);
        }
    }


    // InventoryClickEvent
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e){

        Player p = (Player) e.getWhoClicked();

        // If you click an empty slot, this should avoid the error
        if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
            activeGuiEventCanceller(p, e);
            return;
        }

        // Get action of the Inventory from the event
        InventoryAction action = e.getAction();

        // If an action equals one of these, and the inventory open from the player equals one of the Prison Title, it'll cancel the event
        if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) || action.equals(InventoryAction.HOTBAR_SWAP) || action.equals(InventoryAction.HOTBAR_MOVE_AND_READD) || action.equals(InventoryAction.NOTHING) || action.equals(InventoryAction.CLONE_STACK) || action.equals(InventoryAction.COLLECT_TO_CURSOR) || action.equals(InventoryAction.DROP_ONE_SLOT) || action.equals(InventoryAction.DROP_ONE_CURSOR) || action.equals(InventoryAction.DROP_ALL_SLOT) || action.equals(InventoryAction.DROP_ALL_CURSOR) || action.equals(InventoryAction.PICKUP_ALL) || action.equals(InventoryAction.PICKUP_HALF) || action.equals(InventoryAction.PICKUP_ONE) || action.equals(InventoryAction.PICKUP_SOME) || action.equals(InventoryAction.PLACE_ALL) || action.equals(InventoryAction.PLACE_ONE) || action.equals(InventoryAction.PLACE_SOME) || action.equals(InventoryAction.SWAP_WITH_CURSOR) || action.equals(InventoryAction.UNKNOWN)) {
            activeGuiEventCanceller(p, e);
        }

        // check if the item has itemMeta
        if (!(e.getCurrentItem().hasItemMeta())){
            return;
        }

        // Get the button name
        String buttonNameMain = e.getCurrentItem().getItemMeta().getDisplayName().substring(2);

        // Split the button name in parts
        String[] parts = buttonNameMain.split(" ");

        // Get ranks module
        Module module = Prison.get().getModuleManager().getModule( PrisonRanks.MODULE_NAME ).orElse( null );

        // Check if the GUI have the right title and do the actions
        switch (e.getView().getTitle().substring(2)) {

            case "PrisonManager":

                // Call the method
                PrisonManagerGUI(e, p, buttonNameMain);

                break;

            // Check the title
            case "RanksManager -> Ladders": {

                // Call the method
                LaddersGUI(e, p, buttonNameMain, module);

                break;
            }

            // Check the title of the inventory and do the actions
            case "Ladders -> Ranks": {

                // Call the method
                RanksGUI(e, p, buttonNameMain);

                break;
            }
            // Check the title and do the actions
            case "Prestiges -> PlayerPrestiges": {

                // Call the method
                PlayerPrestigesGUI(e, p, buttonNameMain);

                break;
            }
            // Check the title and do the actions
            case "Prestige -> Confirmation": {

                // Call the method
                PrestigeConfirmationGUI(e, p, buttonNameMain);

                break;
            }
            // Check the title of the inventory and do things
            case "Ranks -> RankManager": {

                // Call the method
                RankManagerGUI(e, p, parts);

                break;
            }
            // Check the title and do the actions
            case "Ranks -> PlayerRanks":{

                // Call the method
                PlayerRanksGUI(e, p, buttonNameMain);

                break;
            }
            // Check the title and do the actions
            case "RankManager -> RankUPCommands": {

                // Call the method
                RankUPCommandsGUI(e, p, buttonNameMain);

                break;
            }
            // Check the inventory name and do the actions
            case "RankManager -> RankPrice": {

                // Call the method
                RankPriceGUI(e, p, parts);

                break;
            }
            // Check the title and do the actions
            case "MinesManager -> Mines": {

                // Call the method
                MinesGUI(e, p, buttonNameMain);

                break;
            }
            // Check the title and do the actions
            case "Mines -> PlayerMines": {

                // Call the method
                PlayerMinesGUI(p, buttonNameMain);

                break;
            }
            case "Mines -> MineInfo": {

                // Call the method
                MineInfoGUI(e, p, parts);

                break;
            }

            // Check the title of the inventory and do the actions
            case "Mines -> Delete": {

                // Call the method
                MinesDeleteGUI(p, parts);

                break;
            }

            // Check the title of the inventory and do the actions
            case "MineInfo -> Blocks": {

                // Call the method
                BlocksGUI(e, p, parts);

                break;
            }

            // Check the inventory name and do the actions
            case "MinesInfo -> ResetTime": {

                // Call the method
                ResetTimeGUI(e, p, parts);

                break;
            }

            // Check the inventory title and do the actions
            case "MineInfo -> MineNotifications": {

                // Call the method
                MineNotificationsGUI(e, p, parts);

                break;
            }

            // Check the inventory title and do the actions
            case "MineNotifications -> Radius": {

                // Call the method
                RadiusGUI(e, p, parts);

                break;
            }
            // Check the inventory title and do the actions
            case "PrisonManager -> AutoFeatures": {

                // Call the method
                AutoFeaturesGUI(e, p, parts);

                break;
            }

            // Check the title and do the actions
            case "AutoFeatures -> AutoPickup":{

                // Call the method
                AutoPickupGUI(e, p, parts);

                break;
            }

            // Check the title and do the actions
            case "AutoFeatures -> AutoSmelt":{

                // Call the method
                AutoSmeltGUI(e, p, parts);

                break;
            }

            // Check the title and do the actions
            case "AutoFeatures -> AutoBlock":{

                // Call the method
                AutoBlockGUI(e, p, parts);

                break;
            }
        }
    }

    private void PrisonManagerGUI(InventoryClickEvent e, Player p, String buttonNameMain) {

        // Check the Item display name and do open the right GUI
        switch (buttonNameMain) {
            case "Ranks": {
                SpigotLaddersGUI gui = new SpigotLaddersGUI(p);
                gui.open();
                break;
            }

            // Check the Item display name and do open the right GUI
            case "AutoManager": {
                SpigotAutoFeaturesGUI gui = new SpigotAutoFeaturesGUI(p);
                gui.open();
                break;
            }

            // Check the Item display name and do open the right GUI
            case "Mines": {
                SpigotMinesGUI gui = new SpigotMinesGUI(p);
                gui.open();
                break;
            }
        }

        // Cancel the event
        e.setCancelled(true);
    }

    private void LaddersGUI(InventoryClickEvent e, Player p, String buttonNameMain, Module module) {

        // Check if the Ranks module's loaded
        if(!(module instanceof PrisonRanks)){
            p.sendMessage(SpigotPrison.format("&cThe GUI can't open because the &3Rank module &cisn't loaded"));
            p.closeInventory();
            e.setCancelled(true);
            return;
        }

        // Get the ladder by the name of the button got before
        Optional<RankLadder> ladder = PrisonRanks.getInstance().getLadderManager().getLadder(buttonNameMain);

        // Check if the ladder exist, everything can happen but this shouldn't
        if (!ladder.isPresent()) {
            p.sendMessage("What did you actually click? Sorry ladder not found.");
            return;
        }

        // When the player click an item with shift and right click, e.isShiftClick should be enough but i want
        // to be sure's a right click
        if (e.isShiftClick() && e.isRightClick()) {

            // Execute the command
            Bukkit.dispatchCommand(p, "ranks ladder delete " + buttonNameMain);
            e.setCancelled(true);
            p.closeInventory();
            SpigotLaddersGUI gui = new SpigotLaddersGUI(p);
            gui.open();
            return;

        }

        // Open the GUI of ranks
        SpigotRanksGUI gui = new SpigotRanksGUI(p, ladder);
        gui.open();

        // Cancel the event
        e.setCancelled(true);
    }

    private void RanksGUI(InventoryClickEvent e, Player p, String buttonNameMain) {

        // Get the rank
        Optional<Rank> rankOptional = PrisonRanks.getInstance().getRankManager().getRank(buttonNameMain);

        // Check if the rank exist
        if (!rankOptional.isPresent()) {
            p.sendMessage(SpigotPrison.format("&cThe rank " + buttonNameMain + " does not exist."));
            return;
        }

        // Get the rank
        Rank rank = rankOptional.get();

        // Check clicks
        if (e.isShiftClick() && e.isRightClick()) {

            // Execute the command
            Bukkit.dispatchCommand(p, "ranks delete " + buttonNameMain);
            e.setCancelled(true);
            p.closeInventory();
            return;

        } else {

            // Open a GUI
            SpigotRankManagerGUI gui = new SpigotRankManagerGUI(p, rank);
            gui.open();

        }

        // Cancel the event
        e.setCancelled(true);
    }

    private void PlayerPrestigesGUI(InventoryClickEvent e, Player p, String buttonNameMain) {

        // Check the button name and do the actions
        if (buttonNameMain.equals(SpigotPrison.format("Prestige").substring(2))){
            // Close the inventory
            p.closeInventory();
            // Execute the command
            Bukkit.dispatchCommand(p, "prestige");
        }

        // Cancel the event
        e.setCancelled(true);
    }

    private void PrestigeConfirmationGUI(InventoryClickEvent e, Player p, String buttonNameMain) {

        // Check the button name and do the actions
        if (buttonNameMain.equals(SpigotPrison.format("Confirm: Prestige").substring(2))){
            // Execute the command
            Bukkit.dispatchCommand(p, "rankup prestiges");
        } else if (buttonNameMain.equals((SpigotPrison.format("Cancel: Don't Prestige").substring(2)))){
            // Send a message to the player
            p.sendMessage(SpigotPrison.format("&7[&3Info&7] &cCancelled"));
            // Close the inventory
            p.closeInventory();
        }

        // Cancel the event
        e.setCancelled(true);
    }

    private void RankManagerGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Output finally the buttonname and the minename explicit out of the array
        String buttonname = parts[0];
        String rankName = parts[1];

        // Get the rank
        Optional<Rank> rankOptional = PrisonRanks.getInstance().getRankManager().getRank(rankName);

        // Check the button name and do the actions
        if (buttonname.equalsIgnoreCase("RankupCommands")){

            // Check if the rank exist
            if (!rankOptional.isPresent()) {
                // Send a message to the player
                p.sendMessage(SpigotPrison.format("&c[ERROR] The rank " + rankName + " does not exist."));
                return;
            }

            // Get the rank
            Rank rank = rankOptional.get();

            // Check the rankupCommand of the Rank
            if (rank.rankUpCommands == null) {
                // Send a message to the player
                p.sendMessage(SpigotPrison.format("&c[ERROR] There aren't commands for this rank anymore."));
            }

            // Open the GUI of commands
            else {
                SpigotRankUPCommandsGUI gui = new SpigotRankUPCommandsGUI(p, rank);
                gui.open();
            }

        // Check the button name and do the actions
        } else if (buttonname.equalsIgnoreCase("RankPrice")){

            // Check and open a GUI
            if(rankOptional.isPresent()) {
                SpigotRankPriceGUI gui = new SpigotRankPriceGUI(p, (int) rankOptional.get().cost, rankOptional.get().name);
                gui.open();
            }

        // Check the button name and do the actions
        } else if (buttonname.equalsIgnoreCase("RankTag")){

            // Send messages to the player
            p.sendMessage(SpigotPrison.format("&7[&3Info&7] &3Please write the &6tag &3you'd like to use and &6submit&3."));
            p.sendMessage(SpigotPrison.format("&7[&3Info&7] &3Input &cclose &3to cancel or wait &c30 seconds&3."));
            // Start the async task
            isChatEventActive = true;
            rankNameOfChat = rankName;
            id = Bukkit.getScheduler().scheduleSyncDelayedTask(SpigotPrison.getInstance(), () -> {
                isChatEventActive = false;
                p.sendMessage(SpigotPrison.format("&cYou ran out of time, tag not changed."));
            }, 20L * 30);
            p.closeInventory();
        }

        // Cancel the event
        e.setCancelled(true);
    }

    private void PlayerRanksGUI(InventoryClickEvent e, Player p, String buttonNameMain) {

        // Load config
        Configuration GuiConfig = SpigotPrison.getGuiConfig();

        // Check the buttonName and do the actions
        if (buttonNameMain.equals(SpigotPrison.format(GuiConfig.getString("Gui.Lore.Rankup").substring(2)))){
            Bukkit.dispatchCommand(p, "rankup " + GuiConfig.getString("Options.Ranks.Ladder"));
            p.closeInventory();
        }

        // Cancel the event
        e.setCancelled(true);
    }

    private void RankUPCommandsGUI(InventoryClickEvent e, Player p, String buttonNameMain) {

        // Check the clickType
        if (e.isShiftClick() && e.isRightClick()) {

            // Execute the command
            Bukkit.dispatchCommand(p, "ranks command remove " + buttonNameMain);
            // Cancel the event
            e.setCancelled(true);
            // Close the inventory
            p.closeInventory();
            return;

        }

        // Cancel the event
        e.setCancelled(true);
    }

    private void RankPriceGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Rename the parts
        String part1 = parts[0];
        String part2 = parts[1];
        String part3 = parts[2];

        // Initialize the variable
        int decreaseOrIncreaseValue = 0;

        // If there're enough parts init another variable
        if (parts.length == 4){
            decreaseOrIncreaseValue = Integer.parseInt(parts[3]);
        }

        // Check the button name and do the actions
        if (part1.equalsIgnoreCase("Confirm:")) {

            // Check the click type and do the actions
            if (e.isLeftClick()){

                // Execute the command
                Bukkit.dispatchCommand(p,"ranks set cost " + part2 + " " + part3);

                // Close the inventory
                p.closeInventory();

                return;

                // Check the click type and do the actions
            } else if (e.isRightClick()){

                // Send a message to the player
                p.sendMessage(SpigotPrison.format("&cEvent cancelled."));

                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;
            } else {

                // Cancel the event
                e.setCancelled(true);
                return;
            }
        }

        // Give to val a value
        int val = Integer.parseInt(part2);

        // Check the calculator symbol
        if (part3.equals("-")){

            // Check if the value's already too low
            if (!((val -  decreaseOrIncreaseValue) < 0)) {

                // If it isn't too low then decrease it
                val = val - decreaseOrIncreaseValue;

                // If it is too low
            } else {

                // Tell to the player that the value's too low
                p.sendMessage(SpigotPrison.format("&cToo low value."));

                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();
                return;
            }

            // Open an updated GUI after the value changed
            SpigotRankPriceGUI gui = new SpigotRankPriceGUI(p, val, part1);
            gui.open();

            // Check the calculator symbol
        } else if (part3.equals("+")){

            // Check if the value isn't too high
            if (!((val + decreaseOrIncreaseValue) > 2147483646)) {

                // Increase the value
                val = val + decreaseOrIncreaseValue;

                // If the value's too high then do the action
            } else {

                // Close the GUI and tell it to the player
                p.sendMessage(SpigotPrison.format("&cToo high value."));
                e.setCancelled(true);
                p.closeInventory();
                return;
            }

            // Open a new updated GUI with new values
            SpigotRankPriceGUI gui = new SpigotRankPriceGUI(p, val, part1);
            gui.open();

        }
    }

    private void MinesGUI(InventoryClickEvent e, Player p, String buttonNameMain) {

        // Variables
        PrisonMines pMines = PrisonMines.getInstance();
        Mine m = pMines.getMine(buttonNameMain);

        // Check the clicks
        if (e.isShiftClick() && e.isRightClick()) {
            // Execute the command
            Bukkit.dispatchCommand(p, "mines delete " + buttonNameMain);
            // Cancel the event
            e.setCancelled(true);
            // Close the inventory
            p.closeInventory();
            // Open a GUI
            SpigotMinesConfirmGUI gui = new SpigotMinesConfirmGUI(p, buttonNameMain);
            gui.open();
            return;
        }

        // Open the GUI of mines info
        SpigotMineInfoGUI gui = new SpigotMineInfoGUI(p, m, buttonNameMain);
        gui.open();

        // Cancel the event
        e.setCancelled(true);
    }

    private void PlayerMinesGUI(Player p, String buttonNameMain) {

        // Load config
        Configuration GuiConfig = SpigotPrison.getGuiConfig();

        if (p.hasPermission(SpigotPrison.format(GuiConfig.getString("Options.Mines.PermissionWarpPlugin") + buttonNameMain))){
            Bukkit.dispatchCommand(p, SpigotPrison.format(GuiConfig.getString("Options.Mines.CommandWarpPlugin") + " " + buttonNameMain));
        }
    }

    private void MineInfoGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Output finally the buttonname and the minename explicit out of the array
        String buttonname = parts[0];
        String mineName = parts[1];

        // Check the name of the button and do the actions
        switch (buttonname) {
            case "Blocks_of_the_Mine:":

                // Open the GUI
                SpigotMinesBlocksGUI gui = new SpigotMinesBlocksGUI(p, mineName);
                gui.open();

                break;

            // Check the name of the button and do the actions
            case "Reset_Mine:":

                // Check the clickType and do the actions
                if (e.isLeftClick()) {
                    // Execute the command
                    Bukkit.dispatchCommand(p, "mines reset " + mineName);
                } else if (e.isRightClick()){
                    // Execute the command
                    Bukkit.dispatchCommand(p, "mines set skipReset " + mineName);
                } else if (e.isRightClick() && e.isShiftClick()){
                    // Execute the command
                    Bukkit.dispatchCommand(p, "mines set zeroBlockResetDelay " + mineName);
                }

                // Cancel the event
                e.setCancelled(true);

                break;

            // Check the name of the button and do the actions
            case "Mine_Spawn:":

                // Execute the command
                Bukkit.dispatchCommand(p, "mines set spawn " + mineName);

                // Cancel the event
                e.setCancelled(true);
                break;

            // Check the name of the button and do the actions
            case "Mine_notifications:":

                // Open the GUI
                SpigotMineNotificationsGUI gui1 = new SpigotMineNotificationsGUI(p, mineName);
                gui1.open();

                break;

            // Check the name of the button and do the actions
            case "TP_to_the_Mine:":

                // Close the inventory
                p.closeInventory();

                // Execute the Command
                Bukkit.dispatchCommand(p, "mines tp " + mineName);

                break;

            // Check the name of the button and do the actions
            case "Reset_Time:":

                // Initialize the variables
                PrisonMines pMines = PrisonMines.getInstance();
                Mine m = pMines.getMine(mineName);
                int val = m.getResetTime();

                // Open the GUI
                SpigotMineResetTimeGUI gui2 = new SpigotMineResetTimeGUI(p, val, mineName);
                gui2.open();

                break;
        }
    }

    private void MinesDeleteGUI(Player p, String[] parts) {

        // Output finally the buttonname and the minename explicit out of the array
        String buttonname = parts[0];
        String mineName = parts[1];

        // Check the name of the button and do the actions
        if (buttonname.equals("Confirm:")) {

            // Confirm
            Bukkit.dispatchCommand(p, "mines delete " + mineName + " confirm");

            // Close the Inventory
            p.closeInventory();

        // Check the name of the button and do the actions
        } else if (buttonname.equals("Cancel:")) {

            // Cancel
            Bukkit.dispatchCommand(p, "mines delete " + mineName + " cancel");

            // Close the inventory
            p.closeInventory();

        }
    }

    private void BlocksGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Output finally the buttonname and the minename explicit out of the array
        String buttonname = parts[0];
        String mineName = parts[1];

        // Check the click Type and do the actions
        if (e.isShiftClick() && e.isRightClick()) {

            // Execute the command
            Bukkit.dispatchCommand(p, "mines block remove " + mineName + " " + buttonname.substring(0, buttonname.length() - 1));

            // Cancel the event
            e.setCancelled(true);

            // Close the GUI so it can be updated
            p.closeInventory();

            // Open the GUI
            SpigotMinesBlocksGUI gui = new SpigotMinesBlocksGUI(p, mineName);
            gui.open();
            return;
        }

        // Cancel the event
        e.setCancelled(true);
    }

    private void ResetTimeGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Rename the parts
        String part1 = parts[0];
        String part2 = parts[1];
        String part3 = parts[2];

        // Initialize the variable
        int decreaseOrIncreaseValue = 0;

        // If there're enough parts init another variable
        if (parts.length == 4){
            decreaseOrIncreaseValue = Integer.parseInt(parts[3]);
        }

        // Check the button name and do the actions
        if (part1.equalsIgnoreCase("Confirm:")) {

            // Check the click type and do the actions
            if (e.isLeftClick()){

                // Execute the command
                Bukkit.dispatchCommand(p,"mines set resettime " + part2 + " " + part3);

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;

            // Check the click type and do the actions
            } else if (e.isRightClick()){

                // Send a message to the player
                p.sendMessage(SpigotPrison.format("&cEvent cancelled."));

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;
            } else {

                // Cancel the event
                e.setCancelled(true);
                return;
            }
        }

        // Give to val a value
        int val = Integer.parseInt(part2);

        // Check the calculator symbol
        if (part3.equals("-")){

            // Check if the value's already too low
            if (!((val -  decreaseOrIncreaseValue) < 0)) {

                // If it isn't too low then decrease it
                val = val - decreaseOrIncreaseValue;

            // If it is too low
            } else {

                // Tell to the player that the value's too low
                p.sendMessage(SpigotPrison.format("&cToo low value."));

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;
            }

            // Open an updated GUI after the value changed
            SpigotMineResetTimeGUI gui = new SpigotMineResetTimeGUI(p, val, part1);
            gui.open();

        // Check the calculator symbol
        } else if (part3.equals("+")){

            // Check if the value isn't too high
            if (!((val + decreaseOrIncreaseValue) > 999999)) {

                // Increase the value
                val = val + decreaseOrIncreaseValue;

            // If the value's too high then do the action
            } else {

                // Close the GUI and tell it to the player
                p.sendMessage(SpigotPrison.format("&cToo high value."));

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;
            }

            // Open a new updated GUI with new values
            SpigotMineResetTimeGUI gui = new SpigotMineResetTimeGUI(p, val, part1);
            gui.open();

            // Cancel the event
            e.setCancelled(true);

        }
    }

    private void MineNotificationsGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Output finally the buttonname and the minename explicit out of the array
        String buttonname = parts[0];
        String mineName = parts[1];
        String typeNotification;
        long val;

        // Init variables
        PrisonMines pMines = PrisonMines.getInstance();
        Mine m = pMines.getMine(mineName);

        // Check the button name and do the actions
        if (buttonname.equalsIgnoreCase("Within_Mode:")){

            // Change the value of the variable
            typeNotification = "within";

            // Execute command
            Bukkit.dispatchCommand(p, "mines set notification " + mineName + " " + typeNotification + " " + "0");

            // Cancel the event and close the inventory
            e.setCancelled(true);
            p.closeInventory();

            // Check the button name and do the actions
        } else if (buttonname.equalsIgnoreCase("Radius_Mode:")){

            // Change the value of the variable
            typeNotification = "radius";

            // Get the variable value
            val = m.getNotificationRadius();

            // Open the GUI
            SpigotMineNotificationRadiusGUI gui = new SpigotMineNotificationRadiusGUI(p, val,  typeNotification, mineName);
            gui.open();

        // Check the button name and do the actions
        } else if (buttonname.equalsIgnoreCase("Disabled_Mode:")){

            // Change the value of the variable
            typeNotification = "disabled";

            // Execute the command
            Bukkit.dispatchCommand(p, "mines set notification " + mineName + " " + typeNotification + " " + "0");

            // Cancel the event and close the inventory
            e.setCancelled(true);
            p.closeInventory();

        }
    }

    private void RadiusGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Rename the variables
        String part1 = parts[0];
        String part2 = parts[1];
        String part3 = parts[2];
        String typeNotification;

        // Init the variable
        int decreaseOrIncreaseValue = 0;

        // Check the button name and do the actions
        if (!(part1.equalsIgnoreCase("Confirm:"))){

            // Give them a value
            decreaseOrIncreaseValue = Integer.parseInt(parts[3]);
            typeNotification = parts[4];

        // Do others actions
        } else {

            // Give it a value
            typeNotification = parts[3];
        }

        // Check the button name and do the actions
        if (part1.equalsIgnoreCase("Confirm:")) {

            // Check the click type
            if (e.isLeftClick()){

                // Execute the command
                Bukkit.dispatchCommand(p,"mines set notification " + part2 + " " + typeNotification + " " + part3);

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;
            } else if (e.isRightClick()){

                // Close the inventory
                p.sendMessage(SpigotPrison.format("&cEvent cancelled."));

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();

                return;
            } else {

                // Cancel the event
                e.setCancelled(true);
                return;
            }
        }

        // Init a new value
        long val = Integer.parseInt(part2);

        // Check the calculator symbol
        if (part3.equals("-")){

            // Check if the value's too low
            if (!((val -  decreaseOrIncreaseValue) < 0)) {

                // Decrease the value
                val = val - decreaseOrIncreaseValue;

            // If the value's too low
            } else {

                // Close the inventory and tell it the player
                p.sendMessage(SpigotPrison.format("&cToo low value."));

                // Cancel the event
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();
                return;
            }

            // Open a new updated GUI with new values
            SpigotMineNotificationRadiusGUI gui = new SpigotMineNotificationRadiusGUI(p, val,  typeNotification, part1);
            gui.open();

        // Check the calculator symbol
        } else if (part3.equals("+")){

            // Check if the value's too high
            if (!((val + decreaseOrIncreaseValue) > 9999999)) {

                // Increase the value
                val = val + decreaseOrIncreaseValue;

            // If the value's too high
            } else {

                // Close the inventory and tell it to the player
                p.sendMessage(SpigotPrison.format("&cToo high value."));

                // Cancel the inventory
                e.setCancelled(true);

                // Close the inventory
                p.closeInventory();
                return;
            }

            // Open a new updated GUI with new values
            SpigotMineNotificationRadiusGUI gui = new SpigotMineNotificationRadiusGUI(p, val,  typeNotification, part1);
            gui.open();

        }
    }

    private void AutoFeaturesGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Get the config
        FileConfiguration configThings = SpigotPrison.getInstance().getAutoFeatures().getAutoFeaturesConfig();

        // Output finally the buttonname and the mode explicit out of the array
        String buttonname = parts[0];
        String mode = parts[1];

        // Check the buttonName and do the actionsm also check the clickType etc
        if (buttonname.equalsIgnoreCase("Full_Inv_Play_Sound")){
            if (mode.equalsIgnoreCase("Enabled")){
                if (e.isRightClick() && e.isShiftClick()){
                    configThings.set("Options.General.playSoundIfInventoryIsFull", false);
                    saveConfigAutoFeatures(e, p);
                }
            } else if (mode.equalsIgnoreCase("Disabled")){
                if (e.isRightClick()){
                    configThings.set("Options.General.playSoundIfInventoryIsFull", true);
                    saveConfigAutoFeatures(e, p);
                }
            }
        } else if (buttonname.equalsIgnoreCase("Full_Inv_Hologram")){
            if (mode.equalsIgnoreCase("Enabled")){
                if (e.isRightClick() && e.isShiftClick()){
                    configThings.set("Options.General.hologramIfInventoryIsFull", false);
                    saveConfigAutoFeatures(e, p);
                }
            } else if (mode.equalsIgnoreCase("Disabled")){
                if (e.isRightClick()){
                    configThings.set("Options.General.hologramIfInventoryIsFull", true);
                    saveConfigAutoFeatures(e, p);
                }
            }
        } else if (buttonname.equalsIgnoreCase("All")){
            if (mode.equalsIgnoreCase("Enabled")){
                if (e.isRightClick() && e.isShiftClick()){
                    configThings.set("Options.General.AreEnabledFeatures", false);
                    saveConfigAutoFeatures(e, p);
                }
            } else if (mode.equalsIgnoreCase("Disabled")){
                if (e.isRightClick()){
                    configThings.set("Options.General.AreEnabledFeatures", true);
                    saveConfigAutoFeatures(e, p);
                }
            }
        } else if (buttonname.equalsIgnoreCase("AutoPickup")){
            if (mode.equalsIgnoreCase("Enabled")){
                if (e.isRightClick() && e.isShiftClick()){
                    configThings.set("Options.AutoPickup.AutoPickupEnabled", false);
                    saveConfigAutoFeatures(e, p);
                } else if (e.isLeftClick()){
                    SpigotAutoPickupGUI gui = new SpigotAutoPickupGUI(p);
                    gui.open();
                }
            } else if (mode.equalsIgnoreCase("Disabled")){
                if (e.isRightClick()){
                    configThings.set("Options.AutoPickup.AutoPickupEnabled", true);
                    saveConfigAutoFeatures(e, p);
                } else if (e.isLeftClick()){
                SpigotAutoPickupGUI gui = new SpigotAutoPickupGUI(p);
                gui.open();
                }
            }
        } else if (buttonname.equalsIgnoreCase("AutoSmelt")){
            if (mode.equalsIgnoreCase("Enabled")){
                if (e.isRightClick() && e.isShiftClick()){
                    configThings.set("Options.AutoSmelt.AutoSmeltEnabled", false);
                    saveConfigAutoFeatures(e, p);
                } else if (e.isLeftClick()){
                    SpigotAutoSmeltGUI gui = new SpigotAutoSmeltGUI(p);
                    gui.open();
                }
            } else if (mode.equalsIgnoreCase("Disabled")){
                if (e.isRightClick()){
                    configThings.set("Options.AutoSmelt.AutoSmeltEnabled", true);
                    saveConfigAutoFeatures(e, p);
                } else if (e.isLeftClick()){
                    SpigotAutoSmeltGUI gui = new SpigotAutoSmeltGUI(p);
                    gui.open();
                }
            }
        } else if (buttonname.equalsIgnoreCase("AutoBlock")){
            if (mode.equalsIgnoreCase("Enabled")){
                if (e.isRightClick() && e.isShiftClick()){
                    configThings.set("Options.AutoBlock.AutoBlockEnabled", false);
                    saveConfigAutoFeatures(e, p);
                } else if (e.isLeftClick()){
                    SpigotAutoBlockGUI gui = new SpigotAutoBlockGUI(p);
                    gui.open();
                }
            } else if (mode.equalsIgnoreCase("Disabled")){
                if (e.isRightClick()){
                    configThings.set("Options.AutoBlock.AutoBlockEnabled", true);
                    saveConfigAutoFeatures(e, p);
                } else if (e.isLeftClick()){
                    SpigotAutoBlockGUI gui = new SpigotAutoBlockGUI(p);
                    gui.open();
                }
            }
        }
        e.setCancelled(true);
    }

    private void AutoPickupGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Get the config
        FileConfiguration configThings = SpigotPrison.getInstance().getAutoFeatures().getAutoFeaturesConfig();

        // Output finally the buttonname and the mode explicit out of the array
        String buttonname = parts[0];
        String mode = parts[1];

        // Check the mode and do the actions
        if (mode.equalsIgnoreCase("Enabled")){

            // Check the click and do the actions, also the buttonName
            if (e.isRightClick() && e.isShiftClick()){

                if (buttonname.equalsIgnoreCase("All_Blocks")){
                    configThings.set("Options.AutoPickup.AutoPickupAllBlocks", false);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Gold_Ore")){
                    configThings.set("Options.AutoPickup.AutoPickupGoldOre", false);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Iron_Ore")){
                    configThings.set("Options.AutoPickup.AutoPickupIronOre", false);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Coal_Ore")){
                    configThings.set("Options.AutoPickup.AutoPickupCoalOre", false);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Diamond_Ore")){
                    configThings.set("Options.AutoPickup.AutoPickupDiamondOre", false);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Redstone_Ore")){
                    configThings.set("Options.AutoPickup.AutoPickupRedstoneOre", false);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Emerald_Ore")){
                    configThings.set("Options.AutoPickup.AutoPickupEmeraldOre", false);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Quartz_Ore")){
                    configThings.set("Options.AutoPickup.AutoPickupQuartzOre", false);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Lapis_Ore")){
                    configThings.set("Options.AutoPickup.AutoPickupLapisOre", false);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Snow_Ball")){
                    configThings.set("Options.AutoPickup.AutoPickupSnowBall", false);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Glowstone_Dust")){
                    configThings.set("Options.AutoPickup.AutoPickupGlowstoneDust", false);
                    saveConfigPickup(e, p);
                }

            }

            // Cancel the event
            e.setCancelled(true);

        // Check the mode and do the actions
        } else if (mode.equalsIgnoreCase("Disabled")){

            // Check the clickType and the buttonName
            if (e.isRightClick()){

                if (buttonname.equalsIgnoreCase("All_Blocks")){
                    configThings.set("Options.AutoPickup.AutoPickupAllBlocks", true);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Gold_Ore")){
                    configThings.set("Options.AutoPickup.AutoPickupGoldOre", true);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Iron_Ore")){
                    configThings.set("Options.AutoPickup.AutoPickupIronOre", true);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Coal_Ore")){
                    configThings.set("Options.AutoPickup.AutoPickupCoalOre", true);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Diamond_Ore")){
                    configThings.set("Options.AutoPickup.AutoPickupDiamondOre", true);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Redstone_Ore")){
                    configThings.set("Options.AutoPickup.AutoPickupRedstoneOre", true);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Emerald_Ore")){
                    configThings.set("Options.AutoPickup.AutoPickupEmeraldOre", true);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Quartz_Ore")){
                    configThings.set("Options.AutoPickup.AutoPickupQuartzOre", true);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Lapis_Ore")){
                    configThings.set("Options.AutoPickup.AutoPickupLapisOre", true);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Snow_Ball")){
                    configThings.set("Options.AutoPickup.AutoPickupSnowBall", true);
                    saveConfigPickup(e, p);
                }

                if (buttonname.equalsIgnoreCase("Glowstone_Dust")){
                    configThings.set("Options.AutoPickup.AutoPickupGlowstoneDust", true);
                    saveConfigPickup(e, p);
                }

            }

            // Cancel the event
            e.setCancelled(true);

        }
    }

    private void AutoSmeltGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Get the config
        FileConfiguration configThings = SpigotPrison.getInstance().getAutoFeatures().getAutoFeaturesConfig();

        // Output finally the buttonname and the mode explicit out of the array
        String buttonname = parts[0];
        String mode = parts[1];

        // Check the mode and do the actions
        if (mode.equalsIgnoreCase("Enabled")){

            // Check the clickType and do the actions
            if (e.isRightClick() && e.isShiftClick()){

                if (buttonname.equalsIgnoreCase("Gold_Ore")){
                    configThings.set("Options.AutoSmelt.AutoSmeltGoldOre", false);
                    saveConfigSmelt(e, p);
                }

                if (buttonname.equalsIgnoreCase("Iron_Ore")){
                    configThings.set("Options.AutoSmelt.AutoSmeltIronOre", false);
                    saveConfigSmelt(e, p);
                }

                if (buttonname.equalsIgnoreCase("All_Ores")){
                    configThings.set("Options.AutoSmelt.AutoSmeltAllBlocks", false);
                    saveConfigSmelt(e, p);
                }

            }

            // Cancel the event
            e.setCancelled(true);

        // Check the mode and do the actions
        } else if (mode.equalsIgnoreCase("Disabled")){

            // Check the clickType and do the actions
            if (e.isRightClick()){

                if (buttonname.equalsIgnoreCase("Gold_Ore")){
                    configThings.set("Options.AutoSmelt.AutoSmeltGoldOre", true);
                    saveConfigSmelt(e, p);
                }

                if (buttonname.equalsIgnoreCase("Iron_Ore")){
                    configThings.set("Options.AutoSmelt.AutoSmeltIronOre", true);
                    saveConfigSmelt(e, p);
                }

                if (buttonname.equalsIgnoreCase("All_Ores")){
                    configThings.set("Options.AutoSmelt.AutoSmeltAllBlocks", true);
                    saveConfigSmelt(e, p);
                }

            }

            // Cancel the event
            e.setCancelled(true);

        }
    }

    private void AutoBlockGUI(InventoryClickEvent e, Player p, String[] parts) {

        // Get the config
        FileConfiguration configThings = SpigotPrison.getInstance().getAutoFeatures().getAutoFeaturesConfig();

        // Output finally the buttonname and the mode explicit out of the array
        String buttonname = parts[0];
        String mode = parts[1];

        // Check the mode and do the actions
        if (mode.equalsIgnoreCase("Enabled")){

            // Check the clickType and do the actions
            if (e.isRightClick() && e.isShiftClick()){

                if (buttonname.equalsIgnoreCase("Gold_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockGoldBlock", false);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Iron_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockIronBlock", false);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Coal_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockCoalBlock", false);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Diamond_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockDiamondBlock", false);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Redstone_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockRedstoneBlock", false);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Emerald_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockEmeraldBlock", false);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Quartz_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockQuartzBlock", false);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Prismarine_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockPrismarineBlock", false);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Lapis_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockLapisBlock", false);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Snow_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockSnowBlock", false);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Glowstone_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockGlowstone", false);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("All_Blocks")){
                    configThings.set("Options.AutoBlock.AutoBlockAllBlocks", false);
                    saveConfigBlock(e, p);
                }

            }

            // Cancel the event
            e.setCancelled(true);

        // Check the mode and do the actions
        } else if (mode.equalsIgnoreCase("Disabled")){

            // Check the clickType and do the actions
            if (e.isRightClick()){

                if (buttonname.equalsIgnoreCase("Gold_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockGoldBlock", true);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Iron_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockIronBlock", true);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Coal_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockCoalBlock", true);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Diamond_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockDiamondBlock", true);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Redstone_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockRedstoneBlock", true);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Emerald_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockEmeraldBlock", true);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Quartz_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockQuartzBlock", true);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Prismarine_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockPrismarineBlock", true);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Lapis_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockLapisBlock", true);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Snow_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockSnowBlock", true);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("Glowstone_Block")){
                    configThings.set("Options.AutoBlock.AutoBlockGlowstone", true);
                    saveConfigBlock(e, p);
                }

                if (buttonname.equalsIgnoreCase("All_Blocks")){
                    configThings.set("Options.AutoBlock.AutoBlockAllBlocks", true);
                    saveConfigBlock(e, p);
                }

            }

            // Cancel the event
            e.setCancelled(true);

        }
    }


    /**
     * Save the auto features, and then cancel the event and close the inventory.
     * 
     * @param e
     * @param player
     */
    private void saveAutoFeatures( InventoryClickEvent e, Player player ) {
    	SpigotPrison.getInstance().getAutoFeatures().saveAutoFeaturesConfig();
    	e.setCancelled(true);
    	player.closeInventory();
    }

    
    private void saveConfigBlock(InventoryClickEvent e, Player p) {
        saveAutoFeatures( e, p );
        SpigotAutoBlockGUI gui = new SpigotAutoBlockGUI(p);
        gui.open();
    }

    private void saveConfigSmelt(InventoryClickEvent e, Player p) {
        saveAutoFeatures( e, p );
        SpigotAutoSmeltGUI gui = new SpigotAutoSmeltGUI(p);
        gui.open();
    }

    private void saveConfigPickup(InventoryClickEvent e, Player p) {
        saveAutoFeatures( e, p );
        SpigotAutoPickupGUI gui = new SpigotAutoPickupGUI(p);
        gui.open();
    }

    private void saveConfigAutoFeatures(InventoryClickEvent e, Player p) {
        saveAutoFeatures( e, p );
        SpigotAutoFeaturesGUI gui = new SpigotAutoFeaturesGUI(p);
        gui.open();
    }
}