/*
 * BankDTAmendFrame.java
 *
 * Created on April 17, 2002, 5:20 PM
 */

package fina2.bank;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.StyledEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import fina2.BaseFrame;
import fina2.Main;
import fina2.regions.RegionStructureSession;
import fina2.regions.RegionStructureSessionHome;
import fina2.security.UserSession;
import fina2.security.UserSessionHome;
import fina2.servergate.FIGate;
import fina2.ui.UIManager;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableReviewFrame;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;
import fina2.ui.tree.EJBTree;
import fina2.ui.tree.Node;
import fina2.util.Comparators;

public class BanksFrame extends BaseFrame {

	Set<String> suggestionSet = new TreeSet<String>();

	private boolean hide_flag = false;

	/**
	 * Handler for selection item of bank tree
	 * 
	 * @author Askhat Asanaliev, May 2007
	 * 
	 */
	private class BankTreeSelectionListener implements TreeSelectionListener {

		// Called whenever the value of the selection changes.
		public void valueChanged(TreeSelectionEvent evt) {

			if (tree.getSelectedTreeNode() == null)
				return;
			/* By default disabling all buttons */
			enableAllButtons(false);
			boolean userCanAmend = getUserAccess();
			Node selectedNode = tree.getSelectedNode();

			if (selectedNode == null) {
				// No node is selected. Nothing to do.
				return;
			}

			int type = ((Integer) selectedNode.getType()).intValue();

			switch (type) {

			case BanksConstants.NODETYPE_NODE:
				createBankAction.setEnabled(canAmend);
				break;

			case BanksConstants.NODETYPE_BANK:
				deleteAction.setEnabled(userCanAmend && canDelete);
				amendAction.setEnabled(userCanAmend && canAmend);
				reviewAction.setEnabled(true);
				break;

			case BanksConstants.NODETYPE_LICENSE_NODE:
				createLicenseAction.setEnabled(userCanAmend && canAmend);
				break;

			case BanksConstants.NODETYPE_LICENSE:
				deleteAction.setEnabled(userCanAmend && canAmend);
				amendAction.setEnabled(userCanAmend && canAmend);
				reviewAction.setEnabled(true);
				break;

			case BanksConstants.NODETYPE_MANAGEMENT_NODE:
				createManagementAction.setEnabled(canAmend);
				break;
			case BanksConstants.NODETYPE_MANAGEMENT:
				deleteAction.setEnabled(userCanAmend && canAmend);
				amendAction.setEnabled(userCanAmend && canAmend);
				reviewAction.setEnabled(true);
				break;

			case BanksConstants.NODETYPE_HISTORY_NODE:
				createHistoryAction.setEnabled(userCanAmend && canAmend);
				break;

			case BanksConstants.NODETYPE_BRANCH_NODE:
				createBranchAction.setEnabled(userCanAmend && canAmend);
				break;

			case BanksConstants.NODETYPE_BRANCH:
				createBranchManagementAction.setEnabled(userCanAmend && canAmend);
				deleteAction.setEnabled(userCanAmend && canAmend);
				amendAction.setEnabled(userCanAmend && canAmend);
				reviewAction.setEnabled(true);
				break;

			case BanksConstants.NODETYPE_BRANCH_MANAGEMENT:
				deleteAction.setEnabled(userCanAmend && canAmend);
				amendAction.setEnabled(userCanAmend && canAmend);
				reviewAction.setEnabled(true);
				break;

			case BanksConstants.NODETYPE_BANK_GROUP_NODE:
				amendAction.setEnabled(userCanAmend && canAmend);
				reviewAction.setEnabled(true);
				break;

			case BanksConstants.NODETYPE_BANK_GROUP:
				amendAction.setEnabled(canAmend);
				reviewAction.setEnabled(true);
				break;

			case BanksConstants.NODETYPE_DEF_BANK_GROUP_NODE:
				amendAction.setEnabled(userCanAmend && canAmend);
				reviewAction.setEnabled(true);
				break;

			case BanksConstants.NODETYPE_BANK_TYPE:

				/* Bank type node selected */
				createBankAction.setEnabled(true);
				break;
			}

		}

		/** Enables/disables all buttons of BanksFrame */
		void enableAllButtons(boolean enable) {
			createBankAction.setEnabled(enable);
			createLicenseAction.setEnabled(enable);
			createManagementAction.setEnabled(enable);
			createBranchAction.setEnabled(enable);
			createBranchManagementAction.setEnabled(enable);
			createHistoryAction.setEnabled(enable);
			deleteAction.setEnabled(enable);
			amendAction.setEnabled(enable);
			reviewAction.setEnabled(enable);
		}
	}

	/* ===================================================================== */
	/* BanksFrame */

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTree tree;

	private boolean canAmend = false;
	private boolean canDelete = false;
	private boolean canReview = false;

	private String sync = "sync";

	private CreateBankAction createBankAction;
	private CreateLicenseAction createLicenseAction;
	private CreateManagementAction createManagementAction;
	private CreateBranchAction createBranchAction;
	private CreateBranchManagementAction createBranchManagementAction;
	private CreateHistoryAction createHistoryAction;
	private AmendAction amendAction;
	private DeleteAction deleteAction;
	private ReviewAction reviewAction;
	private PrintAction printAction;
	private InitialContext jndi = null;
	private Object ref = null;
	private BankSessionHome home = null;
	private BankSession bankSession = null;
	// private Hashtable amendBanks;
	// private Hashtable reviewBanks;
	BankAccess bankAccess;

