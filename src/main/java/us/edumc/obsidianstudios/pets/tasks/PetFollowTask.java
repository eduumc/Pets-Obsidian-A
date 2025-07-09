package us.edumc.obsidianstudios.pets.tasks;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.integrations.WorldGuardIntegration;
import us.edumc.obsidianstudios.pets.models.FollowStyle;
import us.edumc.obsidianstudios.pets.models.PetConfig;
import us.edumc.obsidianstudios.pets.models.PlayerPetData;
import us.edumc.obsidianstudios.pets.util.ChatUtil;

public class PetFollowTask extends BukkitRunnable {

    private final Player player;
    private final ArmorStand pet;
    private final PetConfig petConfig;
    private final PetsObsidian plugin;
    private int ticksLived = 0;
    private boolean wasInDeniedRegion = false;

    public PetFollowTask(Player player, ArmorStand pet, PetConfig petConfig) {
        this.player = player;
        this.pet = pet;
        this.petConfig = petConfig;
        this.plugin = PetsObsidian.getInstance();
    }

    @Override
    public void run() {
        if (!player.isOnline() || player.isDead() || pet.isDead()) {
            this.cancel();
            return;
        }

        WorldGuardIntegration wg = plugin.getWorldGuardIntegration();
        if (wg != null && !wg.isAllowed(player)) {
            if (!wasInDeniedRegion) {
                player.sendMessage(plugin.getConfigManager().getPrefixedMessage("region-denied"));
                wasInDeniedRegion = true;
            }
            if (pet.isVisible()) {
                pet.setVisible(false);
            }
            return;
        } else {
            if (wasInDeniedRegion) {
                if (!pet.isVisible()) {
                    pet.setVisible(true);
                }
            }
            wasInDeniedRegion = false;
        }

        pet.teleport(getTargetLocation());

        if (++ticksLived % 20 == 0) {
            applyEffects();
        }

        if (ticksLived % 10 == 0) {
            spawnParticles();
        }
    }

    public void updatePetName() {
        if (petConfig.isShowDisplayName()) {
            PlayerPetData petData = plugin.getPlayerDataManager().getPetData(player, petConfig.getId());
            String name = petData.getCustomName() != null ? petData.getCustomName() : petConfig.getDisplayName();
            pet.setCustomName(ChatUtil.translate(name));
            pet.setCustomNameVisible(petData.isDisplayNameVisible());
        }
    }

    private Location getTargetLocation() {
        PlayerPetData petData = plugin.getPlayerDataManager().getPetData(player, petConfig.getId());
        Location playerLoc = player.getLocation();

        double yOffset = 1.2;
        double followDistance = plugin.getConfig().getDouble("pet-settings.follow-distance", 1.5);

        Location target = playerLoc.clone();
        FollowStyle style = petData.getFollowStyle();
        Vector direction = player.getLocation().getDirection().setY(0).normalize();

        switch (style) {
            case SIDE_RIGHT:
                target.add(direction.clone().crossProduct(new Vector(0, 1, 0)).normalize().multiply(-followDistance));
                break;
            case SIDE_LEFT:
                target.add(direction.clone().crossProduct(new Vector(0, -1, 0)).normalize().multiply(-followDistance));
                break;
            case BEHIND:
                target.add(direction.clone().multiply(-followDistance));
                break;
            case ABOVE:
                yOffset = 2.0;
                target.add(0, yOffset, 0);
                break;
        }

        if(style != FollowStyle.ABOVE) {
            target.setY(playerLoc.getY() + yOffset);
        }

        target.setYaw(playerLoc.getYaw());
        target.setPitch(playerLoc.getPitch());

        return target;
    }

    private void spawnParticles() {
        PlayerPetData petData = plugin.getPlayerDataManager().getPetData(player, petConfig.getId());
        if (!petData.isParticlesEnabled()) return;
        if (petConfig.getParticles() == null || !((boolean) petConfig.getParticles().getOrDefault("enabled", false))) return;

        String particleName = petData.getParticleType() != null ? petData.getParticleType() : (String) petConfig.getParticles().get("type");
        if (particleName == null) return;

        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            int amount = (int) petConfig.getParticles().getOrDefault("amount", 1);
            Location particleLocation = pet.getLocation().add(0, 1.5, 0);
            pet.getWorld().spawnParticle(particle, particleLocation, amount, 0.2, 0.2, 0.2, 0);
        } catch (IllegalArgumentException e) {
            // Falla silenciosamente
        }
    }

    private void applyEffects() {
        if (petConfig.getEffects() == null) return;
        for (String effectString : petConfig.getEffects()) {
            try {
                String[] parts = effectString.split(":");
                PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
                int amplifier = Integer.parseInt(parts[1]);
                if (type != null) {
                    player.addPotionEffect(new PotionEffect(type, 40, amplifier, true, false, false));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Efecto mal configurado: " + effectString);
            }
        }
    }
}