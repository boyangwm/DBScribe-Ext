/*
 * BankGroupSelectDialog.java
 *
 * Created on October 26, 2001, 1:18 PM
 */

package fina2.bank;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.swing.ListSelectionModel;

import fina2.FinaTypeException;
import fina2.Main;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableRow;

/**
 * 
 * @author Administrator
 */
public class BankGroupSelectDialog extends javax.swing.JDialog {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTable table;
	private TableRow tableRow;
	private Collection tableRows = new LinkedList();

	/** Creates new form BankGroupSelectDialog */
	public BankGroupSelectDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);

		ui.loadIcon("fina2.cancel", "cancel.gif");
		ui.loadIcon("fina2.ok", "ok.gif");

		table = new EJBTable();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				if (table.getSelectedRow() == -1) {
					okButton.setEnabled(false);
				} else {
					okButton.setEnabled(true);
				}
			}
		});

		table.addMouseListener(new java.awt.event.MouseListener() {
			public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					if (table.getSelectedRow() != -1) {
						okButtonActionPerformed(null);
					}
				}
			}

			public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
			}
		});

		initComponents();
		scrollPane.setViewportView(table);
		setLocationRelativeTo(parent);
	}

	public TableRow getTableRow() {
		return tableRow;
	}

	public Collection getTableRows() {
		return tableRows;
	}

	@SuppressWarnings("unchecked")
	private void initTable() {
		try {
			tableRow = null;
			tableRows.clear();
			tableRows.addAll(getDefaultBankGroups());

			Vector colNames = new Vector();
			colNames.add(ui.getString("fina2.code"));
			colNames.add(ui.getString("fina2.description"));
			table.initTable(colNames, tableRows);

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	@SuppressWarnings("unchecked")
	public Collection getDefaultBankGroups() throws FinaTypeException, RemoteException, EJBException, CreateException, ClassCastException, NamingException {

		Collection retVal = new LinkedList();
		InitialContext jndi = fina2.Main.getJndiContext();
		Object ref = jndi.lookup("fina2/bank/BankSession");
		BankSessionHome home = (BankSessionHome) PortableRemoteObject.narrow(ref, BankSessionHome.class);

		BankSession session = home.create();

		Collection col = session.getBankGroupsRows(main.getUserHandle(), main.getLanguageHandle());
		for (Iterator iter = col.iterator(); iter.hasNext();) {
			TableRow tableRow = (TableRow) iter.next();
			if (tableRow.getValue(5).equals(String.valueOf(BankCriterionConstants.DEF_CRITERION))) {
				tableRow.setValue(1, tableRow.getValue(3));
				retVal.add(tableRow);
			}
		}
		return retVal;
	}

	public void show() {
		if (isVisible())
			return;

		initTable();
		super.show();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		jPanel1 = new javax.swing.JPanel();
		scrollPane = new javax.swing.JScrollPane();
		jPanel2 = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();

		setTitle(ui.getString("fina2.bank.bankGroupsAction"));
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jPanel1.setLayout(new java.awt.BorderLayout());

		scrollPane.setPreferredSize(new java.awt.Dimension(350, 350));
		jPanel1.add(scrollPane, java.awt.BorderLayout.CENTER);

		getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

		okButton.setIcon(ui.getIcon("fina2.ok"));
		okButton.setFont(ui.getFont());
		okButton.setText(ui.getString("fina2.ok"));
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonActionPerformed(evt);
			}
		});

		jPanel2.add(okButton);

		cancelButton.setIcon(ui.getIcon("fina2.cancel"));
		cancelButton.setFont(ui.getFont());
		cancelButton.setText(ui.getString("fina2.cancel"));
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		jPanel2.add(cancelButton);

		getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

		pack();
	}// GEN-END:initComponents

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_okButtonActionPerformed

		tableRow = table.getSelectedTableRow();

		setVisible(false);
		dispose();
	}// GEN-LAST:event_okButtonActionPerformed

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cancelButtonActionPerformed
		setVisible(false);
		dispose();
	}// GEN-LAST:event_cancelButtonActionPerformed

	private void closeDialog(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_closeDialog
		setVisible(false);
		dispose();
	}// GEN-LAST:event_closeDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JButton okButton;
	private javax.swing.JButton cancelButton;
	// End of variables declaration//GEN-END:variables

}
