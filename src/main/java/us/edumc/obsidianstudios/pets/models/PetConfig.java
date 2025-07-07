package us.edumc.obsidianstudios.pets.models;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
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
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.util.ChatUtil;

import java.lang.reflect.Field;
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

    public PetConfig(String id, String displayName, boolean showDisplayName, List<String> lore, String headType, String headTexture, double price, String permission, List<String> effects, List<String> onHitEffects, int onHitCooldown, Map<String, Object> particles, boolean showInShop) {
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
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", headTexture));
            try {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                PetsObsidian.getInstance().getLogger().warning("No se pudo aplicar la textura a la cabeza para '" + id + "'.");
                e.printStackTrace();
            }
        } else if (headType.equalsIgnoreCase("player") && headTexture != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(headTexture));
        }

        head.setItemMeta(meta);
        return head;
    }

    public ItemStack getGuiItem(Player player, boolean isShop) {
        ItemStack item = createHead();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(ChatUtil.translate(displayName));

        List<String> formattedLore = lore.stream()
                .map(line -> line.replace("{price}", String.valueOf(price)))
                .map(line -> line.replace("{permission}", permission != null ? permission : "Ninguno"))
                .map(ChatUtil::translate)
                .collect(Collectors.toList());

        if (isShop) {
            formattedLore.add("");
            if (hasPermission(player)) {
                formattedLore.add(ChatUtil.translate("&a✔ Disponible para comprar"));
            } else {
                formattedLore.add(ChatUtil.translate("&c✖ Bloqueado (Sin permiso)"));
            }
        }

        meta.setLore(formattedLore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(new NamespacedKey(PetsObsidian.getInstance(), "pet-id"), PersistentDataType.STRING, id);
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
