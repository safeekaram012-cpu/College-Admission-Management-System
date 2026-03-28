package com.cams.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection – centralises all JDBC connection management.
 * Update DB_URL / USER / PASSWORD to match your MySQL installation.
 */
public class DBConnection {

    // ── Connection settings ──────────────────────────────────────────────
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/cams_db"
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "root";   // ← change to your password

    // Singleton connection (fine for a single-threaded console app)
    private static Connection connection = null;

    /**
     * Returns a live (or freshly opened) JDBC Connection.
     * Throws RuntimeException so callers don't need checked-exception boilerplate.
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Load driver explicitly (needed for older MySQL connectors)
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                System.out.println("[DB] Connected to cams_db successfully.");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found on classpath.\n"
                    + "Add mysql-connector-j-*.jar to /lib and rebuild.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect to MySQL: " + e.getMessage()
                    + "\nCheck DB_URL / USER / PASSWORD in DBConnection.java", e);
        }
        return connection;
    }

    /** Gracefully close the shared connection (call on application exit). */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            }
        }
    }
}
