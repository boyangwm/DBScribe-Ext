/*
 * SelectParameterValuesDialog.java
 *
 * Created on 15 ќкт€брь 2002 г., 15:10
 */

package fina2.reportoo;

import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import fina2.Main;
import fina2.bank.BankSession;
import fina2.bank.BankSessionHome;
import fina2.metadata.MDTConstants;
import fina2.metadata.MDTNodePK;
import fina2.metadata.MDTSession;
import fina2.metadata.MDTSessionHome;
import fina2.period.PeriodSession;
import fina2.period.PeriodSessionHome;
import fina2.ui.UIManager;
import fina2.ui.sheet.openoffice.OOIterator;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;
import fina2.ui.tree.EJBTree;
import fina2.ui.tree.Node;

/**
 * 
 * @author Administrator
 */
public class SelectParameterValuesDialog extends javax.swing.JDialog {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTable sourceTable;
	private EJBTable targetTable;

	private int type = 0;
	private boolean yes = false;

	/** Node tree */
	private EJBTree tree;

	/**
	 * BankTreeAssistant. Used for the bank trees init, loading data and etc.
	 * See initBankTreeParameters method.
	 */
	private BankParameterAssistant bankAssistant = null;

	public Vector values = null;

	/** Creates new form SelectParameterValuesDialog */
	public SelectParameterValuesDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);

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

		tree = new fina2.ui.tree.EJBTree();

		sourceTable
				.addSelectionListener(new javax.swing.event.ListSelectionListener() {
					public void valueChanged(
							javax.swing.event.ListSelectionEvent evt) {
						if (type == fina2.ui.sheet.openoffice.OOIterator.NODE_ITERATOR) {
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
						if (type == fina2.ui.sheet.openoffice.OOIterator.NODE_ITERATOR) {
							if (node == null) {
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
							if (((MDTNodePK) ((Node) child.getUserObject())
									.getPrimaryKey()).getId() != -1)
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

	private void allowFilterPannel() {
		jFilterComponent.setVisible(true);
	}

	private void disableFilterPannel() {
		jFilterComponent.setVisible(false);
	}

	private void attachFilterPannel(java.awt.Component filter) {
		jPanel15.remove(jFilterComponent);
		jPanel15.add(filter, java.awt.BorderLayout.NORTH);
		jFilterComponent = filter;
		javax.swing.SwingUtilities.updateComponentTreeUI(jPanel15);
	}

	/** Inits bank parameter */
	private void initBankParameter() {

		bankAssistant = new BankParameterAssistant();

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

	private void initTable() {

		try {

			Vector rows = new Vector();
			disableFilterPannel();

			// -----------------------------------------------------------------
			// Bank parameter

			if (type == OOIterator.BANK_ITERATOR) {
				initBankParameter();
				return;
			}

			// -----------------------------------------------------------------
			// Period parameter

			if (type == fina2.ui.sheet.openoffice.OOIterator.PERIOD_ITERATOR) {

				rows = new Vector();

				initPeriods(rows, sourceTable, targetTable);
				allowFilterPannel();

				sourceScrollPane.setViewportView(sourceTable);
				targetScrollPane.setViewportView(targetTable);
			}

			// -----------------------------------------------------------------
			// Other type parameters

			if (type == fina2.ui.sheet.openoffice.OOIterator.NODE_ITERATOR) {
				// if(iter!=null && iter.getValues()!=null) {
				// rows = (Vector)session.getNodeValues(iter.getValues(),
				// main.getUserHandle(), main.getLanguageHandle());
				// }
				rows = new Vector();
				initNodes(rows, sourceTable, targetTable);
				sourceScrollPane.setViewportView(tree);
				targetScrollPane.setViewportView(targetTable);
			}
			if (type == fina2.ui.sheet.openoffice.OOIterator.PEER_ITERATOR) {
				// if(iter!=null && iter.getValues()!=null) {
				// rows = (Vector)session.getPeerValues(iter.getValues(),
				// main.getUserHandle(), main.getLanguageHandle());
				// }
				rows = new Vector();
				initPeers(rows, sourceTable, targetTable);
				sourceScrollPane.setViewportView(sourceTable);
				targetScrollPane.setViewportView(targetTable);
			}
			if (type == fina2.ui.sheet.openoffice.OOIterator.OFFSET_ITERATOR) {
				// if(iter!=null && iter.getValues()!=null) {
				// for(Iterator it = iter.getValues().iterator(); it.hasNext();)
				// {
				// Object data = it.next();
				// TableRowImpl row = new TableRowImpl(data, 4);
				// row.setValue(0, data.toString());
				// rows.add(row);
				//
				// }
				// }
				rows = new Vector();
				initOffsets(rows, sourceTable, targetTable);
				// sourceScrollPane.setViewportView(sourceTable);
				targetScrollPane.setViewportView(targetTable);
			}

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		// checkValid();

	}

	private void initPeriods(Vector rows, EJBTable source, EJBTable target) {

		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/period/PeriodSession");
			PeriodSessionHome home = (PeriodSessionHome) PortableRemoteObject
					.narrow(ref, PeriodSessionHome.class);

			PeriodSession session = home.create();

			Vector colNames = new Vector();
			colNames.add(ui.getString("fina2.period.periodType"));
			colNames.add(ui.getString("fina2.period.periodNumber"));
			colNames.add(ui.getString("fina2.period.fromDate"));
			colNames.add(ui.getString("fina2.period.toDate"));

			Vector sourceRows = (Vector) session.getPeriodRows(main
					.getUserHandle(), main.getLanguageHandle());
			Vector targetRows = rows;

			source.setAllowSort(false);
			source.initTable(colNames, sourceRows);
			target.initTable(colNames, targetRows);

			PeriodFilterPannel filter = new PeriodFilterPannel(colNames,
					sourceRows, sourceTable);
			attachFilterPannel(filter);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private void initOffsets(Vector rows, EJBTable source, EJBTable target) {

		Vector colNames = new Vector();
		colNames.add(ui.getString("fina2.reportoo.parameterType.offset"));
		Vector sourceRows = new Vector();
		Vector targetRows = rows;
		source.initTable(colNames, sourceRows);
		target.initTable(colNames, targetRows);
		// targetScrollPane.setViewportView(targetTable);
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
			Vector sourceRows = (Vector) session.getBankGroupsRows(main
					.getUserHandle(), main.getLanguageHandle());
			Vector targetRows = rows;
			source.setAllowSort(false);
			source.initTable(colNames, sourceRows);
			target.setAllowSort(false);
			target.initTable(colNames, targetRows);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
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

	private void checkValid() {

		if ((targetTable.getRowCount() > 0)) {
			okButton.setEnabled(true);
		} else {
			okButton.setEnabled(false);
		}
	}

	public void show(int type, String name) {

		this.type = type;

		setSize(getParent().getWidth() - 60, getParent().getHeight() - 60);
		splitPane.setDividerLocation((getParent().getWidth() - 200) / 2);
		setLocationRelativeTo(getParent());
		iteratorNameText.setText(name);

		if (type != fina2.ui.sheet.openoffice.OOIterator.OFFSET_ITERATOR) {
			textButton.setVisible(false);
			textLabel.setVisible(false);
			textField.setVisible(false);
		} else {
			textButton.setVisible(true);
			textLabel.setVisible(true);
			textField.setVisible(true);
		}

		initTable();

		checkValid();

		/* For banks parameter OK button should enabled */
		if (type == OOIterator.BANK_ITERATOR) {
			okButton.setEnabled(true);
		}

		super.show();
	}

	public boolean Ok() {
		return yes;
	}

	public Vector getValues() {
		return values;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() { // GEN-BEGIN:initComponents

		jPanel8 = new javax.swing.JPanel();
		jPanel9 = new javax.swing.JPanel();
		jButton5 = new javax.swing.JButton();
		jPanel10 = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		jPanel11 = new javax.swing.JPanel();
		jPanel12 = new javax.swing.JPanel();
		jPanel13 = new javax.swing.JPanel();
		textLabel = new javax.swing.JLabel();
		textField = new javax.swing.JTextField();
		textButton = new javax.swing.JButton();
		jPanel14 = new javax.swing.JPanel();
		splitPane = new javax.swing.JSplitPane();
		jPanel15 = new javax.swing.JPanel();
		sourceScrollPane = new javax.swing.JScrollPane();
		jPanel16 = new javax.swing.JPanel();
		jPanel17 = new javax.swing.JPanel();
		jPanel18 = new javax.swing.JPanel();
		jFilterComponent = new javax.swing.JPanel();
		upButton = new javax.swing.JButton();
		downButton = new javax.swing.JButton();
		targetScrollPane = new javax.swing.JScrollPane();
		jPanel19 = new javax.swing.JPanel();
		rightButton = new javax.swing.JButton();
		leftButton = new javax.swing.JButton();
		jPanel1 = new javax.swing.JPanel();
		jLabel2 = new javax.swing.JLabel();
		iteratorNameText = new javax.swing.JTextField();

		setTitle(ui.getString("fina2.reportoo.selectParameterValuesDialog"));
		setFont(ui.getFont());
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jPanel8.setLayout(new java.awt.BorderLayout());

		jButton5.setIcon(ui.getIcon("fina2.help"));
		jButton5.setFont(ui.getFont());
		jButton5.setText(ui.getString("fina2.help"));
		jButton5.setEnabled(false);
		jPanel9.add(jButton5);

		jPanel8.add(jPanel9, java.awt.BorderLayout.WEST);

		okButton.setIcon(ui.getIcon("fina2.ok"));
		okButton.setFont(ui.getFont());
		okButton.setText(ui.getString("fina2.ok"));
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonActionPerformed(evt);
			}
		});

		jPanel10.add(okButton);

		cancelButton.setIcon(ui.getIcon("fina2.cancel"));
		cancelButton.setFont(ui.getFont());
		cancelButton.setText(ui.getString("fina2.cancel"));
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		jPanel10.add(cancelButton);

		jPanel8.add(jPanel10, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel8, java.awt.BorderLayout.SOUTH);

		jPanel11.setLayout(new java.awt.BorderLayout());

		jPanel12.setLayout(new java.awt.BorderLayout());

		textLabel.setText(ui.getString("fina2.reportoo.iteratorWizard.text"));
		textLabel.setFont(ui.getFont());
		jPanel13.add(textLabel);

		textField.setColumns(20);
		textField.setFont(ui.getFont());
		jPanel13.add(textField);

		textButton.setIcon(ui.getIcon("fina2.insert"));
		textButton.setFont(ui.getFont());
		textButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		textButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				textButtonActionPerformed(evt);
			}
		});

		jPanel13.add(textButton);

		jPanel12.add(jPanel13, java.awt.BorderLayout.EAST);

		jPanel11.add(jPanel12, java.awt.BorderLayout.SOUTH);

		jPanel14.setLayout(new java.awt.BorderLayout());

		splitPane.setDividerSize(4);
		jPanel15.setLayout(new java.awt.BorderLayout());

		jPanel15.add(sourceScrollPane, java.awt.BorderLayout.CENTER);
		jPanel15.add(jFilterComponent, java.awt.BorderLayout.NORTH);

		splitPane.setLeftComponent(jPanel15);

		jPanel16.setLayout(new java.awt.BorderLayout());

		jPanel17.setLayout(new java.awt.BorderLayout());

		upButton.setIcon(ui.getIcon("fina2.reportoo.iteratorWizard.up"));
		upButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		upButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				upButtonActionPerformed(evt);
			}
		});

		jPanel18.add(upButton);

		downButton.setIcon(ui.getIcon("fina2.reportoo.iteratorWizard.down"));
		downButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		downButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				downButtonActionPerformed(evt);
			}
		});

		jPanel18.add(downButton);

		jPanel17.add(jPanel18, java.awt.BorderLayout.EAST);

		jPanel16.add(jPanel17, java.awt.BorderLayout.SOUTH);

		jPanel16.add(targetScrollPane, java.awt.BorderLayout.CENTER);

		jPanel19.setLayout(new java.awt.GridBagLayout());

		rightButton.setIcon(ui.getIcon("fina2.next"));
		rightButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		rightButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rightButtonActionPerformed(evt);
			}
		});
		jPanel19.add(rightButton, UIManager.getGridBagConstraints(0, 0, -1, -1,
				-1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(
						0, 5, 5, 5)));

		leftButton.setIcon(ui.getIcon("fina2.back"));
		leftButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		leftButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				leftButtonActionPerformed(evt);
			}
		});
		jPanel19.add(leftButton, UIManager.getGridBagConstraints(0, 1, -1, -1,
				-1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(
						0, 5, 0, 5)));

		jPanel16.add(jPanel19, java.awt.BorderLayout.WEST);

		splitPane.setRightComponent(jPanel16);

		jPanel14.add(splitPane, java.awt.BorderLayout.CENTER);

		jPanel11.add(jPanel14, java.awt.BorderLayout.CENTER);

		getContentPane().add(jPanel11, java.awt.BorderLayout.CENTER);

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jLabel2.setText(ui.getString("fina2.reportoo.iteratorName"));
		jLabel2.setFont(ui.getFont());
		jPanel1.add(jLabel2, UIManager.getGridBagConstraints(-1, -1, 10, -1,
				-1, -1, -1, -1, null));

		iteratorNameText.setEditable(false);
		iteratorNameText.setFont(ui.getFont());
		iteratorNameText.setText(" ");
		jPanel1.add(iteratorNameText, new GridBagConstraints());

		getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

		pack();
	} // GEN-END:initComponents

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) { //GEN-
		// FIRST
		// :
		// event_okButtonActionPerformed

		// ---------------------------------------------------------------------
		// Bank parameter

		if (type == OOIterator.BANK_ITERATOR) {
			closeBankParameter();
			return;
		}

		// ---------------------------------------------------------------------
		// Other type parameters

		Collection rows = targetTable.getRows();
		values = new Vector();

		for (java.util.Iterator it = rows.iterator(); it.hasNext();) {

			TableRowImpl row = (TableRowImpl) it.next();

			if (type == OOIterator.OFFSET_ITERATOR
					|| type == OOIterator.PERIOD_ITERATOR) {

				values.add(row.getPrimaryKey());

			} else {

				if (type == fina2.ui.sheet.openoffice.OOIterator.NODE_ITERATOR) {
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

		yes = true;
		setVisible(false);
		dispose();

	} // GEN-LAST:event_okButtonActionPerformed

	/**
	 * Closes the dialog in case of bank parameter. If no bank is selected the
	 * dialog isn't closed.
	 */
	private void closeBankParameter() {

		if (!bankAssistant.hasSelectedBanks()) {
			// No bank is selected
			return;
		}

		// Putting the selected banks to the result vector
		values = bankAssistant.getSelectedBanks();

		// The dialog result
		yes = true;
		setVisible(false);
		dispose();
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_cancelButtonActionPerformed
		values = null;
		yes = false;
		setVisible(false);
		dispose();
	} // GEN-LAST:event_cancelButtonActionPerformed

	private void textButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_textButtonActionPerformed

		TableRowImpl row = null;
		if (type == fina2.ui.sheet.openoffice.OOIterator.OFFSET_ITERATOR) {
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

	private void downButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_downButtonActionPerformed

		if (type == OOIterator.BANK_ITERATOR) {
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

		if (type == OOIterator.BANK_ITERATOR) {
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

	private void leftButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_leftButtonActionPerformed

		// ---------------------------------------------------------------------
		// Banks parameter

		if (type == OOIterator.BANK_ITERATOR) {
			// Just removing current selected node
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
		// Banks parameter

		if (type == OOIterator.BANK_ITERATOR) {
			bankAssistant.moveSourceNodeToRight();
			return;
		}

		// ---------------------------------------------------------------------

		TableRow row = null;

		Collection rows = null;
		if (type == fina2.ui.sheet.openoffice.OOIterator.NODE_ITERATOR) {
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

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) { // GEN-FIRST:
		// event_closeDialog
		values = null;
		yes = false;
		setVisible(false);
		dispose();
	} // GEN-LAST:event_closeDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton cancelButton;
	private javax.swing.JButton downButton;
	private javax.swing.JTextField iteratorNameText;
	private javax.swing.JButton jButton5;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel10;
	private javax.swing.JPanel jPanel11;
	private javax.swing.JPanel jPanel12;
	private javax.swing.JPanel jPanel13;
	private javax.swing.JPanel jPanel14;
	private javax.swing.JPanel jPanel15;
	private javax.swing.JPanel jPanel16;
	private javax.swing.JPanel jPanel17;
	private javax.swing.JPanel jPanel18;
	private javax.swing.JPanel jPanel19;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JPanel jPanel9;
	private java.awt.Component jFilterComponent;
	private javax.swing.JButton leftButton;
	private javax.swing.JButton okButton;
	private javax.swing.JButton rightButton;
	private javax.swing.JScrollPane sourceScrollPane;
	private javax.swing.JSplitPane splitPane;
	private javax.swing.JScrollPane targetScrollPane;
	private javax.swing.JButton textButton;
	private javax.swing.JTextField textField;
	private javax.swing.JLabel textLabel;
	private javax.swing.JButton upButton;
	// End of variables declaration//GEN-END:variables

}
