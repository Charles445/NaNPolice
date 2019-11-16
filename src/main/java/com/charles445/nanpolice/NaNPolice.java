package com.charles445.nanpolice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.charles445.nanpolice.command.CommandNaNPolice;

import net.minecraft.command.ICommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod
(
	modid = NaNPolice.MODID, 
	name = NaNPolice.NAME, 
	version = NaNPolice.VERSION,
	acceptableRemoteVersions = "*"
)

public class NaNPolice 
{
	public static final String MODID = "nanpolice";
    public static final String NAME = "NaNPolice";
    public static final String VERSION = "1.0";
    
    @Mod.Instance(NaNPolice.MODID)
    public static NaNPolice instance;
    public static Logger logger = LogManager.getLogger("NaNPolice");
    
    public static EventWatcher watcher;
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	ModConfig.init(event.getModConfigurationDirectory());
    	watcher = new EventWatcher();
    }
    
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
    	watcher.tryInitialize();
    	event.registerServerCommand(new CommandNaNPolice());
    }
}