/*
 * ReturnDefinitionTableAmendDialog.java
 *
 * Created on 5 ������ 2001 �., 14:03
 */

package fina2.returns;

import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.Vector;

import fina2.Main;
import fina2.metadata.MDTNodePK;
import fina2.reportoo.ReportConstants;
import fina2.ui.UIManager;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableRow;
import fina2.ui.tree.Node;

/**
 * 
 * @author Shota Shalamberidze
 */
public class ReturnDefinitionTableAmendDialog extends javax.swing.JDialog {
	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;
	private DefinitionTable definitionTable;
	private fina2.metadata.SelectNodeDialog selectNodeDialog;
	private Node node;
	private MDTNodePK nodePK;
	private Collection rows;
	private int row;
	private EJBTable table;

	private boolean canAmend = false;
	private boolean ok;

	public ReturnDefinitionTableAmendDialog(java.awt.Frame parent, boolean modal) {

		super(parent, modal);

		ui.loadIcon("fina2.cancel", "cancel.gif");
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.ok", "ok.gif");
		ui.loadIcon("fina2.ok", "find.gif");

		selectNodeDialog = new fina2.metadata.SelectNodeDialog(parent, true);
		initComponents();

		jLabel4.setVisible(false);
		nodeVisibleCheck.setVisible(false);
		Vector eval = new Vector(6);
		eval.add(ui.getString("fina2.none"));
		eval.add(ui.getString("fina2.sum"));
		eval.add(ui.getString("fina2.average"));
		eval.add(ui.getString("fina2.min"));
		eval.add(ui.getString("fina2.max"));
		eval.add(ui.getString("fina2.equation"));
		evalMetodList.setModel(new javax.swing.DefaultComboBoxModel(new Vector(eval)));

		Vector type = new Vector(3);
		type.add(ui.getString("fina2.returns.multiple"));
		type.add(ui.getString("fina2.returns.normal"));
		type.add(ui.getString("fina2.returns.variable"));
		typeList.setModel(new javax.swing.DefaultComboBoxModel(new Vector(type)));
	}

	public boolean isOk() {
		return ok;
	}

	public DefinitionTable getDefinitionTable() {
		return this.definitionTable;
	}

	public void show(DefinitionTable definitionTable, Collection rows, int row, boolean canAmend) {
		this.definitionTable = definitionTable;
		this.canAmend = canAmend;
		this.rows = rows;
		this.row = row;

		if (definitionTable != null) {

			codeText.setText(definitionTable.getCode());

			System.out.println(definitionTable.getCode());

			levelText.setText(String.valueOf(definitionTable.getLevel()));
			textNodeName.setText(definitionTable.getNodeName());
			nodePK = definitionTable.getNode();

			switch (definitionTable.getType()) {
			case ReturnConstants.TABLETYPE_MULTIPLE:
				typeList.setSelectedIndex(0);
				break;
			case ReturnConstants.TABLETYPE_NORMAL:
				typeList.setSelectedIndex(1);
				break;
			case ReturnConstants.TABLETYPE_VARIABLE:
				typeList.setSelectedIndex(2);
				break;
			default:
				typeList.setSelectedIndex(0);
			}

			switch (definitionTable.getEvalType()) {
			case ReturnConstants.EVAL_SUM:
				evalMetodList.setSelectedIndex(1);
				break;
			case ReturnConstants.EVAL_AVERAGE:
				evalMetodList.setSelectedIndex(2);
				break;
			case ReturnConstants.EVAL_MIN:
				evalMetodList.setSelectedIndex(3);
				break;
			case ReturnConstants.EVAL_MAX:
				evalMetodList.setSelectedIndex(4);
				break;
			case ReturnConstants.EVAL_EQUATION:
				evalMetodList.setSelectedIndex(5);
				break;
			default:
				evalMetodList.setSelectedIndex(0);
			}

			if (definitionTable.getNodeVisible() == 1)
				nodeVisibleCheck.setSelected(true);
			else
				nodeVisibleCheck.setSelected(false);

			nodeVisibleCheck.setEnabled(canAmend);
			codeText.setEditable(canAmend);
			levelText.setEditable(canAmend);
			nodeVisibleCheck.setEnabled(canAmend);
			typeList.setEnabled(canAmend);
			// evalMetodList.setEnabled(canAmend);
			nodeButton.setEnabled(canAmend);
		} else
			this.definitionTable = new DefinitionTable();

		switch (typeList.getSelectedIndex()) {
		case 2:
			if (canAmend)
				evalMetodList.setEnabled(true);
			else
				evalMetodList.setEnabled(false);
			break;
		default:
			evalMetodList.setEnabled(false);
			break;
		}
		setLocationRelativeTo(getParent());
		show();
	}

