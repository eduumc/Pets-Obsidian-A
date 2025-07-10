package us.edumc.obsidianstudios.pets.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.gui.MainMenuGUI;
import us.edumc.obsidianstudios.pets.gui.MyPetsGUI;
import us.edumc.obsidianstudios.pets.gui.ShopGUI;

public class MainMenuListener implements Listener {

    private final PetsObsidian plugin;

    public MainMenuListener(PetsObsidian plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMainMenuClick(InventoryClickEvent event) {
        if (!ChatColor.stripColor(event.getView().getTitle()).equals("Mascotas - Menú Principal")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        if (displayName.equals("Mis Mascotas")) {
            new MyPetsGUI(plugin).open(player);
        } else if (displayName.equals("Tienda de Mascotas")) {
            new ShopGUI(plugin).open(player);
        }
    }
}
