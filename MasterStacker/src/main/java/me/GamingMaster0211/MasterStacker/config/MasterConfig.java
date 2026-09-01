package me.GamingMaster0211.MasterStacker.config;

import me.GamingMaster0211.MasterStacker.MasterStacker;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class MasterConfig {

    private final MasterStacker plugin;
    private final FileConfiguration config;

    public MasterConfig(MasterStacker plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public int getMaxSpawnerStackSize() {
        return Math.max(1, config.getInt("spawner.max-stack-size", 64));
    }

    public int getMaxMobStackSize() {
        return Math.max(1, config.getInt("mob-stack.max-stack-size", 64));
    }

    public int getMultiKillAmount() {
        return Math.max(1, config.getInt("mob-stack.multi-kill-amount", 1));
    }

    public boolean multiplyLoot() {
        return config.getBoolean("mob-stack.multi-kill-looting", true);
    }

    public String getSilkTouchPermission() {
        return config.getString(
                "spawner.silk-touch-permission",
                "masterstacker.silk"
        );
    }

    public String getSpawnerPlacePermission() {
        return config.getString(
                "spawner.place-permission",
                "masterstacker.place"
        );
    }

    public boolean hologramsEnabled() {
        return config.getBoolean("hologram.enabled", true);
    }

    public double getHologramHeight() {
        return config.getDouble("hologram.height", 1.5);
    }

    public String getSpawnerName() {
        return config.getString(
                "spawner.name",
                "<aqua>{amount}x</aqua> <white>{mob} Spawner</white>"
        );
    }

    public Set<String> getDisabledMobTypes() {
        Set<String> result = new HashSet<>();

        for (String value : config.getStringList("mob-stack.disabled")) {
            result.add(value.toUpperCase(Locale.ROOT));
        }

        return result;
    }

    public String getNoPermissionMessage() {
        return config.getString(
                "messages.no-permission",
                "<red>You don't have permission to mine spawners.</red>"
        );
    }

    public String getMaxSpawnerStackMessage() {
        return config.getString(
                "messages.max-spawner-stack",
                "<red>This spawner is already at the maximum stack size.</red>"
        );
    }

    public String getMaxMobStackMessage() {
        return config.getString(
                "messages.max-mob-stack",
                "<red>This mob stack is already at the maximum size.</red>"
        );
    }

    public String getInvalidSpawnerMessage() {
        return config.getString(
                "messages.invalid-spawner",
                "<red>This spawner has no mob type.</red>"
        );
    }
}
