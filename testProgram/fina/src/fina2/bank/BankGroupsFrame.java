/*
 * BankGroupsFrame.java
 *
 * Created on October 19, 2001, 7:10 PM
 */

package fina2.bank;

import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.AbstractAction;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import fina2.BaseFrame;
import fina2.FinaTypeException;
import fina2.Main;
import fina2.ui.ScrollableMessageBox;
import fina2.ui.UIManager;
import fina2.ui.UIManager.IndeterminateLoading;
import fina2.ui.sheet.Spreadsheet;
import fina2.ui.sheet.SpreadsheetsManager;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableReviewFrame;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;
import fina2.ui.tree.EJBTree;
import fina2.ui.tree.Node;

public class BankGroupsFrame extends BaseFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTree tree;

	private BankGroupsAmendAction amendAction;
	private BankGroupsInsertAction insertAction;
	private BankGroupsReviewAction reviewAction;
	private BankGroupsDeleteAction deleteAction;
	private BankGroupsSetDefaultAction setDefaultAction;

	private boolean canAmend = false;
	private boolean canDelete = false;
	private boolean canReview = false;

	private DefaultMutableTreeNode root;

	/** Creates new form Banks */
	public BankGroupsFrame() {
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.print", "print.gif");
		ui.loadIcon("fina2.refresh", "refresh.gif");
		ui.loadIcon("fina2.close", "cancel.gif");

		ui.loadIcon("fina2.amend", "amend.gif");
		ui.loadIcon("fina2.create", "insert.gif");
		ui.loadIcon("fina2.review", "review.gif");
		ui.loadIcon("fina2.delete", "delete.gif");

		ui.loadIcon("fina2.node.default", "node_def.gif");

		this.setFont(ui.getFont());

		tree = new EJBTree();

		amendAction = new BankGroupsAmendAction(main.getMainFrame(), tree);
		insertAction = new BankGroupsInsertAction(main.getMainFrame(), tree);
		reviewAction = new BankGroupsReviewAction(main.getMainFrame(), tree);
		deleteAction = new BankGroupsDeleteAction(main.getMainFrame(), tree);
		setDefaultAction = new BankGroupsSetDefaultAction(main.getMainFrame(),
				tree);

		tree.addMouseListener(new java.awt.event.MouseListener() {
			public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2
						&& tree.getSelectionPath().getPathCount() > 2) {
					if (amendAction.isEnabled()) {
						amendAction.actionPerformed(null);
					} else {
						if (reviewAction.isEnabled()) {
							reviewAction.actionPerformed(null);
						}
					}
				}
			}

			public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
			}
		});

		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
				Node n = null;
				TreePath path = tree.getSelectionPath();
				int level = (path == null) ? 0 : path.getPathCount();
				switch (level) {
				case 1: // root node
					amendAction.setEnabled(false);
					reviewAction.setEnabled(false);
					insertAction.setEnabled(canAmend);
					deleteAction.setEnabled(false);
					setDefaultAction.setEnabled(false);
					break;
				case 2: // criterion node
					amendAction.setEnabled(canAmend);
					reviewAction.setEnabled(canReview || canAmend);
					insertAction.setEnabled(canAmend);
					Node node = (Node) ((DefaultMutableTreeNode) path
							.getLastPathComponent()).getUserObject();
					if (node.isDefaultNode()) {
						deleteAction.setEnabled(false);
						setDefaultAction.setEnabled(false);
					} else {
						deleteAction.setEnabled(canDelete);
						setDefaultAction.setEnabled(canAmend);
					}
					break;
				case 3: // bank group node
					amendAction.setEnabled(canAmend);
					reviewAction.setEnabled(canReview || canAmend);
					insertAction.setEnabled(false);
					deleteAction.setEnabled(canDelete);
					setDefaultAction.setEnabled(false);
					break;
				default:
					amendAction.setEnabled(false);
					reviewAction.setEnabled(false);
					insertAction.setEnabled(false);
					deleteAction.setEnabled(false);
					setDefaultAction.setEnabled(false);
				}
			}
		});

		initComponents();

		tree.setPopupMenu(popupMenu);

		scrollPane.setViewportView(tree);
		BaseFrame.ensureVisible(this);

	}

	@SuppressWarnings("unchecked")
	private void initTree() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/bank/BankSession");
			BankSessionHome home = (BankSessionHome) PortableRemoteObject
					.narrow(ref, BankSessionHome.class);

			BankSession session = home.create();

			Vector colNames = new Vector();
			colNames.add(ui.getString("fina2.code"));
			colNames.add(ui.getString("fina2.description"));

			DefaultTreeModel model = ((javax.swing.tree.DefaultTreeModel) tree
					.getModel());
			root = (DefaultMutableTreeNode) model.getRoot();
			List colapsedBankGroupIDs = new LinkedList();
			if (root != null) {
				Enumeration enu = root.children();
				while (enu.hasMoreElements()) {
					DefaultMutableTreeNode tn = (DefaultMutableTreeNode) enu
							.nextElement();
					if (tn.getUserObject() instanceof Node
							& tree.isCollapsed(new TreePath(tn.getPath()))) {
						Node n = (Node) tn.getUserObject();
						colapsedBankGroupIDs.add(n.getPrimaryKey());
					}
				}
			}

			Node rootNode = new Node(new BankPK(0),
					ui.getString("fina2.bank.criterions"), new Integer(
							BanksConstants.NODETYPE_ROOT));
			tree.initTree(rootNode);

			root = new DefaultMutableTreeNode(rootNode);
			prepareNodes(
					root,
					session.getBankCriterionRows(main.getUserHandle(),
							main.getLanguageHandle()),
					session.getBankGroupsRows(main.getUserHandle(),
							main.getLanguageHandle()));
			((javax.swing.tree.DefaultTreeModel) tree.getModel()).setRoot(root);

			UIManager.sortAllTree(root);

			tree.addIcon(new Integer(BanksConstants.NODETYPE_BANK_GROUP_NODE),
					ui.getIcon("fina2.node"));

			tree.addIcon(new Integer(
					BanksConstants.NODETYPE_BANK_CRITERION_NODE), ui
					.getIcon("fina2.node"));

			tree.addIcon(new Integer(
					BanksConstants.NODETYPE_DEF_BANK_CRITERION_NODE), ui
					.getIcon("fina2.node.default"));

			tree.setRootVisible(true);

			for (int i = 0; i < tree.getRowCount(); i++) {
				TreePath path = tree.getPathForRow(i);
				tree.expandRow(i);
				if (path != null && path.getPathCount() == 2) {
					DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path
							.getLastPathComponent();
					if (colapsedBankGroupIDs.contains(((Node) treeNode
							.getUserObject()).getPrimaryKey())) {
						tree.collapsePath(path);
					}
				}
			}
			tree.setSelectionRow(0);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
			ui.putConfigValue("fina2.bank.BankGroupsFrame.visible",
					new Boolean(false));
		}
	}

	@SuppressWarnings("unchecked")
	private synchronized void prepareNodes(DefaultMutableTreeNode parent,
			Collection criterionRows, Collection bankGroupRows) {

		ui.sortCriterionRowsData(criterionRows);

		String def = String.valueOf(BankCriterionConstants.DEF_CRITERION);
		for (Iterator iter = criterionRows.iterator(); iter.hasNext();) {
			TableRow criterionRow = (TableRow) iter.next();
			Node criterionNode = new Node(
					criterionRow.getPrimaryKey(),
					criterionRow.getValue(0) + " / " + criterionRow.getValue(1),
					new Integer(BanksConstants.NODETYPE_BANK_CRITERION_NODE));
			if (def.equals(criterionRow.getValue(2))) { // is default
				// criterionNode.setDefaultNode(true);
				criterionNode.setType(new Integer(
						BanksConstants.NODETYPE_DEF_BANK_CRITERION_NODE));
			}

			DefaultMutableTreeNode criterionTreeNode = new DefaultMutableTreeNode(
					criterionNode);
			parent.add(criterionTreeNode);
			String criterionId = String.valueOf(((BankCriterionPK) criterionRow
					.getPrimaryKey()).getId());
			for (Iterator bankGroupIter = bankGroupRows.iterator(); bankGroupIter
					.hasNext();) {
				TableRow bankGroupRow = (TableRow) bankGroupIter.next();
				if (criterionId.equals(bankGroupRow.getValue(2))) {
					Node bankGroupNode = new Node(bankGroupRow.getPrimaryKey(),
							bankGroupRow.getValue(0) + " / "
									+ bankGroupRow.getValue(3), new Integer(
									BanksConstants.NODETYPE_BANK_GROUP_NODE));
					DefaultMutableTreeNode bankGroupTreeNode = new DefaultMutableTreeNode(
							bankGroupNode);
					criterionTreeNode.add(bankGroupTreeNode);
				}
			}
		}
	}

	public void show() {
		if (isVisible())
			return;
		try {
			fina2.security.User user = (fina2.security.User) main
					.getUserHandle().getEJBObject();
			canAmend = user.hasPermission("fina2.bank.amend");
			canDelete = user.hasPermission("fina2.bank.delete");
			canReview = user.hasPermission("fina2.bank.review");
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		amendAction.setEnabled(canAmend);
		amendButton.setVisible(canAmend);
		amendItem.setVisible(canAmend);

		insertAction.setEnabled(canAmend);
		createButton.setVisible(canAmend);
		insertItem.setVisible(canAmend);

		deleteAction.setEnabled(canDelete);
		deleteButton.setVisible(canDelete);
		deleteItem.setVisible(canDelete);

		setDefaultAction.setEnabled(canAmend);
		setDefaultButton.setVisible(canAmend);
		setDefaultItem.setVisible(canAmend);

		reviewAction.setEnabled(canAmend || canReview);
		reviewButton.setVisible(canAmend || canReview);
		printButton.setVisible(canAmend || canReview);
		reviewItem.setVisible(canAmend || canReview);

		initTree();

		super.show();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		popupMenu = new javax.swing.JPopupMenu();
		amendItem = new javax.swing.JMenuItem();
		insertItem = new javax.swing.JMenuItem();
		reviewItem = new javax.swing.JMenuItem();
		deleteItem = new javax.swing.JMenuItem();
		setDefaultItem = new javax.swing.JMenuItem();
		jPanel1 = new javax.swing.JPanel();
		scrollPane = new javax.swing.JScrollPane();
		jPanel5 = new javax.swing.JPanel();
		jPanel6 = new javax.swing.JPanel();
		amendButton = new javax.swing.JButton();
		createButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();
		reviewButton = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();
		setDefaultButton = new javax.swing.JButton();

		// form popup munus
		amendItem.setFont(ui.getFont());
		amendItem.setText("Item");
		amendItem.setAction(amendAction);
		popupMenu.add(amendItem);
		insertItem.setFont(ui.getFont());
		insertItem.setText("Item");
		insertItem.setAction(insertAction);
		popupMenu.add(insertItem);
		reviewItem.setFont(ui.getFont());
		reviewItem.setAction(reviewAction);
		popupMenu.add(reviewItem);
		deleteItem.setFont(ui.getFont());
		deleteItem.setText("Item");
		deleteItem.setAction(deleteAction);
		popupMenu.add(deleteItem);
		setDefaultItem.setFont(ui.getFont());
		setDefaultItem.setText("Item");
		setDefaultItem.setAction(setDefaultAction);
		popupMenu.add(setDefaultItem);

		this.setFont(ui.getFont());

		setTitle(ui.getString("fina2.bank.bankGroupsAction"));
		initBaseComponents();

		jPanel1.setLayout(new java.awt.BorderLayout());

		jPanel1.add(scrollPane, java.awt.BorderLayout.CENTER);

		jPanel5.setLayout(new java.awt.BorderLayout());

		jPanel5.setBorder(new javax.swing.border.EmptyBorder(
				new java.awt.Insets(1, 5, 1, 5)));
		jPanel6.setLayout(new java.awt.GridBagLayout());

		amendButton.setFont(ui.getFont());
		amendButton.setText("");
		amendButton.setAction(amendAction);
		jPanel6.add(amendButton, UIManager.getGridBagConstraints(0, 1, -1, -1,
				-1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(
						5, 0, 0, 0)));

		createButton.setFont(ui.getFont());
		createButton.setAction(insertAction);
		jPanel6.add(createButton, UIManager.getGridBagConstraints(0, 0, -1, -1,
				-1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(
						5, 0, 0, 0)));

		deleteButton.setFont(ui.getFont());
		deleteButton.setText("");
		deleteButton.setAction(deleteAction);
		jPanel6.add(deleteButton, UIManager.getGridBagConstraints(0, 3, -1, -1,
				-1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(
						5, 0, 0, 0)));

		reviewButton.setFont(ui.getFont());
		reviewButton.setText("");
		reviewButton.setAction(reviewAction);
		jPanel6.add(reviewButton, UIManager.getGridBagConstraints(0, 2, -1, -1,
				-1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(
						5, 0, 0, 0)));

		setDefaultButton.setFont(ui.getFont());
		setDefaultButton.setText("");
		setDefaultButton.setAction(setDefaultAction);
		jPanel6.add(setDefaultButton, UIManager.getGridBagConstraints(0, 4, -1,
				-1, -1, -1, -1, GridBagConstraints.HORIZONTAL,
				new java.awt.Insets(5, 0, 0, 0)));

		jPanel5.add(jPanel6, java.awt.BorderLayout.NORTH);

		jPanel1.add(jPanel5, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

		jPanel2.setLayout(new java.awt.BorderLayout());

		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "Peer_Groups");
		} else {
			helpButton.setEnabled(false);
		}
		jPanel3.add(helpButton);

		jPanel2.add(jPanel3, java.awt.BorderLayout.WEST);

		jPanel4.add(printButton);

		jPanel4.add(refreshButton);

		jPanel4.add(closeButton);

		jPanel2.add(jPanel4, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

	}// GEN-END:initComponents

	protected void printButtonActionPerformed(java.awt.event.ActionEvent evt) {
		new PrintAction(getTitle());
	}

	public class PrintAction {
		private String title;
		private Spreadsheet sheet;
		private int width;
		private int height;
		private IndeterminateLoading loading;

		PrintAction(final String title) {
			loading = ui.createIndeterminateLoading(main.getMainFrame());
			Thread thread = new Thread() {
				public void run() {
					loading.start();
					sheet = SpreadsheetsManager.getInstance()
							.createSpreadsheet(title);
					print(1, 0);
					loading.stop();
				}
			};
			thread.start();
			this.title = title;
		}

		private void print(int x, int y) {
			ArrayList<ArrayList<String>> rows = getData();
			Object[][] data = getDataArray(rows);

			// /init Sheet
			sheet.setFontName(x, y, x + height, y + width, ui.getFont()
					.getName());
			sheet.setFontSize(x, y, x + 1, y + 1, ui.getFont().getSize() + 2);
			sheet.setFontWeight(x, y, x + 1, y + 1, Spreadsheet.BOLD);

			sheet.setFontName(x + 2, y, x + 2, y + 4, ui.getFont().getName());
			sheet.setFontSize(x + 2, y, x + 2, y + 4, ui.getFont().getSize());
			sheet.setFontWeight(x + 2, y, x + 2, y + 4, Spreadsheet.BOLD);

			sheet.setFontSize(x + 2, y, height + y, width + x, ui.getFont()
					.getSize() - 2);
			sheet.setBorder(x + 2, y, height + x - 1, width + y - 1,
					Spreadsheet.LINE_YES, Spreadsheet.LINE_YES,
					Spreadsheet.LINE_YES, Spreadsheet.LINE_YES,
					Spreadsheet.LINE_YES, Spreadsheet.LINE_YES, (short) 1);

			sheet.setDataArray(x, y, height + x - 1, width + y - 1, data);

			sheet.setHorizontalAlign(2, y, height + y, y, Spreadsheet.CENTER);

			sheet.setOptimalColWidth(y, 100);
			sheet.showGrid(false);
			sheet.setViewMode(Spreadsheet.VIEW_FULL);
		}

		private Object[][] getDataArray(ArrayList<ArrayList<String>> rows) {
			height = rows.size() + 2;
			width = rows.get(0).size() + 1;
			Object[][] tmp = new Object[height][width];
			tmp[0][1] = title;
			for (int i = 0; i < height - 2; i++) {
				ArrayList<String> al = rows.get(i);
				for (int j = 0; j < width - 1; j++) {
					tmp[i + 2][j + 1] = al.get(j);
					tmp[i + 2][0] = i;
				}
			}
			tmp[2][0] = "#";
			return tmp;
		}

		@SuppressWarnings("unchecked")
		private ArrayList<ArrayList<String>> getData() {

			Enumeration<DefaultMutableTreeNode> en = root
					.breadthFirstEnumeration();

			ArrayList<String> al = null;
			ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();

			// set column Title
			al = new ArrayList<String>();
			al.add(ui.getString("fina2.bank.criterions") + " "
					+ ui.getString("fina2.code"));
			al.add(ui.getString("fina2.bank.criterions") + " "
					+ ui.getString("fina2.description"));
			al.add(ui.getString("fina2.license.group") + " "
					+ ui.getString("fina2.code"));
			al.add(ui.getString("fina2.license.group") + " "
					+ ui.getString("fina2.description"));
			rows.add(al);

			int count = 0;
			do {
				DefaultMutableTreeNode current = en.nextElement();
				count = current.getChildCount();

				for (int i = 0; i < count; i++) {
					TreeNode rootNode = current.getChildAt(i);

					Enumeration<TreeNode> nodes = rootNode.children();

					while (nodes.hasMoreElements()) {
						al = new ArrayList<String>();

						String group = rootNode.toString();
						StringTokenizer tokenGroup = new StringTokenizer(group,
								"/");
						while (tokenGroup.hasMoreElements()) {
							al.add(tokenGroup.nextToken());
						}

						TreeNode node = nodes.nextElement();
						String location = node.toString();
						StringTokenizer tokenLocation = new StringTokenizer(
								location, "/");
						while (tokenLocation.hasMoreElements()) {
							al.add(tokenLocation.nextToken());
						}

						rows.add(al);
					}
				}
			} while (en.hasMoreElements() && count == 0);

			return rows;
		}

	}

	protected void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {
		initTree();
	}

	protected void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		formComponentHidden(null);
		dispose();
	}

	protected void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {

	}

	/** Exit the Application */
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPopupMenu popupMenu;
	private javax.swing.JMenuItem amendItem;
	private javax.swing.JMenuItem insertItem;
	private javax.swing.JMenuItem reviewItem;
	private javax.swing.JMenuItem deleteItem;
	private javax.swing.JMenuItem setDefaultItem;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JButton amendButton;
	private javax.swing.JButton createButton;
	private javax.swing.JButton deleteButton;
	private javax.swing.JButton reviewButton;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JButton setDefaultButton;
	// End of variables declaration//GEN-END:variables

}

class BankGroupsAmendAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private BankGroupAmendDialog dialog;
	private java.awt.Frame parent;
	private EJBTree tree;

	BankGroupsAmendAction(java.awt.Frame parent, EJBTree tree) {
		super();

		dialog = new BankGroupAmendDialog(parent, true);

		ui.loadIcon("fina2.amend", "amend.gif");

		this.parent = parent;
		this.tree = tree;

		putValue(AbstractAction.NAME, ui.getString("fina2.amend"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.amend"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		dialog.show(tree.getSelectedNode(), true, false);
		((javax.swing.tree.DefaultTreeModel) tree.getModel()).nodeChanged(tree
				.getSelectedTreeNode());
	}

}

class BankGroupsInsertAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private BankGroupAmendDialog dialog;
	private java.awt.Frame parent;
	private EJBTree tree;

	BankGroupsInsertAction(java.awt.Frame parent, EJBTree tree) {
		super();

		dialog = new BankGroupAmendDialog(parent, true);

		ui.loadIcon("fina2.insert", "insert.gif");

		this.parent = parent;
		this.tree = tree;

		putValue(AbstractAction.NAME, ui.getString("fina2.create"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.insert"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		Node node = tree.getSelectedNode();
		DefaultMutableTreeNode treeNode = tree.getSelectedTreeNode();
		dialog.show(node, true, true);
		if (dialog.getNode() != node) {
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
					dialog.getNode());
			((javax.swing.tree.DefaultTreeModel) tree.getModel())
					.insertNodeInto(newNode, treeNode, treeNode.getChildCount());
			TreePath path = new TreePath(newNode.getPath());
			tree.setExpandsSelectedPaths(true);
			tree.setSelectionPath(path);
			((javax.swing.tree.DefaultTreeModel) tree.getModel())
					.nodeChanged(newNode);
		}
	}

}

class BankGroupsReviewAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private BankGroupAmendDialog dialog;
	private java.awt.Frame parent;
	private EJBTree tree;

	BankGroupsReviewAction(java.awt.Frame parent, EJBTree tree) {
		super();

		dialog = new BankGroupAmendDialog(parent, true);
		ui.loadIcon("fina2.review", "review.gif");

		this.parent = parent;
		this.tree = tree;

		putValue(AbstractAction.NAME, ui.getString("fina2.review"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.review"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		dialog.show(tree.getSelectedNode(), false, false);
	}

}

class BankGroupsDeleteAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private java.awt.Frame parent;
	private EJBTree tree;

	// private BankGroup bankGroup;

	BankGroupsDeleteAction(java.awt.Frame parent, EJBTree tree) {
		super();

		ui.loadIcon("fina2.delete", "delete.gif");

		this.parent = parent;
		this.tree = tree;

		putValue(AbstractAction.NAME, ui.getString("fina2.delete"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.delete"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		if (!ui.showConfirmBox(parent,
				ui.getString("fina2.bank.bankCriterionDeleteQuestion")))
			return;

		try {

			switch (((Integer) tree.getSelectedNode().getType()).intValue()) {

			case BanksConstants.NODETYPE_BANK_CRITERION_NODE:
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/bank/BankCriterion");
				BankCriterionHome criterionHome = (BankCriterionHome) PortableRemoteObject
						.narrow(ref, BankCriterionHome.class);
				BankCriterionPK BankCriterionPK = (BankCriterionPK) tree
						.getSelectedNode().getPrimaryKey();
				BankCriterion bankCriterion = criterionHome
						.findByPrimaryKey(BankCriterionPK);
				try {
					bankCriterion.remove();
					((javax.swing.tree.DefaultTreeModel) tree.getModel())
							.removeNodeFromParent(tree.getSelectedTreeNode());
				} catch (Exception ex) {
					Main.errorHandler(parent, Main.getString("fina2.title"),
							Main.getString("fina2.delete.bankCriterion"));
				}
				break;

			case BanksConstants.NODETYPE_BANK_GROUP_NODE:
				jndi = fina2.Main.getJndiContext();
				ref = jndi.lookup("fina2/bank/BankGroup");
				BankGroupHome bankGroupHome = (BankGroupHome) PortableRemoteObject
						.narrow(ref, BankGroupHome.class);
				BankGroupPK bankGroupPK = (BankGroupPK) tree.getSelectedNode()
						.getPrimaryKey();
				BankGroup bankGroup = bankGroupHome
						.findByPrimaryKey(bankGroupPK);
				try {
					bankGroup.remove();
					((javax.swing.tree.DefaultTreeModel) tree.getModel())
							.removeNodeFromParent(tree.getSelectedTreeNode());
				} catch (Exception ex) {
					Main.errorHandler(parent, Main.getString("fina2.title"),
							Main.getString("fina2.delete.bankGroup"));
				}
				break;
			}

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

	}

}

class BankGroupsSetDefaultAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private java.awt.Frame parent;
	private EJBTree tree;

	BankGroupsSetDefaultAction(java.awt.Frame parent, EJBTree tree) {
		super();

		ui.loadIcon("fina2.default", "default.gif");

		this.parent = parent;
		this.tree = tree;

		putValue(AbstractAction.NAME, ui.getString("fina2.default"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.default"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		TreePath path = tree.getSelectionPath();
		if (path != null && path.getPathCount() == 2) {
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path
					.getLastPathComponent();
			Node node = (Node) ((DefaultMutableTreeNode) treeNode)
					.getUserObject();

			InitialContext jndi = null;
			Object ref = null;
			try {
				jndi = fina2.Main.getJndiContext();
				ref = jndi.lookup("fina2/bank/BankSession");
				BankSessionHome sesHome = (BankSessionHome) PortableRemoteObject
						.narrow(ref, BankSessionHome.class);

				BankSession session = sesHome.create();

				Collection notAssignedBanks = session.getNotAssignedBankRows(
						main.getUserHandle(), main.getLanguageHandle(),
						(BankCriterionPK) node.getPrimaryKey());

				if (notAssignedBanks == null || notAssignedBanks.size() == 0) {

					Enumeration enu = treeNode.getParent().children();
					while (enu.hasMoreElements()) {
						DefaultMutableTreeNode n = (DefaultMutableTreeNode) enu
								.nextElement();
						Node nn = (Node) n.getUserObject();
						nn.setDefaultNode(false);
						nn.setType(new Integer(
								BanksConstants.NODETYPE_BANK_CRITERION_NODE));
					}

					jndi = fina2.Main.getJndiContext();
					ref = jndi.lookup("fina2/bank/BankCriterion");
					BankCriterionHome home = (BankCriterionHome) PortableRemoteObject
							.narrow(ref, BankCriterionHome.class);

					BankCriterion bankCriterion = home
							.findByPrimaryKey((BankCriterionPK) node
									.getPrimaryKey());
					bankCriterion.setDefault(true);

					node.setDefaultNode(true);
					node.setType(new Integer(
							BanksConstants.NODETYPE_DEF_BANK_CRITERION_NODE));
					((DefaultTreeModel) tree.getModel()).nodeChanged(treeNode);
				} else {
					StringBuffer message = new StringBuffer();
					for (Iterator iter = notAssignedBanks.iterator(); iter
							.hasNext();) {
						TableRow tableRow = (TableRow) iter.next();
						message.append(tableRow.getValue(1)).append('\n');
					}
					ScrollableMessageBox mBox = new ScrollableMessageBox(
							parent, ui.getString("fina2.title"), true,
							ui.getString("fina2.bank.cantChangeCriterion")
									+ "\n", message.toString());
					mBox.setLocationRelativeTo(parent);
					mBox.show();
				}
			} catch (FinaTypeException e) {
				Main.generalErrorHandler(e);
			} catch (Exception e) {
				Main.errorHandler(parent, Main.getString("fina2.title"), Main
						.getString("fina2.bank.cantSetDefaultBankCriterion"));
			}
		}
	}
}
