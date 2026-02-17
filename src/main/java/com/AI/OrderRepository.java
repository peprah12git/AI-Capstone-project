package com.AI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    public int saveOrder(OrderWithInventory order) {
        try {
            Connection conn = DatabaseInitializer.getConnection();
            conn.setAutoCommit(false);
            try {
                String orderSql = "INSERT INTO orders (customer_name, status) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, order.getCustomerName());
                    pstmt.setString(2, order.getStatus().name());
                    pstmt.executeUpdate();
                    
                    ResultSet rs = pstmt.getGeneratedKeys();
                    int orderId = rs.next() ? rs.getInt(1) : -1;
                    
                    String itemsSql = "INSERT INTO order_items (order_id, product, quantity) VALUES (?, ?, ?)";
                    try (PreparedStatement itemStmt = conn.prepareStatement(itemsSql)) {
                        for (int i = 0; i < order.getProducts().size(); i++) {
                            itemStmt.setInt(1, orderId);
                            itemStmt.setString(2, order.getProducts().get(i));
                            itemStmt.setInt(3, order.getQuantities().get(i));
                            itemStmt.executeUpdate();
                        }
                    }
                    
                    conn.commit();
                    return orderId;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save order", e);
        }
    }

    public void updateOrderStatus(int orderId, OrderWithInventory.OrderStatus status) {
        try {
            Connection conn = DatabaseInitializer.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement("UPDATE orders SET status = ? WHERE order_id = ?")) {
                pstmt.setString(1, status.name());
                pstmt.setInt(2, orderId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order status", e);
        }
    }

    public List<String> getOrderProducts(int orderId) {
        List<String> products = new ArrayList<>();
        try {
            Connection conn = DatabaseInitializer.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT product FROM order_items WHERE order_id = ?")) {
                pstmt.setInt(1, orderId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    products.add(rs.getString("product"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get order products", e);
        }
        return products;
    }
}
