package tech.mcprison.prison.spigot.block;

import java.util.Optional;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.vk2gpz.tokenenchant.event.TEBlockExplodeEvent;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.mines.PrisonMines;
import tech.mcprison.prison.mines.data.Mine;
import tech.mcprison.prison.mines.features.MineBlockEvent.BlockEventType;
import tech.mcprison.prison.modules.Module;
import tech.mcprison.prison.spigot.game.SpigotPlayer;

/**
 * <p>This is a pivotal class that "monitors" onBlockBreak events so it can
 * help keep accurate counts within all mines of how many blocks have been
 * mined.  
 * </p>
 * 
 * <p><b>Note:</b> Because this is a MONITOR event, we cannot do anything with the 
 * target block here. Mostly because everything has already been done with it, and 
 * this is only intended to MONITOR the final results. 
 * </p>
 * 
 * <p>This is a very critical class too, in that every single block break on
 * server will hit this class, even if the block is not within a mine. 
 * Therefore it is paramount that the computational cost, and temporal consumption,
 * is as minimal as possible.  Many of the behaviors, or goals, that this class
 * tries to accomplish can be done through simpler way, but performance is
 * a high goal so some of the code may be convoluted, but it has its purpose.
 * <p>
 * 
 * <p>Performance considerations: Unfortunately, this class can only be optimized 
 * for mines.  If a player is within a mine and breaking blocks, then we can
 * help ensure the next block they break within the mine will have optimal performance
 * in getting the reference to that mine.  If they are outside of all mines, like 
 * in a free world, then all mines will have to be checked each time.  There may
 * be room for optimizations in the future.
 * </p>
 * 
 * <p><b>Goals and Purposes:</b> These are needs of this class, or more specifically, the 
 * goals and purposes that it is trying to solve.</p>
 * 
 * <ul>
 *   <li>Record onBlockBreak events within a mine - Primary goal and purpose</li>
 *   <li>If a mine becomes empty, submit a manual reset - Secondary purpose</li>
 * </ul>
 *
 * <p><b>Resources:</b> These are the resources that are needed and their interdependencies.
 * </p>
 * 
 * <ul>
 *   <li>Ranks Module: Not needed & not used.
 *     <ul>
 *       <li>Would have added a level of complexity that is not needed</li>
 *       <li>Could have added a last mine used field, but the overhead of 
 *       		getting a Prison player object would have been too high.</ul>
 *       <li>Using internal player lookup and caching instead for optimal performance</li>
 *      </ul>
 *   </li>
 *   <li>MineModule: Required
 *     <ul>
 *       <li>Used to find the correct mine</li>
 *       <li>Actions performed on the mine: increment block break count & submit a manual 
 *           mine reset job if block count hits zero.</li>
 *     </ul>
 *   </li>
 * </ul>
 * 
 * 
 * <p>General performance results:  Enable the commented out code to test the milliseconds it
 * spends on monitoring:
 * </p>
 * 
 * <ul>
 *   <li>Spigot 1.8.8, drop, single block mine: 
 *   		0.03371 0.033003 0.036944 0.033234 0.032782 0.030926 0.036374 0.036431</li> 
 *   <li>Spigot 1.8.8, drop, large block mine:
 *   		0.01759 0.017882 0.00455 .003232 0.002935 0.00344 0.00344 0.003248 0.002831 0.002898</li>
 *   <li>Spigot 1.8.8, drop, wilderness: 
 *   		0.024488 0.016086 0.010138 0.007265 0.009756 0.008951 0.009319 0.008664 0.007657 0.007013</li>
 *   <li>Spigot 1.8.8, drop, wilderness:
 *   		0.014462 0.009286 0.011038 0.011808 0.014291 0.011617 0.007516 0.008267 0.005324 0.004896</li>
 *   <li>Spigot 1.8.8, drop, wilderness, disabled:
 *          0.001549 0.001138 0.001162 0.001487 0.00181 0.000084</li>
 *   		</li>
 * </ul>
 *
 */
