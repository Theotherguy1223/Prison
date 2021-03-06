package tech.mcprison.prison.spigot.gui.sellall;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import tech.mcprison.prison.spigot.SpigotPrison;
import tech.mcprison.prison.spigot.SpigotUtil;
import tech.mcprison.prison.spigot.gui.SpigotGUIComponents;

import java.util.List;
import java.util.Set;

/**
 * @author GABRYCA
 */
public class SellAllPlayerGUI extends SpigotGUIComponents {

    private final Player p;
    private final Configuration messages = messages();
    private final Configuration conf = sellAll();

    public SellAllPlayerGUI(Player p){
        this.p = p;
    }

    public void open() {

        Inventory inv;

        if (guiBuilder()) return;

        inv = buttonsSetup();
        if (inv == null) return;

        openGUI(p, inv);
    }

    private Inventory buttonsSetup() {

        Inventory inv;
        boolean emptyInv = false;

        try {
            if (conf.getConfigurationSection("Items") == null) {
                emptyInv = true;
            }
        } catch (NullPointerException e){
            emptyInv = true;
        }

        if (emptyInv){
            p.sendMessage(SpigotPrison.format(messages.getString("Message.NoSellAllItems")));
            p.closeInventory();
            return null;
        }

        // Get the Items config section
        Set<String> items = conf.getConfigurationSection("Items").getKeys(false);

        // Get the dimensions and if needed increases them
        int dimension = (int) Math.ceil(items.size() / 9D) * 9;

        if (dimension > 54){
            p.sendMessage(SpigotPrison.format(messages.getString("Message.TooManySellAllItems")));
            return null;
        }

        inv = Bukkit.createInventory(null, dimension, SpigotPrison.format("&3Prison -> SellAll-Player"));

        for (String key : items) {
            List<String> itemsLore = createLore(
                    messages.getString("Lore.Value") + conf.getString("Items." + key + ".ITEM_VALUE")
            );

            ItemStack item = createButton(SpigotUtil.getItemStack(SpigotUtil.getXMaterial(conf.getString("Items." + key + ".ITEM_ID")), 1), itemsLore, SpigotPrison.format("&3" + conf.getString("Items." + key + ".ITEM_ID")));
            inv.addItem(item);
        }
        return inv;
    }

    private boolean guiBuilder() {
        try {
            buttonsSetup();
        } catch (NullPointerException ex){
            p.sendMessage(SpigotPrison.format("&cThere's a null value in the GuiConfig.yml [broken]"));
            ex.printStackTrace();
            return true;
        }
        return false;
    }

}
