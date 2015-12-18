/*
 * FunctionAmendDialog.java
 *
 * Created on December 8, 2001, 9:24 PM
 */

package fina2.script;

import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import fina2.ui.UIManager;

/**
 * 
 * @author David Shalamberidze
 */
public class FunctionAmendDialog extends javax.swing.JDialog {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private boolean ok;

	/** Creates new form FunctionAmendDialog */
	public FunctionAmendDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);

		ui.loadIcon("fina2.cancel", "cancel.gif");
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.ok", "ok.gif");

		initComponents();

	}

	public boolean isOk() {
		return ok;
	}

	private void setFunction(Function f) {
		paramsPanel.removeAll();
		Vector params = f.getParameters();
		int y = 0;
		for (Iterator iter = params.iterator(); iter.hasNext(); y++) {
			Parameter p = (Parameter) iter.next();

			JLabel label = new JLabel();
			label.setFont(ui.getFont());
			label.setText(p.getName());
			paramsPanel.add(label, UIManager.getGridBagConstraints(0, y, -1,
					10, -1, -1, GridBagConstraints.WEST, -1,
					new java.awt.Insets(0, 2, 0, 2)));

			JTextField text = new JTextField();
			text.setColumns(25);
			text.setFont(ui.getFont());
			if (p.getType() != p.NUMBER)
				text.setEditable(false);
			paramsPanel.add(text, UIManager.getGridBagConstraints(1, y, -1, -1,
					-1, -1, -1, -1, new java.awt.Insets(0, 2, 0, 2)));
			if (p.getType() == p.NUMBER) {
				text.setText("0");
				p.setValue("0");
				text.addPropertyChangeListener(new NumberListener(text, p));
			}

			if (p.getType() != p.NUMBER) {
				JButton button = new JButton();
				button.setFont(new java.awt.Font("Dialog", 0, 11));
				button.setText("...");
				button.setMargin(new java.awt.Insets(0, 5, 0, 5));
				paramsPanel.add(button, UIManager.getGridBagConstraints(2, y, -1, -1,
						-1, -1, -1, -1, new java.awt.Insets(0, 2, 0, 2)));
				switch (p.getType()) {
				case Parameter.NODE:
					button.addActionListener(new NodeListener(
							(java.awt.Frame) getParent(), text, p));
					text.setText("curNode");
					p.setValue("curNode");
					break;
				case Parameter.BANK:
					button.addActionListener(new BankListener(
							(java.awt.Frame) getParent(), text, p));
					text.setText("curBank");
					p.setValue("curBank");
					break;
				case Parameter.PEER_GROUP:
					button.addActionListener(new BankGroupListener(
							(java.awt.Frame) getParent(), text, p));
					text.setText("curPeerGroup");
					p.setValue("curPeerGroup");
					break;
				/*
				 * case Parameter.PERIOD: button.addActionListener( new
				 * PeriodListener((java.awt.Frame)getParent(), text, p) );
				 * break;
				 */
				case Parameter.PERIOD_TYPE:
					button.addActionListener(new PeriodTypeListener(
							(java.awt.Frame) getParent(), text, p));
					text.setText("curPeriodType");
					p.setValue("curPeriodType");
					break;
				case Parameter.MATH_FUNCTION:
					button.addActionListener(new MathFunctionListener(
							(java.awt.Frame) getParent(), text, p));
					break;
				}
			}

		}
	}

	public void show(Function f) {
		setFunction(f);

		pack();
		setLocationRelativeTo(getParent());
		super.show();
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
		jPanel5 = new javax.swing.JPanel();
		paramsPanel = new javax.swing.JPanel();
		label = new javax.swing.JLabel();
		text = new javax.swing.JTextField();
		button = new javax.swing.JButton();

		setTitle(ui.getString("fina2.script.parameters"));
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

		jPanel5.setLayout(new java.awt.BorderLayout());

		jPanel5.setBorder(new javax.swing.border.EmptyBorder(
				new java.awt.Insets(10, 20, 10, 20)));
		paramsPanel.setLayout(new java.awt.GridBagLayout());

		label.setText("jLabel1");
		paramsPanel.add(label, UIManager.getGridBagConstraints(0, 0, -1, 10,
				-1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(0, 2, 0, 2)));

		text.setColumns(25);
		text.setFont(new java.awt.Font("Dialog", 0, 11));
		paramsPanel.add(text, UIManager.getGridBagConstraints(1, 0, -1, -1,
				-1, -1, -1, -1, new java.awt.Insets(0, 2, 0, 2)));

		button.setFont(new java.awt.Font("Dialog", 0, 11));
		button.setText("...");
		button.setMargin(new java.awt.Insets(0, 5, 0, 5));
		paramsPanel.add(button, UIManager.getGridBagConstraints(2, 0, -1, -1,
				-1, -1, -1, -1, new java.awt.Insets(0, 2, 0, 2)));

		jPanel5.add(paramsPanel, java.awt.BorderLayout.NORTH);

		getContentPane().add(jPanel5, java.awt.BorderLayout.CENTER);

		pack();
	}// GEN-END:initComponents

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_cancelButtonActionPerformed
		ok = false;
		dispose();
	}// GEN-LAST:event_cancelButtonActionPerformed

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-
		// FIRST
		// :
		// event_okButtonActionPerformed
		ok = true;
		dispose();
	}// GEN-LAST:event_okButtonActionPerformed

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) {// GEN-FIRST:
		// event_closeDialog
		ok = false;
		setVisible(false);
		dispose();
	}// GEN-LAST:event_closeDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JButton helpButton;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JButton okButton;
	private javax.swing.JButton cancelButton;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel paramsPanel;
	private javax.swing.JLabel label;
	private javax.swing.JTextField text;
	private javax.swing.JButton button;
	// End of variables declaration//GEN-END:variables

}

