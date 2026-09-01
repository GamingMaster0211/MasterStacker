package me.GamingMaster0211.MasterStacker;

import me.GamingMaster0211.MasterStacker.command.MasterStackerCommand;
import me.GamingMaster0211.MasterStacker.listener.MobStackListener;
import me.GamingMaster0211.MasterStacker.listener.SpawnerListener;
import me.GamingMaster0211.MasterStacker.manager.MobStackManager;
import me.GamingMaster0211.MasterStacker.manager.SpawnerManager;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

public final class MasterStacker extends JavaPlugin {

    private SpawnerManager spawnerManager;
    private MobStackManager mobStackManager;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        spawnerManager = new SpawnerManager(this);
        mobStackManager = new MobStackManager(this);

        getServer().getPluginManager().registerEvents(new SpawnerListener(this, spawnerManager), this);
        getServer().getPluginManager().registerEvents(new MobStackListener(this, mobStackManager), this);

        MasterStackerCommand command = new MasterStackerCommand(this);

        if (getCommand("masterstacker") != null) {
            getCommand("masterstacker").setExecutor(command);
            getCommand("masterstacker").setTabCompleter(command);
        }

        Bukkit.getScheduler().runTask(this, () -> {
                    for (var world : Bukkit.getWorlds()) {
                        for (var chunk : world.getLoadedChunks()) {
                            for (var tile : chunk.getTileEntities()) {
                                if (tile instanceof org.bukkit.block.CreatureSpawner) {
                                    spawnerManager.updateHologram(tile.getBlock());
                                }
                            }
                        }
                    }
                }
        );

        getLogger().info("MasterStacker enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("MasterStacker disabled.");
    }

    public SpawnerManager getSpawnerManager() {
        return spawnerManager;
    }

    public MobStackManager getMobStackManager() {
        return mobStackManager;
    }

    public void cleanupHolograms() {
        int removed = 0;

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof TextDisplay display)) {
                    continue;
                }

                PersistentDataContainer data = display.getPersistentDataContainer();

                boolean mobHologram = data.has(Keys.mobHologram(this), PersistentDataType.BYTE);
                boolean spawnerHologram = data.has(Keys.hologram(this), PersistentDataType.BYTE);

                if (mobHologram || spawnerHologram) {
                    display.remove();
                    removed++;
                }
            }
        }

        getLogger().info("Removed " + removed + " MasterStacker holograms.");
    }
}
