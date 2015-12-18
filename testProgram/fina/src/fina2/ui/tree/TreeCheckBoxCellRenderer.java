package fina2.ui.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public class TreeCheckBoxCellRenderer extends JCheckBox implements
        TreeCellRenderer {

    private DefaultTreeCellRenderer regularTreeNode;
    private Color textSelectionColor;
    private Color textNonSelectionColor;
    private Color backgroundSelectionColor;
    private Color backgroundNonSelectionColor;
    private fina2.ui.UIManager ui = fina2.Main.main.ui;

    public TreeCheckBoxCellRenderer(EJBTree tree) {

        regularTreeNode = new EJBTreeCellRenderer(tree);
        setTextSelectionColor(regularTreeNode.getTextSelectionColor());
        setTextNonSelectionColor(regularTreeNode.getTextNonSelectionColor());
        setBackgroundSelectionColor(regularTreeNode
                .getBackgroundSelectionColor());
        setBackgroundNonSelectionColor(regularTreeNode
                .getBackgroundNonSelectionColor());

        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        Component retVal = regularTreeNode.getTreeCellRendererComponent(tree,
                value, selected, expanded, leaf, row, hasFocus);
        TreePath path;
        if (leaf && (path = tree.getPathForRow(row)) != null
                && path.getPathCount() == 3) {
            retVal = this;
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path
                    .getLastPathComponent();
            Node node = (Node) ((DefaultMutableTreeNode) treeNode)
                    .getUserObject();
            this.setText(node.getLabel());
            this.setSelected(node.isChecked());
            this.setFont(ui.getFont());

            if (node.isDefaultNode()) {
                this
                        .setFont(this.getFont().deriveFont(
                                Font.BOLD | Font.ITALIC));
            }

            if (selected) {
                this.setBackground(getBackgroundSelectionColor());
                this.setForeground(getTextSelectionColor());
            } else {
                this.setBackground(getBackgroundNonSelectionColor());
                this.setForeground(getTextNonSelectionColor());
            }
        }
        return retVal;
    }

    private void jbInit() throws Exception {
    }

    public void setTextSelectionColor(Color textSelectionColor) {
        this.textSelectionColor = textSelectionColor;
    }

    public void setTextNonSelectionColor(Color textNonSelectionColor) {
        this.textNonSelectionColor = textNonSelectionColor;
    }

    public void setBackgroundSelectionColor(Color backgroundSelectionColor) {
        this.backgroundSelectionColor = backgroundSelectionColor;
    }

    public void setBackgroundNonSelectionColor(Color backgroundNonSelectionColor) {
        this.backgroundNonSelectionColor = backgroundNonSelectionColor;
    }

    public Color getTextSelectionColor() {
        return textSelectionColor;
    }

    public Color getTextNonSelectionColor() {
        return textNonSelectionColor;
    }

    public Color getBackgroundSelectionColor() {
        return backgroundSelectionColor;
    }

    public Color getBackgroundNonSelectionColor() {
        return backgroundNonSelectionColor;
    }
}
