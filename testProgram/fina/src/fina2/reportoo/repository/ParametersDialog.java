package fina2.reportoo.repository;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import fina2.Main;
import fina2.bank.SelectBankDialog;
import fina2.bank.SelectBankGroupDialog;
import fina2.metadata.SelectNodeDialog;
import fina2.period.Period;
import fina2.period.PeriodHome;
import fina2.period.PeriodPK;
import fina2.period.PeriodType;
import fina2.period.PeriodTypeHome;
import fina2.period.SelectPeriodDialog;
import fina2.reportoo.RepositoryFormulasPanel;
import fina2.ui.UIManager;

public class ParametersDialog extends javax.swing.JDialog {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private fina2.Main main = fina2.Main.main;

	private Hashtable values;

	private Formula formula;

	private boolean ok;

	/** Creates new form ParametersDialog */
	public ParametersDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		ui.loadIcon("fina2.browse", "find.gif");
		ui.loadIcon("fina2.ok", "ok.gif");
		ui.loadIcon("fina2.cancel", "cancel.gif");
		ui.loadIcon("fina2.help", "help.gif");

		initComponents();
	}

	public void show() {
		formulasPanel.initTree(formula);
		super.show();
	}

	void setValue(String name, String value) {
		values.put(name, value);
	}

	public String getParameters() {
		String s = "";
		for (Iterator iter = values.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String value = (String) values.get(name);
			s += name + "=" + value + ";";
		}
		return s;
	}

	public void setFormula(Formula formula) {
		setFormula(formula, new Hashtable());
	}

	public void setFormula(Formula formula, Hashtable values) {

		this.formula = formula;
		this.values = values;

		nameLabel.setText(formula.getName());

		int i = 0;
		for (Iterator iter = formula.getParameters().iterator(); iter.hasNext(); i++) {
			Parameter p = (Parameter) iter.next();

			JLabel pName = new JLabel();
			pName.setText(p.getName());
			pName.setFont(ui.getFont());
			pPanel.add(pName, UIManager.getGridBagConstraints(0, 1, -1, -1, -1,
					-1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 0,
							5, 5)));

			JTextField pValueText = new JTextField();
			pValueText.setFont(ui.getFont());
			Object value = values.get(p.getName());
			if (value != null) {
				if (p.getType() == RepositoryConstants.PARAMETER_TYPE_PERIOD) {
					try {
						InitialContext jndi = fina2.Main.main.getJndiContext();
						Object ref = jndi.lookup("fina2/period/Period");
						PeriodHome home = (PeriodHome) PortableRemoteObject
								.narrow(ref, PeriodHome.class);

						ref = jndi.lookup("fina2/period/PeriodType");
						PeriodTypeHome typeHome = (PeriodTypeHome) PortableRemoteObject
								.narrow(ref, PeriodTypeHome.class);

						PeriodPK pk = null;
						if (value instanceof PeriodPK)
							pk = (PeriodPK) value;
						else
							pk = new PeriodPK(Integer.valueOf((String) value)
									.intValue());
						Period period = home.findByPrimaryKey(pk);

						PeriodType type = typeHome.findByPrimaryKey(period
								.getType());

						pValueText.setText(type.getDescription(main
								.getLanguageHandle())
								+ " "
								+ period.getFromDate(main.getLanguageHandle())
								+ " - "
								+ period.getToDate(main.getLanguageHandle()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					pValueText.setText((String) value);
				}
			} else {
				pValueText.setText("");
			}
			pValueText.setColumns(15);
			if (p.getType() == RepositoryConstants.PARAMETER_TYPE_OFFSET) {
				pValueText.setEditable(true);
				pValueText
						.addKeyListener(new OffsetListener(this, p.getName()));
			} else {
				pValueText.setEditable(false);
			}
			GridBagConstraints grid = UIManager.getGridBagConstraints(1, 1, -1,
					-1, -1, -1, -1, GridBagConstraints.HORIZONTAL,
					new java.awt.Insets(0, 0, 5, 5));
			grid.weightx = 1.0;
			pPanel.add(pValueText, grid);

			if (p.getType() != RepositoryConstants.PARAMETER_TYPE_OFFSET) {
				JButton pBrowseButton = new JButton();
				switch (p.getType()) {
				case RepositoryConstants.PARAMETER_TYPE_NODE:
					pBrowseButton.addActionListener(new NodeBrowseAction(this,
							p.getName(), pValueText));
					break;
				case RepositoryConstants.PARAMETER_TYPE_BANKS:
					pBrowseButton.addActionListener(new BankBrowseAction(this,
							p.getName(), pValueText));
					break;
				case RepositoryConstants.PARAMETER_TYPE_PEERGROUP:
					pBrowseButton.addActionListener(new PeerGroupBrowseAction(
							this, p.getName(), pValueText));
					break;
				case RepositoryConstants.PARAMETER_TYPE_PERIOD:
					pBrowseButton.addActionListener(new PeriodBrowseAction(
							this, p.getName(), pValueText));
					break;
				}
				pBrowseButton.setIcon(ui.getIcon("fina2.browse"));
				pBrowseButton.setToolTipText("");
				pBrowseButton.setFont(ui.getFont());
				pBrowseButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
				pPanel.add(pBrowseButton, UIManager
						.getGridBagConstraints(2, 1, -1, -1, -1, -1, -1, -1,
								new java.awt.Insets(0, 0, 5, 0)));
			}
		}
	}

	public void formulaChanged(TreeSelectionEvent event) {
		TreePath path = formulasPanel.getFormulasTree().getSelectionPath();
		if (path == null) {
			return;
		}

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
				.getLastPathComponent();
		if (node.getUserObject() instanceof Formula) {
			okButton.setEnabled(true);
			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi
						.lookup("fina2/reportoo/repository/RepositorySession");
				RepositorySessionHome home = (RepositorySessionHome) PortableRemoteObject
						.narrow(ref, RepositorySessionHome.class);
				RepositorySession session = home.create();

				Formula formula = session.findFormula(((Formula) node
						.getUserObject()).getId());

				pPanel.removeAll();
				setFormula(formula, (values == null) ? new Hashtable() : values);

			} catch (Exception ex) {
				Main.generalErrorHandler(ex);
			}
		} else {
			nameLabel.setText(ui
					.getString("fina2.reportoo.selectRepositoryFormula"));
			okButton.setEnabled(false);
			pPanel.removeAll();
		}

		pPanel.updateUI();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		jPanel1 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		helpButton = new javax.swing.JButton();
		pPanel = new javax.swing.JPanel();
		nameLabel = new javax.swing.JLabel();
		formulasPanel = new RepositoryFormulasPanel();
		paramsPanel = new javax.swing.JPanel();

		setTitle(ui.getString("fina2.reportoo.repositoryFormulaParameters"));
		setResizable(false);
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jPanel1.setLayout(new java.awt.BorderLayout());

		jPanel1.setPreferredSize(new java.awt.Dimension(400, 37));

		okButton.setIcon(ui.getIcon("fina2.ok"));
		okButton.setFont(ui.getFont());
		okButton.setText(ui.getString("fina2.ok"));
		okButton.setEnabled(false);
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

		helpButton.setIcon(ui.getIcon("fina2.help"));
		helpButton.setFont(ui.getFont());
		helpButton.setText(ui.getString("fina2.help"));
		helpButton.setEnabled(false);
		jPanel2.add(helpButton);

		jPanel1.add(jPanel2, java.awt.BorderLayout.WEST);

		getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

		paramsPanel.setLayout(new java.awt.BorderLayout());
		getContentPane().add(paramsPanel, java.awt.BorderLayout.EAST);

		pPanel.setLayout(new java.awt.GridBagLayout());

		pPanel.setBorder(new javax.swing.border.EmptyBorder(
				new java.awt.Insets(50, 70, 80, 70)));
		pPanel.setPreferredSize(new java.awt.Dimension(400, 500));
		paramsPanel.add(pPanel, java.awt.BorderLayout.CENTER);

		getContentPane().add(formulasPanel, java.awt.BorderLayout.WEST);

		nameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
		nameLabel.setBorder(new javax.swing.border.EmptyBorder(
				new java.awt.Insets(25, 1, 1, 1)));
		paramsPanel.add(nameLabel, java.awt.BorderLayout.NORTH);

		formulasPanel.getFormulasTree().addTreeSelectionListener(
				new TreeSelectionListener() {
					public void valueChanged(TreeSelectionEvent evt) {
						formulaChanged(evt);
					}
				});

		pack();
	}// GEN-END:initComponents

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
		ok = true;
		dispose();
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();
	}

	private void closeDialog(java.awt.event.WindowEvent evt) {
		dispose();
	}

	public Formula getFormula() {
		return formula;
	}

	public boolean isOk() {
		return ok;
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JButton okButton;
	private javax.swing.JButton cancelButton;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JButton helpButton;
	private javax.swing.JPanel pPanel;
	private javax.swing.JPanel paramsPanel;
	private RepositoryFormulasPanel formulasPanel;
	private javax.swing.JLabel nameLabel;
	// End of variables declaration//GEN-END:variables

}

