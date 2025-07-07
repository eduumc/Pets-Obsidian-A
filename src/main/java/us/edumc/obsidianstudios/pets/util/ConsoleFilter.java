package us.edumc.obsidianstudios.pets.util;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class ConsoleFilter implements Filter {

    @Override
    public boolean isLoggable(LogRecord record) {
        // Ya no es necesario con la nueva API de cabezas, pero se mantiene por si acaso.
        if (record.getMessage() != null && record.getMessage().contains("Found inconsistent skull meta")) {
            return false;
        }
        return true;
    }
}