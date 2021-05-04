package com.github.jinks.extbackup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;

public enum BackupHandler {
	INSTANCE;
	
	public long nextBackup = -1L;
	public int doingBackup = 0;
	public boolean hadPlayersOnline = false;
	private boolean youHaveBeenWarned = false;
	public void init() {
		doingBackup = 0;
		nextBackup = System.currentTimeMillis() + ConfigHandler.COMMON.time();
		File script = ConfigHandler.COMMON.getScript();
		
		if (!script.exists()) {
			script.getParentFile().mkdirs();
			try {
				Files.write(script.toPath(), "#!/bin/bash\n# Put your backup script here!\n\nexit 0".getBytes(StandardCharsets.UTF_8));
				script.setExecutable(true);
				ExtBackup.logger.info("No backup script was found, a script has been created at " + script.getAbsolutePath() + ", please modify it to your liking.");
			} catch (IOException e) {
				ExtBackup.logger.error("Backup script does not exist and cannot be created!");
				ExtBackup.logger.error("Disabling ExtBackup!");
				ConfigHandler.COMMON.enabled.set(false);
			}
		}

		ExtBackup.logger.info("Active script: " + script.getAbsolutePath());
		ExtBackup.logger.info("Next Backup at: " + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(nextBackup)));
	}

	public boolean run(MinecraftServer server) {
		if (doingBackup != 0 || !ConfigHandler.COMMON.enabled.get()) {
			return false;
		}
		if (doingBackup != 0) {
			ExtBackup.logger.warn("Tried to start backup while one is already running!");
			return false;
		}
		
		File script = ConfigHandler.COMMON.getScript();
		if (!script.exists() || !script.canExecute()) {
			ExtBackup.logger.error("Cannot access or execute backup script. Bailing out!");
			return false;
		}
		
		doingBackup = 1;
		
		try	{
			if (server.getPlayerList() != null)	{
				server.getPlayerList().saveAll();
			}

			for (ServerWorld world : server.getAllLevels())	{
				if (world != null) {
					//world.save(null, true, false);
					world.noSave = true;
				}
			}
		} catch (Exception ex) {
			ExtBackup.logger.error("Saving the world failed!");
			enableSaving(server);
			ex.printStackTrace();
			return false;
		}

		new Thread(() -> {
			try	{
				doBackup(server, script);
			} catch (Exception ex)	{
				ex.printStackTrace();
			}

			doingBackup = 2;
		}).start();
		
		nextBackup = System.currentTimeMillis() + ConfigHandler.COMMON.time();
		return true;
	}
	
	private int doBackup(MinecraftServer server, File script) {
		if (ConfigHandler.COMMON.silent.get()) {ExtBackup.logger.info("Starting backup.");}
		ExtBackupUtil.broadcast(server, "Starting Backup!");
		
		ProcessBuilder pb = new ProcessBuilder(script.getAbsolutePath());
		int returnValue = -1;
		//Map<String, String> env = pb.environment();
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
		if (ConfigHandler.COMMON.silent.get()) {ExtBackup.logger.info("Backup done.");}
		ExtBackupUtil.broadcast(server, "Backup done!");
		ExtBackup.logger.info("Next Backup at: " + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(nextBackup)));
		return returnValue;
	}
	
	private void enableSaving(MinecraftServer server) {
		for (ServerWorld world : server.getAllLevels())	{
			if (world != null) {
				world.noSave = false;
			}
		}
	}

	public void tick(MinecraftServer server, long now) {
		if (nextBackup > 0L && nextBackup <= now) {
			//ExtBackup.logger.info("Backup time!");
			if (!ConfigHandler.COMMON.only_if_players_online.get() || hadPlayersOnline || !server.getPlayerList().getPlayers().isEmpty()) {
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