package fina2.reportoo;

import java.awt.CardLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.Hashtable;
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
import fina2.returns.ReturnConstants;
import fina2.returns.ReturnDefinitionTablePK;
import fina2.returns.ReturnSession;
import fina2.returns.ReturnSessionHome;
import fina2.returns.ReturnVersion;
import fina2.returns.ReturnVersionSession;
import fina2.returns.ReturnVersionSessionHome;
import fina2.ui.UIManager;
import fina2.ui.sheet.Spreadsheet;
import fina2.ui.sheet.openoffice.OOIterator;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;
import fina2.ui.tree.EJBTree;
import fina2.ui.tree.Node;

public class IteratorWizard extends javax.swing.JDialog {

	/**
	 * BankTreeAssistant. Used for the bank trees init, loading data and etc.
	 * See initBankTreeParameters method.
	 */
	private BankParameterAssistant bankAssistant = null;

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private fina2.returns.SelectDefinitionTableDialog selectTableDialog;
	private int step = 1;

	private String[] names;
	private String[] descriptions;
	private JLabel[] labels;
	private int maxSteps;
	private int maxStepsT;

	private boolean yes = false;

	private EJBTable sourceTable;
	private EJBTable targetTable;

	private EJBTable sourceAggregateTable;
	private EJBTable targetAggregateTable;

	private EJBTable sourcePeriodTable;
	private EJBTable targetPeriodTable;

	private fina2.ui.tree.EJBTree tree;
	public OOIterator iter;
	private Collection t_rows;
	private Vector parameters;
	private Spreadsheet sheet;
	private String parameterName = null;
	private String aggregateParameterName = null;
	private String periodParameterName = null;
	private Hashtable nodes;
	private ReturnDefinitionTablePK tablePK;
	private String pref = "";
	boolean returnVersionsInitialized = false;

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

