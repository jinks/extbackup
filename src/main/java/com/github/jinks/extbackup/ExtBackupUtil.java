package com.github.jinks.extbackup;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;

public class ExtBackupUtil {
	
	public static void broadcast(MinecraftServer server, String message) {
		if (!ConfigHandler.COMMON.silent.get()) {
			StringTextComponent bcMessage = new StringTextComponent("[§1ExtBackup§r] " + message);
			server.getPlayerList().broadcastMessage(bcMessage, ChatType.SYSTEM, Util.NIL_UUID);
		}
	}

}
