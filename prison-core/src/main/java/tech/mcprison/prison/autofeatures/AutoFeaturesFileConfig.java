package tech.mcprison.prison.autofeatures;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.autofeatures.ValueNode.NodeType;
import tech.mcprison.prison.file.YamlFileIO;
import tech.mcprison.prison.output.Output;

public class AutoFeaturesFileConfig {

	public static final String FILE_NAME__AUTO_FEATURES_CONFIG_YML = "/autoFeaturesConfig.yml";
	private File configFile;
//    private FileConfiguration config;
    
    private Map<String, ValueNode> config;
    
    public enum AutoFeatures {

    	
    	messages,
    	
	    	inventoryIsFull(messages, "&cWARNING! Your inventory's full!"),
	    	inventoryIsFullDroppingItems(messages, "&cWARNING! Your inventory's full and you're dropping items!"),
	    	inventoryIsFullLosingItems(messages, "&cWARNING! Your inventory's full and you're losing items!"),
    	
    	
    	options,
    	
	    	general(options),
	    	
		    	isAutoManagerEnabled(general, false),
		    	
		    	isCalculateDurabilityEnabled(general, false),
		    	isCalculateFortuneEnabled(general, true),
		    	isCalculateSilkEnabled(general, true),
		    	
		    	isCalculateXPEnabled(general, true),
		    	givePlayerXPAsOrbDrops(general, true),
		    	
		    	dropItemsIfInventoryIsFull(general, true),
				playSoundIfInventoryIsFull(general, true),
				hologramIfInventoryIsFull(general, false),

				
			permissions(options),
				permissionAutoPickup(permissions, "prison.automanager.pickup"),
				permissionAutoSmelt(permissions, "prison.automanager.smelt"),
				permissionAutoBlock(permissions, "prison.automanager.block"),
			
				
			lore(options),
				isLoreEnabled(lore, true),
				lorePickupValue(lore, "&dPickup&7"),
				loreSmeltValue(lore, "&dSmelt&7"),
				loreBlockValue(lore, "&dBlock&7"),
				
				loreTrackBlockBreakCount(lore, false),
				loreBlockBreakCountName(lore, "&dPrison Blocks Mined:&7 "),
				loreBlockExplosionCountName(lore, "&dPrison Blocks Exploded:&7 "),
				
				loreDurabiltyResistance(lore, false),
				loreDurabiltyResistanceName(lore, "&dDurability Resistance&7"),
				
	    	autoPickup(options),
		    	autoPickupEnabled(autoPickup, true),
		    	autoPickupLimitToMines(autoPickup, true),
		    	autoPickupAllBlocks(autoPickup, true),
		    	
		    	autoPickupBlockNameList(autoPickup, NodeType.STRING_LIST,
		    				"coal_block", "iron_ore"),
		    	
		    	autoPickupCobbleStone(autoPickup, true),
		    	autoPickupStone(autoPickup, true),
		    	autoPickupGoldOre(autoPickup, true),
		    	autoPickupIronOre(autoPickup, true),
		    	autoPickupCoalOre(autoPickup, true),
		    	autoPickupDiamondOre(autoPickup, true),
		    	autoPickupRedStoneOre(autoPickup, true),
		    	autoPickupEmeraldOre(autoPickup, true),
		    	autoPickupQuartzOre(autoPickup, true),
		    	autoPickupLapisOre(autoPickup, true),
		    	autoPickupSnowBall(autoPickup, true),
		    	autoPickupGlowstoneDust(autoPickup, true),
    	
    	
	    	autoSmelt(options),
		    	autoSmeltEnabled(autoSmelt, true),
		    	autoSmeltLimitToMines(autoSmelt, true),
		    	autoSmeltAllBlocks(autoSmelt, true),

		    	
		    	autoSmeltGoldOre(autoSmelt, true),
		    	autoSmeltIronOre(autoSmelt, true),
	   
	    	
	    	autoBlock(options),
		    	autoBlockEnabled(autoBlock, true),
		    	autoBlockLimitToMines(autoBlock, true),
		    	autoBlockAllBlocks(autoBlock, true),

		    	
		    	autoBlockGoldBlock(autoBlock, true),
		    	autoBlockIronBlock(autoBlock, true),
		    	autoBlockCoalBlock(autoBlock, true),
		    	autoBlockDiamondBlock(autoBlock, true),
		    	autoBlockRedstoneBlock(autoBlock, true),
		    	autoBlockEmeraldBlock(autoBlock, true),
		    	autoBlockQuartzBlock(autoBlock, true),
		    	autoBlockPrismarineBlock(autoBlock, true),
		    	autoBlockLapisBlock(autoBlock, true),
		    	autoBlockSnowBlock(autoBlock, true),
		    	autoBlockGlowstone(autoBlock, true),