	public IteratorWizard(java.awt.Frame parent, boolean modal) {
		super(parent, modal);

		selectTableDialog = new fina2.returns.SelectDefinitionTableDialog(
				parent, true);

		names = new String[] { "",
				ui.getString("fina2.reportoo.iteratorWizard.step1Name"),
				ui.getString("fina2.reportoo.iteratorWizard.step2Name"),
				ui.getString("fina2.reportoo.iteratorWizard.step3Name"),
				ui.getString("fina2.reportoo.iteratorWizard.step4Name"),
				ui.getString("fina2.reportoo.iteratorWizard.step5Name"),
				ui.getString("fina2.reportoo.iteratorWizard.step6Name"),
				ui.getString("fina2.reportoo.iteratorWizard.step7Name") };
		descriptions = new String[] { "",
				ui.getString("fina2.reportoo.iteratorWizard.step1Description"),
				ui.getString("fina2.reportoo.iteratorWizard.step2Description"),
				ui.getString("fina2.reportoo.iteratorWizard.step3Description"),
				ui.getString("fina2.reportoo.iteratorWizard.step4Description"),
				ui.getString("fina2.reportoo.iteratorWizard.step5Description"),
				ui.getString("fina2.reportoo.iteratorWizard.step6Description"),
				ui.getString("fina2.reportoo.iteratorWizard.step7Description") };
		maxSteps = 4;
		maxStepsT = 7;

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

		sourceAggregateTable = new EJBTable();
		targetAggregateTable = new EJBTable();

		sourcePeriodTable = new EJBTable();
		targetPeriodTable = new EJBTable();

		targetTable.setAllowSort(false);

		sourceTable
				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		targetTable
				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		sourceAggregateTable
				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		targetAggregateTable
				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		sourcePeriodTable
				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		targetPeriodTable
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

		sourcePeriodTable
				.addSelectionListener(new javax.swing.event.ListSelectionListener() {
					public void valueChanged(
							javax.swing.event.ListSelectionEvent evt) {
						// if(periodParameterCombo.getSelectedIndex()>0) {
						if (sourcePeriodTable.getSelectedRow() == -1) {
							periodRightButton.setEnabled(false);
						} else {
							periodRightButton.setEnabled(true);
						}
						// }
					}
				});

		sourceAggregateTable
				.addSelectionListener(new javax.swing.event.ListSelectionListener() {
					public void valueChanged(
							javax.swing.event.ListSelectionEvent evt) {
						// if(aggregateParameterCombo.getSelectedIndex()>0) {
						if (sourceAggregateTable.getSelectedRow() == -1) {
							aggregateRightButton.setEnabled(false);
						} else {
							aggregateRightButton.setEnabled(true);
						}
						// }
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

		targetAggregateTable
				.addSelectionListener(new javax.swing.event.ListSelectionListener() {
					public void valueChanged(
							javax.swing.event.ListSelectionEvent evt) {
						// if(aggregateParameterCombo.getSelectedIndex()>0) {
						if (targetAggregateTable.getSelectedRow() == -1) {
							aggregateLeftButton.setEnabled(false);
						} else {
							aggregateLeftButton.setEnabled(true);
						}
						// }
					}
				});

		targetPeriodTable
				.addSelectionListener(new javax.swing.event.ListSelectionListener() {
					public void valueChanged(
							javax.swing.event.ListSelectionEvent evt) {
						// if(periodParameterCombo.getSelectedIndex()>0) {
						if (targetPeriodTable.getSelectedRow() == -1) {
							periodLeftButton.setEnabled(false);
						} else {
							periodLeftButton.setEnabled(true);
						}
						// }
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

		Vector v = new Vector(7);
		v.add(ui.getString("fina2.select"));
		v.add(ui.getString("fina2.reportoo.iteratorType.bank"));
		v.add(ui.getString("fina2.reportoo.iteratorType.peer"));
		v.add(ui.getString("fina2.reportoo.iteratorType.node"));
		v.add(ui.getString("fina2.reportoo.iteratorType.period"));
		v.add(ui.getString("fina2.reportoo.iteratorType.offset"));
		v.add(ui.getString("fina2.reportoo.iteratorType.table"));
		typeCombo.setModel(new javax.swing.DefaultComboBoxModel(new Vector(v)));

		v = new Vector(1);
		v.add(ui.getString("fina2.none"));
		parameterCombo.setModel(new javax.swing.DefaultComboBoxModel(
				new Vector(v)));

		v = new Vector(3);
		v.add(ui.getString("fina2.select"));
		v.add(ui.getString("fina2.reportoo.iteratorType.bank"));
		v.add(ui.getString("fina2.reportoo.iteratorType.peer"));
		aggregateCombo.setModel(new javax.swing.DefaultComboBoxModel(
				new Vector(v)));

		labels = new JLabel[] { null, nameLabel, typeLabel, orientationLabel,
				valuesLabel, label5, label6, label7 };

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
			if (!rowsRadio.isSelected() && !columnsRadio.isSelected()
					|| !checkRange()) {
				nextButton.setEnabled(false);
			} else {
				nextButton.setEnabled(true);
			}
			break;
		case 3:
			if (typeCombo.getSelectedIndex() < 1) {
				nextButton.setEnabled(false);
			} else {
				nextButton.setEnabled(true);
			}
			break;
		case 4:
			if ((typeCombo.getSelectedIndex() != fina2.ui.sheet.openoffice.OOIterator.VCT_ITERATOR)
					|| ((typeCombo.getSelectedIndex() == fina2.ui.sheet.openoffice.OOIterator.VCT_ITERATOR) && (groupbyCombo
							.getSelectedIndex() > 0))) {
				nextButton.setEnabled(true);
			} else {
				nextButton.setEnabled(false);
			}
			break;

		case 5:
			if (aggregateCombo.getSelectedIndex() > 0) {
				nextButton.setEnabled(true);
			} else {
				nextButton.setEnabled(false);
			}
			break;
		case 6:
			nextButton.setEnabled(true);
			break;
		case 7:
			nextButton.setEnabled(true);
			break;

		}
	}

	private void initAggregateTable() {
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

			if (aggregateCombo.getSelectedItem().equals(
					ui.getString("fina2.reportoo.iteratorType.bank"))) {
				if (iter != null && iter.getAggregateValues() != null) {
					rows = (Vector) session.getBankValues(iter
							.getAggregateValues(), main.getUserHandle(), main
							.getLanguageHandle());
				} else {
					rows = new Vector();
				}
				initBanks(rows, sourceAggregateTable, targetAggregateTable);
				aggregateSourceScrollPanel
						.setViewportView(sourceAggregateTable);
				aggregateTargetScrollPanel
						.setViewportView(targetAggregateTable);
			}
			if (aggregateCombo.getSelectedItem().equals(
					ui.getString("fina2.reportoo.iteratorType.peer"))) {
				if (iter != null && iter.getAggregateValues() != null) {
					rows = (Vector) session.getPeerValues(iter
							.getAggregateValues(), main.getUserHandle(), main
							.getLanguageHandle());
				} else {
					rows = new Vector();
				}

				initPeers(rows, sourceAggregateTable, targetAggregateTable);
				aggregateSourceScrollPanel
						.setViewportView(sourceAggregateTable);
				aggregateTargetScrollPanel
						.setViewportView(targetAggregateTable);
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		checkValid();

	}

	private void initPeriodTable() {
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

			if (iter != null && iter.getPeriodValues() != null) {
				rows = (Vector) session.getPeriodValues(iter.getPeriodValues(),
						main.getUserHandle(), main.getLanguageHandle());
			} else {
				rows = new Vector();
			}

			initPeriods(rows, sourcePeriodTable, targetPeriodTable);
			periodSourceScrollPanel.setViewportView(sourcePeriodTable);
			periodTargetScrollPanel.setViewportView(targetPeriodTable);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		checkValid();

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
				initNodes(rows, sourceTable, targetTable);
				sourceScrollPane.setViewportView(tree);
				targetScrollPane.setViewportView(targetTable);
			}
			if (typeCombo.getSelectedItem().equals(
					ui.getString("fina2.reportoo.iteratorType.peer"))) {
				if (iter != null && iter.getValues() != null) {
					rows = (Vector) session.getPeerValues(iter.getValues(),
							main.getUserHandle(), main.getLanguageHandle());
				}
				initPeers(rows, sourceTable, targetTable);
				sourceScrollPane.setViewportView(tree);
				targetScrollPane.setViewportView(targetTable);
				for (int i = 0; i < tree.getRowCount(); i++) {
					tree.expandRow(i);
				}
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
				initOffsets(rows, sourceTable, targetTable);
				targetScrollPane.setViewportView(targetTable);
			}
			if (typeCombo.getSelectedItem().equals(
					ui.getString("fina2.reportoo.iteratorType.period"))) {
				if (iter != null && iter.getValues() != null) {
					rows = (Vector) session.getPeriodValues(iter.getValues(),
							main.getUserHandle(), main.getLanguageHandle());
				}
				allowFilterPannel();
				initPeriods(rows, sourceTable, targetTable);
				sourceScrollPane.setViewportView(sourceTable);
				targetScrollPane.setViewportView(targetTable);
			}

			if (typeCombo.getSelectedItem().equals(
					ui.getString("fina2.reportoo.iteratorType.table"))) {
				initReturnVersions();
			}
		} catch (Exception e) {
			e.printStackTrace();
			ui.showMessageBox(null, e.getMessage());
		}
		checkValid();

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

	private void initPeriods(Vector rows, EJBTable source, EJBTable target) {
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

			source.initTable(colNames, sourceRows);
			target.initTable(colNames, targetRows);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private Vector getPeriodColumns() {
		return periodColumn;
	}

	private void initOffsets(Vector rows, EJBTable source, EJBTable target) {

		Vector colNames = new Vector();
		colNames.add(ui.getString("fina2.reportoo.parameterType.offset"));
		Vector sourceRows = new Vector();
		Vector targetRows = rows;
		source.initTable(colNames, sourceRows);
		target.initTable(colNames, targetRows);
	}

	private void initPeers(Vector rows, EJBTable source, EJBTable target) {

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
		source.initTable(colNames, sourceRows);
		target.initTable(colNames, targetRows);
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

	private void initNodes(Vector rows, EJBTable source, EJBTable target) {

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
		source.initTable(colNames, sourceRows);
		target.initTable(colNames, targetRows);
	}

	private void initBanks(Vector rows, EJBTable source, EJBTable target) {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/bank/BankSession");
			BankSessionHome home = (BankSessionHome) PortableRemoteObject
					.narrow(ref, BankSessionHome.class);

			BankSession session = home.create();

			Vector colNames = new Vector();
			colNames.add(ui.getString("fina2.code"));
			colNames.add(ui.getString("fina2.description"));
			Vector sourceRows = (Vector) session.getBanksRows(main
					.getUserHandle(), main.getLanguageHandle());
			Vector targetRows = rows;
			source.initTable(colNames, sourceRows);
			target.initTable(colNames, targetRows);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

	}

	private void initReturnVersions() {

		if (!returnVersionsInitialized) {
			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/returns/ReturnVersionSession");
				ReturnVersionSessionHome home = (ReturnVersionSessionHome) PortableRemoteObject
						.narrow(ref, ReturnVersionSessionHome.class);
				ReturnVersionSession session = home.create();

				Collection versions = session.getReturnVersions(main
						.getLanguageHandle(), main.getUserHandle());

				jVersionCodeCombo.removeAllItems();
				for (Iterator iter = versions.iterator(); iter.hasNext();) {
					ReturnVersion rv = (ReturnVersion) iter.next();
					jVersionCodeCombo.addItem(rv.getCode());
				}

				if (iter != null && iter.getVersionCode() != null) {

					if (ReportConstants.LATEST_VERSION.equals(iter
							.getVersionCode())) {
						jLatestVersionCheckBox.setSelected(true);
						jVersionCodeCombo.setEnabled(false);
					} else {
						jVersionCodeCombo
								.setSelectedItem(iter.getVersionCode());
						jLatestVersionCheckBox.setSelected(false);
						jVersionCodeCombo.setEnabled(true);
					}
				}

				returnVersionsInitialized = true;
			} catch (Exception e) {
				Main.generalErrorHandler(e);
			}
		}
	}

	private boolean checkRange() {
		int sel_start = 0;
		int sel_end = 0;
		int cur_orient = 0;

		if (columnsRadio.isSelected()) {
			sel_start = sheet.getStartSelCol();
			sel_end = sheet.getEndSelCol();
			cur_orient = fina2.ui.sheet.openoffice.OOIterator.COL_ITERATOR;
		}
		if (rowsRadio.isSelected()) {
			sel_start = sheet.getStartSelRow();
			sel_end = sheet.getEndSelRow();
			cur_orient = fina2.ui.sheet.openoffice.OOIterator.ROW_ITERATOR;
		}

		for (Iterator it = t_rows.iterator(); it.hasNext();) {
			TableRow row = (TableRow) it.next();
			if (row.getPrimaryKey() instanceof fina2.ui.sheet.openoffice.OOIterator) {
				fina2.ui.sheet.openoffice.OOIterator i = (fina2.ui.sheet.openoffice.OOIterator) row
						.getPrimaryKey();
				int start = i.getStart();
				int end = i.getEnd();
				int orient = i.getOrientation();

				if (orient == cur_orient
						&& (sel_start < start && ((sel_end > start || sel_end == start) && (sel_end < end || sel_end == end)))) {
					return false;
				}
				if (orient == cur_orient
						&& ((sel_start < end || sel_start == end) && sel_end > end)) {
					return false;
				}
				if (orient == cur_orient && sel_start < start && sel_end > end) {
					return false;
				}
			}
		}
		return true;
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

	private void getTableName(ReturnDefinitionTablePK tablePK) {

		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject
					.narrow(ref, ReturnSessionHome.class);

			ReturnSession session = home.create();

			Collection rows = session.getAllDefinitionTables(main
					.getLanguageHandle());

			for (Iterator it = rows.iterator(); it.hasNext();) {
				TableRow row = (TableRow) it.next();
				if (((ReturnDefinitionTablePK) row.getPrimaryKey())
						.equals(tablePK)) {
					tableNameText.setText("[" + row.getValue(0) + "]["
							+ row.getValue(2) + "] " + row.getValue(3));
				}
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private void loadTable(ReturnDefinitionTablePK tablePK) {
		Vector v = new Vector();
		v.add(ui.getString("fina2.select"));
		groupbyCombo.setModel(new javax.swing.DefaultComboBoxModel(
				new Vector(v)));
		if (tablePK.getType() == ReturnConstants.TABLETYPE_VARIABLE) {
			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/metadata/MDTSession");
				MDTSessionHome home = (MDTSessionHome) PortableRemoteObject
						.narrow(ref, MDTSessionHome.class);

				MDTSession session = home.create();

				Collection _nodes = session.getChNodes(new MDTNodePK(tablePK
						.getNodeID()));

				for (Iterator it = _nodes.iterator(); it.hasNext();) {
					Node node = (Node) it.next();
					v.add(node.getLabel());

				}
				groupbyCombo.setModel(new javax.swing.DefaultComboBoxModel(
						new Vector(v)));

				if (iter != null) {
					if (iter.getGroupCode() != null) {
						String selCode = iter.getGroupCode();
						int i = 0;
						for (Iterator it = v.iterator(); it.hasNext();) {
							String code = (String) it.next();
							if (i > 0) {
								if (code.substring(code.indexOf('[') + 1,
										code.indexOf(']')).equals(selCode)) {
									groupbyCombo.setSelectedIndex(i);
								}
							}
							i++;
						}
					}

				}

			} catch (Exception e) {
				Main.generalErrorHandler(e);
				cancelButtonActionPerformed(null);
			}
		} else {
		}
	}

	public void show(fina2.ui.sheet.openoffice.OOIterator iter,
			Collection t_rows, Collection parameters, Spreadsheet sheet) {

		this.iter = iter;
		this.t_rows = t_rows;
		this.sheet = sheet;
		this.parameters = new Vector(parameters);
		step = 1;
		rightButton.setEnabled(false);
		leftButton.setEnabled(false);

		setSize(getParent().getWidth() - 60, getParent().getHeight() - 60);
		splitPane.setDividerLocation((getParent().getWidth() - 200) / 2);
		aggregateSplitPane
				.setDividerLocation((getParent().getWidth() - 200) / 2);
		periodSplitPane.setDividerLocation((getParent().getWidth() - 200) / 2);
		setLocationRelativeTo(getParent());
		if (iter != null) {
			nameText.setText(iter.getName());
			typeCombo.setSelectedIndex(iter.getType());
			if (iter.getOrientation() == fina2.ui.sheet.Iterator.ROW_ITERATOR)
				rowsRadio.setSelected(true);
			if (iter.getOrientation() == fina2.ui.sheet.Iterator.COL_ITERATOR)
				columnsRadio.setSelected(true);
		}
		if (iter != null) {
			rowsRadio.setEnabled(false);
			columnsRadio.setEnabled(false);
		}
		if (iter != null) {
			parameterName = iter.getParameter();
			if (parameterName != null) {
				parameterCombo.setSelectedItem(parameterName);
			} else {
				parameterCombo.setSelectedIndex(0);
			}
		}
		if (iter != null) {
			aggregateCombo.setSelectedIndex(iter.getAggregateType());
			aggregateParameterName = iter.getAggergateParameter();
			if (aggregateParameterName != null) {
				aggregateParameterCombo.setSelectedItem(aggregateParameterName);
			} else {
				aggregateParameterCombo.setSelectedIndex(0);
			}
			periodParameterName = iter.getPeriodParameter();
			if (periodParameterName != null) {
				periodParameterCombo.setSelectedItem(periodParameterName);
			} else {
				periodParameterCombo.setSelectedIndex(0);
			}
		}

		if (iter != null) {
			if (iter.getTable() != null) {
				tablePK = (ReturnDefinitionTablePK) iter.getTable();
				getTableName((ReturnDefinitionTablePK) iter.getTable());
				loadTable((ReturnDefinitionTablePK) iter.getTable());
			}
		}

		updateStep();
		super.show();

	}

	public boolean Ok() {
		return yes;
	}

	public fina2.ui.sheet.openoffice.OOIterator getIterator() {
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
		label5 = new javax.swing.JLabel();
		label6 = new javax.swing.JLabel();
		label7 = new javax.swing.JLabel();
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
		jLabel2 = new javax.swing.JLabel();
		parameterCombo = new javax.swing.JComboBox();
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
		step4t = new javax.swing.JPanel();
		jLabel3 = new javax.swing.JLabel();
		tableNameText = new javax.swing.JTextField();
		selectTableButton = new javax.swing.JButton();
		jLabel4 = new javax.swing.JLabel();
		groupbyCombo = new javax.swing.JComboBox();
		step5t = new javax.swing.JPanel();
		jLabel5 = new javax.swing.JLabel();
		aggregateCombo = new javax.swing.JComboBox();
		step6t = new javax.swing.JPanel();
		jPanel18 = new javax.swing.JPanel();
		jPanel19 = new javax.swing.JPanel();
		jLabel9 = new javax.swing.JLabel();
		aggregateParameterCombo = new javax.swing.JComboBox();
		jPanel20 = new javax.swing.JPanel();
		jPanel21 = new javax.swing.JPanel();
		aggregateSplitPane = new javax.swing.JSplitPane();
		jPanel22 = new javax.swing.JPanel();
		aggregateSourceScrollPanel = new javax.swing.JScrollPane();
		jPanel23 = new javax.swing.JPanel();
		aggregateTargetScrollPanel = new javax.swing.JScrollPane();
		jPanel26 = new javax.swing.JPanel();
		aggregateRightButton = new javax.swing.JButton();
		aggregateLeftButton = new javax.swing.JButton();
		step7t = new javax.swing.JPanel();
		jPanel24 = new javax.swing.JPanel();
		jPanel25 = new javax.swing.JPanel();
		jLabel10 = new javax.swing.JLabel();
		periodParameterCombo = new javax.swing.JComboBox();
		jPanel17 = new javax.swing.JPanel();
		jPanel28 = new javax.swing.JPanel();
		periodSplitPane = new javax.swing.JSplitPane();
		jPanel29 = new javax.swing.JPanel();
		periodSourceScrollPanel = new javax.swing.JScrollPane();
		jPanel30 = new javax.swing.JPanel();
		periodTargetScrollPanel = new javax.swing.JScrollPane();
		jPanel31 = new javax.swing.JPanel();
		periodRightButton = new javax.swing.JButton();
		periodLeftButton = new javax.swing.JButton();
		jVersionLabel = new javax.swing.JLabel();
		jVersionCodeCombo = new javax.swing.JComboBox();
		jLatestVersionCheckBox = new javax.swing.JCheckBox();

		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jPanel1.setLayout(new java.awt.BorderLayout());

		jPanel1.setBackground(new java.awt.Color(45, 31, 173));
		jPanel2.setLayout(new java.awt.GridLayout(9, 1));

		jPanel2.setBorder(new javax.swing.border.EmptyBorder(
				new java.awt.Insets(1, 10, 1, 10)));
		jPanel2.setBackground(new java.awt.Color(45, 31, 173));
		wizardLabel.setText(ui.getString("fina2.reportoo.iteratorWizard"));
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

		label5.setText(ui.getString("fina2.reportoo.iteratorWizard.step5Name"));
		label5.setForeground(java.awt.Color.white);
		label5.setFont(ui.getFont());
		jPanel2.add(label5);

		label6.setText(ui.getString("fina2.reportoo.iteratorWizard.step6Name"));
		label6.setForeground(java.awt.Color.white);
		label6.setFont(ui.getFont());
		jPanel2.add(label6);

		label7.setText(ui.getString("fina2.reportoo.iteratorWizard.step7Name"));
		label7.setForeground(java.awt.Color.white);
		label7.setFont(ui.getFont());
		jPanel2.add(label7);

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

		cardPanel.add(step2, "step2");

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

		jLabel2.setText(ui.getString("fina2.reportoo.parameter"));
		jLabel2.setFont(ui.getFont());
		step3.add(jLabel2, UIManager.getGridBagConstraints(0, 1, -1, -1, -1,
				-1, GridBagConstraints.EAST, -1, new java.awt.Insets(25, 0, 0,
						10)));

		parameterCombo.setFont(ui.getFont());
		parameterCombo.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				parameterComboActionPerformed(evt);
			}
		});
		step3.add(parameterCombo, UIManager.getGridBagConstraints(1, 1, -1, -1,
				-1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new java.awt.Insets(25, 0, 0, 0)));

		cardPanel.add(step3, "step3");

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

		cardPanel.add(step4, "step4");

		step4t.setLayout(new java.awt.GridBagLayout());

		jLabel3.setText(ui.getString("fina2.report.table"));
		jLabel3.setFont(ui.getFont());
		step4t.add(jLabel3, UIManager.getGridBagConstraints(0, 0, 10, 10, -1,
				-1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
				null));

		tableNameText.setEditable(false);
		tableNameText.setColumns(27);
		tableNameText.setFont(ui.getFont());
		step4t.add(tableNameText, UIManager.getGridBagConstraints(1, 0, -1, -1,
				-1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				null));

		selectTableButton.setIcon(ui.getIcon("fina2.find"));
		selectTableButton.setFont(ui.getFont());
		selectTableButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		selectTableButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						selectTableButtonActionPerformed(evt);
					}
				});
		step4t.add(selectTableButton, UIManager.getGridBagConstraints(2, 0, -1,
				-1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(0,
						5, 0, 0)));

		jLabel4.setText(ui.getString("fina2.reportoo.iteratorWizard.groupby"));
		jLabel4.setFont(ui.getFont());
		step4t.add(jLabel4, UIManager.getGridBagConstraints(0, 1, 10, 10, -1,
				-1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
				new java.awt.Insets(25, 0, 0, 0)));

		groupbyCombo.setFont(ui.getFont());
		groupbyCombo.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				groupbyComboActionPerformed(evt);
			}
		});
		step4t.add(groupbyCombo, UIManager.getGridBagConstraints(1, 1, -1, -1,
				-1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new java.awt.Insets(25, 0, 0, 0)));

