package us.edumc.obsidianstudios.pets.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.gui.PetManagementGUI;
import us.edumc.obsidianstudios.pets.models.PlayerPetData;
import us.edumc.obsidianstudios.pets.util.ChatUtil;

public class PlayerChatListener implements Listener {

    private final PetsObsidian plugin;

    public PlayerChatListener(PetsObsidian plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (plugin.getPlayerNamingMap().containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            String petId = plugin.getPlayerNamingMap().remove(player.getUniqueId());

            if (message.equalsIgnoreCase("cancelar")) {
                player.sendMessage(ChatColor.RED + "Cambio de nombre cancelado.");
                Bukkit.getScheduler().runTask(plugin, () -> new PetManagementGUI(plugin).open(player, petId));
                return;
            }

            PlayerPetData petData = plugin.getPlayerDataManager().getPetData(player, petId);
            petData.setCustomName(message);
            plugin.getPlayerDataManager().savePetData(player, petData);

            // Actualizar la mascota activa si es la que se está editando
            if (plugin.getPetManager().hasPet(player) && plugin.getPetManager().getActivePet(player).getConfig().getId().equals(petId)) {
                plugin.getPetManager().getActivePet(player).updateName(message);
            }

            player.sendMessage(ChatUtil.translate("&a¡Nombre de la mascota actualizado a '" + message + "&a'!"));
            Bukkit.getScheduler().runTask(plugin, () -> new PetManagementGUI(plugin).open(player, petId));
        }

        else if (plugin.getPlayerParticleMap().containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            String petId = plugin.getPlayerParticleMap().remove(player.getUniqueId());

            if (message.equalsIgnoreCase("cancelar")) {
                player.sendMessage(ChatColor.RED + "Cambio de partículas cancelado.");
                return;
            }

            try {
                Particle.valueOf(message.toUpperCase());
                PlayerPetData petData = plugin.getPlayerDataManager().getPetData(player, petId);
                petData.setParticleType(message.toUpperCase());
                plugin.getPlayerDataManager().savePetData(player, petData);
                player.sendMessage(ChatUtil.translate("&a¡Partículas actualizadas a '&d" + message.toUpperCase() + "&a'!"));
                Bukkit.getScheduler().runTask(plugin, () -> new PetManagementGUI(plugin).open(player, petId));
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "Partícula no válida. Inténtalo de nuevo.");
                plugin.getPlayerParticleMap().put(player.getUniqueId(), petId);
            }
        }
    }
}