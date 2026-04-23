package Kiosk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class OrderAdd {

    public int saveOrder(Connection conn, Cart cart) throws Exception {
        conn.setAutoCommit(false);

        try {
            int orderNumber = getNextOrderNumber(conn);
            double total = cart.getTotal();

            String orderSql = "INSERT INTO orders(orderNumber, total, discountRate, discountAmount, finalTotal, orderStatus) "
                    + "VALUES (?, ?, 0, 0, ?, 'pending')";

            int orderId;

            try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, orderNumber);
                ps.setDouble(2, total);
                ps.setDouble(3, total);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    orderId = rs.getInt(1);
                }
            }

            String itemSql = "INSERT INTO order_items(orderId, menuItemId, quantity, pricePerItem, itemTotal) "
                    + "VALUES (?, ?, ?, ?, ?)";

            String drinkSql = "INSERT INTO order_item_drinks(orderItemId, drinkId) VALUES (?, ?)";
            String addonSql = "INSERT INTO order_item_addons(orderItemId, addonId) VALUES (?, ?)";
            String paymentSql = "INSERT INTO payments(orderId, amountReceived, changeAmount) VALUES (?, ?, ?)";

            for (OrderItem item : cart.getItems()) {
                int orderItemId;

                try (PreparedStatement ps = conn.prepareStatement(itemSql, Statement.RETURN_GENERATED_KEYS)) {
                    int debugId = (item.getMenuItem() != null) ? item.getMenuItem().getId() : 0;
                    System.out.println("DEBUG: Trying to save Main Food ID: " + debugId);
                    
                    ps.setInt(1, orderId);
                    
                    if (item.getMenuItem() != null && item.getMenuItem().getId() > 0) {
                        ps.setInt(2, item.getMenuItem().getId());
                    } else {
                        ps.setNull(2, java.sql.Types.INTEGER);
                    }
                    ps.setInt(3, item.getQuantity());
                    ps.setDouble(4, item.getUnitPrice());
                    ps.setDouble(5, item.getSubtotal());
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        orderItemId = rs.getInt(1);
                    }
                }

                if (item.getDrink() != null) {
                    try (PreparedStatement ps = conn.prepareStatement(drinkSql)) {
                        ps.setInt(1, orderItemId);
                        ps.setInt(2, item.getDrink().getId());
                        ps.executeUpdate();
                    }
                }

                if (item.getAddons() != null && !item.getAddons().isEmpty()) {
                    for (MenuItemData addon : item.getAddons()) {
                        try (PreparedStatement ps = conn.prepareStatement(addonSql)) {
                            ps.setInt(1, orderItemId);
                            ps.setInt(2, addon.getId());
                            ps.executeUpdate();
                        }
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(paymentSql)) {
                ps.setInt(1, orderId);
                ps.setDouble(2, total);
                ps.setDouble(3, 0);
                ps.executeUpdate();
            }

            conn.commit();
            return orderNumber;

        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private int getNextOrderNumber(Connection conn) throws Exception {
        String sql = "SELECT COALESCE(MAX(orderNumber), 20000) + 1 AS nextNum FROM orders";

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt("nextNum");
        }
    }
}
