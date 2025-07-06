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

    public boolean isDenied(Player player) {
        StateFlag flag = PetsObsidian.PETS_DENY_FLAG;
        if (flag == null) return false;
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        Location loc = player.getLocation();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(loc));
        return set.testState(null, flag);
    }
}