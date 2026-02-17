package com.AI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private static String DB_URL = "jdbc:sqlite:order_management.db";
    private static Connection sharedConnection = null;

    public static void setDatabaseUrl(String url) {
        DB_URL = url;
    }

    public static void initialize() {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            
            createTables(stmt);
            insertInitialInventory(stmt);
            stmt.close();
            
            // Only close connection if it's not a shared in-memory connection
            if (!DB_URL.contains(":memory:")) {
                conn.close();
            }
            
            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static void createTables(Statement stmt) throws SQLException {
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS inventory (" +
            "product TEXT PRIMARY KEY, " +
            "stock INTEGER NOT NULL)"
        );

        stmt.execute(
            "CREATE TABLE IF NOT EXISTS orders (" +
            "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "customer_name TEXT NOT NULL, " +
            "status TEXT NOT NULL)"
        );

        stmt.execute(
            "CREATE TABLE IF NOT EXISTS order_items (" +
            "order_id INTEGER NOT NULL, " +
            "product TEXT NOT NULL, " +
            "quantity INTEGER NOT NULL, " +
            "FOREIGN KEY (order_id) REFERENCES orders(order_id), " +
            "FOREIGN KEY (product) REFERENCES inventory(product))"
        );
    }

    private static void insertInitialInventory(Statement stmt) throws SQLException {
        stmt.execute("INSERT OR IGNORE INTO inventory (product, stock) VALUES ('Book', 10)");
        stmt.execute("INSERT OR IGNORE INTO inventory (product, stock) VALUES ('Pen', 50)");
        stmt.execute("INSERT OR IGNORE INTO inventory (product, stock) VALUES ('Notebook', 20)");
    }

    public static Connection getConnection() throws SQLException {
        if (DB_URL.contains(":memory:") && sharedConnection != null) {
            return sharedConnection;
        }
        if (DB_URL.contains(":memory:") && sharedConnection == null) {
            sharedConnection = DriverManager.getConnection(DB_URL);
            return sharedConnection;
        }
        return DriverManager.getConnection(DB_URL);
    }

    public static void closeSharedConnection() throws SQLException {
        if (sharedConnection != null && !sharedConnection.isClosed()) {
            sharedConnection.close();
            sharedConnection = null;
        }
    }
}
