package me.GamingMaster0211.MasterStacker.util;

import me.GamingMaster0211.MasterStacker.Keys;
import me.GamingMaster0211.MasterStacker.MasterStacker;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;

public final class SpawnerItemUtil {

    private SpawnerItemUtil() {}

    public static ItemStack create(
            MasterStacker plugin,
            EntityType type,
            int virtualAmount
    ) {

        ItemStack item = ItemStack.of(Material.SPAWNER);
        ItemMeta meta = item.getItemMeta();

        meta.getPersistentDataContainer().set(
                Keys.spawnerType(plugin),
                PersistentDataType.STRING,
                type.name()
        );

        meta.getPersistentDataContainer().set(
                Keys.spawnerAmount(plugin),
                PersistentDataType.INTEGER,
                virtualAmount
        );

        String format = plugin.getConfig().getString(
                "spawners.item.format",
                "<aqua>%amount%x</aqua> <white>%mob% Spawner</white>"
        );

        meta.itemName(TextUtil.format(format, virtualAmount, formatMobName(type)));

        item.setItemMeta(meta);

        return item;
    }

    public static EntityType getType(
            MasterStacker plugin,
            ItemStack item
    ) {

        if (item == null || item.getType() != Material.SPAWNER) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return null;
        }

        String value = meta.getPersistentDataContainer().get(Keys.spawnerType(plugin), PersistentDataType.STRING);

        if (value == null) {
            return null;
        }

        try {
            return EntityType.valueOf(
                    value.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static int getAmount(
            MasterStacker plugin,
            ItemStack item
    ) {

        if (item == null || item.getType() != Material.SPAWNER) {
            return 1;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return 1;
        }

        Integer amount = meta.getPersistentDataContainer().get(Keys.spawnerAmount(plugin), PersistentDataType.INTEGER);

        return amount == null ? 1 : Math.max(1, amount);
    }

    public static String formatMobName(
            EntityType type
    ) {

        String raw = type.name().toLowerCase(Locale.ROOT).replace('_', ' ');

        StringBuilder result = new StringBuilder();

        for (String word : raw.split(" ")) {

            if (word.isEmpty()) {
                continue;
            }

            if (!result.isEmpty()) {
                result.append(' ');
            }

            result.append(
                    Character.toUpperCase(word.charAt(0))
            );

            if (word.length() > 1) {
                result.append(word.substring(1));
            }
        }

        return result.toString();
    }
}
