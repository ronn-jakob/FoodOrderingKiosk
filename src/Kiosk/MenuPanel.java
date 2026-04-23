package Kiosk;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class MenuPanel extends JPanel {

    private static final Color DEFAULT_BG = Color.WHITE;
    private static final Color HOVER_BG = new Color(255, 240, 240);
    private static final Color SELECTED_BG = new Color(255, 224, 224);

    private final int itemId;
    private final String itemName;
    private final double price;
    private final String imagePath;

    private JLabel lblImage;
    private JLabel lblName;
    private JLabel lblPrice;
    private JPanel bottomPanel;
    private boolean selected;

    public MenuPanel(int itemId, String itemName, double price, String imagePath) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.imagePath = imagePath;

        createMenuCard();
    }

    private void createMenuCard() {
        setBackground(DEFAULT_BG);
        setPreferredSize(new Dimension(100, 140));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(171, 48, 61), 2),
                new EmptyBorder(4, 4, 4, 4)
        ));
        setLayout(new BorderLayout(4, 4));

        lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setPreferredSize(new Dimension(90, 90));
        lblImage.setOpaque(true);
        lblImage.setBackground(DEFAULT_BG);
        ImageSize.setFitImage(lblImage, imagePath, 90, 90);

        lblName = new JLabel(
                "<html><div style='text-align:center; width:80px;'>"
                + escapeHtml(itemName)
                + "</div></html>"
        );
        lblName.setHorizontalAlignment(SwingConstants.CENTER);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblName.setFont(new Font("Arial Narrow", Font.BOLD, 12));
        lblName.setForeground(new Color(50, 50, 50));
        lblName.setOpaque(true);
        lblName.setBackground(DEFAULT_BG);

        lblPrice = new JLabel(String.format("%.2f Php", price));
        lblPrice.setHorizontalAlignment(SwingConstants.CENTER);
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPrice.setFont(new Font("Arial Narrow", Font.BOLD, 12));
        lblPrice.setForeground(new Color(171, 48, 61));
        lblPrice.setOpaque(true);
        lblPrice.setBackground(DEFAULT_BG);

        bottomPanel = new JPanel();
        bottomPanel.setBackground(DEFAULT_BG);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(lblName);
        bottomPanel.add(Box.createVerticalStrut(3));
        bottomPanel.add(lblPrice);

        MouseAdapter hoverEffect = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!selected) {
                    applyBackground(HOVER_BG);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!selected) {
                    applyBackground(DEFAULT_BG);
                }
            }
        };

        addMouseListener(hoverEffect);
        lblImage.addMouseListener(hoverEffect);
        lblName.addMouseListener(hoverEffect);
        lblPrice.addMouseListener(hoverEffect);
        bottomPanel.addMouseListener(hoverEffect);

        add(lblImage, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void applyBackground(Color color) {
        setBackground(color);
        lblImage.setBackground(color);
        lblName.setBackground(color);
        lblPrice.setBackground(color);
        bottomPanel.setBackground(color);
        repaint();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        applyBackground(selected ? SELECTED_BG : DEFAULT_BG);
    }

    public boolean isSelectedCard() {
        return selected;
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public void setCardClick(Runnable action) {
        java.awt.event.MouseAdapter click = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.run();
            }
        };

        addRecursiveClick(this, click);
    }

    private void addRecursiveClick(java.awt.Component comp, java.awt.event.MouseAdapter click) {
        comp.addMouseListener(click);

        if (comp instanceof java.awt.Container container) {
            for (java.awt.Component child : container.getComponents()) {
                addRecursiveClick(child, click);
            }
        }
    }

    public int getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public double getPrice() {
        return price;
    }

    public String getImagePath() {
        return imagePath;
    }
}
