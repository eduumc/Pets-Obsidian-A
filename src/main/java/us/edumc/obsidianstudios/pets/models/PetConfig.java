package us.edumc.obsidianstudios.pets.models;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.util.ChatUtil;

import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PetConfig {

    private final String id;
    private final String displayName;
    private final boolean showDisplayName;
    private final List<String> lore;
    private final String headType;
    private final String headTexture;
    private final double price;
    private final String permission;
    private final List<String> effects;
    private final List<String> onHitEffects;
    private final int onHitCooldown;
    private final Map<String, Object> particles;
    private final boolean showInShop;

    public PetConfig(
            String id,
            String displayName,
            boolean showDisplayName,
            List<String> lore,
            String headType,
            String headTexture,
            double price,
            String permission,
            List<String> effects,
            List<String> onHitEffects,
            int onHitCooldown,
            Map<String, Object> particles,
            boolean showInShop
    ) {
        this.id = id;
        this.displayName = displayName;
        this.showDisplayName = showDisplayName;
        this.lore = lore;
        this.headType = headType;
        this.headTexture = headTexture;
        this.price = price;
        this.permission = permission;
        this.effects = effects;
        this.onHitEffects = onHitEffects;
        this.onHitCooldown = onHitCooldown;
        this.particles = particles;
        this.showInShop = showInShop;
    }

    public ItemStack createHead() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;

        if (headType.equalsIgnoreCase("base64") && headTexture != null) {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            URL url;
            try {
                String decoded = new String(Base64.getDecoder().decode(headTexture));
                url = new URL(decoded.substring("{\"textures\":{\"SKIN\":{\"url\":\"".length(), decoded.length() - "\"}}}".length()));
                textures.setSkin(url);
            } catch (Exception e) {
                PetsObsidian.getInstance().getLogger().warning("Error al decodificar la textura de la mascota '" + id + "'.");
                return head;
            }
            try {
                meta.getClass().getMethod("setPlayerProfile", PlayerProfile.class).invoke(meta, profile);
            } catch (NoSuchMethodException ignored) {
                try {
                    meta.getClass().getMethod("setOwnerProfile", PlayerProfile.class).invoke(meta, profile);
                } catch (Exception ex) {
                    PetsObsidian.getInstance().getLogger().warning("No se pudo aplicar la textura de cabeza personalizada para la mascota '" + id + "'.");
                }
            } catch (Exception ex) {
                PetsObsidian.getInstance().getLogger().warning("No se pudo aplicar la textura de cabeza personalizada para la mascota '" + id + "'.");
            }
        } else if (headType.equalsIgnoreCase("player") && headTexture != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(headTexture);
            meta.setOwningPlayer(owner);
        }

        head.setItemMeta(meta);
        return head;
    }

    public ItemStack getGuiItem(Player player, boolean isShop) {
        ItemStack item = createHead();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Compatibilidad displayName (1.19+), sino setDisplayName legacy
        try {
            meta.getClass().getMethod("displayName", Component.class).invoke(meta, ChatUtil.parse(displayName));
        } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            meta.setDisplayName(ChatUtil.translate(displayName));
        }

        List<Component> formattedLore = lore.stream()
                .map(line -> line.replace("{price}", String.valueOf(price)))
                .map(line -> line.replace("{permission}", permission))
                .map(ChatUtil::parse)
                .collect(Collectors.toList());

        if (isShop) {
            formattedLore.add(Component.empty());
            if (hasPermission(player)) {
                formattedLore.add(ChatUtil.parse("<green>✔ Disponible para comprar</green>"));
            } else {
                formattedLore.add(ChatUtil.parse("<red>✖ Bloqueado (Sin permiso)</red>"));
            }
        }

        // Compatibilidad lore Component (1.19+) o setLore legacy
        try {
            meta.getClass().getMethod("lore", List.class).invoke(meta, formattedLore);
        } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            List<String> legacyLore = formattedLore.stream()
                    .map(ChatUtil::legacy)
                    .collect(Collectors.toList());
            meta.setLore(legacyLore);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(
                new NamespacedKey(PetsObsidian.getInstance(), "pet-id"),
                PersistentDataType.STRING,
                id
        );
        item.setItemMeta(meta);
        return item;
    }

    public boolean hasPermission(Player player) {
        if (permission == null || permission.isEmpty()) {
            return true;
        }
        return player.hasPermission(permission) || player.hasPermission("pet.use.*");
    }

    public boolean isShowDisplayName() { return showDisplayName; }
    public List<String> getOnHitEffects() { return onHitEffects; }
    public int getOnHitCooldown() { return onHitCooldown; }
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public List<String> getLore() { return lore; }
    public String getHeadType() { return headType; }
    public String getHeadTexture() { return headTexture; }
    public double getPrice() { return price; }
    public String getPermission() { return permission; }
    public List<String> getEffects() { return effects; }
    public Map<String, Object> getParticles() { return particles; }
    public boolean isShowInShop() { return showInShop; }
}