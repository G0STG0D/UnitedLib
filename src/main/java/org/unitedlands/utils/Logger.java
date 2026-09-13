package org.unitedlands.utils;

@Deprecated(forRemoval = true)
public class Logger {

    private Logger() {}

    @Deprecated(forRemoval = true)
    public static void log(String message) {
        United.logger().info(message);
    }

    @Deprecated(forRemoval = true)
    public static void log(String message, String prefix) {
        United.logger().info(message, prefix);
    }

    @Deprecated(forRemoval = true)
    public static void logWarning(String message) {
        United.logger().warning(message);
    }

    @Deprecated(forRemoval = true)
    public static void logWarning(String message, String prefix) {
        United.logger().warning(message, prefix);
    }

    @Deprecated(forRemoval = true)
    public static void logError(String message) {
        United.logger().error(message);
    }

    @Deprecated(forRemoval = true)
    public static void logError(String message, String prefix) {
        United.logger().error(message, prefix);
    }

    @Deprecated(forRemoval = true)
    public static void debug(String message) {
        United.logger().debug(message);
    }

    @Deprecated(forRemoval = true)
    public static void debug(String message, String prefix) {
        United.logger().debug(message, prefix);
    }

}