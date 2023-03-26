package com.github.jinks.extbackup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ExtBackup.MOD_ID)
@Mod.EventBusSubscriber(modid = ExtBackup.MOD_ID)
public class ExtBackup {
	
	public static final String MOD_ID = "extbackup";

    public static final Logger logger = LogManager.getLogger("ExtBackup");

    public ExtBackup() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);
        //modBus.addListener((ModConfig.Loading e) -> ConfigHandler.onConfigLoad());
		//modBus.addListener((ModConfig.Reloading e) -> ConfigHandler.onConfigLoad());

        // Register ourselves for server and other game events we are interested in
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.register(this);
        forgeBus.addListener(this::serverAboutToStart);
		forgeBus.addListener(this::serverStopping);
    }

    private void setup(final FMLCommonSetupEvent event) {
        ConfigHandler.onConfigLoad();
    }
    
    public void serverAboutToStart(ServerStartedEvent event) {
    	BackupHandler.INSTANCE.init();
    }
    
    @SubscribeEvent
    public void serverStopping(ServerStoppingEvent event) {
    	if (ConfigHandler.COMMON.force_on_shutdown.get()) {
    		MinecraftServer server = event.getServer();

    		if (server != null) {
    			BackupHandler.INSTANCE.run(server);
    		}
    	}
    }
    
    @SubscribeEvent
	public static void serverTick(TickEvent.WorldTickEvent event) {
    	if (event.phase != TickEvent.Phase.START) {
			//MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
			MinecraftServer server = event.world.getServer();

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