public class OnBlockBreakEventListener 
	implements Listener {

//	private final TreeMap<Long, Mine> playerCache;
	
	private PrisonMines prisonMineManager;
	private boolean mineModuleDisabled = false;
	
	private int uses = 0;
	private long usesElapsedTimeNano = 0L;
	
	public OnBlockBreakEventListener() {
		super();
		
//		this.playerCache = new TreeMap<>();
		this.prisonMineManager = null;
	}
	
    /**
     * <p>The EventPriorty.MONITOR means that the state of the event is OVER AND DONE,
     * so this function CANNOT do anything with the block, other than "monitor" what
     * happened.  That is all we need to do, is to just count the number of blocks within
     * a mine that have been broken.
     * </p>
     * 
     * <p><b>Note:</b> Because this is a MONITOR event, we cannot do anything with the 
     * target block here. Mostly because everything has already been done with it, and 
     * this is only intended to MONITOR the final results. 
     * </p>
     * 
     * <p>One interesting fact about this monitoring is that we know that a block was broken,
     * not because of what is left (should be air), but because this function was called.
     * There is a chance that the event was canceled and the block remains unbroken, which
     * is what WorldGuard would do.  But the event will also be canceled when auto pickup is
     * enabled, and at that point the BlockType will be air.
     * </p>
     * 
     * <p>If the event is canceled it's important to check to see that the BlockType is Air,
     * since something already broke the block and took the drop.  
     * If it is not canceled we still need to count it since it will be a normal drop.  
     * </p>
     * 
     * @param e
     */
    @EventHandler(priority=EventPriority.MONITOR) 
    public void onBlockBreak(BlockBreakEvent e) {
    	
    	genericBlockEvent( e );
    }
    
    @EventHandler(priority=EventPriority.MONITOR) 
    public void onTEBlockExplode(TEBlockExplodeEvent e) {
    	
    	genericBlockExplodeEvent( e );
    }

    /**
     * <p>This genericBlockEvent handles the basics of a BlockBreakEvent to see if it has happened
     * within a mine or not.  If it is happening within a mine, then we process it with the doAction()
     * function.
     * </p>
     * 
     * <p>For this class only. the doAction() is only counting the block break event, but does
     * nothing with the actual block such that there is no need to have knowledge as to if 
     * it is a custom block.  In other doAction() functions that would exist in other classes 
     * that extend from this one, it may need that information.  The hooks to find the custom
     * blocks is within the Block's getPrisonBlock() function. 
     * </p>
     * 
     * @param e
     */
	protected void genericBlockEvent( BlockBreakEvent e ) {
		// Fast fail: If the prison's mine manager is not loaded, then no point in processing anything.
    	if ( getPrisonMineManager() != null ) 
    	{
    		
    		// long startNano = System.nanoTime();
    		
    		boolean isAir = e.getBlock().getType() != null && e.getBlock().getType() == Material.AIR;

    		// If canceled it must be AIR, otherwise if it is not canceled then 
    		// count it since it will be a normal drop
    		if ( e.isCancelled() && isAir || !e.isCancelled() ) {

    			// Need to wrap in a Prison block so it can be used with the mines:
    			SpigotBlock block = new SpigotBlock(e.getBlock());
    			
    			Long playerUUIDLSB = Long.valueOf( e.getPlayer().getUniqueId().getLeastSignificantBits() );

    			// Get the cached mine, if it exists:
    			Mine mine = getPlayerCache().get( playerUUIDLSB );
    			
    			if ( mine == null || !mine.isInMine( block.getLocation() ) ) {
    				// Look for the correct mine to use. 
    				// Set mine to null so if cannot find the right one it will return a null:
    				mine = findMineLocation( block );
    				
    				// Store the mine in the player cache if not null:
    				if ( mine != null ) {
    					getPlayerCache().put( playerUUIDLSB, mine );
    				}
    			}
    			
    			// This is where the processing actually happens:
    			if ( mine != null ) {
    				
    				// Set the mine's PrisonBlockTypes for the block. Used to identify custom blocks.
    				// Needed since processing of the block will lose track of which mine it came from.
    				block.setPrisonBlockTypes( mine.getPrisonBlockTypes() );

    				doAction( block, mine, e );
    			}
    				
    			// future change to allow auto features outside of mines:
//    			doAction( block, mine, e );
    		}
    		
    		// for debug use: Uncomment to use.
//    		String message = incrementUses(System.nanoTime() - startNano);
//    		if ( message != null ) {
//    			e.getPlayer().sendMessage( message );
//    		}
    	}
	}


	/**
	 * <p>Since there are multiple blocks associated with this event, pull out the player first and
	 * get the mine, then loop through those blocks to make sure they are within the mine.
	 * </p>
	 * 
	 * <p>The logic in this function is slightly different compared to genericBlockEvent() because this
	 * event contains multiple blocks so it's far more efficient to process the player data once. 
	 * So that basically needed a slight refactoring.
	 * </p>
	 * 
	 * @param e
	 */
	protected void genericBlockExplodeEvent( TEBlockExplodeEvent e )
	{
		// Fast fail: If the prison's mine manager is not loaded, then no point in processing anything.
    	if ( getPrisonMineManager() != null ) {
    		
    		// long startNano = System.nanoTime();
    		Long playerUUIDLSB = Long.valueOf( e.getPlayer().getUniqueId().getLeastSignificantBits() );
    		
    		// Get the cached mine, if it exists:
    		Mine mine = getPlayerCache().get( playerUUIDLSB );
    		
    		if ( mine == null ) {
    			
    			// have to go through all blocks since some blocks may be outside the mine.
    			// but terminate search upon first find:
    			for ( Block blk : e.blockList() ) {
    				// Need to wrap in a Prison block so it can be used with the mines:
    				SpigotBlock block = new SpigotBlock(blk);
    				
    				// Look for the correct mine to use. 
    				// Set mine to null so if cannot find the right one it will return a null:
    				mine = findMineLocation( block );
    				
    				// Store the mine in the player cache if not null:
    				if ( mine != null ) {
    					getPlayerCache().put( playerUUIDLSB, mine );
    					
    					// we found the mine!
    					break;
    				}
    			}
    		}
    		
    		// now process all blocks:
    		if ( mine != null ) {
    			// have to go through all blocks since some blocks may be outside the mine.
    			// but terminate search upon first find:
    			
    			int blockCount = 0;
    			for ( Block blk : e.blockList() ) {
    				boolean isAir = blk.getType() != null && blk.getType() == Material.AIR;
    				
    				// If canceled it must be AIR, otherwise if it is not canceled then 
    				// count it since it will be a normal drop
    				if ( e.isCancelled() && isAir || !e.isCancelled() ) {
    					
    					// Need to wrap in a Prison block so it can be used with the mines:
    					SpigotBlock block = new SpigotBlock(blk);
    					
    					if ( !mine.isInMine( block.getLocation() ) ) {
    						
    						blockCount++;
    					}
    					
    				}
    			}
    			if ( blockCount > 0 ) {
    				// This is where the processing actually happens:
    				doAction( mine, e, blockCount );
    				
    			}
    		}
    			
    		
    		
    		// for debug use: Uncomment to use.
//    		String message = incrementUses(System.nanoTime() - startNano);
//    		if ( message != null ) {
//    			e.getPlayer().sendMessage( message );
//    		}
    	}
	}

	
	public void doAction( SpigotBlock block, Mine mine, BlockBreakEvent e ) {
		if ( mine != null ) {
			
			mine.incrementBlockBreakCount();
			mine.incrementTotalBlocksMined();
			
			// Other possible processing:
			
			// Process mine block break events:
			SpigotPlayer player = new SpigotPlayer( e.getPlayer() );
			mine.processBlockBreakEventCommands( 1, player, BlockEventType.eventBlockBreak );
			
			
			// Checks to see if the mine ran out of blocks, and if it did, then
			// it will reset the mine:
			mine.checkZeroBlockReset();
		}
	}
	
	public void doAction( Mine mine, TEBlockExplodeEvent e, int blockCount ) {
		if ( mine != null ) {
			
			mine.addBlockBreakCount( blockCount );
			mine.addTotalBlocksMined( blockCount );
			
			// Other possible processing:
			
			// Process mine block break events:
			SpigotPlayer player = new SpigotPlayer( e.getPlayer() );
			mine.processBlockBreakEventCommands( blockCount, player, BlockEventType.eventTEXplosion );
			
			
			// Checks to see if the mine ran out of blocks, and if it did, then
			// it will reset the mine:
			mine.checkZeroBlockReset();
		}
	}
	
	private Mine findMineLocation( SpigotBlock block ) {
		return getPrisonMineManager().findMineLocation( block.getLocation() );
	}
	
//    /**
//     * <p>Search all mines to find if the given block is located within any
//     * of the mines. If not, then return a null.
//     * </p>
//     * 
//     * @param block
//     * @return
//     */
//	private Mine findMineLocation( SpigotBlock block ) {
//		Mine mine = null;
//		for ( Mine m : getPrisonMineManager().getMines() ) {
//			if ( m.isInMine( block.getLocation() ) ) {
//				mine = m;
//				break;
//			}
//		}
//		return mine;
//	}

	private TreeMap<Long, Mine> getPlayerCache() {
		return getPrisonMineManager().getPlayerCache();
	}

	public PrisonMines getPrisonMineManager() {
		if ( prisonMineManager == null && !isMineModuleDisabled() ) {
			Optional<Module> mmOptional = Prison.get().getModuleManager().getModule( PrisonMines.MODULE_NAME );
			if ( mmOptional.isPresent() && mmOptional.get().isEnabled() ) {
				PrisonMines prisonMines = (PrisonMines) mmOptional.get();
				this.prisonMineManager = prisonMines;
			} else {
				setMineModuleDisabled( true );
			}
		}
		return prisonMineManager;
	}

	private boolean isMineModuleDisabled() {
		return mineModuleDisabled;
	}
	private void setMineModuleDisabled( boolean mineModuleDisabled ) {
		this.mineModuleDisabled = mineModuleDisabled;
	}

	
	@SuppressWarnings( "unused" )
	private synchronized String incrementUses(Long elapsedNano) {
		String message = null;
		usesElapsedTimeNano += elapsedNano;
		
		if ( ++uses >= 100 ) {
			double avgNano = usesElapsedTimeNano / uses;
			double avgMs = avgNano / 1000000;
			message = String.format( "OnBlockBreak: count= %s avgNano= %s avgMs= %s ", 
					Integer.toString(uses), Double.toString(avgNano), Double.toString(avgMs) );
			
			uses = 0;
			usesElapsedTimeNano = 0L;
		}
		return message;
	}

}
