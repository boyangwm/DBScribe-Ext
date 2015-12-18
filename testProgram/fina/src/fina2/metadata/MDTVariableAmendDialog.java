package fina2.metadata;

import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.transaction.UserTransaction;

import fina2.Main;
import fina2.javascript.Wizard;
import fina2.reportoo.ReportConstants;
import fina2.ui.UIManager;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;
import fina2.ui.tree.EJBTree;
import fina2.ui.tree.Node;

public class MDTVariableAmendDialog extends javax.swing.JDialog implements AbstractComparisonDialog {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private MDTNode mdtNode;
	private Node parent;
	private Node node;
	private boolean canAmend = false;
	private String source;
	private EJBTable table;
	private java.awt.Frame parentFrame;
	private MDTNodePK pk;
	private NodeComparisonAmendDialog comparisonDialog;

	private Wizard wizard = null;

	private EJBTree tree;
	private DefaultMutableTreeNode treeNode;
	private static boolean compWizard = false;

	public MDTVariableAmendDialog(java.awt.Frame parentFrame, boolean modal) {
		super(parentFrame, modal);
		this.parentFrame = parentFrame;
		ui.loadIcon("fina2.ok", "ok.gif");
		ui.loadIcon("fina2.cancel", "cancel.gif");
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.lookup", "find.gif");
		ui.loadIcon("fina2.create", "insert.gif");
		ui.loadIcon("fina2.amend", "amend.gif");
		ui.loadIcon("fina2.review", "review.gif");
		ui.loadIcon("fina2.delete", "delete.gif");

		table = new EJBTable();

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		table.addSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				if (table.getSelectedRow() == -1) {
					amendButton.setEnabled(false);
					deleteButton.setEnabled(false);
				} else {
					createButton.setEnabled(true);
					amendButton.setEnabled(true);
					deleteButton.setEnabled(true);
				}
			}
		});

		initComponents();

		scrollPane.setViewportView(table);
	}

	public void setVisible(boolean v) {

		if ((v) && (wizard != null)) {
			if (!compWizard) {
				Collection _refNodes = wizard.getRefNodes();
				if (_refNodes != null) {
					Collection codes = wizard.getCodes();
					if (codes != null) {
						for (Iterator iter = codes.iterator(); iter.hasNext();) {
							String code = (String) iter.next();
							if (code.equals(codeText.getText())) {
								ui.showMessageBox(parentFrame, ui.getString("fina2.title"), "CODE: " + code + " loop");
								compWizard = false;
								wizard = null;
								return;
							}
						}
					} else {
						System.out.println("codes null");
					}
					source = wizard.getSource();
				}
			} else {
				Collection _refNodes = wizard.getRefNodes();
				if (_refNodes != null) {
					table.getSelectedTableRow().setValue(2, wizard.getSource());
				}
			}
			compWizard = false;
			wizard = null;
		}
		super.setVisible(v);
	}

	public Node getNode() {
		return node;
	}

	private void initTable() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/metadata/MDTSession");
			MDTSessionHome home = (MDTSessionHome) PortableRemoteObject.narrow(ref, MDTSessionHome.class);

			MDTSession session = home.create();

			Vector colNames = new Vector();
			colNames.add(ui.getString("fina2.code"));
			colNames.add(ui.getString("fina2.condition"));
			colNames.add(ui.getString("fina2.equation"));

			Collection rows = new Vector();
			Vector f = new Vector();
			f.add(" ");
			f.add(" ");
			f.add(" ");

			Collection format = new Vector();
			rows = session.getComparisons(main.getUserHandle(), pk);
			for (Iterator iter = rows.iterator(); iter.hasNext();) {
				TableRow row = (TableRow) iter.next();
				format.add(f);
			}
			table.initTable(colNames, rows, format);

			if (!canAmend) {
				table.setEditable(0);
			} else {
				table.setEditable(1);
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	public void show(EJBTree tree, Node parent, Node node, boolean canAmend) {

		this.tree = tree;
		this.node = node;
		this.parent = parent;
		this.canAmend = canAmend;
		this.treeNode = tree.getSelectedTreeNode();

		if (!canAmend) {
			codeText.setEditable(false);
			descriptionText.setEditable(false);
			requiredCheckBox.setEnabled(false);

			createButton.setVisible(false);
			deleteButton.setVisible(false);

			amendButton.setText(ui.getString("fina2.review"));
			amendButton.setIcon(ui.getIcon("fina2.review"));
		} else {
			codeText.setEditable(true);
			descriptionText.setEditable(true);
			createButton.setEnabled(true);
			amendButton.setEnabled(true);
			deleteButton.setEnabled(true);
			requiredCheckBox.setEnabled(true);
		}

		try {
			if (node != null) {
				InitialContext jndi = fina2.Main.getJndiContext();

				Object ref = jndi.lookup("fina2/metadata/MDTNode");
				MDTNodeHome home = (MDTNodeHome) PortableRemoteObject.narrow(ref, MDTNodeHome.class);

				pk = (MDTNodePK) node.getPrimaryKey();
				mdtNode = home.findByPrimaryKey(pk);

				descriptionText.setText(mdtNode.getDescription(main.getLanguageHandle()));
				codeText.setText(mdtNode.getCode());
				codeText.setEditable(mdtNode.getInUsed() && canAmend);

				source = mdtNode.getEquation();

				if (mdtNode.getRequired() == 0)
					requiredCheckBox.setSelected(false);
				else
					requiredCheckBox.setSelected(true);

			} else {
				pk = null;
				source = "return ;";
				descriptionText.setText(ui.getString("fina2.noname") + " " + ui.getString("fina2.variable"));
				requiredCheckBox.setSelected(true);
			}

			initTable();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
			return;
		}

		setLocationRelativeTo(getParent());
		show();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		helpButton = new javax.swing.JButton();
		jPanel3 = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		descriptionText = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		codeText = new javax.swing.JTextField();
		amendEquationButton = new javax.swing.JButton();
		requiredCheckBox = new javax.swing.JCheckBox();
		jPanel5 = new javax.swing.JPanel();
		jPanel7 = new javax.swing.JPanel();
		jPanel8 = new javax.swing.JPanel();
		createButton = new javax.swing.JButton();
		amendButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();
		jPanel9 = new javax.swing.JPanel();
		scrollPane = new javax.swing.JScrollPane();
		jPanel6 = new javax.swing.JPanel();

		setTitle(ui.getString("fina2.variable"));
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jPanel1.setLayout(new java.awt.BorderLayout());

		helpButton.setIcon(ui.getIcon("fina2.help"));
		helpButton.setFont(ui.getFont());
		helpButton.setText(ui.getString("fina2.help"));
		helpButton.setEnabled(false);
		jPanel2.add(helpButton);

		jPanel1.add(jPanel2, java.awt.BorderLayout.WEST);

		okButton.setIcon(ui.getIcon("fina2.ok"));
		okButton.setFont(ui.getFont());
		okButton.setText(ui.getString("fina2.ok"));
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonActionPerformed(evt);
			}
		});

		jPanel3.add(okButton);

		cancelButton.setIcon(ui.getIcon("fina2.cancel"));
		cancelButton.setFont(ui.getFont());
		cancelButton.setText(ui.getString("fina2.cancel"));
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		jPanel3.add(cancelButton);

		jPanel1.add(jPanel3, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

		jPanel4.setLayout(new java.awt.GridBagLayout());

		jPanel4.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(30, 20, 20, 40)));
		jLabel1.setText(ui.getString("fina2.description"));
		jLabel1.setFont(ui.getFont());
		jPanel4.add(jLabel1, UIManager.getGridBagConstraints(0, 1, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		descriptionText.setColumns(28);
		descriptionText.setFont(ui.getFont());
		jPanel4.add(descriptionText, UIManager.getGridBagConstraints(1, 1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, null));

		jLabel2.setText(ui.getString("fina2.equation"));
		jLabel2.setFont(ui.getFont());
		jPanel4.add(jLabel2, UIManager.getGridBagConstraints(0, 2, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		jLabel3.setText(UIManager.formatedHtmlString(ui.getString("fina2.code")));
		jLabel3.setFont(ui.getFont());
		jPanel4.add(jLabel3, UIManager.getGridBagConstraints(0, 0, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		codeText.setColumns(10);
		codeText.setFont(ui.getFont());
		jPanel4.add(codeText, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, null));

		amendEquationButton.setIcon(ui.getIcon("fina2.amend"));
		amendEquationButton.setFont(ui.getFont());
		amendEquationButton.setText(ui.getString("fina2.metadata.amendequation"));
		amendEquationButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				amendEquationButtonActionPerformed(evt);
			}
		});
		jPanel4.add(amendEquationButton, UIManager.getGridBagConstraints(1, 2, -1, -1, -1, -1, GridBagConstraints.WEST, -1, null));

		requiredCheckBox.setFont(ui.getFont());
		requiredCheckBox.setText(ui.getString("fina2.metadata.required"));
		jPanel4.add(requiredCheckBox, UIManager.getGridBagConstraints(1, 3, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(5, 0, 0, 0)));

		getContentPane().add(jPanel4, java.awt.BorderLayout.NORTH);

		jPanel5.setLayout(new java.awt.BorderLayout());

		jPanel5.setBorder(new javax.swing.border.TitledBorder(null, ui.getString("fina2.metadata.comparisonrules"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, ui.getFont()));
		jPanel5.setFont(ui.getFont());
		jPanel7.setLayout(new java.awt.BorderLayout());

		jPanel8.setLayout(new java.awt.GridBagLayout());

		createButton.setIcon(ui.getIcon("fina2.create"));
		createButton.setFont(ui.getFont());
		createButton.setText(ui.getString("fina2.create"));
		createButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		createButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				createButtonActionPerformed(evt);
			}
		});
		jPanel8.add(createButton, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 5, 0, 5)));

		amendButton.setIcon(ui.getIcon("fina2.amend"));
		amendButton.setFont(ui.getFont());
		amendButton.setText(ui.getString("fina2.amend"));
		amendButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		amendButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				amendButtonActionPerformed(evt);
			}
		});
		jPanel8.add(amendButton, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		deleteButton.setIcon(ui.getIcon("fina2.delete"));
		deleteButton.setFont(ui.getFont());
		deleteButton.setText(ui.getString("fina2.delete"));
		deleteButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		deleteButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteButtonActionPerformed(evt);
			}
		});
		jPanel8.add(deleteButton, UIManager.getGridBagConstraints(0, 3, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 5, 0, 5)));

		jPanel7.add(jPanel8, java.awt.BorderLayout.NORTH);

		jPanel5.add(jPanel7, java.awt.BorderLayout.EAST);

		jPanel9.setLayout(new java.awt.BorderLayout());

		scrollPane.setPreferredSize(new java.awt.Dimension(300, 64));
		jPanel9.add(scrollPane, java.awt.BorderLayout.CENTER);

		jPanel5.add(jPanel9, java.awt.BorderLayout.CENTER);

		jPanel5.add(jPanel6, java.awt.BorderLayout.NORTH);

		getContentPane().add(jPanel5, java.awt.BorderLayout.CENTER);

		pack();
	}// GEN-END:initComponents

	private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_deleteButtonActionPerformed
		if (!ui.showConfirmBox(parentFrame, ui.getString("fina2.metadata.comparisonDeleteQuestion")))
			return;
		table.removeRow(table.getSelectedRow());
	}// GEN-LAST:event_deleteButtonActionPerformed

	private void amendButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_amendButtonActionPerformed
		compWizard = true;
		wizard = new fina2.javascript.Wizard(this, table.getSelectedTableRow().getValue(2), canAmend);
		wizard.show();
	}// GEN-LAST:event_amendButtonActionPerformed

	private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_createButtonActionPerformed
		compWizard = true;
