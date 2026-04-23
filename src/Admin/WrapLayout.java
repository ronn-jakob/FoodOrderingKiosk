/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Admin;

import java.awt.*;
import javax.swing.*;

public class WrapLayout extends FlowLayout {

    public WrapLayout() {
        super(LEFT);
    }

    public WrapLayout(int align) {
        super(align);
    }

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target);
    }

    private Dimension layoutSize(Container target) {
        synchronized (target.getTreeLock()) {

            int maxWidth = target.getWidth();
            if (maxWidth == 0) maxWidth = Integer.MAX_VALUE;

            int hgap = getHgap();
            int vgap = getVgap();

            int x = 0;
            int y = vgap;
            int rowHeight = 0;

            for (Component comp : target.getComponents()) {
                Dimension d = comp.getPreferredSize();

                if (x + d.width > maxWidth) {
                    x = 0;
                    y += rowHeight + vgap;
                    rowHeight = 0;
                }

                x += d.width + hgap;
                rowHeight = Math.max(rowHeight, d.height);
            }

            y += rowHeight;
            return new Dimension(maxWidth, y);
        }
    }
}
