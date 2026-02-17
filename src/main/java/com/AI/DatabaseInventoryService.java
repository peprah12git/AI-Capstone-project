package com.AI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DatabaseInventoryService implements InventoryService {

    @Override
    public boolean checkStock(List<String> products, List<Integer> quantities) {
        try {
            Connection conn = DatabaseInitializer.getConnection();
            for (int i = 0; i < products.size(); i++) {
                String sql = "SELECT stock FROM inventory WHERE product = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, products.get(i));
                    ResultSet rs = pstmt.executeQuery();
                    if (!rs.next() || rs.getInt("stock") < quantities.get(i)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check stock", e);
        }
    }

    @Override
    public void reserveStock(List<String> products, List<Integer> quantities) {
        try {
            Connection conn = DatabaseInitializer.getConnection();
            conn.setAutoCommit(false);
            try {
                String sql = "UPDATE inventory SET stock = stock - ? WHERE product = ?";
                for (int i = 0; i < products.size(); i++) {
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, quantities.get(i));
                        pstmt.setString(2, products.get(i));
                        pstmt.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reserve stock", e);
        }
    }

    @Override
    public void returnStock(List<String> products, List<Integer> quantities) {
        try {
            Connection conn = DatabaseInitializer.getConnection();
            conn.setAutoCommit(false);
            try {
                String sql = "UPDATE inventory SET stock = stock + ? WHERE product = ?";
                for (int i = 0; i < products.size(); i++) {
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, quantities.get(i));
                        pstmt.setString(2, products.get(i));
                        pstmt.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to return stock", e);
        }
    }

    public int getStock(String product) {
        try {
            Connection conn = DatabaseInitializer.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT stock FROM inventory WHERE product = ?")) {
                pstmt.setString(1, product);
                ResultSet rs = pstmt.executeQuery();
                return rs.next() ? rs.getInt("stock") : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get stock", e);
        }
    }
}
