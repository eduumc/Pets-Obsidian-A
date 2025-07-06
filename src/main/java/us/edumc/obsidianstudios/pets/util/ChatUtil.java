package us.edumc.obsidianstudios.pets.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Clase de utilidad para manejar el formato de texto.
 * Proporciona métodos para parsear strings que pueden contener
 * tanto códigos de color legacy (&) como formato MiniMessage (RGB, gradientes, etc.).
 */
public class ChatUtil {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static Component parse(String text) {
        if (text == null) {
            return Component.empty();
        }
        return miniMessage.deserialize(text);
    }

    public static String translate(String text) {
        if (text == null) {
            return "";
        }
        return LegacyComponentSerializer.legacySection().serialize(parse(text));
    }

    public static String legacy(Component component) {
        if (component == null) return "";
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
}