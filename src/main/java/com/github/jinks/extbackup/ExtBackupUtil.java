package com.github.jinks.extbackup;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class ExtBackupUtil {
	
	public static void broadcast(MinecraftServer server, String message) {
		if (!ExtBackupConfig.general.silent) {
			TextComponentString bcMessage = new TextComponentString("[§1ExtBackup§r] " + message);
			server.getPlayerList().sendMessage(bcMessage);
		}
	}

}
