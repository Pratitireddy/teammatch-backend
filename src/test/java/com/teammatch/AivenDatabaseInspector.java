package com.teammatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only inspector for Aiven MySQL database.
 * Does NOT create, alter, truncate, insert, or delete anything.
 */
public class AivenDatabaseInspector {

    public static void main(String[] args) {
        System.out.println("=== Aiven MySQL Read-Only Database Inspection ===");

        Map<String, String> env = loadEnv();
        String url = env.get("DB_URL");
        String username = env.get("DB_USERNAME");
        String password = env.get("DB_PASSWORD");

        if (url == null || username == null || password == null) {
            System.err.println("ERROR: Missing DB_URL, DB_USERNAME, or DB_PASSWORD in environment / .env file.");
            System.exit(1);
        }

        System.out.println("Configured Username: " + username);
        System.out.println("Target Host: " + extractHost(url));
        System.out.println("TLS Verification: " + (url.contains("sslMode=VERIFY") ? "Enabled" : "Not specified"));

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            conn.setReadOnly(true);
            System.out.println("Connection status: Successfully connected (read-only mode active)");
            java.sql.DatabaseMetaData meta = conn.getMetaData();
            System.out.println("Database Product: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());

            try (Statement stmt = conn.createStatement()) {
                // 1. Inspect existing tables
                List<String> tables = new ArrayList<>();
                try (ResultSet rs = stmt.executeQuery("SHOW TABLES;")) {
                    while (rs.next()) {
                        tables.add(rs.getString(1));
                    }
                }

                System.out.println("\n--- Existing Database Tables (" + tables.size() + ") ---");
                if (tables.isEmpty()) {
                    System.out.println("(No tables found - database is completely empty)");
                } else {
                    for (String t : tables) {
                        System.out.println("  - " + t);
                    }
                }

                // 2. Check flyway_schema_history
                boolean hasFlyway = tables.stream().anyMatch(t -> t.equalsIgnoreCase("flyway_schema_history"));
                System.out.println("\n--- Flyway Schema History ---");
                if (!hasFlyway) {
                    System.out.println("flyway_schema_history table does not exist yet.");
                } else {
                    try (ResultSet rs = stmt.executeQuery(
                            "SELECT installed_rank, version, description, type, script, installed_on, success " +
                            "FROM flyway_schema_history ORDER BY installed_rank ASC;")) {
                        System.out.printf("%-5s | %-10s | %-30s | %-10s | %-10s%n",
                                "Rank", "Version", "Description", "Type", "Success");
                        System.out.println("----------------------------------------------------------------------");
                        int count = 0;
                        while (rs.next()) {
                            count++;
                            System.out.printf("%-5d | %-10s | %-30s | %-10s | %-10b%n",
                                    rs.getInt("installed_rank"),
                                    rs.getString("version"),
                                    rs.getString("description"),
                                    rs.getString("type"),
                                    rs.getBoolean("success"));
                        }
                        if (count == 0) {
                            System.out.println("(flyway_schema_history is empty)");
                        }
                    }
                }
            }

            System.out.println("\nInspection complete. No schema or data changes were performed.");
        } catch (Exception e) {
            System.err.println("\nConnection/Inspection failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String extractHost(String url) {
        try {
            int start = url.indexOf("://");
            if (start != -1) {
                int end = url.indexOf("/", start + 3);
                if (end != -1) {
                    return url.substring(start + 3, end);
                }
            }
        } catch (Exception ignored) {}
        return "Unknown";
    }

    private static Map<String, String> loadEnv() {
        Map<String, String> map = new HashMap<>(System.getenv());
        File envFile = new File("../.env");
        if (!envFile.exists()) {
            envFile = new File(".env");
        }
        if (envFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                        int idx = line.indexOf('=');
                        String k = line.substring(0, idx).trim();
                        String v = line.substring(idx + 1).trim();
                        if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
                            v = v.substring(1, v.length() - 1);
                        }
                        map.put(k, v);
                    }
                }
            } catch (Exception ignored) {}
        }
        return map;
    }
}
