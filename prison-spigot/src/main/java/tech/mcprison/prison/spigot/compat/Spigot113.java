package tech.mcprison.prison.spigot.compat;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import tech.mcprison.prison.spigot.SpigotUtil;
import tech.mcprison.prison.spigot.block.SpigotItemStack;

public class Spigot113
	extends Spigot113GUI
	implements Compatibility {

    @Override 
    public EquipmentSlot getHand(PlayerInteractEvent e) {
        if (e.getHand() == null) {
            return null;
        } else {
            return EquipmentSlot.valueOf(e.getHand().name());
        }
    }

    @Override 
    public ItemStack getItemInMainHand(PlayerInteractEvent e) {
        return e.getPlayer().getInventory().getItemInMainHand();
    }

    @Override 
    public ItemStack getItemInMainHand(Player player) {
    	return player.getInventory().getItemInMainHand();
    }
    
    
    public SpigotItemStack getPrisonItemInMainHand(PlayerInteractEvent e) {
    	return SpigotUtil.bukkitItemStackToPrison( getItemInMainHand( e ) );
    }
    
    public SpigotItemStack getPrisonItemInMainHand(Player player) {
    	return SpigotUtil.bukkitItemStackToPrison( getItemInMainHand( player ) );
    }
    
    
    @Override 
    public void playIronDoorSound(Location loc) {
        loc.getWorld().playEffect(loc, Effect.IRON_DOOR_TOGGLE, null);
    }
    

	@Override
	public void breakItemInMainHand( Player player ) {
		player.getInventory().setItemInMainHand( null );
		
		player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 0.85F); 
	}
}
