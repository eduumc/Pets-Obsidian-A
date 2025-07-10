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

import java.util.List;

public class MyPetsGUI {

    public static final String GUI_TITLE = ChatUtil.translate("§8Mis Mascotas");
    private final PetsObsidian plugin;
    private final ConfigManager configManager;
    private final PlayerDataManager playerDataManager;

    public MyPetsGUI(PetsObsidian plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    public void open(Player player) {
        List<String> ownedPets = playerDataManager.getOwnedPets(player);
        int guiSize = Math.max(18, (int) (Math.ceil(ownedPets.size() / 9.0) * 9) + 9);
        if (guiSize > 54) guiSize = 54;

        Inventory gui = Bukkit.createInventory(null, guiSize, GUI_TITLE);

        for (String petId : ownedPets) {
            PetConfig petConfig = configManager.getPetConfig(petId);
            if (petConfig != null) {
                gui.addItem(petConfig.getGuiItem(player, false));
            }
        }

        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatUtil.translate("&c« Volver"));
        backButton.setItemMeta(backMeta);
        gui.setItem(guiSize - 5, backButton);

        player.openInventory(gui);
    }
}
