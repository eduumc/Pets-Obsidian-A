package us.edumc.obsidianstudios.pets.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.models.PetConfig;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {

    private final PetsObsidian plugin;
    private FileConfiguration config;
    private final Map<String, PetConfig> petConfigs = new HashMap<>();

    public ConfigManager(PetsObsidian plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadPetConfigs();
    }

    private void loadPetConfigs() {
        petConfigs.clear();
        File petsFolder = new File(plugin.getDataFolder(), "pets");
        if (!petsFolder.exists()) {
            petsFolder.mkdirs();
            plugin.saveResource("pets/gryffindor.yml", false);
            plugin.saveResource("pets/slytherin.yml", false);
            plugin.saveResource("pets/ravenclaw.yml", false);
            plugin.saveResource("pets/hufflepuff.yml", false);
        }

        File[] petFiles = petsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (petFiles == null) return;

        for (File petFile : petFiles) {
            try {
                FileConfiguration petYml = YamlConfiguration.loadConfiguration(petFile);

                String id = petYml.getString("id", petFile.getName().replace(".yml", ""));
                String displayName = petYml.getString("display-name", "Mascota sin nombre");
                boolean showDisplayName = petYml.getBoolean("show-display-name", false);
                List<String> lore = petYml.getStringList("lore");
                String headType = petYml.getString("head-type", "player");
                String headTexture = petYml.getString("head-texture");
                double price = petYml.getDouble("price", 0.0);
                String permission = petYml.getString("permission", "");
                boolean showInShop = petYml.getBoolean("shop", true);
                List<String> effects = petYml.getStringList("effects");
                List<String> onHitEffects = petYml.getStringList("on-hit-effects");
                int onHitCooldown = petYml.getInt("on-hit-cooldown", 200);

                Map<String, Object> particles = new HashMap<>();
                ConfigurationSection particleSection = petYml.getConfigurationSection("particles");
                if (particleSection != null) {
                    particles = particleSection.getValues(false);
                }

                Map<String, Object> rewards = new HashMap<>();
                ConfigurationSection rewardsSection = petYml.getConfigurationSection("rewards");
                if (rewardsSection != null) {
                    rewards = rewardsSection.getValues(true);
                }

                PetConfig petConfig = new PetConfig(id, displayName, showDisplayName, lore, headType, headTexture, price, permission, effects, onHitEffects, onHitCooldown, particles, showInShop, rewards);
                petConfigs.put(id, petConfig);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error al cargar el archivo de mascota: " + petFile.getName(), e);
            }
        }
        plugin.getLogger().info("Se cargaron " + petConfigs.size() + " mascotas.");
    }

    public String getPrefixedMessage(String key) {
        String prefix = config.getString("prefix", "&d&lPets &8» &r");
        String message = config.getString("messages." + key, "&cMessage not found: " + key);
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    public FileConfiguration getConfig() { return config; }
    public PetConfig getPetConfig(String id) { return petConfigs.get(id); }
    public Map<String, PetConfig> getAllPetConfigs() { return petConfigs; }
}