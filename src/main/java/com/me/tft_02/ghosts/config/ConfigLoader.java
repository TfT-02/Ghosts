package com.me.tft_02.ghosts.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.me.tft_02.ghosts.Ghosts;

public abstract class ConfigLoader {
    protected static final Ghosts plugin = Ghosts.p;
    protected String fileName;
    protected File configFile;
    protected FileConfiguration config;

    public ConfigLoader(String relativePath, String fileName) {
        this.fileName = fileName;
        configFile = new File(plugin.getDataFolder(), relativePath + File.separator + fileName);
        loadFile();
    }

    public ConfigLoader(String fileName) {
        this.fileName = fileName;
        configFile = new File(plugin.getDataFolder(), fileName);
        loadFile();
    }

    protected void loadFile() {
        if (!configFile.exists()) {
            plugin.debug("Creating Ghosts " + fileName + " File...");
            createFile();
        }
        else {
            plugin.debug("Loading Ghosts " + fileName + " File...");
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    protected abstract void loadKeys();

    protected void createFile() {
        if (!configFile.getParentFile().mkdirs()) {
            plugin.getLogger().severe("Could not create folder at " + configFile.getParentFile());
        }

        InputStream inputStream = plugin.getResource(fileName);

        if (inputStream == null) {
            plugin.getLogger().severe("Missing resource file: '" + fileName + "' please notify the plugin authors");
            return;
        }

        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(configFile);

            int read;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                inputStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
