package com.github.jinks.extbackup;

import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

@EventBusSubscriber(modid = ExtBackup.MODID)
@Config(modid = ExtBackup.MODID, category = "")
public class ExtBackupConfig {
	public static final General general = new General();

	public static class General {
		@Config.Comment("Enables backups.")
		public boolean enabled = true;
		
		@Config.RangeInt(min = 1, max = 3600)
		@Config.Comment({
				"Timer in Minutes.",
				"   5 - backups every 5 minutes",
				"  60 - backups every hour",
				"3600 - backups once a day",
		})
		public int backup_timer = 10;
		
		@Config.Comment("If set to true, no messages will be displayed in chat/status bar.")
		public boolean silent = false;
		
		@Config.Comment("Only create backups when players have been online.")
		public boolean only_if_players_online = true;

		@Config.Comment("Create a backup when server is stopped.")
		public boolean force_on_shutdown = true;
		
		@Config.Comment({
				"Path to backup script.",
				"Default: 'local/extbackup/runbackup.sh' inside the Minecraft instance."})
		public String script = "local/extbackup/runbackup.sh";
		
		public long time() {
			return (long) (backup_timer * 60000L);
		}
		
		private File cachedScript;
		public File getScript() {
			if (cachedScript == null) {
				cachedScript = ExtBackupConfig.general.script.trim().isEmpty() ? new File(FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory(), "local/extbackup/runbackup.sh") : new File(ExtBackupConfig.general.script.trim());
			}
			return cachedScript;
		}
	}
	
	public static boolean sync() {
		ConfigManager.sync(ExtBackup.MODID, Config.Type.INSTANCE);
		general.cachedScript = null;
		return true;
	}
	
	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if (event.getModID().equals(ExtBackup.MODID)) {
			sync();
		}
	}
	
}