//		TableRowImpl row = new TableRowImpl(new ComparisonPK(table.getRowCount() + 1, 1), 3);
//		row.setValue(0, codeText.getText());
//		row.setValue(1, "=");
//		row.setValue(2, "return ;");
//		table.addRow(row);
//		wizard = new fina2.javascript.Wizard(this, table.getSelectedTableRow().getValue(2));
//		wizard.show();
		TableRowImpl row = new TableRowImpl(new ComparisonPK(table.getRowCount() + 1, 1), 3);
		row.setValue(0, codeText.getText());
		row.setValue(1, "=");
		row.setValue(2, "return ;");
		//table.addRow(row);
		wizard = new fina2.javascript.Wizard(this,table,row);// table.getSelectedTableRow().getValue(2));
		wizard.show();
		table.fireSelectionChangedEvent();
	}// GEN-LAST:event_createButtonActionPerformed

	private void amendEquationButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:
		// event_amendEquationButtonActionPerformed
		wizard = new fina2.javascript.Wizard(this, source, canAmend);
		wizard.show();
	}// GEN-LAST:event_amendEquationButtonActionPerformed

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_cancelButtonActionPerformed
		dispose();
	}// GEN-LAST:event_cancelButtonActionPerformed

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-
// FIRST
		// :
		// event_okButtonActionPerformed
