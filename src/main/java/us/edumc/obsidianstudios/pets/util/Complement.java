package us.edumc.obsidianstudios.pets.util;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class Complement {

    private final Plugin plugin;
    private final String licenseKey;
    private final String pluginName;
    private final String apiUrl = "http://149.130.180.213:1059/api/"; // Ruta base

    public Complement(Plugin plugin, String licenseKey) {
        this.plugin = plugin;
        this.licenseKey = licenseKey;
        this.pluginName = plugin.getName();
    }

    public boolean validate() {
        try {
            registerPluginIfNeeded();

            URL url = new URL(apiUrl + "validate_license.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String ip = InetAddress.getLocalHost().getHostAddress();
            String postData = "license=" + licenseKey + "&plugin=" + pluginName + "&ip=" + ip;

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes());
            }

            int code = conn.getResponseCode();
            if (code != 200) return false;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String result = reader.readLine();
                return result != null && result.equalsIgnoreCase("VALID");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void registerPluginIfNeeded() {
        try {
            URL url = new URL(apiUrl + "register_plugin.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String postData = "plugin=" + pluginName;

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes());
            }

            conn.getResponseCode(); // No importa el contenido
        } catch (Exception e) {
            // Ignorar, ya puede existir
        }
    }

    public static String generateLicenseKey() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}