/*
 * EJBTree.java
 *
 * Created on October 17, 2001, 12:00 PM
 */

package fina2.ui.tree;

import java.awt.Font;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

/**
 * 
 * @author David Shalamberidze
 * @version
 */
public class EJBTree extends javax.swing.JTree implements java.awt.event.MouseListener {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private Hashtable icons;
	private Hashtable menus;
	private JPopupMenu popupMenu;

	private DefaultTreeModel model;

	/** Creates new EJBTree */
	public EJBTree() {
		super();
		icons = new Hashtable();
		menus = new Hashtable();

		setFont(ui.getFont());
		addMouseListener(this);
	}

	public void initTree(Node rootNode) {
		DefaultMutableTreeNode treeNode = initNodes(rootNode);
		model = new DefaultTreeModel(treeNode);
		setModel(model);
		setRootVisible(false);
		EJBTreeCellRenderer renderer = new EJBTreeCellRenderer(this);
		// renderer.addMouseListener(this);
		putClientProperty("JTree.lineStyle", "Angled");
		setCellRenderer(renderer);
	}

	private DefaultMutableTreeNode initNodes(Node node) {
		DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node);
		if (!node.isLeaf()) {
			for (Iterator iter = node.getChildren().iterator(); iter.hasNext();) {
				Node n = (Node) iter.next();
				treeNode.add(initNodes(n));
				
			}
		}

		return treeNode;
	}

	public void gotoNode(Object pk) {
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
		DefaultMutableTreeNode treeNode = findTreeNode(rootNode, pk);
		if (treeNode == null)
			return;
		expandPath(new javax.swing.tree.TreePath(treeNode.getPath()));
		scrollPathToVisible(new javax.swing.tree.TreePath(treeNode.getPath()));
		setSelectionPath(new javax.swing.tree.TreePath(treeNode.getPath()));
	}

	public DefaultMutableTreeNode findTreeNode(DefaultMutableTreeNode treeNode, Object pk) {

		if (((Node) treeNode.getUserObject()).getPrimaryKey().equals(pk)) {
			return treeNode;
		}

		if (!treeNode.isLeaf()) {
			for (java.util.Enumeration enu = treeNode.children(); enu.hasMoreElements();) {
				DefaultMutableTreeNode tNode = (DefaultMutableTreeNode) enu.nextElement();
				DefaultMutableTreeNode res = findTreeNode(tNode, pk);
				if (res != null)
					return res;
			}
		}

		return null;
	}

	public Hashtable getIcons() {
		return icons;
	}

	public Hashtable getMenus() {
		return menus;
	}

	public void setPopupMenu(JPopupMenu popupMenu) {
		this.popupMenu = popupMenu;
	}

	public void addIcon(Object type, Icon icon) {
		icons.put(type, icon);
	}

	public void addMenu(Object type, JPopupMenu menu) {
		menus.put(type, menu);
	}

	public Collection getSelectedNodes() {
		if (getSelectionCount() == 0)
			return new Vector();
		javax.swing.tree.TreePath paths[] = getSelectionPaths();

		Vector v = new Vector();
		for (int i = 0; i < paths.length; i++) {
			v.add(((DefaultMutableTreeNode) paths[i].getLastPathComponent()).getUserObject());
		}
		return v;
	}

	public Node getSelectedNode() {
		javax.swing.tree.TreePath path = getSelectionPath();
		if (path == null)
			return null;
		return (Node) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
	}

	public DefaultMutableTreeNode getSelectedTreeNode() {
		javax.swing.tree.TreePath path = getSelectionPath();
		if (path == null)
			return null;
		return (DefaultMutableTreeNode) path.getLastPathComponent();
	}

	public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
	}

	public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
	}

	public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
		if (mouseEvent.getModifiers() != mouseEvent.BUTTON3_MASK)
			return;
		javax.swing.tree.TreePath newPath = getPathForLocation(mouseEvent.getPoint().x, mouseEvent.getPoint().y);
		if (newPath == null)
			return;
		setSelectionPath(newPath);
		Node node = getSelectedNode();
		JPopupMenu menu = null;
		if (menus.size() > 0) {
			menu = (JPopupMenu) menus.get(node.getType());
		} else {
			menu = popupMenu;
		}
		if (menu == null)
			return;
		menu.show((java.awt.Component) mouseEvent.getSource(), mouseEvent.getPoint().x, mouseEvent.getPoint().y);
	}

	public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
	}

	public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
	}

}

class EJBTreeCellRenderer extends DefaultTreeCellRenderer {

	private EJBTree ejbTree;

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	public EJBTreeCellRenderer(EJBTree ejbTree) {
		super();
		this.ejbTree = ejbTree;
	}

	public java.awt.Component getTreeCellRendererComponent(javax.swing.JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		try {
			Node node = (Node) ((DefaultMutableTreeNode) tree.getPathForRow(row).getLastPathComponent()).getUserObject();
			javax.swing.Icon icon = (javax.swing.Icon) ejbTree.getIcons().get(node.getType());
			if (node.isDefaultNode()) {
				this.setFont((ui.getFont().deriveFont(Font.ITALIC)));
			} else {
				this.setFont(ui.getFont());
			}
			if (icon != null)
				setIcon(icon);
		} catch (Exception e) {
		}
		// addMouseListener(ejbTree);
		return this;
	}
}
