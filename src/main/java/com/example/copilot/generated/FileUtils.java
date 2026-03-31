package com.example.copilot.generated;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class FileUtils {
    
    private FileUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static List<String> readLines(Path file) throws IOException {
        Objects.requireNonNull(file, "file must not be null");
        return Files.readAllLines(file);
    }
    
    public static String humanReadableSize(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("bytes must not be negative");
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        double size = bytes;
        String[] units = {"KB", "MB", "GB", "TB", "PB"};
        int unitIndex = -1;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.1f %s", size, units[unitIndex]);
    }
    
    public static String extension(Path file) {
        Objects.requireNonNull(file, "file must not be null");
        Path fileName = file.getFileName();
        if (fileName == null) {
            return "";
        }
        String name = fileName.toString();
        int lastDot = name.lastIndexOf('.');
        if (lastDot <= 0) {
            return "";
        }
        return name.substring(lastDot + 1);
    }
}