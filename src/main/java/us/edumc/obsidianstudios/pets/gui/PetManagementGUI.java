package us.edumc.obsidianstudios.pets.gui;

import org.bukkit.Bukkit;
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

        // --- Ítem de la Mascota ---
        ItemStack petItem = petConfig.getGuiItem(player, false);
        ItemMeta petMeta = petItem.getItemMeta();
        String displayName = petData.getCustomName() != null ? petData.getCustomName() : petConfig.getDisplayName();
        petMeta.setDisplayName(ChatUtil.translate(displayName));

        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.translate("&7Nivel: &e" + petData.getLevel()));
        double requiredXp = plugin.getPetLevelManager().getRequiredXp(petData.getLevel());
        lore.add(createXpBar(petData.getXp(), requiredXp));
        lore.add(ChatUtil.translate(String.format("&7XP: &a%.2f &7/ &c%.2f", petData.getXp(), requiredXp)));
        lore.add("");
        lore.add(ChatUtil.translate("&7Estilo de seguimiento: &e" + petData.getFollowStyle().getDisplayName()));
        lore.add(ChatUtil.translate("&7Partículas: " + (petData.isParticlesEnabled() ? "&aActivadas" : "&cDesactivadas")));
        lore.add(ChatUtil.translate("&7Nombre visible: " + (petData.isDisplayNameVisible() ? "&aSí" : "&cNo")));
        petMeta.setLore(lore);
        petItem.setItemMeta(petMeta);

        gui.setItem(4, petItem);

        // --- Botones de Gestión ---
        if (plugin.getConfig().getBoolean("gui-settings.enable-rename", true)) {
            ItemStack renameItem = createGuiItem(Material.NAME_TAG, "&6Cambiar Nombre", "&7Haz clic para asignarle un apodo.");
            gui.setItem(11, renameItem);
        } else {
            gui.setItem(11, createDisabledItem());
        }

        if (plugin.getConfig().getBoolean("gui-settings.enable-particle-change", true)) {
            ItemStack particleItem = createGuiItem(Material.BLAZE_POWDER, "&dCambiar Partículas", "&7Haz clic para elegir un efecto.");
            gui.setItem(12, particleItem);
        } else {
            gui.setItem(12, createDisabledItem());
        }

        if (plugin.getConfig().getBoolean("gui-settings.enable-name-toggle", true)) {
            ItemStack toggleNameItem = createGuiItem(petData.isDisplayNameVisible() ? Material.LIME_DYE : Material.GRAY_DYE, "&fActivar/Desactivar Nombre", petData.isDisplayNameVisible() ? "&cClic para desactivar" : "&aClic para activar");
            gui.setItem(13, toggleNameItem);
        } else {
            gui.setItem(13, createDisabledItem());
        }

        if (plugin.getConfig().getBoolean("gui-settings.enable-follow-style-change", true)) {
            ItemStack followItem = createGuiItem(Material.LEAD, "&eEstilo de Seguimiento", "&7Haz clic para cambiar cómo te sigue.");
            gui.setItem(14, followItem);
        } else {
            gui.setItem(14, createDisabledItem());
        }

        if (plugin.getConfig().getBoolean("gui-settings.enable-particle-toggle", true)) {
            ItemStack toggleParticlesItem = createGuiItem(petData.isParticlesEnabled() ? Material.GLOWSTONE_DUST : Material.GUNPOWDER, "&fActivar/Desactivar Partículas", petData.isParticlesEnabled() ? "&cClic para desactivar" : "&aClic para activar");
            gui.setItem(15, toggleParticlesItem);
        } else {
            gui.setItem(15, createDisabledItem());
        }

        ItemStack spawnItem = createGuiItem(Material.SHULKER_SPAWN_EGG, "&a» Invocar Mascota «", "&7Haz clic para que esta mascota te siga.");
        ItemStack despawnItem = createGuiItem(Material.CHEST, "&c» Guardar Mascota «", "&7Haz clic para guardar tu mascota activa.");
        ItemStack backButton = createGuiItem(Material.ARROW, "&c« Volver a Mis Mascotas");

        gui.setItem(27, backButton);
        gui.setItem(34, despawnItem);
        gui.setItem(35, spawnItem);

        ItemStack filler = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(ChatUtil.translate("&f"));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        player.openInventory(gui);
    }

    private String createXpBar(double currentXp, double requiredXp) {
        int barLength = 20;
        double progress = (requiredXp > 0) ? (currentXp / requiredXp) * barLength : 0;
        StringBuilder bar = new StringBuilder("&a");
        for (int i = 0; i < barLength; i++) {
            if (i < progress) {
                bar.append("▌");
            } else {
                if (i == (int)progress) bar.append("&7");
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

    private ItemStack createDisabledItem() {
        return createGuiItem(Material.BARRIER, "&c¡No disponible!");
    }
}
