package me.GamingMaster0211.MasterStacker.manager;

import me.GamingMaster0211.MasterStacker.Keys;
import me.GamingMaster0211.MasterStacker.MasterStacker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import me.GamingMaster0211.MasterStacker.util.TextUtil;
import java.util.*;
import static me.GamingMaster0211.MasterStacker.util.SpawnerItemUtil.formatMobName;

public final class MobStackManager {

    private final MasterStacker plugin;

    private final Map<UUID, UUID> mobHolograms = new HashMap<>();

    public MobStackManager(
            MasterStacker plugin
    ) {
        this.plugin = plugin;

        startHologramTask();
    }

    public boolean canStack(
            LivingEntity entity
    ) {

        if (!plugin.getConfig().getBoolean(
                "mob-stacking.enabled",
                true
        )) {
            return false;
        }

        if (entity.getType().isAlive() == false) {
            return false;
        }

        if (entity instanceof org.bukkit.entity.Player) {
            return false;
        }

        String type = entity.getType().name();

        return !plugin.getConfig().getStringList("mob-stacking.disabled-mobs").contains(type);
    }

    public int getAmount(
            LivingEntity entity
    ) {
        return entity.getPersistentDataContainer().getOrDefault(Keys.mobAmount(plugin), PersistentDataType.INTEGER, 1);
    }

    public void setAmount(
            LivingEntity entity,
            int amount
    ) {

        entity.getPersistentDataContainer().set(Keys.mobAmount(plugin), PersistentDataType.INTEGER, Math.max(1, amount));

        updateHologram(entity);
    }

    public int getMaxStackSize() {
        return Math.max(1, plugin.getConfig().getInt("mob-stacking.max-stack-size", 100));
    }

    public boolean isStacked(
            LivingEntity entity
    ) {
        return getAmount(entity) > 1;
    }

    public LivingEntity findNearbyCompatible(
            LivingEntity source
    ) {

        double radius = 6;

        Collection<Entity> nearby = source.getNearbyEntities(radius, radius, radius);

        int max = getMaxStackSize();

        for (Entity entity : nearby) {

            if (!(entity instanceof LivingEntity living)) {
                continue;
            }

            if (living.isDead()) {
                continue;
            }

            if (living.getType() != source.getType()) {
                continue;
            }

            if (!canStack(living)) {
                continue;
            }

            if (getAmount(living) >= max) {
                continue;
            }

            return living;
        }

        return null;
    }

    public void merge(
            LivingEntity target,
            LivingEntity source
    ) {

        if (target.isDead() || source.isDead()) {
            return;
        }

        int targetAmount = getAmount(target);
        int sourceAmount = getAmount(source);
        int max = getMaxStackSize();
        int room = max - targetAmount;

        if (room <= 0) {
            return;
        }

        int merged = Math.min(room, sourceAmount);

        removeHologram(source);

        setAmount(target, targetAmount + merged);

        int remaining = sourceAmount - merged;

        if (remaining <= 0) {
            source.remove();
        } else {
            setAmount(
                    source,
                    remaining
            );
        }
    }

    public void updateHologram(LivingEntity entity) {
        if (entity == null || entity.isDead()) {
            return;
        }

        if (!canStack(entity)) {
            return;
        }

        int amount = getAmount(entity);

        if (amount <= 1) {
            removeHologram(entity);

            return;
        }

        String format = plugin.getConfig().getString(
                "mob-stacking.hologram.format",
                "<aqua>%amount%x</aqua> <white>%mob%</white>"
        );

        String mobName = formatMobName(entity.getType());

        Component text =
                TextUtil.parse(
                        format
                                .replace(
                                        "%amount%",
                                        String.valueOf(amount)
                                )
                                .replace(
                                        "%mob%",
                                        mobName
                                )
                );

        TextDisplay display = findHologram(entity);

        if (display == null) {
            double height =plugin.getConfig().getDouble("mob-stacking.hologram.height", 1.35);

            display = entity.getWorld().spawn(entity.getLocation().add(0, height, 0), TextDisplay.class);

            mobHolograms.put(entity.getUniqueId(), display.getUniqueId());

            display.setBillboard(Display.Billboard.CENTER);
            display.setSeeThrough(true);
            display.setShadowed(false);
            display.setDefaultBackground(false);
            display.getPersistentDataContainer().set(
                    Keys.mobHologram(plugin),
                    PersistentDataType.BYTE,
                    (byte) 1
            );

            entity.getPersistentDataContainer().set(
                    Keys.mobHologram(plugin),
                    PersistentDataType.STRING,
                    display.getUniqueId().toString()
            );
        }

        double height = plugin.getConfig().getDouble("mob-stacking.hologram.height", 1.35);

        display.teleport(entity.getLocation().add(0, height, 0));
        display.text(text);
    }

    private TextDisplay findHologram(
            LivingEntity entity
    ) {

        String id = entity.getPersistentDataContainer().get(Keys.mobHologram(plugin), PersistentDataType.STRING);

        if (id == null) {
            return null;
        }

        try {
            UUID uuid = UUID.fromString(id);
            Entity hologram = Bukkit.getEntity(uuid);

            if (hologram instanceof TextDisplay display && !display.isDead()) {
                return display;
            }

        } catch (IllegalArgumentException ignored) {

        }

        return null;
    }

    public void removeHologram(
            LivingEntity entity
    ) {

        if (entity == null) {
            return;
        }

        UUID mobId = entity.getUniqueId();
        UUID hologramId = mobHolograms.remove(mobId);

        if (hologramId != null) {
            Entity hologram = Bukkit.getEntity(hologramId);

            if (hologram != null) {
                hologram.remove();
            }
        }

        entity.getPersistentDataContainer().remove(Keys.mobHologram(plugin));
    }

    private void startHologramTask() {
        Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> {
                    Iterator<Map.Entry<UUID, UUID>> iterator =  mobHolograms.entrySet().iterator();

                    while (iterator.hasNext()) {
                        Map.Entry<UUID, UUID> entry = iterator.next();
                        Entity mob = Bukkit.getEntity(entry.getKey());
                        Entity hologram = Bukkit.getEntity(entry.getValue());

                        if (!(mob instanceof LivingEntity living)
                                || living.isDead()
                                || hologram == null
                                || hologram.isDead()) {

                            if (hologram != null) {
                                hologram.remove();
                            }

                            iterator.remove();

                            continue;
                        }

                        updateHologramPosition(
                                living,
                                hologram
                        );
                    }

                },
                1L,
                1L
        );
    }

    private void updateHologramPosition(
            LivingEntity entity,
            Entity hologram
    ) {

        if (!(hologram instanceof TextDisplay display)) {
            return;
        }

        double height = plugin.getConfig().getDouble("mob-stacking.hologram.height", 0.5);

        Location target = entity.getLocation().add(0, entity.getHeight() + height, 0);

        display.setTeleportDuration(2);
        display.teleport(target);
    }
}