	private void initComponents() {// GEN-BEGIN:initComponents

		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		helpButton = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		jPanel5 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		codeText = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		nodeButton = new javax.swing.JButton();
		jLabel3 = new javax.swing.JLabel();
		levelText = new javax.swing.JTextField();
		jLabel4 = new javax.swing.JLabel();
		nodeVisibleCheck = new javax.swing.JCheckBox();
		jLabel5 = new javax.swing.JLabel();
		typeList = new javax.swing.JComboBox();
		jLabel6 = new javax.swing.JLabel();
		evalMetodList = new javax.swing.JComboBox();
		textNodeName = new javax.swing.JTextField();

		setTitle(ui.getString("fina2.returns.returnDefinitionTable"));
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jPanel1.setLayout(new java.awt.BorderLayout());

		jPanel2.setLayout(new java.awt.BorderLayout());

		helpButton.setIcon(ui.getIcon("fina2.help"));
		helpButton.setFont(ui.getFont());
		helpButton.setText(ui.getString("fina2.help"));
		helpButton.setEnabled(false);
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

		jPanel1.add(jPanel2, java.awt.BorderLayout.SOUTH);

		jPanel5.setLayout(new java.awt.GridBagLayout());

		jPanel5.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(30, 120, 30, 120)));
		jLabel1.setText(UIManager.formatedHtmlString(ui.getString("fina2.code")));
		jLabel1.setFont(ui.getFont());
		jPanel5.add(jLabel1, UIManager.getGridBagConstraints(0, 0, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		codeText.setColumns(8);
		codeText.setFont(ui.getFont());
		jPanel5.add(codeText, UIManager.getGridBagConstraints(1, 0, -1, -1, -1, -1, GridBagConstraints.WEST, -1, null));

		jLabel2.setText(UIManager.formatedHtmlString(ui.getString("fina2.node")));
		jLabel2.setFont(ui.getFont());
		jPanel5.add(jLabel2, UIManager.getGridBagConstraints(0, 1, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		nodeButton.setIcon(ui.getIcon("fina2.find"));
		nodeButton.setFont(ui.getFont());
		nodeButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
		nodeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				nodeButtonActionPerformed(evt);
			}
		});
		jPanel5.add(nodeButton, UIManager.getGridBagConstraints(2, 1, -1, -1, -1, -1, -1, -1, new java.awt.Insets(0, 5, 0, 0)));

		jLabel3.setText(UIManager.formatedHtmlString(ui.getString("fina2.level")));
		jLabel3.setFont(ui.getFont());
		jPanel5.add(jLabel3, UIManager.getGridBagConstraints(0, 2, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		levelText.setColumns(3);
		levelText.setFont(ui.getFont());
		jPanel5.add(levelText, UIManager.getGridBagConstraints(1, 2, -1, -1, -1, -1, GridBagConstraints.WEST, -1, null));

		jLabel4.setText(ui.getString("fina2.returns.nodeVisible"));
		jLabel4.setFont(ui.getFont());
		jPanel5.add(jLabel4, UIManager.getGridBagConstraints(0, 3, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		nodeVisibleCheck.setFont(ui.getFont());
		jPanel5.add(nodeVisibleCheck, UIManager.getGridBagConstraints(1, 3, -1, -1, -1, -1, GridBagConstraints.WEST, -1, null));

		jLabel5.setText(ui.getString("fina2.type"));
		jLabel5.setFont(ui.getFont());
		jPanel5.add(jLabel5, UIManager.getGridBagConstraints(0, 4, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		typeList.setFont(ui.getFont());
		typeList.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				typeListActionPerformed(evt);
			}
		});
		jPanel5.add(typeList, UIManager.getGridBagConstraints(1, 4, -1, -1, -1, -1, GridBagConstraints.WEST, -1, null));

		jLabel6.setText(ui.getString("fina2.metadata.evalmetod"));
		jLabel6.setFont(ui.getFont());
		jPanel5.add(jLabel6, UIManager.getGridBagConstraints(0, 5, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		evalMetodList.setFont(ui.getFont());
		jPanel5.add(evalMetodList, UIManager.getGridBagConstraints(1, 5, -1, -1, -1, -1, GridBagConstraints.WEST, -1, null));

		textNodeName.setEditable(false);
		textNodeName.setColumns(16);
		textNodeName.setFont(ui.getFont());
		jPanel5.add(textNodeName, UIManager.getGridBagConstraints(1, 1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, null));

		jPanel1.add(jPanel5, java.awt.BorderLayout.WEST);

		getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

		pack();
	}// GEN-END:initComponents

	private void typeListActionPerformed(java.awt.event.ActionEvent evt) {// GEN-
		// FIRST
		// :
		// event_typeListActionPerformed
		switch (typeList.getSelectedIndex()) {
		case 2:
			evalMetodList.setEnabled(true);
			break;
		default:
			evalMetodList.setEnabled(false);
			break;
		}
	}// GEN-LAST:event_typeListActionPerformed

	private void nodeButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_nodeButtonActionPerformed
		selectNodeDialog.show();
		if (!selectNodeDialog.isOk())
			return;

		node = (Node) selectNodeDialog.getNode();

		if (node != null) {
			if (((Integer) node.getType()).intValue() == fina2.metadata.MDTConstants.NODETYPE_NODE) {
				textNodeName.setText(node.getLabel());
				nodePK = (MDTNodePK) node.getPrimaryKey();
			} else
				ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.returns.invalidNode"));
		}
	}// GEN-LAST:event_nodeButtonActionPerformed

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-
		// FIRST
		// :
		// event_okButtonActionPerformed
		if (canAmend) {
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

			try {
				ok = true;
				int i = 0;

				for (java.util.Iterator iter = rows.iterator(); iter.hasNext(); i++) {
					if (((codeText.getText().trim().equals(((TableRow) iter.next()).getValue(0).trim())) && ((row == -1) || (row != i))) || (codeText.getText().trim().length() == 0)) {
						ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.returns.invalidCode"));
						return;
					}
				}

				definitionTable.setNodeName(textNodeName.getText());

				if (nodePK == null) {
					ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.returns.invalidNode"));
					return;
				}
				definitionTable.setCode(codeText.getText().trim());
				definitionTable.setNode(nodePK);
				switch (typeList.getSelectedIndex()) {
				case 0:
					definitionTable.setType(ReturnConstants.TABLETYPE_MULTIPLE);
					break;
				case 1:
					definitionTable.setType(ReturnConstants.TABLETYPE_NORMAL);
					break;
				case 2:
					definitionTable.setType(ReturnConstants.TABLETYPE_VARIABLE);
					break;
				}

				switch (evalMetodList.getSelectedIndex()) {
				case 0:
					definitionTable.setEvalType(0);
					break;
				case 1:
					definitionTable.setEvalType(ReturnConstants.EVAL_SUM);
					break;
				case 2:
					definitionTable.setEvalType(ReturnConstants.EVAL_AVERAGE);
					break;
				case 3:
					definitionTable.setEvalType(ReturnConstants.EVAL_MIN);
					break;
				case 4:
					definitionTable.setEvalType(ReturnConstants.EVAL_MAX);
					break;
				case 5:
					definitionTable.setEvalType(ReturnConstants.EVAL_EQUATION);
					break;
				}

				if (nodeVisibleCheck.isSelected())
					definitionTable.setNodeVisible(1);
				else
					definitionTable.setNodeVisible(0);

				definitionTable.setLevel(Integer.valueOf(levelText.getText()).intValue());

			} catch (NumberFormatException e) {
				Main.errorHandler(null, Main.getString("fina2.title"), Main.getString("fina2.returns.invalidLevelNumber"));
				return;
			} catch (Exception e) {
				Main.generalErrorHandler(e);
				return;
			}
		}
		dispose();
	}// GEN-LAST:event_okButtonActionPerformed

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_cancelButtonActionPerformed
		ok = false;
		dispose();
	}// GEN-LAST:event_cancelButtonActionPerformed

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) {// GEN-FIRST:
		// event_closeDialog
		ok = false;
		setVisible(false);
		dispose();
	}// GEN-LAST:event_closeDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton cancelButton;
	private javax.swing.JTextField codeText;
	private javax.swing.JComboBox evalMetodList;
	private javax.swing.JButton helpButton;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JTextField levelText;
	private javax.swing.JButton nodeButton;
	private javax.swing.JCheckBox nodeVisibleCheck;
	private javax.swing.JButton okButton;
	private javax.swing.JTextField textNodeName;
	private javax.swing.JComboBox typeList;
	// End of variables declaration//GEN-END:variables

}
