package us.edumc.obsidianstudios.pets.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.managers.PetLevelManager;
import us.edumc.obsidianstudios.pets.models.PetConfig;
import us.edumc.obsidianstudios.pets.models.PlayerPetData;
import us.edumc.obsidianstudios.pets.util.ChatUtil;

import java.util.ArrayList;
import java.util.List;

public class PetLevelGUI {

    public static final String GUI_TITLE = ChatUtil.translate("&3Nivel de Mascota");
    private final PetsObsidian plugin;

    public PetLevelGUI(PetsObsidian plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, String petId) {
        PetConfig petConfig = plugin.getConfigManager().getPetConfig(petId);
        PlayerPetData petData = plugin.getPlayerDataManager().getPetData(player, petId);
        if (petConfig == null) return;

        Inventory gui = Bukkit.createInventory(null, 45, GUI_TITLE);

        // --- Item de Información de la Mascota ---
        ItemStack petItem = petConfig.createHead();
        ItemMeta petMeta = petItem.getItemMeta();
        String displayName = petData.getCustomName() != null ? petData.getCustomName() : petConfig.getDisplayName();
        petMeta.setDisplayName(ChatUtil.translate(displayName));

        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.translate("&7Nivel: &e" + petData.getLevel()));
        lore.add(createXpBar(petData.getXp(), plugin.getPetLevelManager().getRequiredXp(petData.getLevel())));
        lore.add(ChatUtil.translate(String.format("&7XP: &a%.2f &7/ &c%.2f", petData.getXp(), plugin.getPetLevelManager().getRequiredXp(petData.getLevel()))));
        petMeta.setLore(lore);
        petItem.setItemMeta(petMeta);
        gui.setItem(4, petItem);

        // --- Relleno de la GUI ---
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        // --- Botón de Volver ---
        ItemStack backButton = createGuiItem(Material.BARRIER, "&c« Volver");
        gui.setItem(40, backButton);

        player.openInventory(gui);
    }

    private String createXpBar(double currentXp, double requiredXp) {
        int barLength = 20;
        int progress = (int) ((currentXp / requiredXp) * barLength);
        StringBuilder bar = new StringBuilder("&a");
        for (int i = 0; i < barLength; i++) {
            if (i < progress) {
                bar.append("▌");
            } else {
                if (i == progress) bar.append("&7");
                bar.append("▌");
            }
        }
        return ChatUtil.translate(bar.toString());
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