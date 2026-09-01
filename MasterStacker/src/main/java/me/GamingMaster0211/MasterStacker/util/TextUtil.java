package me.GamingMaster0211.MasterStacker.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class TextUtil {

    private static final MiniMessage MINI_MESSAGE =
            MiniMessage.miniMessage();

    private TextUtil() {
    }

    public static Component parse(String text) {
        if (text == null) {
            return Component.empty();
        }

        return MINI_MESSAGE.deserialize(text);
    }

    public static Component format(
            String format,
            int amount,
            String mob
    ) {
        return parse(
                replace(
                        replace(
                                format,
                                "%amount%",
                                amount
                        ),
                        "%mob%",
                        mob
                )
        );
    }

    public static String replace(
            String text,
            String placeholder,
            int value
    ) {
        if (text == null) {
            return "";
        }

        return text.replace(
                placeholder,
                String.valueOf(value)
        );
    }

    public static String replace(
            String text,
            String placeholder,
            String value
    ) {
        if (text == null) {
            return "";
        }

        return text.replace(
                placeholder,
                value == null ? "" : value
        );
    }

    /**
     * Compatibility method for the older ItemUtil.
     *
     * Supports legacy Bukkit color codes such as:
     *
     * &bHello
     * &fWorld
     */
    public static String color(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("&0", "§0")
                .replace("&1", "§1")
                .replace("&2", "§2")
                .replace("&3", "§3")
                .replace("&4", "§4")
                .replace("&5", "§5")
                .replace("&6", "§6")
                .replace("&7", "§7")
                .replace("&8", "§8")
                .replace("&9", "§9")
                .replace("&a", "§a")
                .replace("&b", "§b")
                .replace("&c", "§c")
                .replace("&d", "§d")
                .replace("&e", "§e")
                .replace("&f", "§f")
                .replace("&k", "§k")
                .replace("&l", "§l")
                .replace("&m", "§m")
                .replace("&n", "§n")
                .replace("&o", "§o")
                .replace("&r", "§r");
    }
}
