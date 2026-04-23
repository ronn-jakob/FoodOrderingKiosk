/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class EditItemMenuCard extends JPanel {
    
    int itemId;

    public EditItemMenuCard(int itemId, String name, double price, String category, String imagePath, AdminMain adminMain, MenuManager mm) { 
        this.itemId = itemId;

        setPreferredSize(new Dimension(150, 180));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        setLayout(new BorderLayout());

        // --- FIX 2: Added safety check for the card thumbnail ---
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        
        if (imagePath != null && !imagePath.isEmpty()) {
            ImageIcon icon = new ImageIcon(imagePath);
            Image img = icon.getImage().getScaledInstance(120, 80, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(img));
        } else {
            imageLabel.setText("No Image"); // Fallback if database has no image
        }

        // Name
        JLabel nameLabel = new JLabel(name, JLabel.CENTER);
        nameLabel.setFont(new Font("Poppins", Font.BOLD, 12));

        // Price
        JLabel priceLabel = new JLabel("₱" + price, JLabel.CENTER);

        // Button
        JButton addButton = new JButton("Edit");
        addButton.addActionListener(e -> {
            
            mm.currentEditItemId = itemId;
            mm.currentEditImagePath = imagePath;

            adminMain.editMenuNameTextField.setText(name);
            adminMain.editMenuPriceTextField.setText(String.valueOf(price));
            adminMain.editMenuComboBox.setSelectedItem(category);
            
            adminMain.addtoMenuButton.setEnabled(false);
            adminMain.editToMenuButton.setEnabled(true);
            adminMain.deleteMenuButton.setEnabled(true);

            adminMain.imagePanel.removeAll();
            adminMain.imagePanel.setLayout(new BorderLayout());
            
            if (imagePath != null && !imagePath.isEmpty()) {
                ImageIcon previewIcon = new ImageIcon(imagePath);
                
                int w = adminMain.imagePanel.getWidth() > 0 ? adminMain.imagePanel.getWidth() : 150;
                int h = adminMain.imagePanel.getHeight() > 0 ? adminMain.imagePanel.getHeight() : 150;
                
                Image previewImg = previewIcon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                adminMain.imagePanel.add(new JLabel(new ImageIcon(previewImg)), BorderLayout.CENTER);
            }
            
            adminMain.imagePanel.revalidate();
            adminMain.imagePanel.repaint();
        });

        // Bottom panel
        JPanel bottomPanel = new JPanel(new GridLayout(3,1));
        bottomPanel.add(nameLabel);
        bottomPanel.add(priceLabel);
        bottomPanel.add(addButton);

        add(imageLabel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.CENTER);
    }
}
