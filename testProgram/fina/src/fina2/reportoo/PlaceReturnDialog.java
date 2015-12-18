package fina2.reportoo;

import java.awt.GridBagConstraints;
import java.util.Vector;

import fina2.returns.ReturnDefinitionPK;
import fina2.ui.UIManager;

/**
 * 
 * @author David Shalamberidze
 */
public class PlaceReturnDialog extends javax.swing.JDialog {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private fina2.returns.SelectReturnDefinitionDialog selectDialog;

	private ReturnDefinitionPK tablePK;

	private Designer designer;

	/** Creates new form PlaceReturnDialog */
	public PlaceReturnDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		designer = (Designer) parent;

		selectDialog = new fina2.returns.SelectReturnDefinitionDialog(parent, true);

		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.close", "cancel.gif");
		ui.loadIcon("fina2.ok", "ok.gif");
		ui.loadIcon("fina2.lookup", "find.gif");

		initComponents();
		Vector v = new Vector(4);
		v.add("BANKVALUE");
		v.add("PEERVALUE");
		v.add("PCTVALUE");
		v.add("ALLBANKSVALUE");
		v.add("SELBANKSVALUE");
		formulaList.setModel(new javax.swing.DefaultComboBoxModel(v));
		formulaList.setSelectedIndex(3);
	}

	public ReturnDefinitionPK getPK() {
		return tablePK;
	}

	public String getStartString() {
		return "=" + formulaList.getSelectedItem().toString().trim() + "(";
	}

	public String getEndString() {
		if (formulaList.getSelectedItem().equals("BANKVALUE")) {
			return ";" + bankCodeText.getText().trim() + ";" + periodText.getText().trim() + ";" + periodFunctionText.getText().trim() + ";" + offsetText.getText().trim() + ")";
		}
		if (formulaList.getSelectedItem().equals("PEERVALUE")) {
			return ";" + peerGroupText.getText() + ";" + peerFunctionText.getText() + ";" + periodText.getText() + ";" + periodFunctionText.getText() + ";" + offsetText.getText() + ")";
		}
		if (formulaList.getSelectedItem().equals("PCTVALUE")) {
			return ";" + bankCodeText.getText() + ";" + peerGroupText.getText() + ";" + periodText.getText() + ";" + periodFunctionText.getText() + ";" + offsetText.getText() + ")";
		}
		if (formulaList.getSelectedItem().equals("ALLBANKSVALUE")) {
			return ";" + banksFunctionText.getText() + ";" + periodText.getText() + ";" + periodFunctionText.getText() + ";" + offsetText.getText() + ")";
		}
		if (formulaList.getSelectedItem().equals("SELBANKSVALUE")) {
			return ";" + banksParameterText.getText() + ";" + banksFunctionText.getText() + ";" + periodText.getText() + ";" + periodFunctionText.getText() + ";" + offsetText.getText() + ")";
		}
		return null;
	}

	public void show() {
		okButton.setEnabled(false);
		tablePK = null;

		super.show();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		jPanel1 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		helpButton = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		formPanel = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		retDefText = new javax.swing.JTextField();
		lookupButton = new javax.swing.JButton();
		jLabel2 = new javax.swing.JLabel();
		formulaList = new javax.swing.JComboBox();
		bankCodeLabel = new javax.swing.JLabel();
		bankCodeText = new javax.swing.JTextField();
		banksFunctionLabel = new javax.swing.JLabel();
		banksFunctionText = new javax.swing.JTextField();
		peerGroupLabel = new javax.swing.JLabel();
		peerGroupText = new javax.swing.JTextField();
		peerFunctionLabel = new javax.swing.JLabel();
		peerFunctionText = new javax.swing.JTextField();
		periodLabel = new javax.swing.JLabel();
		periodText = new javax.swing.JTextField();
		periodFunctionLabel = new javax.swing.JLabel();
		periodFunctionText = new javax.swing.JTextField();
		offsetLabel = new javax.swing.JLabel();
		offsetText = new javax.swing.JTextField();
		banksParameterLabel = new javax.swing.JLabel();
		banksParameterText = new javax.swing.JTextField();

		setTitle(ui.getString("fina2.reportoo.placeReturn"));
		setResizable(false);
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
		jPanel3.add(helpButton);

		jPanel1.add(jPanel3, java.awt.BorderLayout.WEST);

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

		jPanel1.add(jPanel4, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

		formPanel.setLayout(new java.awt.GridBagLayout());

		formPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(50, 50, 50, 50)));
		jLabel1.setText(ui.formatedHtmlString(ui.getString("fina2.returns.returnDefinition")));
		jLabel1.setFont(ui.getFont());
		formPanel.add(jLabel1, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 0, 5, 5)));

		retDefText.setEditable(false);
		retDefText.setColumns(20);
		retDefText.setFont(ui.getFont());
		formPanel.add(retDefText, UIManager.getGridBagConstraints(1, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 5, 5)));

		lookupButton.setIcon(ui.getIcon("fina2.lookup"));
		lookupButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
		lookupButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				lookupButtonActionPerformed(evt);
			}
		});
		formPanel.add(lookupButton, UIManager.getGridBagConstraints(2, 0, -1, -1, -1, -1, -1, -1, new java.awt.Insets(0, 0, 5, 5)));

		jLabel2.setText(ui.formatedHtmlString(ui.getString("fina2.reportoo.formula")));
		jLabel2.setFont(ui.getFont());
		formPanel.add(jLabel2, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 0, 5, 5)));

		formulaList.setFont(ui.getFont());
		formulaList.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				formulaListActionPerformed(evt);
			}
		});
		formPanel.add(formulaList, UIManager.getGridBagConstraints(1, 1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 5, 5)));

		bankCodeLabel.setText(ui.formatedHtmlString("bankCode"));
		bankCodeLabel.setFont(ui.getFont());
		formPanel.add(bankCodeLabel, UIManager.getGridBagConstraints(0, 2, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 0, 5, 5)));

		bankCodeText.setFont(ui.getFont());
		bankCodeText.setText("CURBANK()");
		formPanel.add(bankCodeText, UIManager.getGridBagConstraints(1, 2, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 5, 5)));

		banksFunctionLabel.setText(ui.formatedHtmlString("banksFunction"));
		banksFunctionLabel.setFont(ui.getFont());
		formPanel.add(banksFunctionLabel, UIManager.getGridBagConstraints(0, 4, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 0, 5, 5)));

		banksFunctionText.setFont(ui.getFont());
		banksFunctionText.setText("\"sum\"");
		formPanel.add(banksFunctionText, UIManager.getGridBagConstraints(1, 4, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 5, 5)));

		peerGroupLabel.setText(ui.formatedHtmlString("peerGroup"));
		peerGroupLabel.setFont(ui.getFont());
		formPanel.add(peerGroupLabel, UIManager.getGridBagConstraints(0, 5, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 0, 5, 5)));

		peerGroupText.setFont(ui.getFont());
		peerGroupText.setText("CURPEERGROUP()");
		formPanel.add(peerGroupText, UIManager.getGridBagConstraints(1, 5, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 5, 5)));

		peerFunctionLabel.setText(ui.formatedHtmlString("peerFunction"));
		peerFunctionLabel.setFont(ui.getFont());
		formPanel.add(peerFunctionLabel, UIManager.getGridBagConstraints(0, 6, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 0, 5, 5)));

		peerFunctionText.setFont(ui.getFont());
		peerFunctionText.setText("\"sum\"");
		formPanel.add(peerFunctionText, UIManager.getGridBagConstraints(1, 6, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 5, 5)));

		periodLabel.setText(ui.formatedHtmlString("period"));
		periodLabel.setFont(ui.getFont());
		formPanel.add(periodLabel, UIManager.getGridBagConstraints(0, 7, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 0, 5, 5)));

		periodText.setFont(ui.getFont());
		periodText.setText("CURPERIOD()");
		formPanel.add(periodText, UIManager.getGridBagConstraints(1, 7, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 5, 5)));

		periodFunctionLabel.setText(ui.formatedHtmlString("periodFunction"));
		periodFunctionLabel.setFont(ui.getFont());
		formPanel.add(periodFunctionLabel, UIManager.getGridBagConstraints(0, 8, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 0, 5, 5)));

		periodFunctionText.setFont(ui.getFont());
		periodFunctionText.setText("\"last\"");
		formPanel.add(periodFunctionText, UIManager.getGridBagConstraints(1, 8, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 5, 5)));

		offsetLabel.setText(ui.formatedHtmlString("offset"));
		offsetLabel.setFont(ui.getFont());
		formPanel.add(offsetLabel, UIManager.getGridBagConstraints(0, 9, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 0, 5, 5)));

		offsetText.setFont(ui.getFont());
		offsetText.setText("0");
		formPanel.add(offsetText, UIManager.getGridBagConstraints(1, 9, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 5, 5)));

		banksParameterLabel.setText(ui.formatedHtmlString("banksParameterName"));
		banksParameterLabel.setFont(ui.getFont());
		formPanel.add(banksParameterLabel, UIManager.getGridBagConstraints(0, 3, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 0, 5, 5)));

		banksParameterText.setFont(ui.getFont());
		formPanel.add(banksParameterText, UIManager.getGridBagConstraints(1, 3, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 5, 5)));

		getContentPane().add(formPanel, java.awt.BorderLayout.CENTER);

		pack();
	}// GEN-END:initComponents

	private void formulaListActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_formulaListActionPerformed
		if (formulaList.getSelectedItem().equals("BANKVALUE")) {
			bankCodeLabel.setVisible(true);
			bankCodeText.setVisible(true);

			banksParameterLabel.setVisible(false);
			banksParameterText.setVisible(false);

			banksFunctionLabel.setVisible(false);
			banksFunctionText.setVisible(false);

			peerGroupLabel.setVisible(false);
			peerGroupText.setVisible(false);

			peerFunctionLabel.setVisible(false);
			peerFunctionText.setVisible(false);

			periodLabel.setVisible(true);
			periodText.setVisible(true);

			periodFunctionLabel.setVisible(true);
			periodFunctionText.setVisible(true);

			offsetLabel.setVisible(true);
			offsetText.setVisible(true);

			formPanel.doLayout();
			pack();
		}
		if (formulaList.getSelectedItem().equals("PEERVALUE")) {
			bankCodeLabel.setVisible(false);
			bankCodeText.setVisible(false);

			banksParameterLabel.setVisible(false);
			banksParameterText.setVisible(false);

			banksFunctionLabel.setVisible(false);
			banksFunctionText.setVisible(false);

			peerGroupLabel.setVisible(true);
			peerGroupText.setVisible(true);

			peerFunctionLabel.setVisible(true);
			peerFunctionText.setVisible(true);

			periodLabel.setVisible(true);
			periodText.setVisible(true);

			periodFunctionLabel.setVisible(true);
			periodFunctionText.setVisible(true);

			offsetLabel.setVisible(true);
			offsetText.setVisible(true);

			formPanel.doLayout();
			pack();
		}
		if (formulaList.getSelectedItem().equals("PCTVALUE")) {
			bankCodeLabel.setVisible(true);
			bankCodeText.setVisible(true);

			banksParameterLabel.setVisible(false);
			banksParameterText.setVisible(false);

			banksFunctionLabel.setVisible(false);
			banksFunctionText.setVisible(false);

			peerGroupLabel.setVisible(true);
			peerGroupText.setVisible(true);

			peerFunctionLabel.setVisible(false);
			peerFunctionText.setVisible(false);

			periodLabel.setVisible(true);
			periodText.setVisible(true);

			periodFunctionLabel.setVisible(true);
			periodFunctionText.setVisible(true);

			offsetLabel.setVisible(true);
			offsetText.setVisible(true);

			formPanel.doLayout();
			pack();
		}
		if (formulaList.getSelectedItem().equals("ALLBANKSVALUE")) {
			bankCodeLabel.setVisible(false);
			bankCodeText.setVisible(false);

			banksParameterLabel.setVisible(false);
			banksParameterText.setVisible(false);

			banksFunctionLabel.setVisible(true);
			banksFunctionText.setVisible(true);

			peerGroupLabel.setVisible(false);
			peerGroupText.setVisible(false);

			peerFunctionLabel.setVisible(false);
			peerFunctionText.setVisible(false);

			periodLabel.setVisible(true);
			periodText.setVisible(true);

			periodFunctionLabel.setVisible(true);
			periodFunctionText.setVisible(true);

			offsetLabel.setVisible(true);
			offsetText.setVisible(true);

			formPanel.doLayout();
			pack();
		}
		if (formulaList.getSelectedItem().equals("SELBANKSVALUE")) {
			bankCodeLabel.setVisible(false);
			bankCodeText.setVisible(false);

			banksParameterLabel.setVisible(true);
			banksParameterText.setVisible(true);

			banksFunctionLabel.setVisible(true);
			banksFunctionText.setVisible(true);

			peerGroupLabel.setVisible(false);
			peerGroupText.setVisible(false);

			peerFunctionLabel.setVisible(false);
			peerFunctionText.setVisible(false);

			periodLabel.setVisible(true);
			periodText.setVisible(true);

			periodFunctionLabel.setVisible(true);
			periodFunctionText.setVisible(true);

			offsetLabel.setVisible(true);
			offsetText.setVisible(true);

			formPanel.doLayout();
			pack();
		}
	}// GEN-LAST:event_formulaListActionPerformed

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_cancelButtonActionPerformed
		tablePK = null;
		setVisible(false);
		dispose();
	}// GEN-LAST:event_cancelButtonActionPerformed

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-
		// FIRST
		// :
		// event_okButtonActionPerformed
		if (formulaList.getSelectedItem().equals("BANKVALUE")
				&& (bankCodeText.getText().trim().length() == 0 || periodText.getText().trim().length() == 0 || periodFunctionText.getText().trim().length() == 0 || offsetText.getText().trim()
						.length() == 0)) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.PlaceReturnBankvalue"));
			return;
		} else if (formulaList.getSelectedItem().equals("PEERVALUE")
				&& (peerGroupText.getText().trim().length() == 0 || peerFunctionText.getText().trim().length() == 0 || periodFunctionText.getText().trim().length() == 0
						|| periodText.getText().trim().length() == 0 || offsetText.getText().trim().length() == 0)) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.PlaceReturnPeervalue"));
			return;

		} else if (formulaList.getSelectedItem().equals("PCTVALUE")
				&& (bankCodeText.getText().trim().length() == 0 || peerGroupText.getText().trim().length() == 0 || periodFunctionText.getText().trim().length() == 0
						|| periodText.getText().trim().length() == 0 || offsetText.getText().trim().length() == 0)) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.PlaceReturnPtcvalue"));
			return;

		} else if (formulaList.getSelectedItem().equals("ALLBANKSVALUE")
				&& (banksFunctionText.getText().trim().length() == 0 || periodFunctionText.getText().trim().length() == 0 || periodText.getText().trim().length() == 0 || offsetText.getText().trim()
						.length() == 0)) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.PlaceReturnAllbankValue"));
			return;

		} else if (formulaList.getSelectedItem().equals("SELBANKSVALUE")
				&& (banksParameterText.getText().trim().length() == 0 || banksFunctionText.getText().trim().length() == 0 || periodFunctionText.getText().trim().length() == 0
						|| periodText.getText().trim().length() == 0 || offsetText.getText().trim().length() == 0)) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.PlaceReturnselbanksvalue"));
			return;

		}

		ReturnDefinitionPK pk = getPK();
		if (pk != null) {
			designer.placeReturn(pk, getStartString(), getEndString());
		}

		setVisible(false);
		dispose();
	}// GEN-LAST:event_okButtonActionPerformed

	private void lookupButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_lookupButtonActionPerformed
		selectDialog.show();

		fina2.ui.table.TableRow row = selectDialog.getTableRow();
		if (row == null)
			return;

		tablePK = (ReturnDefinitionPK) row.getPrimaryKey();
		retDefText.setText("[" + row.getValue(0) + "] " + row.getValue(1));
		okButton.setEnabled(true);
	}// GEN-LAST:event_lookupButtonActionPerformed

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) {// GEN-FIRST:
		// event_closeDialog
		setVisible(false);
		dispose();
	}// GEN-LAST:event_closeDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JButton helpButton;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JButton okButton;
	private javax.swing.JButton cancelButton;
	private javax.swing.JPanel formPanel;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JTextField retDefText;
	private javax.swing.JButton lookupButton;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JComboBox formulaList;
	private javax.swing.JLabel bankCodeLabel;
	private javax.swing.JTextField bankCodeText;
	private javax.swing.JLabel banksFunctionLabel;
	private javax.swing.JTextField banksFunctionText;
	private javax.swing.JLabel peerGroupLabel;
	private javax.swing.JTextField peerGroupText;
	private javax.swing.JLabel peerFunctionLabel;
	private javax.swing.JTextField peerFunctionText;
	private javax.swing.JLabel periodLabel;
	private javax.swing.JTextField periodText;
	private javax.swing.JLabel periodFunctionLabel;
	private javax.swing.JTextField periodFunctionText;
	private javax.swing.JLabel offsetLabel;
	private javax.swing.JTextField offsetText;
	private javax.swing.JLabel banksParameterLabel;
	private javax.swing.JTextField banksParameterText;
	// End of variables declaration//GEN-END:variables

}