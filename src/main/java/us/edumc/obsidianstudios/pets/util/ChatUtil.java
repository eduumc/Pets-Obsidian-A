package us.edumc.obsidianstudios.pets.util;

import org.bukkit.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clase de utilidad para traducir códigos de color, incluyendo los códigos hexadecimales.
 * Esta versión utiliza la API clásica de Spigot para máxima compatibilidad.
 */
public class ChatUtil {

    // Patrón para encontrar códigos de color hexadecimales como &#RRGGBB
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Traduce un string que contiene códigos de color legacy (&) y hexadecimales (&#RRGGBB).
     * @param text El texto a traducir.
     * @return El texto con los colores aplicados, compatible con la API de Spigot.
     */
    public static String translate(String text) {
        if (text == null) {
            return "";
        }

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer(text.length() + 4 * 8); // Pre-allocate buffer

        while (matcher.find()) {
            String group = matcher.group(1);

            // Reemplaza &#RRGGBB con el formato de Spigot §x§R§R§G§G§B§B
            matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x"
                    + ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1)
                    + ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3)
                    + ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5)
            );
        }
        matcher.appendTail(buffer);

        // Finalmente, traduce los códigos de color legacy como &a, &c, etc.
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
