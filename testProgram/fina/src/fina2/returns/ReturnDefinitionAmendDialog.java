package fina2.returns;

import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.transaction.UserTransaction;

import fina2.FinaTypeException;
import fina2.Main;
import fina2.ui.MessageBox;
import fina2.ui.UIManager;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;

public class ReturnDefinitionAmendDialog extends javax.swing.JDialog {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private TableRow tableRow;
	private ReturnDefinition returnDefinition;
	private ReturnTypePK returnTypePK;
	private ReturnType returnType;

	private EJBTable table;

	private ReturnDefinitionCreateAction createAction;
	private ReturnDefinitionAmendAction amendAction;
	private ReturnDefinitionReviewAction reviewAction;
	private ReturnDefinitionDeleteAction deleteAction;
	private ReturnDefinitionUpAction upAction;
	private ReturnDefinitionDownAction downAction;

	private java.awt.Frame parent;

	private boolean canAmend = false;

	public ReturnDefinitionAmendDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		this.parent = parent;

		ui.loadIcon("fina2.cancel", "cancel.gif");
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.ok", "ok.gif");

		ui.loadIcon("fina2.amend", "amend.gif");
		ui.loadIcon("fina2.review", "review.gif");
		ui.loadIcon("fina2.delete", "delete.gif");
		ui.loadIcon("fina2.up", "up.gif");
		ui.loadIcon("fina2.down", "down.gif");

		table = new EJBTable();

		table.setAllowSort(false);

		createAction = new ReturnDefinitionCreateAction(parent, table);
		amendAction = new ReturnDefinitionAmendAction(parent, table);
		reviewAction = new ReturnDefinitionReviewAction(parent, table);
		deleteAction = new ReturnDefinitionDeleteAction(parent, table);
		upAction = new ReturnDefinitionUpAction(parent, table);
		downAction = new ReturnDefinitionDownAction(parent, table);

		table.addMouseListener(new java.awt.event.MouseListener() {
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

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		table.addSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {

				if (table.getSelectedRow() == -1) {
					amendAction.setEnabled(false);
					reviewAction.setEnabled(false);
					deleteAction.setEnabled(false);
					upAction.setEnabled(false);
					downAction.setEnabled(false);

				} else {
					reviewAction.setEnabled(true);
					if (canAmend) {
						amendAction.setEnabled(true);
						deleteAction.setEnabled(true);
						upAction.setEnabled(true);
						downAction.setEnabled(true);
					}
				}
			}
		});

		initComponents();

