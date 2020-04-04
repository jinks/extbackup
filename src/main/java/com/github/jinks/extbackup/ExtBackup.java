package com.github.jinks.extbackup;

import org.apache.logging.log4j.Logger;

import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod(modid = ExtBackup.MODID, name = ExtBackup.NAME, version = ExtBackup.VERSION, acceptableRemoteVersions = "*")
@Mod.EventBusSubscriber
public class ExtBackup {
	
	public static final String MODID = "extbackup";
    public static final String NAME = "ExtBackup";
    public static final String VERSION = "1.0";

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // some example code
        //logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }
    
    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
    	BackupHandler.INSTANCE.init();
    }
    
    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
    	if (ExtBackupConfig.general.force_on_shutdown) {
    		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

    		if (server != null) {
    			BackupHandler.INSTANCE.run(server);
    		}
    	}
    }
    
    @SubscribeEvent
	public static void serverTick(TickEvent.ServerTickEvent event) {
    	if (event.phase != TickEvent.Phase.START) {
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

			if (server != null)	{
				//logger.debug("Server Tick! " + event.phase);
				BackupHandler.INSTANCE.tick(server, System.currentTimeMillis());
			}
		}
	}
    
    @SubscribeEvent
	public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		BackupHandler.INSTANCE.hadPlayersOnline = true;
	}
}