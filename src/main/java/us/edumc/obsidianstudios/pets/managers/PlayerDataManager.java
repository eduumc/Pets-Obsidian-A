package us.edumc.obsidianstudios.pets.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.models.FollowStyle;
import us.edumc.obsidianstudios.pets.models.PlayerPetData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerDataManager {

    private final PetsObsidian plugin;
    private final File dataFolder;

    public PlayerDataManager(PetsObsidian plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    private File getPlayerFile(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".yml");
    }

    public List<String> getOwnedPets(Player player) {
        File playerFile = getPlayerFile(player.getUniqueId());
        if (!playerFile.exists()) return new ArrayList<>();
        FileConfiguration data = YamlConfiguration.loadConfiguration(playerFile);
        if (data.getConfigurationSection("owned-pets") == null) return new ArrayList<>();
        return new ArrayList<>(data.getConfigurationSection("owned-pets").getKeys(false));
    }

    public void removePet(Player player, String petId) {
        File playerFile = getPlayerFile(player.getUniqueId());
        FileConfiguration data = YamlConfiguration.loadConfiguration(playerFile);
        data.set("owned-pets." + petId, null);
        try {
            data.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerPetData getPetData(Player player, String petId) {
        PlayerPetData petData = new PlayerPetData(petId);
        File playerFile = getPlayerFile(player.getUniqueId());
        if (!playerFile.exists()) return petData;

        FileConfiguration data = YamlConfiguration.loadConfiguration(playerFile);
        String path = "owned-pets." + petId + ".";

        if (!data.contains("owned-pets." + petId)) return petData;

        petData.setCustomName(data.getString(path + "custom-name"));
        petData.setParticlesEnabled(data.getBoolean(path + "particles-enabled", true));
        try {
            petData.setFollowStyle(FollowStyle.valueOf(data.getString(path + "follow-style", "SIDE_RIGHT")));
        } catch (IllegalArgumentException e) {
            petData.setFollowStyle(FollowStyle.SIDE_RIGHT);
        }
        return petData;
    }

    public void savePetData(Player player, PlayerPetData petData) {
        File playerFile = getPlayerFile(player.getUniqueId());
        FileConfiguration data = YamlConfiguration.loadConfiguration(playerFile);
        String path = "owned-pets." + petData.getPetId() + ".";

        data.set(path + "custom-name", petData.getCustomName());
        data.set(path + "particles-enabled", petData.isParticlesEnabled());
        data.set(path + "follow-style", petData.getFollowStyle().name());

        try {
            data.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPet(Player player, String petId) {
        File playerFile = getPlayerFile(player.getUniqueId());
        FileConfiguration data = YamlConfiguration.loadConfiguration(playerFile);
        data.set("owned-pets." + petId + ".follow-style", "SIDE_RIGHT");
        data.set("owned-pets." + petId + ".particles-enabled", true);
        try {
            data.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}