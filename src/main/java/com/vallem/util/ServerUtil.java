package com.vallem.util;

public class ServerUtil {
    private static final int DEFAULT_PORT = 8080;
    private static final String PORT = System.getenv("PORT");

    public static int getPort() {
        if (PORT == null) return DEFAULT_PORT;
        return Integer.parseInt(PORT);
    }
}
