package me.GamingMaster0211.MasterStacker.listener;

import me.GamingMaster0211.MasterStacker.MasterStacker;
import me.GamingMaster0211.MasterStacker.manager.MobStackManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class MobStackListener implements Listener {

    private final MasterStacker plugin;
    private final MobStackManager manager;
    private final Set<UUID> pending = new HashSet<>();

    public MobStackListener(
            MasterStacker plugin,
            MobStackManager manager
    ) {

        this.plugin = plugin;
        this.manager = manager;

        startStackingTask();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(
            CreatureSpawnEvent event
    ) {

        LivingEntity entity = event.getEntity();

        if (!manager.canStack(entity)) {
            return;
        }

        pending.add(entity.getUniqueId());
    }

    private void startStackingTask() {
        long cooldown = Math.max(1L, plugin.getConfig().getLong("mob-stacking.stacking-cooldown-ticks", 100L));

        Bukkit.getScheduler().runTaskTimer(plugin, this::processPending, cooldown, cooldown);
    }

    private void processPending() {
        if (pending.isEmpty()) {
            return;
        }

        Set<UUID> current = new HashSet<>(pending);

        pending.clear();

        for (UUID uuid : current) {
            Entity entity = Bukkit.getEntity(uuid);

            if (!(entity instanceof LivingEntity living)) {
                continue;
            }

            if (living.isDead()) {
                continue;
            }

            if (!manager.canStack(living)) {
                continue;
            }

            int currentAmount = manager.getAmount(living);

            if (currentAmount >= manager.getMaxStackSize()) {
                continue;
            }

            LivingEntity nearby = manager.findNearbyCompatible(living);

            if (nearby == null) {
                continue;
            }

            manager.merge(nearby, living);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(
            EntityDeathEvent event
    ) {

        LivingEntity entity = event.getEntity();

        if (!manager.canStack(entity)) {
            return;
        }

        int stackAmount = manager.getAmount(entity);

        if (stackAmount <= 1) {
            return;
        }

        manager.removeHologram(entity);

        int killAmount = getKillAmount(event);

        killAmount = Math.min(killAmount, stackAmount);

        int remaining = stackAmount - killAmount;

        multiplyDrops(event.getDrops(), killAmount );

        // event.setDroppedExp(event.getDroppedExp() * killAmount);
        event.setDroppedExp(0);

        if (remaining <= 0) {
            return;
        }

        var location = entity.getLocation().clone();
        var world = entity.getWorld();

        Bukkit.getScheduler().runTask(
                plugin,
                () -> {

                    if (world == null) {
                        return;
                    }

                    if (!world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                        return;
                    }

                    LivingEntity replacement = (LivingEntity) world.spawnEntity(location, entity.getType());

                    manager.setAmount(replacement, remaining);
                    pending.add(replacement.getUniqueId());
                }
        );
    }

    private int getKillAmount(
            EntityDeathEvent event
    ) {

        int amount = 1;

        Entity causing = event.getDamageSource().getCausingEntity();

        if (!(causing instanceof Player player)) {
            return 1;
        }

        if (!hasMultiKillEnchant(player)) {
            return 1;
        }

        boolean enabled = plugin.getConfig().getBoolean("mob-stacking.multi-kill.enabled", true);

        if (!enabled) {
            return 1;
        }

        return Math.max(1, plugin.getConfig().getInt("mob-stacking.multi-kill.amount", 5));
    }

    private boolean hasMultiKillEnchant(
            Player player
    ) {

        String configured = plugin.getConfig().getString("mob-stacking.multi-kill.enchantment", "minecraft:sweeping_edge");
        NamespacedKey key = NamespacedKey.fromString(configured);

        if (key == null) {
            return false;
        }

        Enchantment enchantment = Registry.ENCHANTMENT.get(key);

        if (enchantment == null) {
            return false;
        }

        ItemStack weapon = player.getInventory().getItemInMainHand();

        return weapon.containsEnchantment(enchantment);
    }

    private void multiplyDrops(
            List<ItemStack> drops,
            int multiplier
    ) {

        if (multiplier <= 1) {
            return;
        }

        List<ItemStack> original = new ArrayList<>();

        for (ItemStack drop : drops) {
            if (drop == null || drop.getType().isAir()) {
                continue;
            }

            original.add(drop.clone());
        }

        drops.clear();

        for (int i = 0; i < multiplier; i++) {
            for (ItemStack drop : original) {
                drops.add(drop.clone());
            }
        }
    }
}
