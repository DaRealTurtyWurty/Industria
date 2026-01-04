package dev.turtywurty.industria.config;

import com.google.gson.JsonObject;
import dev.turtywurty.industria.Industria;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ServerConfig {
    private static final LevelResource CONFIG_PATH = new LevelResource("config/" + Industria.MOD_ID + ".json");
    private static final LevelResource BACKUP_PATH = new LevelResource("config/" + Industria.MOD_ID + ".json.bak");

    private static ServerConfig currentConfig;

    public static void onServerLoad(MinecraftServer server) {
        currentConfig = readConfig(server);
    }

    public static void onServerSave(MinecraftServer server) {
        writeConfig(currentConfig, server);
    }

    public static void onReload(MinecraftServer server) {
        currentConfig = readConfig(server);
    }

    public static ServerConfig getConfig() {
        return currentConfig;
    }

    private static ServerConfig readConfig(MinecraftServer server) {
        Path configPath = server.getWorldPath(CONFIG_PATH);
        try {
            var config = new ServerConfig();
            if (Files.notExists(configPath)) {
                writeConfig(config, server);
                return config;
            }

            String jsonStr = Files.readString(configPath);
            JsonObject json = Industria.GSON.fromJson(jsonStr, JsonObject.class);
            config.deserialize(json);
            return config;
        } catch (IOException exception) {
            Industria.LOGGER.error("Failed to read config file!", exception);

            // make a backup of the config file
            backupConfig(server);

            var config = new ServerConfig();
            writeConfig(config, server);
            return config;
        }
    }

    private static void writeConfig(ServerConfig config, MinecraftServer server) {
        Path configPath = server.getWorldPath(CONFIG_PATH);
        try {
            Files.createDirectories(configPath.getParent());
            JsonObject json = config.serialize();
            Files.writeString(configPath, Industria.GSON.toJson(json));
        } catch (IOException exception) {
            Industria.LOGGER.error("Failed to write config file!", exception);
        }
    }

    private static void backupConfig(MinecraftServer server) {
        Path configPath = server.getWorldPath(CONFIG_PATH);
        if (Files.notExists(configPath))
            return;

        Path backupPath = server.getWorldPath(BACKUP_PATH);
        try {
            Files.createDirectories(backupPath.getParent());
            Files.move(configPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            Industria.LOGGER.error("Failed to backup config file!", exception);
        }
    }

    private JsonObject serialize() {
        return new JsonObject();
    }

    private void deserialize(JsonObject json) {

    }
}
