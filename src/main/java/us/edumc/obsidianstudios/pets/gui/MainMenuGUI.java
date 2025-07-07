package us.edumc.obsidianstudios.pets.gui;

import net.kyori.adventure.text.Component;
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
        Inventory gui = Bukkit.createInventory(null, 27, "§8Mascotas - Menú Principal");

        ItemStack myPetsItem = new ItemStack(Material.CHEST);
        ItemMeta myPetsMeta = myPetsItem.getItemMeta();
        myPetsMeta.displayName(ChatUtil.parse("<aqua>Mis Mascotas</aqua>"));
        myPetsMeta.lore(Collections.singletonList(ChatUtil.parse("<gray>Haz clic para ver las mascotas que posees.")));
        myPetsItem.setItemMeta(myPetsMeta);

        boolean shopEnabled = plugin.getConfig().getBoolean("shop.enabled", true);
        if (shopEnabled) {
            ItemStack shopItem = new ItemStack(Material.EMERALD);
            ItemMeta shopMeta = shopItem.getItemMeta();
            shopMeta.displayName(ChatUtil.parse("<green>Tienda de Mascotas</green>"));
            shopMeta.lore(Collections.singletonList(ChatUtil.parse("<gray>Haz clic para comprar nuevas mascotas.")));
            shopItem.setItemMeta(shopMeta);
            gui.setItem(15, shopItem);
        }

        gui.setItem(11, myPetsItem);

        player.openInventory(gui);
    }
}
