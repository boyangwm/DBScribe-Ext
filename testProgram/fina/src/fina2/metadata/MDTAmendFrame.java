package fina2.metadata;

import java.awt.GridBagConstraints;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.FinderException;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import fina2.BaseFrame;
import fina2.Main;
import fina2.actions.FormulaRepositoryAction;
import fina2.i18n.Language;
import fina2.metadata.jaxb.MDTNodeData;
import fina2.ui.ProcessDialog;
import fina2.ui.UIManager;
import fina2.ui.UIManager.IndeterminateLoading;
import fina2.ui.tree.DNDTree;
import fina2.ui.tree.EJBTree;
import fina2.ui.tree.Node;
import fina2.ui.tree.TreeReviewFrame;

public class MDTAmendFrame extends BaseFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private DNDTree tree;
	private ExportNodeAction exportNodeAction;
	private ImportNodeAction importNodeAction;
	private CreateNodeAction createNodeAction;
	private CreateInputAction createInputAction;
	private CreateVariableAction createVariableAction;
	private AmendAction amendAction;
	private ReviewAction reviewAction;
	private DeleteAction deleteAction;
	private DisableAction disableAction;
	private UpAction upAction;
	private DownAction downAction;
	private PrintAction printAction;
	private RefreshAction refreshAction;
	private FormulaRepositoryAction frAction;

	private boolean canAmend = false;
	private boolean canDelete = false;
	private boolean canReview = false;

	private String sync = "sync";
	private boolean dontExpand = false;

	private DefaultMutableTreeNode pasteTreeNode = null;
	private Node pasteNode = null;
	private boolean cut = false;

	private MDTDependenciesFrame dependDialog;

	/** Creates new form MDTAmendFrame */
	public MDTAmendFrame() {
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.close", "cancel.gif");
		ui.loadIcon("fina2.node", "node.gif");
		ui.loadIcon("fina2.input", "input.gif");
		ui.loadIcon("fina2.variable", "variable.gif");
		ui.loadIcon("fina2.amend", "amend.gif");
		ui.loadIcon("fina2.review", "review.gif");
		ui.loadIcon("fina2.delete", "delete.gif");
		ui.loadIcon("fina2.disable", "disable.gif");
		ui.loadIcon("fina2.enable", "enable.gif");
		ui.loadIcon("fina2.up", "up.gif");
		ui.loadIcon("fina2.down", "down.gif");
		ui.loadIcon("fina2.cut", "cut.gif");
		ui.loadIcon("fina2.copy", "copy.gif");
		ui.loadIcon("fina2.paste", "paste.gif");
		ui.loadIcon("fina2.print", "print.gif");
		ui.loadIcon("fina2.refresh", "refresh.gif");
		ui.loadIcon("fina2.filter", "filter.gif");
		ui.loadIcon("fina2.exportMdt", "exportMdt.gif");
		ui.loadIcon("fina2.importMdt", "importMdt.gif");

		// dialogComparison = new MDTComparisonAmendDialog(this, true);
		dependDialog = new MDTDependenciesFrame(this, true); // this, true);

		frAction = (FormulaRepositoryAction) ui.getAction("fina2.actions.formulaRepository");

		tree = new DNDTree();
		tree.addTreeExpansionListener(new TreeExpansionListener() {

			private DefaultMutableTreeNode node;

			public void treeCollapsed(TreeExpansionEvent evt) {
			}

			public synchronized void treeExpanded(TreeExpansionEvent evt) {
				if (dontExpand) {
					return;
				}
				node = (DefaultMutableTreeNode) evt.getPath().getLastPathComponent();
				Thread thread = new Thread() {
					public void run() {
						getChildNodes(node);
					}
				};
				thread.start();
			}
		});

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

		tree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {

			public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {

				Node node = tree.getSelectedNode();

				if ((node == null) || (((Integer) node.getType()).intValue() == -1))
					levelText.setText(" ");
				else if (node != null)
					levelText.setText(String.valueOf(tree.getSelectionPath().getPathCount() - 1));

				if (node == null) {
					exportNodeAction.setEnabled(false);
					importNodeAction.setEnabled(false);
					createNodeAction.setEnabled(false);
					createInputAction.setEnabled(false);
					createVariableAction.setEnabled(false);
					amendAction.setEnabled(false);
					deleteAction.setEnabled(false);
					disableAction.setEnabled(false);
					reviewAction.setEnabled(false);
					upAction.setEnabled(false);
					downAction.setEnabled(false);

					cutNodeItem.setEnabled(false);
					cutIVItem.setEnabled(false);
					copyNodeItem.setEnabled(false);
					copyIVItem.setEnabled(false);
					pasteNodeItem.setEnabled(false);
					pasteIVItem.setEnabled(false);

					return;
				}

				if (node.toString().startsWith("[DISABLED][")) {
					disableAction.putValue(AbstractAction.NAME, ui.getString("fina2.enable"));
					disableAction.putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.enable"));
				} else {
					disableAction.putValue(AbstractAction.NAME, ui.getString("fina2.disable"));
					disableAction.putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.disable"));
				}

				int type = ((Integer) node.getType()).intValue();

				if ((type == MDTConstants.NODETYPE_INPUT) || (type == MDTConstants.NODETYPE_NODE) || (type == MDTConstants.NODETYPE_VARIABLE))
					frAction.setNode((Node) tree.getSelectedNode());
				else
					frAction.setNode(null);

				if ((type == MDTConstants.NODETYPE_INPUT) || (type == MDTConstants.NODETYPE_VARIABLE)) {
					exportNodeAction.setEnabled(true);
					importNodeAction.setEnabled(false);
					createNodeAction.setEnabled(false);
					createInputAction.setEnabled(false);
					createVariableAction.setEnabled(false);
				} else {
					if (canAmend) {
						exportNodeAction.setEnabled(true);
						importNodeAction.setEnabled(true);
						createNodeAction.setEnabled(true);
						createInputAction.setEnabled(true);
						createVariableAction.setEnabled(true);
						if (type == -1) {
							createInputAction.setEnabled(false);
							createVariableAction.setEnabled(false);
						} else {
							createInputAction.setEnabled(true);
							createVariableAction.setEnabled(true);
						}
					}
				}
				if (type == -1) {
					exportNodeItem.setEnabled(false);
					importNodeItem.setEnabled(false);

					amendAction.setEnabled(false);
					deleteAction.setEnabled(false);
					disableAction.setEnabled(false);
					reviewAction.setEnabled(false);
					upAction.setEnabled(false);
					downAction.setEnabled(false);
					dependenceButton.setEnabled(false);
					formulaRepositoryButton.setEnabled(false);
				} else {
					exportNodeItem.setEnabled(true);
					importNodeItem.setEnabled(true);
					exportNodeAction.setEnabled(canAmend);
					reviewAction.setEnabled(true);
					dependenceButton.setEnabled(true);
					formulaRepositoryButton.setEnabled(true);
					amendAction.setEnabled(canAmend);
					deleteAction.setEnabled(canDelete);
					disableAction.setEnabled(canAmend);

					if (((MutableTreeNode) (tree.getSelectedTreeNode()).getParent()).getIndex((MutableTreeNode) tree.getSelectedTreeNode()) == 0) {
						upAction.setEnabled(false);
					} else
						upAction.setEnabled(canAmend);

					if (((MutableTreeNode) (tree.getSelectedTreeNode()).getParent()).getChildCount() - 1 == ((MutableTreeNode) (tree.getSelectedTreeNode()).getParent())
							.getIndex((MutableTreeNode) tree.getSelectedTreeNode())) {

						downAction.setEnabled(false);
					} else {
						downAction.setEnabled(canAmend);
					}
				}
				if (canAmend) {
					if (type == -1) {
						cutNodeItem.setEnabled(false);
						cutIVItem.setEnabled(false);
						copyNodeItem.setEnabled(false);
						copyIVItem.setEnabled(false);
						if (tree.getSelectedNode() != pasteNode && pasteNode != null) {
							pasteNodeItem.setEnabled(true);
							pasteIVItem.setEnabled(true);
						} else {
							pasteNodeItem.setEnabled(false);
							pasteIVItem.setEnabled(false);
						}
					} else {
						cutNodeItem.setEnabled(true);
						cutIVItem.setEnabled(true);
						copyNodeItem.setEnabled(true);
						copyIVItem.setEnabled(true);
						if (tree.getSelectedNode() != pasteNode && pasteNode != null && type != MDTConstants.NODETYPE_INPUT && type != MDTConstants.NODETYPE_VARIABLE) {
							pasteNodeItem.setEnabled(true);
							pasteIVItem.setEnabled(true);
						} else {
							pasteNodeItem.setEnabled(false);
							pasteIVItem.setEnabled(false);
						}
					}
				}
				copyNodeItem.setEnabled(false);
				copyIVItem.setEnabled(false);

				if (pasteNode != null) {
					Object[] nodes = tree.getSelectionPath().getPath();
					for (int i = 0; i < nodes.length; i++) {
						MDTNodePK pk = (MDTNodePK) ((Node) ((DefaultMutableTreeNode) nodes[i]).getUserObject()).getPrimaryKey();
						if (pasteNode.getPrimaryKey().equals(pk)) {
							pasteNodeItem.setEnabled(false);
							pasteIVItem.setEnabled(false);
						}
					}
				}
			}
		});
		exportNodeAction = new ExportNodeAction(this, tree);
		importNodeAction = new ImportNodeAction(this, tree);
		createNodeAction = new CreateNodeAction(main.getMainFrame(), tree);
		createInputAction = new CreateInputAction(main.getMainFrame(), tree);
		createVariableAction = new CreateVariableAction(main.getMainFrame(), tree);
		amendAction = new AmendAction(main.getMainFrame(), tree);
		reviewAction = new ReviewAction(main.getMainFrame(), tree);
		deleteAction = new DeleteAction(main.getMainFrame(), tree);
		disableAction = new DisableAction(main.getMainFrame(), tree);
		upAction = new UpAction(main.getMainFrame(), tree);
		downAction = new DownAction(main.getMainFrame(), tree);
		printAction = new PrintAction(main.getMainFrame(), tree);
		refreshAction = new RefreshAction(main.getMainFrame(), this, tree);

		initComponents();

		tree.addMenu(new Integer(-1), popupNodeMenu);
		tree.addMenu(new Integer(MDTConstants.NODETYPE_NODE), popupNodeMenu);
		tree.addMenu(new Integer(MDTConstants.NODETYPE_INPUT), popupIVMenu);
		tree.addMenu(new Integer(MDTConstants.NODETYPE_VARIABLE), popupIVMenu);

		cutNodeItem.setEnabled(false);
		cutIVItem.setEnabled(false);
		copyNodeItem.setEnabled(false);
		copyIVItem.setEnabled(false);
		pasteNodeItem.setEnabled(false);
		pasteIVItem.setEnabled(false);

		scrollPane.setViewportView(tree);
		BaseFrame.ensureVisible(this);
	}

	private boolean getChildNodes(DefaultMutableTreeNode node) {
		synchronized (sync) {
			if (node.getChildCount() == 0) {
				return false;
			}

			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getFirstChild();
			if (((MDTNodePK) ((Node) child.getUserObject()).getPrimaryKey()).getId() != -1) {
				return false;
			}

			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/metadata/MDTSession");
				MDTSessionHome home = (MDTSessionHome) PortableRemoteObject.narrow(ref, MDTSessionHome.class);

				MDTSession session = home.create();

				Collection nodes = session.getChildNodes(main.getUserHandle(), main.getLanguageHandle(), (MDTNodePK) ((Node) node.getUserObject()).getPrimaryKey());

				prepareNodes((DefaultTreeModel) tree.getModel(), node, nodes);
				((DefaultTreeModel) tree.getModel()).removeNodeFromParent(child);

			} catch (Exception e) {
				Main.generalErrorHandler(e);
			}
			return true;
		}
	}

	private synchronized void prepareNodes(javax.swing.tree.DefaultTreeModel model, DefaultMutableTreeNode parent, Collection nodes) {
		for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			Node node = (Node) iter.next();
			DefaultMutableTreeNode n = new DefaultMutableTreeNode(node);
			model.insertNodeInto(n, parent, parent.getChildCount());
			if (((Integer) node.getType()).intValue() == MDTConstants.NODETYPE_NODE) {
				model.insertNodeInto(new DefaultMutableTreeNode(new Node(new MDTNodePK(-1), ui.getString("fina2.metadata.loading"), new Integer(-1))), n, 0);
			}
			// parent.add(n);
		}
	}

	public void show() {
		if (isVisible())
			return;

		try {
			frAction = (FormulaRepositoryAction) ui.getAction("fina2.actions.formulaRepository");
			formulaRepositoryButton.setAction(frAction);

			fina2.security.User user = (fina2.security.User) main.getUserHandle().getEJBObject();
			canAmend = user.hasPermission("fina2.metadata.amend");
			canDelete = user.hasPermission("fina2.metadata.delete");
			canReview = user.hasPermission("fina2.metadata.review");
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

		createNodeAction.setEnabled(canAmend);
		createNodeButton.setVisible(canAmend);
		exportNodeItem.setVisible(canAmend);

		importNodeItem.setVisible(canAmend);
		createNodeItem.setVisible(canAmend);

		createInputAction.setEnabled(canAmend);
		createInputButton.setVisible(canAmend);
		createInputItem.setVisible(canAmend);

		createVariableAction.setEnabled(canAmend);
		createVariableButton.setVisible(canAmend);
		createVariableItem.setVisible(canAmend);

		createMenu.setVisible(canAmend);

		amendAction.setEnabled(canAmend);
		amendButton.setVisible(canAmend);
		amendNodeItem.setVisible(canAmend);
		amendIVItem.setVisible(canAmend);

		disableAction.setEnabled(canAmend);
		disableButton.setVisible(canAmend);
		disableNodeItem.setVisible(canAmend);
		disableIVItem.setVisible(canAmend);

		copyNodeItem.setVisible(canAmend);
		copyIVItem.setVisible(canAmend);

		cutNodeItem.setVisible(canAmend);
		cutIVItem.setVisible(canAmend);

		pasteNodeItem.setVisible(canAmend);
		pasteIVItem.setVisible(canAmend);

		upAction.setEnabled(canAmend);
		upButton.setVisible(canAmend);
		upNodeItem.setVisible(canAmend);
		upIVItem.setVisible(canAmend);

		downAction.setEnabled(canAmend);
		downButton.setVisible(canAmend);
		downNodeItem.setVisible(canAmend);
		downIVItem.setVisible(canAmend);

		deleteAction.setEnabled(canDelete);
		deleteButton.setVisible(canDelete);
		deleteNodeItem.setVisible(canDelete);
		deleteIVItem.setVisible(canDelete);

		reviewAction.setEnabled(canAmend || canReview);
		reviewButton.setVisible(canAmend || canReview);
		reviewNodeItem.setVisible(canAmend || canReview);
		reviewIVItem.setVisible(canAmend || canReview);
		printButton.setVisible(canAmend || canReview);
		dependenceButton.setVisible(canAmend || canReview);
		formulaRepositoryButton.setEnabled(canAmend || canReview);

		initTree();

		super.show();
		// setVisible(true);

	}

	public void initTree() {
		try {

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/metadata/MDTSession");
			MDTSessionHome home = (MDTSessionHome) PortableRemoteObject.narrow(ref, MDTSessionHome.class);

			MDTSession session = home.create();

			Node rootNode = new Node(new MDTNodePK(0l), "        ", new Integer(-1));
			tree.initTree(rootNode);

			DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootNode);
			Collection nodes = session.getChildNodes(main.getUserHandle(), main.getLanguageHandle(), new MDTNodePK(0l));

			prepareNodes((javax.swing.tree.DefaultTreeModel) tree.getModel(), root, nodes);

			((javax.swing.tree.DefaultTreeModel) tree.getModel()).setRoot(root);
			/*
			 * tree.initTree( session.getTreeNodes(main.getUserHandle(),
			 * main.getLanguageHandle()) );
			 */

			tree.addIcon(new Integer(-1), ui.getIcon("fina2.node"));

			tree.addIcon(new Integer(MDTConstants.NODETYPE_NODE), ui.getIcon("fina2.node"));
			tree.addIcon(new Integer(MDTConstants.NODETYPE_INPUT), ui.getIcon("fina2.input"));
			tree.addIcon(new Integer(MDTConstants.NODETYPE_VARIABLE), ui.getIcon("fina2.variable"));

			tree.setRootVisible(true);
			tree.setSelectionRow(0);
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	public void findNode(String code, boolean exact) {

		dontExpand = true;
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/metadata/MDTNode");
			MDTNodeHome homeEn = (MDTNodeHome) PortableRemoteObject.narrow(ref, MDTNodeHome.class);

			ref = jndi.lookup("fina2/metadata/MDTSession");
			MDTSessionHome homeSs = (MDTSessionHome) PortableRemoteObject.narrow(ref, MDTSessionHome.class);

			MDTNode node = (exact == true) ? (MDTNode) homeEn.findByCodeExact(code) : (MDTNode) homeEn.findByCode(code);

			MDTSession session = homeSs.create();
			Collection parents = session.getParentNodes((MDTNodePK) (node.getPrimaryKey()));

			Object[] pkO = parents.toArray();
			for (int i = pkO.length - 1; i >= 0; i--) {
				loadSearchNodes((MDTNodePK) pkO[i]);
			}
			tree.gotoNode((MDTNodePK) node.getPrimaryKey());

		} catch (FinderException e) {
			Main.errorHandler(null, Main.getString("fina2.title"), Main.getString("fina2.metadata.nodeNotFound"));
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		dontExpand = false;

	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		popupNodeMenu = new javax.swing.JPopupMenu();
		amendNodeItem = new javax.swing.JMenuItem();
		reviewNodeItem = new javax.swing.JMenuItem();
		jSeparator1 = new javax.swing.JSeparator();
		createMenu = new javax.swing.JMenu();
		exportNodeItem = new javax.swing.JMenuItem();
		importNodeItem = new javax.swing.JMenuItem();
		createNodeItem = new javax.swing.JMenuItem();
		createInputItem = new javax.swing.JMenuItem();
		createVariableItem = new javax.swing.JMenuItem();
		jSeparator2 = new javax.swing.JSeparator();
		cutNodeItem = new javax.swing.JMenuItem();
		copyNodeItem = new javax.swing.JMenuItem();
		pasteNodeItem = new javax.swing.JMenuItem();
		jSeparator6 = new javax.swing.JSeparator();
		deleteNodeItem = new javax.swing.JMenuItem();
		disableNodeItem = new javax.swing.JMenuItem();
		jSeparator3 = new javax.swing.JSeparator();
		upNodeItem = new javax.swing.JMenuItem();
		downNodeItem = new javax.swing.JMenuItem();
		popupIVMenu = new javax.swing.JPopupMenu();
		amendIVItem = new javax.swing.JMenuItem();
		reviewIVItem = new javax.swing.JMenuItem();
		jSeparator4 = new javax.swing.JSeparator();
		cutIVItem = new javax.swing.JMenuItem();
		copyIVItem = new javax.swing.JMenuItem();
		pasteIVItem = new javax.swing.JMenuItem();
		jSeparator7 = new javax.swing.JSeparator();
		deleteIVItem = new javax.swing.JMenuItem();
		disableIVItem = new javax.swing.JMenuItem();
		jSeparator5 = new javax.swing.JSeparator();
		upIVItem = new javax.swing.JMenuItem();
		downIVItem = new javax.swing.JMenuItem();
		jButton1 = new javax.swing.JButton();
		jPanel3 = new javax.swing.JPanel();
		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		printButton = new javax.swing.JButton();
		jPanel7 = new javax.swing.JPanel();
		jPanel8 = new javax.swing.JPanel();
		jLabel4 = new javax.swing.JLabel();
		findCodeText = new javax.swing.JTextField();
		jLabel5 = new javax.swing.JLabel();
		findDescriptionText = new javax.swing.JTextField();
		jPanel4 = new javax.swing.JPanel();
		jPanel5 = new javax.swing.JPanel();
		jLabel2 = new javax.swing.JLabel();
		createNodeButton = new javax.swing.JButton();
		createInputButton = new javax.swing.JButton();
		createVariableButton = new javax.swing.JButton();
		jLabel3 = new javax.swing.JLabel();
		upButton = new javax.swing.JButton();
		downButton = new javax.swing.JButton();
		jPanel6 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		levelText = new javax.swing.JTextField();
		amendButton = new javax.swing.JButton();
		reviewButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();
		disableButton = new javax.swing.JButton();
		dependenceButton = new javax.swing.JButton();
		formulaRepositoryButton = new javax.swing.JButton();
		scrollPane = new javax.swing.JScrollPane();

		setTitle(ui.getString("fina2.metadata.MDTAmendAction"));
		initBaseComponents();

		popupNodeMenu.setFont(ui.getFont());
		exportNodeItem.setFont(ui.getFont());
		exportNodeItem.setAction(exportNodeAction);
		exportNodeItem.setText("Export");
		importNodeItem.setFont(ui.getFont());
		importNodeItem.setAction(importNodeAction);
		importNodeItem.setText("Import");
		amendNodeItem.setFont(ui.getFont());
		amendNodeItem.setAction(amendAction);
		popupNodeMenu.add(amendNodeItem);
		popupNodeMenu.add(exportNodeItem);
		popupNodeMenu.add(importNodeItem);

		reviewNodeItem.setFont(ui.getFont());
		reviewNodeItem.setText("Item");
		reviewNodeItem.setAction(reviewAction);
		popupNodeMenu.add(reviewNodeItem);

		popupNodeMenu.add(jSeparator1);

		createMenu.setText(ui.getString("fina2.create"));
		createMenu.setFont(ui.getFont());
		createNodeItem.setFont(ui.getFont());
		createNodeItem.setText("Item");
		createNodeItem.setAction(createNodeAction);
		createMenu.add(createNodeItem);

		createInputItem.setFont(ui.getFont());
		createInputItem.setText("Item");
		createInputItem.setAction(createInputAction);
		createMenu.add(createInputItem);

		createVariableItem.setFont(ui.getFont());
		createVariableItem.setText("Item");
		createVariableItem.setAction(createVariableAction);
		createMenu.add(createVariableItem);

		popupNodeMenu.add(createMenu);

		popupNodeMenu.add(jSeparator2);

		cutNodeItem.setFont(ui.getFont());
		cutNodeItem.setText(ui.getString("fina2.cut"));
		cutNodeItem.setIcon(ui.getIcon("fina2.cut"));
		cutNodeItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cutIVItemActionPerformed(evt);
			}
		});

		popupNodeMenu.add(cutNodeItem);

		copyNodeItem.setFont(ui.getFont());
		copyNodeItem.setText(ui.getString("fina2.copy"));
		copyNodeItem.setIcon(ui.getIcon("fina2.copy"));
		copyNodeItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				copyIVItemActionPerformed(evt);
			}
		});

		// Removed Copy command frm menu as it's not implemented yet
		// popupNodeMenu.add(copyNodeItem);

		pasteNodeItem.setFont(ui.getFont());
		pasteNodeItem.setText(ui.getString("fina2.paste"));
		pasteNodeItem.setIcon(ui.getIcon("fina2.paste"));
		pasteNodeItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				pasteIVItemActionPerformed(evt);
			}
		});

		popupNodeMenu.add(pasteNodeItem);

		popupNodeMenu.add(jSeparator6);

		deleteNodeItem.setFont(ui.getFont());
		deleteNodeItem.setText("Item");
		deleteNodeItem.setAction(deleteAction);
		popupNodeMenu.add(deleteNodeItem);

		disableNodeItem.setFont(ui.getFont());
		disableNodeItem.setText("Item");
		disableNodeItem.setAction(disableAction);
		popupNodeMenu.add(disableNodeItem);

		popupNodeMenu.add(jSeparator3);

		upNodeItem.setFont(ui.getFont());
		upNodeItem.setText("Item");
		upNodeItem.setAction(upAction);
		popupNodeMenu.add(upNodeItem);

		downNodeItem.setFont(ui.getFont());
		downNodeItem.setText("Item");
		downNodeItem.setAction(downAction);
		popupNodeMenu.add(downNodeItem);

		popupIVMenu.setFont(ui.getFont());
		amendIVItem.setFont(ui.getFont());

		amendIVItem.setAction(amendAction);
		popupIVMenu.add(amendIVItem);

		reviewIVItem.setFont(ui.getFont());
		reviewIVItem.setText("Item");
		reviewIVItem.setAction(reviewAction);
		popupIVMenu.add(reviewIVItem);

		popupIVMenu.add(jSeparator4);

		cutIVItem.setFont(ui.getFont());
		cutIVItem.setText(ui.getString("fina2.cut"));
		cutIVItem.setIcon(ui.getIcon("fina2.cut"));
		cutIVItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cutIVItemActionPerformed(evt);
			}
		});

		popupIVMenu.add(cutIVItem);

		copyIVItem.setFont(ui.getFont());
		copyIVItem.setText(ui.getString("fina2.copy"));
		copyIVItem.setIcon(ui.getIcon("fina2.copy"));
		copyIVItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				copyIVItemActionPerformed(evt);
			}
		});

		popupIVMenu.add(copyIVItem);

		pasteIVItem.setFont(ui.getFont());
		pasteIVItem.setText(ui.getString("fina2.paste"));
		pasteIVItem.setIcon(ui.getIcon("fina2.paste"));
		pasteIVItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				pasteIVItemActionPerformed(evt);
			}
		});

		popupIVMenu.add(pasteIVItem);

		popupIVMenu.add(jSeparator7);

		deleteIVItem.setFont(ui.getFont());
		deleteIVItem.setText("Item");
		deleteIVItem.setAction(deleteAction);
		popupIVMenu.add(deleteIVItem);

		disableIVItem.setFont(ui.getFont());
		disableIVItem.setText("Item");
		disableIVItem.setAction(disableAction);
		popupIVMenu.add(disableIVItem);

		popupIVMenu.add(jSeparator5);

		upIVItem.setFont(ui.getFont());
		upIVItem.setText("Item");
		upIVItem.setAction(upAction);
		popupIVMenu.add(upIVItem);

		downIVItem.setFont(ui.getFont());
		downIVItem.setText("Item");
		downIVItem.setAction(downAction);
		popupIVMenu.add(downIVItem);

		jButton1.setText("jButton1");

		setFont(ui.getFont());
		jPanel3.setLayout(new java.awt.BorderLayout());

		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "Meta_Data_Tree");
		} else {
			helpButton.setEnabled(false);
		}
		jPanel1.add(helpButton);

		jPanel3.add(jPanel1, java.awt.BorderLayout.WEST);

		printButton.setFont(ui.getFont());
		printButton.setAction(printAction);
		jPanel2.add(printButton);

		refreshButton.setFont(ui.getFont());
		refreshButton.setAction(refreshAction);
		jPanel2.add(refreshButton);

		jPanel2.add(closeButton);

		jPanel3.add(jPanel2, java.awt.BorderLayout.EAST);

		jPanel7.setLayout(new java.awt.BorderLayout());

		jPanel8.setLayout(new java.awt.GridBagLayout());

		jPanel8.setBorder(new javax.swing.border.EtchedBorder());
		jLabel4.setText(ui.getString("fina2.code"));
		jLabel4.setFont(ui.getFont());
		jPanel8.add(jLabel4, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, java.awt.GridBagConstraints.WEST, -1, new java.awt.Insets(5, 5, 5, 0)));

		findCodeText.setColumns(10);
		findCodeText.setFont(ui.getFont());
		findCodeText.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				findCodeTextKeyPressed(evt);
			}
		});

		jPanel8.add(findCodeText, UIManager.getGridBagConstraints(1, 0, -1, -1, -1, -1, java.awt.GridBagConstraints.WEST, -1, new java.awt.Insets(5, 5, 5, 0)));

		jLabel5.setText(ui.getString("fina2.description"));
		jLabel5.setFont(ui.getFont());
		jPanel8.add(jLabel5, UIManager.getGridBagConstraints(2, 0, -1, -1, -1, -1, java.awt.GridBagConstraints.WEST, -1, new java.awt.Insets(5, 5, 5, 0)));

		findDescriptionText.setColumns(24);
		findDescriptionText.setFont(ui.getFont());
		findDescriptionText.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				findDescriptionTextKeyPressed(evt);
			}
		});
		jPanel8.add(findDescriptionText, UIManager.getGridBagConstraints(3, 0, -1, -1, -1, -1, java.awt.GridBagConstraints.WEST, -1, new java.awt.Insets(5, 5, 5, 0)));

		jPanel7.add(jPanel8, java.awt.BorderLayout.WEST);

		jPanel3.add(jPanel7, java.awt.BorderLayout.NORTH);

		getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

		jPanel4.setLayout(new java.awt.BorderLayout());

		jPanel4.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 5, 1, 5)));
		jPanel5.setLayout(new java.awt.GridBagLayout());

		jLabel2.setText(ui.getString("fina2.create"));
		jLabel2.setFont(ui.getFont());
		jPanel5.add(jLabel2, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(15, 0, 5, 0)));

		createNodeButton.setFont(ui.getFont());
		createNodeButton.setAction(createNodeAction);
		createNodeButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel5.add(createNodeButton, UIManager.getGridBagConstraints(0, 2, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, null));

		createInputButton.setFont(ui.getFont());
		createInputButton.setAction(createInputAction);
		createInputButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel5.add(createInputButton, UIManager.getGridBagConstraints(0, 3, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		createVariableButton.setFont(ui.getFont());
		createVariableButton.setAction(createVariableAction);
		createVariableButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel5.add(createVariableButton, UIManager.getGridBagConstraints(0, 4, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		jLabel3.setText(ui.getString("fina2.sequence"));
		jLabel3.setFont(ui.getFont());
		jPanel5.add(jLabel3, UIManager.getGridBagConstraints(0, 9, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(15, 0, 5, 0)));

		upButton.setFont(ui.getFont());
		upButton.setAction(upAction);
		upButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel5.add(upButton, UIManager.getGridBagConstraints(0, 10, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, null));

		downButton.setFont(ui.getFont());
		downButton.setAction(downAction);
		downButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel5.add(downButton, UIManager.getGridBagConstraints(0, 11, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		jLabel1.setText(ui.getString("fina2.level"));
		jLabel1.setFont(ui.getFont());
		jPanel6.add(jLabel1);

		levelText.setEditable(false);
		levelText.setColumns(6);
		levelText.setFont(ui.getFont());
		jPanel6.add(levelText);

		jPanel5.add(jPanel6, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, null));

		amendButton.setFont(ui.getFont());
		amendButton.setAction(amendAction);
		amendButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel5.add(amendButton, UIManager.getGridBagConstraints(0, 5, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(15, 0, 0, 0)));

		reviewButton.setFont(ui.getFont());
		reviewButton.setAction(reviewAction);
		reviewButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel5.add(reviewButton, UIManager.getGridBagConstraints(0, 6, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		deleteButton.setFont(ui.getFont());
		deleteButton.setAction(deleteAction);
		deleteButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel5.add(deleteButton, UIManager.getGridBagConstraints(0, 7, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(15, 0, 0, 0)));

		disableButton.setFont(ui.getFont());
		disableButton.setAction(disableAction);
		disableButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel5.add(disableButton, UIManager.getGridBagConstraints(0, 8, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		dependenceButton.setFont(ui.getFont());
		dependenceButton.setText(ui.getString("fina2.metadata.dependencies"));
		dependenceButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				dependenceButtonActionPerformed(evt);
			}
		});
		jPanel5.add(dependenceButton, UIManager.getGridBagConstraints(0, 12, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(15, 0, 0, 0)));

		formulaRepositoryButton.setFont(ui.getFont());
		formulaRepositoryButton.setText(ui.getString("fina2.metadata.fr"));
		jPanel5.add(formulaRepositoryButton, UIManager.getGridBagConstraints(0, 13, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		jPanel4.add(jPanel5, java.awt.BorderLayout.NORTH);

		getContentPane().add(jPanel4, java.awt.BorderLayout.EAST);

		getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

	}// GEN-END:initComponents

	private void dependenceButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_dependenceButtonActionPerformed
		dependDialog.show(tree.getSelectedNode());
	}// GEN-LAST:event_dependenceButtonActionPerformed

	private void pasteIVItemActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_pasteIVItemActionPerformed

		if (pasteNode == null) {
			return;
		}

		Node node = tree.getSelectedNode();
		if (node == null) {
			return;
		}

		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/metadata/MDTSession");
			MDTSessionHome home = (MDTSessionHome) PortableRemoteObject.narrow(ref, MDTSessionHome.class);
			MDTSession session = home.create();

			session.copyPaste((MDTNodePK) pasteNode.getPrimaryKey(), (MDTNodePK) node.getPrimaryKey(), cut);

			DefaultMutableTreeNode treeNode = tree.getSelectedTreeNode();

			boolean childsRetrieved = getChildNodes(treeNode);

			((DefaultTreeModel) tree.getModel()).removeNodeFromParent(pasteTreeNode);

			if (childsRetrieved == false) {
				((DefaultTreeModel) tree.getModel()).insertNodeInto(pasteTreeNode, treeNode, treeNode.getChildCount());
			}
			tree.gotoNode((MDTNodePK) pasteNode.getPrimaryKey());
			tree.scrollPathToVisible(new TreePath(pasteTreeNode.getPath()));

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private void copyIVItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_copyIVItemActionPerformed
		pasteNode = tree.getSelectedNode();
		if (pasteNode == null)
			return;
		cut = false;
		// System.out.println("copy "+ pasteNode);
		pasteNodeItem.setEnabled(false);
		pasteIVItem.setEnabled(false);
	}// GEN-LAST:event_copyIVItemActionPerformed

	private void cutIVItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-
		// FIRST
		// :
		// event_cutIVItemActionPerformed
		pasteTreeNode = tree.getSelectedTreeNode();
		pasteNode = tree.getSelectedNode();
		if (pasteNode == null)
			return;
		cut = true;
		// System.out.println("cut "+ pasteNode);
		pasteNodeItem.setEnabled(false);
		pasteIVItem.setEnabled(false);
	}// GEN-LAST:event_cutIVItemActionPerformed

	private void findDescriptionTextKeyPressed(java.awt.event.KeyEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_findDescriptionTextKeyPressed
		if (evt.getKeyCode() != evt.VK_ENTER)
			return;

		String desc = findDescriptionText.getText().trim();
		if (desc.equals(""))
			return;
		dontExpand = true;
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/metadata/MDTNode");
			MDTNodeHome homeEn = (MDTNodeHome) PortableRemoteObject.narrow(ref, MDTNodeHome.class);

			ref = jndi.lookup("fina2/metadata/MDTSession");
			MDTSessionHome homeSs = (MDTSessionHome) PortableRemoteObject.narrow(ref, MDTSessionHome.class);

			MDTNode node = (MDTNode) homeEn.findByDescription(main.getLanguageHandle(), desc);
			MDTSession session = homeSs.create();

			Collection parents = session.getParentNodes((MDTNodePK) (node.getPrimaryKey()));

			Object[] pkO = parents.toArray();
			for (int i = pkO.length - 1; i >= 0; i--) {
				loadSearchNodes((MDTNodePK) pkO[i]);
			}
			tree.gotoNode((MDTNodePK) node.getPrimaryKey());

		} catch (FinderException e) {
			e.printStackTrace();
			Main.errorHandler(null, Main.getString("fina2.title"), Main.getString("fina2.metadata.nodeNotFound"));
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		dontExpand = false;
	}// GEN-LAST:event_findDescriptionTextKeyPressed

	private synchronized void loadSearchNodes(MDTNodePK parents) {
		try {
			tree.gotoNode(parents);
			Node actNode = tree.getSelectedNode();
			DefaultMutableTreeNode actParent = tree.getSelectedTreeNode(); // new
			// DefaultMutableTreeNode
			// (
			// actNode
			// )
			// ;
			if ((actParent.getChildCount() > 0) && (((MDTNodePK) ((Node) ((DefaultMutableTreeNode) actParent.getFirstChild()).getUserObject()).getPrimaryKey()).getId() == -1)) {
				tree.expandPath(tree.getSelectionPath().pathByAddingChild(actParent.getFirstChild()));
				try {
					InitialContext jndi = fina2.Main.getJndiContext();
					Object ref = jndi.lookup("fina2/metadata/MDTSession");
					MDTSessionHome home = (MDTSessionHome) PortableRemoteObject.narrow(ref, MDTSessionHome.class);

					MDTSession session = home.create();

					Collection nodes = session.getChildNodes(main.getUserHandle(), main.getLanguageHandle(), parents);

					prepareNodes((javax.swing.tree.DefaultTreeModel) tree.getModel(), actParent, nodes);
					((javax.swing.tree.DefaultTreeModel) tree.getModel()).removeNodeFromParent(((DefaultMutableTreeNode) actParent.getFirstChild()));

				} catch (Exception e) {
					Main.generalErrorHandler(e);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void findCodeTextKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST
		// :
		// event_findCodeTextKeyPressed
		if (evt.getKeyCode() != evt.VK_ENTER)
			return;

		String code = findCodeText.getText().trim();
		if (code.equals(""))
			return;
		findNode(code, false);
	}// GEN-LAST:event_findCodeTextKeyPressed

	protected void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		formComponentHidden(null);
		dispose();
	}

	/** Exit the Application */
	private void exitForm(java.awt.event.WindowEvent evt) {// GEN-FIRST:
		// event_exitForm
		hide();
	}// GEN-LAST:event_exitForm

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton amendButton;
	private javax.swing.JMenuItem amendIVItem;
	private javax.swing.JMenuItem amendNodeItem;
	private javax.swing.JMenuItem copyIVItem;
	private javax.swing.JMenuItem copyNodeItem;
	private javax.swing.JButton createInputButton;
	private javax.swing.JMenuItem createInputItem;
	private javax.swing.JMenu createMenu;
	private javax.swing.JButton createNodeButton;
	private javax.swing.JMenuItem exportNodeItem;
	private javax.swing.JMenuItem importNodeItem;
	private javax.swing.JMenuItem createNodeItem;
	private javax.swing.JButton createVariableButton;
	private javax.swing.JMenuItem createVariableItem;
	private javax.swing.JMenuItem cutIVItem;
	private javax.swing.JMenuItem cutNodeItem;
	private javax.swing.JButton deleteButton;
	private javax.swing.JMenuItem deleteIVItem;
	private javax.swing.JMenuItem deleteNodeItem;
	private javax.swing.JButton dependenceButton;
	private javax.swing.JButton disableButton;
	private javax.swing.JMenuItem disableIVItem;
	private javax.swing.JMenuItem disableNodeItem;
	private javax.swing.JButton downButton;
	private javax.swing.JMenuItem downIVItem;
	private javax.swing.JMenuItem downNodeItem;
	private javax.swing.JTextField findCodeText;
	private javax.swing.JTextField findDescriptionText;
	private javax.swing.JButton formulaRepositoryButton;
	private javax.swing.JButton jButton1;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JSeparator jSeparator2;
	private javax.swing.JSeparator jSeparator3;
	private javax.swing.JSeparator jSeparator4;
	private javax.swing.JSeparator jSeparator5;
	private javax.swing.JSeparator jSeparator6;
	private javax.swing.JSeparator jSeparator7;
	private javax.swing.JTextField levelText;
	private javax.swing.JMenuItem pasteIVItem;
	private javax.swing.JMenuItem pasteNodeItem;
	private javax.swing.JPopupMenu popupIVMenu;
	private javax.swing.JPopupMenu popupNodeMenu;
	private javax.swing.JButton printButton;
	private javax.swing.JButton reviewButton;
	private javax.swing.JMenuItem reviewIVItem;
	private javax.swing.JMenuItem reviewNodeItem;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JButton upButton;
	private javax.swing.JMenuItem upIVItem;
	private javax.swing.JMenuItem upNodeItem;
	// End of variables declaration//GEN-END:variables

}

@SuppressWarnings("serial")
class ExportNodeAction extends AbstractAction {
	private fina2.Main main = fina2.Main.main;
	private EJBTree tree;
	private JFileChooser fc = null;
	private MDTAmendFrame frame;
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private IndeterminateLoading loading;

	public ExportNodeAction() {

	}

	public ExportNodeAction(MDTAmendFrame frame, EJBTree tree) {
		this.frame = frame;
		this.tree = tree;
		putValue(AbstractAction.NAME, ui.getString("fina2.exportMdt"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.exportMdt"));
		loading = ui.createIndeterminateLoading(main.getMainFrame());
	}

	public void actionPerformed(java.awt.event.ActionEvent ev) {
		// System.out.println("Selected Node " +
		// tree.getSelectedNode().getLabel() + "\n");

		fc = new JFileChooser();
		fc.setDialogTitle("Select Directory To Export");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setApproveButtonText("Export");
		fc.showOpenDialog(frame);
		Thread t = new Thread() {
			public void run() {
				try {
					loading.start();
					InitialContext jndi = fina2.Main.getJndiContext();
					Object ref = jndi.lookup("fina2/metadata/MDTSession");
					MDTSessionHome home = (MDTSessionHome) PortableRemoteObject.narrow(ref, MDTSessionHome.class);
					MDTSession session = home.create();
					MDTNodePK selectedNodePk = new MDTNodePK(((MDTNodePK) tree.getSelectedNode().getPrimaryKey()).getId());
					List<MDTNodeData> exportedNodes = session.getAllSubTreeWithParent(selectedNodePk);

					// for (int i = 0; i < exportedNodes.size(); i++) {
					// System.out.println(exportedNodes.get(i).getDESCRIPTIONS().getDESCRIPTION().get(1).getVALUE());
					// }

					File f = new File(fc.getSelectedFile().getAbsolutePath() + System.getProperties().getProperty("file.separator") + "exportedMdt-" + exportedNodes.get(0).getCODE().trim() + ".xml");
					fina2.Main m = fina2.Main.main;
					String exported = session.exportMDT(exportedNodes, m.getLanguageHandle());
					System.out.println(exported);
					String encoding = ((Language) m.getLanguageHandle().getEJBObject()).getXmlEncoding();
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), encoding));
					bw.write(exported);

					bw.close();

					System.out.println("DONE");

					loading.stop();
				} catch (Exception ex) {
					Main.generalErrorHandler(ex);
					loading.stop();
				}
			}
		};
		t.start();

	}
}

@SuppressWarnings("serial")
class ImportNodeAction extends AbstractAction {
	private fina2.Main main = fina2.Main.main;
	private EJBTree tree;
	private MDTAmendFrame frame;
	private JFileChooser fc;
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private IndeterminateLoading loading;

	public ImportNodeAction() {

	}

	public ImportNodeAction(MDTAmendFrame frame, EJBTree tree) {
		this.tree = tree;
		this.frame = frame;
		putValue(AbstractAction.NAME, ui.getString("fina2.importMdt"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.importMdt"));
		loading = ui.createIndeterminateLoading(main.getMainFrame());
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
		fc = new JFileChooser();
		fc.setDialogTitle("Select MDT To Import");
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setApproveButtonText("Import");
		fc.showOpenDialog(frame);

		Thread t = new Thread() {
			public void run() {
				try {
					loading.start();
					InitialContext jndi = fina2.Main.getJndiContext();
					Object ref = jndi.lookup("fina2/metadata/MDTSession");
					MDTSessionHome home = (MDTSessionHome) PortableRemoteObject.narrow(ref, MDTSessionHome.class);

					MDTSession session = home.create();
					MDTNodePK selectedNodePk = new MDTNodePK(((MDTNodePK) tree.getSelectedNode().getPrimaryKey()).getId());

					File f = new File(fc.getSelectedFile().getAbsolutePath());
					String encoding = ((Language) main.getLanguageHandle().getEJBObject()).getXmlEncoding();

					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), encoding));
					StringBuilder s = new StringBuilder();
					String tmp = "";

					while (((tmp = br.readLine()) != null) && (tmp.length() != 0)) {
						s.append(tmp);
						s.append("\n");
					}
					
					session.importMDT(s.toString(), selectedNodePk, encoding);

					s = null;
					frame.initTree();

					loading.stop();
				} catch (Exception ex) {
					ex.printStackTrace();
					loading.stop();
				}
			}
		};
		t.start();

	}
}

class CreateNodeAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTree tree;
	private MDTNodeAmendDialog dialog;

	CreateNodeAction(java.awt.Frame parent, EJBTree tree) {
		this.tree = tree;
		dialog = new MDTNodeAmendDialog(parent, true);
		putValue(AbstractAction.NAME, ui.getString("fina2.node"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.node"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		try {
			Node node = tree.getSelectedNode();

			MDTNodePK pk = ((MDTNodePK) node.getPrimaryKey());
			if (pk.getId() != 0) {
				MDTNode mdtNode;
				InitialContext jndi = fina2.Main.getJndiContext();

				Object ref = jndi.lookup("fina2/metadata/MDTNode");
				MDTNodeHome home = (MDTNodeHome) PortableRemoteObject.narrow(ref, MDTNodeHome.class);

				pk = (MDTNodePK) node.getPrimaryKey();
				mdtNode = home.findByPrimaryKey(pk);
			}
			if (node == null) {
				return;
			}

			if (((Integer) node.getType()).intValue() != MDTConstants.NODETYPE_NODE && ((Integer) node.getType()).intValue() != -1) {
				return;
			}

			dialog.show(tree, node, null, true);
		} catch (Exception ex) {
			ui.showMessageBox(dialog, "Error during Adding Node", "Node Does Not Exist,Please Refresh", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}

	}
}

class CreateInputAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTree tree;
	private MDTInputAmendDialog dialog;

	CreateInputAction(java.awt.Frame parent, EJBTree tree) {

		this.tree = tree;
		dialog = new MDTInputAmendDialog(parent, true);
		putValue(AbstractAction.NAME, ui.getString("fina2.input"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.input"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		try {
			Node node = tree.getSelectedNode();
			MDTNodePK pk = ((MDTNodePK) node.getPrimaryKey());
			MDTNode mdtNode;
			InitialContext jndi = fina2.Main.getJndiContext();

			Object ref = jndi.lookup("fina2/metadata/MDTNode");
			MDTNodeHome home = (MDTNodeHome) PortableRemoteObject.narrow(ref, MDTNodeHome.class);

			pk = (MDTNodePK) node.getPrimaryKey();
			mdtNode = home.findByPrimaryKey(pk);
			if (node == null) {
				return;
			}

			if (((Integer) node.getType()).intValue() != MDTConstants.NODETYPE_NODE && ((Integer) node.getType()).intValue() != -1) {
				return;
			}

			dialog.show(tree, node, null, true);
		} catch (Exception ex) {

			ui.showMessageBox(dialog, "Error during Adding Node", "Node Does Not Exist,Please Refresh", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}
}

class CreateVariableAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTree tree;
	private MDTVariableAmendDialog dialog;

	CreateVariableAction(java.awt.Frame parent, EJBTree tree) {
		this.tree = tree;
		dialog = new MDTVariableAmendDialog(parent, true);
		putValue(AbstractAction.NAME, ui.getString("fina2.variable"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.variable"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		boolean nodeExists = true;
		Node node = tree.getSelectedNode();
		try {
			MDTNodePK pk = ((MDTNodePK) node.getPrimaryKey());
			MDTNode mdtNode;
			InitialContext jndi = fina2.Main.getJndiContext();

			Object ref = jndi.lookup("fina2/metadata/MDTNode");
			MDTNodeHome home = (MDTNodeHome) PortableRemoteObject.narrow(ref, MDTNodeHome.class);

			pk = (MDTNodePK) node.getPrimaryKey();
			mdtNode = home.findByPrimaryKey(pk);

			if (node == null) {
				return;
			}

			if (((Integer) node.getType()).intValue() != MDTConstants.NODETYPE_NODE && ((Integer) node.getType()).intValue() != -1) {
				return;
			}

			dialog.show(tree, node, null, true);
		} catch (Exception ex) {
			nodeExists = false;
			ui.showMessageBox(dialog, "Error during Adding Node", "Node Does Not Exist,Please Refresh", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}

	}
}

class AmendAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTree tree;
	private Node node;
	private Node newNode;
	private java.awt.Frame parent;
	private MDTNodeAmendDialog dialogNode;
	private MDTInputAmendDialog dialogInput;
	private MDTVariableAmendDialog dialogVariable;

	AmendAction(java.awt.Frame parent, EJBTree tree) {

		this.parent = parent;
		this.tree = tree;
		dialogNode = new MDTNodeAmendDialog(parent, true);
		dialogInput = new MDTInputAmendDialog(parent, true);
		dialogVariable = new MDTVariableAmendDialog(parent, true);

		putValue(AbstractAction.NAME, ui.getString("fina2.amend"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.amend"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		try {
			Node node = tree.getSelectedNode();
			MDTNodePK pk = ((MDTNodePK) node.getPrimaryKey());
			MDTNode mdtNode;
			InitialContext jndi = fina2.Main.getJndiContext();

			Object ref = jndi.lookup("fina2/metadata/MDTNode");
			MDTNodeHome home = (MDTNodeHome) PortableRemoteObject.narrow(ref, MDTNodeHome.class);

			pk = (MDTNodePK) node.getPrimaryKey();
			mdtNode = home.findByPrimaryKey(pk);

			if (node == null)
				return;
			if (((Integer) node.getType()).intValue() == MDTConstants.NODETYPE_NODE)
				dialogNode.show(tree, null, node, true);
			if (((Integer) node.getType()).intValue() == MDTConstants.NODETYPE_INPUT)
				dialogInput.show(tree, null, node, true);
			if (((Integer) node.getType()).intValue() == MDTConstants.NODETYPE_VARIABLE)
				dialogVariable.show(tree, null, node, true);

			((javax.swing.tree.DefaultTreeModel) tree.getModel()).nodeChanged(tree.getSelectedTreeNode());
		} catch (Exception ex) {
			ui.showMessageBox(parent, "Error during Amend", "Node Does Not Exist,Please Refresh", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}
}

class ReviewAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTree tree;
	private Node node;
	private Node newNode;
	private java.awt.Frame parent;
	private MDTNodeAmendDialog dialogNode;
	private MDTInputAmendDialog dialogInput;
	private MDTVariableAmendDialog dialogVariable;

	ReviewAction(java.awt.Frame parent, EJBTree tree) {
		this.tree = tree;

		dialogNode = new MDTNodeAmendDialog(parent, true);
		dialogInput = new MDTInputAmendDialog(parent, true);
		dialogVariable = new MDTVariableAmendDialog(parent, true);

		putValue(AbstractAction.NAME, ui.getString("fina2.review"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.review"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		try {
			Node node = tree.getSelectedNode();
			MDTNodePK pk = ((MDTNodePK) node.getPrimaryKey());
			MDTNode mdtNode;
			InitialContext jndi = fina2.Main.getJndiContext();

			Object ref = jndi.lookup("fina2/metadata/MDTNode");
			MDTNodeHome home = (MDTNodeHome) PortableRemoteObject.narrow(ref, MDTNodeHome.class);

			pk = (MDTNodePK) node.getPrimaryKey();
			mdtNode = home.findByPrimaryKey(pk);

			if (node == null)
				return;
			if (((Integer) node.getType()).intValue() == MDTConstants.NODETYPE_NODE)
				dialogNode.show(tree, null, node, false);
			if (((Integer) node.getType()).intValue() == MDTConstants.NODETYPE_INPUT)
				dialogInput.show(tree, null, node, false);
			if (((Integer) node.getType()).intValue() == MDTConstants.NODETYPE_VARIABLE)
				dialogVariable.show(tree, null, node, false);
		} catch (Exception ex) {
			ui.showMessageBox(parent, "Error during Amend", "Node Does Not Exist,Please Refresh", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
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

		putValue(AbstractAction.NAME, ui.getString("fina2.delete"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.delete"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		Node node = tree.getSelectedNode();
		if (node == null)
			return;

		if (!ui.showConfirmBox(parent, ui.getString("fina2.metadata.itemDeleteQuestion")))
			return;

		try {
			InitialContext jndi = fina2.Main.getJndiContext();

			Object ref = jndi.lookup("fina2/metadata/MDTNode");
			MDTNodeHome home = (MDTNodeHome) PortableRemoteObject.narrow(ref, MDTNodeHome.class);

			MDTNodePK pk = (MDTNodePK) node.getPrimaryKey();
			MDTNode mdtNode = home.findByPrimaryKey(pk);

			mdtNode.remove();

			((javax.swing.tree.DefaultTreeModel) tree.getModel()).removeNodeFromParent(tree.getSelectedTreeNode());

		} catch (Exception e) {
			Main.errorHandler(parent, Main.getString("fina2.title"), Main.getString("fina2.delete.item"));
		}

	}
}

class DisableAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;
	private EJBTree tree;
	private java.awt.Frame parent;

	DisableAction(java.awt.Frame parent, EJBTree tree) {
		this.tree = tree;
		this.parent = parent;

		putValue(AbstractAction.NAME, ui.getString("fina2.disable"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.disable"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		Node node = tree.getSelectedNode();
		if (node == null)
			return;

		try {
			InitialContext jndi = fina2.Main.getJndiContext();

			Object ref = jndi.lookup("fina2/metadata/MDTNode");
			MDTNodeHome home = (MDTNodeHome) PortableRemoteObject.narrow(ref, MDTNodeHome.class);

			MDTNodePK pk = (MDTNodePK) node.getPrimaryKey();
			MDTNode mdtNode = home.findByPrimaryKey(pk);

			mdtNode.setDisabled();

			if (node.toString().startsWith("[DISABLED][")) {
				node.setLabel(node.toString().substring(10));
				putValue(AbstractAction.NAME, ui.getString("fina2.disable"));
				putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.disable"));
			} else {
				node.setLabel("[DISABLED]" + node.toString());
				putValue(AbstractAction.NAME, ui.getString("fina2.enable"));
				putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.enable"));
			}

			((javax.swing.tree.DefaultTreeModel) tree.getModel()).nodeChanged(tree.getSelectedTreeNode());

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

	UpAction(java.awt.Frame parent, EJBTree tree) {

		this.tree = tree;
		this.parent = parent;
		putValue(AbstractAction.NAME, ui.getString("fina2.up"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.up"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

		Node selectedNode = tree.getSelectedNode();

		if (selectedNode == null) {
			return;
		}

		MutableTreeNode currentNode = tree.getSelectedTreeNode();
		MutableTreeNode parentTreeNode = (MutableTreeNode) (tree.getSelectedTreeNode()).getParent();
		int nodeIndex = parentTreeNode.getIndex((MutableTreeNode) currentNode);

		if (nodeIndex == 0) {
			// Can't move selected node upper
			return;
		}

		try {

			//
			// Get remote interface
			//

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/metadata/MDTSession");
			MDTSessionHome home = (MDTSessionHome) PortableRemoteObject.narrow(ref, MDTSessionHome.class);
			MDTSession session = home.create();

			//
			// Process the current node
			//

			MDTNodePK nodePK = (MDTNodePK) selectedNode.getPrimaryKey();
			long nodeId = nodePK.getId();
			int seq = session.getNodeSequence(nodeId);
			session.setNodeSequence(nodeId, seq - 1);

			//
			// Process the previous node
			//

			MutableTreeNode parentNode = (MutableTreeNode) (tree.getSelectedTreeNode()).getParent();
			DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) parentNode.getChildAt(nodeIndex - 1);
			Node node = (Node) prevNode.getUserObject();
			nodePK = (MDTNodePK) node.getPrimaryKey();
			nodeId = nodePK.getId();
			session.setNodeSequence(nodeId, seq);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

		//
		// Update GUI
		//

		if (nodeIndex != 0) {
			((javax.swing.tree.DefaultTreeModel) tree.getModel()).removeNodeFromParent(currentNode);
			((javax.swing.tree.DefaultTreeModel) tree.getModel()).insertNodeInto(currentNode, parentTreeNode, nodeIndex - 1);
			tree.setSelectionPath(new TreePath(((DefaultMutableTreeNode) currentNode).getPath()));
		}

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

		Node selectedNode = tree.getSelectedNode();

		if (selectedNode == null) {
			return;
		}

		MutableTreeNode currentNode = tree.getSelectedTreeNode();
		MutableTreeNode parentTreeNode = (MutableTreeNode) (tree.getSelectedTreeNode()).getParent();
		int nodeIndex = parentTreeNode.getIndex((MutableTreeNode) currentNode);

		int childCount = parentTreeNode.getChildCount();
		if (nodeIndex == (childCount - 1)) {
			// Can't move selected node below
			return;
		}

		try {

			//
			// Get remote interface
			//

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/metadata/MDTSession");
			MDTSessionHome home = (MDTSessionHome) PortableRemoteObject.narrow(ref, MDTSessionHome.class);
			MDTSession session = home.create();

			//
			// Process the current node
			//

			MDTNodePK nodePK = (MDTNodePK) selectedNode.getPrimaryKey();
			long nodeId = nodePK.getId();
			int seq = session.getNodeSequence(nodeId);
			session.setNodeSequence(nodeId, seq + 1);

			//
			// Process the next node
			//

			MutableTreeNode parentNode = (MutableTreeNode) (tree.getSelectedTreeNode()).getParent();
			DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) parentNode.getChildAt(nodeIndex + 1);
			Node node = (Node) prevNode.getUserObject();
			nodePK = (MDTNodePK) node.getPrimaryKey();
			nodeId = nodePK.getId();
			session.setNodeSequence(nodeId, seq);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

		//
		// Update GUI
		//
		if (parentTreeNode.getChildCount() - 1 > nodeIndex) {
			((javax.swing.tree.DefaultTreeModel) tree.getModel()).removeNodeFromParent(currentNode);
			((javax.swing.tree.DefaultTreeModel) tree.getModel()).insertNodeInto(currentNode, parentTreeNode, nodeIndex + 1);
			tree.setSelectionPath(new TreePath(((DefaultMutableTreeNode) currentNode).getPath()));
		}

	}
}

class PrintAction extends AbstractAction {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTree tree;
	private TreeReviewFrame treePrintFrame;

	PrintAction(java.awt.Frame parent, EJBTree tree) {
		this.tree = tree;
		putValue(AbstractAction.NAME, ui.getString("fina2.print"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.print"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		treePrintFrame = new TreeReviewFrame();
		treePrintFrame.show(treePrintFrame.getTitle(), tree);
	}
}

class RefreshAction extends AbstractAction {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private MDTAmendFrame frame;

	RefreshAction(java.awt.Frame parent, MDTAmendFrame frame, EJBTree tree) {
		this.frame = frame;
		putValue(AbstractAction.NAME, ui.getString("fina2.refresh"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.refresh"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		frame.initTree();
	}
}
