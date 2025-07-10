package us.edumc.obsidianstudios.pets.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.managers.ConfigManager;
import us.edumc.obsidianstudios.pets.managers.PlayerDataManager;
import us.edumc.obsidianstudios.pets.models.PetConfig;
import us.edumc.obsidianstudios.pets.util.ChatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShopGUI {

    public static final String GUI_TITLE = ChatUtil.translate("§8Tienda de Mascotas");
    private final PetsObsidian plugin;
    private final ConfigManager configManager;
    private final PlayerDataManager playerDataManager;

    public ShopGUI(PetsObsidian plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    public void open(Player player) {
        List<PetConfig> shopPets = configManager.getAllPetConfigs().values().stream()
                .filter(PetConfig::isShowInShop)
                .collect(Collectors.toList());

        int petCount = shopPets.size();
        int guiSize = Math.max(18, (int) (Math.ceil(petCount / 9.0) * 9) + 9);
        if (guiSize > 54) guiSize = 54;

        Inventory gui = Bukkit.createInventory(null, guiSize, GUI_TITLE);

        for (PetConfig petConfig : shopPets) {
            ItemStack item = petConfig.getGuiItem(player, true);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

            lore.add("");

            if (playerDataManager.getOwnedPets(player).contains(petConfig.getId())) {
                lore.add(ChatUtil.translate("&6✔ Ya la posees"));
            } else if (petConfig.hasPermission(player)) {
                lore.add(ChatUtil.translate("&a▶ Clic para comprar por &b$" + petConfig.getPrice()));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.addItem(item);
        }

        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatUtil.translate("&c« Volver"));
        backButton.setItemMeta(backMeta);
        gui.setItem(guiSize - 5, backButton);

        player.openInventory(gui);
    }
}