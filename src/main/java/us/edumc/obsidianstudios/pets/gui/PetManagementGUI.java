package us.edumc.obsidianstudios.pets.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.managers.ConfigManager;
import us.edumc.obsidianstudios.pets.managers.PetManager;
import us.edumc.obsidianstudios.pets.managers.PlayerDataManager;
import us.edumc.obsidianstudios.pets.models.PetConfig;
import us.edumc.obsidianstudios.pets.models.PlayerPetData;
import us.edumc.obsidianstudios.pets.util.ChatUtil;
import us.edumc.obsidianstudios.pets.models.FollowStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PetManagementGUI {

    private final PetsObsidian plugin;
    private final PetManager petManager;
    private final PlayerDataManager playerDataManager;
    private final ConfigManager configManager;
    private final Map<UUID, String> playerNamingMap;
    private final Map<UUID, String> playerParticleMap;

    public PetManagementGUI(PetsObsidian plugin) {
        this.plugin = plugin;
        this.petManager = plugin.getPetManager();
        this.playerDataManager = plugin.getPlayerDataManager();
        this.configManager = plugin.getConfigManager();
        this.playerNamingMap = plugin.getPlayerNamingMap();
        this.playerParticleMap = plugin.getPlayerParticleMap();
    }

    public void open(Player player, String petId) {
        PetConfig petConfig = plugin.getConfigManager().getPetConfig(petId);
        PlayerPetData petData = plugin.getPlayerDataManager().getPetData(player, petId);
        if (petConfig == null) return;

        Inventory gui = Bukkit.createInventory(null, 36, "§bGestionar Mascota");

        ItemStack petItem = petConfig.createHead();
        ItemMeta petMeta = petItem.getItemMeta();
        String displayName = petData.getCustomName() != null ? petData.getCustomName() : petConfig.getDisplayName();
        petMeta.displayName(ChatUtil.parse(displayName));

        List<Component> lore = new ArrayList<>();
        lore.add(ChatUtil.parse("<gray>Estilo de seguimiento: <yellow>" + petData.getFollowStyle().getDisplayName()));
        lore.add(ChatUtil.parse("<gray>Partículas: " + (petData.isParticlesEnabled() ? "<green>Activadas" : "<red>Desactivadas")));
        lore.add(Component.empty());
        lore.add(ChatUtil.parse("<black>ID: " + petId));
        petMeta.lore(lore);
        petItem.setItemMeta(petMeta);

        ItemStack renameItem = createGuiItem(Material.ANVIL, "<gold>Cambiar Nombre", "<gray>Haz clic para asignarle un apodo.");
        ItemStack followItem = createGuiItem(Material.LEAD, "<yellow>Estilo de Seguimiento", "<gray>Haz clic para cambiar cómo te sigue.");
        ItemStack toggleParticlesItem = createGuiItem(petData.isParticlesEnabled() ? Material.GLOWSTONE_DUST : Material.GUNPOWDER, "<white>Activar/Desactivar Partículas", petData.isParticlesEnabled() ? "<red>Clic para desactivar" : "<green>Clic para activar");

        ItemStack spawnItem = createGuiItem(Material.DIAMOND, "<green>» Invocar Mascota «", "<gray>Haz clic para que esta mascota te siga.");
        ItemStack despawnItem = createGuiItem(Material.BONE, "<red>» Guardar Mascota «", "<gray>Haz clic para guardar tu mascota activa.");

        ItemStack backButton = createGuiItem(Material.BARRIER, "<red>« Volver a Mis Mascotas");

        gui.setItem(4, petItem);
        gui.setItem(11, renameItem);
        gui.setItem(13, followItem);
        gui.setItem(15, toggleParticlesItem);

        gui.setItem(27, backButton);
        gui.setItem(34, despawnItem);
        gui.setItem(35, spawnItem);

        player.openInventory(gui);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ChatUtil.parse(name));
        if (lore.length > 0) {
            List<Component> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(ChatUtil.parse(line));
            }
            meta.lore(loreList);
        }
        item.setItemMeta(meta);
        return item;
    }
}