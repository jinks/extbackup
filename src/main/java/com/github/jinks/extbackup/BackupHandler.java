package com.github.jinks.extbackup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ThreadedFileIOBase;

public enum BackupHandler {
	INSTANCE;
	
	public long nextBackup = -1L;
	public int doingBackup = 0;
	public boolean hadPlayersOnline = false;
	private boolean youHaveBeenWarned = false;
	
	public void init() {
		doingBackup = 0;
		nextBackup = System.currentTimeMillis() + ExtBackupConfig.general.time();
		File script = ExtBackupConfig.general.getScript();
		
		if (!script.exists()) {
			script.getParentFile().mkdirs();
			try {
				Files.write(script.toPath(), "#!/bin/bash\n# Put your backup script here!\n\nexit 0".getBytes(StandardCharsets.UTF_8));
				script.setExecutable(true);
			} catch (IOException e) {
				ExtBackup.logger.error("Backup script does not exist and cannot be created!");
				ExtBackup.logger.error("Disabling ExtBackup!");
				ExtBackupConfig.general.enabled = false;
			}
		}
		
		ExtBackup.logger.info("Starting "+ExtBackup.NAME+" v"+ExtBackup.VERSION);
		ExtBackup.logger.info("Active script: " + script.getAbsolutePath());
		ExtBackup.logger.info("Next Backup at: " + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(nextBackup)));
	}

	public boolean run(MinecraftServer server) {
		if (doingBackup != 0 || !ExtBackupConfig.general.enabled) {
			return false;
		}
		if (doingBackup != 0) {
			ExtBackup.logger.warn("Tried to start backup while one is already running!");
			return false;
		}
		
		File script = ExtBackupConfig.general.getScript();
		if (!script.exists() || !script.canExecute()) {
			ExtBackup.logger.error("Cannot access or execute backup script. Bailing out!");
			return false;
		}
		
		doingBackup = 1;
		
		ThreadedFileIOBase.getThreadedIOInstance().queueIO(() -> {
			try	{
				doBackup(server, script);
			} catch (Exception ex)	{
				ex.printStackTrace();
			}

			doingBackup = 2;
			return false;
		});
		
		nextBackup = System.currentTimeMillis() + ExtBackupConfig.general.time();
		return true;
	}
	
	private void doBackup(MinecraftServer server, File script) {
		ExtBackup.logger.info("Starting backup.");
		PlayerList pl = server.getPlayerList();
		ExtBackupUtil.broadcast(server, "Starting Backup!");
		try	{
			if (server.getPlayerList() != null)	{
				server.getPlayerList().saveAllPlayerData();
			}

			for (WorldServer world : server.worlds)	{
				if (world != null) {
					world.saveAllChunks(true, null);
					world.flushToDisk();
					world.disableLevelSaving = true;
				}
			}
		} catch (Exception ex) {
			ExtBackup.logger.error("Saving the world failed!");
			enableSaving(server);
			ex.printStackTrace();
			return;
		}
		
		ProcessBuilder pb = new ProcessBuilder(script.getAbsolutePath());
		int returnValue = -1;
		Map<String, String> env = pb.environment();
		pb.redirectErrorStream(true);
		try {
			Process backup = pb.start();
			returnValue = backup.waitFor();			
		} catch (Exception ex) {
			enableSaving(server);
			ExtBackup.logger.error("Something went wrong with the Backup script!");
			ExtBackup.logger.error("Check your Backups.");
			ex.printStackTrace();
		}
		enableSaving(server);
		youHaveBeenWarned = false;
		ExtBackup.logger.info("Backup done.");
		ExtBackupUtil.broadcast(server, "Backup done!");
	}
	
	private void enableSaving(MinecraftServer server) {
		for (WorldServer world : server.worlds)	{
			if (world != null) {
				world.disableLevelSaving = false;
			}
		}
	}

	public void tick(MinecraftServer server, long now) {
		if (nextBackup > 0L && nextBackup <= now) {
			//ExtBackup.logger.info("Backup time!");
			if (!ExtBackupConfig.general.only_if_players_online || hadPlayersOnline || !server.getPlayerList().getPlayers().isEmpty()) {
				hadPlayersOnline = false;
				run(server);
			}
		}
		if (doingBackup > 1) {
			doingBackup = 0;
		} else if (doingBackup > 0) {
			if (now - nextBackup > 1200000 && !youHaveBeenWarned) {
				ExtBackup.logger.warn("There has been a running backup for more than 20 minutes.");
				ExtBackup.logger.warn("Something seems to be wrong.");
				youHaveBeenWarned = true;
			}
		}
	}
}