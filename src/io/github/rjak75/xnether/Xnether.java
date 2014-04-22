/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.rjak75.xnether;

import java.io.File;
import static java.lang.Math.abs;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.event.Listener;

/**
 *
 * @author Yuris
 */
public final class Xnether extends JavaPlugin implements Listener {

    // Prefix used for console output
    private String prefix;

    // General config for i.e. debugging
    private Config config;

    // Specific dynamic world configurations
    private File worldFolder;
    private final Map<String, Config> worldConfigs = new HashMap<String, Config>();

    private boolean DEBUG;
    
    @Override
    public void onEnable() {
        // Inizialize everything
        prefix = "[" + getName() + "] ";
        config = Config.loadConfig(new File(getDataFolder() + File.separator + "config.yml"));
        worldFolder = new File(getDataFolder() + File.separator + "worlds");
        DEBUG = config.getBoolean("debug", false);

        // Register listeners
        getServer().getPluginManager().registerEvents(this, this);

        // Load the configs
        loadConfigs();
    }

    @Override
    public void onDisable() {

    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true)
    public void onPlayerPortal(org.bukkit.event.player.PlayerPortalEvent event) {
        // Check if the target world is valid
        switch (event.getTo().getWorld().getEnvironment()) {
            case NETHER:
            case NORMAL:

                // Check if the current world is valid
                switch (event.getFrom().getWorld().getEnvironment()) {
                    case NETHER:
                    case NORMAL:
                        
                        World targetWorld = event.getTo().getWorld();

                        if (worldConfigs.containsKey(targetWorld.getName())) {
                            // Get the location vector for the player
                            Vector vector = worldConfigs.get(targetWorld.getName()).getVector(event.getPlayer().getName());

                            if (vector != null) {
                                Vector vector1 = worldConfigs.get(event.getFrom().getWorld().getName()).getVector(event.getPlayer().getName());
                                if (vector1 != null) {
                                    if (DEBUG) {
                                        printLine(Level.INFO, "Saved from " + event.getFrom().getWorld().getName()+ " " + event.getPlayer().getName() + ": " + String.valueOf(vector1.getX()) + ", " + String.valueOf(vector1.getY()) + ", " + String.valueOf(vector1.getZ()));
                                        printLine(Level.INFO, "Portal from " + event.getFrom().getWorld().getName()+ " " + event.getPlayer().getName() + ": " + String.valueOf(event.getFrom().getX()) + ", " + String.valueOf(event.getFrom().getY()) + ", " + String.valueOf(event.getFrom().getZ()));
                                    }
                                    if ((abs(event.getFrom().getX() - vector1.getX()) <= 4)
                                            && (abs(event.getFrom().getY() - vector1.getY()) <= 4)
                                            && (abs(event.getFrom().getZ() - vector1.getZ()) <= 4)) {
                                        // Player had a location, check for a portal
                                        // there
                                        Location location = vector.toLocation(targetWorld);

                                        boolean portalFound = false;
                                        for (BlockFace face : BlockFace.values()) {
                                            if (location.getBlock().getRelative(face).getType() == Material.PORTAL) {
                                                // Block is a portal, alternate location
                                                event.setTo(location);

                                                // Signalize that the portal has been found
                                                portalFound = true;

                                                if (DEBUG) {
                                                    printLine(Level.INFO, "Alternated teleport location for player '" + event.getPlayer().getName() + "'!");
                                                }

                                                // Break the for loop
                                                break;
                                            }
                                        }

                                        if (!portalFound) {
                                            if (DEBUG) {
                                                printLine(Level.WARNING, "Deleting '" + event.getPlayer().getName() + "'s save in world '" + targetWorld.getName()
                                                        + "' since it was not found!");
                                            }

                                            // Remove the old save
                                            worldConfigs.get(targetWorld.getName()).set(event.getPlayer().getName(), null);

                                            // Save the config
                                            worldConfigs.get(targetWorld.getName()).saveConfig();
                                        }
                                    }
                                } else {
                                    Location location = vector.toLocation(targetWorld);

                                    boolean portalFound = false;
                                    for (BlockFace face : BlockFace.values()) {
                                        if (location.getBlock().getRelative(face).getType() == Material.PORTAL) {
                                            // Block is a portal, alternate location
                                            event.setTo(location);

                                            // Signalize that the portal has been found
                                            portalFound = true;

                                            if (DEBUG) {
                                                printLine(Level.INFO, "Alternated teleport location for player '" + event.getPlayer().getName() + "'!");
                                            }

                                            // Break the for loop
                                            break;
                                        }
                                    }

                                    if (!portalFound) {
                                        if (DEBUG) {
                                            printLine(Level.WARNING, "Deleting '" + event.getPlayer().getName() + "'s save in world '" + targetWorld.getName()
                                                    + "' since it was not found!");
                                        }

                                        // Remove the old save
                                        worldConfigs.get(targetWorld.getName()).set(event.getPlayer().getName(), null);

                                        // Save the config
                                        worldConfigs.get(targetWorld.getName()).saveConfig();
                                    }
                                }
                            }
                        }
                        World sourceWorld = event.getFrom().getWorld();

                        if (worldConfigs.containsKey(sourceWorld.getName())) {
                            // Save the current location of the player for later
                            // arrival
                            worldConfigs.get(sourceWorld.getName()).set(event.getPlayer().getName(), event.getFrom().toVector());

                            // Save the config
                            worldConfigs.get(sourceWorld.getName()).saveConfig();
                            
                        }

                        break;

                    default:
                        break;

                }

            default:
                break;
        }
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true)
    public void onPlayerTeleport(org.bukkit.event.player.PlayerChangedWorldEvent event) {

        
            World targetWorld = event.getPlayer().getWorld();
            if (DEBUG) {
                printLine(Level.INFO, "Teleport " + event.getPlayer().getName() +": " + event.getPlayer().getLocation().toString());
            }
            worldConfigs.get(targetWorld.getName()).set(event.getPlayer().getName(), event.getPlayer().getLocation().toVector());

            // Save the config
            worldConfigs.get(targetWorld.getName()).saveConfig();

        
    }

    private void loadConfigs() {
        // Clear previous configs
        worldConfigs.clear();

        // Create missing folders
        worldFolder.mkdirs();

        int count = 0;
        for (World world : Bukkit.getWorlds()) {
            switch (world.getEnvironment()) {
                case NETHER:
                case NORMAL:

                    // Put in a new config
                    worldConfigs.put(world.getName(), Config.loadConfig(new File(worldFolder + File.separator + world.getName() + ".yml")));

                    // Increase the config count
                    count++;

                    break;

                default:
                    break;
            }
        }

        // Save config save
        config.set("debug", DEBUG);
        config.saveConfig();

        if (DEBUG) {
            printLine(Level.INFO, "Loaded " + count + " world configs.");
        }
    }

    private void printLine(Level level, String message) {
        Bukkit.getLogger().log(level, prefix + message);
    }
}
