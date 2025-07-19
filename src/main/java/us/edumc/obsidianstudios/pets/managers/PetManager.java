package us.edumc.obsidianstudios.pets.managers;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.models.Pet;
import us.edumc.obsidianstudios.pets.models.PetConfig;
import us.edumc.obsidianstudios.pets.models.PlayerPetData;
import us.edumc.obsidianstudios.pets.tasks.PetFollowTask;
import us.edumc.obsidianstudios.pets.util.ChatUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PetManager {

    private final PetsObsidian plugin;
    private final Map<UUID, Pet> activePets = new HashMap<>();
    public static final NamespacedKey PET_OWNER_KEY = new NamespacedKey(PetsObsidian.getInstance(), "pet-owner-uuid");

    public PetManager(PetsObsidian plugin) {
        this.plugin = plugin;
    }

    public void spawnPet(Player player, PetConfig config) {
        if (plugin.getWorldGuardIntegration() != null && !plugin.getWorldGuardIntegration().isAllowed(player)) {
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("region-denied"));
            return;
        }

        if (hasPet(player)) {
            removePet(player, false); // No enviar mensaje de guardado aquí
            player.sendMessage(plugin.getConfigManager().getPrefixedMessage("pet-already-active"));
        }

        ArmorStand petEntity = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);

        petEntity.setVisible(false);
        petEntity.setGravity(false);
        petEntity.setSmall(true);
        petEntity.setInvulnerable(true);
        petEntity.setCollidable(false);
        petEntity.setMarker(true);
        petEntity.setBasePlate(false);

        petEntity.getEquipment().setHelmet(config.createHead());
        petEntity.getPersistentDataContainer().set(PET_OWNER_KEY, PersistentDataType.STRING, player.getUniqueId().toString());

        PlayerPetData petData = plugin.getPlayerDataManager().getPetData(player, config.getId());
        if (config.isShowDisplayName()) {
            String name = petData.getCustomName() != null ? petData.getCustomName() : config.getDisplayName();
            petEntity.setCustomName(ChatUtil.translate(name));
            petEntity.setCustomNameVisible(petData.isDisplayNameVisible());
        }

        PetFollowTask followTask = new PetFollowTask(player, petEntity, config);
        followTask.runTaskTimer(plugin, 0L, 1L);

        Pet pet = new Pet(player, config, petEntity, followTask);
        activePets.put(player.getUniqueId(), pet);

        String message = plugin.getConfigManager().getPrefixedMessage("pet-summoned")
                .replace("{pet_name}", ChatUtil.translate(config.getDisplayName()));
        player.sendMessage(message);
    }

    public void removePet(Player player) {
        removePet(player, true);
    }

    private void removePet(Player player, boolean sendRemoveMessage) {
        Pet pet = activePets.remove(player.getUniqueId());
        if (pet != null) {
            pet.remove();
            clearPetEffects(player, pet.getConfig());
            if (sendRemoveMessage) {
                player.sendMessage(plugin.getConfigManager().getPrefixedMessage("pet-removed"));
            }
        }
    }

    public void removeAllPets() {
        for (UUID playerId : new HashMap<>(activePets).keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                removePet(player, false);
            } else {
                Pet pet = activePets.remove(playerId);
                if (pet != null) pet.remove();
            }
        }
        activePets.clear();
    }

    private void clearPetEffects(Player player, PetConfig config) {
        // Eliminar efectos base
        if (config.getEffects() != null) {
            removeEffectList(player, config.getEffects());
        }

        // Eliminar efectos de recompensas por nivel
        PlayerPetData petData = plugin.getPlayerDataManager().getPetData(player, config.getId());
        Map<String, Object> rewards = config.getRewards();
        if (rewards != null) {
            for (int i = 1; i <= petData.getLevel(); i++) {
                String levelKey = String.valueOf(i);
                if (rewards.containsKey(levelKey)) {
                    Object rawData = rewards.get(levelKey);
                    // Los datos de rewards pueden venir como un ConfigurationSection
                    if (rawData instanceof Map || rawData instanceof ConfigurationSection) {
                        Map<String, Object> levelData;
                        if (rawData instanceof ConfigurationSection) {
                            levelData = ((ConfigurationSection) rawData).getValues(false);
                        } else {
                            levelData = (Map<String, Object>) rawData;
                        }

                        if (levelData.containsKey("effects")) {
                            List<String> effects = (List<String>) levelData.get("effects");
                            if (effects != null) {
                                removeEffectList(player, effects);
                            }
                        }
                    }
                }
            }
        }
    }

    private void removeEffectList(Player player, List<String> effects) {
        for (String effectString : effects) {
            try {
                String[] parts = effectString.split(":");
                PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
                int amplifier = Integer.parseInt(parts[1]);
                if (type != null) {
                    PotionEffect activeEffect = player.getPotionEffect(type);
                    // Solo eliminar el efecto si el amplificador coincide
                    if (activeEffect != null && activeEffect.getAmplifier() == amplifier) {
                        player.removePotionEffect(type);
                    }
                }
            } catch (Exception ignored) {}
        }
    }


    public boolean hasPet(Player player) {
        return activePets.containsKey(player.getUniqueId());
    }

    public Pet getActivePet(Player player) {
        return activePets.get(player.getUniqueId());
    }
}