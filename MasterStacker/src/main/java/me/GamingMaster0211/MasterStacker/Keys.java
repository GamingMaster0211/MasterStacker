package me.GamingMaster0211.MasterStacker;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class Keys {

    private Keys() {}

    public static NamespacedKey spawnerType(JavaPlugin plugin) {
        return new NamespacedKey(plugin, "spawner_type");
    }

    public static NamespacedKey spawnerAmount(JavaPlugin plugin) {
        return new NamespacedKey(plugin, "spawner_amount");
    }

    public static NamespacedKey hologram(JavaPlugin plugin) {
        return new NamespacedKey(plugin, "hologram");
    }

    public static NamespacedKey mobAmount(JavaPlugin plugin) {
        return new NamespacedKey(plugin, "mob_amount");
    }
    public static NamespacedKey mobHologram(MasterStacker plugin) {
        return new NamespacedKey(plugin, "mob_hologram");
    }
}
