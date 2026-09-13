package org.unitedlands.utils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UnitedDiscord {

    UnitedDiscord() {}

    public void sendEmbed(String webhookUrl, String embed, String pingRoleId, Map<String, String> replacements) {
        try {
            if (webhookUrl == null)
                return;

            var url        = URI.create(webhookUrl).toURL();
            var connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            replacements.put("ping-role", pingRoleId != null && !pingRoleId.isEmpty() ? "<@&" + pingRoleId + ">" : "");

            for (var entry : replacements.entrySet())
                embed = embed.replace("{" + entry.getKey() + "}", entry.getValue() != null ? entry.getValue() : "");

            try (OutputStream os = connection.getOutputStream()) {
                var input = embed.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            var responseCode = connection.getResponseCode();
            if (responseCode != 204)
                United.logger().error("Failed to send Discord embed. Response code: " + responseCode);

        } catch (Exception e) {
            United.logger().error("Error occurred while sending Discord embed.");
            United.logger().error(e.getMessage());
        }
    }

}