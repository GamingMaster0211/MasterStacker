package me.GamingMaster0211.MasterStacker.manager;

import me.GamingMaster0211.MasterStacker.Keys;
import me.GamingMaster0211.MasterStacker.MasterStacker;
import me.GamingMaster0211.MasterStacker.util.SpawnerItemUtil;
import me.GamingMaster0211.MasterStacker.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public final class SpawnerManager {

    private final MasterStacker plugin;

    private static final BlockFace[] NEIGHBORS = {
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.UP,
            BlockFace.DOWN
    };

    public SpawnerManager(
            MasterStacker plugin
    ) {
        this.plugin = plugin;
    }

    public boolean isSpawner(Block block) {
        return block != null && block.getType() == Material.SPAWNER;
    }

    public int getAmount(Block block) {

        if (!isSpawner(block)) {
            return 0;
        }

        if (!(block.getState() instanceof CreatureSpawner spawner)) {
            return 1;
        }

        return Math.max(
                1,
                spawner.getPersistentDataContainer()
                        .getOrDefault(
                                Keys.spawnerAmount(plugin),
                                PersistentDataType.INTEGER,
                                1
                        )
        );
    }

    public EntityType getType(Block block) {

        if (!isSpawner(block)) {
            return null;
        }

        if (!(block.getState()
                instanceof CreatureSpawner spawner)) {
            return null;
        }

        return spawner.getSpawnedType();
    }

    public void setAmount(
            Block block,
            int amount
    ) {

        if (!(block.getState()
                instanceof CreatureSpawner spawner)) {
            return;
        }

        amount =
                Math.max(
                        1,
                        Math.min(
                                amount,
                                getMaxStackSize()
                        )
                );

        spawner.getPersistentDataContainer().set(
                Keys.spawnerAmount(plugin),
                PersistentDataType.INTEGER,
                amount
        );

        spawner.update(true, false);

        updateHologram(block);
    }

    public void initialize(
            Block block,
            EntityType type,
            int amount
    ) {

        if (!(block.getState()
                instanceof CreatureSpawner spawner)) {
            return;
        }

        spawner.setSpawnedType(type);

        amount =
                Math.max(
                        1,
                        Math.min(
                                amount,
                                getMaxStackSize()
                        )
                );

        spawner.getPersistentDataContainer().set(
                Keys.spawnerAmount(plugin),
                PersistentDataType.INTEGER,
                amount
        );

        spawner.update(true, false);

        updateHologram(block);
    }

    public Block findMergeTarget(
            Block placedBlock,
            EntityType type
    ) {

        for (BlockFace face : NEIGHBORS) {

            Block nearby =
                    placedBlock.getRelative(face);

            if (!isSpawner(nearby)) {
                continue;
            }

            EntityType nearbyType =
                    getType(nearby);

            if (nearbyType != type) {
                continue;
            }

            int amount =
                    getAmount(nearby);

            if (amount >= getMaxStackSize()) {
                continue;
            }

            return nearby;
        }

        return null;
    }

    public int getMaxStackSize() {

        return Math.max(
                1,
                plugin.getConfig().getInt(
                        "spawners.max-stack-size",
                        100
                )
        );
    }

    public ItemStack createItem(
            EntityType type,
            int amount
    ) {

        return SpawnerItemUtil.create(plugin, type, amount);
    }

    public void updateHologram(Block block) {

        if (!isSpawner(block)) {
            return;
        }

        if (!plugin.getConfig().getBoolean(
                "spawners.hologram.enabled",
                true
        )) {
            removeHologram(block);

            return;
        }

        if (!(block.getState() instanceof CreatureSpawner spawner)) {
            return;
        }

        EntityType type = spawner.getSpawnedType();

        if (type == null) {
            return;
        }

        int amount = getAmount(block);

        String format = plugin.getConfig().getString("spawners.hologram.format", "<aqua>%amount%x</aqua> <white>%mob% Spawner</white>");

        var text =
                TextUtil.format(
                        format,
                        amount,
                        SpawnerItemUtil.formatMobName(type)
                );

        double height = plugin.getConfig().getDouble("spawners.hologram.height", 1.5);

        TextDisplay display = null;

        String storedId =
                spawner.getPersistentDataContainer()
                        .get(
                                Keys.hologram(plugin),
                                PersistentDataType.STRING
                        );

        if (storedId != null) {
            try {
                UUID uuid = UUID.fromString(storedId);

                Entity entity = Bukkit.getEntity(uuid);

                if (entity instanceof TextDisplay && !entity.isDead()) {
                    display = (TextDisplay) entity;
                }
            } catch (IllegalArgumentException ignored) {

            }
        }

        if (display == null) {
            for (Entity nearby :
                    block.getWorld().getNearbyEntities(
                            block.getLocation().add(
                                    0.5,
                                    height,
                                    0.5
                            ),
                            0.75,
                            0.75,
                            0.75
                    )) {

                if (!(nearby instanceof TextDisplay textDisplay)) {
                    continue;
                }

                if (nearby.getPersistentDataContainer()
                        .has(
                                Keys.hologram(plugin),
                                PersistentDataType.BYTE
                        )) {

                    display = textDisplay;

                    break;
                }
            }
        }

        if (display == null) {
            display = block.getWorld().spawn(
                            block.getLocation().add(
                                    0.5,
                                    height,
                                    0.5
                            ),
                            TextDisplay.class
                    );

            display.setBillboard(Display.Billboard.CENTER);
            display.setSeeThrough(true);
            display.setShadowed(false);
            display.setDefaultBackground(false);
            display.getPersistentDataContainer().set(
                    Keys.hologram(plugin),
                    PersistentDataType.BYTE,
                    (byte) 1
            );

            spawner.getPersistentDataContainer().set(Keys.hologram(plugin), PersistentDataType.STRING, display.getUniqueId().toString());
            spawner.update(true, false);
        }

        display.teleport(
                block.getLocation().add(
                        0.5,
                        height,
                        0.5
                )
        );

        display.text(text);
    }

    public void removeHologram(Block block) {
        if (block == null) {
            return;
        }

        UUID storedUuid = null;

        if (block.getState() instanceof CreatureSpawner spawner) {
            String storedId =
                    spawner.getPersistentDataContainer()
                            .get(
                                    Keys.hologram(plugin),
                                    PersistentDataType.STRING
                            );

            if (storedId != null) {

                try {
                    storedUuid =
                            UUID.fromString(storedId);
                } catch (IllegalArgumentException ignored) {

                }
            }
        }

        if (storedUuid != null) {
            Entity entity =
                    Bukkit.getEntity(storedUuid);

            if (entity != null) {
                entity.remove();
            }
        }

        double height = plugin.getConfig().getDouble("spawners.hologram.height", 1.5);

        for (Entity nearby :
                block.getWorld().getNearbyEntities(
                        block.getLocation().add(
                                0.5,
                                height,
                                0.5
                        ),
                        0.8,
                        0.8,
                        0.8
                )) {

            if (!(nearby instanceof TextDisplay)) {
                continue;
            }

            if (nearby.getPersistentDataContainer()
                    .has(
                            Keys.hologram(plugin),
                            PersistentDataType.BYTE
                    )) {

                nearby.remove();
            }
        }
    }
}