		jVersionLabel.setText(ui
				.getString("fina2.reportoo.iteratorWizard.returnVersion"));
		jVersionLabel.setFont(ui.getFont());
		step4t.add(jVersionLabel, UIManager.getGridBagConstraints(0, 2, 10, 10,
				-1, -1, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
				new java.awt.Insets(25, 0, 0, 0)));

		jVersionCodeCombo.setFont(ui.getFont());
		jVersionCodeCombo.setEnabled(false);
		step4t.add(jVersionCodeCombo, UIManager
				.getGridBagConstraints(1, 2, -1, -1, -1, -1,
						GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new java.awt.Insets(25, 0, 0, 0)));

		jLatestVersionCheckBox.setText(ui
				.getString("fina2.reportoo.iteratorWizard.latestVersion"));
		jLatestVersionCheckBox.setFont(ui.getFont());
		jLatestVersionCheckBox.setSelected(true);
		jLatestVersionCheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						latestChecked(evt);
					}
				});
		step4t.add(jLatestVersionCheckBox, UIManager
				.getGridBagConstraints(2, 2, -1, -1, -1, -1,
						GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new java.awt.Insets(25, 0, 0, 0)));
		cardPanel.add(step4t, "step4t");

		step5t.setLayout(new java.awt.GridBagLayout());

		jLabel5.setText(ui
				.getString("fina2.reportoo.iteratorWizard.aggregateby"));
		jLabel5.setFont(ui.getFont());
		step5t.add(jLabel5, UIManager.getGridBagConstraints(0, 0, -1, -1, -1,
				-1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 0, 0,
						10)));

		aggregateCombo.setFont(ui.getFont());
		aggregateCombo.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				aggregateComboActionPerformed(evt);
			}
		});
		step5t.add(aggregateCombo, UIManager.getGridBagConstraints(1, 0, -1,
				-1, -1, -1, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, null));

		cardPanel.add(step5t, "step5t");

		step6t.setLayout(new java.awt.CardLayout());

		jPanel18.setLayout(new java.awt.BorderLayout());

		jPanel19.setLayout(new java.awt.GridLayout(1, 0, 10, 10));

		jLabel9.setText(ui.getString("fina2.reportoo.parameter"));
		jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel9.setFont(ui.getFont());
		jPanel19.add(jLabel9);

		aggregateParameterCombo.setFont(ui.getFont());
		aggregateParameterCombo
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						aggregateParameterComboActionPerformed(evt);
					}
				});

		jPanel19.add(aggregateParameterCombo);

		jPanel19.add(jPanel20);

		jPanel18.add(jPanel19, java.awt.BorderLayout.SOUTH);

		jPanel21.setLayout(new java.awt.BorderLayout());

		aggregateSplitPane.setDividerSize(4);
		jPanel22.setLayout(new java.awt.BorderLayout());

		jPanel22.add(aggregateSourceScrollPanel, java.awt.BorderLayout.CENTER);

		aggregateSplitPane.setLeftComponent(jPanel22);

		jPanel23.setLayout(new java.awt.BorderLayout());

		jPanel23.add(aggregateTargetScrollPanel, java.awt.BorderLayout.CENTER);

		jPanel26.setLayout(new java.awt.GridBagLayout());

		aggregateRightButton.setIcon(ui.getIcon("fina2.next"));
		aggregateRightButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		aggregateRightButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						aggregateRightButtonActionPerformed(evt);
					}
				});
		jPanel26.add(aggregateRightButton, UIManager.getGridBagConstraints(0,
				0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL,
				new java.awt.Insets(0, 5, 5, 5)));

		aggregateLeftButton.setIcon(ui.getIcon("fina2.back"));
		aggregateLeftButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		aggregateLeftButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						aggregateLeftButtonActionPerformed(evt);
					}
				});
		jPanel26.add(aggregateLeftButton, UIManager.getGridBagConstraints(0, 1,
				-1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL,
				new java.awt.Insets(0, 5, 0, 5)));

		jPanel23.add(jPanel26, java.awt.BorderLayout.WEST);

		aggregateSplitPane.setRightComponent(jPanel23);

		jPanel21.add(aggregateSplitPane, java.awt.BorderLayout.CENTER);

		jPanel18.add(jPanel21, java.awt.BorderLayout.CENTER);

		step6t.add(jPanel18, "bank");

		cardPanel.add(step6t, "step6t");

		step7t.setLayout(new java.awt.CardLayout());

		jPanel24.setLayout(new java.awt.BorderLayout());

		jPanel25.setLayout(new java.awt.GridLayout(1, 0, 10, 10));

		jLabel10.setText(ui.getString("fina2.reportoo.parameter"));
		jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		jLabel10.setFont(ui.getFont());
		jPanel25.add(jLabel10);

		periodParameterCombo.setFont(ui.getFont());
		periodParameterCombo
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						periodParameterComboActionPerformed(evt);
					}
				});

		jPanel25.add(periodParameterCombo);

		jPanel25.add(jPanel17);

		jPanel24.add(jPanel25, java.awt.BorderLayout.SOUTH);

		jPanel28.setLayout(new java.awt.BorderLayout());

		periodSplitPane.setDividerSize(4);
		jPanel29.setLayout(new java.awt.BorderLayout());

		jPanel29.add(periodSourceScrollPanel, java.awt.BorderLayout.CENTER);

		periodSplitPane.setLeftComponent(jPanel29);

		jPanel30.setLayout(new java.awt.BorderLayout());

		jPanel30.add(periodTargetScrollPanel, java.awt.BorderLayout.CENTER);

		jPanel31.setLayout(new java.awt.GridBagLayout());

		periodRightButton.setIcon(ui.getIcon("fina2.next"));
		periodRightButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		periodRightButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						periodRightButtonActionPerformed(evt);
					}
				});
		jPanel31.add(periodRightButton, UIManager.getGridBagConstraints(0, 0,
				-1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL,
				new java.awt.Insets(0, 5, 5, 5)));

		periodLeftButton.setIcon(ui.getIcon("fina2.back"));
		periodLeftButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		periodLeftButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				periodLeftButtonActionPerformed(evt);
			}
		});
		jPanel31.add(periodLeftButton, UIManager.getGridBagConstraints(0, 1,
				-1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL,
				new java.awt.Insets(0, 5, 0, 5)));

		jPanel30.add(jPanel31, java.awt.BorderLayout.WEST);

		periodSplitPane.setRightComponent(jPanel30);

		jPanel28.add(periodSplitPane, java.awt.BorderLayout.CENTER);

		jPanel24.add(jPanel28, java.awt.BorderLayout.CENTER);

		step7t.add(jPanel24, "bank");

		cardPanel.add(step7t, "step7t");

		jPanel8.add(cardPanel, java.awt.BorderLayout.CENTER);

		getContentPane().add(jPanel8, java.awt.BorderLayout.CENTER);

		pack();
	} // GEN-END:initComponents

	private void periodParameterComboActionPerformed(
			java.awt.event.ActionEvent evt) { // GEN-FIRST:
		// event_periodParameterComboActionPerformed
		if (periodParameterCombo.getSelectedIndex() > 0) {
			sourcePeriodTable.setVisible(false);
			targetPeriodTable.setVisible(false);
			periodSourceScrollPanel.setVisible(false);
			periodTargetScrollPanel.setVisible(false);
			periodRightButton.setVisible(false);
			periodLeftButton.setVisible(false);
		} else {
			sourcePeriodTable.setVisible(true);
			targetPeriodTable.setVisible(true);
			periodSourceScrollPanel.setVisible(true);
			periodTargetScrollPanel.setVisible(true);
			periodRightButton.setVisible(true);
			periodLeftButton.setVisible(true);
		}
	} // GEN-LAST:event_periodParameterComboActionPerformed

	private void aggregateParameterComboActionPerformed(
			java.awt.event.ActionEvent evt) { // GEN-FIRST:
		// event_aggregateParameterComboActionPerformed
		if (aggregateParameterCombo.getSelectedIndex() > 0) {
			sourceAggregateTable.setVisible(false);
			targetAggregateTable.setVisible(false);
			aggregateSourceScrollPanel.setVisible(false);
			aggregateTargetScrollPanel.setVisible(false);
			aggregateRightButton.setVisible(false);
			aggregateLeftButton.setVisible(false);
		} else {
			sourceAggregateTable.setVisible(true);
			targetAggregateTable.setVisible(true);
			aggregateSourceScrollPanel.setVisible(true);
			aggregateTargetScrollPanel.setVisible(true);
			aggregateRightButton.setVisible(true);
			aggregateLeftButton.setVisible(true);
		}
	} // GEN-LAST:event_aggregateParameterComboActionPerformed

	private void periodLeftButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_periodLeftButtonActionPerformed
		if (targetPeriodTable.getSelectedRow() > -1) {
			int[] sel = targetPeriodTable.getSelectedRows();
			for (int i = sel.length - 1; i >= 0; i--) {
				targetPeriodTable.removeRow(sel[i]);
			}
			targetPeriodTable.fireSelectionChangedEvent();
		}
		checkValid();
	} // GEN-LAST:event_periodLeftButtonActionPerformed

	private void aggregateLeftButtonActionPerformed(
			java.awt.event.ActionEvent evt) { // GEN-FIRST:
		// event_aggregateLeftButtonActionPerformed
		if (targetAggregateTable.getSelectedRow() > -1) {
			int[] sel = targetAggregateTable.getSelectedRows();
			for (int i = sel.length - 1; i >= 0; i--) {
				targetAggregateTable.removeRow(sel[i]);
			}
			targetAggregateTable.fireSelectionChangedEvent();
		}
		checkValid();
	} // GEN-LAST:event_aggregateLeftButtonActionPerformed

	private void periodRightButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_periodRightButtonActionPerformed
		TableRow row = null;
		Collection rows = null;
		// row = sourcePeriodTable.getSelectedTableRow();
		rows = sourcePeriodTable.getSelectedTableRows();
		for (Iterator iter = rows.iterator(); iter.hasNext();) {
			row = (TableRow) iter.next();
			int selectedIndex = targetPeriodTable.getSelectedRow();
			if (selectedIndex > -1) {
				targetPeriodTable.addRow(row);
				for (int i = targetPeriodTable.getRowCount() - 1; i > selectedIndex; i--) {
					targetPeriodTable.updateRow(i, targetPeriodTable
							.getTableRow(i - 1));
				}
				targetPeriodTable.updateRow(selectedIndex + 1, row);
				targetPeriodTable.changeSelection(selectedIndex + 1, 0, false,
						false);
			} else {
				targetPeriodTable.addRow(row);
			}
		}
		targetPeriodTable.fireSelectionChangedEvent();
		checkValid();
	} // GEN-LAST:event_periodRightButtonActionPerformed

	private void aggregateRightButtonActionPerformed(
			java.awt.event.ActionEvent evt) { // GEN-FIRST:
		// event_aggregateRightButtonActionPerformed
		TableRow row = null;
		Collection rows = null;
		// row = sourceAggregateTable.getSelectedTableRow();
		rows = sourceAggregateTable.getSelectedTableRows();
		for (Iterator iter = rows.iterator(); iter.hasNext();) {
			row = (TableRow) iter.next();
			int selectedIndex = targetAggregateTable.getSelectedRow();
			if (selectedIndex > -1) {
				targetAggregateTable.addRow(row);
				for (int i = targetAggregateTable.getRowCount() - 1; i > selectedIndex; i--) {
					targetAggregateTable.updateRow(i, targetAggregateTable
							.getTableRow(i - 1));
				}
				targetAggregateTable.updateRow(selectedIndex + 1, row);
				targetAggregateTable.changeSelection(selectedIndex + 1, 0,
						false, false);
			} else {
				targetAggregateTable.addRow(row);
			}
		}
		targetAggregateTable.fireSelectionChangedEvent();
		checkValid();
	} // GEN-LAST:event_aggregateRightButtonActionPerformed

	private void aggregateComboActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_aggregateComboActionPerformed
		Vector v = new Vector(1);
		v.add(ui.getString("fina2.none"));
		if (parameters != null) {
			for (Iterator it = parameters.iterator(); it.hasNext();) {
				fina2.ui.sheet.openoffice.OOParameter param = (fina2.ui.sheet.openoffice.OOParameter) it
						.next();
				if (param.getType() == aggregateCombo.getSelectedIndex()) {
					v.add(param.getName());
				}
			}
		}
		aggregateParameterCombo.setModel(new javax.swing.DefaultComboBoxModel(
				new Vector(v)));
		if (iter != null) {
			aggregateParameterName = iter.getAggergateParameter();
			if (aggregateParameterName != null)
				aggregateParameterCombo.setSelectedItem(aggregateParameterName);
		}

		v = new Vector(1);
		v.add(ui.getString("fina2.none"));
		if (parameters != null) {
			for (Iterator it = parameters.iterator(); it.hasNext();) {
				fina2.ui.sheet.openoffice.OOParameter param = (fina2.ui.sheet.openoffice.OOParameter) it
						.next();
				if (param.getType() == fina2.ui.sheet.openoffice.OOParameter.PERIOD_ITERATOR) {
					v.add(param.getName());
				}
			}
		}
		periodParameterCombo.setModel(new javax.swing.DefaultComboBoxModel(
				new Vector(v)));
		if (iter != null) {
			periodParameterName = iter.getPeriodParameter();
			if (periodParameterName != null)
				periodParameterCombo.setSelectedItem(periodParameterName);
		}
		checkValid();
	} // GEN-LAST:event_aggregateComboActionPerformed

	private void groupbyComboActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_groupbyComboActionPerformed
		checkValid();
	} // GEN-LAST:event_groupbyComboActionPerformed

	private void latestChecked(java.awt.event.ActionEvent evt) { // GEN-FIRST:
		// event_groupbyComboActionPerformed

		jVersionCodeCombo.setEnabled(!jLatestVersionCheckBox.isSelected());

	} // GEN-LAST:event_groupbyComboActionPerformed

	private void selectTableButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_selectTableButtonActionPerformed
		selectTableDialog.show();

		nodes = null;

		fina2.ui.table.TableRow row = selectTableDialog.getTableRow();
		if (row == null)
			return;

		tablePK = (ReturnDefinitionTablePK) row.getPrimaryKey();
		loadTable(tablePK);
		tableNameText.setText("[" + row.getValue(0) + "][" + row.getValue(2)
				+ "] " + row.getValue(3));
		checkValid();
	} // GEN-LAST:event_selectTableButtonActionPerformed

	private void parameterComboActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_parameterComboActionPerformed
		if (parameterCombo.getSelectedIndex() > 0) {
			nextButton.setEnabled(true);
			nextButton.setText(ui.getString("fina2.finish"));
			nextButton.setIcon(ui.getIcon("fina2.ok"));
			parameterName = (String) parameterCombo.getSelectedItem();
		} else {
			nextButton.setEnabled(true);
			nextButton.setText(ui.getString("fina2.next"));
			nextButton.setIcon(ui.getIcon("fina2.next"));
			parameterName = null;
		}
	} // GEN-LAST:event_parameterComboActionPerformed

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
		Vector v = new Vector(1);
		v.add(ui.getString("fina2.none"));
		if (parameters != null) {
			for (Iterator it = parameters.iterator(); it.hasNext();) {
				fina2.ui.sheet.openoffice.OOParameter param = (fina2.ui.sheet.openoffice.OOParameter) it
						.next();
				if (param.getType() == typeCombo.getSelectedIndex()) {
					v.add(param.getName());
				}
			}
		}
		parameterCombo.setModel(new javax.swing.DefaultComboBoxModel(
				new Vector(v)));
		if (iter != null) {
			parameterName = iter.getParameter();
			if (parameterName != null)
				parameterCombo.setSelectedItem(parameterName);
		}
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
		if (((step == maxStepsT) && (pref != ""))
				|| ((step == maxSteps) && (pref == ""))) {
			System.out.println("Step = " + step + "  " + pref);
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
			initTable();
		}
		if (step == 6) {
			try {
				initAggregateTable();
			} catch (Exception e) {
				e.printStackTrace();
				ui.showMessageBox(main.getMainFrame(), ui
						.getString("fina2.title"), e.toString());
			}

		}

		if (step == 7) {
			try {
				initPeriodTable();
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

		if ((typeCombo.getSelectedIndex() == fina2.ui.sheet.openoffice.OOIterator.VCT_ITERATOR)
				&& (step > 2)) {
			pref = "t";
		} else {
			pref = "";
		}

		if ((((step < maxSteps) && (parameterCombo.getSelectedIndex() == 0)) || ((step < 3) && (parameterCombo
				.getSelectedIndex() > 0)))
				|| (pref.equals("t") && (step < maxStepsT))) {

			// Open next step
			step++;

			((CardLayout) cardPanel.getLayout()).show(cardPanel, "step" + step
					+ pref);

			updateStep();

		} else {
			// Get selected result
			Collection rows = targetTable.getRows();
			Vector values = new Vector();

			// Get selected values
			if (typeCombo.getSelectedItem().equals(
					ui.getString("fina2.reportoo.iteratorType.bank"))) {
				// Bank parameter
				if (parameterCombo.getSelectedIndex() == 0) {
					values = bankAssistant.getSelectedBanks();
				}
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
				iter = new OOIterator();
			}

			if (rowsRadio.isSelected())
				iter.setOrientation(iter.ROW_ITERATOR);

			if (columnsRadio.isSelected())
				iter.setOrientation(iter.COL_ITERATOR);

			iter.setValues(values);
			iter.setName(nameText.getText());
			iter.setType(typeCombo.getSelectedIndex());
			if (parameterCombo.getSelectedIndex() > 0) {
				iter.setParameter((String) parameterCombo.getSelectedItem());
			} else {
				iter.setParameter(null);
			}

			if (typeCombo.getSelectedIndex() == iter.VCT_ITERATOR) {
				iter.setTable(tablePK);
				iter.setGroupCode(((String) groupbyCombo.getSelectedItem())
						.substring(((String) groupbyCombo.getSelectedItem())
								.indexOf('[') + 1, ((String) groupbyCombo
								.getSelectedItem()).indexOf(']')));
				iter.setAggregateType(aggregateCombo.getSelectedIndex());

				if (jLatestVersionCheckBox.isSelected()) {
					iter.setVersionCode(ReportConstants.LATEST_VERSION);
				} else {
					iter.setVersionCode((String) jVersionCodeCombo
							.getSelectedItem());
				}

				Vector aggregateValues = new Vector();
				if (aggregateParameterCombo.getSelectedIndex() > 0) {
					iter.setAggregateParameter((String) aggregateParameterCombo
							.getSelectedItem());
				} else {
					iter.setAggregateParameter(null);

					Collection aggregateRows = targetAggregateTable.getRows();

					for (java.util.Iterator it = aggregateRows.iterator(); it
							.hasNext();) {
						TableRowImpl row = (TableRowImpl) it.next();
						aggregateValues.add((String) row.getValue(0));
					}
				}
				iter.setAggregateValues(aggregateValues);

				Vector periodValues = new Vector();
				if (periodParameterCombo.getSelectedIndex() > 0) {
					iter.setPeriodParameter((String) periodParameterCombo
							.getSelectedItem());
				} else {
					iter.setPeriodParameter(null);
					Collection periodRows = targetPeriodTable.getRows();
					for (java.util.Iterator it = periodRows.iterator(); it
							.hasNext();) {
						TableRowImpl row = (TableRowImpl) it.next();
						periodValues.add(row.getPrimaryKey());
					}
				}
				iter.setPeriodValues(periodValues);

			} else {
				iter.setTable(null);
				iter.setGroupCode(null);
				iter.setAggregateType(0);
				iter.setAggregateValues(new Vector());
				iter.setPeriodValues(new Vector());
				iter.setAggregateParameter(null);
				iter.setPeriodParameter(null);
			}

			iter.setSheet(sheet);
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

		if ((typeCombo.getSelectedIndex() == OOIterator.VCT_ITERATOR)
				&& (step > 4)) {
			pref = "t";
		} else {
			pref = "";
		}

		if (step > 1) {
			step--;
			((CardLayout) cardPanel.getLayout()).show(cardPanel, "step" + step
					+ pref);
			updateStep();
		}
	} // GEN-LAST:event_backButtonActionPerformed

	private void closeDialog(java.awt.event.WindowEvent evt) { // GEN-FIRST:
		// event_closeDialog
		setVisible(false);
		dispose();
	} // GEN-LAST:event_closeDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.ButtonGroup orienationGroup;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JLabel wizardLabel;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel nameLabel;
	private javax.swing.JLabel typeLabel;
	private javax.swing.JLabel orientationLabel;
	private javax.swing.JLabel valuesLabel;
	private javax.swing.JLabel label5;
	private javax.swing.JLabel label6;
	private javax.swing.JLabel label7;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JButton helpButton;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JButton backButton;
	private javax.swing.JButton nextButton;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JButton cancelButton;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JPanel jPanel9;
	private javax.swing.JLabel stepNameLabel;
	private javax.swing.JTextArea stepDescText;
	private javax.swing.JPanel cardPanel;
	private javax.swing.JPanel step1;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JTextField nameText;
	private javax.swing.JPanel step2;
	private javax.swing.JRadioButton rowsRadio;
	private javax.swing.JRadioButton columnsRadio;
	private javax.swing.JPanel step3;
	private javax.swing.JLabel jLabel8;
	private javax.swing.JComboBox typeCombo;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JComboBox parameterCombo;
	private javax.swing.JPanel step4;
	private javax.swing.JPanel finishPanel;
	private javax.swing.JPanel jPanel10;
	private javax.swing.JPanel jPanel11;
	private javax.swing.JLabel textLabel;
	private javax.swing.JTextField textField;
	private javax.swing.JButton textButton;
	private javax.swing.JPanel jPanel16;
	private javax.swing.JSplitPane splitPane;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JScrollPane sourceScrollPane;
	private java.awt.Component jFilterComponent;
	private javax.swing.JPanel jPanel12;
	private javax.swing.JPanel jPanel13;
	private javax.swing.JPanel jPanel15;
	private javax.swing.JButton upButton;
	private javax.swing.JButton downButton;
	private javax.swing.JScrollPane targetScrollPane;
	private javax.swing.JPanel jPanel14;
	private javax.swing.JButton rightButton;
	private javax.swing.JButton leftButton;
	private javax.swing.JPanel step4t;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JTextField tableNameText;
	private javax.swing.JButton selectTableButton;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JComboBox groupbyCombo;
	private javax.swing.JPanel step5t;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JComboBox aggregateCombo;
	private javax.swing.JPanel step6t;
	private javax.swing.JPanel jPanel18;
	private javax.swing.JPanel jPanel19;
	private javax.swing.JLabel jLabel9;
	private javax.swing.JComboBox aggregateParameterCombo;
	private javax.swing.JPanel jPanel20;
	private javax.swing.JPanel jPanel21;
	private javax.swing.JSplitPane aggregateSplitPane;
	private javax.swing.JPanel jPanel22;
	private javax.swing.JScrollPane aggregateSourceScrollPanel;
	private javax.swing.JPanel jPanel23;
	private javax.swing.JScrollPane aggregateTargetScrollPanel;
	private javax.swing.JPanel jPanel26;
	private javax.swing.JButton aggregateRightButton;
	private javax.swing.JButton aggregateLeftButton;
	private javax.swing.JPanel step7t;
	private javax.swing.JPanel jPanel24;
	private javax.swing.JPanel jPanel25;
	private javax.swing.JLabel jLabel10;
	private javax.swing.JComboBox periodParameterCombo;
	private javax.swing.JPanel jPanel17;
	private javax.swing.JPanel jPanel28;
	private javax.swing.JSplitPane periodSplitPane;
	private javax.swing.JPanel jPanel29;
	private javax.swing.JScrollPane periodSourceScrollPanel;
	private javax.swing.JPanel jPanel30;
	private javax.swing.JScrollPane periodTargetScrollPanel;
	private javax.swing.JPanel jPanel31;
	private javax.swing.JButton periodRightButton;
	private javax.swing.JButton periodLeftButton;
	private javax.swing.JLabel jVersionLabel;
	private javax.swing.JComboBox jVersionCodeCombo;
	private javax.swing.JCheckBox jLatestVersionCheckBox;
	// End of variables declaration//GEN-END:variables
}