	private void initSuggestion() {
		try {
			jndi = fina2.Main.getJndiContext();
			ref = jndi.lookup("fina2/bank/BankSession");
			home = (BankSessionHome) PortableRemoteObject.narrow(ref, BankSessionHome.class);

			bankSession = home.create();

			ArrayList<TableRowImpl> c = (ArrayList<TableRowImpl>) bankSession.getBanks(main.getUserHandle(), main.getLanguageHandle());

			for (int i = 0; i < c.size(); i++) {
				suggestionSet.add(createSuggestionString(c.get(i)));
			}
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
	}

	public String createSuggestionString(TableRowImpl row) {
		String desc = "";
		if (row.getValue(4).length() > 10)
			desc = row.getValue(4).substring(0, 10) + "...";
		else
			desc = row.getValue(4);
		return "[" + row.getValue(3).toString() + "] " + desc;
	}

	/*
	 * private DisableAction disableAction; private UpAction upAction; private
	 * DownAction downAction;
	 */
	/** Creates new form BankDTAmendFrame */
	public BanksFrame() {
		setFont(ui.getFont());
		ui.loadIcon("fina2.node", "node.gif");
		ui.loadIcon("fina2.node.default", "node_def.gif");

		setModel(new DefaultComboBoxModel(suggestionSet.toArray()), "");

		tree = new EJBTree();
		tree.setFont(ui.getFont());
		bankAccess = new BankAccess();

		// amendBanks=new Hashtable();
		// reviewBanks=new Hashtable();

		/* Action for bank creation */
		createBankAction = new CreateBankAction(this);

		createLicenseAction = new CreateLicenseAction(main.getMainFrame(), tree);
		createManagementAction = new CreateManagementAction(main.getMainFrame(), tree);
		createBranchAction = new CreateBranchAction(main.getMainFrame(), tree);
		createBranchManagementAction = new CreateBranchManagementAction(main.getMainFrame(), tree);
		createHistoryAction = new CreateHistoryAction(main.getMainFrame(), tree);
		deleteAction = new DeleteAction(main.getMainFrame(), tree, bankAccess, this);
		amendAction = new AmendAction(main.getMainFrame(), tree, this);
		reviewAction = new ReviewAction(main.getMainFrame(), tree);
		printAction = new PrintAction(main.getMainFrame());
		/*
		 * disableAction = new DisableAction(this, tree);
		 */

		tree.addTreeExpansionListener(new TreeExpansionListener() {
			DefaultMutableTreeNode node;

			public void treeCollapsed(TreeExpansionEvent evt) {
			}

			public synchronized void treeExpanded(TreeExpansionEvent evt) {
				node = (DefaultMutableTreeNode) evt.getPath().getLastPathComponent();
				Thread thread = new Thread() {
					public void run() {
						synchronized (sync) {
							DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getFirstChild();
							int id = 0;
							// int id =
							// ((BankPK)((Node)child.getUserObject()).
							// getPrimaryKey()).getId();
							Object pk = ((Node) child.getUserObject()).getPrimaryKey();
							if (pk instanceof BankPK) {
								id = ((BankPK) pk).getId();
							}
							if (pk instanceof BankManagPK) {
								id = ((BankManagPK) pk).getId();
							}
							if (pk instanceof LicencePK) {
								id = ((LicencePK) pk).getId();
							}
							if (pk instanceof BranchPK) {
								id = ((BranchPK) pk).getId();
							}
							if (pk instanceof BranchManagPK) {
								id = ((BranchManagPK) pk).getId();
							}
							if (pk instanceof BankGroupPK) {
								id = ((BankGroupPK) pk).getId();
							}
							if (id >= 0)
								return;
							try {

								Collection nodes = new Vector();
								switch (-id) {
								case BanksConstants.NODETYPE_BANK:
									nodes = getInfoNodes((BankPK) ((Node) node.getUserObject()).getPrimaryKey());
									break;
								case BanksConstants.NODETYPE_LICENSE_NODE:
									nodes = bankSession.getLicencesNodes(main.getUserHandle(), main.getLanguageHandle(), (BankPK) getBankPK(node));
									break;
								case BanksConstants.NODETYPE_BRANCH_NODE:
									nodes = bankSession.getBranchNodes(main.getUserHandle(), main.getLanguageHandle(), (BankPK) getBankPK(node));
									break;
								case BanksConstants.NODETYPE_MANAGEMENT_NODE:
									nodes = bankSession.getBankManagNodes(main.getUserHandle(), main.getLanguageHandle(), (BankPK) getBankPK(node));
									break;
								case BanksConstants.NODETYPE_BRANCH:
									nodes = bankSession.getBranchManagNodes(main.getUserHandle(), main.getLanguageHandle(), (BranchPK) ((Node) node.getUserObject()).getPrimaryKey());
									break;
								case BanksConstants.NODETYPE_BANK_GROUP:
									nodes = bankSession.getBankGroupNodes(main.getUserHandle(), main.getLanguageHandle(), (BankPK) getBankPK(node));
									break;
								}

								loadBankList((javax.swing.tree.DefaultTreeModel) tree.getModel(), node, nodes);
								((javax.swing.tree.DefaultTreeModel) tree.getModel()).removeNodeFromParent(child);

							} catch (Exception e) {
								Main.generalErrorHandler(e);
							}
						}
					}
				};
				thread.start();
			}
		});

		/* Adding selection listener for bank tree */
		tree.addTreeSelectionListener(new BankTreeSelectionListener());

		initComponents();
		scrollPane.setViewportView(tree);
		BaseFrame.ensureVisible(this);

	}

	private BankPK getBankPK(DefaultMutableTreeNode node) {
		BankPK pk = null;
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
		int id = ((BankPK) ((Node) parent.getUserObject()).getPrimaryKey()).getId();
		if (id < 0)
			pk = getBankPK(parent);
		return new BankPK(id);
	}

	private BankPK getBankPKNodeLevel4(DefaultMutableTreeNode node) {
		BankPK pk = null;
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
		parent = (DefaultMutableTreeNode) parent.getParent();
		int id = ((BankPK) ((Node) parent.getUserObject()).getPrimaryKey()).getId();
		if (id < 0)
			pk = getBankPK(parent);
		return new BankPK(id);
	}

	public void show() {

		if (isVisible())
			return;

		try {
			fina2.security.User user = (fina2.security.User) main.getUserHandle().getEJBObject();
			canAmend = user.hasPermission("fina2.bank.amend");
			canDelete = user.hasPermission("fina2.bank.delete");
			canReview = user.hasPermission("fina2.bank.review");
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

		createBankAction.setEnabled(canAmend);
		createBankButton.setVisible(canAmend);
		// insertItem.setVisible(canAmend);

		createLicenseAction.setEnabled(canAmend);
		createLicenceButton.setVisible(canAmend);
		// insertItem.setVisible(canAmend);

		createManagementAction.setEnabled(canAmend);
		createManagementButton.setVisible(canAmend);
		// insertItem.setVisible(canAmend);

		createBranchAction.setEnabled(canAmend);
		createBranchButton.setVisible(canAmend);
		// insertItem.setVisible(canAmend);

		createBranchManagementAction.setEnabled(canAmend);
		createBranchManagementButton.setVisible(canAmend);
		// insertItem.setVisible(canAmend);

		createHistoryAction.setEnabled(canAmend);
		createHistoryButton.setVisible(canAmend);
		// insertItem.setVisible(canAmend);

		amendAction.setEnabled(canAmend);
		amendButton.setVisible(canAmend);
		// amendItem.setVisible(canAmend);

		deleteAction.setEnabled(canDelete);
		// deleteButton.setVisible(canDelete);
		// deleteItem.setVisible(canDelete);

		reviewAction.setEnabled(canAmend || canReview);
		reviewButton.setVisible(canAmend || canReview);
		printButton.setVisible(canAmend || canReview);
		// reviewItem.setVisible(canAmend || canReview);

		initTree();

		super.show();

	}

	/** Loads the bank list */
	synchronized void loadBankList(DefaultTreeModel model, final DefaultMutableTreeNode parent, Collection bankList) {

		for (Iterator iter = bankList.iterator(); iter.hasNext();) {

			/* The nodes are added to this parent */
			DefaultMutableTreeNode currentParent = parent;

			Node bankNode = (Node) iter.next();

			int pk = 0;
			if (bankNode.getPrimaryKey() instanceof BankPK) {
				/* Bank node */

				/* Getting its PK */
				pk = ((BankPK) bankNode.getPrimaryKey()).getId();

				/* Getting bank type node. */
				DefaultMutableTreeNode bankTypeNode = getBankTypeNodeFor(parent, bankNode);
				if (bankTypeNode != null) {
					currentParent = bankTypeNode;
				}

			} else {
				/* Bank property node. Getting its bank PK. */
				pk = ((BankPK) getBankPK(parent)).getId();
			}

			if (bankAccess.getReviewSize() > 0 && bankAccess.getReviewKey(pk)) {

				/* New tree node from bank node */
				DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(bankNode);

				/* Adding the new node to the tree */
				model.insertNodeInto(treeNode, currentParent, currentParent.getChildCount());

				/* Node type. (See BanksConstants class) */
				int typeValue = ((Integer) bankNode.getType()).intValue();
				BankPK bankPK = new BankPK(-typeValue);
				Node propertyNode = new Node(bankPK, ui.getString("fina2.metadata.loading"), new Integer(-1));
				model.insertNodeInto(new DefaultMutableTreeNode(propertyNode), treeNode, 0);
			}
		}
	}

	/** Returns the bank type node for given bank node */
	private DefaultMutableTreeNode getBankTypeNodeFor(DefaultMutableTreeNode rootNode, Node bankNode) {

		Integer bankTypeId = (Integer) bankNode.getProperty("bankType");
		DefaultMutableTreeNode bankTypeNode = tree.findTreeNode(rootNode, bankTypeId);

		return bankTypeNode;
	}

	/** Loads bank type list */
	private void loadBankTypes(DefaultTreeModel model, DefaultMutableTreeNode rootNode) {
		List<Node> bankTypes = null;
		try {
			bankTypes = FIGate.getBankTypes();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
			return;
		}
		for (Node node : bankTypes) {
			/* New tree node from bank node */
			DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node);

			/* Adding the new node to the tree */
			model.insertNodeInto(treeNode, rootNode, rootNode.getChildCount());
		}

	}

	@SuppressWarnings("unchecked")
	synchronized void updateBankGroupNodes(DefaultMutableTreeNode node) {
		javax.swing.tree.DefaultTreeModel model = (javax.swing.tree.DefaultTreeModel) tree.getModel();

		node.removeAllChildren();
		Enumeration enu = node.children();
		while (enu.hasMoreElements()) {
			DefaultMutableTreeNode dn = (DefaultMutableTreeNode) enu.nextElement();
			model.removeNodeFromParent(dn);
		}

		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/bank/BankSession");
			BankSessionHome home = (BankSessionHome) PortableRemoteObject.narrow(ref, BankSessionHome.class);

			BankSession session = home.create();
			Collection nodes = session.getBankGroupNodes(main.getUserHandle(), main.getLanguageHandle(), (BankPK) getBankPK(node));

			for (Iterator nodeIter = nodes.iterator(); nodeIter.hasNext();) {
				Node n = (Node) nodeIter.next();
				model.insertNodeInto(new DefaultMutableTreeNode(n), node, node.getChildCount());
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

		model.nodeStructureChanged(node);

	}

	/** Inits the banks tree */
	public void initTree() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/bank/BankSession");
			BankSessionHome home = (BankSessionHome) PortableRemoteObject.narrow(ref, BankSessionHome.class);

			BankSession session = home.create();

			Node rootNode = new Node(new BankPK(0), ui.getString("fina2.bank.bankList"), new Integer(0));
			setFont(ui.getFont());
			tree.initTree(rootNode);

			DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootNode);
			Collection nodes = session.getBankListNodes(main.getUserHandle(), main.getLanguageHandle());

			ref = jndi.lookup("fina2/security/UserSession");
			UserSessionHome homeU = (UserSessionHome) PortableRemoteObject.narrow(ref, UserSessionHome.class);

			UserSession sessionU = homeU.create();

			bankAccess.initReviewBanks(sessionU.getUserCanReviewBanks((fina2.security.UserPK) ((fina2.security.User) main.getUserHandle().getEJBObject()).getPrimaryKey()));
			bankAccess.initAmendBanks(sessionU.getUserCanAmendBanks((fina2.security.UserPK) ((fina2.security.User) main.getUserHandle().getEJBObject()).getPrimaryKey()));

			/* ============================================================== */
			/* Loading data of tree */

			DefaultTreeModel model = (javax.swing.tree.DefaultTreeModel) tree.getModel();
			model.setRoot(root);

			((javax.swing.tree.DefaultTreeModel) tree.getModel()).setRoot(root);

			/* Loading bank type list */
			loadBankTypes(model, root);

			/* Loading bank list */
			loadBankList(model, root, nodes);

			/* ============================================================== */
			/* Setting tree visual behaviour */

			tree.addIcon(BanksConstants.NODETYPE_ROOT, ui.getIcon("fina2.node"));
			tree.addIcon(BanksConstants.NODETYPE_BANK_GROUP, ui.getIcon("fina2.node"));
			tree.addIcon(BanksConstants.NODETYPE_DEF_BANK_GROUP_NODE, ui.getIcon("fina2.node.default"));
			tree.addIcon(BanksConstants.NODETYPE_BANK_GROUP_NODE, ui.getIcon("fina2.node"));
			tree.addIcon(BanksConstants.NODETYPE_BANK, ui.getIcon("fina2.node"));
			tree.addIcon(BanksConstants.NODETYPE_LICENSE, ui.getIcon("fina2.node"));
			tree.addIcon(BanksConstants.NODETYPE_MANAGEMENT, ui.getIcon("fina2.node"));
			tree.addIcon(BanksConstants.NODETYPE_BRANCH_NODE, ui.getIcon("fina2.node"));
			tree.addIcon(BanksConstants.NODETYPE_BRANCH, ui.getIcon("fina2.node"));
			tree.addIcon(BanksConstants.NODETYPE_BRANCH_MANAGEMENT, ui.getIcon("fina2.node"));
			tree.addIcon(BanksConstants.NODETYPE_HISTORY, ui.getIcon("fina2.node"));
			tree.addIcon(BanksConstants.NODETYPE_BRANCH_NODE, ui.getIcon("fina2.node"));
			tree.addIcon(BanksConstants.NODETYPE_LICENSE_NODE, ui.getIcon("fina2.node"));
			tree.addIcon(BanksConstants.NODETYPE_MANAGEMENT_NODE, ui.getIcon("fina2.node"));
			tree.addIcon(BanksConstants.NODETYPE_HISTORY_NODE, ui.getIcon("fina2.node"));
			tree.addIcon(BanksConstants.NODETYPE_BANK_TYPE, ui.getIcon("fina2.node"));

			initSuggestion();

			tree.setRootVisible(true);
			tree.setSelectionRow(0);
			tree.setFont(ui.getFont());
			tree.expandRow(0);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
			ui.putConfigValue("fina2.bank.BanksFrame.visible", new Boolean(false));
		}
	}

	@SuppressWarnings("unchecked")
	private Collection getInfoNodes(BankPK pk) {
		Vector nodes = new Vector();
		Node node = null;
		node = new Node(pk, ui.getString("fina2.bank.bankGroups"), new Integer(BanksConstants.NODETYPE_BANK_GROUP));
		nodes.add(node);
		node = new Node(pk, ui.getString("fina2.bank.licences"), new Integer(BanksConstants.NODETYPE_LICENSE_NODE));
		nodes.add(node);
		node = new Node(pk, ui.getString("fina2.bank.management"), new Integer(BanksConstants.NODETYPE_MANAGEMENT_NODE));
		nodes.add(node);
		node = new Node(pk, ui.getString("fina2.bank.history"), new Integer(BanksConstants.NODETYPE_HISTORY_NODE));
		nodes.add(node);
		node = new Node(pk, ui.getString("fina2.bank.branches"), new Integer(BanksConstants.NODETYPE_BRANCH_NODE));
		nodes.add(node);
		return nodes;
	}

	private boolean getUserAccess() {
		int pk;
		Object pko = ((Node) (tree.getSelectedTreeNode()).getUserObject()).getPrimaryKey();
		if (pko instanceof BranchManagPK) {
			pk = ((BankPK) getBankPKNodeLevel4(tree.getSelectedTreeNode())).getId();
		} else if (pko instanceof BankPK) {
			pk = ((BankPK) pko).getId();
		} else {
			pk = ((BankPK) getBankPK(tree.getSelectedTreeNode())).getId();
		}

		if (bankAccess.getAmendKey(pk)) {
			return true;
		} else {
			return false;
		}
	}

	/** Returns current selected node */
	Node getSelectedNode() {
		return tree.getSelectedNode();
	}

	public void setModel(DefaultComboBoxModel mdl, String str) {
		searchCombo.setModel(mdl);
		searchCombo.setFont(ui.getFont());
		searchCombo.setSelectedIndex(-1);

		searchText.setFont(ui.getFont());
		searchText.setText(str);
	}

	private DefaultComboBoxModel getSuggestedModel(Set<String> list, String text) {
		DefaultComboBoxModel m = new DefaultComboBoxModel();
		for (String s : list) {
			if (s.toLowerCase().trim().contains(text))
				m.addElement(s);
		}
		return m;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() { // GEN-BEGIN:initComponents
		setFont(ui.getFont());
		scrollPane = new javax.swing.JScrollPane();
		jPanel1 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		createBankButton = new javax.swing.JButton();
		createLicenceButton = new javax.swing.JButton();
		createManagementButton = new javax.swing.JButton();
		createBranchButton = new javax.swing.JButton();
		createBranchManagementButton = new javax.swing.JButton();
		createHistoryButton = new javax.swing.JButton();
		amendButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();
		reviewButton = new javax.swing.JButton();
		searchLabel = new JLabel("Search");

		searchCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox b = (JComboBox) e.getSource();
				Object item = b.getSelectedItem();
				try {
					if (item != null) {
						StringTokenizer st = new StringTokenizer(item.toString().substring(1), "]");
						Integer id = null;
						try {
							id = bankSession.getBankId(st.nextToken());
						} catch (NoSuchElementException ex) {
							// TODO no parsing Text.
						}
						if (id != null)
							tree.gotoNode(new BankPK(id));
						else if (suggestionSet != null)
							suggestionSet.remove(item);
					}
				} catch (Exception ex) {
					Main.generalErrorHandler(ex);
				}
			}
		});
		searchText = (JTextField) searchCombo.getEditor().getEditorComponent();
		searchText.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent ke) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {

						String text = searchText.getText();
						if (text.trim().length() == 0) {
							searchCombo.hidePopup();
							setModel(new DefaultComboBoxModel(suggestionSet.toArray()), "");
						} else {
							DefaultComboBoxModel m = getSuggestedModel(suggestionSet, text);
							if (m.getSize() == 0 || hide_flag) {
								searchCombo.hidePopup();
								hide_flag = false;
							} else {
								setModel(m, text);
								searchCombo.showPopup();
							}
						}

					}

				});
			}

			public void keyPressed(KeyEvent e) {
				String text = searchText.getText();
				int code = e.getKeyCode();
				if (code == KeyEvent.VK_ENTER) {
					if (suggestionSet != null) {
						suggestionSet.add(text);
						// Collections.sort(v);
						setModel(getSuggestedModel(suggestionSet, text), text);
					}
					hide_flag = true;
				} else if (code == KeyEvent.VK_ESCAPE) {
					hide_flag = true;
				} else if (code == KeyEvent.VK_RIGHT) {
					for (String str : suggestionSet) {
						if (str.startsWith(text)) {
							searchCombo.setSelectedIndex(-1);
							searchText.setText(str);
							return;
						}
					}
				}
			}
		});

		jPanel2 = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();
		jPanel5 = new javax.swing.JPanel();
		printButton = new javax.swing.JButton();

		setTitle(ui.getString("fina2.bank.bankList"));
		initBaseComponents();

		getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

		jPanel1.setLayout(new java.awt.BorderLayout());

		jPanel3.setLayout(new java.awt.GridBagLayout());

		createBankButton.setFont(ui.getFont());
		createBankButton.setAction(createBankAction);
		jPanel3.add(createBankButton, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 0)));

		createLicenceButton.setFont(ui.getFont());
		createLicenceButton.setText(ui.getString("fina2.bank.newLicense"));
		createLicenceButton.setAction(createLicenseAction);
		jPanel3.add(createLicenceButton, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 0)));

		createManagementButton.setFont(ui.getFont());
		createManagementButton.setText(ui.getString("fina2.bank.newManagement"));
		createManagementButton.setAction(createManagementAction);
		jPanel3.add(createManagementButton, UIManager.getGridBagConstraints(0, 2, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 0)));

		createBranchButton.setFont(ui.getFont());
		createBranchButton.setAction(createBranchAction);
		jPanel3.add(createBranchButton, UIManager.getGridBagConstraints(0, 3, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 0)));

		createBranchManagementButton.setFont(ui.getFont());
		createBranchManagementButton.setText(ui.getString("fina2.bank.newBranchManagement"));
		createBranchManagementButton.setAction(createBranchManagementAction);
		jPanel3.add(createBranchManagementButton, UIManager.getGridBagConstraints(0, 4, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 0)));

		createHistoryButton.setFont(ui.getFont());
		createHistoryButton.setAction(createHistoryAction);
		jPanel3.add(createHistoryButton, UIManager.getGridBagConstraints(0, 5, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 0)));

		amendButton.setIcon(ui.getIcon("fina2.amend"));
		amendButton.setFont(ui.getFont());
		amendButton.setText(ui.getString("fina2.amend"));
		amendButton.setAction(amendAction);
		jPanel3.add(amendButton, UIManager.getGridBagConstraints(0, 6, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 0)));

		deleteButton.setIcon(ui.getIcon("fina2.delete"));
		deleteButton.setFont(ui.getFont());
		deleteButton.setText(ui.getString("fina2.delete"));
		deleteButton.setAction(deleteAction);
		jPanel3.add(deleteButton, UIManager.getGridBagConstraints(0, 7, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 0)));

		reviewButton.setIcon(ui.getIcon("fina2.review"));
		reviewButton.setFont(ui.getFont());
		reviewButton.setText(ui.getString("fina2.review"));
		reviewButton.setAction(reviewAction);
		jPanel3.add(reviewButton, UIManager.getGridBagConstraints(0, 8, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 0)));

		searchCombo.setEditable(true);

		jPanel3.add(searchLabel, UIManager.getGridBagConstraints(0, 9, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 0)));
		jPanel3.add(searchCombo, UIManager.getGridBagConstraints(0, 10, -1, -1, 5, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 0)));

		jPanel1.add(jPanel3, java.awt.BorderLayout.NORTH);

		getContentPane().add(jPanel1, java.awt.BorderLayout.EAST);

		jPanel2.setLayout(new java.awt.BorderLayout());
		jPanel2.setFont(ui.getFont());
		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "Fl_List");
		} else {
			helpButton.setEnabled(false);
		}
		jPanel4.add(helpButton);

		jPanel2.add(jPanel4, java.awt.BorderLayout.WEST);

		jPanel5.add(refreshButton);

		printButton.setFont(ui.getFont());
		printButton.setAction(printAction);

		jPanel5.add(printButton);

		jPanel5.add(closeButton);

		jPanel2.add(jPanel5, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

	} // GEN-END:initComponents

	protected void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {
		initTree();
	}

	protected void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		formComponentHidden(null);
		dispose();
	}

	protected void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {

	}

	public JComboBox getSearchCombo() {
		return searchCombo;
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton amendButton;
	private javax.swing.JButton createBankButton;
	private javax.swing.JButton createBranchButton;
	private javax.swing.JButton createBranchManagementButton;
	private javax.swing.JButton createHistoryButton;
	private javax.swing.JButton createLicenceButton;
	private javax.swing.JButton createManagementButton;
	private javax.swing.JButton deleteButton;
	private javax.swing.JButton printButton;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JButton reviewButton;
	private javax.swing.JLabel searchLabel;
	private javax.swing.JComboBox searchCombo = new JComboBox();
	private javax.swing.JTextField searchText = new JTextField();
	private javax.swing.JScrollPane scrollPane;
	// End of variables declaration//GEN-END:variables
}

