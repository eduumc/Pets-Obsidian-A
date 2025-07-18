package us.edumc.obsidianstudios.pets;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import us.edumc.obsidianstudios.pets.commands.PetCommand;
import us.edumc.obsidianstudios.pets.integrations.PAPIExpansion;
import us.edumc.obsidianstudios.pets.integrations.VaultIntegration;
import us.edumc.obsidianstudios.pets.integrations.WorldGuardIntegration;
import us.edumc.obsidianstudios.pets.listeners.*;
import us.edumc.obsidianstudios.pets.managers.*;
// Asegúrate de que el import apunte a la clase correcta.
import us.edumc.obsidianstudios.pets.Complements;
import us.edumc.obsidianstudios.pets.util.ConsoleFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PetsObsidian extends JavaPlugin {

    private static PetsObsidian instance;
    private ConfigManager configManager;
    private PetManager petManager;
    private PlayerDataManager playerDataManager;
    private PetLevelManager petLevelManager;
    private VaultIntegration vaultIntegration;
    private WorldGuardIntegration worldGuardIntegration;
    public static StateFlag PERMITTED_PETS_FLAG;

    // Se añade la variable para la clase de licencias.
    private Complements complements;

    private final Map<UUID, String> playerNamingMap = new HashMap<>();
    private final Map<UUID, String> playerParticleMap = new HashMap<>();

    @Override
    public void onLoad() {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            try {
                StateFlag flag = new StateFlag("permitted-pets", true);
                registry.register(flag);
                PERMITTED_PETS_FLAG = flag;
            } catch (FlagConflictException e) {
                getLogger().log(Level.WARNING, "No se pudo registrar la flag 'permitted-pets' de WorldGuard (probablemente ya existe).");
            }
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        // --- Sistema de Licencia (Forma Correcta) ---
        saveDefaultConfig(); // Asegura que config.yml exista.

        // 1. Se crea una instancia de la clase Complements.
        this.complements = new Complements(this);
        // 2. Se llama al método que valida la licencia.
        this.complements.validate();

        // Si la licencia es inválida, el método validate() se encargará de
        // deshabilitar el plugin. El código de abajo no se ejecutará.
        // ----------------------------------------------------

        // Carga de configuraciones y sistemas
        this.configManager = new ConfigManager(this);
        configManager.loadConfigs();
        this.playerDataManager = new PlayerDataManager(this);
        this.petLevelManager = new PetLevelManager(this);
        this.petManager = new PetManager(this);

        // Hooks y dependencias
        setupIntegrations();

        // Comandos y listeners
        PetCommand petCommand = new PetCommand(this);
        getCommand("pet").setExecutor(petCommand);
        getCommand("pet").setTabCompleter(petCommand);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new MainMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new PetManagementListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);

        // Ocultar errores
        Logger.getLogger("").setFilter(new ConsoleFilter());
    }

    @Override
    public void onDisable() {
        if (petManager != null) {
            petManager.removeAllPets();
        }
        getLogger().info("Pets-Obsidian ha sido desactivado.");
    }

    private void setupIntegrations() {
        if (isPluginEnabled("Vault")) {
            this.vaultIntegration = new VaultIntegration();
            if (vaultIntegration.setupEconomy()) {
                getLogger().info("Hooked into Vault for economy features.");
            } else {
                getLogger().warning("Vault found, but no economy provider. Pricing features will be disabled.");
                this.vaultIntegration = null;
            }
        }
        if (isPluginEnabled("PlaceholderAPI")) {
            new PAPIExpansion(this).register();
            getLogger().info("Hooked into PlaceholderAPI.");
        }
        if (isPluginEnabled("WorldGuard")) {
            this.worldGuardIntegration = new WorldGuardIntegration();
            getLogger().info("Hooked into WorldGuard for region protection.");
        }
    }

    private boolean isPluginEnabled(String pluginName) {
        return getServer().getPluginManager().getPlugin(pluginName) != null
                && getServer().getPluginManager().isPluginEnabled(pluginName);
    }

    // --- Getters ---
    public static PetsObsidian getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public PetManager getPetManager() { return petManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public PetLevelManager getPetLevelManager() { return petLevelManager; }
    public VaultIntegration getVaultIntegration() { return vaultIntegration; }
    public WorldGuardIntegration getWorldGuardIntegration() { return worldGuardIntegration; }
    public Map<UUID, String> getPlayerNamingMap() { return playerNamingMap; }
    public Map<UUID, String> getPlayerParticleMap() { return playerParticleMap; }
}