/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Kiosk;

import java.sql.Connection;

public class Summary extends javax.swing.JFrame {

    private Cart cart;
    private java.sql.Connection conn;

    public Summary() {
        initComponents();
        setLocationRelativeTo(null);
    }

    public Summary(Cart cart, java.sql.Connection conn) {
        initComponents();
        setLocationRelativeTo(null);

        this.cart = cart;
        this.conn = conn;

        lblCategory1.setText(cart.getOrderType());

        panelSum.setLayout(new javax.swing.BoxLayout(panelSum, javax.swing.BoxLayout.Y_AXIS));

        refreshSummary();
    }

    private void refreshSummary() {
        panelSum.removeAll();
        panelSum.setLayout(new javax.swing.BoxLayout(panelSum, javax.swing.BoxLayout.Y_AXIS));

        for (int i = 0; i < cart.getItems().size(); i++) {
            int index = i;
            OrderItem item = cart.getItems().get(i);

            javax.swing.JPanel itemPanel = createItemPanel(item, index);
            panelSum.add(itemPanel);
            panelSum.add(javax.swing.Box.createVerticalStrut(10));
        }

        lblTotal.setText(String.format("%.2f", cart.getTotal()));

        panelSum.revalidate();
        panelSum.repaint();
    }

    private javax.swing.JPanel createItemPanel(OrderItem item, int index) {
        javax.swing.JPanel row = new javax.swing.JPanel();
        row.setBackground(java.awt.Color.WHITE);
        row.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(171, 48, 61), 2));
        row.setMaximumSize(new java.awt.Dimension(440, 120));
        row.setPreferredSize(new java.awt.Dimension(440, 120));

        String displayTitle = "Unknown Item";
        
        if (item.getMenuItem() != null) {
            displayTitle = item.getMenuItem().getName(); // It's a normal food item
        } else if (item.getDrink() != null) {
            displayTitle = item.getDrink().getName();    // It's a standalone drink
        } else if (item.getAddons() != null && !item.getAddons().isEmpty()) {
            displayTitle = item.getAddons().get(0).getName(); // It's a standalone add-on
        }

        // Now create the label safely!
        javax.swing.JLabel lblName = new javax.swing.JLabel(displayTitle);
        lblName.setFont(new java.awt.Font("Arial Narrow", java.awt.Font.BOLD, 18));

        javax.swing.JLabel lblDetails = new javax.swing.JLabel(item.getDetailsText());
        lblDetails.setFont(new java.awt.Font("Arial Narrow", java.awt.Font.PLAIN, 14));

        javax.swing.JLabel lblPriceItem = new javax.swing.JLabel(String.format("%.2f", item.getSubtotal()));
        lblPriceItem.setFont(new java.awt.Font("Arial Narrow", java.awt.Font.BOLD, 18));
        lblPriceItem.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        javax.swing.JButton btnMinusItem = new javax.swing.JButton("-");
        btnMinusItem.setFont(new java.awt.Font("Arial Narrow", java.awt.Font.BOLD, 24));
        btnMinusItem.setForeground(new java.awt.Color(171, 48, 61));
        btnMinusItem.setBorder(null);
        btnMinusItem.setContentAreaFilled(false);

        javax.swing.JLabel lblQtyItem = new javax.swing.JLabel(String.valueOf(item.getQuantity()));
        lblQtyItem.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblQtyItem.setFont(new java.awt.Font("Arial Narrow", java.awt.Font.BOLD, 18));
        lblQtyItem.setForeground(new java.awt.Color(171, 48, 61));
        lblQtyItem.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(171, 48, 61), 2));

        javax.swing.JButton btnPlusItem = new javax.swing.JButton("+");
        btnPlusItem.setFont(new java.awt.Font("Arial Narrow", java.awt.Font.BOLD, 24));
        btnPlusItem.setForeground(new java.awt.Color(171, 48, 61));
        btnPlusItem.setBorder(null);
        btnPlusItem.setContentAreaFilled(false);

        javax.swing.JButton btnRemoveItem = new javax.swing.JButton("REMOVE ITEM");
        btnRemoveItem.setFont(new java.awt.Font("Arial Narrow", java.awt.Font.BOLD, 15));
        btnRemoveItem.setForeground(new java.awt.Color(171, 48, 61));
        btnRemoveItem.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(171, 48, 61), 2));
        btnRemoveItem.setContentAreaFilled(false);

        javax.swing.JButton btnEditItem = new javax.swing.JButton("EDIT ITEM");
        btnEditItem.setFont(new java.awt.Font("Arial Narrow", java.awt.Font.BOLD, 15));
        btnEditItem.setForeground(java.awt.Color.WHITE);
        btnEditItem.setBackground(new java.awt.Color(171, 48, 61));
        btnEditItem.setBorder(null);
        btnEditItem.setOpaque(true);

        btnMinusItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    refreshSummary();
                }
            }
        });

        btnPlusItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                item.setQuantity(item.getQuantity() + 1);
                refreshSummary();
            }
        });

        btnRemoveItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cart.removeItem(index);
                refreshSummary();
            }
        });

        btnEditItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Customize customize = new Customize(conn, item.getMenuItem(), cart, index);
                customize.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent we) {
                        refreshSummary();
                    }
                });
                customize.setVisible(true);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(row);
        row.setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(btnMinusItem, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(lblQtyItem, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnPlusItem, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
                                                .addComponent(btnRemoveItem, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnEditItem, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(lblPriceItem, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblName)
                                        .addComponent(lblPriceItem))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblDetails)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnMinusItem)
                                        .addComponent(lblQtyItem, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnPlusItem)
                                        .addComponent(btnRemoveItem, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnEditItem, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(14, Short.MAX_VALUE))
        );

        return row;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblCategory = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        lblCategory1 = new javax.swing.JLabel();
        lblCategory5 = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        panelSum = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        lblCategory.setFont(new java.awt.Font("Arial Narrow", 1, 24)); // NOI18N
        lblCategory.setForeground(new java.awt.Color(171, 48, 61));
        lblCategory.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCategory.setText("ORDER SUMMARY");

        jPanel8.setBackground(new java.awt.Color(255, 204, 18));

        lblCategory1.setFont(new java.awt.Font("Arial Narrow", 1, 18)); // NOI18N
        lblCategory1.setForeground(new java.awt.Color(255, 255, 255));
        lblCategory1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCategory1.setText("Dine In");

        lblCategory5.setFont(new java.awt.Font("Arial Narrow", 1, 18)); // NOI18N
        lblCategory5.setForeground(new java.awt.Color(255, 255, 255));
        lblCategory5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCategory5.setText("TOTAL: ");

        lblTotal.setFont(new java.awt.Font("Arial Narrow", 1, 18)); // NOI18N
        lblTotal.setForeground(new java.awt.Color(255, 255, 255));
        lblTotal.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTotal.setText("99.00");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(lblCategory1, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 107, Short.MAX_VALUE)
                .addComponent(lblCategory5, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(11, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCategory1)
                    .addComponent(lblCategory5)
                    .addComponent(lblTotal))
                .addContainerGap())
        );

        jButton1.setFont(new java.awt.Font("Arial Narrow", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(171, 48, 61));
        jButton1.setText("BACK TO MENU");
        jButton1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(171, 48, 61), 2));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(171, 48, 61));
        jButton2.setFont(new java.awt.Font("Arial Narrow", 1, 18)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("CHECKOUT");
        jButton2.setBorder(null);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setViewportView(panelSum);

        panelSum.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panelSumLayout = new javax.swing.GroupLayout(panelSum);
        panelSum.setLayout(panelSumLayout);
        panelSumLayout.setHorizontalGroup(
            panelSumLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 694, Short.MAX_VALUE)
        );
        panelSumLayout.setVerticalGroup(
            panelSumLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 618, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(panelSum);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(132, 132, 132)
                        .addComponent(lblCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 481, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(87, 87, 87)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(lblCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35))
        );

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
        // TODO add your handling code here:
        SelectOrder order = new SelectOrder(cart.getOrderType(), cart);
        order.setVisible(true);
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        if (cart.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }

        Payment payment = new Payment(cart, conn);
        payment.setVisible(true);
        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Summary.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Summary.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Summary.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Summary.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Summary().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblCategory1;
    private javax.swing.JLabel lblCategory5;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel panelSum;
    // End of variables declaration//GEN-END:variables
}
