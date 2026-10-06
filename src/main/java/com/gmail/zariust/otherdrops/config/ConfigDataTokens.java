package com.gmail.zariust.otherdrops.config;

import java.util.ArrayList;
import java.util.List;

public class ConfigDataTokens {
    private ConfigDataTokens(){}
    /**
     * Splits on one or more "!", trims each entry and drops empty ones: "!PINK!!STRENGTH@600" -> [PINK, STRENGTH@600].
     * "\!" is kept as a literal "!" inside an entry.
     */
    public static List<String> split(String data) {
        List<String> result = new ArrayList<>();
        if (data == null) return result;
        String protectedData = data.replace("\\!", "\u0000");
        for (String part : protectedData.split("!+")) {
            String token = part.replace("\u0000", "!").trim();
            if (!token.isEmpty()) result.add(token);
        }
        return result;
    }
}
