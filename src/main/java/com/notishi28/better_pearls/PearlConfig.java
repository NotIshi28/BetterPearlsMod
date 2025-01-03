package com.notishi28.better_pearls;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class PearlConfig {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLLoader.getGamePath().resolve("config").resolve("better_pearls.json");
    private static PearlConfig instance;

    private int pearlCooldown = 80;
    private boolean canRide = true;
    private boolean waterBounce = true;

    public static PearlConfig getInstance() {
        if (instance == null) {
            instance = loadConfig();
        }
        return instance;
    }

    public int getPearlCooldown() {
    
        return pearlCooldown;
    
    }

    public boolean getCanRide() {
    
        return canRide;
    
    }

    public boolean getWaterBounce() {
    
        return waterBounce;
    
    }

    private static PearlConfig loadConfig() {
        PearlConfig config;

        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                config = GSON.fromJson(reader, PearlConfig.class);
            } catch (Exception e) {
                System.err.println("Error loading config: " + e.getMessage());
                config = new PearlConfig();
                saveConfig(config);
            }
        } else {
            config = new PearlConfig();
            saveConfig(config);
        }

        return config;
    }

    private static void saveConfig(PearlConfig config) {
    
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }
        } 
        
        catch (IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
        }
    
    }
}