package fina2.reportoo;

import java.awt.CardLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import fina2.Main;
import fina2.bank.BankCriterionConstants;
import fina2.bank.BankCriterionPK;
import fina2.bank.BankPK;
import fina2.bank.BankSession;
import fina2.bank.BankSessionHome;
import fina2.bank.BanksConstants;
import fina2.metadata.MDTConstants;
import fina2.metadata.MDTNodePK;
import fina2.metadata.MDTSession;
import fina2.metadata.MDTSessionHome;
import fina2.period.PeriodSession;
import fina2.period.PeriodSessionHome;
import fina2.ui.UIManager;
import fina2.ui.sheet.Spreadsheet;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;
import fina2.ui.tree.EJBTree;
import fina2.ui.tree.Node;

public class ParameterWizard extends javax.swing.JDialog {

	/**
	 * BankTreeAssistant. Used for the bank trees init, loading data and etc.
	 * See initBankTreeParameters method.
	 */
	private BankParameterAssistant bankAssistant = null;

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private int step = 1;

	private String[] names;
	private String[] descriptions;
	private JLabel[] labels;
	private int maxSteps;

	private boolean yes = false;

	private EJBTable sourceTable;
	private EJBTable targetTable;
	private fina2.ui.tree.EJBTree tree;
	public fina2.ui.sheet.openoffice.OOParameter iter;
	private Collection t_rows;
	private Spreadsheet sheet;

	private static Vector periodColumn;
	static {
		periodColumn = new Vector();
		periodColumn.add(fina2.Main.main.ui
				.getString("fina2.period.periodType"));
		periodColumn.add(fina2.Main.main.ui
				.getString("fina2.period.periodNumber"));
		periodColumn.add(fina2.Main.main.ui.getString("fina2.period.fromDate"));
		periodColumn.add(fina2.Main.main.ui.getString("fina2.period.toDate"));
	}

