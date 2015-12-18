package fina2.regions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import fina2.BaseFrame;
import fina2.FinaTypeException;
import fina2.Main;
import fina2.regions.RegionStructureTree.PrintData;
import fina2.ui.MessageBox;
import fina2.ui.UIManager;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableReviewFrame;
import fina2.ui.tree.Node;

@SuppressWarnings("serial")
public class RegionStructureFrame extends BaseFrame {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private enum CreateAmendAndReview {
		CREATE, AMEND, REVIEW
	}

	private Container container;

	private JTextField searchField;

	private JPanel eastPanel;
	private JPanel eastComponentPanel;
	private JPanel southPanel;
	private JPanel spacer;

	private JButton createButton;
	private JButton reviewButton;
	private JButton amendButton;
	private JButton deleteButton;
	private JButton upButton;
	private JButton downButton;
	private JButton propertiesButton;

	private JList propertiesList;

	private RegionStructureTree regionStructureTree = new RegionStructureTree();

	public RegionStructureFrame() {
		// load icons
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.print", "print.gif");
		ui.loadIcon("fina2.refresh", "refresh.gif");
		ui.loadIcon("fina2.close", "cancel.gif");

		ui.loadIcon("fina2.amend", "amend.gif");
		ui.loadIcon("fina2.create", "insert.gif");
		ui.loadIcon("fina2.review", "review.gif");
		ui.loadIcon("fina2.delete", "delete.gif");
		ui.loadIcon("fina2.regions.properties", "properties.png");

		ui.loadIcon("fina2.country", "region.png");
		ui.loadIcon("fina2.state", "state.gif");
		ui.loadIcon("fina2.city", "city.gif");
		ui.loadIcon("fina2.other.regions", "other-regions.png");

		// initial GUI Components
		initComponents();

		BaseFrame.ensureVisible(this);
	}

	@Override
	public void show() {
		if (!isVisible()) {
			// initial Tree
			regionStructureTree.initTree();
			super.show();
		}
	}

