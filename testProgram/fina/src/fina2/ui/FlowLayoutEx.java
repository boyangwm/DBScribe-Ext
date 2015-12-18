package fina2.ui;

import java.awt.Component;
import java.awt.Dimension;

public class FlowLayoutEx extends java.awt.FlowLayout {
    public Dimension preferredLayoutSize(java.awt.Container target) {
        layoutContainer(target);
        java.awt.Dimension dim = super.preferredLayoutSize(target);
        int maxY = 0;
        for (int i = 0; i < target.getComponentCount(); i++) {
            Component component = target.getComponent(i);
            int y = (int) component.getLocation().getY()
                    + component.getHeight();
            if (y > maxY) {
                maxY = y;
            }
        }
        dim.height = maxY + getVgap();
        return dim;
    }
}