class NodeBrowseAction implements java.awt.event.ActionListener {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private ParametersDialog dlg;
	private JTextField text;
	private String name;

	NodeBrowseAction(ParametersDialog dlg, String name, JTextField text) {
		this.dlg = dlg;
		this.name = name;
		this.text = text;
	}

	public void actionPerformed(java.awt.event.ActionEvent event) {
		/*
		 * dlg.setVisible(false);
		 * ui.getAction("fina2.actions.MDTAmend").actionPerformed(null);
		 */
		SelectNodeDialog d = new SelectNodeDialog((java.awt.Frame) dlg
				.getParent(), true);
		d.show();
		if (d.isOk()) {
			String s = (String) d.getNode().getProperty("code");
			text.setText(s);
			dlg.setValue(name, s);
		}
	}
}

class BankBrowseAction implements java.awt.event.ActionListener {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private ParametersDialog dlg;
	private JTextField text;
	private String name;

	BankBrowseAction(ParametersDialog dlg, String name, JTextField text) {
		this.dlg = dlg;
		this.name = name;
		this.text = text;
	}

	public void actionPerformed(java.awt.event.ActionEvent event) {
		// ui.getAction("fina2.actions.banks").actionPerformed(null);
		SelectBankDialog d = new SelectBankDialog((java.awt.Frame) dlg
				.getParent(), true);
		d.show();
		if (d.getTableRow() != null) {
			String s = (String) d.getTableRow().getValue(0);
			text.setText(s);
			dlg.setValue(name, s);
		}
	}
}

