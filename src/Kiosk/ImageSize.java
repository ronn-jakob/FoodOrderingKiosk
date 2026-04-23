package Kiosk;

import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public final class ImageSize {

    private ImageSize() {
    }

    public static void setFillImage(JLabel label, String path) {
        ImageIcon icon = loadIcon(path);

        if (icon == null) {
            label.setText("No Image");
            label.setIcon(null);
            return;
        }

        int labelWidth = getSafeSize(label.getWidth(), label.getPreferredSize().width, icon.getIconWidth());
        int labelHeight = getSafeSize(label.getHeight(), label.getPreferredSize().height, icon.getIconHeight());

        double scale = Math.max(
                (double) labelWidth / icon.getIconWidth(),
                (double) labelHeight / icon.getIconHeight()
        );

        int newWidth = (int) (icon.getIconWidth() * scale);
        int newHeight = (int) (icon.getIconHeight() * scale);

        Image scaledImage = icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        label.setText("");
        label.setIcon(new ImageIcon(scaledImage));
    }

    public static void setFitImage(JLabel label, String path, int width, int height) {
        ImageIcon icon = loadIcon(path);

        if (icon == null) {
            label.setText("No Image");
            label.setIcon(null);
            return;
        }

        double scale = Math.min(
                (double) width / icon.getIconWidth(),
                (double) height / icon.getIconHeight()
        );

        int newWidth = (int) (icon.getIconWidth() * scale);
        int newHeight = (int) (icon.getIconHeight() * scale);

        Image scaledImage = icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        label.setText("");
        label.setIcon(new ImageIcon(scaledImage));
    }

    private static int getSafeSize(int currentSize, int preferredSize, int fallbackSize) {
        if (currentSize > 0) {
            return currentSize;
        }
        if (preferredSize > 0) {
            return preferredSize;
        }
        return fallbackSize;
    }

    private static ImageIcon loadIcon(String path) {
        URL url = Image.class.getResource("/" + path);

        if (url != null) {
            return new ImageIcon(url);
        }

        ImageIcon icon = new ImageIcon(path);
        if (icon.getIconWidth() > 0) {
            return icon;
        }

        return null;
    }
}
