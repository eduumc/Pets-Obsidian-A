package us.edumc.obsidianstudios.pets.util;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class ConsoleFilter implements Filter {

    @Override
    public boolean isLoggable(LogRecord record) {
        if (record.getMessage() != null && record.getMessage().contains("Found inconsistent skull meta")) {
            return false; // No mostrar este mensaje en la consola
        }
        return true;
    }
}