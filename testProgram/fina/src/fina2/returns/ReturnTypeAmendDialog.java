/*
 * AmendBankTypes.java
 *
 * Created on October 20, 2001, 2:13 PM
 */

package fina2.returns;

import java.awt.GridBagConstraints;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.transaction.UserTransaction;

import fina2.Main;
import fina2.ui.UIManager;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;

/**
 * 
 * @author Administrator
 */
public class ReturnTypeAmendDialog extends javax.swing.JDialog {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private TableRow tableRow;
	private ReturnType returnType;

	/** Creates new form AmendBankTypes */
	public ReturnTypeAmendDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);

		ui.loadIcon("fina2.cancel", "cancel.gif");
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.ok", "ok.gif");

		initComponents();

		// setLocationRelativeTo(parent);
	}

	public TableRow getTableRow() {
		return tableRow;
	}

	public void show(TableRow tableRow, boolean canAmend) {
		this.tableRow = tableRow;
		codeText.setEditable(canAmend);
		descriptionText.setEditable(canAmend);
		returnType = null;
		if (tableRow != null) {

			ReturnTypePK pk = (ReturnTypePK) tableRow.getPrimaryKey();

			try {
				InitialContext jndi = fina2.Main.getJndiContext();

				Object ref = jndi.lookup("fina2/returns/ReturnType");
				ReturnTypeHome home = (ReturnTypeHome) PortableRemoteObject
						.narrow(ref, ReturnTypeHome.class);

				returnType = home.findByPrimaryKey(pk);

				codeText.setText(returnType.getCode());
				descriptionText.setText(returnType.getDescription(main
						.getLanguageHandle()));

			} catch (Exception e) {
				Main.generalErrorHandler(e);
				return;
			}
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
		codeLabel = new javax.swing.JLabel();
		codeText = new javax.swing.JTextField();
		descriptionLabel = new javax.swing.JLabel();
		descriptionText = new javax.swing.JTextField();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		helpButton = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();

		setTitle(ui.getString("fina2.bank.amendBankTypes"));
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jPanel1.setBorder(new javax.swing.border.EmptyBorder(
				new java.awt.Insets(30, 70, 30, 70)));
		codeLabel.setText(UIManager.formatedHtmlString(ui
				.getString("fina2.code")));
		codeLabel.setFont(ui.getFont());
		jPanel1.add(codeLabel, UIManager.getGridBagConstraints(0, 0, 10, 10,
				-1, -1, -1, -1, null));

		codeText.setColumns(8);
		codeText.setFont(ui.getFont());
		jPanel1.add(codeText, UIManager.getGridBagConstraints(1, 0, -1, -1, -1,
				-1, GridBagConstraints.WEST, -1, null));

		descriptionLabel.setText(UIManager.formatedHtmlString(ui
				.getString("fina2.description")));
		descriptionLabel.setFont(ui.getFont());
		jPanel1.add(descriptionLabel, UIManager.getGridBagConstraints(0, 1, 10,
				10, -1, -1, -1, -1, null));

		descriptionText.setColumns(15);
		descriptionText.setFont(ui.getFont());
		jPanel1.add(descriptionText, UIManager.getGridBagConstraints(1, 1, -1,
				-1, -1, -1, GridBagConstraints.WEST, -1, null));

		getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

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

		getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

		pack();
	}// GEN-END:initComponents

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-
		// FIRST
		// :
		// event_okButtonActionPerformed
		if (!fina2.Main.isValidCode(codeText.getText())) {
			ui.showMessageBox(null, ui.getString("fina2.title"),
					ui.getString("fina2.invalidCode"));
			return;
		}
		try {
			if (codeText.getText().trim().equals("")) {
				ui.showMessageBox(null, ui.getString("fina2.title"),
						ui.getString("fina2.pleaseEnterCode"));
				return;
			}
			if (!ui.isValidLength(codeText.getText(), false)) {
				ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.web.banktype.code.length"));
				return;
			}
			if (descriptionText.getText().trim().equals("")) {
				ui.showMessageBox(null, ui.getString("fina2.title"),
						ui.getString("fina2.pleaseEnterDescription"));
				return;
			}
			if (!ui.isValidLength(descriptionText.getText(), true)) {
				ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.web.banktype.description.length255"));
				return;
			}

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnType");
			ReturnTypeHome home = (ReturnTypeHome) PortableRemoteObject.narrow(
					ref, ReturnTypeHome.class);

			try {
				if (tableRow == null) {
					returnType = home.create();
				}
			} catch (Exception e) {
				if (tableRow == null)
					returnType = null;
				Main.generalErrorHandler(e);
				return;
			}
			UserTransaction trans = main.getUserTransaction(jndi);
			trans.begin();

			try {
				returnType.setCode(codeText.getText());
				returnType.setDescription(main.getLanguageHandle(),
						descriptionText.getText());
				trans.commit();
			} catch (Exception e) {
				Main.generalErrorHandler(e);
				trans.rollback();
				if (tableRow == null) {
					returnType.remove();
					returnType = null;
				}
				return;
			}
			if (tableRow == null) {
				tableRow = new TableRowImpl(returnType.getPrimaryKey(), 2);
				tableRow.setValue(0, returnType.getCode());
				tableRow.setValue(1,
						returnType.getDescription(main.getLanguageHandle()));
			} else {
				tableRow.setValue(0, returnType.getCode());
				tableRow.setValue(1,
						returnType.getDescription(main.getLanguageHandle()));
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
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

	/** Closes the dialog */
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
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JButton okButton;
	// End of variables declaration//GEN-END:variables

}
