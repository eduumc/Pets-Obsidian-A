package us.edumc.obsidianstudios.pets.models;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import us.edumc.obsidianstudios.pets.tasks.PetFollowTask;

public class Pet {

    private final Player owner;
    private final PetConfig config;
    private final ArmorStand armorStand;
    private final PetFollowTask followTask;

    public Pet(Player owner, PetConfig config, ArmorStand armorStand, PetFollowTask followTask) {
        this.owner = owner;
        this.config = config;
        this.armorStand = armorStand;
        this.followTask = followTask;
    }

    public Player getOwner() { return owner; }
    public PetConfig getConfig() { return config; }
    public ArmorStand getArmorStand() { return armorStand; }

    public void remove() {
        if (followTask != null && !followTask.isCancelled()) {
            followTask.cancel();
        }
        if (armorStand != null && !armorStand.isDead()) {
            armorStand.remove();
        }
    }
}
