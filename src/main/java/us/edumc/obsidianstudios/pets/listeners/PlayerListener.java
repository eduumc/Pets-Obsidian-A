package us.edumc.obsidianstudios.pets.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.managers.PetManager;
import us.edumc.obsidianstudios.pets.models.Pet;
import us.edumc.obsidianstudios.pets.models.PetConfig;
import us.edumc.obsidianstudios.pets.util.ChatUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final PetsObsidian plugin;
    private final Map<UUID, Long> hitCooldowns = new HashMap<>();
    private final Map<UUID, Map<UUID, Long>> playerKillCooldowns = new HashMap<>();

    public PlayerListener(PetsObsidian plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getPetManager().hasPet(player)) {
            plugin.getPetManager().removePet(player);
        }
    }

    // ================== CORRECCIÓN AQUÍ ==================
    // Se ha añadido el listener para la muerte del jugador.
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (plugin.getPetManager().hasPet(player)) {
            plugin.getPetManager().removePet(player);
        }
    }
    // =====================================================

    @EventHandler
    public void onPetItemClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.PLAYER_HEAD) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "pet-spawner-id");
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return;
        }

        event.setCancelled(true);
        String petId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        PetConfig petConfig = plugin.getConfigManager().getPetConfig(petId);

        if (petConfig == null) {
            player.sendMessage(ChatColor.RED + "Error: Este invocador de mascota está corrupto.");
            return;
        }

        plugin.getPlayerDataManager().addPet(player, petId);
        player.sendMessage(ChatUtil.translate("&aHas reclamado la mascota " + petConfig.getDisplayName() + "&a!"));

        item.setAmount(item.getAmount() - 1);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player attacker = (Player) event.getDamager();
        LivingEntity victim = (LivingEntity) event.getEntity();

        Pet activePet = plugin.getPetManager().getActivePet(attacker);
        if (activePet == null) return;

        PetConfig config = activePet.getConfig();
        if (config.getOnHitEffects().isEmpty()) return;

        UUID attackerId = attacker.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long lastHitTime = hitCooldowns.getOrDefault(attackerId, 0L);
        long cooldownMillis = config.getOnHitCooldown() * 50L;

        if (currentTime - lastHitTime < cooldownMillis) {
            return;
        }

        hitCooldowns.put(attackerId, currentTime);

        for (String effectString : config.getOnHitEffects()) {
            try {
                String[] parts = effectString.split(":");
                PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
                int amplifier = Integer.parseInt(parts[1]);
                int duration = Integer.parseInt(parts[2]) * 20;
                if (type != null) {
                    victim.addPotionEffect(new PotionEffect(type, duration, amplifier));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Efecto de golpeo mal configurado: " + effectString);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) return;

        Pet activePet = plugin.getPetManager().getActivePet(killer);
        if (activePet == null) return;

        PetLevelManager levelManager = plugin.getPetLevelManager();
        double xpAmount = 0;

        if (entity instanceof Monster) {
            xpAmount = plugin.getConfigManager().getLevelsConfig().getDouble("xp-sources.mob-kill", 5.0);
        } else if (entity instanceof Player) {
            Player victim = (Player) entity;
            if (canGetPlayerXp(killer.getUniqueId(), victim.getUniqueId())) {
                xpAmount = plugin.getConfigManager().getLevelsConfig().getDouble("xp-sources.player-kill", 25.0);
                updatePlayerKillCooldown(killer.getUniqueId(), victim.getUniqueId());
            }
        }

        if (xpAmount > 0) {
            levelManager.addXp(killer, activePet, xpAmount);
        }
    }

    private boolean canGetPlayerXp(UUID killerId, UUID victimId) {
        long cooldown = plugin.getConfigManager().getLevelsConfig().getLong("anti-abuse.player-kill-cooldown-seconds", 300) * 1000;
        long currentTime = System.currentTimeMillis();

        Map<UUID, Long> victimCooldowns = playerKillCooldowns.getOrDefault(killerId, new HashMap<>());
        long lastKillTime = victimCooldowns.getOrDefault(victimId, 0L);

        return currentTime - lastKillTime > cooldown;
    }

    private void updatePlayerKillCooldown(UUID killerId, UUID victimId) {
        Map<UUID, Long> victimCooldowns = playerKillCooldowns.getOrDefault(killerId, new HashMap<>());
        victimCooldowns.put(victimId, System.currentTimeMillis());
        playerKillCooldowns.put(killerId, victimCooldowns);
    }

    private boolean isPet(Entity entity) {
        return entity instanceof ArmorStand && entity.getPersistentDataContainer().has(PetManager.PET_OWNER_KEY, PersistentDataType.STRING);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (isPet(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (isPet(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (isPet(event.getRightClicked())) {
            event.setCancelled(true);
        }
    }
}
