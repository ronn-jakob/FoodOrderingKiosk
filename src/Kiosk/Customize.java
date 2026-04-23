package Kiosk;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public class Customize extends javax.swing.JFrame {

    private Connection conn;
    private MenuSQL menuQuery;
    private Cart cart;
    private MenuItemData selectedMenuItem;
    private MenuItemData selectedDrink;
    private java.util.List<MenuItemData> selectedAddons = new java.util.ArrayList<>();
    private int quantity = 1;
    private int editIndex = -1;

    private final java.util.List<MenuPanel> drinkPanels = new java.util.ArrayList<>();
    private final java.util.List<MenuPanel> addonPanels = new java.util.ArrayList<>();

    public Customize() {
        initComponents();
        setLocationRelativeTo(null);
    }

    public Customize(Connection conn, MenuItemData selectedMenuItem, Cart cart, int editIndex) {
        initComponents();
        setLocationRelativeTo(null);

        this.conn = conn;
        this.menuQuery = new MenuSQL(conn);
        this.selectedMenuItem = selectedMenuItem;
        this.cart = cart;
        this.editIndex = editIndex;

        if (selectedMenuItem != null) {
            lblOrder1.setText(selectedMenuItem.getName());
        }

        if (cart != null && editIndex >= 0 && editIndex < cart.getItems().size()) {
            loadExistingSelection(cart.getItems().get(editIndex));
        }

        loadDrinksToPanel();
        loadAddonsToPanel();
        refreshSelectionUI();
        updateTotal();
    }

    private void loadExistingSelection(OrderItem item) {
        if (item == null) {
            return;
        }

        selectedDrink = item.getDrink();
        selectedAddons = new java.util.ArrayList<>(item.getAddons());
        quantity = item.getQuantity();
    }

    private void loadDrinksToPanel() {
        try {
            panelDrinks.removeAll();
            drinkPanels.clear();
            java.util.List<MenuItemData> drinks = menuQuery.findDrinks();

            for (MenuItemData item : drinks) {
                MenuPanel panel = new MenuPanel(
                        item.getId(),
                        item.getName(),
                        item.getPrice(),
                        item.getImagePath()
                );

                if (selectedDrink != null && selectedDrink.getId() == item.getId()) {
                    panel.setSelected(true);
                }

                panel.setCardClick(() -> {
                    if (selectedDrink != null && selectedDrink.getId() == item.getId()) {
                        selectedDrink = null;
                    } else {
                        selectedDrink = item;
                    }
                    refreshDrinkSelection();
                    updateTotal();
                });

                drinkPanels.add(panel);
                panelDrinks.add(panel);
            }

            panelDrinks.revalidate();
            panelDrinks.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading drinks: " + e.getMessage());
        }
    }

    private void loadAddonsToPanel() {
        try {
            panelAdd.removeAll();
            addonPanels.clear();
            java.util.List<MenuItemData> addons = menuQuery.findAddons();

            for (MenuItemData item : addons) {
                MenuPanel panel = new MenuPanel(
                        item.getId(),
                        item.getName(),
                        item.getPrice(),
                        item.getImagePath()
                );

                if (isAddonSelected(item.getId())) {
                    panel.setSelected(true);
                }

                panel.setCardClick(new Runnable() {
                    @Override
                    public void run() {
                        toggleAddon(item);
                        refreshAddonSelection();
                        updateTotal();
                    }
                });

                addonPanels.add(panel);
                panelAdd.add(panel);
            }

            panelAdd.revalidate();
            panelAdd.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading add-ons: " + e.getMessage());
        }
    }

    private boolean isAddonSelected(int addonId) {
        for (MenuItemData addon : selectedAddons) {
            if (addon.getId() == addonId) {
                return true;
            }
        }
        return false;
    }

    private void toggleAddon(MenuItemData item) {
        for (int i = 0; i < selectedAddons.size(); i++) {
            if (selectedAddons.get(i).getId() == item.getId()) {
                selectedAddons.remove(i);
                return;
            }
        }
        selectedAddons.add(item);
    }

    private void refreshSelectionUI() {
        refreshDrinkSelection();
        refreshAddonSelection();
    }

    private void refreshDrinkSelection() {
        for (MenuPanel panel : drinkPanels) {
            panel.setSelected(selectedDrink != null && panel.getItemId() == selectedDrink.getId());
        }
    }

    private void refreshAddonSelection() {
        for (MenuPanel panel : addonPanels) {
            panel.setSelected(isAddonSelected(panel.getItemId()));
        }
    }

    private void updateTotal() {
        if (selectedMenuItem == null) {
            return;
        }

        double total = selectedMenuItem.getPrice();

        if (selectedDrink != null) {
            total += selectedDrink.getPrice();
        }

        for (MenuItemData addon : selectedAddons) {
            total += addon.getPrice();
        }

        total *= quantity;
        lblCategory5.setText(String.format("%.2f", total));
    }

    private void addQuantity() {
        quantity++;
        updateTotal();
    }

    private void minusQuantity() {
        if (quantity > 1) {
            quantity--;
            updateTotal();
        }
    }

    private void addToOrder() {
        if (selectedMenuItem == null) {
            JOptionPane.showMessageDialog(this, "No menu item selected.");
            return;
        }

        if (cart == null) {
            JOptionPane.showMessageDialog(this, "Cart is not available.");
            return;
        }

        // --- THE IDENTITY CRISIS FIX (SMART SORTER) ---
        MenuItemData finalFood = selectedMenuItem;
        MenuItemData finalDrink = selectedDrink;
        java.util.List<MenuItemData> finalAddons = new java.util.ArrayList<>(selectedAddons);

        // Check if the clicked item is actually a DRINK (IDs in the 3000s)
        if (selectedMenuItem.getId() >= 3000 && selectedMenuItem.getId() < 4000) {
            finalFood = null; // Empty the Food slot!
            
            // If they didn't pick a secondary drink inside Customize, make this the main drink
            if (finalDrink == null) {
                finalDrink = selectedMenuItem;
            }
        } 
        // Check if the clicked item is actually an ADD-ON (IDs 4000 and above)
        else if (selectedMenuItem.getId() >= 4000) {
            finalFood = null; // Empty the Food slot!
            finalAddons.add(selectedMenuItem); // Put it in the Add-ons list!
        }
        // ----------------------------------------------

        // Create the item with everything sorted into its proper place
        OrderItem item = new OrderItem(finalFood, finalDrink, finalAddons, quantity);

        if (editIndex >= 0) {
            cart.updateItem(editIndex, item);
        } else {
            cart.addItem(item);
        }

        dispose();
    }

    private void cancelCustomize() {
        dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        steplbl = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        lblOrder1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        lblCategory1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        panelDrinks = new javax.swing.JPanel();
        lblCategory2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        panelAdd = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        lblCategory3 = new javax.swing.JLabel();
        lblCategory4 = new javax.swing.JLabel();
        lblCategory5 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        steplbl2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(171, 48, 61));
        jPanel3.setPreferredSize(new java.awt.Dimension(105, 50));
        jPanel3.setLayout(null);

        jLabel3.setFont(new java.awt.Font("Arial Narrow", 0, 13)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Step 1");
        jPanel3.add(jLabel3);
        jLabel3.setBounds(30, 10, 89, 16);

        jLabel4.setFont(new java.awt.Font("Arial Narrow", 0, 13)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Step 2");
        jPanel3.add(jLabel4);
        jLabel4.setBounds(140, 10, 89, 16);

        jLabel5.setFont(new java.awt.Font("Arial Narrow", 1, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Select Order");
        jPanel3.add(jLabel5);
        jLabel5.setBounds(140, 20, 89, 20);

        jLabel6.setFont(new java.awt.Font("Arial Narrow", 0, 13)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Step 2");
        jPanel3.add(jLabel6);
        jLabel6.setBounds(270, 10, 89, 16);

        jLabel7.setFont(new java.awt.Font("Arial Narrow", 1, 16)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Payment");
        jPanel3.add(jLabel7);
        jLabel7.setBounds(270, 20, 89, 20);

        jLabel8.setFont(new java.awt.Font("Arial Narrow", 0, 13)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Step 3");
        jPanel3.add(jLabel8);
        jLabel8.setBounds(390, 10, 89, 16);

        jLabel9.setFont(new java.awt.Font("Arial Narrow", 1, 16)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Checkout");
        jPanel3.add(jLabel9);
        jLabel9.setBounds(390, 20, 89, 20);

        jPanel7.setBackground(new java.awt.Color(255, 204, 18));
        jPanel7.setPreferredSize(new java.awt.Dimension(100, 3));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 4, Short.MAX_VALUE)
        );

        jPanel3.add(jPanel7);
        jPanel7.setBounds(128, 45, 100, 4);

        steplbl.setFont(new java.awt.Font("Arial Narrow", 1, 16)); // NOI18N
        steplbl.setForeground(new java.awt.Color(255, 255, 255));
        steplbl.setText("Dine In");
        jPanel3.add(steplbl);
        steplbl.setBounds(30, 20, 89, 20);

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 500, -1));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        lblOrder1.setFont(new java.awt.Font("Arial Narrow", 1, 24)); // NOI18N
        lblOrder1.setForeground(new java.awt.Color(171, 48, 61));
        lblOrder1.setText("1 pc Chicken");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(lblOrder1, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(55, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(27, Short.MAX_VALUE)
                .addComponent(lblOrder1)
                .addGap(4, 4, 4))
        );

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, 500, 60));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        lblCategory1.setFont(new java.awt.Font("Arial Narrow", 1, 20)); // NOI18N
        lblCategory1.setText("Drinks");

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        panelDrinks.setBackground(new java.awt.Color(255, 255, 255));
        panelDrinks.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 7, 7));
        jScrollPane1.setViewportView(panelDrinks);

        lblCategory2.setFont(new java.awt.Font("Arial Narrow", 1, 20)); // NOI18N
        lblCategory2.setText("Add ons");

        jScrollPane2.setBorder(null);
        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        panelAdd.setBackground(new java.awt.Color(255, 255, 255));
        panelAdd.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 7, 7));
        jScrollPane2.setViewportView(panelAdd);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCategory2)
                            .addComponent(lblCategory1)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCategory1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(lblCategory2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(77, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 110, 500, 520));

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        lblCategory3.setFont(new java.awt.Font("Arial Narrow", 1, 24)); // NOI18N
        lblCategory3.setText("Item Total");

        lblCategory4.setFont(new java.awt.Font("Arial Narrow", 1, 18)); // NOI18N
        lblCategory4.setText("Total");

        lblCategory5.setFont(new java.awt.Font("Arial Narrow", 1, 22)); // NOI18N
        lblCategory5.setText("00.00");

        jPanel8.setBackground(new java.awt.Color(255, 204, 18));

        steplbl2.setFont(new java.awt.Font("Arial Narrow", 1, 20)); // NOI18N
        steplbl2.setForeground(new java.awt.Color(255, 255, 255));
        steplbl2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        steplbl2.setText("Dine In");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(193, 193, 193)
                .addComponent(steplbl2, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(218, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(7, Short.MAX_VALUE)
                .addComponent(steplbl2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jButton1.setFont(new java.awt.Font("Arial Narrow", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(171, 48, 61));
        jButton1.setText("CANCEL");
        jButton1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(171, 48, 61), 2));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(171, 48, 61));
        jButton2.setFont(new java.awt.Font("Arial Narrow", 1, 18)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("ADD TO ORDER");
        jButton2.setBorder(null);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblCategory3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCategory4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblCategory5, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(14, 14, 14))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(77, 77, 77)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(lblCategory5))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCategory4)
                            .addComponent(lblCategory3))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18))
        );

        jPanel1.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 630, 500, 170));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        cancelCustomize();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        addToOrder();
    }//GEN-LAST:event_jButton2ActionPerformed

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Customize.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Customize.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Customize.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Customize.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Customize().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCategory1;
    private javax.swing.JLabel lblCategory2;
    private javax.swing.JLabel lblCategory3;
    private javax.swing.JLabel lblCategory4;
    private javax.swing.JLabel lblCategory5;
    private javax.swing.JLabel lblOrder1;
    private javax.swing.JPanel panelAdd;
    private javax.swing.JPanel panelDrinks;
    private javax.swing.JLabel steplbl;
    private javax.swing.JLabel steplbl2;
    // End of variables declaration//GEN-END:variables
}
