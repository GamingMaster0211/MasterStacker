package me.GamingMaster0211.MasterStacker.command;

import me.GamingMaster0211.MasterStacker.MasterStacker;
import me.GamingMaster0211.MasterStacker.util.SpawnerItemUtil;
import me.GamingMaster0211.MasterStacker.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MasterStackerCommand implements CommandExecutor, TabCompleter {

    private final MasterStacker plugin;

    public MasterStackerCommand(
            MasterStacker plugin
    ) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        String permission = plugin.getConfig().getString("permissions.admin", "masterstacker.admin");

        if (!sender.hasPermission(permission)) {
            sender.sendMessage(TextUtil.parse("<red>You do not have permission.</red>"));

            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);

            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "give" -> handleGive(sender, args);

            case "reload" -> {
                plugin.reloadConfig();

                sender.sendMessage(TextUtil.parse("<green>MasterStacker reloaded.</green>"));
            }

            case "cleanupholograms" -> handleCleanupHolograms(sender);

            default -> sendUsage(sender);
        }

        return true;
    }

    private void handleCleanupHolograms(
            CommandSender sender
    ) {

        int removed = 0;

        for (var world : Bukkit.getWorlds()) {

            for (Entity entity : world.getEntities()) {

                if (!(entity instanceof TextDisplay display)) {
                    continue;
                }

                PersistentDataContainer data = display.getPersistentDataContainer();

                boolean mobHologram = data.has(me.GamingMaster0211.MasterStacker.Keys.mobHologram(plugin), PersistentDataType.BYTE);
                boolean spawnerHologram = data.has(me.GamingMaster0211.MasterStacker.Keys.hologram(plugin), PersistentDataType.BYTE);

                if (mobHologram || spawnerHologram) {

                    display.remove();

                    removed++;
                }
            }
        }

        sender.sendMessage(TextUtil.parse("<green>Removed " + removed + " MasterStacker hologram(s).</green>"));
    }

    private void handleGive(
            CommandSender sender,
            String[] args
    ) {

        if (args.length < 3) {
            sender.sendMessage(TextUtil.parse("<red>Usage: /masterstacker give <player> <mob> [amount]</red>"));

            return;
        }

        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage(TextUtil.parse("<red>Player is not online.</red>"));

            return;
        }

        EntityType type;

        try {
            type = EntityType.valueOf(args[2].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(TextUtil.parse("<red>Unknown entity type.</red>"));

            return;
        }

        if (!type.isAlive()
                || !type.isSpawnable()
                || type == EntityType.PLAYER) {

            sender.sendMessage(TextUtil.parse("<red>That entity cannot be used for a spawner.</red>"));

            return;
        }

        int amount = 1;

        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException exception) {
                sender.sendMessage(TextUtil.parse("<red>Amount must be a number.</red>"));

                return;
            }
        }

        amount = Math.max(1, amount);

        int max = plugin.getConfig().getInt("spawners.max-stack-size", 64);

        while (amount > 0) {
            int current = Math.min(amount, max);

            ItemStack item = SpawnerItemUtil.create(plugin, type, current);

            var leftovers = target.getInventory().addItem(item);

            for (ItemStack leftover : leftovers.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), leftover);
            }

            amount -= current;
        }

        sender.sendMessage(TextUtil.parse("<green>Spawner given.</green>"));
    }

    private void sendUsage(
            CommandSender sender
    ) {
        sender.sendMessage(TextUtil.parse("<aqua>MasterStacker</aqua>"));
        sender.sendMessage(TextUtil.parse("<white>/masterstacker give <player> <mob> [amount]</white>"));
        sender.sendMessage(TextUtil.parse("<white>/masterstacker reload</white>"));
        sender.sendMessage(TextUtil.parse("<white>/masterstacker cleanupholograms</white>"));
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {

        List<String> result = new ArrayList<>();

        if (args.length == 1) {

            result.add("give");
            result.add("reload");
            result.add("cleanupholograms");

            return result;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                result.add(player.getName());
            }

            return result;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            for (EntityType type : EntityType.values()) {
                if (type.isAlive() && type.isSpawnable()) {

                    result.add(type.name().toLowerCase(Locale.ROOT));
                }
            }
        }

        return result;
    }
}