class PeerGroupBrowseAction implements java.awt.event.ActionListener {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private ParametersDialog dlg;
	private JTextField text;
	private String name;

	PeerGroupBrowseAction(ParametersDialog dlg, String name, JTextField text) {
		this.dlg = dlg;
		this.name = name;
		this.text = text;
	}

	public void actionPerformed(java.awt.event.ActionEvent event) {
		// ui.getAction("fina2.actions.bankGroups").actionPerformed(null);
		SelectBankGroupDialog d = new SelectBankGroupDialog(
				(java.awt.Frame) dlg.getParent(), true);
		d.show();
		if (d.getTableRow() != null) {
			String s = (String) d.getTableRow().getValue(0);
			text.setText(s);
			dlg.setValue(name, s);
		}
	}
}

class PeriodBrowseAction implements java.awt.event.ActionListener {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private ParametersDialog dlg;
	private JTextField text;
	private String name;

	PeriodBrowseAction(ParametersDialog dlg, String name, JTextField text) {
		this.dlg = dlg;
		this.name = name;
		this.text = text;
	}

	public void actionPerformed(java.awt.event.ActionEvent event) {
		// ui.getAction("fina2.actions.periods").actionPerformed(null);
		SelectPeriodDialog d = new SelectPeriodDialog((java.awt.Frame) dlg
				.getParent(), true);
		d.show();
		if (d.getTableRow() != null) {
			String s = d.getTableRow().getValue(0) + " "
					+ d.getTableRow().getValue(2) + " - "
					+ d.getTableRow().getValue(3);

			String pk = String.valueOf(((fina2.period.PeriodPK) d.getTableRow()
					.getPrimaryKey()).getId());
			text.setText(s);
			dlg.setValue(name, pk);
		}
	}
}

class OffsetListener implements java.awt.event.KeyListener {

	private ParametersDialog dlg;
	private String name;

	OffsetListener(ParametersDialog dlg, String name) {
		this.dlg = dlg;
		this.name = name;
	}

	public void keyPressed(java.awt.event.KeyEvent keyEvent) {
	}

	public void keyReleased(java.awt.event.KeyEvent evt) {
		dlg.setValue(name, ((JTextField) evt.getSource()).getText());
	}

	public void keyTyped(java.awt.event.KeyEvent keyEvent) {
	}
}
