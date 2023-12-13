package com.github.jinks.extbackup;

import net.minecraft.server.MinecraftServer;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ChatType;

public class ExtBackupUtil {
	
	public static void broadcast(MinecraftServer server, String message) {
		if (!ConfigHandler.COMMON.silent.get()) {
			//Component bcMessage = new TextComponent("[§1ExtBackup§r] " + message);
			Component bcMessage = Component.literal("[§1ExtBackup§r] " + message);
			//server.getPlayerList().broadcastMessage(bcMessage, ChatType.SYSTEM, Util.NIL_UUID);
			server.getPlayerList().broadcastSystemMessage(bcMessage, true);
		}
	}

}
