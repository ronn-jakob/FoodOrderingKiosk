/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class MenuManager {
    
    private Connection conn;
    private AdminMain admin;
    
    // State Tracking
    public int currentEditItemId = -1;
    public String currentEditImagePath = "";

    public MenuManager(Connection conn, AdminMain admin) {
        this.conn = conn;
        this.admin = admin;
    }

    public String saveImageLocally(File sourceFile) {
        try {

            File destFolder = new File("Images");
            if (!destFolder.exists()) {
                destFolder.mkdir(); 
            }

            String uniqueName = System.currentTimeMillis() + "_" + sourceFile.getName();
            File destFile = new File(destFolder, uniqueName);

            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return "Images/" + uniqueName;
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(admin, "Error saving image locally!");
            return "";
        }
    }

    public void loadEditMenuItems(String categoryName) {
        admin.editMenuCardPanel.removeAll();
        admin.editMenuCardPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 15, 15)); 
        
        PreparedStatement pst;
        String sql = "";
        
        try {

            // We prepare the SQL and bind the '?' immediately in the same block 
            // so they can never accidentally get mismatched!
            if (categoryName.equals("Drinks")) {
                sql = "SELECT drinkID AS itemID, drinkName AS itemName, price, image_path AS imagePath FROM drinks";
                pst = conn.prepareStatement(sql);
                
            } else if (categoryName.equals("Add-ons")) {
                sql = "SELECT addonId AS itemID, addonName AS itemName, price, imagePath FROM add_ons";
                pst = conn.prepareStatement(sql);
                
            } else {
                sql = "SELECT m.itemID, m.itemName, m.price, m.imagePath " +
                             "FROM menu_Items m " +
                             "JOIN categories c ON m.categoryId = c.categoryId " +
                             "WHERE c.categoryName = ?"; 
                
                pst = conn.prepareStatement(sql);
                pst.setString(1, categoryName); // Safely applied right away!
            }

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("itemID");
                String name = rs.getString("itemName");
                double price = rs.getDouble("price");
                String imgPath = rs.getString("imagePath");
                
                EditItemMenuCard card = new EditItemMenuCard(id, name, price, categoryName, imgPath, admin, this);
                admin.editMenuCardPanel.add(card);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        admin.editMenuCardPanel.revalidate();
        admin.editMenuCardPanel.repaint();

        admin.editMenuCardPanel.revalidate();
        admin.editMenuCardPanel.repaint();
    }

    public void addMenuItem() {
        if (!admin.verifyAdminCredentials()) return;
        
        try {
            String name = admin.editMenuNameTextField.getText();
            double price = Double.parseDouble(admin.editMenuPriceTextField.getText());
            String categoryName = admin.editMenuComboBox.getSelectedItem().toString();

            PreparedStatement pst;

            if (categoryName.equals("Drinks")) {
                String sql = "INSERT INTO drinks (drinkName, price, image_path) VALUES (?, ?, ?)";
                pst = conn.prepareStatement(sql);
                pst.setString(1, name);
                pst.setDouble(2, price);
                pst.setString(3, currentEditImagePath);
                
            } else if (categoryName.equals("Add-ons")) {
                String sql = "INSERT INTO add_ons (addonName, price, imagePath) VALUES (?, ?, ?)";
                pst = conn.prepareStatement(sql);
                pst.setString(1, name);
                pst.setDouble(2, price);
                pst.setString(3, currentEditImagePath);
                
            } else {
                String sql = "INSERT INTO menu_Items (itemName, price, imagePath, categoryId) " +
                             "VALUES (?, ?, ?, (SELECT categoryId FROM categories WHERE categoryName = ?))";
                pst = conn.prepareStatement(sql);
                pst.setString(1, name);
                pst.setDouble(2, price);
                pst.setString(3, currentEditImagePath);
                pst.setString(4, categoryName);
            }
            
            pst.executeUpdate();
            JOptionPane.showMessageDialog(admin, "Item Added Successfully!");
            
            loadEditMenuItems(categoryName); 
            clearForm();
        } catch (Exception ex) {
            //
            JOptionPane.showMessageDialog(admin, "Please Fill in all Fields");
        }
    }

    public void updateMenuItem() {
        if (currentEditItemId == -1) {
            JOptionPane.showMessageDialog(admin, "Please select an item to edit first!");
            return;
        }
        
        if (!admin.verifyAdminCredentials()) return;
        
        try {
            String name = admin.editMenuNameTextField.getText();
            double price = Double.parseDouble(admin.editMenuPriceTextField.getText());
            String categoryName = admin.editMenuComboBox.getSelectedItem().toString();

            PreparedStatement pst;

            if (categoryName.equals("Drinks")) {
                String sql = "UPDATE drinks SET drinkName=?, price=?, image_path=? WHERE drinkID=?";
                pst = conn.prepareStatement(sql);
                pst.setString(1, name);
                pst.setDouble(2, price);
                pst.setString(3, currentEditImagePath);
                pst.setInt(4, currentEditItemId);
                
            } else if (categoryName.equals("Add-ons")) {
                String sql = "UPDATE add_ons SET addonName=?, price=?, imagePath=? WHERE addonId=?";
                pst = conn.prepareStatement(sql);
                pst.setString(1, name);
                pst.setDouble(2, price);
                pst.setString(3, currentEditImagePath);
                pst.setInt(4, currentEditItemId);
                
            } else {
                String sql = "UPDATE menu_Items SET itemName=?, price=?, imagePath=?, " +
                             "categoryId=(SELECT categoryId FROM categories WHERE categoryName = ?) " +
                             "WHERE itemID=?";
                pst = conn.prepareStatement(sql);
                pst.setString(1, name);
                pst.setDouble(2, price);
                pst.setString(3, currentEditImagePath);
                pst.setString(4, categoryName);
                pst.setInt(5, currentEditItemId);
            }
            
            pst.executeUpdate();
            JOptionPane.showMessageDialog(admin, "Item Updated Successfully!");
            
            loadEditMenuItems(categoryName);
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Menu Already Contains this Item");
            ex.printStackTrace();
        }
    }

    public void deleteMenuItem() {
        if (currentEditItemId == -1) return;
        
        String categoryName = admin.editMenuComboBox.getSelectedItem().toString();
        
        int confirm = JOptionPane.showConfirmDialog(admin, "Delete this item?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (!admin.verifyAdminCredentials()) return;
            try {
                PreparedStatement pst;

                if (categoryName.equals("Drinks")) {
                    pst = conn.prepareStatement("DELETE FROM drinks WHERE drinkID=?");
                } else if (categoryName.equals("Add-ons")) {
                    pst = conn.prepareStatement("DELETE FROM add_ons WHERE addonId=?");
                } else {
                    pst = conn.prepareStatement("DELETE FROM menu_Items WHERE itemID=?");
                }
                
                pst.setInt(1, currentEditItemId);
                pst.executeUpdate();
                
                JOptionPane.showMessageDialog(admin, "Item Deleted!");
                loadEditMenuItems(categoryName);
                clearForm();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void clearForm() {
        currentEditItemId = -1;
        currentEditImagePath = "";
        admin.editMenuNameTextField.setText("");
        admin.editMenuPriceTextField.setText("");
        admin.imagePanel.setPreferredSize(new java.awt.Dimension(212, 170));
        admin.imagePanel.removeAll();
        admin.imagePanel.revalidate();
        admin.imagePanel.repaint();
        admin.addtoMenuButton.setEnabled(true);
        admin.editToMenuButton.setEnabled(false);
        admin.deleteMenuButton.setEnabled(false);
    }
    
}