	/** Creates new form IteratorWizard */
	public ParameterWizard(java.awt.Frame parent, boolean modal) {
		super(parent, modal);

		names = new String[] { "",
				ui.getString("fina2.reportoo.iteratorWizard.step1Name"),
				ui.getString("fina2.reportoo.iteratorWizard.step2Name"),
				ui.getString("fina2.reportoo.iteratorWizard.step3Name"),
				ui.getString("fina2.reportoo.iteratorWizard.step4Name") };
		descriptions = new String[] { "",
				ui.getString("fina2.reportoo.iteratorWizard.step1Description"),
				ui.getString("fina2.reportoo.iteratorWizard.step2Description"),
				ui.getString("fina2.reportoo.iteratorWizard.step3Description"),
				ui.getString("fina2.reportoo.iteratorWizard.step4Description") };
		maxSteps = 3;

		ui.loadIcon("fina2.ok", "ok.gif");
		ui.loadIcon("fina2.cancel", "cancel.gif");
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.next", "forward.gif");
		ui.loadIcon("fina2.back", "back.gif");
		ui.loadIcon("fina2.amend", "amend.gif");
		ui.loadIcon("fina2.delete", "delete.gif");
		ui.loadIcon("fina2.insert", "insert.gif");
		ui.loadIcon("fina2.reportoo.iteratorWizard.up", "up.gif");
		ui.loadIcon("fina2.reportoo.iteratorWizard.down", "down.gif");

		sourceTable = new EJBTable();
		targetTable = new EJBTable();

		targetTable.setAllowSort(false);

		sourceTable
				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		targetTable
				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

		tree = new fina2.ui.tree.EJBTree();

		sourceTable
				.addSelectionListener(new javax.swing.event.ListSelectionListener() {
					public void valueChanged(
							javax.swing.event.ListSelectionEvent evt) {
						if (!typeCombo
								.getSelectedItem()
								.equals(
										ui
												.getString("fina2.reportoo.iteratorType.node"))) {
							if (sourceTable.getSelectedRow() == -1) {
								rightButton.setEnabled(false);
							} else {
								rightButton.setEnabled(true);
							}
						}

					}
				});

		targetTable
				.addSelectionListener(new javax.swing.event.ListSelectionListener() {
					public void valueChanged(
							javax.swing.event.ListSelectionEvent evt) {
						if (targetTable.getSelectedRow() == -1) {
							leftButton.setEnabled(false);
							upButton.setEnabled(false);
							downButton.setEnabled(false);
						} else {
							leftButton.setEnabled(true);
							if (targetTable.getSelectedRow() == targetTable
									.getRowCount() - 1) {
								downButton.setEnabled(false);
							} else {
								downButton.setEnabled(true);
							}
							if (targetTable.getSelectedRow() == 0) {
								upButton.setEnabled(false);
							} else {
								upButton.setEnabled(true);
							}
						}
					}
				});

		tree
				.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
					public void valueChanged(
							javax.swing.event.TreeSelectionEvent evt) {
						Node node = tree.getSelectedNode();
						if (typeCombo
								.getSelectedItem()
								.equals(
										ui
												.getString("fina2.reportoo.iteratorType.node"))) {
							if (node == null) {
								rightButton.setEnabled(false);
							} else {
								rightButton.setEnabled(true);
							}
						} else if (typeCombo
								.getSelectedItem()
								.equals(
										ui
												.getString("fina2.reportoo.iteratorType.peer"))) {
							if (node == null
									|| !node
											.getType()
											.equals(
													new Integer(
															BanksConstants.NODETYPE_BANK_GROUP_NODE))
									&& tree.getSelectedNodes().size() == 1) {
								rightButton.setEnabled(false);
							} else {
								rightButton.setEnabled(true);
							}
						}
					}
				});

		tree.addTreeExpansionListener(new TreeExpansionListener() {
			DefaultMutableTreeNode node;
			String sync = "sync";

			public void treeCollapsed(TreeExpansionEvent evt) {
			}

			public synchronized void treeExpanded(TreeExpansionEvent evt) {
				node = (DefaultMutableTreeNode) evt.getPath()
						.getLastPathComponent();
				Thread thread = new Thread() {
					public void run() {
						synchronized (sync) {
							DefaultMutableTreeNode child = (DefaultMutableTreeNode) node
									.getFirstChild();
							if (!(((Node) child.getUserObject())
									.getPrimaryKey() instanceof MDTNodePK)
									|| ((MDTNodePK) ((Node) child
											.getUserObject()).getPrimaryKey())
											.getId() != -1)
								return;
							try {
								InitialContext jndi = fina2.Main
										.getJndiContext();
								Object ref = jndi
										.lookup("fina2/metadata/MDTSession");
								MDTSessionHome home = (MDTSessionHome) PortableRemoteObject
										.narrow(ref, MDTSessionHome.class);

								MDTSession session = home.create();

								Collection nodes = session.getChildNodes(main
										.getUserHandle(), main
										.getLanguageHandle(),
										(MDTNodePK) ((Node) node
												.getUserObject())
												.getPrimaryKey());

								prepareNodes(
										(javax.swing.tree.DefaultTreeModel) tree
												.getModel(), node, nodes);
								((javax.swing.tree.DefaultTreeModel) tree
										.getModel())
										.removeNodeFromParent(child);

							} catch (Exception e) {
								Main.generalErrorHandler(e);
							}
						}
					}
				};
				thread.start();
			}
		});

		initComponents();

		Vector v = new Vector(5);
		v.add(ui.getString("fina2.select"));
		v.add(ui.getString("fina2.reportoo.iteratorType.bank"));
		v.add(ui.getString("fina2.reportoo.iteratorType.peer"));
		v.add(ui.getString("fina2.reportoo.iteratorType.node"));
		v.add(ui.getString("fina2.reportoo.iteratorType.period"));
		// v.add(ui.getString("fina2.reportoo.iteratorType.period"));
		// v.add(ui.getString("fina2.reportoo.iteratorType.table"));
		typeCombo.setModel(new javax.swing.DefaultComboBoxModel(new Vector(v)));

		// v = new Vector(1);
		// v.add(ui.getString("fina2.none"));
		// parameterCombo.setModel(
		// new javax.swing.DefaultComboBoxModel(new Vector(v))
		// );

		labels = new JLabel[] { null, nameLabel, typeLabel, orientationLabel,
				valuesLabel };

		Font uiFont = ui.getFont();
		wizardLabel.setFont(new Font(uiFont.getFontName(), Font.BOLD
				| Font.ITALIC, 18));
		stepNameLabel.setFont(new Font(uiFont.getFontName(), Font.BOLD, 14));
		stepDescText.setBackground(cardPanel.getBackground());
		((CardLayout) cardPanel.getLayout()).show(cardPanel, "step1");
	}

	private synchronized void prepareNodes(
			javax.swing.tree.DefaultTreeModel model,
			DefaultMutableTreeNode parent, Collection nodes) {
		for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			Node node = (Node) iter.next();
			DefaultMutableTreeNode n = new DefaultMutableTreeNode(node);
			model.insertNodeInto(n, parent, parent.getChildCount());
			if (((Integer) node.getType()).intValue() == MDTConstants.NODETYPE_NODE) {
				model.insertNodeInto(new DefaultMutableTreeNode(new Node(
						new MDTNodePK(-1), ui
								.getString("fina2.metadata.loading"),
						new Integer(-1))), n, 0);
			}
		}
	}

	private void checkValid() {
		switch (step) {
		case 1:
			if (nameText.getText().trim().equals("") || !checkName()) {
				nextButton.setEnabled(false);
			} else {
				nextButton.setEnabled(true);
			}
			break;
		case 2:
			if (typeCombo.getSelectedIndex() < 1) {
				nextButton.setEnabled(false);
			} else {
				nextButton.setEnabled(true);
			}
			break;
		case 3:

			/* if(targetTable.getRowCount()>0) { */
			nextButton.setEnabled(true);
			/*
			 * } else { nextButton.setEnabled(false); } break;
			 */
		}
	}

	private void allowFilterPannel() {
		jFilterComponent.setVisible(true);
	}

	private void disableFilterPannel() {
		jFilterComponent.setVisible(false);
	}

	private void attachFilterPannel(java.awt.Component filter) {
		jPanel3.remove(jFilterComponent);
		jPanel3.add(filter, java.awt.BorderLayout.NORTH);
		jFilterComponent = filter;
		javax.swing.SwingUtilities.updateComponentTreeUI(jPanel3);
	}

	private void initTable() {

		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi
					.lookup("fina2/reportoo/repository/RepositorySession");
			fina2.reportoo.repository.RepositorySessionHome home = (fina2.reportoo.repository.RepositorySessionHome) PortableRemoteObject
					.narrow(
							ref,
							fina2.reportoo.repository.RepositorySessionHome.class);
			fina2.reportoo.repository.RepositorySession session = home.create();

			Vector rows = new Vector();

			disableFilterPannel();

			// -----------------------------------------------------------------
			// Bank parameter
			if (typeCombo.getSelectedItem().equals(
					ui.getString("fina2.reportoo.iteratorType.bank"))) {
				if (iter != null && iter.getValues() != null) {
					rows = (Vector) session.getBankValues(iter.getValues(),
							main.getUserHandle(), main.getLanguageHandle());
				}

				initBankParameter(rows);
				return;
			}

			// -----------------------------------------------------------------
			// Other type parameters
			if (typeCombo.getSelectedItem().equals(
					ui.getString("fina2.reportoo.iteratorType.node"))) {
				if (iter != null && iter.getValues() != null) {
					rows = (Vector) session.getNodeValues(iter.getValues(),
							main.getUserHandle(), main.getLanguageHandle());
				}
				initNodes(rows);
			}
			if (typeCombo.getSelectedItem().equals(
					ui.getString("fina2.reportoo.iteratorType.peer"))) {
				if (iter != null && iter.getValues() != null) {
					rows = (Vector) session.getPeerValues(iter.getValues(),
							main.getUserHandle(), main.getLanguageHandle());
				}
				initPeers(rows);
			}
			if (typeCombo.getSelectedItem().equals(
					ui.getString("fina2.reportoo.iteratorType.offset"))) {
				if (iter != null && iter.getValues() != null) {
					for (Iterator it = iter.getValues().iterator(); it
							.hasNext();) {
						Object data = it.next();
						TableRowImpl row = new TableRowImpl(data, 4);
						row.setValue(0, data.toString());
						rows.add(row);

					}
				}
				initOffsets(rows);
			}
			if (typeCombo.getSelectedItem().equals(
					ui.getString("fina2.reportoo.iteratorType.period"))) {
				if (iter != null && iter.getValues() != null) {
					rows = (Vector) session.getPeriodValues(iter.getValues(),
							main.getUserHandle(), main.getLanguageHandle());
				}
				initPeriods(rows);
				allowFilterPannel();
			}

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		checkValid();

	}

	private void initPeriods(Vector rows) {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/period/PeriodSession");
			PeriodSessionHome home = (PeriodSessionHome) PortableRemoteObject
					.narrow(ref, PeriodSessionHome.class);

			PeriodSession session = home.create();

			Vector colNames = getPeriodColumns();
			Vector sourceRows = (Vector) session.getPeriodRows(main
					.getUserHandle(), main.getLanguageHandle());

			PeriodFilterPannel filter = new PeriodFilterPannel(colNames,
					sourceRows, sourceTable);
			attachFilterPannel(filter);

			Vector targetRows = rows;

			sourceTable.setAllowSort(false);
			sourceTable.initTable(colNames, sourceRows);
			targetTable.initTable(colNames, targetRows);

			sourceScrollPane.setViewportView(sourceTable);
			targetScrollPane.setViewportView(targetTable);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private Vector getPeriodColumns() {
		return periodColumn;
	}

	private void initOffsets(Vector rows) {

		Vector colNames = new Vector();
		colNames.add(ui.getString("fina2.reportoo.parameterType.offset"));
		Vector sourceRows = new Vector();
		Vector targetRows = rows;
		sourceTable.initTable(colNames, sourceRows);
		targetTable.initTable(colNames, targetRows);
		targetScrollPane.setViewportView(targetTable);
	}

	private void initPeers(Vector rows) {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/bank/BankSession");
			BankSessionHome home = (BankSessionHome) PortableRemoteObject
					.narrow(ref, BankSessionHome.class);

			BankSession session = home.create();

			Vector colNames = new Vector();
			colNames.add(ui.getString("fina2.code"));
			colNames.add(ui.getString("fina2.description"));

			Node rootNode = new Node(new BankPK(0), ui
					.getString("fina2.bank.criterions"), new Integer(
					BanksConstants.NODETYPE_ROOT));
			tree.initTree(rootNode);

			DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootNode);
			prepareNodes(root, session.getBankCriterionRows(main
					.getUserHandle(), main.getLanguageHandle()), session
					.getBankGroupsRows(main.getUserHandle(), main
							.getLanguageHandle()));
			((javax.swing.tree.DefaultTreeModel) tree.getModel()).setRoot(root);

			tree.addIcon(new Integer(BanksConstants.NODETYPE_BANK_GROUP_NODE),
					ui.getIcon("fina2.node"));

			tree.addIcon(new Integer(
					BanksConstants.NODETYPE_BANK_CRITERION_NODE), ui
					.getIcon("fina2.node"));

			tree.addIcon(new Integer(
					BanksConstants.NODETYPE_DEF_BANK_CRITERION_NODE), ui
					.getIcon("fina2.node.default"));

			tree.setRootVisible(true);
			tree.setSelectionRow(0);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
			ui.putConfigValue("fina2.bank.BankGroupsFrame.visible",
					new Boolean(false));
		}

		Vector colNames = new Vector();
		colNames.add(ui.getString("fina2.code"));
		colNames.add(ui.getString("fina2.description"));
		Vector sourceRows = new Vector();
		Vector targetRows = rows;
		sourceTable.initTable(colNames, sourceRows);
		targetTable.initTable(colNames, targetRows);

		sourceScrollPane.setViewportView(tree);
		targetScrollPane.setViewportView(targetTable);

		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

	private synchronized void prepareNodes(DefaultMutableTreeNode parent,
			Collection criterionRows, Collection bankGroupRows) {
		String def = String.valueOf(BankCriterionConstants.DEF_CRITERION);
		for (Iterator iter = criterionRows.iterator(); iter.hasNext();) {
			TableRow criterionRow = (TableRow) iter.next();
			Node criterionNode = new Node(
					criterionRow.getPrimaryKey(),
					criterionRow.getValue(0) + " / " + criterionRow.getValue(1),
					new Integer(BanksConstants.NODETYPE_BANK_CRITERION_NODE));
			if (def.equals(criterionRow.getValue(2))) { // is default
				criterionNode.setDefaultNode(true);
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
					bankGroupNode.putProperty("code", bankGroupRow.getValue(0));
					bankGroupNode
							.putProperty("label", bankGroupRow.getValue(3));
					DefaultMutableTreeNode bankGroupTreeNode = new DefaultMutableTreeNode(
							bankGroupNode);
					criterionTreeNode.add(bankGroupTreeNode);
				}
			}
		}
	}

	private void initNodes(Vector rows) {

		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/metadata/MDTSession");
			MDTSessionHome home = (MDTSessionHome) PortableRemoteObject.narrow(
					ref, MDTSessionHome.class);

			MDTSession session = home.create();

			Node rootNode = new Node(new MDTNodePK(0), "        ", new Integer(
					-1));
			tree.initTree(rootNode);

			DefaultMutableTreeNode root = new DefaultMutableTreeNode(rootNode);
			Collection nodes = session.getChildNodes(main.getUserHandle(), main
					.getLanguageHandle(), new MDTNodePK(0));

			prepareNodes((javax.swing.tree.DefaultTreeModel) tree.getModel(),
					root, nodes);

			((javax.swing.tree.DefaultTreeModel) tree.getModel()).setRoot(root);

			tree.addIcon(new Integer(-1), ui.getIcon("fina2.node"));

			tree.addIcon(new Integer(MDTConstants.NODETYPE_NODE), ui
					.getIcon("fina2.node"));
			tree.addIcon(new Integer(MDTConstants.NODETYPE_INPUT), ui
					.getIcon("fina2.input"));
			tree.addIcon(new Integer(MDTConstants.NODETYPE_VARIABLE), ui
					.getIcon("fina2.variable"));

			tree.setRootVisible(false);
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

		Vector colNames = new Vector();
		// colNames.add(ui.getString("fina2.code"));
		colNames.add(ui.getString("fina2.description"));
		Vector sourceRows = new Vector();
		Vector targetRows = rows;
		sourceTable.initTable(colNames, sourceRows);
		targetTable.initTable(colNames, targetRows);

		sourceScrollPane.setViewportView(tree);
		targetScrollPane.setViewportView(targetTable);
	}

	/** Inits bank parameter */
	private void initBankParameter(Vector rows) {

		bankAssistant = new BankParameterAssistant();
		bankAssistant.selectBanks(rows);

		// Source tree
		EJBTree sourceTree = bankAssistant.getSourceTree();
		sourceScrollPane.setViewportView(sourceTree);

		// Target tree
		EJBTree targetTree = bankAssistant.getTargetTree();
		targetScrollPane.setViewportView(targetTree);

		// Processing buttons state
		rightButton.setEnabled(true);
		leftButton.setEnabled(true);

		// downButton.setVisible(false);
		// upButton.setVisible(false);

	}

	private boolean checkName() {
		for (Iterator it = t_rows.iterator(); it.hasNext();) {
			TableRow row = (TableRow) it.next();
			if (row.getValue(1).equals(nameText.getText().trim())) {
				if (row.getPrimaryKey().equals(iter)) {
					return true;
				} else {
					return false;
				}
			}
		}
		return true;
	}

	public void show(fina2.ui.sheet.openoffice.OOParameter iter,
			Collection t_rows, Spreadsheet sheet) {

		this.iter = iter;
		this.t_rows = t_rows;
		this.sheet = sheet;
		step = 1;

		setSize(getParent().getWidth() - 60, getParent().getHeight() - 60);
		splitPane.setDividerLocation((getParent().getWidth() - 200) / 2);
		setLocationRelativeTo(getParent());
		if (iter != null) {
			nameText.setText(iter.getName());
			typeCombo.setSelectedIndex(iter.getType());
			// if(iter.getOrientation()==fina2.ui.sheet.Iterator.ROW_ITERATOR)
			// rowsRadio.setSelected(true);
			// if(iter.getOrientation()==fina2.ui.sheet.Iterator.COL_ITERATOR)
			// columnsRadio.setSelected(true);
		}
		// if(iter!=null) {
		// rowsRadio.setEnabled(false);
		// columnsRadio.setEnabled(false);
		// }
		updateStep();
		super.show();

	}

	public boolean Ok() {
		return yes;
	}

	public fina2.ui.sheet.openoffice.OOParameter getIterator() {
		return iter;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() { // GEN-BEGIN:initComponents

		orienationGroup = new javax.swing.ButtonGroup();
		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		wizardLabel = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		nameLabel = new javax.swing.JLabel();
		typeLabel = new javax.swing.JLabel();
		orientationLabel = new javax.swing.JLabel();
		valuesLabel = new javax.swing.JLabel();
		jPanel4 = new javax.swing.JPanel();
		jPanel5 = new javax.swing.JPanel();
		helpButton = new javax.swing.JButton();
		jPanel6 = new javax.swing.JPanel();
		backButton = new javax.swing.JButton();
		nextButton = new javax.swing.JButton();
		jPanel7 = new javax.swing.JPanel();
		cancelButton = new javax.swing.JButton();
		jPanel8 = new javax.swing.JPanel();
		jPanel9 = new javax.swing.JPanel();
		stepNameLabel = new javax.swing.JLabel();
		stepDescText = new javax.swing.JTextArea();
		cardPanel = new javax.swing.JPanel();
		step1 = new javax.swing.JPanel();
		jLabel7 = new javax.swing.JLabel();
		nameText = new javax.swing.JTextField();
		step2 = new javax.swing.JPanel();
		rowsRadio = new javax.swing.JRadioButton();
		columnsRadio = new javax.swing.JRadioButton();
		step3 = new javax.swing.JPanel();
		jLabel8 = new javax.swing.JLabel();
		typeCombo = new javax.swing.JComboBox();
		step4 = new javax.swing.JPanel();
		finishPanel = new javax.swing.JPanel();
		jPanel10 = new javax.swing.JPanel();
		jPanel11 = new javax.swing.JPanel();
		textLabel = new javax.swing.JLabel();
		textField = new javax.swing.JTextField();
		textButton = new javax.swing.JButton();
		jPanel16 = new javax.swing.JPanel();
		splitPane = new javax.swing.JSplitPane();
		jPanel3 = new javax.swing.JPanel();
		sourceScrollPane = new javax.swing.JScrollPane();
		jFilterComponent = new javax.swing.JPanel();
		jPanel12 = new javax.swing.JPanel();
		jPanel13 = new javax.swing.JPanel();
		jPanel15 = new javax.swing.JPanel();
		upButton = new javax.swing.JButton();
		downButton = new javax.swing.JButton();
		targetScrollPane = new javax.swing.JScrollPane();
		jPanel14 = new javax.swing.JPanel();
		rightButton = new javax.swing.JButton();
		leftButton = new javax.swing.JButton();

		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jPanel1.setLayout(new java.awt.BorderLayout());

		jPanel1.setBackground(new java.awt.Color(45, 31, 173));
		jPanel2.setLayout(new java.awt.GridLayout(6, 1));

		jPanel2.setBorder(new javax.swing.border.EmptyBorder(
				new java.awt.Insets(1, 10, 1, 10)));
		jPanel2.setBackground(new java.awt.Color(45, 31, 173));
		wizardLabel.setText(ui.getString("fina2.reportoo.parameterWizard"));
		wizardLabel.setForeground(java.awt.Color.white);
		wizardLabel.setFont(ui.getFont());
		wizardLabel.setBorder(new javax.swing.border.EmptyBorder(
				new java.awt.Insets(1, 5, 1, 5)));
		jPanel2.add(wizardLabel);

		jPanel2.add(jLabel6);

		nameLabel.setText(ui
				.getString("fina2.reportoo.iteratorWizard.step1Name"));
		nameLabel.setForeground(java.awt.Color.white);
		nameLabel.setFont(ui.getFont());
		jPanel2.add(nameLabel);

		typeLabel.setText(ui
				.getString("fina2.reportoo.iteratorWizard.step2Name"));
		typeLabel.setForeground(java.awt.Color.white);
		typeLabel.setFont(ui.getFont());
		jPanel2.add(typeLabel);

		orientationLabel.setText(ui
				.getString("fina2.reportoo.iteratorWizard.step3Name"));
		orientationLabel.setForeground(java.awt.Color.white);
		orientationLabel.setFont(ui.getFont());
		jPanel2.add(orientationLabel);

		valuesLabel.setText(ui
				.getString("fina2.reportoo.iteratorWizard.step4Name"));
		valuesLabel.setForeground(java.awt.Color.white);
		valuesLabel.setFont(ui.getFont());
		jPanel2.add(valuesLabel);

		jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);

		getContentPane().add(jPanel1, java.awt.BorderLayout.WEST);

		jPanel4.setLayout(new java.awt.BorderLayout());

		jPanel4.setBorder(new javax.swing.border.EtchedBorder());
		helpButton.setIcon(ui.getIcon("fina2.help"));
		helpButton.setFont(ui.getFont());
		helpButton.setText(ui.getString("fina2.help"));
		helpButton.setEnabled(false);
		jPanel5.add(helpButton);

		jPanel4.add(jPanel5, java.awt.BorderLayout.WEST);

		backButton.setIcon(ui.getIcon("fina2.back"));
		backButton.setFont(ui.getFont());
		backButton.setText(ui.getString("fina2.back"));
		backButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				backButtonActionPerformed(evt);
			}
		});

		jPanel6.add(backButton);

		nextButton.setIcon(ui.getIcon("fina2.next"));
		nextButton.setFont(ui.getFont());
		nextButton.setText(ui.getString("fina2.next"));
		nextButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				nextButtonActionPerformed(evt);
			}
		});

		jPanel6.add(nextButton);

		jPanel6.add(jPanel7);

		cancelButton.setIcon(ui.getIcon("fina2.cancel"));
		cancelButton.setFont(ui.getFont());
		cancelButton.setText(ui.getString("fina2.cancel"));
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		jPanel6.add(cancelButton);

		jPanel4.add(jPanel6, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel4, java.awt.BorderLayout.SOUTH);

		jPanel8.setLayout(new java.awt.BorderLayout());

		jPanel9.setLayout(new java.awt.BorderLayout());

		stepNameLabel.setText("Step 1. Name");
		stepNameLabel.setFont(ui.getFont());
		stepNameLabel.setBorder(new javax.swing.border.EmptyBorder(
				new java.awt.Insets(5, 5, 5, 1)));
		jPanel9.add(stepNameLabel, java.awt.BorderLayout.NORTH);

		stepDescText.setEditable(false);
		stepDescText.setColumns(50);
		stepDescText.setRows(1);
		stepDescText.setFont(ui.getFont());
		stepDescText.setText("Enter name of iterator");
		stepDescText.setBorder(new javax.swing.border.EmptyBorder(
				new java.awt.Insets(1, 5, 1, 1)));
		jPanel9.add(stepDescText, java.awt.BorderLayout.SOUTH);

		jPanel8.add(jPanel9, java.awt.BorderLayout.NORTH);

		cardPanel.setLayout(new java.awt.CardLayout());

		step1.setLayout(new java.awt.GridBagLayout());

		step1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(
				110, 70, 110, 70)));
		jLabel7
				.setText(ui
						.getString("fina2.reportoo.iteratorWizard.step1Name"));
		jLabel7.setFont(ui.getFont());
		step1.add(jLabel7, UIManager.getGridBagConstraints(0, 0, -1, -1, -1,
				-1, -1, -1, new java.awt.Insets(0, 0, 0, 10)));

		nameText.setColumns(20);
		nameText.setFont(ui.getFont());
		nameText.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(java.awt.event.KeyEvent evt) {
				nameTextKeyReleased(evt);
			}
		});
		step1.add(nameText, UIManager.getGridBagConstraints(1, 0, -1, -1, -1,
				-1, -1, -1, null));

		cardPanel.add(step1, "step1");

		step2.setLayout(new java.awt.GridBagLayout());

		rowsRadio.setFont(ui.getFont());
		rowsRadio.setText(ui
				.getString("fina2.reportoo.iteratorOrientation.rows"));
		orienationGroup.add(rowsRadio);
		rowsRadio.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rowsRadioActionPerformed(evt);
			}
		});
		step2.add(rowsRadio, UIManager.getGridBagConstraints(0, 0, -1, -1, -1,
				-1, GridBagConstraints.WEST, -1, new java.awt.Insets(0, 0, 10,
						0)));

		columnsRadio.setFont(ui.getFont());
		columnsRadio.setText(ui
				.getString("fina2.reportoo.iteratorOrientation.columns"));
		orienationGroup.add(columnsRadio);
		columnsRadio.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				columnsRadioActionPerformed(evt);
			}
		});
		step2.add(columnsRadio, UIManager.getGridBagConstraints(0, 1, -1, -1,
				-1, -1, GridBagConstraints.WEST, -1, null));

		cardPanel.add(step2, "stepx");

		step3.setLayout(new java.awt.GridBagLayout());

		jLabel8
				.setText(ui
						.getString("fina2.reportoo.iteratorWizard.step3Name"));
		jLabel8.setFont(ui.getFont());
		step3.add(jLabel8, UIManager.getGridBagConstraints(0, 0, -1, -1, -1,
				-1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 0, 0,
						10)));

		typeCombo.setFont(ui.getFont());
		typeCombo.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				typeComboActionPerformed(evt);
			}
		});
		step3.add(typeCombo, UIManager.getGridBagConstraints(1, 0, -1, -1, -1,
				-1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				null));

		cardPanel.add(step3, "step2");

		step4.setLayout(new java.awt.CardLayout());

		finishPanel.setLayout(new java.awt.BorderLayout());

		jPanel10.setLayout(new java.awt.BorderLayout());

		textLabel.setText(ui.getString("fina2.reportoo.iteratorWizard.text"));
		textLabel.setFont(ui.getFont());
		jPanel11.add(textLabel);

		textField.setColumns(20);
		textField.setFont(ui.getFont());
		jPanel11.add(textField);

		textButton.setIcon(ui.getIcon("fina2.insert"));
		textButton.setFont(ui.getFont());
		textButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		textButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				textButtonActionPerformed(evt);
			}
		});

		jPanel11.add(textButton);

		jPanel10.add(jPanel11, java.awt.BorderLayout.EAST);

		finishPanel.add(jPanel10, java.awt.BorderLayout.SOUTH);

		jPanel16.setLayout(new java.awt.BorderLayout());

		splitPane.setDividerSize(4);
		jPanel3.setLayout(new java.awt.BorderLayout());

		jPanel3.add(sourceScrollPane, java.awt.BorderLayout.CENTER);
		jPanel3.add(jFilterComponent, java.awt.BorderLayout.NORTH);

		splitPane.setLeftComponent(jPanel3);

		jPanel12.setLayout(new java.awt.BorderLayout());

		jPanel13.setLayout(new java.awt.BorderLayout());

		upButton.setIcon(ui.getIcon("fina2.reportoo.iteratorWizard.up"));
		upButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		upButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				upButtonActionPerformed(evt);
			}
		});

		jPanel15.add(upButton);

		downButton.setIcon(ui.getIcon("fina2.reportoo.iteratorWizard.down"));
		downButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		downButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				downButtonActionPerformed(evt);
			}
		});

		jPanel15.add(downButton);

		jPanel13.add(jPanel15, java.awt.BorderLayout.EAST);

		jPanel12.add(jPanel13, java.awt.BorderLayout.SOUTH);

		jPanel12.add(targetScrollPane, java.awt.BorderLayout.CENTER);

		jPanel14.setLayout(new java.awt.GridBagLayout());

		rightButton.setIcon(ui.getIcon("fina2.next"));
		rightButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		rightButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rightButtonActionPerformed(evt);
			}
		});
		jPanel14.add(rightButton, UIManager.getGridBagConstraints(0, 0, -1, -1,
				-1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(
						0, 5, 5, 5)));

		leftButton.setIcon(ui.getIcon("fina2.back"));
		leftButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		leftButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				leftButtonActionPerformed(evt);
			}
		});
		jPanel14.add(leftButton, UIManager.getGridBagConstraints(0, 1, -1, -1,
				-1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(
						0, 5, 0, 5)));

		jPanel12.add(jPanel14, java.awt.BorderLayout.WEST);

		splitPane.setRightComponent(jPanel12);

		jPanel16.add(splitPane, java.awt.BorderLayout.CENTER);

		finishPanel.add(jPanel16, java.awt.BorderLayout.CENTER);

		step4.add(finishPanel, "bank");

		cardPanel.add(step4, "step3");

		jPanel8.add(cardPanel, java.awt.BorderLayout.CENTER);

		getContentPane().add(jPanel8, java.awt.BorderLayout.CENTER);

		pack();
	} // GEN-END:initComponents

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_cancelButtonActionPerformed
		setVisible(false);
		dispose();
	} // GEN-LAST:event_cancelButtonActionPerformed

	private void downButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_downButtonActionPerformed

		if (typeCombo.getSelectedItem().equals(
				ui.getString("fina2.reportoo.iteratorType.bank"))) {
			bankAssistant.moveTargetNodeDown();
			return;
		}

		int selectedIndex = targetTable.getSelectedRow();
		TableRow row1 = targetTable.getSelectedTableRow();
		targetTable.changeSelection(selectedIndex + 1, 0, false, false);
		TableRow row2 = targetTable.getSelectedTableRow();
		targetTable.updateRow(selectedIndex, row2);
		targetTable.updateRow(selectedIndex + 1, row1);
		targetTable.fireSelectionChangedEvent();
	} // GEN-LAST:event_downButtonActionPerformed

	private void upButtonActionPerformed(java.awt.event.ActionEvent evt) { //GEN-
		// FIRST
		// :
		// event_upButtonActionPerformed

		if (typeCombo.getSelectedItem().equals(
				ui.getString("fina2.reportoo.iteratorType.bank"))) {
			bankAssistant.moveTargetNodeUp();
			return;
		}

		int selectedIndex = targetTable.getSelectedRow();
		TableRow row1 = targetTable.getSelectedTableRow();
		targetTable.changeSelection(selectedIndex - 1, 0, false, false);
		TableRow row2 = targetTable.getSelectedTableRow();
		targetTable.updateRow(selectedIndex, row2);
		targetTable.updateRow(selectedIndex - 1, row1);
		targetTable.fireSelectionChangedEvent();
	} // GEN-LAST:event_upButtonActionPerformed

	private void textButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_textButtonActionPerformed

		TableRowImpl row = null;
		if (typeCombo.getSelectedItem().equals(
				ui.getString("fina2.reportoo.iteratorType.offset"))) {
			try {
				row = new TableRowImpl(new Integer(textField.getText()), 4);

			} catch (NumberFormatException e) {
				row = new TableRowImpl((String) textField.getText(), 4);
			}
		} else {
			row = new TableRowImpl((String) textField.getText(), 4);
		}
		row.setValue(0, textField.getText());
		int selectedIndex = targetTable.getSelectedRow();
		if (selectedIndex > -1) {
			targetTable.addRow(sourceTable.getSelectedTableRow());
			for (int i = targetTable.getRowCount() - 1; i > selectedIndex; i--) {
				targetTable.updateRow(i, targetTable.getTableRow(i - 1));
			}
			targetTable.updateRow(selectedIndex + 1, row);
			targetTable.changeSelection(selectedIndex + 1, 0, false, false);
		} else {
			targetTable.addRow(row);
		}
		targetTable.fireSelectionChangedEvent();
		checkValid();
	} // GEN-LAST:event_textButtonActionPerformed

	private void leftButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_leftButtonActionPerformed
		// ---------------------------------------------------------------------
		// Bank parameter
		if (typeCombo.getSelectedItem().equals(
				ui.getString("fina2.reportoo.iteratorType.bank"))) {
			// Just remove current selected node
			bankAssistant.removeTargetNode();
			return;
		}

		// ---------------------------------------------------------------------
		// Other type parameters
		if (targetTable.getSelectedRow() > -1) {
			int[] sel = targetTable.getSelectedRows();
			for (int i = sel.length - 1; i >= 0; i--) {
				targetTable.removeRow(sel[i]);
			}
			targetTable.fireSelectionChangedEvent();
		}

		checkValid();

	} // GEN-LAST:event_leftButtonActionPerformed

	private void rightButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_rightButtonActionPerformed

		// ---------------------------------------------------------------------
		// Bank parameter
		if (typeCombo.getSelectedItem().equals(
				ui.getString("fina2.reportoo.iteratorType.bank"))) {
			bankAssistant.moveSourceNodeToRight();
			return;
		}

		// ---------------------------------------------------------------------
		// Other type parameters
		TableRow row = null;
		Collection rows = null;
		if (typeCombo.getSelectedItem().equals(
				ui.getString("fina2.reportoo.iteratorType.node"))) {
			Collection v = tree.getSelectedNodes();
			for (Iterator it = v.iterator(); it.hasNext();) {
				Node node = (Node) it.next();
				row = new TableRowImpl((MDTNodePK) node.getPrimaryKey(), 4);
				row.setValue(0, node.getLabel());
				row.setValue(1, (String) node.getProperty("code"));

				int selectedIndex = targetTable.getSelectedRow();
				if (selectedIndex > -1) {
					targetTable.addRow(row);
					for (int i = targetTable.getRowCount() - 1; i > selectedIndex; i--) {
						targetTable
								.updateRow(i, targetTable.getTableRow(i - 1));
					}
					targetTable.updateRow(selectedIndex + 1, row);
					targetTable.changeSelection(selectedIndex + 1, 0, false,
							false);
				} else {
					targetTable.addRow(row);
				}

			}
			// Node node = tree.getSelectedNode();
			// row = new TableRowImpl((MDTNodePK)node.getPrimaryKey(), 4);
			// row.setValue(0, node.getLabel());
			// row.setValue(1, (String)node.getProperty("code"));
		} else if (typeCombo.getSelectedItem().equals(
				ui.getString("fina2.reportoo.iteratorType.peer"))) {
			Collection v = tree.getSelectedNodes();
			for (Iterator it = v.iterator(); it.hasNext();) {
				Node node = (Node) it.next();
				int nodeType = ((Integer) node.getType()).intValue();
				if (nodeType == BanksConstants.NODETYPE_NODE
						|| nodeType == BanksConstants.NODETYPE_BANK_GROUP_NODE) {
					row = new TableRowImpl(node.getPrimaryKey(), 4);
					row.setValue(0, (String) node.getProperty("code"));
					row.setValue(1, (String) node.getProperty("label"));

					int selectedIndex = targetTable.getSelectedRow();
					if (selectedIndex > -1) {
						targetTable.addRow(row);
						for (int i = targetTable.getRowCount() - 1; i > selectedIndex; i--) {
							targetTable.updateRow(i, targetTable
									.getTableRow(i - 1));
						}
						targetTable.updateRow(selectedIndex + 1, row);
						targetTable.changeSelection(selectedIndex + 1, 0,
								false, false);
					} else {
						targetTable.addRow(row);
					}
				}
			}
		} else {
			row = sourceTable.getSelectedTableRow();
			rows = sourceTable.getSelectedTableRows();

			for (Iterator iter = rows.iterator(); iter.hasNext();) {
				row = (TableRow) iter.next();
				int selectedIndex = targetTable.getSelectedRow();
				if (selectedIndex > -1) {
					targetTable.addRow(row);
					for (int i = targetTable.getRowCount() - 1; i > selectedIndex; i--) {
						targetTable
								.updateRow(i, targetTable.getTableRow(i - 1));
					}
					targetTable.updateRow(selectedIndex + 1, row);
					targetTable.changeSelection(selectedIndex + 1, 0, false,
							false);
				} else {
					targetTable.addRow(row);
				}
			}
		}
		targetTable.fireSelectionChangedEvent();
		checkValid();
	} // GEN-LAST:event_rightButtonActionPerformed

	private void columnsRadioActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_columnsRadioActionPerformed
		checkValid();
	} // GEN-LAST:event_columnsRadioActionPerformed

	private void rowsRadioActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_rowsRadioActionPerformed
		checkValid();
	} // GEN-LAST:event_rowsRadioActionPerformed

	private void typeComboActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_typeComboActionPerformed
		if (!typeCombo.getSelectedItem().equals(
				ui.getString("fina2.reportoo.iteratorType.offset"))) {
			textButton.setVisible(false);
			textLabel.setVisible(false);
			textField.setVisible(false);
		} else {
			textButton.setVisible(true);
			textLabel.setVisible(true);
			textField.setVisible(true);
		}

		checkValid();
	} // GEN-LAST:event_typeComboActionPerformed

	private void nameTextKeyReleased(java.awt.event.KeyEvent evt) { //GEN-FIRST:
		// event_nameTextKeyReleased
		checkValid();
	} // GEN-LAST:event_nameTextKeyReleased

	private void updateStep() {
		stepNameLabel.setText(ui
				.getString("fina2.reportoo.iteratorWizard.step")
				+ " " + step + ". " + names[step]);
		stepDescText.setText(descriptions[step]);

		Font uiFont = ui.getFont();
		for (int i = 1; i < labels.length; i++) {
			labels[i].setFont(new Font(uiFont.getFontName(), Font.PLAIN, uiFont
					.getSize()));
		}
		labels[step].setFont(new Font(uiFont.getFontName(), Font.BOLD, uiFont
				.getSize()));

		if (step == 1) {
			backButton.setEnabled(false);
		} else {
			backButton.setEnabled(true);
		}
		if (step == maxSteps) {
			nextButton.setEnabled(false);
			nextButton.setText(ui.getString("fina2.finish"));
			nextButton.setIcon(ui.getIcon("fina2.ok"));
		} else {
			nextButton.setEnabled(true);
			nextButton.setText(ui.getString("fina2.next"));
			nextButton.setIcon(ui.getIcon("fina2.next"));
		}
		checkValid();
		if (step == maxSteps) {
			try {
				initTable();
			} catch (Exception e) {
				e.printStackTrace();
				ui.showMessageBox(main.getMainFrame(), ui
						.getString("fina2.title"), e.toString());
			}

		}
	}

	private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_nextButtonActionPerformed
		if (step < maxSteps) {
			step++;
			((CardLayout) cardPanel.getLayout()).show(cardPanel, "step" + step);
			updateStep();
		} else {

			Collection rows = targetTable.getRows();
			Vector values = new Vector();

			// Get selected values
			if (typeCombo.getSelectedItem().equals(
					ui.getString("fina2.reportoo.iteratorType.bank"))) {
				// Bank parameter
				values = bankAssistant.getSelectedBanks();
			} else {
				// Other type parameters

				for (java.util.Iterator it = rows.iterator(); it.hasNext();) {
					TableRowImpl row = (TableRowImpl) it.next();
					if (typeCombo.getSelectedItem().equals(
							ui.getString("fina2.reportoo.iteratorType.offset"))
							|| typeCombo
									.getSelectedItem()
									.equals(
											ui
													.getString("fina2.reportoo.iteratorType.period"))) {
						values.add(row.getPrimaryKey());
					} else {
						if (typeCombo
								.getSelectedItem()
								.equals(
										ui
												.getString("fina2.reportoo.iteratorType.node"))) {
							if (row.getPrimaryKey() instanceof MDTNodePK) {
								values.add((String) row.getValue(1));
							} else {
								values.add((String) row.getValue(0));
							}
						} else {
							values.add((String) row.getValue(0));
						}
					}
				}
			}

			if (iter == null) {
				iter = new fina2.ui.sheet.openoffice.OOParameter();
				// iter.create(sheet, "", iter.BANK_ITERATOR, iter.COL_ITERATOR,
				// sheet.getStartSelCol(), sheet.getStartSelRow());
			}
			// if(rowsRadio.isSelected())
			// iter.setOrientation(iter.ROW_ITERATOR);
			// if(columnsRadio.isSelected())
			// iter.setOrientation(iter.COL_ITERATOR);
			iter.setValues(values);
			iter.setName(nameText.getText());
			iter.setType(typeCombo.getSelectedIndex());
			iter.setSheet(sheet);

			System.out.println(values);
			yes = true;
			setVisible(false);
			dispose();
		}
	} // GEN-LAST:event_nextButtonActionPerformed

	private void backButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_backButtonActionPerformed
		if (step > 1) {
			step--;
			((CardLayout) cardPanel.getLayout()).show(cardPanel, "step" + step);
			updateStep();
		}
	} // GEN-LAST:event_backButtonActionPerformed

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) { // GEN-FIRST:
		// event_closeDialog
		setVisible(false);
		dispose();
	} // GEN-LAST:event_closeDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton backButton;
	private javax.swing.JButton cancelButton;
	private javax.swing.JPanel cardPanel;
	private javax.swing.JRadioButton columnsRadio;
	private javax.swing.JButton downButton;
	private javax.swing.JPanel finishPanel;
	private javax.swing.JButton helpButton;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JLabel jLabel8;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel10;
	private javax.swing.JPanel jPanel11;
	private javax.swing.JPanel jPanel12;
	private javax.swing.JPanel jPanel13;
	private javax.swing.JPanel jPanel14;
	private javax.swing.JPanel jPanel15;
	private javax.swing.JPanel jPanel16;
	private java.awt.Component jFilterComponent;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JPanel jPanel9;
	private javax.swing.JButton leftButton;
	private javax.swing.JLabel nameLabel;
	private javax.swing.JTextField nameText;
	private javax.swing.JButton nextButton;
	private javax.swing.ButtonGroup orienationGroup;
	private javax.swing.JLabel orientationLabel;
	private javax.swing.JButton rightButton;
	private javax.swing.JRadioButton rowsRadio;
	private javax.swing.JScrollPane sourceScrollPane;
	private javax.swing.JSplitPane splitPane;
	private javax.swing.JPanel step1;
	private javax.swing.JPanel step2;
	private javax.swing.JPanel step3;
	private javax.swing.JPanel step4;
	private javax.swing.JTextArea stepDescText;
	private javax.swing.JLabel stepNameLabel;
	private javax.swing.JScrollPane targetScrollPane;
	private javax.swing.JButton textButton;
	private javax.swing.JTextField textField;
	private javax.swing.JLabel textLabel;
	private javax.swing.JComboBox typeCombo;
	private javax.swing.JLabel typeLabel;
	private javax.swing.JButton upButton;
	private javax.swing.JLabel valuesLabel;
	private javax.swing.JLabel wizardLabel;
	// End of variables declaration//GEN-END:variables
}