		setLocationRelativeTo(parent);
	}

	private void initTable() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);

			ReturnSession session = home.create();

			Vector colNames = new Vector();
			colNames.add(ui.getString("fina2.code"));
			colNames.add("Node Name");
			colNames.add(ui.getString("fina2.type"));

			Collection rows = new Vector();

			if (tableRow != null) {
				ReturnDefinitionPK pk = (ReturnDefinitionPK) tableRow.getPrimaryKey();
				rows = session.getDefinitionTables(main.getLanguageHandle(), pk);
			}

			table.initTable(colNames, rows);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

		scrollPane.setViewportView(table);

	}

	public TableRow getTableRow() {
		return tableRow;
	}

	public void show(TableRow tableRow, boolean canAmend) {
		this.tableRow = tableRow;
		this.canAmend = canAmend;

		if (!canAmend) {
			amendAction.setEnabled(false);
			createAction.setEnabled(false);
			deleteAction.setEnabled(false);
			upAction.setEnabled(false);
			downAction.setEnabled(false);
		}

		codeText.setEditable(canAmend);
		descriptionText.setEditable(canAmend);
		typeList.setEnabled(canAmend);

		returnDefinition = null;

		try {
			InitialContext jndi = fina2.Main.getJndiContext();

			Object ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome home1 = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);
			ReturnSession session = home1.create();

			Collection rows = session.getReturnTypesRows(main.getUserHandle(), main.getLanguageHandle());

			if (rows.size() == 0) {
				ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.returns.zeroReturnType"));
				return;
			}

			for (java.util.Iterator iter = rows.iterator(); iter.hasNext();) {
				TableRowImpl row = (TableRowImpl) iter.next();
				row.setDefaultCol(1);
			}
			typeList.setModel(new javax.swing.DefaultComboBoxModel(new Vector(rows)));

			if (rows.size() > 0)
				typeList.setSelectedIndex(0);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
			return;
		}

		if (tableRow != null) {

			ReturnDefinitionPK pk = (ReturnDefinitionPK) tableRow.getPrimaryKey();

			try {
				InitialContext jndi = fina2.Main.getJndiContext();

				Object ref = jndi.lookup("fina2/returns/ReturnDefinition");
				ReturnDefinitionHome home = (ReturnDefinitionHome) PortableRemoteObject.narrow(ref, ReturnDefinitionHome.class);

				returnDefinition = home.findByPrimaryKey(pk);

				codeText.setText(returnDefinition.getCode());
				descriptionText.setText(returnDefinition.getDescription(main.getLanguageHandle()));

				returnTypePK = returnDefinition.getType();

				ref = jndi.lookup("fina2/returns/ReturnType");
				ReturnTypeHome typeHome = (ReturnTypeHome) PortableRemoteObject.narrow(ref, ReturnTypeHome.class);

				returnType = typeHome.findByPrimaryKey(returnTypePK);

				TableRowImpl selRow = new TableRowImpl(returnType.getPrimaryKey(), 2);
				selRow.setValue(1, returnType.getDescription(main.getLanguageHandle()));
				selRow.setDefaultCol(1);
				typeList.setSelectedItem(selRow);

			} catch (Exception e) {
				Main.generalErrorHandler(e);
				return;
			}

		}

		setLocationRelativeTo(getParent());
		show();
	}

	public void show() {
		if (isVisible())
			return;
		initTable();
		super.show();
	}

	private void initComponents() {// GEN-BEGIN:initComponents

		jPanel5 = new javax.swing.JPanel();
		jPanel1 = new javax.swing.JPanel();
		codeLabel = new javax.swing.JLabel();
		codeText = new javax.swing.JTextField();
		descriptionLabel = new javax.swing.JLabel();
		descriptionText = new javax.swing.JTextField();
		typeList = new javax.swing.JComboBox();
		jLabel1 = new javax.swing.JLabel();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		helpButton = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		jPanel7 = new javax.swing.JPanel();
		scrollPane = new javax.swing.JScrollPane();
		jPanel6 = new javax.swing.JPanel();
		jPanel8 = new javax.swing.JPanel();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		jButton3 = new javax.swing.JButton();
		jButton4 = new javax.swing.JButton();
		jButton5 = new javax.swing.JButton();
		jButton7 = new javax.swing.JButton();
		jLabel2 = new javax.swing.JLabel();
		jPanel9 = new javax.swing.JPanel();
		jLabel3 = new javax.swing.JLabel();
		jPanel10 = new javax.swing.JPanel();
		jPanel11 = new javax.swing.JPanel();

		setTitle(ui.getString("fina2.returns.returnDefinition"));
		setFont(ui.getFont());
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jPanel5.setLayout(new java.awt.BorderLayout());

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jPanel1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(10, 10, 10, 10)));
		codeLabel.setFont(ui.getFont());
		codeLabel.setText(UIManager.formatedHtmlString(ui.getString("fina2.code")));
		jPanel1.add(codeLabel, UIManager.getGridBagConstraints(0, 0, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		codeText.setColumns(8);
		codeText.setFont(ui.getFont());
		jPanel1.add(codeText, UIManager.getGridBagConstraints(1, 0, -1, -1, -1, -1, GridBagConstraints.WEST, -1, null));

		descriptionLabel.setFont(ui.getFont());
		descriptionLabel.setText(ui.getString("fina2.description"));
		jPanel1.add(descriptionLabel, UIManager.getGridBagConstraints(0, 1, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		descriptionText.setColumns(25);
		descriptionText.setFont(ui.getFont());
		jPanel1.add(descriptionText, UIManager.getGridBagConstraints(1, 1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, null));

		typeList.setFont(ui.getFont());
		jPanel1.add(typeList, UIManager.getGridBagConstraints(1, 2, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(5, 0, 0, 0)));

		jLabel1.setFont(ui.getFont());
		jLabel1.setText(ui.getString("fina2.type"));
		jPanel1.add(jLabel1, UIManager.getGridBagConstraints(0, 2, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		jPanel5.add(jPanel1, java.awt.BorderLayout.NORTH);

		jPanel2.setLayout(new java.awt.BorderLayout());

		helpButton.setIcon(ui.getIcon("fina2.help"));
		helpButton.setFont(ui.getFont());
		helpButton.setText(ui.getString("fina2.help"));
		helpButton.setEnabled(false);
		helpButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				helpButtonActionPerformed(evt);
			}
		});

		jPanel3.add(helpButton);

		jPanel2.add(jPanel3, java.awt.BorderLayout.WEST);

		okButton.setIcon(ui.getIcon("fina2.ok"));
		okButton.setFont(ui.getFont());
		okButton.setText(ui.getString("fina2.ok"));
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonActionPerformed(evt);
			}
		});

		jPanel4.add(okButton);

		cancelButton.setIcon(ui.getIcon("fina2.cancel"));
		cancelButton.setFont(ui.getFont());
		cancelButton.setText(ui.getString("fina2.cancel"));
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		jPanel4.add(cancelButton);

		jPanel2.add(jPanel4, java.awt.BorderLayout.EAST);

		jPanel5.add(jPanel2, java.awt.BorderLayout.SOUTH);

		jPanel7.setLayout(new java.awt.BorderLayout());

		jPanel7.setBorder(new javax.swing.border.EtchedBorder());
		scrollPane.setPreferredSize(new java.awt.Dimension(400, 200));
		jPanel7.add(scrollPane, java.awt.BorderLayout.CENTER);

		jPanel6.setLayout(new java.awt.BorderLayout());

		jPanel8.setLayout(new java.awt.GridBagLayout());

		jPanel8.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 5, 0, 5)));
		jButton1.setFont(ui.getFont());
		jButton1.setAction(createAction);
		jButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel8.add(jButton1, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		jButton2.setFont(ui.getFont());
		jButton2.setAction(amendAction);
		jButton2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel8.add(jButton2, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(15, 0, 0, 0)));

		jButton3.setFont(ui.getFont());
		jButton3.setAction(reviewAction);
		jButton3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel8.add(jButton3, UIManager.getGridBagConstraints(0, 2, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		jButton4.setFont(ui.getFont());
		jButton4.setAction(deleteAction);
		jButton4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel8.add(jButton4, UIManager.getGridBagConstraints(0, 3, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(15, 0, 0, 0)));

		jButton5.setAction(upAction);
		jButton5.setFont(ui.getFont());
		jButton5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel8.add(jButton5, UIManager.getGridBagConstraints(0, 5, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		jButton7.setAction(downAction);
		jButton7.setFont(ui.getFont());
		jButton7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jPanel8.add(jButton7, UIManager.getGridBagConstraints(0, 6, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 5, 0)));

		jLabel2.setFont(ui.getFont());
		jLabel2.setIcon(new javax.swing.ImageIcon(""));
		jLabel2.setText(ui.getString("fina2.sequence"));
		jPanel8.add(jLabel2, UIManager.getGridBagConstraints(0, 4, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(15, 0, 0, 0)));

		jPanel6.add(jPanel8, java.awt.BorderLayout.NORTH);

		jPanel7.add(jPanel6, java.awt.BorderLayout.EAST);

		jLabel3.setText(ui.getString("fina2.tables"));
		jLabel3.setFont(ui.getFont());
		jPanel9.add(jLabel3);

		jPanel7.add(jPanel9, java.awt.BorderLayout.NORTH);

		jPanel7.add(jPanel10, java.awt.BorderLayout.SOUTH);

		jPanel7.add(jPanel11, java.awt.BorderLayout.WEST);

		jPanel5.add(jPanel7, java.awt.BorderLayout.CENTER);

		getContentPane().add(jPanel5, java.awt.BorderLayout.CENTER);

		pack();
	}

	// click OK button //
	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
		if (canAmend) {
			if (!fina2.Main.isValidCode(codeText.getText())) {
				ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidCode"));
				return;
			}
			if (!ui.isValidName(codeText.getText())) {
				ui.showMessageBox(null, ui.getString("fina2.region.delete.title"), ui.getString("fina2.report.invalidName"));
				return;
			}
			if (descriptionText.getText().length() > 255) {
				ui.showMessageBox(null, ui.getString("fina2.region.delete.title"), ui.getString("fina2.web.banktype.description.length"));
				return;
			}
			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/returns/ReturnDefinition");
				ReturnDefinitionHome home = (ReturnDefinitionHome) PortableRemoteObject.narrow(ref, ReturnDefinitionHome.class);

				try {
					if (tableRow == null) {
						returnDefinition = home.create(fina2.Main.main.getUserHandle());
					}
				} catch (Exception e) {
					if (tableRow == null)
						returnDefinition = null;
					Main.generalErrorHandler(e);
					return;
				}
				UserTransaction trans = main.getUserTransaction(jndi);
				trans.begin();

				try {
					returnDefinition.setCode(codeText.getText());
					returnDefinition.setDescription(main.getLanguageHandle(), descriptionText.getText());
					// returnDefinition.getPrimaryKey();
					TableRow row = (TableRow) typeList.getSelectedItem();

					if (row != null)
						returnDefinition.setType((ReturnTypePK) row.getPrimaryKey());

					table.getRows();

					ref = jndi.lookup("fina2/returns/ReturnSession");
					ReturnSessionHome home1 = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);

					ReturnSession session1 = home1.create();

					// if (tableRow!=null) {

					// ReturnDefinitionPK pk =
					// (ReturnDefinitionPK)tableRow.getPrimaryKey();
					session1.setDefinitionTables(main.getLanguageHandle(), (ReturnDefinitionPK) returnDefinition.getPrimaryKey(), table.getRows());
					// }

					trans.commit();
				} catch (FinaTypeException ex) {
					String additionalMessage = "";
					if (ex.getParams()!=null && ex.getParams()[0]!=null)
						additionalMessage = ex.getParams()[0];
					
					JLabel label = new JLabel("<html>"+ui.getString(ex.getMessageUrl())+"<br>"+additionalMessage+"</html>");
					label.setFont(ui.getFont());
					JOptionPane.showMessageDialog(null, label, "Message", JOptionPane.WARNING_MESSAGE);

					try {
						trans.rollback();
					} catch (Exception tex) {
					}
					if (tableRow == null) {
						returnDefinition.remove();
						returnDefinition = null;
					}
					return;
				} catch (Exception e) {
					/* Main.generalErrorHandler(e); */
					javax.swing.JOptionPane.showMessageDialog(null, ui.getString(new FinaTypeException(FinaTypeException.Type.FINA2_GENERAL_ERROR).getMessageUrl()), "Fina2", javax.swing.JOptionPane.ERROR_MESSAGE);

					try {
						trans.rollback();
					} catch (Exception tex) {
					}
					if (tableRow == null) {
						returnDefinition.remove();
						returnDefinition = null;
					}
										
					return;
				}
				if (tableRow == null) {
					tableRow = new TableRowImpl(returnDefinition.getPrimaryKey(), 3);
					tableRow.setValue(0, returnDefinition.getCode());
					tableRow.setValue(1, returnDefinition.getDescription(main.getLanguageHandle()));
					tableRow.setValue(2, typeList.getSelectedItem().toString());
				} else {
					tableRow.setValue(0, returnDefinition.getCode());
					tableRow.setValue(1, returnDefinition.getDescription(main.getLanguageHandle()));
					tableRow.setValue(2, typeList.getSelectedItem().toString());
				}

				// ReturnViewFormatFrame dialog = new ReturnViewFormatFrame();
				// dialog.show((ReturnDefinitionPK)returnDefinition.getPrimaryKey
				// (), tableRow);

			} catch (Exception e) {
				Main.generalErrorHandler(e);
			}
		}
		dispose();
	}// GEN-LAST:event_okButtonActionPerformed

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_cancelButtonActionPerformed
		setVisible(false);
		dispose();
	}// GEN-LAST:event_cancelButtonActionPerformed

	private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_helpButtonActionPerformed
		// Add your handling code here:
	}// GEN-LAST:event_helpButtonActionPerformed

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

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton cancelButton;
	private javax.swing.JLabel codeLabel;
	private javax.swing.JTextField codeText;
	private javax.swing.JLabel descriptionLabel;
	private javax.swing.JTextField descriptionText;
	private javax.swing.JButton helpButton;
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton3;
	private javax.swing.JButton jButton4;
	private javax.swing.JButton jButton5;
	private javax.swing.JButton jButton7;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel10;
	private javax.swing.JPanel jPanel11;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JPanel jPanel9;
	private javax.swing.JButton okButton;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JComboBox typeList;
	// End of variables declaration//GEN-END:variables

}

