/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.rjak75.xnether;

/**
 *
 * @author Yuris
 */
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config extends YamlConfiguration
{
    private File configFile;

    public static Config loadConfig(File configFile)
    {
        Validate.notNull(configFile, "configFile can not be null!");

        Config config = new Config();

        config.configFile = configFile;

        // Create necessary directories
        configFile.getParentFile().mkdirs();

        // Check if that file exists
        if (!configFile.exists())
        {
            try
            {
                configFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return null;
            }
        }

        try
        {
            config.load(configFile);
        }
        catch (Exception e)
        {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + configFile , e);
        }

        // Load that file
        return config;
    }

    public void saveConfig()
    {
        Validate.notNull(configFile, "Config File is not set!");

        try
        {
            super.save(configFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void deleteConfig()
    {
        configFile.delete();
    }
}