/**
 *
 */
class CreateBankAction extends AbstractAction {

	/** Frame with a list of banks */
	private BanksFrame banksFrame = null;

	/** Constructor */
	CreateBankAction(BanksFrame banksFrame) {

		this.banksFrame = banksFrame;

		UIManager ui = fina2.Main.main.ui;
		putValue(AbstractAction.NAME, ui.getString("fina2.bank.newBank"));
	}

	/*
	 * public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
	 * 
	 * Node node = tree.getSelectedNode();
	 * 
	 * if(node == null) return;
	 * 
	 * BankPK bankPK = null; dialog.show(null, bankPK, true); Node newNode =
	 * dialog.getNode();
	 * 
	 * if(newNode != null) {
	 * 
	 * DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(newNode);
	 * DefaultMutableTreeNode treeNode = tree.getSelectedTreeNode();
	 * ((javax.swing.tree.DefaultTreeModel)tree.getModel()).insertNodeInto(
	 * newTreeNode, treeNode, treeNode.getChildCount() );
	 * 
	 * ((javax.swing.tree.DefaultTreeModel)tree.getModel()).insertNodeInto( new
	 * DefaultMutableTreeNode( new Node( new BankPK(-1),
	 * ui.getString("fina2.metadata.loading"), new Integer(-1) ) ), newTreeNode,
	 * 0 );
	 * 
	 * bankPK = (BankPK)newNode.getPrimaryKey(); int bankId = bankPK.getId();
	 * bankAccess.putAmendBanks(new Integer(bankId), new Integer(bankId));
	 * bankAccess.putReviewBanks(new Integer(bankId), new Integer(bankId));
	 * tree.scrollPathToVisible(new
	 * javax.swing.tree.TreePath(newTreeNode.getPath())); } }
	 */

