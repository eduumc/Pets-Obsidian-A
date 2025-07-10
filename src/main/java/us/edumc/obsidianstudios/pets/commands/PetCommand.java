package us.edumc.obsidianstudios.pets.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import us.edumc.obsidianstudios.pets.PetsObsidian;
import us.edumc.obsidianstudios.pets.gui.MainMenuGUI;
import us.edumc.obsidianstudios.pets.gui.ShopGUI;
import us.edumc.obsidianstudios.pets.managers.ConfigManager;
import us.edumc.obsidianstudios.pets.managers.PetManager;
import us.edumc.obsidianstudios.pets.managers.PlayerDataManager;
import us.edumc.obsidianstudios.pets.models.PetConfig;
import us.edumc.obsidianstudios.pets.models.PlayerPetData;
import us.edumc.obsidianstudios.pets.util.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PetCommand implements CommandExecutor, TabCompleter {

    private final PetsObsidian plugin;
    private final ConfigManager configManager;
    private final PetManager petManager;
    private final PlayerDataManager playerDataManager;

    public PetCommand(PetsObsidian plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.petManager = plugin.getPetManager();
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (isPlayer(sender)) {
                new MainMenuGUI(plugin).open((Player) sender);
            } else {
                sendHelpMessage(sender);
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "shop":
                if (isPlayer(sender)) new ShopGUI(plugin).open((Player) sender);
                break;
            case "remove":
                if (isPlayer(sender)) handleRemove((Player) sender);
                break;
            case "spawn":
                if (isPlayer(sender)) handleSpawn((Player) sender, args);
                break;
            case "info":
                // ================== CORRECCIÓN AQUÍ ==================
                // Ahora se necesita ser un jugador para usar /pet info
                if (isPlayer(sender)) handleInfo((Player) sender, args);
                // =====================================================
                break;
            case "give":
                handleGive(sender, args);
                break;
            case "delete":
                handleDelete(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "help":
            default:
                sendHelpMessage(sender);
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(Arrays.asList("shop", "remove", "spawn", "info", "help"));
            if (sender.hasPermission("pet.admin.give")) subCommands.add("give");
            if (sender.hasPermission("pet.admin.delete")) subCommands.add("delete");
            if (sender.hasPermission("pet.admin.reload")) subCommands.add("reload");

            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("spawn") || subCommand.equals("info")) {
                if(sender instanceof Player) {
                    return playerDataManager.getOwnedPets((Player) sender).stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
            if (subCommand.equals("give") || subCommand.equals("delete")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("give"))) {
            return Arrays.asList("egg", "pet").stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            return configManager.getAllPetConfigs().keySet().stream()
                    .filter(s -> s.startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private boolean isPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(configManager.getPrefixedMessage("player-only-command"));
            return false;
        }
        return true;
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("pet.admin.give")) {
            sender.sendMessage(configManager.getPrefixedMessage("no-permission"));
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(ChatUtil.translate("&cUso: /pet give <jugador> <id_mascota> <egg|pet>"));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        String petId = args[2];
        String type = args[3].toLowerCase();

        if (target == null) {
            sender.sendMessage(configManager.getPrefixedMessage("player-not-found"));
            return;
        }

        PetConfig petConfig = configManager.getPetConfig(petId);
        if (petConfig == null) {
            sender.sendMessage(configManager.getPrefixedMessage("give-no-pet-found"));
            return;
        }

        if (type.equals("egg")) {
            ItemStack petSpawner = petConfig.createHead();
            ItemMeta meta = petSpawner.getItemMeta();
            meta.setDisplayName(ChatUtil.translate("&aInvocador de Mascota: " + petConfig.getDisplayName()));
            meta.setLore(Collections.singletonList(ChatUtil.translate("&7¡Haz clic derecho para reclamar esta mascota!")));
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "pet-spawner-id"), PersistentDataType.STRING, petId);
            petSpawner.setItemMeta(meta);
            target.getInventory().addItem(petSpawner);
        } else if (type.equals("pet")) {
            playerDataManager.addPet(target, petId);
        } else {
            sender.sendMessage(ChatUtil.translate("&cTipo inválido. Usa 'egg' o 'pet'."));
            return;
        }

        sender.sendMessage(ChatUtil.translate(configManager.getPrefixedMessage("give-success")
                .replace("{pet_name}", petConfig.getDisplayName())
                .replace("{player_name}", target.getName())));
        target.sendMessage(ChatUtil.translate(configManager.getPrefixedMessage("give-received")
                .replace("{pet_name}", petConfig.getDisplayName())));
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("pet.admin.delete")) {
            sender.sendMessage(configManager.getPrefixedMessage("no-permission"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(configManager.getPrefixedMessage("delete-usage"));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        String petId = args[2];

        if (target == null) {
            sender.sendMessage(configManager.getPrefixedMessage("player-not-found"));
            return;
        }

        if (!playerDataManager.getOwnedPets(target).contains(petId)) {
            sender.sendMessage(configManager.getPrefixedMessage("delete-not-owned")
                    .replace("{pet_id}", petId));
            return;
        }

        playerDataManager.removePet(target, petId);
        sender.sendMessage(configManager.getPrefixedMessage("delete-success")
                .replace("{pet_id}", petId)
                .replace("{player_name}", target.getName()));
    }

    private void handleRemove(Player player) {
        if (petManager.hasPet(player)) {
            petManager.removePet(player);
        } else {
            player.sendMessage(configManager.getPrefixedMessage("no-pet-active"));
        }
    }

    private void handleSpawn(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(configManager.getPrefixedMessage("spawn-usage"));
            return;
        }
        String petId = args[1];
        if (!playerDataManager.getOwnedPets(player).contains(petId)) {
            player.sendMessage(configManager.getPrefixedMessage("spawn-not-owned"));
            return;
        }
        PetConfig petConfig = configManager.getPetConfig(petId);
        if (petConfig == null) {
            player.sendMessage(configManager.getPrefixedMessage("spawn-no-config"));
            return;
        }
        petManager.spawnPet(player, petConfig);
    }

    private void handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatUtil.translate("&cPor favor, especifica el ID de una de tus mascotas."));
            return;
        }
        String petId = args[1];
        if (!playerDataManager.getOwnedPets(player).contains(petId)) {
            player.sendMessage(configManager.getPrefixedMessage("spawn-not-owned"));
            return;
        }

        PetConfig petConfig = configManager.getPetConfig(petId);
        PlayerPetData petData = playerDataManager.getPetData(player, petId);
        if (petConfig == null) {
            player.sendMessage(configManager.getPrefixedMessage("spawn-no-config"));
            return;
        }

        String header = configManager.getConfig().getString("messages.pet-info-header");
        String footer = configManager.getConfig().getString("messages.pet-info-footer");

        player.sendMessage(ChatUtil.translate(header));

        String effects = petConfig.getEffects().isEmpty() ? "Ninguno" : String.join(", ", petConfig.getEffects());
        String onHitEffects = petConfig.getOnHitEffects().isEmpty() ? "Ninguno" : String.join(", ", petConfig.getOnHitEffects());
        String particles = petData.getParticleType() != null ? petData.getParticleType() : (petConfig.getParticles() != null && (boolean) petConfig.getParticles().getOrDefault("enabled", false)) ? (String) petConfig.getParticles().get("type") : "Ninguna";

        for (String line : configManager.getConfig().getStringList("messages.pet-info-body")) {
            String replacedLine = line.replace("{pet_name}", petData.getCustomName() != null ? petData.getCustomName() : petConfig.getDisplayName())
                    .replace("{pet_id}", petConfig.getId())
                    .replace("{pet_level}", String.valueOf(petData.getLevel()))
                    .replace("{pet_xp}", String.format("%.2f/%.2f", petData.getXp(), plugin.getPetLevelManager().getRequiredXp(petData.getLevel())))
                    .replace("{pet_effects}", effects)
                    .replace("{pet_on_hit_effects}", onHitEffects)
                    .replace("{pet_particles}", particles);
            player.sendMessage(ChatUtil.translate(replacedLine));
        }

        player.sendMessage(ChatUtil.translate(footer));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("pet.admin.reload")) {
            sender.sendMessage(configManager.getPrefixedMessage("no-permission"));
            return;
        }
        configManager.loadConfigs();
        sender.sendMessage(configManager.getPrefixedMessage("config-reloaded"));
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatUtil.translate("&6--- Ayuda de Pets-Obsidian ---"));
        sender.sendMessage(ChatUtil.translate("&e/pets &7- Abre el menú principal de mascotas."));
        sender.sendMessage(ChatUtil.translate("&e/pet shop &7- Abre la tienda de mascotas."));
        sender.sendMessage(ChatUtil.translate("&e/pet spawn <id> &7- Invoca una mascota que posees."));
        sender.sendMessage(ChatUtil.translate("&e/pet remove &7- Guarda tu mascota activa."));
        sender.sendMessage(ChatUtil.translate("&e/pet info <id> &7- Muestra información de una mascota."));
        if (sender.hasPermission("pet.admin.give")) {
            sender.sendMessage(ChatUtil.translate("&e/pet give <usuario> <id> <egg|pet> &7- Da una mascota a un jugador."));
        }
        if (sender.hasPermission("pet.admin.delete")) {
            sender.sendMessage(ChatUtil.translate("&e/pet delete <usuario> <id> &7- Quita una mascota a un jugador."));
        }
        if (sender.hasPermission("pet.admin.reload")) {
            sender.sendMessage(ChatUtil.translate("&e/pet reload &7- Recarga la configuración del plugin."));
        }
        sender.sendMessage(ChatUtil.translate("&e/pet help &7- Muestra este mensaje de ayuda."));
    }
}
