package me.GamingMaster0211.MasterStacker.listener;

import me.GamingMaster0211.MasterStacker.MasterStacker;
import me.GamingMaster0211.MasterStacker.manager.SpawnerManager;
import me.GamingMaster0211.MasterStacker.util.SpawnerItemUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class SpawnerListener implements Listener {

    private final MasterStacker plugin;
    private final SpawnerManager manager;

    public SpawnerListener(
            MasterStacker plugin,
            SpawnerManager manager
    ) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerBreak(
            BlockBreakEvent event
    ) {
        Block block = event.getBlock();

        if (block.getType() != Material.SPAWNER) {
            return;
        }

        manager.removeHologram(block);

        var player = event.getPlayer();

        String permission = plugin.getConfig().getString("permissions.silk-touch", "masterstacker.silk");

        if (!player.hasPermission(permission)) {
            return;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!tool.containsEnchantment(
                Enchantment.SILK_TOUCH
        )) {
            return;
        }

        EntityType type = manager.getType(block);

        if (type == null) {
            return;
        }

        int amount = Math.max(1, manager.getAmount(block));

        event.setDropItems(false);

        ItemStack item = manager.createItem(type, amount);

        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);

        for (ItemStack leftover : leftovers.values()) {
            block.getWorld().dropItemNaturally(
                    block.getLocation().add(
                            0.5,
                            0.5,
                            0.5
                    ),
                    leftover
            );
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerPlace(
            BlockPlaceEvent event
    ) {
        Block placed = event.getBlockPlaced();

        if (placed.getType() != Material.SPAWNER) {
            return;
        }

        ItemStack item =  event.getItemInHand();

        EntityType itemType = SpawnerItemUtil.getType(plugin, item);

        int itemAmount = SpawnerItemUtil.getAmount(plugin, item);

        EntityType type = itemType;

        if (type == null) {
            type = manager.getType(placed);
        }

        if (type == null) {
            return;
        }

        manager.initialize(placed, type, itemAmount);

        Block target = manager.findMergeTarget(placed, type);

        if (target == null) {
            manager.updateHologram(placed);

            return;
        }

        int targetAmount = manager.getAmount(target);
        int max = manager.getMaxStackSize();
        int room = max - targetAmount;

        if (room <= 0) {
            manager.updateHologram(placed);

            return;
        }

        int merged = Math.min(room, itemAmount);

        manager.setAmount(target, targetAmount + merged);

        placed.setType(Material.AIR);

        manager.removeHologram(placed);

        int remainder = itemAmount - merged;

        if (remainder > 0) {
            ItemStack remainderItem = manager.createItem(type, remainder);

            Map<Integer, ItemStack> leftovers = event.getPlayer().getInventory().addItem(remainderItem);

            for (ItemStack leftover : leftovers.values()) {
                placed.getWorld().dropItemNaturally(
                        placed.getLocation().add(
                                0.5,
                                0.5,
                                0.5
                        ),
                        leftover
                );
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerSpawn(
            SpawnerSpawnEvent event
    ) {

        if (!(event.getSpawner() instanceof CreatureSpawner spawner)) {
            return;
        }

        Block block = spawner.getBlock();

        int amount = manager.getAmount(block);

        if (amount <= 1) {
            return;
        }

        EntityType type = event.getEntityType();

        if (type == null
                || !type.isAlive()
                || !type.isSpawnable()) {
            return;
        }

        int extra = amount - 1;

        for (int i = 0; i < extra; i++) {
            block.getWorld().spawnEntity(event.getLocation(), type, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.SPAWNER);
        }
    }
}
