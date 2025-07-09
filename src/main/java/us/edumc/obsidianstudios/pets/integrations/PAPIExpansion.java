package us.edumc.obsidianstudios.pets.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.models.Pet;
import us.edumc.obsidianstudios.pets.util.ChatUtil;

public class PAPIExpansion extends PlaceholderExpansion {

    private final PetsObsidian plugin;

    public PAPIExpansion(PetsObsidian plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() { return "pet"; }
    @Override
    public @NotNull String getAuthor() { return "eduumc"; }
    @Override
    public @NotNull String getVersion() { return "1.0.0"; }
    @Override
    public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline()) return "";
        Pet activePet = plugin.getPetManager().getActivePet(player.getPlayer());
        switch (params.toLowerCase()) {
            case "active":
                return activePet != null ? "Sí" : "No";
            case "name":
                return activePet != null ? ChatUtil.translate(activePet.getConfig().getDisplayName()) : "Ninguna";
            case "id":
                return activePet != null ? activePet.getConfig().getId() : "ninguno";
        }
        return null;
    }
}
