package us.edumc.obsidianstudios.pets.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.models.Pet;
import us.edumc.obsidianstudios.pets.models.PlayerPetData;
import us.edumc.obsidianstudios.pets.util.ChatUtil;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.List;

public class PetLevelManager {

    private final PetsObsidian plugin;
    private final PlayerDataManager playerDataManager;
    private final ConfigManager configManager;
    private final String xpFormula;

    public PetLevelManager(PetsObsidian plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
        this.configManager = plugin.getConfigManager();
        this.xpFormula = configManager.getLevelsConfig().getString("level-xp-formula", "100 * level");
    }

    public void addXp(Player player, Pet pet, double amount) {
        PlayerPetData petData = playerDataManager.getPetData(player, pet.getConfig().getId());
        petData.addXp(amount);

        if (petData.getXp() >= getRequiredXp(petData.getLevel())) {
            levelUp(player, pet, petData);
        }

        playerDataManager.savePetData(player, petData);
    }

    private void levelUp(Player player, Pet pet, PlayerPetData petData) {
        double requiredXp = getRequiredXp(petData.getLevel());
        while (petData.getXp() >= requiredXp) {
            petData.setXp(petData.getXp() - requiredXp);
            petData.setLevel(petData.getLevel() + 1);
            player.sendMessage(ChatUtil.translate("&a¡Tu mascota &e" + pet.getConfig().getDisplayName() + "&a ha subido al nivel &6" + petData.getLevel() + "!"));
            applyLevelRewards(player, petData.getLevel());
            requiredXp = getRequiredXp(petData.getLevel());
        }
    }

    public double getRequiredXp(int currentLevel) {
        String formula = xpFormula.replace("level", String.valueOf(currentLevel));
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return ((Number) engine.eval(formula)).doubleValue();
        } catch (Exception e) {
            plugin.getLogger().warning("Fórmula de XP inválida en levels.yml: " + xpFormula);
            return 100 * currentLevel; // Fórmula de respaldo
        }
    }

    private void applyLevelRewards(Player player, int newLevel) {
        ConfigurationSection rewardsSection = configManager.getLevelsConfig().getConfigurationSection("rewards." + newLevel);
        if (rewardsSection == null) return;

        List<String> effects = rewardsSection.getStringList("effects");
        if (effects != null) {
            for (String effectString : effects) {
                try {
                    String[] parts = effectString.split(":");
                    PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
                    int amplifier = Integer.parseInt(parts[1]);
                    if (type != null) {
                        player.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, amplifier, true, false, true));
                        player.sendMessage(ChatUtil.translate("&b¡Tu mascota ha aprendido a darte " + type.getName() + "!"));
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Efecto de recompensa mal configurado para el nivel " + newLevel + ": " + effectString);
                }
            }
        }
    }
}