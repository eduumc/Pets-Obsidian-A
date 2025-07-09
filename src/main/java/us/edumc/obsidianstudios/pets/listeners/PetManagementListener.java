package us.edumc.obsidianstudios.pets.listeners;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.gui.MyPetsGUI;
import us.edumc.obsidianstudios.pets.gui.PetLevelGUI;
import us.edumc.obsidianstudios.pets.gui.PetManagementGUI;
import us.edumc.obsidianstudios.pets.managers.ConfigManager;
import us.edumc.obsidianstudios.pets.managers.PetManager;
import us.edumc.obsidianstudios.pets.managers.PlayerDataManager;
import us.edumc.obsidianstudios.pets.models.FollowStyle;
import us.edumc.obsidianstudios.pets.models.PlayerPetData;
import us.edumc.obsidianstudios.pets.models.PetConfig;
import us.edumc.obsidianstudios.pets.util.ChatUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PetManagementListener implements Listener {

    private final PetsObsidian plugin;
    private final PetManager petManager;
    private final PlayerDataManager playerDataManager;
    private final ConfigManager configManager;
    private final Map<UUID, String> playerNamingMap;
    private final Map<UUID, String> playerParticleMap;

    public PetManagementListener(PetsObsidian plugin) {
        this.plugin = plugin;
        this.petManager = plugin.getPetManager();
        this.playerDataManager = plugin.getPlayerDataManager();
        this.configManager = plugin.getConfigManager();
        this.playerNamingMap = plugin.getPlayerNamingMap();
        this.playerParticleMap = plugin.getPlayerParticleMap();
    }

    @EventHandler
    public void onManageMenuClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(PetManagementGUI.GUI_TITLE)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack petDisplayItem = event.getInventory().getItem(4);

        if (clickedItem == null || !clickedItem.hasItemMeta() || petDisplayItem == null || !petDisplayItem.hasItemMeta()) return;

        String petId = extractPetId(petDisplayItem);
        if (petId == null) return;

        PlayerPetData petData = playerDataManager.getPetData(player, petId);
        String buttonName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        if (buttonName.contains("Cambiar Nombre")) {
            handleRename(player, petId);
        } else if (buttonName.contains("Cambiar Partículas")) {
            handleParticleChange(player, petId);
        } else if (buttonName.contains("Estilo de Seguimiento")) {
            cycleFollowStyle(player, petData);
        } else if (buttonName.contains("Activar/Desactivar Partículas")) {
            toggleParticles(player, petData);
        } else if (buttonName.contains("Activar/Desactivar Nombre")) {
            toggleDisplayName(player, petData);
        } else if (buttonName.contains("Ver Nivel")) {
            new PetLevelGUI(plugin).open(player, petId);
        } else if (buttonName.contains("Volver a Mis Mascotas")) {
            new MyPetsGUI(plugin).open(player);
        } else if (buttonName.contains("Invocar Mascota")) {
            handlePetSpawn(player, petId);
        } else if (buttonName.contains("Guardar Mascota")) {
            handlePetDespawn(player);
        }
    }

    private void handlePetSpawn(Player player, String petId) {
        player.closeInventory();
        PetConfig petConfig = configManager.getPetConfig(petId);
        if (petConfig == null) return;
        petManager.spawnPet(player, petConfig);
    }

    private void handlePetDespawn(Player player) {
        if (petManager.hasPet(player)) {
            petManager.removePet(player);
            player.sendMessage(configManager.getPrefixedMessage("pet-removed"));
            player.closeInventory();
        } else {
            player.sendMessage(configManager.getPrefixedMessage("no-pet-active"));
        }
    }

    private void handleRename(Player player, String petId) {
        player.closeInventory();
        player.sendMessage(ChatUtil.translate("&6Escribe el nuevo nombre para tu mascota en el chat. Usa códigos de color (&). Escribe 'cancelar' para salir."));
        playerNamingMap.put(player.getUniqueId(), petId);
    }

    private void handleParticleChange(Player player, String petId) {
        player.closeInventory();
        player.sendMessage(ChatUtil.translate("&dElige una partícula de la lista y escríbela en el chat:"));
        List<String> particleList = Arrays.asList("HEART", "VILLAGER_HAPPY", "FLAME", "SOUL_FIRE_FLAME", "ENCHANTMENT_TABLE", "NOTE", "TOTEM");
        player.sendMessage(ChatColor.YELLOW + String.join(", ", particleList));
        playerParticleMap.put(player.getUniqueId(), petId);
    }

    private void cycleFollowStyle(Player player, PlayerPetData petData) {
        FollowStyle[] styles = FollowStyle.values();
        int currentIndex = petData.getFollowStyle().ordinal();
        int nextIndex = (currentIndex + 1) % styles.length;
        petData.setFollowStyle(styles[nextIndex]);
        playerDataManager.savePetData(player, petData);
        player.sendMessage(ChatUtil.translate("&aEstilo de seguimiento cambiado a: &e" + petData.getFollowStyle().getDisplayName()));
        new PetManagementGUI(plugin).open(player, petData.getPetId());
    }

    private void toggleParticles(Player player, PlayerPetData petData) {
        petData.setParticlesEnabled(!petData.isParticlesEnabled());
        playerDataManager.savePetData(player, petData);
        player.sendMessage(ChatUtil.translate("&aPartículas: " + (petData.isParticlesEnabled() ? "&bActivadas" : "&cDesactivadas")));
        new PetManagementGUI(plugin).open(player, petData.getPetId());
    }

    private void toggleDisplayName(Player player, PlayerPetData petData) {
        petData.setDisplayNameVisible(!petData.isDisplayNameVisible());
        playerDataManager.savePetData(player, petData);
        player.sendMessage(ChatUtil.translate("&aVisibilidad del nombre: " + (petData.isDisplayNameVisible() ? "&bActivada" : "&cDesactivada")));

        if (petManager.hasPet(player) && petManager.getActivePet(player).getConfig().getId().equals(petData.getPetId())) {
            petManager.getActivePet(player).updateNameVisibility(petData.isDisplayNameVisible());
        }

        new PetManagementGUI(plugin).open(player, petData.getPetId());
    }

    private String extractPetId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey key = new NamespacedKey(plugin, "pet-id");
        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }
}
