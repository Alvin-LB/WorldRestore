package com.bringholm.worldrestore.bukkitutils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigurationUtils {
    public static Location getBlockLocation(FileConfiguration config, String path) {
        return new Location(Bukkit.getServer().getWorld(config.getString(path + ".world")), config.getInt(path + ".x"), config.getInt(path + ".y"), config.getInt(path + ".z"));
    }
}
