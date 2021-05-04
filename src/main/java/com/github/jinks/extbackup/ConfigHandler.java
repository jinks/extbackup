package com.github.jinks.extbackup;

import java.io.File;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;

public final class ConfigHandler {
    
    public static class Common {
        public final ForgeConfigSpec.BooleanValue enabled;
        private final ForgeConfigSpec.IntValue backup_timer;
        public final ForgeConfigSpec.BooleanValue silent;
        public final ForgeConfigSpec.BooleanValue only_if_players_online;
        public final ForgeConfigSpec.BooleanValue force_on_shutdown;
        public final ForgeConfigSpec.ConfigValue<String> script;

        public Common(ForgeConfigSpec.Builder builder) {
            enabled = builder
                                .comment("backups are enabled")
                                .define("enabled", true);
            
            backup_timer = builder
                                .comment("interval in minutes to run the backup script")
                                .defineInRange("backup_timer", 10, 1 , 3600);

            silent = builder
                                .comment("be silent, do not post backups in chat")
                                .define("silent", false);

            only_if_players_online = builder
                                .comment("run only if players are online")
                                .define("only_if_players_online", true);

            force_on_shutdown = builder
                                .comment("run backup on server shutdown")
                                .define("force_on_shutdown", true);

            script = builder
                                .comment("script location - this script is run on each interval")
                                .define("script", "local/extbackup/runbackup.sh");
        }

        public long time() {
            return (long) (ConfigHandler.COMMON.backup_timer.get() * 60000L);
        }

        private static File cachedScript;
        public File getScript() {
            String scr = ConfigHandler.COMMON.script.get();
            if (scr == null) {
                ExtBackup.logger.warn("Script is NULL!");
                return null;
            }
            if (cachedScript == null) {
                cachedScript = scr.trim().isEmpty() ? new File(FMLPaths.GAMEDIR.get() + "local/extbackup/runbackup.sh") : new File(scr.trim());
            }
            return cachedScript;
        }
    }

    public static final Common COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;
	static {
	    final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
	    COMMON_SPEC = specPair.getRight();
	    COMMON = specPair.getLeft();
	}

    public static void onConfigLoad() {
        //BackupHandler.INSTANCE.nextBackup = System.currentTimeMillis() + ConfigHandler.COMMON.time();
        //ExtBackup.logger.info("Config Changed, next backup at: " + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(BackupHandler.INSTANCE.nextBackup)));
    }
}