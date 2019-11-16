package com.charles445.nanpolice;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ModConfig 
{
	public static Configuration config;
	public static String configname = "nanpolice.cfg";
	
	public static boolean autofix_player;
	public static boolean autofix_creatures;
	public static boolean announce_errors;
	public static final Set<String> eventbus_priorities = new HashSet<String>();
	
	
	public static void init(File dir)
	{
		if(config==null)
		{
			config = new Configuration(new File(dir, configname));
			loadConfig();
		}
	}
	
	private static void loadConfig()
	{
		Property prop;
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "autofix_player", true);
		prop.setComment("Attempt to automatically fix players with NaN health");
		prop.requiresMcRestart();
		autofix_player = prop.getBoolean();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "autofix_creatures", true);
		prop.setComment("Attempt to automatically fix creatures and monsters with NaN health");
		prop.requiresMcRestart();
		autofix_creatures = prop.getBoolean();
		
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "announce_errors", false);
		prop.setComment("Announce health errors to all players. ");
		prop.requiresMcRestart();
		announce_errors = prop.getBoolean();
		
		prop = config.get(Configuration.CATEGORY_GENERAL, "eventbus_priorities", new String[]{"LOWEST"});
        prop.setComment("What event bus priorities to watch on. LOWEST alone is recommended. (HIGHEST, HIGH, NORMAL, LOW, and LOWEST)");
        prop.requiresMcRestart();
        for(String s : prop.getStringList())
        {
        	if(!eventbus_priorities.contains(s))
        		eventbus_priorities.add(s);
        }
        
        config.save();
	}
}