    	;


    	private final AutoFeatures parent;
    	private final boolean isSection;
    	private final boolean isBoolean;
    	private final boolean isMessage;
    	private final boolean isInteger;
    	private final boolean isLong;
    	private final boolean isDouble;
    	private final boolean isStringList;
    	
    	private final String path;
    	private final String message;
    	private final Boolean value;
    	private final Integer intValue;
    	private final Long longValue;
    	private final Double doubleValue;
    	private final List<String> listValue;
    	
    	private AutoFeatures() {
    		this.parent = null;
    		this.isSection = true;
    		this.isBoolean = false;
    		this.isMessage = false;
    		this.isInteger = false;
    		this.isLong = false;
    		this.isDouble = false;
    		this.isStringList = false;
    		
    		this.path = null;
    		this.message = null;
    		this.value = null;
    		this.intValue = null;
    		this.longValue = null;
    		this.doubleValue = null;
    		this.listValue = new ArrayList<>();
    	}
    	private AutoFeatures(AutoFeatures section) {
    		this.parent = section;
    		this.isSection = true;
    		this.isBoolean = false;
    		this.isMessage = false;
    		this.isInteger = false;
    		this.isLong = false;
    		this.isDouble = false;
    		this.isStringList = false;

    		this.path = section.getKey();
    		this.message = null;
    		this.value = null;
    		this.intValue = null;
    		this.longValue = null;
    		this.doubleValue = null;
    		this.listValue = new ArrayList<>();
    	}
    	private AutoFeatures(AutoFeatures section, String message) {
    		this.parent = section;
    		this.isSection = false;
    		this.isBoolean = false;
    		this.isMessage = true;
    		this.isInteger = false;
    		this.isLong = false;
    		this.isDouble = false;
    		this.isStringList = false;

    		this.path = section.getKey();
    		this.message = message;
    		this.value = null;
    		this.intValue = null;
    		this.longValue = null;
    		this.doubleValue = null;
    		this.listValue = new ArrayList<>();
    	}
    	private AutoFeatures(AutoFeatures section, Boolean value) {
    		this.parent = section;
    		this.isSection = false;
    		this.isBoolean = true;
    		this.isMessage = false;
    		this.isInteger = false;
    		this.isLong = false;
    		this.isDouble = false;
    		this.isStringList = false;

    		this.path = section.getKey();
    		this.message = null;
    		this.value = value == null ? Boolean.FALSE : value;
    		this.intValue = null;
    		this.longValue = null;
    		this.doubleValue = null;
    		this.listValue = new ArrayList<>();
    	}
    	private AutoFeatures(AutoFeatures section, NodeType nodeType, String... values ) {
    		this.parent = section;
    		this.isSection = false;
    		this.isBoolean = false;
    		this.isMessage = false;
    		this.isInteger = false;
    		this.isLong = false;
    		this.isDouble = false;
    		this.isStringList = (nodeType == NodeType.STRING_LIST);
    		
    		this.path = section.getKey();
    		this.message = null;
    		this.value = null;
    		this.intValue = null;
    		this.longValue = null;
    		this.doubleValue = null;
    		this.listValue = new ArrayList<>();
    		
    		if ( values != null && values.length > 0 ) {
    			
    			this.listValue.addAll( Arrays.asList( values ));
    		}
    	}
    	
		public AutoFeatures getParent() {
			return parent;
		}
		public boolean isSection() {
			return isSection;
		}
		public boolean isBoolean() {
			return isBoolean;
		}
		public boolean isMessage() {
			return isMessage;
		}
		public boolean isInteger() {
			return isInteger;
		}
		public boolean isLong() {
			return isLong;
		}
		public boolean isDouble() {
			return isDouble;
		}
		public boolean isStringList() {
			return isStringList;
		}
		
		public String getPath() {
			return path;
		}
		public String getMessage() {
			return message;
		}
		public Boolean getValue() {
			return value;
		}
		
    	public Integer getIntValue() {
			return intValue;
		}
		public Long getLongValue() {
			return longValue;
		}
		public Double getDoubleValue() {
			return doubleValue;
		}
		public List<String> getListValue() {
			return listValue;
		}
		