	private void initComponents() {
		BaseFrame.ensureVisible(this);
		this.setBounds(100, 100, 500, 300);
		initBaseComponents();
		container = this.getContentPane();
		container.setLayout(new BorderLayout());
		this.setFont(ui.getFont());

		// set Title
		this.setTitle(ui.getString("fina2.bank.city&region"));

		// add Tree
		container.add(new JScrollPane(regionStructureTree), BorderLayout.CENTER);

		regionStructureTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				isRootSelection();
			}
		});
		regionStructureTree.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				treeKeyListeners(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				treeKeyListeners(e);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				treeKeyListeners(e);
			}
		});

		eastPanel = new JPanel();
		eastComponentPanel = new JPanel();

		// initial Base Buttons
		southPanel = new JPanel(new BorderLayout());

		JPanel helpPanel = new JPanel();
		helpPanel.add(helpButton);
		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "Sities_Regions");
		} else {
			helpButton.setEnabled(false);
		}
		southPanel.add(helpPanel, BorderLayout.WEST);

		JPanel baseButtonPanel = new JPanel();
		baseButtonPanel.add(printButton);
		baseButtonPanel.add(refreshButton);
		baseButtonPanel.add(closeButton);

		southPanel.add(baseButtonPanel, BorderLayout.EAST);
		container.add(southPanel, BorderLayout.SOUTH);

		eastPanel.setLayout(new BorderLayout(20, 0));
		eastComponentPanel.setBorder(new EmptyBorder(6, 5, 7, 5));
		eastComponentPanel.setLayout(new GridBagLayout());
		((GridBagLayout) eastComponentPanel.getLayout()).rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		((GridBagLayout) eastComponentPanel.getLayout()).rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4 };

		// Search Fields
		JLabel searchLabel = new JLabel(ui.getString("fina2.web.search"));
		searchField = new JTextField(8);
		eastComponentPanel.add(searchLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
		eastComponentPanel.add(searchField, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
		searchField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				searchEvent(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				searchEvent(e);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				searchEvent(e);
			}
		});

		// ----spacer----
		spacer(eastComponentPanel, 2);

		// ---- createButton ----
		createButton = new JButton(ui.getIcon("fina2.create"));
		createButton.setText(ui.getString("fina2.create"));
		eastComponentPanel.add(createButton, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
		createButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new CreateAndReviewRegionDialog(main.getMainFrame(), regionStructureTree, CreateAmendAndReview.CREATE);
			}
		});

		// ---- amendButton ----
		amendButton = new JButton(ui.getIcon("fina2.amend"));
		amendButton.setText(ui.getString("fina2.amend"));
		eastComponentPanel.add(amendButton, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
		amendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new CreateAndReviewRegionDialog(main.getMainFrame(), regionStructureTree, CreateAmendAndReview.AMEND);
			}
		});

		// ---- reviewButton ----
		reviewButton = new JButton(ui.getIcon("fina2.review"));
		reviewButton.setText(ui.getString("fina2.review"));
		eastComponentPanel.add(reviewButton, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
		reviewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new CreateAndReviewRegionDialog(main.getMainFrame(), regionStructureTree, CreateAmendAndReview.REVIEW);
			}
		});

		// ---- deleteButton ----
		deleteButton = new JButton(ui.getIcon("fina2.delete"));
		deleteButton.setText(ui.getString("fina2.delete"));
		eastComponentPanel.add(deleteButton, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread deleteThread = new Thread() {
					public void run() {
						deleteAction();
					}
				};
				deleteThread.start();
			};
		});

		// ----spacer----
		spacer(eastComponentPanel, 7);

		// ---- upButton ----
		upButton = new JButton(ui.getIcon("fina2.up"));
		upButton.setText(ui.getString("fina2.up"));
		eastComponentPanel.add(upButton, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
		upButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				upAction();
			}
		});

		// ---- downButton ----
		downButton = new JButton(ui.getIcon("fina2.down"));
		downButton.setText(ui.getString("fina2.down"));
		eastComponentPanel.add(downButton, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
		downButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				downAction();
			}
		});

		// ----spacer----
		spacer(eastComponentPanel, 10);

		// ---- propertiesButton ----
		propertiesButton = new JButton(ui.getIcon("fina2.regions.properties"));
		propertiesButton.setText(ui.getString("fina2.region.properties"));
		eastComponentPanel.add(propertiesButton, new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
		propertiesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RegionStructurePropertiesDialog propertiesDialog = new RegionStructurePropertiesDialog(main.getMainFrame());
				regionStructureTree.setMaxLevel();
				Map<Integer, String> map = propertiesDialog.getPropertiesMap();
				if (map != null) {
					initPropertiesList();
					eastPanel.updateUI();
				}
				isRootSelection();
			}
		});

		// ----spacer----
		spacer(eastComponentPanel, 12);

		// Initial Properties List
		propertiesList = new JList();
		propertiesList.setFont(ui.getFont());
		propertiesList.setVisibleRowCount(3);
		propertiesList.setCellRenderer(new PropertiesListRenderer());
		propertiesList.setModel(new DefaultListModel());
		propertiesList.setSelectionForeground(Color.black);

		eastComponentPanel.add(new JScrollPane(propertiesList), new GridBagConstraints(0, 13, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// Properties List
		initPropertiesList();

		// add east Panel
		eastPanel.add(eastComponentPanel, BorderLayout.NORTH);
		container.add(eastPanel, BorderLayout.LINE_END);

		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ui.putConfigValue(RegionStructureFrame.class.getName() + ".visible", new Boolean(false));
				closeDialog();
			}
		});

		// No Selected Tree?
		if (regionStructureTree.getSelectedNode() == null) {
			setEnableAll(false);
			createButton.setEnabled(false);
		}
		this.pack();
	}

	// Space Panel
	private void spacer(JPanel panel, int index) {
		spacer = new JPanel(null);
		panel.add(spacer, new GridBagConstraints(0, index, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
	}

	// up Action
	private void upAction() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/regions/RegionStructureSession");
			RegionStructureSessionHome home = (RegionStructureSessionHome) PortableRemoteObject.narrow(ref, RegionStructureSessionHome.class);
			RegionStructureSession session = home.create();
			RegionStructureNodePK pk = (RegionStructureNodePK) regionStructureTree.getSelectedNode().getPrimaryKey();
			if (session.moveUp(pk)) {
				DefaultTreeModel treeModel = (DefaultTreeModel) regionStructureTree.getModel();
				DefaultMutableTreeNode selNode = regionStructureTree.getSelectedTreeNode();
				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selNode.getParent();
				int index = treeModel.getIndexOfChild(parentNode, selNode);
				if (index != 0) {
					((javax.swing.tree.DefaultTreeModel) treeModel).removeNodeFromParent(selNode);
					((javax.swing.tree.DefaultTreeModel) treeModel).insertNodeInto(selNode, parentNode, index - 1);
					TreePath path = new TreePath(((DefaultMutableTreeNode) selNode).getPath());
					regionStructureTree.scrollPathToVisible(path);
					regionStructureTree.setSelectionPath(path);
				}
			}
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
			ex.printStackTrace();
		}
	}

	// down Action
	private void downAction() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/regions/RegionStructureSession");
			RegionStructureSessionHome home = (RegionStructureSessionHome) PortableRemoteObject.narrow(ref, RegionStructureSessionHome.class);
			RegionStructureSession session = home.create();
			RegionStructureNodePK pk = (RegionStructureNodePK) regionStructureTree.getSelectedNode().getPrimaryKey();
			if (session.moveDown(pk)) {
				DefaultTreeModel treeModel = (DefaultTreeModel) regionStructureTree.getModel();
				DefaultMutableTreeNode selNode = regionStructureTree.getSelectedTreeNode();
				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selNode.getParent();
				int index = treeModel.getIndexOfChild(parentNode, selNode);
				if (index >= 0) {
					((javax.swing.tree.DefaultTreeModel) treeModel).removeNodeFromParent(selNode);
					((javax.swing.tree.DefaultTreeModel) treeModel).insertNodeInto(selNode, parentNode, index + 1);
					TreePath path = new TreePath(((DefaultMutableTreeNode) selNode).getPath());
					regionStructureTree.scrollPathToVisible(path);
					regionStructureTree.setSelectionPath(path);
				}
			}
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
			ex.printStackTrace();
		}
	}

	// is Root Selection
	private void isRootSelection() {
		Node node = regionStructureTree.getSelectedNode();
		RegionStructureNodePK pk = null;
		if (node == null) {
			setEnableAll(false);
		} else {
			int maxLevel = regionStructureTree.getMaxLevel();
			int level = regionStructureTree.getSelectedTreeNode().getLevel();
			pk = (RegionStructureNodePK) node.getPrimaryKey();
			if (pk.getId() == 0) {
				setEnableAll(false);
				createButton.setEnabled(true);
			} else {
				setEnableAll(true);
				if (level + 1 > maxLevel) {
					createButton.setEnabled(false);
				} else {
					createButton.setEnabled(true);
				}
			}
		}
	}

	// set Enable All Button
	private void setEnableAll(boolean enable) {
		amendButton.setEnabled(enable);
		reviewButton.setEnabled(enable);
		deleteButton.setEnabled(enable);
		upButton.setEnabled(enable);
		downButton.setEnabled(enable);
		createButton.setEnabled(enable);
	}

	// Search key Listener
	private void searchEvent(KeyEvent e) {
		regionStructureTree.getTreeSearcher().searctAndSelectTreeNode((DefaultMutableTreeNode) regionStructureTree.getModel().getRoot(), searchField.getText(), regionStructureTree);
	}

	// Tree Key Listeners
	private void treeKeyListeners(KeyEvent ev) {
		Node node = regionStructureTree.getSelectedNode();
		if (node != null) {
			RegionStructureNodePK pk = (RegionStructureNodePK) node.getPrimaryKey();
			int keyCode = ev.getKeyCode();
			if (pk.getId() != 0) {
				if (keyCode == KeyEvent.VK_DELETE) {
					deleteAction();
				}
			}
			if (keyCode == KeyEvent.VK_F5) {
				regionStructureTree.initTree();
				regionStructureTree.addSelectionRow(0);
			}
		}
	}

	// Delete Action
	@SuppressWarnings("unchecked")
	public synchronized void deleteAction() {
		try {
			if (JOptionPane.showConfirmDialog(main.getMainFrame(), ui.getString("fina2.regionstructurenode.delete"), ui.getString("fina2.returns.import.message"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/regions/RegionStructureNode");
				RegionStructureNodeHome home = (RegionStructureNodeHome) PortableRemoteObject.narrow(ref, RegionStructureNodeHome.class);
				Node node = regionStructureTree.getSelectedNode();
				RegionStructureNodePK pk = (RegionStructureNodePK) node.getPrimaryKey();

				RegionStructureNode lang = home.findByPrimaryKey(pk);
				lang.remove();

				if (lang.isInUsed()) {
					DefaultTreeModel m_model = (DefaultTreeModel) regionStructureTree.getModel();
					DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) regionStructureTree.getSelectedTreeNode();

					// remove collection node
					DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selNode.getParent();
					Node n = (Node) parentNode.getUserObject();
					Vector<Node> parentChildren = n.getChildren();
					Node temp = (Node) selNode.getUserObject();
					for (int i = 0; i < parentChildren.size(); i++) {
						if (temp.getLabel().equals(parentChildren.get(i).getLabel())) {
							parentChildren.remove(i);
						}
					}

					if (selNode != null) {
						MutableTreeNode parent = (MutableTreeNode) (selNode.getParent());
						if (parent != null) {
							MutableTreeNode toBeSelNode = getSibling(selNode);
							if (toBeSelNode == null) {
								toBeSelNode = parent;
							}
							TreeNode[] nodes = m_model.getPathToRoot(toBeSelNode);
							TreePath path = new TreePath(nodes);
							regionStructureTree.scrollPathToVisible(path);
							regionStructureTree.setSelectionPath(path);
							m_model.removeNodeFromParent(selNode);
						}
					}
				} else {
					JOptionPane.showMessageDialog(null, ui.getString("fina2.region.delete.nodeUsed"), ui.getString("fina2.region.delete.title"), 2);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Main.generalErrorHandler(ex);
		}
		isRootSelection();
	}

	private MutableTreeNode getSibling(DefaultMutableTreeNode selNode) {
		MutableTreeNode sibling = (MutableTreeNode) selNode.getPreviousSibling();
		if (sibling == null) {
			sibling = (MutableTreeNode) selNode.getNextSibling();
		}
		return sibling;
	}

	@Override
	protected void refreshButtonActionPerformed(ActionEvent event) {
		regionStructureTree.initTree();
		regionStructureTree.addSelectionRow(0);
		initPropertiesList();
	}

	@Override
	protected void printButtonActionPerformed(ActionEvent event) {
		new PrintAction(main.getMainFrame());
	};

	private void closeDialog() {
		this.setVisible(false);
	}

	private void initPropertiesList(Map<Integer, String> propertiesMap) {
		propertiesMap.remove(0);
		Collection<String> values = propertiesMap.values();
		DefaultListModel model = (DefaultListModel) propertiesList.getModel();
		model.removeAllElements();
		for (String s : values) {
			model.addElement(s);
		}
		if (values.size() == 0) {
			model.addElement("");
		}
	}

	private void initPropertiesList() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/regions/RegionStructureSession");
			RegionStructureSessionHome home = (RegionStructureSessionHome) PortableRemoteObject.narrow(ref, RegionStructureSessionHome.class);
			RegionStructureSession session = home.create();
			Map<Integer, String> map = session.getProperties(main.getLanguageHandle());
			initPropertiesList(map);
		} catch (Exception ex) {
			ex.printStackTrace();
			Main.generalErrorHandler(ex);
		}
	}

	private class PropertiesListRenderer extends DefaultListCellRenderer {
		private fina2.ui.UIManager ui = fina2.Main.main.ui;

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			label.setFont(ui.getFont());
			Icon icon = null;
			switch (index) {
			case 0: {
				icon = ui.getIcon("fina2.country");
				break;
			}
			case 1: {
				icon = ui.getIcon("fina2.state");
				break;
			}
			case 2: {
				icon = ui.getIcon("fina2.city");
				break;
			}
			default: {
				icon = ui.getIcon("fina2.other.regions");
			}
			}
			label.setIcon(icon);
			label.setText(value + "");
			label.setBackground(eastPanel.getBackground());
			return label;
		}
	}

	public class CreateAndReviewRegionDialog extends JDialog {
		private JTextField codeTextFileld;
		private JTextField descriptionTextFiled;

		private Container container;
		private JButton InsertButton;

		CreateAmendAndReview createAmendAndReview;

		public CreateAndReviewRegionDialog(java.awt.Frame parent, RegionStructureTree tree, CreateAmendAndReview createAmendAndReview) {
			super(parent, true);
			this.createAmendAndReview = createAmendAndReview;
			this.setSize(500, 300);

			initComponents();

			if (!createAmendAndReview.equals(CreateAmendAndReview.CREATE)) {
				initReview();
			}
			this.setLocationRelativeTo(parent);
			this.setVisible(true);
		}

		private void closeDialog() {
			this.setVisible(false);
		}

		private void initComponents() {
			container = this.getContentPane();
			container.setLayout(new GridBagLayout());

			GridBagConstraints gbc = new GridBagConstraints();

			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(5, 10, 5, 5);

			// Code Field
			gbc.gridwidth = GridBagConstraints.RELATIVE;
			container.add(new JLabel(UIManager.formatedHtmlString(ui.getString("fina2.code"))), gbc);
			gbc.gridwidth = GridBagConstraints.REMAINDER;

			codeTextFileld = new JTextField(14);
			codeTextFileld.setFont(ui.getFont());
			if (createAmendAndReview.equals(CreateAmendAndReview.REVIEW)) {
				codeTextFileld.setEditable(false);
			}
			container.add(codeTextFileld, gbc);

			// Description Field
			gbc.gridwidth = GridBagConstraints.RELATIVE;
			container.add(new JLabel(UIManager.formatedHtmlString(ui.getString("fina2.description"))), gbc);
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			descriptionTextFiled = new JTextField(20);
			descriptionTextFiled.setFont(ui.getFont());
			if (createAmendAndReview.equals(CreateAmendAndReview.REVIEW)) {
				descriptionTextFiled.setEditable(false);
			}
			container.add(descriptionTextFiled, gbc);

			// Insert Button
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.insets = new Insets(10, 0, 5, 0);

			if (createAmendAndReview.equals(CreateAmendAndReview.AMEND)) {
				InsertButton = new JButton(ui.getIcon("fina2.amend"));
				InsertButton.setText(ui.getString("fina2.amend"));
				this.setTitle(ui.getString("fina2.amend"));
			}
			if (createAmendAndReview.equals(CreateAmendAndReview.CREATE)) {
				InsertButton = new JButton(ui.getIcon("fina2.create"));
				InsertButton.setText(ui.getString("fina2.create"));
				this.setTitle(ui.getString("fina2.create"));
			}
			if (createAmendAndReview.equals(CreateAmendAndReview.REVIEW)) {
				InsertButton = new JButton(ui.getIcon("fina2.close"));
				InsertButton.setText(ui.getString("fina2.close"));
				this.setTitle(ui.getString("fina2.close"));
			}

			InsertButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Thread thread = new Thread() {
						public void run() {
							if (createAmendAndReview.equals(CreateAmendAndReview.CREATE)) {
								createAction();
							}

							if (createAmendAndReview.equals(CreateAmendAndReview.AMEND)) {
								amendAction();
							}
							if (createAmendAndReview.equals(CreateAmendAndReview.REVIEW)) {
								closeDialog();
							}
						};
					};
					thread.start();
				}

			});
			container.add(InsertButton, gbc);

			this.pack();
		}

		// Create Action
		private void createAction() {
			RegionStructureNode region = null;
			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/regions/RegionStructureNode");
				RegionStructureNodeHome home = (RegionStructureNodeHome) PortableRemoteObject.narrow(ref, RegionStructureNodeHome.class);

				region = home.create();

				Node node = regionStructureTree.getSelectedNode();
				RegionStructureNodePK pk = (RegionStructureNodePK) node.getPrimaryKey();

				String code = codeTextFileld.getText();

				if (stringvalid(code, 40))
					region.setCode(code, false);

				String description = descriptionTextFiled.getText();
				if (stringvalid(description, 100))
					region.setDescription(main.getLanguageHandle(), description);
				region.setParentId(pk.getId());

				Node newNode = new Node(region.getPrimaryKey(), "[" + code + "]" + description, new Integer(-1));
				newNode.setParentPK(pk);

				DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) regionStructureTree.getLastSelectedPathComponent();

				Node no = (Node) selNode.getUserObject();
				no.addChild(newNode);

				if (selNode != null) {
					DefaultTreeModel treeModel = (DefaultTreeModel) regionStructureTree.getModel();
					DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(newNode);
					treeModel.insertNodeInto(newTreeNode, selNode, selNode.getChildCount());

					TreeNode[] nodes = treeModel.getPathToRoot(selNode);
					TreePath path = new TreePath(nodes);
					regionStructureTree.scrollPathToVisible(path);
					regionStructureTree.setSelectionPath(path);
					regionStructureTree.expandPath(path);
				}

				closeDialog();
			} catch (InvalidTextFormatException ex) {
				showMessageBox(ex.getMessage());
				try {
					region.remove();
				} catch (Exception e1) {
					Main.generalErrorHandler(e1);
				}
			} catch (FinaTypeException ex) {
				Main.generalErrorHandler(ex);
				try {
					region.remove();
				} catch (Exception e1) {
					Main.generalErrorHandler(e1);
				}
			} catch (Exception ex) {
				try {
					region.remove();
				} catch (Exception e1) {
					Main.generalErrorHandler(e1);
				}
				Main.generalErrorHandler(ex);
			}
		}

		// Initial Review And Amend
		public void initReview() {
			{
				try {
					InitialContext jndi = fina2.Main.getJndiContext();
					Object ref = jndi.lookup("fina2/regions/RegionStructureNode");
					RegionStructureNodeHome home = (RegionStructureNodeHome) PortableRemoteObject.narrow(ref, RegionStructureNodeHome.class);

					Node node = regionStructureTree.getSelectedNode();
					RegionStructureNodePK pk = (RegionStructureNodePK) node.getPrimaryKey();

					RegionStructureNode regionNode = home.findByPrimaryKey(pk);

					codeTextFileld.setText(regionNode.getCode());
					descriptionTextFiled.setText(regionNode.getDescription(main.getLanguageHandle()));
				} catch (Exception ex) {
					Main.generalErrorHandler(ex);
					ex.printStackTrace();
				}
			}
		}

		// Amend action
		@SuppressWarnings("unchecked")
		public void amendAction() {
			RegionStructureNode region = null;
			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/regions/RegionStructureNode");
				RegionStructureNodeHome home = (RegionStructureNodeHome) PortableRemoteObject.narrow(ref, RegionStructureNodeHome.class);

				Node node = regionStructureTree.getSelectedNode();
				RegionStructureNodePK pk = (RegionStructureNodePK) node.getPrimaryKey();

				region = home.findByPrimaryKey(pk);

				String code = codeTextFileld.getText();

				if (stringvalid(code, 40))
					region.setCode(code, true);

				String description = descriptionTextFiled.getText();

				if (stringvalid(description, 100))
					region.setDescription(main.getLanguageHandle(), descriptionTextFiled.getText());

				RegionStructureNodePK parentPk = (RegionStructureNodePK) node.getParentPK();
				region.setParentId(parentPk.getId());

				Node newNode = new Node(pk, "[" + code + "]" + description, new Integer(-1));
				newNode.setParentPK(new RegionStructureNodePK(parentPk.getId()));

				DefaultTreeModel treeModel = (DefaultTreeModel) regionStructureTree.getModel();

				DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) regionStructureTree.getSelectedTreeNode();

				// change in collection
				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selNode.getParent();
				Node n = (Node) parentNode.getUserObject();
				Vector<Node> parentChildren = n.getChildren();
				Node temp = (Node) selNode.getUserObject();
				for (int i = 0; i < parentChildren.size(); i++) {
					if (temp.getLabel().equals(parentChildren.get(i).getLabel())) {
						parentChildren.remove(i);
					}
				}
				parentChildren.add(newNode);

				if (selNode != null) {
					TreeNode[] nodes = treeModel.getPathToRoot(selNode);
					TreePath path = new TreePath(nodes);
					treeModel.valueForPathChanged(path, newNode);
					regionStructureTree.scrollPathToVisible(path);
					regionStructureTree.setSelectionPath(path);
				}
				closeDialog();
			} catch (InvalidTextFormatException ex) {
				showMessageBox(ex.getMessage());
			} catch (FinaTypeException ex) {
				Main.generalErrorHandler(ex);
			} catch (Exception ex) {
				Main.generalErrorHandler(ex);
			}
		}

		private boolean stringvalid(String text, int columns) throws InvalidTextFormatException {
			if (text == null) {
				throw new InvalidTextFormatException(ui.getString("fina2.region.invalidtextFormat") + " \"\"");
			}
			if (text.equals("") || (text.length() > columns)) {
				throw new InvalidTextFormatException(ui.getString("fina2.region.invalidtextFormat") + " \"" + text + "\"");
			}
			char[] ch = text.toCharArray();
			for (int i = 0; i < ch.length; i++) {
				if (ch[i] == '/' || ch[i] == '[' || ch[i] == ']' || ch[i] == '\\') {
					throw new InvalidTextFormatException(ui.getString("fina2.region.invalidtextFormat") + " \"" + text + "\"");
				}
			}
			return true;
		}

		private class InvalidTextFormatException extends Exception {
			InvalidTextFormatException(String message) {
				super(message);
			}
		}

		public void showMessageBox(String messageText) {
			MessageBox message = new MessageBox(main.getMainFrame());
			message.setMessage(messageText);
			message.setTitle(ui.getString("fina2.web.message"));
			message.setLocationRelativeTo(this);
			message.pack();
			message.setVisible(true);
		}
	}

	class PrintAction implements Runnable {
		private UIManager ui = Main.main.ui;

		PrintAction(java.awt.Frame parent) {
			run();
		}

		public void run() {
			print();
		}

		public void print() {
			EJBTable table = initTable();
			String frameTitle = ui.getString("fina2.bank.city&region");
			TableReviewFrame tableReviewFrame = new TableReviewFrame();
			tableReviewFrame.show(frameTitle, table);
		}

		@SuppressWarnings("rawtypes")
		private EJBTable initTable() {
			// get Printed Data
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) regionStructureTree.getModel().getRoot();
			PrintData pd = regionStructureTree.createPrintDate(root);
			Collection regionsList = pd.getPrintedData();
			int columnCount = pd.getMaxColumn();

			// get Column names
			ArrayList<String> colNames = new ArrayList<String>();
			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/regions/RegionStructureSession");
				RegionStructureSessionHome home = (RegionStructureSessionHome) PortableRemoteObject.narrow(ref, RegionStructureSessionHome.class);
				RegionStructureSession session = home.create();
				Map<Integer, String> map = session.getProperties(main.getLanguageHandle());

				// remove level max level
				map.remove(0);

				Collection<String> values = map.values();
				for (String s : values) {
					colNames.add(s + " " + ui.getString("fina2.code"));
					colNames.add(s + " " + ui.getString("fina2.description"));
				}
				if (columnCount > values.size()) {
					for (int i = 0; i < columnCount - values.size(); i++) {
						colNames.add("NONAME");
					}
				}
			} catch (Exception ex) {
				Main.generalErrorHandler(ex);
			}

			// Put data to the table
			EJBTable table = new EJBTable();
			table.setAllowSort(false);
			table.initTable(colNames, regionsList);
			return table;
		}
	}
}