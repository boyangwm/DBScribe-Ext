/*
 * UsersFrame.java
 *
 * Created on October 29, 2001, 11:54 AM
 */

package fina2.security.users;

import java.awt.GridBagConstraints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;

import fina2.BaseFrame;
import fina2.Main;
import fina2.security.Role;
import fina2.security.RoleHome;
import fina2.security.RolePK;
import fina2.security.User;
import fina2.security.UserHome;
import fina2.security.UserPK;
import fina2.servergate.SecurityGate;
import fina2.ui.AbstractDialog;
import fina2.ui.UIManager;
import fina2.ui.tree.EJBTree;
import fina2.ui.tree.Node;
import fina2.util.search.TreeSearcher;
import fina2.util.search.TreeSearcherBase;

@SuppressWarnings("serial")
public class UsersFrame extends BaseFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTree tree;

	private UserCreateAction userCreateAction;
	private RoleCreateAction roleCreateAction;
	private UserAmendAction amendAction;
	private UserDeleteAction deleteAction;
	private PrintAction printAction;

	private TreeSearcherBase treeSearcher;

	public UsersFrame() {

		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.close", "cancel.gif");
		ui.loadIcon("fina2.folder", "folder.gif");
		ui.loadIcon("fina2.security.role", "role.gif");
		ui.loadIcon("fina2.security.user", "user.gif");
		ui.loadIcon("fina2.security.permission", "permission.gif");

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
					if (amendAction.isEnabled()) {
						amendAction.actionPerformed(null);
					}
				}
			}

			public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
			}
		});

		tree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {

				Node node = tree.getSelectedNode();

				if (node == null) {
					/* No appropriate node is selected */
					roleCreateAction.setEnabled(false);
					userCreateAction.setEnabled(false);
					amendAction.setEnabled(false);
					deleteAction.setEnabled(false);
					return;
				}

				int type = ((Integer) node.getType()).intValue();

				switch (type) {
				case -1: /* Role root node */
					roleCreateAction.setEnabled(true);
					userCreateAction.setEnabled(false);
					amendAction.setEnabled(false);
					deleteAction.setEnabled(false);
					break;
				case -2: /* User root node */
					roleCreateAction.setEnabled(false);
					userCreateAction.setEnabled(true);
					amendAction.setEnabled(false);
					deleteAction.setEnabled(false);
					break;
				case 1: /* Role node */
					roleCreateAction.setEnabled(true);
					userCreateAction.setEnabled(false);
					amendAction.setEnabled(true);
					deleteAction.setEnabled(true);
					break;
				case 2: /* User node */
					roleCreateAction.setEnabled(false);
					userCreateAction.setEnabled(true);
					amendAction.setEnabled(true);
					deleteAction.setEnabled(true);
					break;
				}
			}
		});

		roleCreateAction = new RoleCreateAction(this);
		amendAction = new UserAmendAction(this);

		userCreateAction = new UserCreateAction(this);
		deleteAction = new UserDeleteAction(this, main.getMainFrame(), tree);

		printAction = new PrintAction(this);

		treeSearcher = new TreeSearcher();

		initComponents();

		tree.addMenu(new Integer(-1), rolesMenu);
		tree.addMenu(new Integer(-2), usersMenu);
		tree.addMenu(new Integer(1), popupMenu);
		tree.addMenu(new Integer(2), popupMenu);

		scrollPane.setViewportView(tree);

		BaseFrame.ensureVisible(this);
	}

	/** Returns the current selected node */
	Node getSelectedNode() {
		return tree.getSelectedNode();
	}

	public void show() {
		if (isVisible()) {
			return;
		}
		initTree();
		super.show();
	}

	public void initTree() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/security/UserSession");
			fina2.security.UserSessionHome home = (fina2.security.UserSessionHome) PortableRemoteObject.narrow(ref, fina2.security.UserSessionHome.class);

			fina2.security.UserSession session = home.create();

			tree.initTree(session.getTreeNodes(main.getUserHandle(), main.getLanguageHandle()));

			DefaultMutableTreeNode n = tree.findTreeNode((DefaultMutableTreeNode) tree.getModel().getRoot(), new UserPK(-1));
			UIManager.sortAllTree(n);

			Node node = (Node) n.getUserObject();

			node.setLabel(ui.getString("fina2.security.roles"));

			n = tree.findTreeNode((DefaultMutableTreeNode) tree.getModel().getRoot(), new UserPK(-2));

			node = (Node) n.getUserObject();
			node.setLabel(ui.getString("fina2.security.users"));

			UIManager.sortAllTree(n);

			tree.addIcon(new Integer(-1), ui.getIcon("fina2.folder"));

			tree.addIcon(new Integer(-2), ui.getIcon("fina2.folder"));

			tree.addIcon(new Integer(1), ui.getIcon("fina2.security.role"));

			tree.addIcon(new Integer(2), ui.getIcon("fina2.security.user"));

			tree.setRootVisible(false);
			tree.setSelectionRow(0);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
			ui.putConfigValue("fina2.security.users.UsersFrame.visible", new Boolean(false));

			tree.setVisible(false);
			createUserButton.setEnabled(false);
			createRoleButton.setEnabled(false);
			amendButton.setEnabled(false);
			deleteButton.setEnabled(false);
			printButton.setEnabled(false);
			refreshButton.setEnabled(false);
		}
	}

	/** Selects the node with given key data */
	public void selectNode(Object key) {
		tree.gotoNode(key);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() { // GEN-BEGIN:initComponents

		popupMenu = new javax.swing.JPopupMenu();
		amendItem = new javax.swing.JMenuItem();
		jSeparator1 = new javax.swing.JSeparator();
		deleteItem = new javax.swing.JMenuItem();
		rolesMenu = new javax.swing.JPopupMenu();
		createRoleItem = new javax.swing.JMenuItem();
		usersMenu = new javax.swing.JPopupMenu();
		createUserItem = new javax.swing.JMenuItem();
		jPanel1 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		jPanel5 = new javax.swing.JPanel();
		searchTextField = new JTextField();
		createUserButton = new javax.swing.JButton();
		createRoleButton = new javax.swing.JButton();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		amendButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();
		scrollPane = new javax.swing.JScrollPane();

		amendItem.setFont(ui.getFont());
		amendItem.setAction(amendAction);
		popupMenu.add(amendItem);

		popupMenu.add(jSeparator1);

		deleteItem.setFont(ui.getFont());
		deleteItem.setText("Item");
		deleteItem.setAction(deleteAction);
		popupMenu.add(deleteItem);

		createRoleItem.setFont(ui.getFont());
		createRoleItem.setText("Item");
		createRoleItem.setAction(roleCreateAction);
		rolesMenu.add(createRoleItem);

		createUserItem.setFont(ui.getFont());
		createUserItem.setText("Item");
		createUserItem.setAction(userCreateAction);
		usersMenu.add(createUserItem);

		setTitle(ui.getString("fina2.security.usersAction"));
		initBaseComponents();
		jPanel1.setLayout(new java.awt.BorderLayout());

		jPanel3.add(refreshButton);

		closeButton.setFont(ui.getFont());
		jPanel3.add(closeButton);

		jPanel1.add(jPanel3, java.awt.BorderLayout.EAST);
		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "User_Manager");
		} else {
			helpButton.setEnabled(false);
		}
		jPanel4.add(helpButton);

		jPanel1.add(jPanel4, java.awt.BorderLayout.WEST);

		getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

		jPanel2.setLayout(new java.awt.BorderLayout());

		jPanel2.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 5, 1, 5)));
		jPanel5.setLayout(new java.awt.GridBagLayout());

		javax.swing.JLabel searchLabel = new javax.swing.JLabel(ui.getString("fina2.web.search"));
		jPanel5.add(searchLabel, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, null));
		searchTextField.setFont(ui.getFont());
		searchTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				searchTextFieldKeyListener(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				searchTextFieldKeyListener(e);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				searchTextFieldKeyListener(e);
			}
		});
		jPanel5.add(searchTextField, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, null));

		createUserButton.setFont(ui.getFont());
		createUserButton.setAction(userCreateAction);
		createUserButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel5.add(createUserButton, UIManager.getGridBagConstraints(0, 3, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, null));

		createRoleButton.setFont(ui.getFont());
		createRoleButton.setAction(roleCreateAction);
		createRoleButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel5.add(createRoleButton, UIManager.getGridBagConstraints(0, 4, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		jLabel1.setText(ui.getString("fina2.create"));
		jLabel1.setFont(ui.getFont());
		jPanel5.add(jLabel1, UIManager.getGridBagConstraints(0, 2, 10, 10, -1, -1, GridBagConstraints.WEST, -1, null));

		jLabel2.setText(ui.getString("fina2.amend"));
		jLabel2.setFont(ui.getFont());
		jPanel5.add(jLabel2, UIManager.getGridBagConstraints(0, 5, -1, 10, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(5, 0, 0, 0)));

		amendButton.setFont(ui.getFont());
		amendButton.setAction(amendAction);
		amendButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel5.add(amendButton, UIManager.getGridBagConstraints(0, 6, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, null));

		deleteButton.setFont(ui.getFont());
		deleteButton.setAction(deleteAction);
		deleteButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel5.add(deleteButton, UIManager.getGridBagConstraints(0, 7, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		/* Print button */
		JButton printButton = new JButton();
		printButton.setFont(ui.getFont());
		printButton.setAction(printAction);
		printButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel5.add(printButton, UIManager.getGridBagConstraints(0, 8, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(20, 0, 0, 0)));
		// /////////////////////////////////////////////////////////////////////

		jPanel2.add(jPanel5, java.awt.BorderLayout.NORTH);

		getContentPane().add(jPanel2, java.awt.BorderLayout.EAST);

		getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

	} // GEN-END:initComponents

	private void searchTextFieldKeyListener(KeyEvent e) {
		treeSearcher.searctAndSelectTreeNode((DefaultMutableTreeNode) tree.getModel().getRoot(), searchTextField.getText(), tree);
	}

	protected void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {
		initTree();
	}

	protected void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		formComponentHidden(null);
		dispose();
	}

	/** Exit the Application */

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JPopupMenu rolesMenu;
	private javax.swing.JMenuItem createUserItem;
	private javax.swing.JMenuItem createRoleItem;
	private javax.swing.JButton amendButton;
	private javax.swing.JPopupMenu usersMenu;
	private javax.swing.JButton createUserButton;
	private javax.swing.JMenuItem deleteItem;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JButton deleteButton;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JMenuItem amendItem;
	private javax.swing.JPopupMenu popupMenu;
	private javax.swing.JButton createRoleButton;
	private javax.swing.JTextField searchTextField;
	// End of variables declaration//GEN-END:variables

}

/**
 * Handles print action for roles and users
 */
@SuppressWarnings("serial")
class PrintAction extends AbstractAction {

	/** The frame which called the action */
	private UsersFrame usersFrame = null;

	/** Creates the instance of the class */
	PrintAction(UsersFrame frame) {
		usersFrame = frame;
		initUI();
	}

	/** Inits UI element resources */
	private void initUI() {
		fina2.ui.UIManager ui = fina2.Main.main.ui;
		ui.loadIcon("fina2.print", "print.gif");

		putValue(AbstractAction.NAME, ui.getString("fina2.print"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.print"));
	}

	/** Handles when the action called */
	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

		Node node = usersFrame.getSelectedNode();

		if (node == null) {
			/* No selected node */
			return;
		}

		/* Type of selected node */
		int type = ((Integer) node.getType()).intValue();

		switch (type) {
		case -1: /* Role root node */
			printAllRoles();
			break;
		case -2: /* User root node */
			printAllUsers();
			break;
		case 1: /* Role node */
			printRole();
			break;
		case 2: /* User node */
			printUser();
			break;
		}
	}

	/** Prints all roles data */
	private void printAllRoles() {

		try {
			RolePrint rolePrint = new RolePrint("Rolses");
			rolePrint.printAllRoles();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	/** Prints all users */
	private void printAllUsers() {

		try {
			UserPrint userPrint = new UserPrint("Users");
			userPrint.printAllUsers();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	/** Prints the current selected role */
	private void printRole() {
		Node node = usersFrame.getSelectedNode();
		RolePK rolePK = (RolePK) node.getPrimaryKey();

		try {
			UserPrint userPrint = new UserPrint(node.getLabel());
			userPrint.printAllUsers(SecurityGate.getRoleUsers(rolePK));
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}

		// TODO Old Role Print
		// try {
		// RolePrint rolePrint = new RolePrint(node.getLabel());
		// rolePrint.print(rolePK);
		// } catch (Exception e) {
		// Main.generalErrorHandler(e);
		// }
	}

	/** Prints the current selected user */
	private void printUser() {

		Node node = usersFrame.getSelectedNode();
		UserPK userPK = (UserPK) node.getPrimaryKey();

		try {
			UserPrint userPrint = new UserPrint(node.getLabel());
			userPrint.print(userPK);
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

}

/**
 * The class handles role create action.
 */
@SuppressWarnings("serial")
class RoleCreateAction extends AbstractAction {

	/** The frame which called the action */
	private UsersFrame usersFrame = null;

	/** Contructs the instance of the class */
	RoleCreateAction(UsersFrame frame) {
		this.usersFrame = frame;

		initUI();
	}

	/** Inits UI element resources */
	private void initUI() {
		fina2.ui.UIManager ui = fina2.Main.main.ui;
		ui.loadIcon("fina2.security.role", "role.gif");

		putValue(AbstractAction.NAME, ui.getString("fina2.security.role"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.security.role"));
	}

	/** Action handler */
	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		try {
			showRoleDialog();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	/** Shows the role dialog */
	private void showRoleDialog() throws Exception {

		JFrame parentFrame = fina2.Main.main.getMainFrame();

		RoleDialog roleDialog = new RoleDialog(parentFrame); // Create mode
		roleDialog.setVisible(true);

		if (roleDialog.getDialogResult() == AbstractDialog.DialogResult.OK) {
			/*
			 * The OK button pressed. Selecting the created role record in
			 * UsersFrame.
			 */
			RolePK rolePK = roleDialog.getRolePK();
			usersFrame.initTree(); // Updating the tree
			usersFrame.selectNode(rolePK);
		}
	}
}

/**
 * The class handles user create action
 */
@SuppressWarnings("serial")
class UserCreateAction extends AbstractAction {

	/** The frame which called the action */
	private UsersFrame usersFrame = null;

	/** Creates an instance of the class */
	UserCreateAction(UsersFrame frame) {
		this.usersFrame = frame;
		initUI();
	}

	/** Inits UI element resources */
	private void initUI() {
		fina2.ui.UIManager ui = fina2.Main.main.ui;
		ui.loadIcon("fina2.security.user", "user.gif");

		putValue(AbstractAction.NAME, ui.getString("fina2.security.user"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.security.user"));
	}

	/** Action handler */
	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		try {
			showUserDialog();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	/** Shows the user dialog */
	private void showUserDialog() throws Exception {

		JFrame parentFrame = fina2.Main.main.getMainFrame();

		UserDialog userDialog = new UserDialog(parentFrame); // Create mode
		userDialog.setVisible(true);

		if (userDialog.getDialogResult() == AbstractDialog.DialogResult.OK) {
			/* The OK button pressed. Updating the UsersFrame. */
			usersFrame.initTree(); // Updating the tree

			UserPK userPK = userDialog.getUserPK();
			usersFrame.selectNode(userPK);
		}
	}
}

/**
 * The class handles amend action for role and user
 */
@SuppressWarnings("serial")
class UserAmendAction extends AbstractAction {

	/** The frame which called the action */
	private UsersFrame usersFrame = null;

	/** Creates the instance of the class */
	UserAmendAction(UsersFrame frame) {
		usersFrame = frame;

		initUI();
	}

	/** Inits UI element resources */
	private void initUI() {
		fina2.ui.UIManager ui = fina2.Main.main.ui;
		ui.loadIcon("fina2.amend", "amend.gif");

		putValue(AbstractAction.NAME, ui.getString("fina2.amend"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.amend"));
	}

	/** Handles when the action called */
	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

		Node node = usersFrame.getSelectedNode();

		if (node != null) {
			/* There is a selected node. Extracting the primary key */
			Object pk = node.getPrimaryKey();

			try {

				if (pk instanceof UserPK) {
					/* A user item selected in the tree */
					showUserDialog((UserPK) pk);
				} else if (pk instanceof RolePK) {
					/* A role item selected in the tree */
					showRoleDialog((RolePK) pk);
				}

			} catch (Exception e) {
				Main.generalErrorHandler(e);
			}
		}
	}

	/** Shows user dialog */
	private void showUserDialog(UserPK userPK) throws Exception {

		JFrame parentFrame = fina2.Main.main.getMainFrame();

		/* The dialog is created in AMEND mode */
		UserDialog userDialog = new UserDialog(parentFrame, AbstractView.ModeType.AMEND, userPK);
		userDialog.setVisible(true);

		if (userDialog.getDialogResult() == AbstractDialog.DialogResult.OK) {
			/* The OK button pressed. Selecting the user's node in UsersFrame. */
			usersFrame.initTree(); // Updating the tree
			usersFrame.selectNode(userPK);
		}
	}

	/** Shows role dialog */
	private void showRoleDialog(RolePK rolePK) throws Exception {

		JFrame parentFrame = fina2.Main.main.getMainFrame();

		/* The dialog is created in AMEND mode */
		RoleDialog roleDialog = new RoleDialog(parentFrame, AbstractView.ModeType.AMEND, rolePK);
		roleDialog.setVisible(true);

		if (roleDialog.getDialogResult() == AbstractDialog.DialogResult.OK) {
			/* The OK button pressed. Selecting the role's node in UsersFrame. */
			usersFrame.initTree(); // Updating the tree
			usersFrame.selectNode(rolePK);
		}
	}

}

@SuppressWarnings("serial")
class UserDeleteAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTree tree;
	private java.awt.Frame parent;
	@SuppressWarnings("unused")
	private UsersFrame frame;

	UserDeleteAction(UsersFrame frame, java.awt.Frame parent, EJBTree tree) {
		this.tree = tree;
		this.parent = parent;
		this.frame = frame;

		ui.loadIcon("fina2.delete", "delete.gif");

		putValue(AbstractAction.NAME, ui.getString("fina2.delete"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.delete"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		Node node = tree.getSelectedNode();
		if (node == null) {
			return;
		}

		if (node.getPrimaryKey() instanceof UserPK) {
			if (!ui.showConfirmBox(parent, ui.getString("fina2.security.userDeleteQuestion"))) {
				return;
			}
		}

		if (node.getPrimaryKey() instanceof RolePK) {
			if (!ui.showConfirmBox(parent, ui.getString("fina2.security.roleDeleteQuestion"))) {
				return;
			}
		}

		try {
			InitialContext jndi = fina2.Main.getJndiContext();

			if (node.getPrimaryKey() instanceof UserPK) {
				Object ref = jndi.lookup("fina2/security/User");
				UserHome home = (UserHome) PortableRemoteObject.narrow(ref, UserHome.class);

				User user = home.findByPrimaryKey((UserPK) node.getPrimaryKey());

				user.remove();
			}

			if (node.getPrimaryKey() instanceof RolePK) {
				Object ref = jndi.lookup("fina2/security/Role");
				RoleHome home = (RoleHome) PortableRemoteObject.narrow(ref, RoleHome.class);

				Role role = home.findByPrimaryKey((RolePK) node.getPrimaryKey());

				role.remove();
			}

			((javax.swing.tree.DefaultTreeModel) tree.getModel()).removeNodeFromParent(tree.getSelectedTreeNode());

		} catch (Exception e) {
			if (node.getPrimaryKey() instanceof RolePK) {
				Main.errorHandler(parent, Main.getString("fina2.title"), Main.getString("fina2.delete.role"));
			} else {
				Main.errorHandler(parent, Main.getString("fina2.title"), Main.getString("fina2.delete.user"));
			}
		}
	}
}