class ReturnDefinitionCreateAction extends AbstractAction {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private java.awt.Frame parent;
	private EJBTable table;
	private ReturnDefinitionTableAmendDialog dialog;
	private DefinitionTable definitionTable;

	ReturnDefinitionCreateAction(java.awt.Frame parent, EJBTable table) {

		this.table = table;
		dialog = new ReturnDefinitionTableAmendDialog(parent, true);
		putValue(AbstractAction.NAME, ui.getString("fina2.create"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.insert"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

		dialog.show(definitionTable, table.getRows(), -1, true);

		if (dialog.isOk())
			table.addRow(dialog.getDefinitionTable());
	}
}

class ReturnDefinitionAmendAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private java.awt.Frame parent;
	private EJBTable table;
	private ReturnDefinitionTableAmendDialog dialog;

	ReturnDefinitionAmendAction(java.awt.Frame parent, EJBTable table) {
		this.table = table;
		this.parent = parent;

		dialog = new ReturnDefinitionTableAmendDialog(parent, true);

		putValue(AbstractAction.NAME, ui.getString("fina2.amend"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.amend"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

		dialog.show((DefinitionTable) table.getSelectedTableRow(), table.getRows(), table.getSelectedRow(), true);
		if (dialog.isOk())
			table.updateRow(table.getSelectedRow(), dialog.getDefinitionTable());
	}
}

class ReturnDefinitionReviewAction extends AbstractAction {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private java.awt.Frame parent;
	private EJBTable table;
	private ReturnDefinitionTableAmendDialog dialog;

	ReturnDefinitionReviewAction(java.awt.Frame parent, EJBTable table) {
		this.table = table;
		this.parent = parent;
		dialog = new ReturnDefinitionTableAmendDialog(parent, true);
		putValue(AbstractAction.NAME, ui.getString("fina2.review"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.review"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		dialog.show((DefinitionTable) table.getSelectedTableRow(), table.getRows(), table.getSelectedRow(), false);
	}
}

class ReturnDefinitionDeleteAction extends AbstractAction {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTable table;

	ReturnDefinitionDeleteAction(java.awt.Frame parent, EJBTable table) {
		this.table = table;

		putValue(AbstractAction.NAME, ui.getString("fina2.delete"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.delete"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		if (!ui.showConfirmBox(null, ui.getString("fina2.returns.returnDefinitionTableDeleteQuestion")))
			return;
		table.removeRow(table.getSelectedRow());
	}
}

class ReturnDefinitionUpAction extends AbstractAction {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTable table;

	ReturnDefinitionUpAction(java.awt.Frame parent, EJBTable table) {
		this.table = table;
		putValue(AbstractAction.NAME, ui.getString("fina2.up"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.up"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		try {
			int r = table.getSelectedRow();
			if (r == -1)
				return;
			if (r == 0)
				return;

			Vector colNames = new Vector();
			colNames.add(ui.getString("fina2.code"));
			colNames.add("Node Name");
			colNames.add(ui.getString("fina2.type"));

			Vector v = (Vector) table.getRows();
			Object o = v.get(r);
			if (o == null)
				return;
			v.remove(r);
			v.insertElementAt(o, r - 1);

			table.initTable(colNames, v);
			table.changeSelection(r - 1, 0, false, false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class ReturnDefinitionDownAction extends AbstractAction {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private EJBTable table;

	ReturnDefinitionDownAction(java.awt.Frame parent, EJBTable table) {
		this.table = table;
		putValue(AbstractAction.NAME, ui.getString("fina2.down"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.down"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		try {
			int r = table.getSelectedRow();
			if (r == -1)
				return;
			if (r == (table.getRowCount() - 1))
				return;

			Vector colNames = new Vector();
			colNames.add(ui.getString("fina2.code"));
			colNames.add("Node Name");
			colNames.add(ui.getString("fina2.type"));

			Vector v = (Vector) table.getRows();
			Object o = v.get(r);
			if (o == null)
				return;
			v.remove(r);
			v.insertElementAt(o, r + 1);

			table.initTable(colNames, v);
			table.changeSelection(r + 1, 0, false, false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
