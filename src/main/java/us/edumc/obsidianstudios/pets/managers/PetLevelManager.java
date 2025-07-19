package us.edumc.obsidianstudios.pets.managers;

import org.bukkit.entity.Player;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.models.Pet;
import us.edumc.obsidianstudios.pets.models.PlayerPetData;
import us.edumc.obsidianstudios.pets.util.ChatUtil;

public class PetLevelManager {

    private final PetsObsidian plugin;

    public PetLevelManager(PetsObsidian plugin) {
        this.plugin = plugin;
    }

    public void addXp(Player player, Pet pet, double amount) {
        PlayerPetData petData = plugin.getPlayerDataManager().getPetData(player, pet.getConfig().getId());
        petData.addXp(amount);

        double requiredXp = getRequiredXp(petData.getLevel());
        while (petData.getXp() >= requiredXp && requiredXp > 0) {
            petData.setXp(petData.getXp() - requiredXp);
            petData.setLevel(petData.getLevel() + 1);
            player.sendMessage(ChatUtil.translate(plugin.getConfigManager().getPrefixedMessage("level-up")
                    .replace("{pet_name}", pet.getConfig().getDisplayName())
                    .replace("{level}", String.valueOf(petData.getLevel()))));
            requiredXp = getRequiredXp(petData.getLevel());
        }

        plugin.getPlayerDataManager().savePetData(player, petData);
    }

    public double getRequiredXp(int currentLevel) {
        String formula = plugin.getConfigManager().getConfig().getString("level-settings.level-xp-formula", "100 * level");
        try {
            return new net.objecthunter.exp4j.ExpressionBuilder(formula)
                    .variable("level")
                    .build()
                    .setVariable("level", currentLevel)
                    .evaluate();
        } catch (Exception e) {
            plugin.getLogger().warning("Fórmula de XP inválida. Usando '100 * level'.");
            return 100 * currentLevel;
        }
    }

    public void setLevel(Player player, String petId, int level) {
        PlayerPetData petData = plugin.getPlayerDataManager().getPetData(player, petId);
        petData.setLevel(Math.max(1, level));
        petData.setXp(0);
        plugin.getPlayerDataManager().savePetData(player, petData);
    }

    public void giveLevels(Player player, String petId, int amount) {
        PlayerPetData petData = plugin.getPlayerDataManager().getPetData(player, petId);
        petData.setLevel(petData.getLevel() + amount);
        plugin.getPlayerDataManager().savePetData(player, petData);
    }

    public void removeLevels(Player player, String petId, int amount) {
        PlayerPetData petData = plugin.getPlayerDataManager().getPetData(player, petId);
        petData.setLevel(Math.max(1, petData.getLevel() - amount));
        plugin.getPlayerDataManager().savePetData(player, petData);
    }
}