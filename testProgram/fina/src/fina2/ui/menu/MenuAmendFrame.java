/*
 * MenuAmendFrame.java
 *
 * Created on October 17, 2001, 12:13 PM
 */

package fina2.ui.menu;

import java.awt.GridBagConstraints;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.AbstractAction;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import fina2.BaseFrame;
import fina2.Main;
import fina2.ui.UIManager;
import fina2.ui.tree.EJBTree;
import fina2.ui.tree.Node;

/**
 * 
 * @author David Shalamberidze
 */
public class MenuAmendFrame extends BaseFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTree tree;

	private CreateMenuAction createMenuAction;
	private CreateMenuItemAction createItemAction;
	private AmendAction amendAction;
	private DeleteAction deleteAction;

	private UpAction upAction;
	private DownAction downAction;

	private boolean canAmend = false;

	/** Creates new form MenuAmendFrame */
	public MenuAmendFrame() {
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.close", "cancel.gif");
		ui.loadIcon("fina2.ui.menu.menuIcon", "menu_icon.gif");
		ui.loadIcon("fina2.ui.menu.menuItemIcon", "menu_item_icon.gif");
		ui.loadIcon("fina2.up", "up.gif");
		ui.loadIcon("fina2.down", "down.gif");

		tree = new EJBTree();

		tree.addMouseListener(new java.awt.event.MouseListener() {
			public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() > 1) {
					DefaultMutableTreeNode node = tree.getSelectedTreeNode();
					if (node.getChildCount() > 0)
						return;
					if (amendAction.isEnabled()) {
						amendAction.actionPerformed(null);
					}
				}
			}

			public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
			}
		});

		tree
				.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
					public void valueChanged(
							javax.swing.event.TreeSelectionEvent evt) {
						Node node = tree.getSelectedNode();
						if (node == null) {
							upAction.setEnabled(false);
							downAction.setEnabled(false);
							return;
						}
						int type = ((Integer) node.getType()).intValue();
						if ((type == MenuConstants.MENU_ACTION_TYPE)
								|| (type == MenuConstants.MENU_APPLICATION_TYPE)) {
							createMenuAction.setEnabled(false);
							createItemAction.setEnabled(false);
						} else {
							createMenuAction.setEnabled(true);
							if (type == -1) {
								createItemAction.setEnabled(false);
								upAction.setEnabled(false);
								downAction.setEnabled(false);
							} else
								createItemAction.setEnabled(true);
						}
						if (type == -1) {
							amendAction.setEnabled(false);
							deleteAction.setEnabled(false);
						} else {
							amendAction.setEnabled(true);
							deleteAction.setEnabled(true);

							if (((MutableTreeNode) (tree.getSelectedTreeNode())
									.getParent())
									.getIndex((MutableTreeNode) tree
											.getSelectedTreeNode()) == 0) {
								upAction.setEnabled(false);
							} else
								upAction.setEnabled(true);

							if (((MutableTreeNode) (tree.getSelectedTreeNode())
									.getParent()).getChildCount() - 1 > ((MutableTreeNode) (tree
									.getSelectedTreeNode()).getParent())
									.getIndex((MutableTreeNode) tree
											.getSelectedTreeNode())) {
								downAction.setEnabled(true);
							} else
								downAction.setEnabled(false);
						}
					}
				});

		createMenuAction = new CreateMenuAction(main.getMainFrame(), tree);
		createItemAction = new CreateMenuItemAction(main.getMainFrame(), tree);
		amendAction = new AmendAction(main.getMainFrame(), tree);
		deleteAction = new DeleteAction(main.getMainFrame(), tree);
		upAction = new UpAction(main.getMainFrame(), tree);
		downAction = new DownAction(main.getMainFrame(), tree);

		initComponents();

		tree.addMenu(new Integer(-1), menuMenu);
		tree.addMenu(new Integer(MenuConstants.MENU_TYPE), menuMenu);
		tree.addMenu(new Integer(MenuConstants.MENU_ACTION_TYPE), itemMenu);
		tree
				.addMenu(new Integer(MenuConstants.MENU_APPLICATION_TYPE),
						itemMenu);

		scrollPane.setViewportView(tree);
		BaseFrame.ensureVisible(this);

	}

	public void show() {
		if (isVisible())
			return;
		initTree();
		super.show();
	}

	private void initTree() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/ui/menu/MenuSession");
			MenuSessionHome home = (MenuSessionHome) PortableRemoteObject
					.narrow(ref, MenuSessionHome.class);

			MenuSession session = home.create();

			tree.initTree(session.getMenuTree(main.getUserHandle(), main
					.getLanguageHandle()));

			/*
			 * Object ref = jndi.lookup("fina2/security/UserSession");
			 * fina2.security.UserSessionHome home =
			 * (fina2.security.UserSessionHome)PortableRemoteObject.narrow (ref,
			 * fina2.security.UserSessionHome.class);
			 * 
			 * fina2.security.UserSession session = home.create();
			 * 
			 * tree.initTree( session.getTreeNodes(main.getUserHandle(),
			 * main.getLanguageHandle()) );
			 */

			tree.addIcon(new Integer(-1), ui.getIcon("fina2.ui.menu.menuIcon"));
			tree.addIcon(new Integer(MenuConstants.MENU_TYPE), ui
					.getIcon("fina2.ui.menu.menuIcon"));
			tree.addIcon(new Integer(MenuConstants.MENU_ACTION_TYPE), ui
					.getIcon("fina2.ui.menu.menuItemIcon"));
			tree.addIcon(new Integer(MenuConstants.MENU_APPLICATION_TYPE), ui
					.getIcon("fina2.ui.menu.menuItemIcon"));

			tree.setRootVisible(true);
			tree.setSelectionRow(0);
		} catch (Exception e) {
			Main.generalErrorHandler(e);
			ui.putConfigValue("fina2.ui.menu.MenuAmendFrame.visible",
					new Boolean(false));
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		menuMenu = new javax.swing.JPopupMenu();
		jMenu1 = new javax.swing.JMenu();
		createMenuItem = new javax.swing.JMenuItem();
		createMenuItemItem = new javax.swing.JMenuItem();
		jSeparator1 = new javax.swing.JSeparator();
		amendItem = new javax.swing.JMenuItem();
		deleteItem = new javax.swing.JMenuItem();
		jSeparator2 = new javax.swing.JSeparator();
		jMenuItem1 = new javax.swing.JMenuItem();
		jMenuItem2 = new javax.swing.JMenuItem();
		itemMenu = new javax.swing.JPopupMenu();
		amend2Item = new javax.swing.JMenuItem();
		delete2Item = new javax.swing.JMenuItem();
		jSeparator3 = new javax.swing.JSeparator();
		jMenuItem3 = new javax.swing.JMenuItem();
		jMenuItem4 = new javax.swing.JMenuItem();
		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		scrollPane = new javax.swing.JScrollPane();
		jPanel4 = new javax.swing.JPanel();
		jPanel5 = new javax.swing.JPanel();
		jLabel2 = new javax.swing.JLabel();
		createMenuButton = new javax.swing.JButton();
		createMenuItemButton = new javax.swing.JButton();
		jLabel3 = new javax.swing.JLabel();
		amendButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();
		jLabel1 = new javax.swing.JLabel();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();

		jMenu1.setText(ui.getString("fina2.create"));
		jMenu1.setFont(ui.getFont());
		createMenuItem.setFont(ui.getFont());
		createMenuItem.setText("Item");
		createMenuItem.setAction(createMenuAction);
		jMenu1.add(createMenuItem);

		createMenuItemItem.setFont(ui.getFont());
		createMenuItemItem.setText("Item");
		createMenuItemItem.setAction(createItemAction);
		jMenu1.add(createMenuItemItem);

		menuMenu.add(jMenu1);

		menuMenu.add(jSeparator1);

		amendItem.setFont(ui.getFont());
		amendItem.setText("Item");
		amendItem.setAction(amendAction);
		menuMenu.add(amendItem);

		deleteItem.setFont(ui.getFont());
		deleteItem.setText("Item");
		deleteItem.setAction(deleteAction);
		menuMenu.add(deleteItem);

		menuMenu.add(jSeparator2);

		jMenuItem1.setFont(ui.getFont());
		jMenuItem1.setText("Item");
		jMenuItem1.setAction(upAction);
		menuMenu.add(jMenuItem1);

		jMenuItem2.setFont(ui.getFont());
		jMenuItem2.setText("Item");
		jMenuItem2.setAction(downAction);
		menuMenu.add(jMenuItem2);

		amend2Item.setFont(ui.getFont());
		amend2Item.setText("Item");
		amend2Item.setAction(amendAction);
		itemMenu.add(amend2Item);

		delete2Item.setFont(ui.getFont());
		delete2Item.setText("Item");
		delete2Item.setAction(deleteAction);
		itemMenu.add(delete2Item);

		itemMenu.add(jSeparator3);

		jMenuItem3.setFont(ui.getFont());
		jMenuItem3.setText("Item");
		jMenuItem3.setAction(upAction);
		itemMenu.add(jMenuItem3);

		jMenuItem4.setFont(ui.getFont());
		jMenuItem4.setText("Item");
		jMenuItem4.setAction(downAction);
		itemMenu.add(jMenuItem4);

		setTitle(ui.getString("fina2.ui.menu.menuAmendAction"));
		initBaseComponents();

		jPanel1.setLayout(new java.awt.BorderLayout());

		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "Menu_Tree");
		} else {
			helpButton.setEnabled(false);
		}
		jPanel2.add(helpButton);

		jPanel1.add(jPanel2, java.awt.BorderLayout.WEST);

		jPanel3.add(refreshButton);

		jPanel3.add(closeButton);

		jPanel1.add(jPanel3, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

		getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

		jPanel4.setLayout(new java.awt.BorderLayout());

		jPanel4.setBorder(new javax.swing.border.EmptyBorder(
				new java.awt.Insets(1, 5, 1, 5)));
		jPanel5.setLayout(new java.awt.GridBagLayout());

		jLabel2.setText(ui.getString("fina2.create"));
		jLabel2.setFont(ui.getFont());
		jPanel5.add(jLabel2, UIManager.getGridBagConstraints(0, 0, -1, 10, -1,
				-1, -1, GridBagConstraints.HORIZONTAL, null));

		createMenuButton.setFont(ui.getFont());
		createMenuButton.setAction(createMenuAction);
		jPanel5.add(createMenuButton, UIManager.getGridBagConstraints(0, 1, -1,
				-1, -1, -1, -1, GridBagConstraints.HORIZONTAL, null));

		createMenuItemButton.setFont(ui.getFont());
		createMenuItemButton.setAction(createItemAction);
		jPanel5.add(createMenuItemButton, UIManager.getGridBagConstraints(0, 2,
				-1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL,
				new java.awt.Insets(5, 0, 0, 0)));

		jLabel3.setText(ui.getString("fina2.amend"));
		jLabel3.setFont(ui.getFont());
		jPanel5.add(jLabel3, UIManager.getGridBagConstraints(0, 3, -1, 10, -1,
				-1, -1, GridBagConstraints.HORIZONTAL, null));

		amendButton.setFont(ui.getFont());
		amendButton.setAction(amendAction);
		jPanel5.add(amendButton, UIManager.getGridBagConstraints(0, 4, -1, -1,
				-1, -1, -1, GridBagConstraints.HORIZONTAL, null));

		deleteButton.setFont(ui.getFont());
		deleteButton.setAction(deleteAction);
		jPanel5.add(deleteButton, UIManager.getGridBagConstraints(0, 5, -1, -1,
				-1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(
						5, 0, 0, 0)));

		jLabel1.setText(ui.getString("fina2.sequence"));
		jLabel1.setFont(ui.getFont());
		jPanel5.add(jLabel1, UIManager.getGridBagConstraints(0, 9, -1, 10, -1,
				-1, -1, GridBagConstraints.HORIZONTAL, null));

		jButton1.setFont(ui.getFont());
		jButton1.setAction(upAction);
		jButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel5.add(jButton1, UIManager.getGridBagConstraints(0, 10, -1, -1,
				-1, -1, -1, GridBagConstraints.HORIZONTAL, null));

		jButton2.setFont(ui.getFont());
		jButton2.setAction(downAction);
		jButton2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel5.add(jButton2, UIManager.getGridBagConstraints(0, 11, -1, -1,
				-1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(
						5, 0, 0, 0)));

		jPanel4.add(jPanel5, java.awt.BorderLayout.NORTH);

		getContentPane().add(jPanel4, java.awt.BorderLayout.EAST);

	}// GEN-END:initComponents

	protected void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {
		initTree();
	}

	protected void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		formComponentHidden(null);
		dispose();
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JButton amendButton;
	private javax.swing.JMenuItem createMenuItemItem;
	private javax.swing.JMenuItem delete2Item;
	private javax.swing.JButton jButton2;
	private javax.swing.JPopupMenu itemMenu;
	private javax.swing.JMenuItem createMenuItem;
	private javax.swing.JButton jButton1;
	private javax.swing.JMenuItem jMenuItem4;
	private javax.swing.JPopupMenu menuMenu;
	private javax.swing.JMenuItem jMenuItem3;
	private javax.swing.JMenuItem jMenuItem2;
	private javax.swing.JMenuItem jMenuItem1;
	private javax.swing.JMenuItem deleteItem;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JButton deleteButton;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JSeparator jSeparator3;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JSeparator jSeparator2;
	private javax.swing.JMenuItem amend2Item;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JButton createMenuButton;
	private javax.swing.JButton createMenuItemButton;
	private javax.swing.JMenuItem amendItem;
	private javax.swing.JMenu jMenu1;
	// End of variables declaration//GEN-END:variables

}

class CreateMenuAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private MenuAmendDialog dialog;

	private EJBTree tree;

	CreateMenuAction(java.awt.Frame parent, EJBTree tree) {
		this.tree = tree;

		dialog = new MenuAmendDialog(parent, true);

		putValue(AbstractAction.NAME, ui.getString("fina2.ui.menu.menu"));
		putValue(AbstractAction.SMALL_ICON, ui
				.getIcon("fina2.ui.menu.menuIcon"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		Node node = tree.getSelectedNode();
		if (node == null)
			return;

		if (((Integer) node.getType()).intValue() != MenuConstants.MENU_TYPE
				&& ((Integer) node.getType()).intValue() != -1)
			return;

		dialog.show(node, null);
		Node newNode = dialog.getNode();
		if (newNode != null) {
			DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(
					newNode);
			DefaultMutableTreeNode treeNode = tree.getSelectedTreeNode();
			((javax.swing.tree.DefaultTreeModel) tree.getModel())
					.insertNodeInto(newTreeNode, treeNode, treeNode
							.getChildCount());
			ui.loadMenuBar();
			main.getMainFrame().setJMenuBar(ui.getMenuBar());
			main.getMainFrame().validate();
		}
	}

}

class CreateMenuItemAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTree tree;
	private MenuItemAmendDialog dialog;

	CreateMenuItemAction(java.awt.Frame parent, EJBTree tree) {
		this.tree = tree;

		dialog = new MenuItemAmendDialog(parent, true);

		putValue(AbstractAction.NAME, ui.getString("fina2.ui.menu.menuItem"));
		putValue(AbstractAction.SMALL_ICON, ui
				.getIcon("fina2.ui.menu.menuItemIcon"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		Node node = tree.getSelectedNode();
		if (node == null)
			return;

		if (((Integer) node.getType()).intValue() != MenuConstants.MENU_TYPE
				&& ((Integer) node.getType()).intValue() != -1)
			return;

		dialog.show(node, null);
		Node newNode = dialog.getNode();
		if (newNode != null) {
			DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(
					newNode);
			DefaultMutableTreeNode treeNode = tree.getSelectedTreeNode();
			((javax.swing.tree.DefaultTreeModel) tree.getModel())
					.insertNodeInto(newTreeNode, treeNode, treeNode
							.getChildCount());
			ui.loadMenuBar();
			main.getMainFrame().setJMenuBar(ui.getMenuBar());
			main.getMainFrame().validate();
		}
	}

}

class AmendAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTree tree;

	private MenuAmendDialog menuDialog;
	private MenuItemAmendDialog itemDialog;

	AmendAction(java.awt.Frame parent, EJBTree tree) {
		this.tree = tree;

		menuDialog = new MenuAmendDialog(parent, true);
		itemDialog = new MenuItemAmendDialog(parent, true);

		ui.loadIcon("fina2.amend", "amend.gif");
		putValue(AbstractAction.NAME, ui.getString("fina2.amend"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.amend"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		Node node = tree.getSelectedNode();
		if (node == null)
			return;

		if (((Integer) node.getType()).intValue() == MenuConstants.MENU_TYPE) {
			menuDialog.show(null, node);
		} else {
			itemDialog.show(null, node);
		}
		((javax.swing.tree.DefaultTreeModel) tree.getModel()).nodeChanged(tree
				.getSelectedTreeNode());
		ui.loadMenuBar();
		main.getMainFrame().setJMenuBar(ui.getMenuBar());
		main.getMainFrame().validate();
	}

}

class DeleteAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTree tree;
	private java.awt.Frame parent;

	DeleteAction(java.awt.Frame parent, EJBTree tree) {
		this.tree = tree;
		this.parent = parent;

		ui.loadIcon("fina2.delete", "delete.gif");
		putValue(AbstractAction.NAME, ui.getString("fina2.delete"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.delete"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		Node node = tree.getSelectedNode();
		if (node == null)
			return;

		if (!ui.showConfirmBox(parent, ui
				.getString("fina2.ui.menu.menuDeleteQuestion")))
			return;

		try {
			InitialContext jndi = fina2.Main.getJndiContext();

			Object ref = jndi.lookup("fina2/ui/menu/Menu");
			MenuHome home = (MenuHome) PortableRemoteObject.narrow(ref,
					MenuHome.class);

			MenuPK pk = (MenuPK) node.getPrimaryKey();
			Menu menu = home.findByPrimaryKey(pk);

			menu.remove();

			((javax.swing.tree.DefaultTreeModel) tree.getModel())
					.removeNodeFromParent(tree.getSelectedTreeNode());
			ui.loadMenuBar();
			main.getMainFrame().setJMenuBar(ui.getMenuBar());
			main.getMainFrame().validate();

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

}

class UpAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;
	private EJBTree tree;
	private java.awt.Frame parent;
	private int nodeIndex;

	UpAction(java.awt.Frame parent, EJBTree tree) {

		this.tree = tree;
		this.parent = parent;
		putValue(AbstractAction.NAME, ui.getString("fina2.up"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.up"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

		Node node = tree.getSelectedNode();

		if (node == null)
			return;

		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/ui/menu/MenuSession");
			MenuSessionHome home = (MenuSessionHome) PortableRemoteObject
					.narrow(ref, MenuSessionHome.class);

			MenuSession session = home.create();
			session.moveUp((MenuPK) node.getPrimaryKey());
			session.sort((MenuPK) node.getPrimaryKey());
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		MutableTreeNode currentNode = tree.getSelectedTreeNode();
		MutableTreeNode parent = (MutableTreeNode) (tree.getSelectedTreeNode())
				.getParent();

		nodeIndex = parent.getIndex(currentNode);
		if (nodeIndex != 0) {
			((javax.swing.tree.DefaultTreeModel) tree.getModel())
					.removeNodeFromParent(currentNode);
			((javax.swing.tree.DefaultTreeModel) tree.getModel())
					.insertNodeInto(currentNode, parent, nodeIndex - 1);
			tree.setSelectionPath(new TreePath(
					((DefaultMutableTreeNode) currentNode).getPath()));
		}
		ui.loadMenuBar();
		main.getMainFrame().setJMenuBar(ui.getMenuBar());
		main.getMainFrame().validate();
	}
}

class DownAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;
	private EJBTree tree;
	private java.awt.Frame parent;
	private int nodeIndex;

	DownAction(java.awt.Frame parent, EJBTree tree) {
		this.tree = tree;
		putValue(AbstractAction.NAME, ui.getString("fina2.down"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.down"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

		Node node = tree.getSelectedNode();

		if (node == null)
			return;

		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/ui/menu/MenuSession");
			MenuSessionHome home = (MenuSessionHome) PortableRemoteObject
					.narrow(ref, MenuSessionHome.class);

			MenuSession session = home.create();
			session.moveDown((MenuPK) node.getPrimaryKey());
			session.sort((MenuPK) node.getPrimaryKey());
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

		MutableTreeNode currentNode = tree.getSelectedTreeNode();
		MutableTreeNode parent = (MutableTreeNode) (tree.getSelectedTreeNode())
				.getParent();

		nodeIndex = parent.getIndex(currentNode);
		if (parent.getChildCount() - 1 > nodeIndex) {
			((javax.swing.tree.DefaultTreeModel) tree.getModel())
					.removeNodeFromParent(currentNode);
			((javax.swing.tree.DefaultTreeModel) tree.getModel())
					.insertNodeInto(currentNode, parent, nodeIndex + 1);
			tree.setSelectionPath(new TreePath(
					((DefaultMutableTreeNode) currentNode).getPath()));
		}
		ui.loadMenuBar();
		main.getMainFrame().setJMenuBar(ui.getMenuBar());
		main.getMainFrame().validate();
	}
}
