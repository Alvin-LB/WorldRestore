package com.bringholm.worldrestore;

import com.bringholm.worldrestore.bukkitutils.ConfigurationHandler;
import com.bringholm.worldrestore.bukkitutils.ConfigurationUtils;
import com.bringholm.worldrestore.bukkitutils.TimeParser;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class WorldRestore extends JavaPlugin {
    private long checkInterval = 0;
    private HashMap<String, Long> worldsToBackup = new HashMap<>();
    private Location teleportLocation;
    private String kickMessage;

    @Override
    public void onEnable() {
        reloadConfigValues();
        new BukkitRunnable() {
            @Override
            public void run() {
                worldsToBackup.entrySet().stream().filter(entry -> entry.getValue() >= System.currentTimeMillis()).forEach(entry -> {
                    resetAndBackupWorld(entry.getKey());
                    worldsToBackup.put(entry.getKey(), System.currentTimeMillis() + getConfig().getLong(entry.getKey()));
                    saveConfigValues();
                });
            }
        }.runTaskTimer(this, checkInterval, checkInterval);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1 || args.length > 2) {
            sender.sendMessage(ChatColor.RED + "Invalid arguments!");
            return true;
        }
        if (args[0].equals("reload")) {
            reloadConfigValues();
            sender.sendMessage(ChatColor.GOLD + "Successfully reloaded configuration values!");
            return true;
        } else if (args[0].equals("reset")) {
            if (args.length == 2) {
                sender.sendMessage(resetAndBackupWorld(args[1]));
                return true;
            }
        }
        sender.sendMessage(ChatColor.RED + "Invalid arguments!");
        return true;
    }

    private void reloadConfigValues() {
        saveDefaultConfig();
        teleportLocation = ConfigurationUtils.getBlockLocation(getConfig(), "player-kick-location");
        kickMessage = getConfig().getString("kick-message");
        checkInterval = TimeParser.parseAsMillis(getConfig().getString("check-interval"));
        worldsToBackup.clear();
        ConfigurationHandler config = new ConfigurationHandler("backup-times.yml", this);
        if (getConfig().getConfigurationSection("worlds-to-backup") == null) {
            return;
        }
        for (String string : getConfig().getConfigurationSection("worlds-to-backup").getKeys(false)) {
            if (config.getConfig().contains(string)) {
                worldsToBackup.put(string, config.getConfig().getLong(string));
            } else {
                worldsToBackup.put(string, System.currentTimeMillis() + TimeParser.parseAsMillis(getConfig().getString(string)));
            }
        }
        saveConfigValues();
    }

    private void saveConfigValues() {
        ConfigurationHandler config = new ConfigurationHandler("backup-times.yml", this);
        config.clearConfig();
        for (HashMap.Entry<String, Long> entry : worldsToBackup.entrySet()) {
            config.getConfig().set(entry.getKey(), entry.getValue());
        }
    }

    private String resetAndBackupWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            this.getLogger().warning("World " + worldName + " could not be found, and therefor could not be backed up!");
            return ChatColor.RED + "World " + worldName + " could not be found, and therefor could not be backed up!";
        }
        if (world.getPlayers().size() > 0) {
            for (Player p : world.getPlayers()) {
                p.teleport(teleportLocation);
                p.sendMessage(kickMessage.replace("%worldname%", world.getName()));
            }
        }
        if (Bukkit.unloadWorld(world, true)) {
            File file = new File(worldName);
            if (file.exists()) {
                File destination = new File(this.getDataFolder(), worldName + " backups" + File.separator + getTimeStamp() + " " + worldName);
                try {
                    Files.move(file.toPath(), destination.toPath());
                    this.getLogger().info("Successfully backed up and reset world " + worldName + "!");
                    return ChatColor.GOLD + "Successfully backed up and reset world " + worldName + "!";
                } catch (IOException e) {
                    this.getLogger().severe("Encountered error \"" + e + "\" while moving world folder!");
                    return ChatColor.RED + "Encountered error \"" + e + "\" while moving world folder!";
                }
            } else {
                this.getLogger().warning("World " + worldName + " could not be found, and therefor could not be backed up!");
                return ChatColor.RED + "World " + worldName + " could not be found, and therefor could not be backed up!";
            }
        } else {
            this.getLogger().warning("World " + worldName + " could not be unloaded, is it perhaps a vanilla world?");
            return ChatColor.RED + "World " + worldName + " could not be unloaded, is it perhaps a vanilla world?";
        }
    }


    private String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HHmmss");
        return sdf.format(new Date());
    }
}