class NodeListener implements ActionListener {

	private Parameter param;
	private JTextField text;
	private fina2.metadata.SelectNodeDialog dlg;

	NodeListener(java.awt.Frame frame, JTextField text, Parameter param) {
		this.text = text;
		this.param = param;
		dlg = new fina2.metadata.SelectNodeDialog(frame, true);
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		dlg.show();
		if (!dlg.isOk())
			return;
		fina2.ui.tree.Node node = dlg.getNode();
		if (node == null)
			return;

		String code = (String) node.getProperty("code");
		param.setValue("\"" + code + "\"");
		text.setText(code);
	}

}

class PeriodListener implements ActionListener {

	private Parameter param;
	private JTextField text;
	private fina2.period.SelectPeriodDialog dlg;

	PeriodListener(java.awt.Frame frame, JTextField text, Parameter param) {
		this.text = text;
		this.param = param;
		dlg = new fina2.period.SelectPeriodDialog(frame, true);
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		dlg.show();
		fina2.ui.table.TableRow row = dlg.getTableRow();
		if (row == null)
			return;

		String code = row.getValue(0);
		param.setValue("\"" + code + "\"");
		text.setText(code);
	}

}

class PeriodTypeListener implements ActionListener {

	private Parameter param;
	private JTextField text;
	private fina2.period.SelectPeriodTypeDialog dlg;

	PeriodTypeListener(java.awt.Frame frame, JTextField text, Parameter param) {
		this.text = text;
		this.param = param;
		dlg = new fina2.period.SelectPeriodTypeDialog(frame, true);
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		dlg.show();
		fina2.ui.table.TableRow row = dlg.getTableRow();
		if (row == null)
			return;

		String code = row.getValue(0);
		param.setValue("\"" + code + "\"");
		text.setText(code);
	}

}

class BankListener implements ActionListener {

	private Parameter param;
	private JTextField text;
	private fina2.bank.SelectBankDialog dlg;

	BankListener(java.awt.Frame frame, JTextField text, Parameter param) {
		this.text = text;
		this.param = param;
		dlg = new fina2.bank.SelectBankDialog(frame, true);
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		dlg.show();
		fina2.ui.table.TableRow row = dlg.getTableRow();
		if (row == null)
			return;

		String code = row.getValue(0);
		param.setValue("\"" + code + "\"");
		text.setText(code);
	}

}

class BankGroupListener implements ActionListener {

	private Parameter param;
	private JTextField text;
	private fina2.bank.SelectBankGroupDialog dlg;

	BankGroupListener(java.awt.Frame frame, JTextField text, Parameter param) {
		this.text = text;
		this.param = param;
		dlg = new fina2.bank.SelectBankGroupDialog(frame, true);
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		dlg.show();
		fina2.ui.table.TableRow row = dlg.getTableRow();
		if (row == null)
			return;

		String code = row.getValue(0);
		param.setValue("\"" + code + "\"");
		text.setText(code);
	}

}

class NumberListener implements PropertyChangeListener {

	private Parameter param;
	private JTextField text;

	NumberListener(JTextField text, Parameter param) {
		this.text = text;
		this.param = param;
	}

	public void propertyChange(
			java.beans.PropertyChangeEvent propertyChangeEvent) {
		param.setValue(text.getText());
	}
}

class MathFunctionListener implements ActionListener {

	private Parameter param;
	private JTextField text;
	private SelectMathFunctionDialog dlg;

	MathFunctionListener(java.awt.Frame frame, JTextField text, Parameter param) {
		this.text = text;
		this.param = param;
		dlg = new SelectMathFunctionDialog(frame, true);
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		dlg.show();
		String f = dlg.getFunction();
		if (f == null)
			return;

		param.setValue("\"" + f + "\"");
		text.setText(f);
	}

}