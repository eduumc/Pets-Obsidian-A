package us.edumc.obsidianstudios.pets.gui;

import net.kyori.adventure.text.Component;
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

        Inventory gui = Bukkit.createInventory(null, guiSize, "§eTienda de Mascotas");

        for (PetConfig petConfig : shopPets) {
            ItemStack item = petConfig.getGuiItem(player, true);
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();

            lore.add(Component.empty());

            if (playerDataManager.getOwnedPets(player).contains(petConfig.getId())) {
                lore.add(ChatUtil.parse("<gold>✔ Ya la posees</gold>"));
            } else if (petConfig.hasPermission(player)) {
                lore.add(ChatUtil.parse("<green>▶ Clic para comprar por <aqua>$" + petConfig.getPrice() + "</aqua></green>"));
            }

            meta.lore(lore);
            item.setItemMeta(meta);
            gui.addItem(item);
        }

        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.displayName(ChatUtil.parse("<red>« Volver</red>"));
        backButton.setItemMeta(backMeta);
        gui.setItem(guiSize - 5, backButton);

        player.openInventory(gui);
    }
}