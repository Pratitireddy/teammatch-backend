package com.teammatch;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class FlywayMigrationRunner {
    public static void main(String[] args) {
        System.out.println("=== Running Flyway Migrations for Aiven MySQL ===");

        Map<String, String> env = loadEnv();
        String url = env.get("DB_URL");
        String username = env.get("DB_USERNAME");
        String password = env.get("DB_PASSWORD");

        if (url == null || username == null || password == null) {
            System.err.println("ERROR: Missing DB_URL, DB_USERNAME, or DB_PASSWORD in environment / .env file.");
            System.exit(1);
        }

        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(url, username, password)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .load();

            System.out.println("Executing flyway.migrate()...");
            MigrateResult result = flyway.migrate();
            System.out.println("Flyway migration completed successfully!");
            System.out.println("Schema: " + result.schemaName);
            System.out.println("Initial schema version: " + result.initialSchemaVersion);
            System.out.println("Target schema version: " + result.targetSchemaVersion);
            System.out.println("Migrations executed: " + result.migrationsExecuted);
            System.out.println("Success: " + result.success);
        } catch (Exception e) {
            System.err.println("Flyway migration failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
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
