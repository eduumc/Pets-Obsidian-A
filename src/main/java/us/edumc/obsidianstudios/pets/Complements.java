package us.edumc.obsidianstudios.pets;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Complements {

    private final JavaPlugin plugin;
    private final String licenseKey;

    // URL de tu panel de licencias.
    private final String validationUrl = "http://149.130.180.213:1057/index.php?action=api_verify";

    public Complements(JavaPlugin plugin) {
        this.plugin = plugin;
        this.licenseKey = plugin.getConfig().getString("license.key", "");
    }

    public void validate() {
        new Thread(() -> {
            try {
                URL url = new URL(this.validationUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                String jsonInputString = "{\"license_key\": \"" + this.licenseKey + "\", \"plugin_name\": \"" + plugin.getDescription().getName() + "\"}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }

                String jsonResponse = response.toString();
                boolean isValid = jsonResponse.contains("\"valid\":true");

                if (isValid) {
                    plugin.getLogger().info("Licencia validada correctamente. ¡Plugin activado!");

                    // ✅ ¡AQUÍ ESTÁ LA LÓGICA QUE PEDISTE!
                    // Revisa si el panel web nos envió una nueva clave.
                    if (jsonResponse.contains("new_key")) {
                        // Extrae la nueva clave de la respuesta JSON.
                        String newKey = jsonResponse.split("\"new_key\":\"")[1].split("\"")[0];
                        plugin.getLogger().info("Se ha generado una nueva licencia gratuita. Guardando en config.yml...");

                        // Usa el planificador de Bukkit para ejecutar la tarea en el hilo principal del servidor.
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            // 1. Pone la nueva clave en la configuración cargada en memoria.
                            plugin.getConfig().set("license.key", newKey);
                            // 2. Guarda la configuración de la memoria al archivo config.yml en el disco.
                            plugin.saveConfig();
                            plugin.getLogger().info("¡Nueva clave guardada exitosamente en config.yml!");
                        });
                    }
                } else {
                    String reason = "Razón desconocida.";
                    if (jsonResponse.contains("reason")) {
                        reason = jsonResponse.split("\"reason\":\"")[1].split("\"")[0];
                    }
                    disablePlugin("Licencia inválida. Razón: " + reason);
                }

            } catch (Exception e) {
                disablePlugin("Error, Please contact the Edumc_ if you buy the plugin");
                e.printStackTrace();
            }
        }).start();
    }

    private void disablePlugin(String reason) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getLogger().severe("==================================================");
            plugin.getLogger().severe("  El plugin " + plugin.getDescription().getName() + " ha sido deshabilitado.");
            plugin.getLogger().severe("  Razón: " + reason);
            plugin.getLogger().severe("==================================================");
            Bukkit.getPluginManager().disablePlugin(plugin);
        });
    }
}
