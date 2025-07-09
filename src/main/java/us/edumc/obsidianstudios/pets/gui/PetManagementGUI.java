package us.edumc.obsidianstudios.pets.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.models.PetConfig;
import us.edumc.obsidianstudios.pets.models.PlayerPetData;
import us.edumc.obsidianstudios.pets.util.ChatUtil;

import java.util.ArrayList;
import java.util.List;

public class PetManagementGUI {

    public static final String GUI_TITLE = ChatUtil.translate("§bGestionar Mascota");
    private final PetsObsidian plugin;

    public PetManagementGUI(PetsObsidian plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, String petId) {
        PetConfig petConfig = plugin.getConfigManager().getPetConfig(petId);
        PlayerPetData petData = plugin.getPlayerDataManager().getPetData(player, petId);
        if (petConfig == null) return;

        Inventory gui = Bukkit.createInventory(null, 36, GUI_TITLE);

        ItemStack petItem = petConfig.getGuiItem(player, false);
        ItemMeta petMeta = petItem.getItemMeta();
        String displayName = petData.getCustomName() != null ? petData.getCustomName() : petConfig.getDisplayName();
        petMeta.setDisplayName(ChatUtil.translate(displayName));

        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.translate("&7Estilo de seguimiento: &e" + petData.getFollowStyle().getDisplayName()));
        lore.add(ChatUtil.translate("&7Partículas: " + (petData.isParticlesEnabled() ? "&aActivadas" : "&cDesactivadas")));
        lore.add(ChatUtil.translate("&7Nombre visible: " + (petData.isDisplayNameVisible() ? "&aSí" : "&cNo")));
        petMeta.setLore(lore);
        petItem.setItemMeta(petMeta);

        // --- Creación de los botones de la GUI ---
        ItemStack renameItem = createGuiItem(Material.ANVIL, "&6Cambiar Nombre", "&7Haz clic para asignarle un apodo.");
        ItemStack particleItem = createGuiItem(Material.BLAZE_POWDER, "&dCambiar Partículas", "&7Haz clic para elegir un efecto.");
        ItemStack followItem = createGuiItem(Material.LEAD, "&eEstilo de Seguimiento", "&7Haz clic para cambiar cómo te sigue.");
        ItemStack toggleParticlesItem = createGuiItem(petData.isParticlesEnabled() ? Material.GLOWSTONE_DUST : Material.GUNPOWDER, "&fActivar/Desactivar Partículas", petData.isParticlesEnabled() ? "&cClic para desactivar" : "&aClic para activar");
        ItemStack toggleNameItem = createGuiItem(petData.isDisplayNameVisible() ? Material.NAME_TAG : Material.BARRIER, "&fActivar/Desactivar Nombre", petData.isDisplayNameVisible() ? "&cClic para desactivar" : "&aClic para activar");

        // ================== CORRECCIÓN AQUÍ ==================
        // Se ha añadido el botón para ver los niveles de la mascota.
        ItemStack levelItem = createGuiItem(Material.NETHER_STAR, "&bVer Nivel", "&7Haz clic para ver el progreso y las recompensas.");
        // =====================================================

        ItemStack spawnItem = createGuiItem(Material.DIAMOND, "&a» Invocar Mascota «", "&7Haz clic para que esta mascota te siga.");
        ItemStack despawnItem = createGuiItem(Material.BONE, "&c» Guardar Mascota «", "&7Haz clic para guardar tu mascota activa.");
        ItemStack backButton = createGuiItem(Material.BARRIER, "&c« Volver a Mis Mascotas");

        // --- Posicionamiento de los botones en la GUI ---
        gui.setItem(4, petItem);
        gui.setItem(11, renameItem);
        gui.setItem(12, particleItem);
        gui.setItem(13, toggleNameItem);
        gui.setItem(14, followItem);
        gui.setItem(15, toggleParticlesItem);

        gui.setItem(22, levelItem); // Botón de niveles en el centro

        gui.setItem(27, backButton);
        gui.setItem(34, despawnItem);
        gui.setItem(35, spawnItem);

        player.openInventory(gui);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatUtil.translate(name));
        if (lore.length > 0) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(ChatUtil.translate(line));
            }
            meta.setLore(loreList);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }
}