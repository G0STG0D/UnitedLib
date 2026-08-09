package org.unitedlands.utils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Formatter {

    public static String formatDuration(long millis) {
        long seconds = millis / 1000 % 60;
        long minutes = millis / (1000 * 60) % 60;
        long hours   = millis / (1000 * 60 * 60) % 24;
        long days    = millis / (1000 * 60 * 60 * 24);

        StringBuilder sb = new StringBuilder();
        if (days > 0)
            sb.append(days).append("d ");
        if (hours > 0 || days > 0)
            sb.append(hours).append("h ");
        if (minutes > 0 || hours > 0 || days > 0)
            sb.append(minutes).append("m ");
        sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    public static List<String> getSortedCompletions(String input, List<String> options) {
        List<String> completions = List.of("");
        if (options != null) {
            completions = options.stream().filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                    .collect(Collectors.toList());
            Collections.sort(completions);
        }
        return completions;
    }

    public static String formatReadable(String name) {
        StringBuilder result = new StringBuilder(name.length());

        for (String part : name.split("_")) {
            if (part.isEmpty())
                continue;

            result.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1).toLowerCase())
                    .append(' ');
        }

        return result.toString().trim();
    }

    public static String removeLegacyFormatting(String string) {
        return string.replaceAll("(?i)[§&][0-9A-FK-OR]", "");
    }

}
