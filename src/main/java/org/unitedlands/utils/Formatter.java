package org.unitedlands.utils;

import java.util.List;

@Deprecated(forRemoval = true)
public class Formatter {

    private Formatter() {}

    @Deprecated(forRemoval = true)
    public static String formatDuration(long millis) {
        return United.formatter().formatDuration(millis);
    }

    @Deprecated(forRemoval = true)
    public static List<String> getSortedCompletions(String input, List<String> options) {
        return United.formatter().getSortedCompletions(input, options);
    }

    @Deprecated(forRemoval = true)
    public static String formatReadable(String name) {
        return United.formatter().formatReadable(name);
    }

    @Deprecated(forRemoval = true)
    public static String removeLegacyFormatting(String string) {
        return United.formatter().removeLegacyFormatting(string);
    }

}