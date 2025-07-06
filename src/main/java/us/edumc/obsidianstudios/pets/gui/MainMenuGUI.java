package us.edumc.obsidianstudios.pets.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.util.ChatUtil;

import java.util.Collections;

public class MainMenuGUI {

    private final PetsObsidian plugin;

    public MainMenuGUI(PetsObsidian plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatUtil.translate("§8Mascotas - Menú Principal"));

        ItemStack myPetsItem = new ItemStack(Material.CHEST);
        ItemMeta myPetsMeta = myPetsItem.getItemMeta();
        myPetsMeta.setDisplayName(ChatUtil.translate("&bMis Mascotas"));
        myPetsMeta.setLore(Collections.singletonList(ChatUtil.translate("&7Haz clic para ver las mascotas que posees.")));
        myPetsItem.setItemMeta(myPetsMeta);

        boolean shopEnabled = plugin.getConfig().getBoolean("shop.enabled", true);
        if (shopEnabled) {
            ItemStack shopItem = new ItemStack(Material.EMERALD);
            ItemMeta shopMeta = shopItem.getItemMeta();
            shopMeta.setDisplayName(ChatUtil.translate("&aTienda de Mascotas"));
            shopMeta.setLore(Collections.singletonList(ChatUtil.translate("&7Haz clic para comprar nuevas mascotas.")));
            shopItem.setItemMeta(shopMeta);
            gui.setItem(15, shopItem);
        }

        gui.setItem(11, myPetsItem);

        player.openInventory(gui);
    }
}