		public String getKey() {
    		return (path != null ? path + "." : "") + this.name();
    	}
    	
    	public AutoFeatures fromString( String autoFeature ) {
    		AutoFeatures results = null;
    		
    		for ( AutoFeatures af : values() ) {
				if ( af.getKey().equalsIgnoreCase( autoFeature )) {
					results = af;
					break;
				}
			}
    		return results;
    	}
		
    	public void setFileConfig( Map<String, ValueNode> conf ) {
    		if ( isSection() ) {
    			// Skip this. The full path will be resolved in each key value for leaf nodes;
    			
    		} else if ( getMessage() != null ) {
    			// create a message entry:
    			TextNode text = TextNode.valueOf( getMessage() );
    			conf.put(getKey(), text);
    			
    		} else if ( getValue() != null ) {
    			// create a boolean entry:
    			BooleanNode bool = BooleanNode.valueOf( getValue().booleanValue() );
    			conf.put( getKey(), bool );
    			
    		}
    	}
    	
		/**
		 * <p>Get a String message from the FileConfiguration, and if the key 
		 * does not exist, return the default value associated with the enum
		 * entry, if it does not exist, then it returns a blank string value.
		 * <p>
		 * 
		 * @param conf
		 * @return
		 */
    	public String getMessage( Map<String, ValueNode> conf ) {
    		String results = null;
    		
    		if ( conf.containsKey(getKey()) && conf.get( getKey() ).isTextNode() ) {
    			TextNode text = (TextNode) conf.get( getKey() );
    			results = text.getValue();
    		}
    		else if ( getMessage() != null ) {
    			results = getMessage();
    		}
    		
    		return results;
    	}
    	
    	/**
    	 * <p>Get's the boolean value from the FileConfiguration, and if the key
    	 * does not exist, then it returns the default value associated with the 
    	 * enum entry, if it does not exist, then it returns false.
    	 * @param conf
    	 * @return
    	 */
    	public boolean getBoolean( Map<String, ValueNode> conf ) {
    		boolean results = false;
    		
    		if ( conf.containsKey(getKey()) && conf.get( getKey() ).isBooleanNode() ) {
    			BooleanNode bool = (BooleanNode) conf.get( getKey() );
    			results = bool.getValue();
    		}
    		else if ( getValue() != null ) {
    			results = getValue().booleanValue();
    		}
    		
    		return results;
    	}    	
    	
    	public List<String> getStringList( Map<String, ValueNode> conf  ) {
    		List<String> results = null;
    		
    		if ( conf.containsKey(getKey()) && conf.get( getKey() ).isStringListNode() ) {
    			StringListNode list = (StringListNode) conf.get( getKey() );
    			results = list.getValue();
    		}
    		else if ( getValue() != null ) {
    			results = new ArrayList<>();
    		}
    		
    		return results;
    	}
    	
    	

    	/**
    	 * <p>Get the children nodes to the given item.
    	 * </p>
    	 * 
    	 * @return
    	 */
    	public List<AutoFeatures> getChildren() {
    		return getChildren(this);
    	}

    	/**
    	 * <p>Get the children for the specified node.
    	 * </p>
    	 * 
    	 * @param parent
    	 * @return
    	 */
    	public List<AutoFeatures> getChildren(AutoFeatures parent) {
    		List<AutoFeatures> results = new ArrayList<>();
    		
    		for ( AutoFeatures af : values() ) {
				if ( af.getParent() == parent ) {
					results.add( af );
				}
			}
    		
    		return results;
    	}
    
    }
    
    public AutoFeaturesFileConfig() {
        
    	this.config = new LinkedHashMap<>();
    	
		
		// The following is strictly not needed to ensure that the configs are
		// created, but this, somehow, does ensure that the order in which 
		// the sections are written are in the order in which they are defined
		// within the enum.
	    for ( AutoFeatures autoFeat : AutoFeatures.values() ) {
			autoFeat.setFileConfig( getConfig() );
		}
	    
	    

        // This may sound counter intuitive if the config file does not exist, 
        // but when trying to load the yaml with the
	    // config fully loaded with the default values will trigger a save if 
	    // anyone of them do not exist in the config file.
        // So do not perform any special first time processing here since it
        // is handled within the loadYamlAutoFeatures code.
        
		YamlFileIO yamlFileIO = Prison.get().getPlatform().getYamlFileIO( getConfigFile() );
		List<AutoFeatures> dne = yamlFileIO.loadYamlAutoFeatures( getConfig() );

		dne.size();
		
//		Set<String> keys = getConfig().keySet();
//		for ( String key : keys ) {
//			ValueNode value = getConfig().get( key );
//			Output.get().logInfo( "AutoFeaturesFileConfig: ### %s  %s", 
//					key, value.toString() );
//		}
		
		List<String> blockNames = getFeatureStringList( AutoFeatures.autoPickupBlockNameList );
		StringBuilder sbBlockName = new StringBuilder( String.join( ", ", blockNames ) ).insert( 0, "[" ).append( "]" );
		Output.get().logInfo( "###--### AutoFeaturesFileConfig: test autoPickupBlockNameList: length = %d  value = %s ", 
				blockNames.size(), sbBlockName.toString() );

    }


