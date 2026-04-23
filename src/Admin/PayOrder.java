
package Admin;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import java.io.File;
import java.util.HashMap;

public class PayOrder {
    
    Connection conn;
    Statement stmt;
    PreparedStatement ps;
    ResultSet rs;
    
    AdminMain am;
    
    public int currentOrderId = 0;
    public double currentTotal = 0;
    public double currentFinalTotal = 0;
    public boolean currentOrderPaid = false;
    public final List<Integer> orderItemIds = new ArrayList<>();
    
    public PayOrder(Connection conn, AdminMain am) {
        this.conn = conn;
        this.am = am;
    }

    
    public double getDiscountRate() {
        String discount = (String) am.discountComboBox.getSelectedItem();
        return ("Senior Discount".equals(discount) || "PWD".equals(discount)) ? 0.20 : 0.0;
    }

    public void loadOrder() {
        String orderNumStr = am.orderNumTextField.getText().trim();
        if (orderNumStr.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(am, "Please enter an order number.");
            return;
        }
        
        am.autoCancelExpiredOrders();
        int orderNum;
        try {
            orderNum = Integer.parseInt(orderNumStr);
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(am, "Order number must be a whole number.");
            return;
        }

        String sql = "SELECT orderId, total, discountRate, finalTotal, orderStatus FROM orders WHERE orderNumber = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderNum);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    javax.swing.JOptionPane.showMessageDialog(am, "Order #" + orderNum + " not found.");
                    return;
                }
                currentOrderId   = rs.getInt("orderId");
                currentTotal     = rs.getDouble("total");
                String status    = rs.getString("orderStatus");
                currentOrderPaid = "completed".equalsIgnoreCase(status);

                if (currentOrderPaid) {
                    javax.swing.JOptionPane.showMessageDialog(am,
                        "Order #" + orderNum + " has already been paid.");
                    return;
                }
                