	/** Called to create bank */
	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

		Node selectedNode = banksFrame.getSelectedNode();

		if (isValidNode(selectedNode)) {
			/* Selected node is valid */
			showBankDialog(selectedNode);
		}
	}

	/** Shows BankAmendDialog */
	private void showBankDialog(Node selectedNode) {

		/* Creating bank type info from a selected node */
		BankTypePK bankType = createBankTypePK(selectedNode);
		String bankTypeStr = getBankTypeString(selectedNode);

		/* Showing the dialog with created bank type */
		BankAmendDialog bankDialog = new BankAmendDialog(bankType, bankTypeStr);
		bankDialog.setBanksFrame(banksFrame);
		bankDialog.show();
	}

	/**
	 * Created bank type object from a given node. If the given node is the
	 * root, the result will be null.
	 */
	private BankTypePK createBankTypePK(Node selectedNode) {

		Integer type = (Integer) selectedNode.getType();

		if (type == BanksConstants.NODETYPE_NODE) {
			/* Selected node is the root. Result is null. */
			return null;
		}

		/* Bank type id is stored in a node's primary key as Integer object */
		int bankTypeId = (Integer) selectedNode.getPrimaryKey();
		BankTypePK bankTypePK = new BankTypePK(bankTypeId);

		return bankTypePK;
	}

	/**
	 * Returns bank type text from a given node. If the given node is the root,
	 * the result will be empty string.
	 */
	private String getBankTypeString(Node selectedNode) {

		Integer type = (Integer) selectedNode.getType();

		if (type == BanksConstants.NODETYPE_NODE) {
			/* Selected node is the root. Result is empty string. */
			return "";
		}

		return selectedNode.getLabel();
	}

	/** Checks whether a given node is valid: not null, is bank type. */
	private boolean isValidNode(Node node) {

		if (node == null) {
			/* Not valid node */
			return false;
		}

		Integer type = (Integer) node.getType();

		if (type != BanksConstants.NODETYPE_NODE && type != BanksConstants.NODETYPE_BANK_TYPE) {
			/* Not the root and bank type node. Not valid node. */
			return false;
		}

		/* Valid node */
		return true;
	}
}

class CreateLicenseAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTree tree;
	private Node node;
	private Node newNode;
	private LicenceAmendDialog dialog;

	CreateLicenseAction(java.awt.Frame parent, EJBTree tree) {
		this.tree = tree;

		dialog = new LicenceAmendDialog(parent, true);

		putValue(AbstractAction.NAME, ui.getString("fina2.bank.newLicense"));
		// putValue(AbstractAction.SMALL_ICON,
		// ui.getIcon("fina2.bank.newBank"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

		Node node = tree.getSelectedNode();
		DefaultMutableTreeNode n = (DefaultMutableTreeNode) (tree.getSelectedTreeNode()).getParent();

		if (node == null)
			return;
		BankPK bankPK = (BankPK) (((Node) n.getUserObject()).getPrimaryKey());
		dialog.show(null, bankPK, true);

		Node newNode = dialog.getNode();
		if (newNode != null) {
			DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(newNode);
			DefaultMutableTreeNode treeNode = tree.getSelectedTreeNode();
			((javax.swing.tree.DefaultTreeModel) tree.getModel()).insertNodeInto(newTreeNode, treeNode, treeNode.getChildCount());

			tree.scrollPathToVisible(new javax.swing.tree.TreePath(newTreeNode.getPath()));
		}
	}

}

class CreateManagementAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTree tree;
	private Node node;
	private Node newNode;
	private ManagAmendDialog dialog;

	CreateManagementAction(java.awt.Frame parent, EJBTree tree) {
		this.tree = tree;

		dialog = new ManagAmendDialog(parent, true);

		putValue(AbstractAction.NAME, ui.getString("fina2.bank.newManagement"));
		// putValue(AbstractAction.SMALL_ICON,
		// ui.getIcon("fina2.bank.newManagement"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		Node node = tree.getSelectedNode();
		DefaultMutableTreeNode n = (DefaultMutableTreeNode) (tree.getSelectedTreeNode()).getParent();

		if (node == null)
			return;
		BankPK bankPK = (BankPK) (((Node) n.getUserObject()).getPrimaryKey());
		dialog.show(null, bankPK, true);

		Node newNode = dialog.getNode();
		if (newNode != null) {
			DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(newNode);
			DefaultMutableTreeNode treeNode = tree.getSelectedTreeNode();
			((javax.swing.tree.DefaultTreeModel) tree.getModel()).insertNodeInto(newTreeNode, treeNode, treeNode.getChildCount());

			tree.scrollPathToVisible(new javax.swing.tree.TreePath(newTreeNode.getPath()));
		}
	}

}

class CreateBranchAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTree tree;
	private Node node;
	private Node newNode;
	private BranchAmendDialog dialog;

	CreateBranchAction(java.awt.Frame parent, EJBTree tree) {
		this.tree = tree;

		dialog = new BranchAmendDialog(parent, true);

		putValue(AbstractAction.NAME, ui.getString("fina2.bank.newBranch"));
		// putValue(AbstractAction.SMALL_ICON,
		// ui.getIcon("fina2.bank.newBranch"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		Node node = tree.getSelectedNode();
		DefaultMutableTreeNode n = (DefaultMutableTreeNode) (tree.getSelectedTreeNode()).getParent();

		if (node == null)
			return;
		BankPK bankPK = (BankPK) (((Node) n.getUserObject()).getPrimaryKey());
		dialog.show(null, bankPK, true);

		Node newNode = dialog.getNode();
		if (newNode != null) {
			DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(newNode);
			DefaultMutableTreeNode treeNode = tree.getSelectedTreeNode();
			((javax.swing.tree.DefaultTreeModel) tree.getModel()).insertNodeInto(newTreeNode, treeNode, treeNode.getChildCount());

			tree.scrollPathToVisible(new javax.swing.tree.TreePath(newTreeNode.getPath()));
		}
	}

}

class CreateBranchManagementAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTree tree;
	private Node node;
	private Node newNode;
	private ManagAmendDialog dialog;

	CreateBranchManagementAction(java.awt.Frame parent, EJBTree tree) {
		this.tree = tree;

		dialog = new ManagAmendDialog(parent, true);

		putValue(AbstractAction.NAME, ui.getString("fina2.bank.newBranchManagement"));
		// putValue(AbstractAction.SMALL_ICON,
		// ui.getIcon("fina2.bank.newBranchManagement"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		Node node = tree.getSelectedNode();
		DefaultMutableTreeNode n = (DefaultMutableTreeNode) tree.getSelectedTreeNode();

		if (node == null)
			return;
		BranchPK branchPK = (BranchPK) (((Node) n.getUserObject()).getPrimaryKey());
		dialog.show(null, branchPK, true);

		Node newNode = dialog.getNode();
		if (newNode != null) {
			DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(newNode);
			DefaultMutableTreeNode treeNode = tree.getSelectedTreeNode();
			((javax.swing.tree.DefaultTreeModel) tree.getModel()).insertNodeInto(newTreeNode, treeNode, treeNode.getChildCount());

			tree.scrollPathToVisible(new javax.swing.tree.TreePath(newTreeNode.getPath()));
		}
	}

}

class CreateHistoryAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTree tree;
	private Node node;
	private Node newNode;

	private HistoryDialog historyDialog;

	CreateHistoryAction(java.awt.Frame parent, EJBTree tree) {
		this.tree = tree;

		putValue(AbstractAction.NAME, ui.getString("fina2.bank.newHistory"));
		// putValue(AbstractAction.SMALL_ICON,
		// ui.getIcon("fina2.bank.newHistory"));
		historyDialog = new HistoryDialog(parent);
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		historyDialog.show();
	}

	@SuppressWarnings("serial")
	class HistoryDialog extends JDialog {
		private fina2.ui.UIManager ui = fina2.Main.main.ui;

		private Container container;

		private JButton saveButton;
		private JButton cancelButton;

		private JPanel buttonPanel;

		private JEditorPane text;

		public HistoryDialog(java.awt.Frame parent) {
			super(parent, true);
			initComtonents();
			ui.loadIcon("fina2.save", "save.gif");
			ui.loadIcon("fina2.close", "close.gif");
			this.setSize(300, 200);
		}

		private void initComtonents() {
			container = this.getContentPane();
			// set Title
			this.setTitle("History");

			// Dialog close Listener
			addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent evt) {
					close();
				}
			});

			StyledEditorKit editor = new StyledEditorKit();

			text = new JEditorPane();
			System.out.println(text.getEditorKit().toString());
			container.add(new JScrollPane(text), BorderLayout.CENTER);
			buttonPanel = new JPanel();

			// Ok Button
			saveButton = new JButton();
			saveButton.setIcon(ui.getIcon("fina2.save"));
			saveButton.setText(ui.getString("fina2.returns.save"));
			saveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
				}
			});
			buttonPanel.add(saveButton);

			// close Button
			cancelButton = new JButton();
			cancelButton.setText(ui.getString("fina2.close"));
			cancelButton.setIcon(ui.getIcon("fina2.close"));
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					close();
				}
			});
			buttonPanel.add(cancelButton);

			container.add(buttonPanel, BorderLayout.SOUTH);
		}

		@Override
		public void show() {
			setLocationRelativeTo(getParent());
			super.show();
		}

		private void close() {
			this.dispose();
		}
	}

}

class AmendAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTree tree;
	private Node node;
	private Node newNode;
	private BankAmendDialog bankDialog;
	private LicenceAmendDialog licenceDialog;
	private ManagAmendDialog managAmendDialog;
	private BranchAmendDialog branchDialog;
	private BanksGroupsDialog bankGroupsDialog;
	private BanksFrame banksFrame;
	private BankGroupAmendDialog bankGroupAmendDialog;

	AmendAction(java.awt.Frame parent, EJBTree tree, BanksFrame bFrame) {
		this.tree = tree;
		this.banksFrame = bFrame;

		bankDialog = new BankAmendDialog(parent, true);
		licenceDialog = new LicenceAmendDialog(parent, true);
		managAmendDialog = new ManagAmendDialog(parent, true);
		branchDialog = new BranchAmendDialog(parent, true);
		bankGroupsDialog = new BanksGroupsDialog(parent, true);
		bankGroupAmendDialog = new BankGroupAmendDialog(parent, true);

		putValue(AbstractAction.NAME, ui.getString("fina2.amend"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.amend"));
	}

	private BankPK getBankPK(DefaultMutableTreeNode node) {
		BankPK pk = null;
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
		int id = ((BankPK) ((Node) parent.getUserObject()).getPrimaryKey()).getId();
		if (id < 0)
			pk = getBankPK(parent);
		return new BankPK(id);
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

		Node node = tree.getSelectedNode();
		DefaultMutableTreeNode n = (DefaultMutableTreeNode) tree.getSelectedTreeNode();
		int selectedIndex = tree.getSelectionRows()[0];

		if (node == null)
			return;
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_BANK) {
			bankDialog.show(node, (BankPK) (((Node) n.getUserObject()).getPrimaryKey()), true);
			banksFrame.updateBankGroupNodes((DefaultMutableTreeNode) n.getFirstChild());
		}
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_LICENSE)
			licenceDialog.show(node, (BankPK) (getBankPK(n)), true);
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_MANAGEMENT)
			managAmendDialog.show(node, (BankPK) (getBankPK(n)), true);
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_BRANCH)
			branchDialog.show(node, (BankPK) (getBankPK(n)), true);
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_BRANCH_MANAGEMENT)
			managAmendDialog.show(node, (BranchPK) (((Node) ((DefaultMutableTreeNode) n.getParent()).getUserObject()).getPrimaryKey()), true);
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_BANK_GROUP_NODE || ((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_DEF_BANK_GROUP_NODE) {
			if (bankGroupAmendDialog.show(node, true, false) == BankGroupAmendDialog.OK) {
				banksFrame.updateBankGroupNodes((DefaultMutableTreeNode) n.getParent());
				tree.setSelectionRow(selectedIndex);
			}
		}
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_BANK_GROUP) {
			if (bankGroupsDialog.show(node, (BankPK) (getBankPK(n)), true) == bankGroupsDialog.OK) {
				banksFrame.updateBankGroupNodes(n);
			}
		}
		((javax.swing.tree.DefaultTreeModel) tree.getModel()).nodeChanged(tree.getSelectedTreeNode());
	}
}

class DeleteAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTree tree;
	private Node node;
	private Node newNode;
	private java.awt.Frame parent;
	private BankAccess bankAccess;
	private BanksFrame banksFrame;

	DeleteAction(java.awt.Frame parent, EJBTree tree, BankAccess bankAccess, BanksFrame banksFrame) {
		this.tree = tree;
		this.parent = parent;
		this.bankAccess = bankAccess;
		this.banksFrame = banksFrame;

		putValue(AbstractAction.NAME, ui.getString("fina2.delete"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.delete"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		Node node = tree.getSelectedNode();
		DefaultMutableTreeNode n = (DefaultMutableTreeNode) tree.getSelectedTreeNode();

		if (node == null)
			return;

		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_BANK) {
			if (!ui.showConfirmBox(parent, ui.getString("fina2.bank.bankDeleteQuestion")))
				return;
		}
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_LICENSE) {
			if (!ui.showConfirmBox(parent, ui.getString("fina2.bank.bankLicenseDeleteQuestion")))
				return;
		}
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_MANAGEMENT) {
			if (!ui.showConfirmBox(parent, ui.getString("fina2.bank.bankManagDeleteQuestion")))
				return;
		}
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_BRANCH) {
			if (!ui.showConfirmBox(parent, ui.getString("fina2.bank.bankBranchDeleteQuestion")))
				return;
		}
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_BRANCH_MANAGEMENT) {
			if (!ui.showConfirmBox(parent, ui.getString("fina2.bank.branchManagDeleteQuestion")))
				return;
		}
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_BANK_GROUP_NODE || ((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_DEF_BANK_GROUP_NODE) {
			if (!ui.showConfirmBox(parent, ui.getString("fina2.bank.bankGroupDeleteQuestion")))
				return;
		}

		try {
			InitialContext jndi = fina2.Main.getJndiContext();

			int nodeType = ((Integer) node.getType()).intValue();

			if (nodeType == BanksConstants.NODETYPE_BANK) {
				Object ref = jndi.lookup("fina2/bank/Bank");
				BankHome home = (BankHome) PortableRemoteObject.narrow(ref, BankHome.class);
				BankPK pk = (BankPK) node.getPrimaryKey();
				Bank bank = home.findByPrimaryKey(pk);

				TableRowImpl row = new TableRowImpl(pk, 10);
				row.setValue(3, bank.getCode());
				row.setValue(4, bank.getName(Main.getCurrentLanguage()));

				bank.remove();

				bankAccess.removeObject(pk.getId());

				String deleteSuggestion = banksFrame.createSuggestionString(row);
				banksFrame.suggestionSet.remove(deleteSuggestion);
				banksFrame.setModel(new DefaultComboBoxModel(banksFrame.suggestionSet.toArray()), "");

			}
			if (nodeType == BanksConstants.NODETYPE_BANK_GROUP_NODE || nodeType == BanksConstants.NODETYPE_DEF_BANK_GROUP_NODE) {
				Object ref = jndi.lookup("fina2/bank/Bank");
				BankHome home = (BankHome) PortableRemoteObject.narrow(ref, BankHome.class);
				BankPK bankPk = (BankPK) node.getParentPK();
				BankGroupPK bankGroupPk = (BankGroupPK) node.getPrimaryKey();
				Bank bank = home.findByPrimaryKey(bankPk);
				Collection bankGroupPKs = bank.getBankGroupPKs();
				bankGroupPKs.remove(bankGroupPk);
				bank.setBankGroupPKs(bankGroupPKs);
			}
			if (nodeType == BanksConstants.NODETYPE_LICENSE) {
				Object ref = jndi.lookup("fina2/bank/Licence");
				LicenceHome home = (LicenceHome) PortableRemoteObject.narrow(ref, LicenceHome.class);
				LicencePK pk = (LicencePK) node.getPrimaryKey();
				Licence licence = home.findByPrimaryKey(pk);
				licence.remove();
			}
			if (nodeType == BanksConstants.NODETYPE_MANAGEMENT) {
				Object ref = jndi.lookup("fina2/bank/BankManag");
				BankManagHome home = (BankManagHome) PortableRemoteObject.narrow(ref, BankManagHome.class);
				BankManagPK pk = (BankManagPK) node.getPrimaryKey();
				BankManag bankManag = home.findByPrimaryKey(pk);
				bankManag.remove();
			}
			if (nodeType == BanksConstants.NODETYPE_BRANCH) {
				Object ref = jndi.lookup("fina2/bank/Branch");
				BranchHome home = (BranchHome) PortableRemoteObject.narrow(ref, BranchHome.class);
				BranchPK pk = (BranchPK) node.getPrimaryKey();
				Branch branch = home.findByPrimaryKey(pk);
				branch.remove();
			}
			if (nodeType == BanksConstants.NODETYPE_BRANCH_MANAGEMENT) {
				Object ref = jndi.lookup("fina2/bank/BranchManag");
				BranchManagHome home = (BranchManagHome) PortableRemoteObject.narrow(ref, BranchManagHome.class);
				BranchManagPK pk = (BranchManagPK) node.getPrimaryKey();
				BranchManag branchManag = home.findByPrimaryKey(pk);
				branchManag.remove();
			}

			((javax.swing.tree.DefaultTreeModel) tree.getModel()).removeNodeFromParent(tree.getSelectedTreeNode());

		} catch (Exception e) {
			e.printStackTrace();
			Main.errorHandler(parent, Main.getString("fina2.title"), Main.getString("fina2.delete.item"));
		}
	}
}

class ReviewAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTree tree;
	private Node node;
	private Node newNode;
	private BankAmendDialog bankDialog;
	private LicenceAmendDialog licenceDialog;
	private ManagAmendDialog managAmendDialog;
	private BranchAmendDialog branchDialog;
	private BanksGroupsDialog bankGroupsDialog;
	private BankGroupAmendDialog bankGroupAmendDialog;

	ReviewAction(java.awt.Frame parent, EJBTree tree) {
		this.tree = tree;

		bankDialog = new BankAmendDialog(parent, true);
		licenceDialog = new LicenceAmendDialog(parent, true);
		managAmendDialog = new ManagAmendDialog(parent, true);
		branchDialog = new BranchAmendDialog(parent, true);
		bankGroupsDialog = new BanksGroupsDialog(parent, true);
		bankGroupAmendDialog = new BankGroupAmendDialog(parent, true);

		putValue(AbstractAction.NAME, ui.getString("fina2.review"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.review"));
	}

	private BankPK getBankPK(DefaultMutableTreeNode node) {
		BankPK pk = null;
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
		int id = ((BankPK) ((Node) parent.getUserObject()).getPrimaryKey()).getId();
		if (id < 0)
			pk = getBankPK(parent);
		return new BankPK(id);
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

		Node node = tree.getSelectedNode();
		DefaultMutableTreeNode n = (DefaultMutableTreeNode) tree.getSelectedTreeNode();

		if (node == null)
			return;
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_BANK)
			bankDialog.show(node, (BankPK) (((Node) n.getUserObject()).getPrimaryKey()), false);
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_LICENSE)
			licenceDialog.show(node, (BankPK) (getBankPK(n)), false);
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_MANAGEMENT)
			managAmendDialog.show(node, (BankPK) (getBankPK(n)), false);
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_BRANCH)
			branchDialog.show(node, (BankPK) (getBankPK(n)), false);
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_BRANCH_MANAGEMENT)
			managAmendDialog.show(node, (BranchPK) (((Node) n.getUserObject()).getPrimaryKey()), false);
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_BANK_GROUP_NODE || ((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_DEF_BANK_GROUP_NODE) {
			bankGroupAmendDialog.show(node, false, false);
		}
		if (((Integer) node.getType()).intValue() == BanksConstants.NODETYPE_BANK_GROUP) {
			bankGroupsDialog.show(node, (BankPK) (getBankPK(n)), false);
		}
		// ((javax.swing.tree.DefaultTreeModel)tree.getModel()).nodeChanged(tree.
		// getSelectedTreeNode());
	}
}

/**
 * Print action for FI frame.
 */
class PrintAction extends AbstractAction {

	private UIManager ui = Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	PrintAction(java.awt.Frame parent) {
		putValue(AbstractAction.NAME, ui.getString("fina2.print"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.print"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		print();
	}

	/**
	 * Prints FI list of the current user.
	 */
	private void print() {
		EJBTable table = initTable();
		String frameTitle = ui.getString("fina2.security.fi");
		// System.out.println(table.getRows());
		TableReviewFrame tableReviewFrame = new TableReviewFrame();
		tableReviewFrame.show(frameTitle, table);

	}

	/** Inits a table with FI list */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private EJBTable initTable() {

		// Define columns
		ArrayList<String> colNames = new ArrayList<String>();
		// colNames.add("#");
		colNames.add(ui.getString("fina2.bank.bankCode"));
		colNames.add(ui.getString("fina2.bank.bankName"));
		colNames.add(ui.getString("fina2.bank.bankType"));
		colNames.add(ui.getString("fina2.bank.bankGroupDefault"));

		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/regions/RegionStructureSession");
			RegionStructureSessionHome home = (RegionStructureSessionHome) PortableRemoteObject.narrow(ref, RegionStructureSessionHome.class);
			RegionStructureSession session = home.create();
			Map<Integer, String> regionProperties = session.getProperties(main.getLanguageHandle());
			int maxCols = Integer.parseInt(regionProperties.get(0));
			regionProperties.remove(0);

			for (Entry<Integer, String> e : regionProperties.entrySet()) {
				colNames.add(e.getValue() + " " + ui.getString("fina2.code"));
				colNames.add(e.getValue() + " " + ui.getString("fina2.description"));
			}
			if (regionProperties.size() < maxCols) {
				for (int i = 0; i < maxCols - regionProperties.size(); i++) {
					colNames.add("NONAME " + " " + ui.getString("fina2.code"));
					colNames.add("NONAME " + " " + ui.getString("fina2.description"));
				}
			}
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}

		colNames.add(ui.getString("fina2.bank.email"));
		colNames.add(ui.getString("fina2.bank.phone"));
		colNames.add(ui.getString("fina2.bank.address"));

		// Get FI list from the server
		Collection fiList = null;
		try {
			// get and correction FIs print list
			fiList = correctionPrintFIList(FIGate.loadBanks());
		} catch (Exception e) {
			Main.generalErrorHandler(e);
			return null;
		}
		// sort FI list
		Collections.sort((List) fiList, new Comparators.TableRowComparatorValueString(3, 1));

		// Put data to the table
		EJBTable table = new EJBTable();
		table.setAllowSort(false);
		table.initTable(colNames, fiList);

		return table;

	}

	// Correction FIs print list
	private Collection<TableRow> correctionPrintFIList(Collection<TableRow> fis) {
		Collection<TableRow> newFIs = new ArrayList<TableRow>();
		for (TableRow row : fis) {
			TableRow newRow = new TableRowImpl(row.getPrimaryKey(), row.getColumnCount() - 1);
			for (int i = 0; i < newRow.getColumnCount(); i++) {
				newRow.setValue(i, row.getValue(i + 1));
			}
			newFIs.add(newRow);
		}
		return newFIs;
	}
}