	/**
	 * <p>This updates AutoFeatures with a String value.
	 * </p>
	 * 
	 * @param feature
	 * @param value
	 */
	public void setFeature( AutoFeatures feature, String value ) {
		
		if ( feature.isSection() ) {
			Output.get().logError( 
					String.format( "Error: AutoFeature %s is a section (path) and cannot be " +
							"used with a value. value = [%s]", 
							feature.getKey(), 
							( value == null ? "null" : value )) );
			
		}
		else if ( value == null || value.trim().length() == 0 ) {
			Output.get().logError( 
					String.format( "Error: AutoFeature %s value cannot be null or empty.", 
							feature.getKey()) );
		} 
		else if ( !feature.isMessage() ) {
			Output.get().logError( 
					String.format( "Error: AutoFeature %s value is not a message type so it " +
							"cannot be assigned a message.", 
							feature.getKey()) );
		}
		else {

			TextNode text = TextNode.valueOf( value );
			
			getConfig().put( feature.getKey(), text );
		}
		
	}
	
	/**
	 * <p>This updates AutoFeatures with a boolean value.
	 * </p>
	 * 
	 * @param feature
	 * @param value
	 */
	public void setFeature( AutoFeatures feature, boolean value ) {
		
		if ( feature.isSection() ) {
			Output.get().logError( 
					String.format( "Error: AutoFeature %s is a section (path) and cannot be " +
							"used with a value. value = [%s]", 
							feature.getKey(), 
							Boolean.valueOf( value ).toString() ) );
			
		} 
		else if ( !feature.isBoolean() ) {
			Output.get().logError( 
					String.format( "Error: AutoFeature %s value is not a boolean type so it " +
							"cannot be assigned a boolean value.", 
							feature.getKey()) );
		}
		else {
			
			BooleanNode bool = BooleanNode.valueOf( value  );
			
			getConfig().put( feature.getKey(), bool );
		}
		
	}
	

	/**
	 * This returns the config's feature value if it is a boolean.  If it is not
	 * a boolean config value, it will return a value false.
	 * 
	 * @param feature
	 * @return
	 */
	public boolean isFeatureBoolean( AutoFeatures feature ) {
		return feature.getBoolean( getConfig() );
	}
	
	public String getFeatureMessage( AutoFeatures feature ) {
		return feature.getMessage( getConfig() );
	}
	
	public List<String> getFeatureStringList( AutoFeatures feature ) {
		
		return feature.getStringList( getConfig() );
	}
	
	public boolean saveConf() {
		return saveConf( getConfig() );
	}

	/**
	 * <p>This function attempts to save the AutoFeatures configurations to the
	 * file system.  This function uses a temporary file to initially perform the save, 
	 * then when it is successfully finished, it then deletes the original file, and 
	 * renames the temp file to the correct file name.  This swapping of files 
	 * will prevent the loss of configuration data if something should go wrong in the 
	 * initial saving of the data since the original file will not be deleted first.
	 * </p>
	 * 
	 * @param afConfig
	 * @return
	 */
	private boolean saveConf( Map<String, ValueNode> config ) {
		
		YamlFileIO yamlFileIO = Prison.get().getPlatform().getYamlFileIO( getConfigFile() );
		return yamlFileIO.saveYamlAutoFeatures( config );
	}

	
	public File getConfigFile() {
		if ( this.configFile == null ) {
			
			this.configFile = new File(
					Prison.get().getDataFolder() + FILE_NAME__AUTO_FEATURES_CONFIG_YML);
		}
		return configFile;
	}
	public void setConfigFile( File configFile ) {
		this.configFile = configFile;
	}

	public Map<String, ValueNode> getConfig() {
		return config;
	}

	public void setConfig( Map<String, ValueNode> config ) {
		this.config = config;
	}

}
