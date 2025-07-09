package us.edumc.obsidianstudios.pets.integrations;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import us.edumc.obsidianstudios.pets.PetsObsidian;

public class WorldGuardIntegration {

    /**
     * Comprueba si las mascotas están permitidas en la ubicación del jugador.
     * @param player El jugador a comprobar.
     * @return true si están permitidas, false si la flag 'permitted-pets' está en DENY.
     */
    public boolean isAllowed(Player player) {
        StateFlag flag = PetsObsidian.PERMITTED_PETS_FLAG;
        if (flag == null) {
            return true; // Si la flag no existe, se permite por defecto.
        }
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        Location loc = player.getLocation();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(loc));

        // Retorna false solo si la flag está explícitamente en DENY.
        // Si está en ALLOW (default) o no está establecida en la región, retorna true.
        return set.testState(null, flag);
    }
}