//		if (!fina2.Main.isValidCode(codeText.getText())) {
//			ui.showMessageBox(null, ui.getString("fina2.title"), ui
//					.getString("fina2.invalidCode"));
//			return;
//		}
		if (!fina2.Main.isValidCode(codeText.getText()) || ReportConstants.LATEST_VERSION.equalsIgnoreCase(codeText.getText().trim())) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidCode"));
			return;
		}
		if (!ui.isValidLength(codeText.getText(), false)) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.web.banktype.code.length"));
			return;
		}
		if (codeText.getText().trim().equals("")) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.pleaseEnterCode"));
			return;
		}
		if (!ui.isValidLength(descriptionText.getText(), true)) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.web.banktype.description.length255"));
			return;
		}
		try {
			InitialContext jndi = fina2.Main.getJndiContext();

			Object ref = jndi.lookup("fina2/metadata/MDTNode");
			MDTNodeHome home = (MDTNodeHome) PortableRemoteObject.narrow(ref,
					MDTNodeHome.class);

			ref = jndi.lookup("fina2/metadata/MDTSession");
			MDTSessionHome sessionHome = (MDTSessionHome) PortableRemoteObject
					.narrow(ref, MDTSessionHome.class);

			MDTSession session = sessionHome.create();

			try {
				if (node == null) {
					MDTNodePK parentPK = (MDTNodePK) parent.getPrimaryKey();
					mdtNode = home.create(parentPK);
					mdtNode.setType(MDTConstants.NODETYPE_VARIABLE);
				}
			} catch (Exception e) {
				Main.generalErrorHandler(e);
				if (node == null)
					mdtNode.remove();
				return;
			}
			UserTransaction trans = main.getUserTransaction(jndi);

			trans.begin();

			try {
				mdtNode.setDescription(main.getLanguageHandle(),
						descriptionText.getText());

				if (!"".equals(codeText.getText().trim())) {
					mdtNode.setCode(codeText.getText().trim());
				}

				if (requiredCheckBox.isSelected()) {
					mdtNode.setRequired(1);
				} else {
					mdtNode.setRequired(0);
				}

				session.setComparisons((MDTNodePK) mdtNode.getPrimaryKey(),
						table.getRows());

				// Collect and set node dependencies
				HashSet refNodes = new HashSet();
				if (source != null) {
					Wizard wizard = new Wizard(this, source);
					refNodes.addAll(wizard.getRefNodes());
				}

				for (Iterator iter = table.getRows().iterator(); iter.hasNext();) {

					TableRow row = (TableRow) iter.next();

					Wizard compWizard = new Wizard(this, row.getValue(2));
					refNodes.addAll(compWizard.getRefNodes());
				}
				session.setDependentNodes((MDTNodePK) mdtNode.getPrimaryKey(),
						refNodes);

				mdtNode.setEquation(source);

				trans.commit();
			} catch (Exception e) {
				try {
					trans.rollback();
				} catch (Exception tex) {
				}
				if (node == null) {
					mdtNode.remove();
					mdtNode = null;
				}
				Main.generalErrorHandler(e);
				return;
			}
			if (node == null) {
				node = new Node(mdtNode.getPrimaryKey(), "["
						+ mdtNode.getCode() + "] " + descriptionText.getText(),
						new Integer(mdtNode.getType()));
				node.putProperty("code", mdtNode.getCode());
				DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(
						node);
				((DefaultTreeModel) tree.getModel()).insertNodeInto(
						newTreeNode, treeNode, treeNode.getChildCount());
				tree.scrollPathToVisible(new TreePath(newTreeNode.getPath()));
			} else {
				node.setLabel("[" + mdtNode.getCode() + "] "
						+ descriptionText.getText());
				((DefaultTreeModel) tree.getModel()).nodeChanged(treeNode);
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		dispose();
			}// GEN-LAST:event_okButtonActionPerformed

	/**
	 * Closes the dialog
	 * 
	 * @param evt
	 *            WindowEvent
	 */
	private void closeDialog(java.awt.event.WindowEvent evt) {// GEN-FIRST:
		// event_closeDialog
		setVisible(false);
		dispose();
	}// GEN-LAST:event_closeDialog

	public void updateComparison(String condition, String equation) {
		TableRowImpl row = new TableRowImpl(table.getSelectedTableRow().getPrimaryKey(), 3);
		row.setValue(0, codeText.getText());
		row.setValue(1, comparisonDialog.getCondition());
		row.setValue(2, comparisonDialog.getEquation());
		table.updateRow(table.getSelectedRow(), row);
	}

	public void createComparison(String condition, String equation) {
		TableRowImpl row = new TableRowImpl(new ComparisonPK(table.getRowCount() + 1, 1), 3);
		row.setValue(0, codeText.getText());
		row.setValue(1, comparisonDialog.getCondition());
		row.setValue(2, comparisonDialog.getEquation());
		table.addRow(row);
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton amendButton;
	private javax.swing.JButton amendEquationButton;
	private javax.swing.JButton cancelButton;
	private javax.swing.JTextField codeText;
	private javax.swing.JButton createButton;
	private javax.swing.JButton deleteButton;
	private javax.swing.JTextField descriptionText;
	private javax.swing.JButton helpButton;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JPanel jPanel9;
	private javax.swing.JButton okButton;
	private javax.swing.JCheckBox requiredCheckBox;
	private javax.swing.JScrollPane scrollPane;
	// End of variables declaration//GEN-END:variables
}
