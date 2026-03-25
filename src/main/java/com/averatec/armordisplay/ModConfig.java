package com.averatec.armordisplay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("armor_display_averatec.json");

    public boolean enabled = true;
    public int textColor = 0xFFFFFF;

    public static final int[] COLOR_PRESETS = {
            0xFFFFFF, // White
            0xFFFF55, // Yellow
            0x55FFFF, // Cyan
            0x55FF55, // Green
    };
    public static final String[] COLOR_NAMES = {
            "color.armor_display_averatec.white",
            "color.armor_display_averatec.yellow",
            "color.armor_display_averatec.cyan",
            "color.armor_display_averatec.green",
    };

    private static ModConfig instance;

    public static ModConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static ModConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                ModConfig config = GSON.fromJson(reader, ModConfig.class);
                return config != null ? config : new ModConfig();
            } catch (IOException e) {
                return new ModConfig();
            }
        }
        return new ModConfig();
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(this, writer);
        } catch (IOException ignored) {
        }
    }
}
