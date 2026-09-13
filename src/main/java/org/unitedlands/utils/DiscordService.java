package org.unitedlands.utils;

import java.util.Map;

@Deprecated(forRemoval = true)
public class DiscordService {

    private DiscordService() {}

    @Deprecated(forRemoval = true)
    public static void sendDiscordEmbed(String webhookUrl, String embed, String pingRoleId, Map<String, String> replacements) {
        United.discord().sendEmbed(webhookUrl, embed, pingRoleId, replacements);
    }

}