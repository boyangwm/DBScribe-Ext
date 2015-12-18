/*
 * BranchAmendDialog.java
 *
 * Created on March 20, 2002, 3:12 AM
 */

package fina2.bank;

import java.awt.GridBagConstraints;
import java.text.ParseException;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.transaction.UserTransaction;

import fina2.Main;
import fina2.regions.RegionStructureNodePK;
import fina2.regions.RegionStructureSelectionDialog;
import fina2.ui.UIManager;
import fina2.ui.table.TableRow;
import fina2.ui.tree.Node;

/**
 * 
 * @author vasop
 */
public class BranchAmendDialog extends javax.swing.JDialog {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private LicenceTypeSelectDialog licenceTypeDialog;
	// private BankRegionSelectDialod regionDialog;
	private RegionStructureSelectionDialog regionStructureSelectionDialog;

	private Node node;
	private Branch branch;
	private BankPK pk;
	private BranchPK branchPK;
	// private BankRegionPK bankRegionPK;

	private String licenseCode;
	private String licenseDate;
	private String licenseDateOfChange;
	private String licenseReason;

	private boolean canAmend;

	/** Creates new form BranchAmendDialog */
	public BranchAmendDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);

		// regionDialog = new BankRegionSelectDialod(parent, true);
		regionStructureSelectionDialog = new RegionStructureSelectionDialog(parent, true);
		ui.loadIcon("fina2.cancel", "cancel.gif");
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.ok", "ok.gif");
		ui.loadIcon("fina2.find", "find.gif");

		initComponents();
	}

	public Node getNode() {
		return node;
	}

	public void show(Node node, BankPK pk, boolean canAmend) {
		this.node = node;
		this.pk = pk;
		this.canAmend = canAmend;

		nameText.setEditable(canAmend);
		cityText.setEditable(false);
		regionText.setEditable(false);
		shortNameText.setEditable(canAmend);
		addressText.setEditable(canAmend);
		createDateText.setEditable(canAmend);
		cancelDateText.setEditable(canAmend);
		commentsText.setEditable(canAmend);
		regSelButton.setEnabled(canAmend);

		if (node != null) {

			branchPK = (BranchPK) node.getPrimaryKey();

			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/bank/Branch");
				BranchHome home = (BranchHome) PortableRemoteObject.narrow(ref, BranchHome.class);
				branch = home.findByPrimaryKey(branchPK);

				ref = jndi.lookup("fina2/bank/BankRegion");
				BankRegionHome bankRegionHome = (BankRegionHome) PortableRemoteObject.narrow(ref, BankRegionHome.class);

				// bankRegionPK = branch.getBankRegionPK();
				// BankRegion bankRegion = bankRegionHome
				// .findByPrimaryKey(bankRegionPK);
				//
				nameText.setText(branch.getName(main.getLanguageHandle()));
				// cityText.setText(bankRegion.getCity(main.getLanguageHandle()));
				// regionText.setText(bankRegion.getRegion(main
				// .getLanguageHandle()));

				long regionId = branch.getBankRegionPK().getId();
				String regionLable = regionStructureSelectionDialog.getTree().getNodePathLabel(new RegionStructureNodePK(regionId));
				cityText.setText(regionLable);

				shortNameText.setText(branch.getShortName(main.getLanguageHandle()));
				addressText.setText(branch.getAddress(main.getLanguageHandle()));
				createDateText.setText(ui.stringToCurrentDateFormat(branch.getDate(main.getLanguageHandle())));
				cancelDateText.setText(ui.stringToCurrentDateFormat(branch.getDateOfChange(main.getLanguageHandle())));
				commentsText.setText(branch.getComments(main.getLanguageHandle()));

			} catch (Exception e) {
				Main.generalErrorHandler(e);
				return;
			}
		} else {
			nameText.setText("");
			cityText.setText("");
			// regionText.setText("");
			shortNameText.setText("");
			addressText.setText("");
			createDateText.setText("");
			cancelDateText.setText("");
			commentsText.setText("");
		}
		setLocationRelativeTo(getParent());
		show();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("static-access")
	private void initComponents() {// GEN-BEGIN:initComponents
		jPanel1 = new javax.swing.JPanel();
		nameLabel = new javax.swing.JLabel();
		nameText = new javax.swing.JTextField();
		cityLabel = new javax.swing.JLabel();
		cityText = new javax.swing.JTextField();
		regionLabel = new javax.swing.JLabel();
		regionText = new javax.swing.JTextField();
		regSelButton = new javax.swing.JButton();
		shortNameLabel = new javax.swing.JLabel();
		shortNameText = new javax.swing.JTextField();
		addressLabel = new javax.swing.JLabel();
		addressText = new javax.swing.JTextField();
		createDateLabel = new javax.swing.JLabel();
		createDateText = new javax.swing.JTextField();
		cancelDateLabel = new javax.swing.JLabel();
		cancelDateText = new javax.swing.JTextField();
		commentsLabel = new javax.swing.JLabel();
		commentsText = new javax.swing.JTextField();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		helpButton = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();

		setTitle(ui.getString("fina2.bank.branch"));
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jPanel1.setPreferredSize(new java.awt.Dimension(603, 193));
		jPanel1.setMinimumSize(new java.awt.Dimension(265, 193));
		nameLabel.setText(ui.formatedHtmlString(ui.getString("fina2.bank.branchName")));
		nameLabel.setFont(ui.getFont());
		jPanel1.add(nameLabel, UIManager.getGridBagConstraints(-1, -1, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		nameText.setColumns(40);
		nameText.setFont(ui.getFont());
		jPanel1.add(nameText, UIManager.getGridBagConstraints(1, 0, -1, -1, 3, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		cityLabel.setText(ui.formatedHtmlString(ui.getString("fina2.bank.city")));
		cityLabel.setFont(ui.getFont());
		jPanel1.add(cityLabel, UIManager.getGridBagConstraints(0, 1, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		cityText.setColumns(35);
		cityText.setFont(ui.getFont());
		jPanel1.add(cityText, UIManager.getGridBagConstraints(1, 1, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		// regionLabel.setText(ui.getString("fina2.bank.region"));
		// regionLabel.setFont(ui.getFont());
		// jPanel1.add(regionLabel, UIManager.getGridBagConstraints(2, 1, 10,
		// 10,
		// -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 5,
		// 0, 0)));
		//
		// regionText.setColumns(15);
		// regionText.setFont(ui.getFont());
		// jPanel1.add(regionText, UIManager.getGridBagConstraints(3, 1, -1, -1,
		// -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
		// null));

		regSelButton.setIcon(ui.getIcon("fina2.find"));
		regSelButton.setFont(ui.getFont());
		regSelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				regSelButtonActionPerformed(evt);
			}
		});

		jPanel1.add(regSelButton, UIManager.getGridBagConstraints(2, 1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(0, 5, 0, 0)));

		shortNameLabel.setText(ui.getString("fina2.bank.branchShortName"));
		shortNameLabel.setFont(ui.getFont());
		jPanel1.add(shortNameLabel, UIManager.getGridBagConstraints(0, 2, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		shortNameText.setColumns(15);
		shortNameText.setFont(ui.getFont());
		jPanel1.add(shortNameText, UIManager.getGridBagConstraints(1, 2, -1, -1, 2, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		addressLabel.setText(ui.getString("fina2.bank.branchAddress"));
		addressLabel.setFont(ui.getFont());
		jPanel1.add(addressLabel, UIManager.getGridBagConstraints(0, 3, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		addressText.setColumns(40);
		addressText.setFont(ui.getFont());
		jPanel1.add(addressText, UIManager.getGridBagConstraints(1, 3, -1, -1, 3, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		createDateLabel.setText(ui.formatedHtmlString(ui.getString("fina2.bank.branchFoundationDate")));
		createDateLabel.setFont(ui.getFont());
		jPanel1.add(createDateLabel, UIManager.getGridBagConstraints(0, 4, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		createDateText.setColumns(15);
		createDateText.setFont(ui.getFont());
		jPanel1.add(createDateText, UIManager.getGridBagConstraints(1, 4, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		cancelDateLabel.setText(ui.getString("fina2.bank.branchCancelDate"));
		cancelDateLabel.setFont(ui.getFont());
		jPanel1.add(cancelDateLabel, UIManager.getGridBagConstraints(2, 4, 10, 10, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 5, 0, 0)));

		cancelDateText.setColumns(15);
		cancelDateText.setFont(ui.getFont());
		jPanel1.add(cancelDateText, UIManager.getGridBagConstraints(3, 4, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		commentsLabel.setText(ui.getString("fina2.bank.branchComments"));
		commentsLabel.setFont(ui.getFont());
		jPanel1.add(commentsLabel, UIManager.getGridBagConstraints(0, 5, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		commentsText.setColumns(15);
		commentsText.setFont(ui.getFont());
		jPanel1.add(commentsText, UIManager.getGridBagConstraints(1, 5, -1, -1, 3, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

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

		getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

		pack();
	}// GEN-END:initComponents

	private void regSelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_regSelButtonActionPerformed
		// regionDialog.show();
		// TableRow row = regionDialog.getTableRow();
		// if (row == null) {
		// bankRegionPK = null;
		// return;
		// }
		// bankRegionPK = (BankRegionPK) row.getPrimaryKey();
		// cityText.setText(row.getValue(0));
		// regionText.setText(row.getValue(1));

		regionStructureSelectionDialog.show();

		if (regionStructureSelectionDialog.getSelectionNode() != null) {
			Node node = regionStructureSelectionDialog.getSelectionNode();
			regionStructureSelectionDialog.getTree().selectNode(new DefaultMutableTreeNode(node), regionStructureSelectionDialog.getTree());
		}

		String selectNodeLabel = regionStructureSelectionDialog.getRegionStructureSelectionNodeLabel();
		if (selectNodeLabel != null)
			cityText.setText(selectNodeLabel);

	}// GEN-LAST:event_regSelButtonActionPerformed

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-
		// FIRST
		// :
		// event_okButtonActionPerformed
		if (canAmend) {
			// ===========branch name validation=================
			if (nameText.getText().trim().length() == 0) {
				ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.bank.branchname"));
				return;
			}
			if (!ui.isValidLength(nameText.getText().trim(), true)) {
				ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.bank.branchnamelength"));
				return;
			}
			// ===========city validation=================
			if (cityText.getText().trim().length() == 0) {
				ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.bank.branchcity"));
				return;
			}
			// ===========short name validation=================
			if (!ui.isValidLength(shortNameText.getText().trim(), true)) {
				ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.bank.branchshortnamelength"));
				return;
			}
			// ===========branch adress validation=================
			if (!ui.isValidLength(addressText.getText().trim(), true)) {
				ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.bank.branchaddress"));
				return;
			}
			// ===========create date validation=================
			if (createDateText.getText().trim().length() == 0) {
				ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.bank.LicenceType.date"));
				return;
			}

			if (!ui.isCorrectDate(createDateText.getText().trim())) {
				ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidDateFormat"));
				return;
			}
			// =========== date validation=================
			if (!ui.isCorrectDate(cancelDateText.getText().trim()) && cancelDateText.getText().trim().length() > 0) {
				ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidDateFormat"));
				return;
			}
			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/bank/Branch");
				BranchHome home = (BranchHome) PortableRemoteObject.narrow(ref, BranchHome.class);

				if (node == null) {
					branch = home.create();
				}
				UserTransaction trans = main.getUserTransaction(jndi);
				trans.begin();
				try {

					if (regionStructureSelectionDialog != null) {
						Node regionSelNode = regionStructureSelectionDialog.getSelectionNode();
						RegionStructureNodePK regionSelNodepk = null;
						if (regionSelNode != null) {
							regionSelNodepk = (RegionStructureNodePK) regionSelNode.getPrimaryKey();
							if (regionSelNodepk != null) {
								branch.setBankRegionPK(regionSelNodepk);

								// Set Nulls
								regionStructureSelectionDialog.setSelectionNode(null);
							}
						}
					}

					// branch.setBankRegionPK((BankRegionPK) bankRegionPK);
					branch.setName(main.getLanguageHandle(), nameText.getText().trim());
					branch.setShortName(main.getLanguageHandle(), shortNameText.getText().trim());
					branch.setAddress(main.getLanguageHandle(), addressText.getText().trim());
					branch.setComments(main.getLanguageHandle(), commentsText.getText().trim());
					branch.setDate(main.getLanguageHandle(), ui.stringFormatedToServerDataFormat(createDateText.getText().trim()));
					branch.setDateOfChange(main.getLanguageHandle(), ui.stringFormatedToServerDataFormat(cancelDateText.getText().trim()));
					branch.setBankPK(pk);
					trans.commit();
				} catch (ParseException e) {
					trans.rollback();
					if (node == null) {
						branch.remove();
						branch = null;
					}
					Main.errorHandler(null, Main.getString("fina2.title"), Main.getString("FinaTypeException.InvalidDateFormat"));
					return;
				} catch (Exception e) {
					trans.rollback();
					if (node == null) {
						branch.remove();
						branch = null;
					}

				}
				if (node == null) {
					node = new Node(branch.getPrimaryKey(), "[" + cityText.getText().trim() + ", "
					// + regionText.getText().trim()
							+ "] " + shortNameText.getText().trim(), new Integer(4));
					// bankRegionPK = branch.getBankRegionPK();
				} else {
					node.setLabel("[" + cityText.getText().trim() + ", "
					// + regionText.getText().trim()
							+ "] " + shortNameText.getText().trim());
					// bankRegionPK = branch.getBankRegionPK();
				}
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

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) {// GEN-FIRST:
		// event_closeDialog
		setVisible(false);
		dispose();
	}// GEN-LAST:event_closeDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel jPanel1;
	private javax.swing.JLabel nameLabel;
	private javax.swing.JTextField nameText;
	private javax.swing.JLabel cityLabel;
	private javax.swing.JTextField cityText;
	private javax.swing.JLabel regionLabel;
	private javax.swing.JTextField regionText;
	private javax.swing.JButton regSelButton;
	private javax.swing.JLabel shortNameLabel;
	private javax.swing.JTextField shortNameText;
	private javax.swing.JLabel addressLabel;
	private javax.swing.JTextField addressText;
	private javax.swing.JLabel createDateLabel;
	private javax.swing.JTextField createDateText;
	private javax.swing.JLabel cancelDateLabel;
	private javax.swing.JTextField cancelDateText;
	private javax.swing.JLabel commentsLabel;
	private javax.swing.JTextField commentsText;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JButton helpButton;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JButton okButton;
	private javax.swing.JButton cancelButton;
	// End of variables declaration//GEN-END:variables

}