                if ("cancelled".equalsIgnoreCase(status)) {
                    javax.swing.JOptionPane.showMessageDialog(am,
                        "Order #" + orderNum + " has been cancelled and cannot be paid.");
                    return;
                }
            }
        } catch (SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(am, "Database error: " + ex.getMessage());
            return;
        }

        loadOrderItems();
        applyDiscount();
    }

    public void loadOrderItems() {
        String sql =
            "SELECT oi.orderItemId, mi.itemName, d.drinkName, " +
            "GROUP_CONCAT(a.addonName ORDER BY a.addonId SEPARATOR ', ') AS addons, " +
            "oi.quantity, oi.itemTotal " +
            "FROM order_items oi " +
            "JOIN menu_items mi ON mi.itemID = oi.menuItemId " +
            "LEFT JOIN order_item_drinks oid ON oid.orderItemId = oi.orderItemId " +
            "LEFT JOIN drinks d ON d.drinkID = oid.drinkId " +
            "LEFT JOIN order_item_addons oia ON oia.orderItemId = oi.orderItemId " +
            "LEFT JOIN add_ons a ON a.addonId = oia.addonId " +
            "WHERE oi.orderId = ? " +
            "GROUP BY oi.orderItemId, mi.itemName, d.drinkName, oi.quantity, oi.itemTotal";

        DefaultTableModel model = (DefaultTableModel) am.orderTable.getModel();
        model.setRowCount(0);
        orderItemIds.clear();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orderItemIds.add(rs.getInt("orderItemId"));
                    String drinks = rs.getString("drinkName");
                    String addons = rs.getString("addons");
                    model.addRow(new Object[]{
                        rs.getString("itemName"),
                        drinks != null ? drinks : "None",
                        addons != null ? addons : "None",
                        rs.getInt("quantity"),
                        rs.getDouble("itemTotal")
                    });
                }
            }
        } catch (SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(am, "Error loading order items: " + ex.getMessage());
        }
    }

    public void applyDiscount() {
        double discountRate   = getDiscountRate();
        double discountAmount = currentTotal * discountRate;
        currentFinalTotal     = currentTotal - discountAmount;
        am.totalPriceLabel.setText(String.format("\u20b1%.2f", currentFinalTotal));
    }

    public void calculateChange() {
        if (currentOrderId == 0) {
            javax.swing.JOptionPane.showMessageDialog(am, "Please load an order first.");
            return;
        }
        String paymentStr = am.paymentTextField.getText().trim();
        if (paymentStr.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(am, "Please enter a payment amount.");
            return;
        }
        double payment;
        try {
            payment = Double.parseDouble(paymentStr);
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(am, "Invalid payment amount.");
            return;
        }
        if (payment < currentFinalTotal) {
            javax.swing.JOptionPane.showMessageDialog(am,
                String.format("Payment is insufficient.\nTotal due: \u20b1%.2f", currentFinalTotal));
            return;
        }
        double change = payment - currentFinalTotal;
        int confirm = javax.swing.JOptionPane.showConfirmDialog(am,
            String.format("Total:    \u20b1%.2f%nPayment:  \u20b1%.2f%nChange:   \u20b1%.2f%n%nConfirm payment?",
                currentFinalTotal, payment, change),
            "Confirm Payment", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            processPayment(payment, change);
        }
    }

    public void processPayment(double payment, double change) {
        double discountRate   = getDiscountRate();
        double discountAmount = currentTotal * discountRate;
        try {
            conn.setAutoCommit(false);

            String updateOrder = "UPDATE orders SET discountRate=?, discountAmount=?, finalTotal=?, orderStatus='completed' WHERE orderId=?";
            try (PreparedStatement ps = conn.prepareStatement(updateOrder)) {
                ps.setDouble(1, discountRate);
                ps.setDouble(2, discountAmount);
                ps.setDouble(3, currentFinalTotal);
                ps.setInt(4, currentOrderId);
                ps.executeUpdate();
            }

            String upsertPayment =
                "INSERT INTO payments(orderId, amountReceived, changeAmount) VALUES(?,?,?) " +
                "ON DUPLICATE KEY UPDATE amountReceived=VALUES(amountReceived), changeAmount=VALUES(changeAmount)";
            try (PreparedStatement ps = conn.prepareStatement(upsertPayment)) {
                ps.setInt(1, currentOrderId);
                ps.setDouble(2, payment);
                ps.setDouble(3, change);
                ps.executeUpdate();
            }

            conn.commit();
            javax.swing.JOptionPane.showMessageDialog(am,
                String.format("Payment processed!\nChange: \u20b1%.2f", change));
            int currentOrderNum = Integer.parseInt(am.orderNumTextField.getText().trim());
            printReceipt(currentOrderNum);
            clearPayOrderForm();
        } catch (SQLException ex) {
            try { conn.rollback(); } catch (SQLException rollbackEx) {

            }
            javax.swing.JOptionPane.showMessageDialog(am, "Error processing payment: " + ex.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException autoCommitEx) {

            }
        }
    }

    public void addOrderItem() {
        if (currentOrderId == 0) {
            javax.swing.JOptionPane.showMessageDialog(am, "Please load an order first.");
            return;
        }

        // --- 1. PREPARE THE DATA LISTS ---
        List<String> names = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        List<Double> prices = new ArrayList<>();

        List<String> drinkNames = new ArrayList<>();
        List<Integer> drinkIds = new ArrayList<>();
        List<Double> drinkPrices = new ArrayList<>();
        
        List<String> addonNames = new ArrayList<>();
        List<Integer> addonIds = new ArrayList<>();
        List<Double> addonPrices = new ArrayList<>();

        // Add default "None" options for Drinks and Addons
        drinkNames.add("None"); drinkIds.add(-1); drinkPrices.add(0.0);
        addonNames.add("None"); addonIds.add(-1); addonPrices.add(0.0);

        // --- 2. FETCH DATA FROM DATABASE ---
        try {
            // Load Menu Items
            try (PreparedStatement ps = conn.prepareStatement("SELECT itemID, itemName, price FROM menu_items ORDER BY itemName");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("itemID"));
                    names.add(rs.getString("itemName"));
                    prices.add(rs.getDouble("price"));
                }
            }
            
            // Load Drinks
            try (PreparedStatement ps = conn.prepareStatement("SELECT drinkID, drinkName, price FROM drinks ORDER BY drinkName");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    drinkIds.add(rs.getInt("drinkID"));
                    drinkNames.add(rs.getString("drinkName"));
                    drinkPrices.add(rs.getDouble("price"));
                }
            }
            
            // Load Add-ons
            try (PreparedStatement ps = conn.prepareStatement("SELECT addonId, addonName, price FROM add_ons ORDER BY addonName");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    addonIds.add(rs.getInt("addonId"));
                    addonNames.add(rs.getString("addonName"));
                    addonPrices.add(rs.getDouble("price"));
                }
            }
        } catch (SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(am, "Error loading menu data: " + ex.getMessage());
            return;
        }

        if (names.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(am, "No menu items available in the database.");
            return;
        }

        // --- 3. BUILD THE EXPANDED UI ---
        javax.swing.JComboBox<String> itemCombo = new javax.swing.JComboBox<>(names.toArray(new String[0]));
        javax.swing.JComboBox<String> drinkCombo = new javax.swing.JComboBox<>(drinkNames.toArray(new String[0]));
        javax.swing.JComboBox<String> addonCombo = new javax.swing.JComboBox<>(addonNames.toArray(new String[0]));
        javax.swing.JTextField qtyField = new javax.swing.JTextField("1", 6);
        
        Object[] message = {
            "Select Menu Item:", itemCombo, 
            "Select Drink:", drinkCombo,
            "Select Add-on:", addonCombo,
            "Quantity:", qtyField
        };

        int result = javax.swing.JOptionPane.showConfirmDialog(am, message,
            "Add Item to Order", javax.swing.JOptionPane.OK_CANCEL_OPTION);
        if (result != javax.swing.JOptionPane.OK_OPTION) return;

        // --- 4. GATHER INPUTS AND DO THE MATH ---
        int menuItemId = ids.get(itemCombo.getSelectedIndex());
        int drinkId = drinkIds.get(drinkCombo.getSelectedIndex());
        int addonId = addonIds.get(addonCombo.getSelectedIndex());
        
        // Calculate the combined price of the item + drink + addon
        double unitPrice = prices.get(itemCombo.getSelectedIndex()) + 
                           drinkPrices.get(drinkCombo.getSelectedIndex()) + 
                           addonPrices.get(addonCombo.getSelectedIndex());
                           
        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(am, "Please enter a valid quantity (positive integer).");
            return;
        }
        
        double itemTotal = unitPrice * qty;

        // --- 5. EXECUTE DATABASE LOGIC ---
        try {
            conn.setAutoCommit(false);

            int existingOrderItemId = -1;
            int existingQty = 0;
            double existingTotal = 0.0;

            // Check if this exact combo (Main + specific Drink + specific Addon) already exists in the order
            String checkSql = "SELECT oi.orderItemId, oi.quantity, oi.itemTotal " +
                              "FROM order_items oi " +
                              "LEFT JOIN order_item_drinks oid ON oi.orderItemId = oid.orderItemId " +
                              "LEFT JOIN order_item_addons oia ON oi.orderItemId = oia.orderItemId " +
                              "WHERE oi.orderId = ? AND oi.menuItemId = ? " +
                              "AND COALESCE(oid.drinkId, -1) = ? " +
                              "AND COALESCE(oia.addonId, -1) = ?";
                              
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, currentOrderId);
                checkPs.setInt(2, menuItemId);
                checkPs.setInt(3, drinkId);
                checkPs.setInt(4, addonId);
                
                try (ResultSet checkRs = checkPs.executeQuery()) {
                    if (checkRs.next()) {
                        existingOrderItemId = checkRs.getInt("orderItemId");
                        existingQty = checkRs.getInt("quantity");
                        existingTotal = checkRs.getDouble("itemTotal");
                    }
                }
            }

            if (existingOrderItemId != -1) {
                // IT EXISTS: Just update the quantity and total of that specific row
                String updateSql = "UPDATE order_items SET quantity = ?, itemTotal = ? WHERE orderItemId = ?";
                try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                    updatePs.setInt(1, existingQty + qty);
                    updatePs.setDouble(2, existingTotal + itemTotal);
                    updatePs.setInt(3, existingOrderItemId);
                    updatePs.executeUpdate();
                }
            } else {
                // IT IS NEW: Insert a brand new row
                int newOrderItemId = 0;
                String insertItemSql = "INSERT INTO order_items(orderId, menuItemId, quantity, pricePerItem, itemTotal) VALUES(?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(insertItemSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, currentOrderId);
                    ps.setInt(2, menuItemId);
                    ps.setInt(3, qty);
                    ps.setDouble(4, unitPrice);
                    ps.setDouble(5, itemTotal);
                    ps.executeUpdate();
                    
                    ResultSet rsItem = ps.getGeneratedKeys();
                    if (rsItem.next()) newOrderItemId = rsItem.getInt(1);
                }
                
                // If they picked a Drink, link it to the new order item
                if (drinkId != -1 && newOrderItemId != 0) {
                    try (PreparedStatement psLinkDrink = conn.prepareStatement("INSERT INTO order_item_drinks (orderItemId, drinkId) VALUES (?, ?)")) {
                        psLinkDrink.setInt(1, newOrderItemId);
                        psLinkDrink.setInt(2, drinkId);
                        psLinkDrink.executeUpdate();
                    }
                }
                
                // If they picked an Add-on, link it to the new order item
                if (addonId != -1 && newOrderItemId != 0) {
                    try (PreparedStatement psLinkAddon = conn.prepareStatement("INSERT INTO order_item_addons (orderItemId, addonId) VALUES (?, ?)")) {
                        psLinkAddon.setInt(1, newOrderItemId);
                        psLinkAddon.setInt(2, addonId);
                        psLinkAddon.executeUpdate();
                    }
                }
            }

            // Save and refresh the UI
            currentTotal = recalcOrderTotal();
            conn.commit();

            loadOrderItems();
            applyDiscount();
        } catch (SQLException ex) {
            try { conn.rollback(); } catch (SQLException rollbackEx) {}
            javax.swing.JOptionPane.showMessageDialog(am, "Error adding item: " + ex.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException autoCommitEx) {}
        }
    }

    public void editOrderItem() {
        int selectedRow = am.orderTable.getSelectedRow();
        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(am, "Please select an item to edit.");
            return;
        }

        int orderItemId = orderItemIds.get(selectedRow);

        // --- 1. FETCH THE CURRENT STATE OF THIS SPECIFIC ITEM ---
        int currentMenuItemId = -1;
        int currentDrinkId = -1;
        int currentAddonId = -1;
        int currentQty = 1;

        String fetchSql = "SELECT oi.menuItemId, oi.quantity, " +
                          "COALESCE(oid.drinkId, -1) AS drinkId, " +
                          "COALESCE(oia.addonId, -1) AS addonId " +
                          "FROM order_items oi " +
                          "LEFT JOIN order_item_drinks oid ON oi.orderItemId = oid.orderItemId " +
                          "LEFT JOIN order_item_addons oia ON oi.orderItemId = oia.orderItemId " +
                          "WHERE oi.orderItemId = ?";
        try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {
            ps.setInt(1, orderItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    currentMenuItemId = rs.getInt("menuItemId");
                    currentQty = rs.getInt("quantity");
                    currentDrinkId = rs.getInt("drinkId");
                    currentAddonId = rs.getInt("addonId");
                }
            }
        } catch (SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(am, "Error fetching item details: " + ex.getMessage());
            return;
        }

        // --- 2. LOAD ALL MENU OPTIONS ---
        List<String> names = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        List<Double> prices = new ArrayList<>();

        List<String> drinkNames = new ArrayList<>();
        List<Integer> drinkIds = new ArrayList<>();
        List<Double> drinkPrices = new ArrayList<>();

        List<String> addonNames = new ArrayList<>();
        List<Integer> addonIds = new ArrayList<>();
        List<Double> addonPrices = new ArrayList<>();

        drinkNames.add("None"); drinkIds.add(-1); drinkPrices.add(0.0);
        addonNames.add("None"); addonIds.add(-1); addonPrices.add(0.0);

        try {
            try (PreparedStatement ps = conn.prepareStatement("SELECT itemID, itemName, price FROM menu_items ORDER BY itemName");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("itemID"));
                    names.add(rs.getString("itemName"));
                    prices.add(rs.getDouble("price"));
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT drinkID, drinkName, price FROM drinks ORDER BY drinkName");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    drinkIds.add(rs.getInt("drinkID"));
                    drinkNames.add(rs.getString("drinkName"));
                    drinkPrices.add(rs.getDouble("price"));
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT addonId, addonName, price FROM add_ons ORDER BY addonName");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    addonIds.add(rs.getInt("addonId"));
                    addonNames.add(rs.getString("addonName"));
                    addonPrices.add(rs.getDouble("price"));
                }
            }
        } catch (SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(am, "Error loading menu data: " + ex.getMessage());
            return;
        }

        // --- 3. BUILD THE UI AND PRE-SELECT CURRENT CHOICES ---
        javax.swing.JComboBox<String> itemCombo = new javax.swing.JComboBox<>(names.toArray(new String[0]));
        javax.swing.JComboBox<String> drinkCombo = new javax.swing.JComboBox<>(drinkNames.toArray(new String[0]));
        javax.swing.JComboBox<String> addonCombo = new javax.swing.JComboBox<>(addonNames.toArray(new String[0]));
        javax.swing.JTextField qtyField = new javax.swing.JTextField(String.valueOf(currentQty), 6);

        // Auto-select the dropdowns based on what the customer currently has
        int menuIdx = ids.indexOf(currentMenuItemId);
        if (menuIdx != -1) itemCombo.setSelectedIndex(menuIdx);

        int drinkIdx = drinkIds.indexOf(currentDrinkId);
        if (drinkIdx != -1) drinkCombo.setSelectedIndex(drinkIdx);

        int addonIdx = addonIds.indexOf(currentAddonId);
        if (addonIdx != -1) addonCombo.setSelectedIndex(addonIdx);

        Object[] message = {
            "Edit Menu Item:", itemCombo, 
            "Edit Drink:", drinkCombo,
            "Edit Add-on:", addonCombo,
            "Quantity:", qtyField
        };

        int result = javax.swing.JOptionPane.showConfirmDialog(am, message,
            "Edit Order Item", javax.swing.JOptionPane.OK_CANCEL_OPTION);
        if (result != javax.swing.JOptionPane.OK_OPTION) return;

        // --- 4. GATHER NEW INPUTS AND CALCULATE ---
        int newMenuItemId = ids.get(itemCombo.getSelectedIndex());
        int newDrinkId = drinkIds.get(drinkCombo.getSelectedIndex());
        int newAddonId = addonIds.get(addonCombo.getSelectedIndex());
        
        double newUnitPrice = prices.get(itemCombo.getSelectedIndex()) + 
                              drinkPrices.get(drinkCombo.getSelectedIndex()) + 
                              addonPrices.get(addonCombo.getSelectedIndex());

        int newQty;
        try {
            newQty = Integer.parseInt(qtyField.getText().trim());
            if (newQty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(am, "Please enter a valid quantity (positive integer).");
            return;
        }

        double newItemTotal = newUnitPrice * newQty;

        // --- 5. UPDATE THE DATABASE ---
        try {
            conn.setAutoCommit(false);

            // Update the main order_items table (Item, Quantity, and Prices)
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE order_items SET menuItemId = ?, quantity = ?, pricePerItem = ?, itemTotal = ? WHERE orderItemId = ?")) {
                ps.setInt(1, newMenuItemId);
                ps.setInt(2, newQty);
                ps.setDouble(3, newUnitPrice);
                ps.setDouble(4, newItemTotal);
                ps.setInt(5, orderItemId);
                ps.executeUpdate();
            }

            // Wipe out the old Drink and Add-on connections for this specific row
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM order_item_drinks WHERE orderItemId = ?")) {
                ps.setInt(1, orderItemId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM order_item_addons WHERE orderItemId = ?")) {
                ps.setInt(1, orderItemId);
                ps.executeUpdate();
            }

            // Insert the new Drink connection (if they selected one)
            if (newDrinkId != -1) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO order_item_drinks (orderItemId, drinkId) VALUES (?, ?)")) {
                    ps.setInt(1, orderItemId);
                    ps.setInt(2, newDrinkId);
                    ps.executeUpdate();
                }
            }

            // Insert the new Add-on connection (if they selected one)
            if (newAddonId != -1) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO order_item_addons (orderItemId, addonId) VALUES (?, ?)")) {
                    ps.setInt(1, orderItemId);
                    ps.setInt(2, newAddonId);
                    ps.executeUpdate();
                }
            }

            // Save and refresh UI
            currentTotal = recalcOrderTotal();
            conn.commit();

            loadOrderItems();
            applyDiscount();
            
        } catch (SQLException ex) {
            try { conn.rollback(); } catch (SQLException rollbackEx) {}
            javax.swing.JOptionPane.showMessageDialog(am, "Error updating item: " + ex.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException autoCommitEx) {}
        }
    }

    public void deleteOrderItem() {
        int selectedRow = am.orderTable.getSelectedRow();
        if (selectedRow == -1) {
            javax.swing.JOptionPane.showMessageDialog(am, "Please select an item to delete.");
            return;
        }

        int confirm = javax.swing.JOptionPane.showConfirmDialog(am,
            "Are you sure you want to delete am item?",
            "Confirm Delete", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm != javax.swing.JOptionPane.YES_OPTION) return;

        int orderItemId = orderItemIds.get(selectedRow);

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM order_item_addons WHERE orderItemId=?")) {
                ps.setInt(1, orderItemId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM order_item_drinks WHERE orderItemId=?")) {
                ps.setInt(1, orderItemId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM order_items WHERE orderItemId=?")) {
                ps.setInt(1, orderItemId);
                ps.executeUpdate();
            }

            currentTotal = recalcOrderTotal();
            conn.commit();

            loadOrderItems();
            applyDiscount();
        } catch (SQLException ex) {
            try { conn.rollback(); } catch (SQLException rollbackEx) {
//                logger.log(java.util.logging.Level.SEVERE, "Rollback failed", rollbackEx);
            }
            javax.swing.JOptionPane.showMessageDialog(am, "Error deleting item: " + ex.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException autoCommitEx) {
//                logger.log(java.util.logging.Level.SEVERE, "Failed to restore auto-commit", autoCommitEx);
            }
        }
    }


    public double recalcOrderTotal() throws SQLException {
        double newTotal = 0;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(itemTotal), 0) AS total FROM order_items WHERE orderId=?")) {
            ps.setInt(1, currentOrderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) newTotal = rs.getDouble("total");
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE orders SET total=? WHERE orderId=?")) {
            ps.setDouble(1, newTotal);
            ps.setInt(2, currentOrderId);
            ps.executeUpdate();
        }
        return newTotal;
    }

    public void clearPayOrderForm() {
        currentOrderId    = 0;
        currentTotal      = 0;
        currentFinalTotal = 0;
        currentOrderPaid  = false;
        orderItemIds.clear();
        am.orderNumTextField.setText("");
        am.paymentTextField.setText("");
        am.totalPriceLabel.setText(" ");
        am.discountComboBox.setSelectedIndex(0);
        ((DefaultTableModel) am.orderTable.getModel()).setRowCount(0);
    }

    public void loadMenuItems(String categoryName) {
        am.mainCreateOrderPanel.removeAll();
        try {
            String query = "SELECT m.itemID, m.itemName, m.price, m.imagePath FROM menu_items m JOIN categories c ON m.categoryId = c.categoryId WHERE c.categoryName = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, categoryName);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Notice we pass 'this' (the PayOrder instance) to the MenuCard
                MenuCard card = new MenuCard(rs.getInt("itemID"), rs.getString("itemName"), rs.getDouble("price"), rs.getString("imagePath"),this);
                am.mainCreateOrderPanel.add(card);
            }
            am.mainCreateOrderPanel.revalidate();
            am.mainCreateOrderPanel.repaint();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void loadDrinks() {
        am.mainCreateOrderPanel.removeAll();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM drinks");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MenuCard card = new MenuCard(rs.getInt("drinkID"), rs.getString("drinkName"), rs.getDouble("price"), rs.getString("image_path"),this);
                am.mainCreateOrderPanel.add(card);
            }
            am.mainCreateOrderPanel.revalidate();
            am.mainCreateOrderPanel.repaint();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void loadAddOns() {
        am.mainCreateOrderPanel.removeAll();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM add_ons");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MenuCard card = new MenuCard(rs.getInt("addonId"), rs.getString("addonName"), rs.getDouble("price"), rs.getString("imagePath"),this);
                am.mainCreateOrderPanel.add(card);
            }
            am.mainCreateOrderPanel.revalidate();
            am.mainCreateOrderPanel.repaint();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void addItemToCreateOrder(String itemName, double price) {
        DefaultTableModel model = (DefaultTableModel) am.tableCreateOrder.getModel();
        boolean itemExists = false;

        for (int i = 0; i < model.getRowCount(); i++) {
            Object cellValue = model.getValueAt(i, 0);
           if (cellValue != null && cellValue.equals(itemName)){
                int currentQty = (int) model.getValueAt(i, 1);
                model.setValueAt(currentQty + 1, i, 1);
                model.setValueAt((currentQty + 1) * price, i, 3);
                itemExists = true;
                break;
            }
        }
        if (!itemExists) {
            model.addRow(new Object[]{itemName, 1, price, price});
        }
        updateCreateOrderTotal();
    }

    public void updateCreateOrderTotal() {
        DefaultTableModel model = (DefaultTableModel) am.tableCreateOrder.getModel();
        double total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            total += (double) model.getValueAt(i, 3);
        }
        
        String discountType = (String) am.discountCreateOrderComboBox.getSelectedItem();
        double discountRate = ("Senior Discount".equals(discountType) || "PWD".equals(discountType)) ? 0.20 : 0.0;
        double finalTotal = total - (total * discountRate);

        am.totalAmountLabel.setText(String.format("₱%.2f", finalTotal));
    }

    public void increaseCreateOrderQuantity() {
        int selectedRow = am.tableCreateOrder.getSelectedRow();
        if (selectedRow >= 0) {
            DefaultTableModel model = (DefaultTableModel) am.tableCreateOrder.getModel();
            int currentQty = (int) model.getValueAt(selectedRow, 1);
            double price = (double) model.getValueAt(selectedRow, 2);
            model.setValueAt(currentQty + 1, selectedRow, 1);
            model.setValueAt((currentQty + 1) * price, selectedRow, 3);
            updateCreateOrderTotal();
        } else {
            javax.swing.JOptionPane.showMessageDialog(am, "Please select an item to increase.");
        }
    }

    public void decreaseCreateOrderQuantity() {
        int selectedRow = am.tableCreateOrder.getSelectedRow();
        if (selectedRow >= 0) {
            DefaultTableModel model = (DefaultTableModel) am.tableCreateOrder.getModel();
            int currentQty = (int) model.getValueAt(selectedRow, 1);
            double price = (double) model.getValueAt(selectedRow, 2);
            if (currentQty > 1) {
                model.setValueAt(currentQty - 1, selectedRow, 1);
                model.setValueAt((currentQty - 1) * price, selectedRow, 3);
            } else {
                model.removeRow(selectedRow);
            }
            updateCreateOrderTotal();
        } else {
            javax.swing.JOptionPane.showMessageDialog(am, "Please select an item to decrease.");
        }
    }

    public void removeCreateOrderItem() {
        int selectedRow = am.tableCreateOrder.getSelectedRow();
        if (selectedRow >= 0) {
            ((DefaultTableModel) am.tableCreateOrder.getModel()).removeRow(selectedRow);
            updateCreateOrderTotal();
        } else {
            javax.swing.JOptionPane.showMessageDialog(am, "Please select an item to remove.");
        }
    }

    public void clearCreateOrder() {
        ((DefaultTableModel) am.tableCreateOrder.getModel()).setRowCount(0);
        updateCreateOrderTotal();
        am.paymentCOTextField.setText("");
        am.discountCreateOrderComboBox.setSelectedIndex(0);
    }

    public void confirmCreateOrder() {
        DefaultTableModel model = (DefaultTableModel) am.tableCreateOrder.getModel();
        
        if (model.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(am, "The order is empty. Please add items first.", "Empty Order", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        double total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            total += (double) model.getValueAt(i, 3);
        }

        String discountType = (String) am.discountCreateOrderComboBox.getSelectedItem();
        double discountRate = ("Senior Discount".equals(discountType) || "PWD".equals(discountType)) ? 0.20 : 0.0;
        double discountAmount = total * discountRate;
        double finalTotal = total - discountAmount;

        String paymentStr = am.paymentCOTextField.getText().trim();
        boolean isPaid = false;
        double paymentReceived = 0;
        double changeAmount = 0;

        if (!paymentStr.isEmpty()) {
            try {
                paymentReceived = Double.parseDouble(paymentStr);
                if (paymentReceived < finalTotal) {
                    javax.swing.JOptionPane.showMessageDialog(am, String.format("Insufficient payment! Total due is ₱%.2f", finalTotal), "Payment Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }
                changeAmount = paymentReceived - finalTotal;
                isPaid = true; 
            } catch (NumberFormatException e) {
                javax.swing.JOptionPane.showMessageDialog(am, "Invalid payment amount entered.", "Input Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        int orderNumber = generateOrderNumber();

        try {
            conn.setAutoCommit(false);

            String insertOrderSql = "INSERT INTO orders (orderNumber, total, discountRate, discountAmount, finalTotal, orderStatus) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement psOrder = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, orderNumber);
            psOrder.setDouble(2, total);
            psOrder.setDouble(3, discountRate);
            psOrder.setDouble(4, discountAmount);
            psOrder.setDouble(5, finalTotal);
            psOrder.setString(6, isPaid ? "completed" : "pending");
            psOrder.executeUpdate();

            ResultSet rsOrder = psOrder.getGeneratedKeys();
            int orderId = 0;
            if (rsOrder.next()) orderId = rsOrder.getInt(1);

            for (int i = 0; i < model.getRowCount(); i++) {
                String itemName = (String) model.getValueAt(i, 0);
                int qty = (int) model.getValueAt(i, 1);
                double price = (double) model.getValueAt(i, 2);
                double itemTotal = (double) model.getValueAt(i, 3);

                int menuItemId = -1, drinkId = -1, addonId = -1;

                PreparedStatement psMenu = conn.prepareStatement("SELECT itemID FROM menu_items WHERE itemName = ?");
                psMenu.setString(1, itemName);
                ResultSet rsMenu = psMenu.executeQuery();
                if (rsMenu.next()) menuItemId = rsMenu.getInt("itemID");

                if (menuItemId == -1) {
                    PreparedStatement psDrink = conn.prepareStatement("SELECT drinkID FROM drinks WHERE drinkName = ?");
                    psDrink.setString(1, itemName);
                    ResultSet rsDrink = psDrink.executeQuery();
                    if (rsDrink.next()) drinkId = rsDrink.getInt("drinkID");
                }

                if (menuItemId == -1 && drinkId == -1) {
                    PreparedStatement psAddon = conn.prepareStatement("SELECT addonId FROM add_ons WHERE addonName = ?");
                    psAddon.setString(1, itemName);
                    ResultSet rsAddon = psAddon.executeQuery();
                    if (rsAddon.next()) addonId = rsAddon.getInt("addonId");
                }

                String insertItemSql = "INSERT INTO order_items (orderId, menuItemId, quantity, pricePerItem, itemTotal) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement psItem = conn.prepareStatement(insertItemSql, Statement.RETURN_GENERATED_KEYS);
                psItem.setInt(1, orderId);
                if (menuItemId != -1) psItem.setInt(2, menuItemId);
                else psItem.setNull(2, java.sql.Types.INTEGER);
                psItem.setInt(3, qty);
                psItem.setDouble(4, price);
                psItem.setDouble(5, itemTotal);
                psItem.executeUpdate();

                ResultSet rsItem = psItem.getGeneratedKeys();
                int orderItemId = 0;
                if (rsItem.next()) orderItemId = rsItem.getInt(1);

                if (drinkId != -1) {
                    PreparedStatement psLinkDrink = conn.prepareStatement("INSERT INTO order_item_drinks (orderItemId, drinkId) VALUES (?, ?)");
                    psLinkDrink.setInt(1, orderItemId);
                    psLinkDrink.setInt(2, drinkId);
                    psLinkDrink.executeUpdate();
                }

                if (addonId != -1) {
                    PreparedStatement psLinkAddon = conn.prepareStatement("INSERT INTO order_item_addons (orderItemId, addonId) VALUES (?, ?)");
                    psLinkAddon.setInt(1, orderItemId);
                    psLinkAddon.setInt(2, addonId);
                    psLinkAddon.executeUpdate();
                }
            }

            if (isPaid) {
                String insertPaymentSql = "INSERT INTO payments (orderId, amountReceived, changeAmount) VALUES (?, ?, ?)";
                PreparedStatement psPayment = conn.prepareStatement(insertPaymentSql);
                psPayment.setInt(1, orderId);
                psPayment.setDouble(2, paymentReceived);
                psPayment.setDouble(3, changeAmount);
                psPayment.executeUpdate();
            }

            conn.commit();

            String successMsg = "Order Created Successfully!\nOrder Number: " + orderNumber;
            if (isPaid) successMsg += String.format("\nChange: ₱%.2f", changeAmount);
            javax.swing.JOptionPane.showMessageDialog(am, successMsg, "Success", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            printReceipt(orderNumber);
            

            clearCreateOrder();

        } catch (SQLException ex) {
            try { conn.rollback(); } catch (SQLException re) {}
            javax.swing.JOptionPane.showMessageDialog(am, "Database Error: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException autoCommitEx) {}
        }
    }

    private int generateOrderNumber() {
        int nextOrderNum = 1001;
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT MAX(orderNumber) FROM orders");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int maxNum = rs.getInt(1);
                if (maxNum >= 1001) nextOrderNum = maxNum + 1;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return nextOrderNum;
    }
    
    private void printReceipt(int orderNumber) {
        try {
            // Point this to where you saved your compiled Jasper file
            File jasperFile = new File("C:\\Users\\jakob\\JaspersoftWorkspace\\MyReports\\Receipt.jasper");

            if (!jasperFile.exists()) {
          //      javax.swing.JOptionPane.showMessageDialog(this, "Receipt template not found!");
                return;
            }

            JasperReport jr = (JasperReport) JRLoader.loadObject(jasperFile);

            // Give Jasper the Order Number
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("P_OrderNumber", orderNumber); 

            // Execute the SQL inside Jasper using your live database connection!
            JasperPrint jp = JasperFillManager.fillReport(jr, parameters, conn);
            
            // Pop up the receipt viewer (false means closing the viewer won't close your whole app)
            JasperViewer.viewReport(jp, false);

        } catch (Exception ex) {
            ex.printStackTrace();
        //    javax.swing.JOptionPane.showMessageDialog(this, "Error printing receipt: " + ex.getMessage());
        }
    }
    
    
}
