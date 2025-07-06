package us.edumc.obsidianstudios.pets.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.gui.MainMenuGUI;
import us.edumc.obsidianstudios.pets.gui.MyPetsGUI;
import us.edumc.obsidianstudios.pets.gui.PetManagementGUI;
import us.edumc.obsidianstudios.pets.gui.ShopGUI;
import us.edumc.obsidianstudios.pets.integrations.VaultIntegration;
import us.edumc.obsidianstudios.pets.managers.ConfigManager;
import us.edumc.obsidianstudios.pets.managers.PlayerDataManager;
import us.edumc.obsidianstudios.pets.models.PetConfig;
import us.edumc.obsidianstudios.pets.util.ChatUtil;

public class GUIListener implements Listener {

    private final PetsObsidian plugin;
    private final ConfigManager configManager;
    private final PlayerDataManager playerDataManager;

    public GUIListener(PetsObsidian plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = ChatColor.stripColor(event.getView().getTitle());
        boolean isMyPetsGUI = title.equals("Mis Mascotas");
        boolean isShopGUI = title.equals("Tienda de Mascotas");

        if (!isMyPetsGUI && !isShopGUI) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        if (clickedItem.getType() == Material.BARRIER) {
            new MainMenuGUI(plugin).open(player);
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "pet-id");
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) return;

        String petId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        PetConfig petConfig = configManager.getPetConfig(petId);
        if (petConfig == null) return;

        if (isMyPetsGUI) {
            new PetManagementGUI(plugin).open(player, petId);
        }

        if (isShopGUI) {
            if (playerDataManager.getOwnedPets(player).contains(petId)) {
                player.sendMessage(ChatUtil.translate("&6Ya posees esta mascota. Puedes gestionarla desde 'Mis Mascotas'."));
            } else {
                handlePetPurchase(player, petConfig);
            }
        }
    }

    private void handlePetPurchase(Player player, PetConfig petConfig) {
        if (!petConfig.hasPermission(player)) {
            player.sendMessage(configManager.getPrefixedMessage("no-permission"));
            player.closeInventory();
            return;
        }

        VaultIntegration vault = plugin.getVaultIntegration();
        boolean economyEnabled = configManager.getConfig().getBoolean("economy.enabled", false);

        if (economyEnabled) {
            if (vault == null) {
                plugin.getLogger().warning("La compra de mascotas falló porque Vault no está disponible o no hay un proveedor de economía.");
                player.sendMessage(ChatColor.RED + "El sistema de economía no está funcionando. Contacta a un administrador.");
                return;
            }
            if (petConfig.getPrice() > 0) {
                if (!vault.hasEnough(player, petConfig.getPrice())) {
                    String message = configManager.getPrefixedMessage("not-enough-money")
                            .replace("{price}", String.valueOf(petConfig.getPrice()));
                    player.sendMessage(message);
                    player.closeInventory();
                    return;
                }
                if (!vault.withdraw(player, petConfig.getPrice())) {
                    player.sendMessage(ChatUtil.translate("&cOcurrió un error durante la transacción. Inténtalo de nuevo."));
                    player.closeInventory();
                    return;
                }
            }
        }

        playerDataManager.addPet(player, petConfig.getId());
        player.sendMessage(ChatUtil.translate("&a¡Has comprado la mascota " + petConfig.getDisplayName() + "&a!"));

        new ShopGUI(plugin).open(player);
    }
}