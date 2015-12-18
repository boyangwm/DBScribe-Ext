/*
 * BankAmendDialog.java
 *
 * Created on October 21, 2001, 6:51 PM
 */

package fina2.bank;

import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.Iterator;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.transaction.UserTransaction;

import fina2.Main;
import fina2.regions.RegionStructureNodePK;
import fina2.regions.RegionStructureSelectionDialog;
import fina2.security.UserSession;
import fina2.security.UserSessionHome;
import fina2.ui.UIManager;
import fina2.ui.table.TableRow;
import fina2.ui.tree.Node;

@SuppressWarnings("serial")
public class BankAmendDialog extends javax.swing.JDialog {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private BankTypeSelectDialog bankTypeDialog;
	private BankGroupSelectDialog bankGroupDialog;
	private RegionStructureSelectionDialog bankRegionDialog;
	private BanksFrame banksFrame;

	private Node node;
	private Bank bank;
	private BankTypePK bankTypePK;
	private BankGroupPK bankGroupPK;
	private BankPK pk;
	private boolean canAmend;

	public BanksFrame getBanksFrame() {
		return banksFrame;
	}

	public void setBanksFrame(BanksFrame banksFrame) {
		this.banksFrame = banksFrame;
	}

	/** Creates new form BankAmendDialog */
	public BankAmendDialog(BankTypePK bankType, String bankTypeStr) {

		/* Init dialog resources */
		this(Main.main.getMainFrame(), true);

		/* Init bank type info */
		setBankTypeInfo(bankType, bankTypeStr);

	}

	/** Creates new form BankAmendDialog */
	public BankAmendDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);

		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.ok", "ok.gif");
		ui.loadIcon("fina2.cancel", "cancel.gif");

		ui.loadIcon("fina2.find", "find.gif");

		bankTypeDialog = new BankTypeSelectDialog(parent, true);
		bankGroupDialog = new BankGroupSelectDialog(parent, true);
		bankRegionDialog = new RegionStructureSelectionDialog(parent, true);

		initComponents();

	}

	/** Sets a type for current bank */
	private void setBankTypeInfo(BankTypePK bankType, String bankTypeStr) {

		/* Bank type PK */
		this.bankTypePK = bankType;

		/* Text field */
		bankTypeText.setText(bankTypeStr);
	}

	public Node getNode() {
		return node;
	}

	public void show(Node node, BankPK pk, boolean canAmend) {

		this.node = node;
		this.pk = pk;
		this.canAmend = canAmend;
		selBankGroupButton.setEnabled(canAmend);
		codeText.setEditable(canAmend);
		swiftText.setEditable(canAmend);
		snameText.setEditable(canAmend);
		nameText.setEditable(canAmend);
		addressText.setEditable(canAmend);
		phoneText.setEditable(canAmend);
		faxText.setEditable(canAmend);
		emailText.setEditable(canAmend);
		telexText.setEditable(canAmend);
		selBankTypeButton.setEnabled(canAmend);
		regionCityButton.setEnabled(canAmend);

		bank = null;
		pk = null;
		if (node != null) {

			pk = (BankPK) node.getPrimaryKey();

			try {
				InitialContext jndi = fina2.Main.getJndiContext();
				Object ref = jndi.lookup("fina2/bank/Bank");
				BankHome home = (BankHome) PortableRemoteObject.narrow(ref, BankHome.class);

				bank = home.findByPrimaryKey(pk);

				ref = jndi.lookup("fina2/bank/BankGroup");

				BankGroupHome groupHome = (BankGroupHome) PortableRemoteObject.narrow(ref, BankGroupHome.class);
				bankGroupPK = getGroupPK();
				BankGroup bankGroup = groupHome.findByPrimaryKey(bankGroupPK);
				String s = bankGroup.getDescription(main.getLanguageHandle());
				if ((s == null) || (s.trim().equals("")))
					bankGroupText.setText("NONAME");
				else
					bankGroupText.setText(s);
				codeText.setText(bank.getCode());
				swiftText.setText(bank.getSwiftCode());
				snameText.setText(bank.getShortName(main.getLanguageHandle()));
				nameText.setText(bank.getName(main.getLanguageHandle()));
				addressText.setText(bank.getAddress(main.getLanguageHandle()));
				phoneText.setText(bank.getPhone());
				faxText.setText(bank.getFax());
				emailText.setText(bank.getEmail());

				String telex = bank.getTelex();
				if (telex == null || telex.trim().equals("")) {
					long regionId = bank.getRegionId();
					String regionLable = bankRegionDialog.getTree().getNodePathLabel(new RegionStructureNodePK(regionId));
					cityText.setText(regionLable);
				} else {
					cityText.setText(telex);
				}

				// StringTokenizer st = new StringTokenizer(telexText.getText(),
				// "/");
				// while (st.hasMoreTokens()) {
				// cityText.setText(st.nextToken());
				// regionText.setText(st.nextToken());
				// }

				ref = jndi.lookup("fina2/bank/BankType");

				BankTypeHome typeHome = (BankTypeHome) PortableRemoteObject.narrow(ref, BankTypeHome.class);
				bankTypePK = bank.getTypePK();
				BankType bankType = typeHome.findByPrimaryKey(bankTypePK);
				bankTypeText.setText(bankType.getDescription(main.getLanguageHandle()));
			} catch (Exception e) {
				Main.generalErrorHandler(e);
				return;
			}
		} else {
			bankGroupText.setText("");
			codeText.setText("");
			swiftText.setText("");
			snameText.setText("");
			nameText.setText("");
			cityText.setText("");
			regionText.setText("");
			addressText.setText("");
			phoneText.setText("");
			faxText.setText("");
			emailText.setText("");
			telexText.setText("");
			bankTypeText.setText("");
		}
		setLocationRelativeTo(getParent());
		show();
	}

	private BankGroupPK getGroupPK() {
		BankGroupPK retVal = null;
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/bank/BankSession");
			BankSessionHome home = (BankSessionHome) PortableRemoteObject.narrow(ref, BankSessionHome.class);
			BankSession session = home.create();
			Collection nodes = session.getBankGroupNodes(main.getUserHandle(), main.getLanguageHandle(), pk);
			for (Iterator iter = nodes.iterator(); iter.hasNext();) {
				Node node = (Node) iter.next();
				if (node.isDefaultNode()) {
					retVal = (BankGroupPK) node.getPrimaryKey();
					break;
				}
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		return retVal;
	}

	public void show() {
		if (isVisible())
			return;
		super.show();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		jPanel1 = new javax.swing.JPanel();
		bankGroupgLabel = new javax.swing.JLabel();
		bankGroupText = new javax.swing.JTextField();
		codeLabel = new javax.swing.JLabel();
		codeText = new javax.swing.JTextField();
		swiftLabel = new javax.swing.JLabel();
		swiftText = new javax.swing.JTextField();
		snameLabel = new javax.swing.JLabel();
		snameText = new javax.swing.JTextField();
		nameLabel = new javax.swing.JLabel();
		nameText = new javax.swing.JTextField();
		cityLabel = new JLabel();
		cityText = new JTextField();
		regionLabel = new JLabel();
		regionText = new JTextField();
		addressLabel = new javax.swing.JLabel();
		addressText = new javax.swing.JTextField();
		phoneLabel = new javax.swing.JLabel();
		phoneText = new javax.swing.JTextField();
		faxLabel = new javax.swing.JLabel();
		faxText = new javax.swing.JTextField();
		emailLabel = new javax.swing.JLabel();
		emailText = new javax.swing.JTextField();
		telexLabel = new javax.swing.JLabel();
		telexText = new javax.swing.JTextField();
		bankTypeLabel = new javax.swing.JLabel();
		bankTypeText = new javax.swing.JTextField();
		selBankGroupButton = new javax.swing.JButton();
		selBankTypeButton = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		helpButton = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		regionCityButton = new JButton();

		setTitle(ui.getString("fina2.bank.bank"));
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jPanel1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(30, 30, 30, 30)));
		bankGroupgLabel.setText(UIManager.formatedHtmlString(ui.getString("fina2.bank.bankGroupDefault")));
		bankGroupgLabel.setFont(ui.getFont());
		jPanel1.add(bankGroupgLabel, UIManager.getGridBagConstraints(0, 0, 10, 10, -1, -1, java.awt.GridBagConstraints.EAST, -1, null));

		bankGroupText.setEditable(false);
		bankGroupText.setColumns(18);
		bankGroupText.setFont(ui.getFont());
		jPanel1.add(bankGroupText, UIManager.getGridBagConstraints(1, 0, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		codeLabel.setText(UIManager.formatedHtmlString(ui.getString("fina2.code")));
		codeLabel.setFont(ui.getFont());
		jPanel1.add(codeLabel, UIManager.getGridBagConstraints(0, 1, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		codeText.setColumns(18);
		codeText.setFont(ui.getFont());
		jPanel1.add(codeText, UIManager.getGridBagConstraints(1, 1, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		swiftLabel.setText(ui.getString("fina2.bank.swiftCode"));
		swiftLabel.setFont(ui.getFont());
		jPanel1.add(swiftLabel, UIManager.getGridBagConstraints(2, 2, 10, 10, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 5, 0, 0)));

		swiftText.setColumns(18);
		swiftText.setFont(ui.getFont());
		jPanel1.add(swiftText, UIManager.getGridBagConstraints(3, 2, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		snameLabel.setText(UIManager.formatedHtmlString(ui.getString("fina2.bank.sname")));
		snameLabel.setFont(ui.getFont());
		jPanel1.add(snameLabel, UIManager.getGridBagConstraints(0, 2, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		snameText.setColumns(18);
		snameText.setFont(ui.getFont());
		jPanel1.add(snameText, UIManager.getGridBagConstraints(1, 2, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		nameLabel.setText(UIManager.formatedHtmlString(ui.getString("fina2.bank.name")));
		nameLabel.setFont(ui.getFont());
		jPanel1.add(nameLabel, UIManager.getGridBagConstraints(0, 3, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		nameText.setColumns(44);
		nameText.setFont(ui.getFont());
		jPanel1.add(nameText, UIManager.getGridBagConstraints(1, 3, -1, -1, 3, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		cityLabel.setText(UIManager.formatedHtmlString(ui.getString("fina2.bank.city")));
		cityLabel.setFont(ui.getFont());
		jPanel1.add(cityLabel, UIManager.getGridBagConstraints(0, 4, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		cityText.setEditable(false);
		cityText.setColumns(44);
		cityText.setFont(ui.getFont());
		jPanel1.add(cityText, UIManager.getGridBagConstraints(1, 4, -1, -1, 1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		// TODO Test Nika
		/*
		 * regionLabel.setText(UIManager.formatedHtmlString(ui
		 * .getString("fina2.bank.region"))); regionLabel.setFont(ui.getFont());
		 * jPanel1.add(regionLabel, UIManager.getGridBagConstraints(2, 4, 10,
		 * 10, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 5, 0,
		 * 0)));
		 * 
		 * regionText.setEditable(false); regionText.setColumns(18);
		 * regionText.setFont(ui.getFont()); jPanel1.add(regionText,
		 * UIManager.getGridBagConstraints(3, 4, -1, -1, -1, -1,
		 * GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));
		 */

		regionCityButton.setIcon(ui.getIcon("fina2.find"));
		regionCityButton.setFont(ui.getFont());
		regionCityButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				regionCityButtonActionPerformed(evt);
			}
		});
		jPanel1.add(regionCityButton, UIManager.getGridBagConstraints(2, 4, -1, -1, -1, -1, -1, -1, null));

		addressLabel.setText(ui.getString("fina2.bank.address"));
		addressLabel.setFont(ui.getFont());
		jPanel1.add(addressLabel, UIManager.getGridBagConstraints(0, 5, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		addressText.setColumns(44);
		addressText.setFont(ui.getFont());
		jPanel1.add(addressText, UIManager.getGridBagConstraints(1, 5, -1, -1, 3, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		phoneLabel.setText(ui.getString("fina2.bank.phone"));
		phoneLabel.setFont(ui.getFont());
		jPanel1.add(phoneLabel, UIManager.getGridBagConstraints(0, 6, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		phoneText.setColumns(18);
		phoneText.setFont(ui.getFont());
		jPanel1.add(phoneText, UIManager.getGridBagConstraints(1, 6, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		faxLabel.setText(ui.getString("fina2.bank.fax"));
		faxLabel.setFont(ui.getFont());
		jPanel1.add(faxLabel, UIManager.getGridBagConstraints(2, 6, 10, 10, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 5, 0, 0)));

		faxText.setColumns(18);
		faxText.setFont(ui.getFont());
		jPanel1.add(faxText, UIManager.getGridBagConstraints(3, 6, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		emailLabel.setText(ui.getString("fina2.bank.email"));
		emailLabel.setFont(ui.getFont());
		jPanel1.add(emailLabel, UIManager.getGridBagConstraints(0, 7, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		emailText.setColumns(18);
		emailText.setFont(ui.getFont());
		jPanel1.add(emailText, UIManager.getGridBagConstraints(1, 7, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		telexLabel.setText(ui.getString("fina2.bank.telex"));
		telexLabel.setFont(ui.getFont());
		// jPanel1.add(telexLabel, UIManager.getGridBagConstraints(2, 7, 10, 10,
		// -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 5, 0,
		// 0)));

		telexText.setColumns(18);
		telexText.setFont(ui.getFont());
		// jPanel1.add(telexText, UIManager.getGridBagConstraints(3, 7, -1, -1,
		// -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
		// null));

		bankTypeLabel.setText(UIManager.formatedHtmlString(ui.getString("fina2.bank.bankType")));
		bankTypeLabel.setFont(ui.getFont());
		jPanel1.add(bankTypeLabel, UIManager.getGridBagConstraints(0, 8, 10, 10, -1, -1, GridBagConstraints.EAST, -1, null));

		bankTypeText.setEditable(false);
		bankTypeText.setColumns(18);
		bankTypeText.setFont(ui.getFont());
		jPanel1.add(bankTypeText, UIManager.getGridBagConstraints(1, 8, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		selBankGroupButton.setIcon(ui.getIcon("fina2.find"));
		selBankGroupButton.setFont(ui.getFont());
		selBankGroupButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				selBankGroupButtonActionPerformed(evt);
			}
		});

		jPanel1.add(selBankGroupButton, UIManager.getGridBagConstraints(2, 0, -1, -1, -1, -1, -1, -1, null));

		selBankTypeButton.setIcon(ui.getIcon("fina2.find"));
		selBankTypeButton.setFont(ui.getFont());
		selBankTypeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				selBankTypeButtonActionPerformed(evt);
			}
		});

		jPanel1.add(selBankTypeButton, UIManager.getGridBagConstraints(2, 8, -1, -1, -1, -1, -1, -1, null));

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
		this.setMinimumSize(this.getSize());
	}// GEN-END:initComponents

	private void licenseDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:
		// event_licenseDeleteButtonActionPerformed
		// Add your handling code here:
	}// GEN-LAST:event_licenseDeleteButtonActionPerformed

	private void licenseAmendButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:
		// event_licenseAmendButtonActionPerformed
		// Add your handling code here:
	}// GEN-LAST:event_licenseAmendButtonActionPerformed

	private void licenseCreateButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:
		// event_licenseCreateButtonActionPerformed
		// Add your handling code here:
	}// GEN-LAST:event_licenseCreateButtonActionPerformed

	private void regionCityButtonActionPerformed(java.awt.event.ActionEvent evt) {
		bankRegionDialog.show();

		if (bankRegionDialog.getSelectionNode() != null) {
			Node node = bankRegionDialog.getSelectionNode();
			bankRegionDialog.getTree().selectNode(new DefaultMutableTreeNode(node), bankRegionDialog.getTree());
		}

		String selectNodeLabel = bankRegionDialog.getRegionStructureSelectionNodeLabel();
		if (selectNodeLabel != null)
			cityText.setText(selectNodeLabel);

		// String cityT = (bankRegionDialog.getTableRow() != null) ?
		// bankRegionDialog.getTableRow().getValue(0) : null;
		// String regionT = (bankRegionDialog.getTableRow() != null) ?
		// bankRegionDialog.getTableRow().getValue(1) : null;
		// cityText.setText(cityT);
		// regionText.setText(regionT);
	}

	private void selBankTypeButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_selBankTypeButtonActionPerformed
		bankTypeDialog.show();
		TableRow row = bankTypeDialog.getTableRow();

		if (row == null) {
			bankTypePK = null;
			return;
		}

		/* Setting the selected info */
		BankTypePK bankType = (BankTypePK) row.getPrimaryKey();
		String bankTypeName = row.getValue(1);

		setBankTypeInfo(bankType, bankTypeName);

	}// GEN-LAST:event_selBankTypeButtonActionPerformed

	private void selBankGroupButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:
		// event_selBankGroupButtonActionPerformed

		bankGroupDialog.show();
		TableRow row = bankGroupDialog.getTableRow();
		if (row == null) {
			bankGroupPK = null;
			// bankGroupText.setText("");
			return;
		}
		bankGroupPK = (BankGroupPK) row.getPrimaryKey();
		bankGroupText.setText(row.getValue(1));
	}// GEN-LAST:event_selBankGroupButtonActionPerformed

	@SuppressWarnings("unused")
	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {

		// ------------Bank-group validation-----
		if (bankGroupText.getText().length() == 0) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidBankGroup"));
			return;
		}

		// ------------Code validation-----
		if (!ui.isValidCode(codeText.getText())) {
			if (!ui.isValidLength(codeText.getText(), false)) {
				ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidLengthCode"));
				return;
			} else if (codeText.getText().length() == 0) {
				ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.definiton.code"));
				return;
			} else {
				ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidCode"));
				return;
			}
		}

		// -------Short name validation-------
		if (!ui.isValidLength(snameText.getText(), true)) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidLengthShortName"));
			return;
		} else if (snameText.getText().length() == 0) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidShortnameEmpty"));
			return;
		}

		// -------Name validation-------
		if (!ui.isValidLength(nameText.getText(), true)) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidLengthName"));
			return;
		} else if (nameText.getText().length() == 0) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidnameEmpty"));
			return;
		}
		// ----------City Validation---

		if (cityText.getText().length() == 0) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidCity"));
			return;
		}
		// -------Swift validation-------

		if (!ui.isValidLength(swiftText.getText(), false)) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidSwiftLength"));
			return;
		} else if (!ui.isValidCode(swiftText.getText()) && swiftText.getText().length() > 0) {

			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidSwiftCode"));
			return;
		}

		// ----------Fax validation------

		if (false) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidFax"));
			return;
		}
		if (!ui.isValidLength(faxText.getText(), true)) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidFaxLength"));
			return;
		}
		// ----------Phone validation------
		if (false) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidPhone"));
			return;
		}
		if (!ui.isValidLength(phoneText.getText(), true)) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidPhoneLength"));
			return;
		}
		// ----------Adress validation------
		if (!ui.isValidLength(addressText.getText(), true)) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidAddressLength"));
			return;
		}
		// ----------Fl Type validation---
		if (bankTypeText.getText().length() == 0) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidBankType"));
			return;
		}
		// ----------Email validation------

		if (ui.isValidName(emailText.getText()) && !ui.isValidEmailAddress(emailText.getText())) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidEmail"));
			return;
		}
		if (emailText.getText().length() > 40) {
			ui.showMessageBox(null, ui.getString("fina2.title"), ui.getString("fina2.invalidLengthEmail"));
			return;
		}
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/bank/Bank");
			BankHome home = (BankHome) PortableRemoteObject.narrow(ref, BankHome.class);

			Object refUser = jndi.lookup("fina2/security/UserSession");
			UserSessionHome homeUser = (UserSessionHome) PortableRemoteObject.narrow(refUser, UserSessionHome.class);

			UserSession sessionUser = homeUser.create();

			if (node == null) {
				bank = home.create();
				sessionUser.setUserBank((fina2.security.UserPK) ((fina2.security.User) main.getUserHandle().getEJBObject()).getPrimaryKey(), (BankPK) bank.getPrimaryKey());
			}

			UserTransaction trans = main.getUserTransaction(jndi);
			trans.begin();
			try {

				if (bankGroupPK != null) {

					Collection bankGroups = bank.getBankGroupPKs();
					for (Iterator iter = bankGroupDialog.getDefaultBankGroups().iterator(); iter.hasNext();) {
						TableRow tableRow = (TableRow) iter.next();
						bankGroups.remove(tableRow.getPrimaryKey());
					}
					bankGroups.add(bankGroupPK);
					bank.setBankGroupPKs(bankGroups);
				}
				bank.setCode(codeText.getText());
				bank.setShortName(main.getLanguageHandle(), snameText.getText());
				bank.setSwiftCode(swiftText.getText());
				bank.setName(main.getLanguageHandle(), nameText.getText());
				bank.setAddress(main.getLanguageHandle(), addressText.getText());
				bank.setPhone(phoneText.getText());
				bank.setFax(faxText.getText());
				bank.setEmail(emailText.getText());
				bank.setTelex(telexText.getText());

				if (bankRegionDialog != null) {
					Node regionSelNode = bankRegionDialog.getSelectionNode();
					RegionStructureNodePK regionSelNodepk = null;
					if (regionSelNode != null) {
						regionSelNodepk = (RegionStructureNodePK) regionSelNode.getPrimaryKey();
						if (regionSelNodepk != null) {
							bank.setRegionId(regionSelNodepk.getId());

							// Set Nulls
							bankRegionDialog.setSelectionNode(null);
							bank.setTelex("");
						}
					}
				}
				if (bankTypePK != null)
					bank.setTypePK(bankTypePK);

				if (bankGroupPK == null) {
					trans.rollback();
					if (node == null) {
						bank.remove();
						bank = null;
					}
				}

				trans.commit();
			} catch (Exception e) {
				trans.rollback();
				if (node == null) {
					bank.remove();
					bank = null;
				}
				Main.generalErrorHandler(e);
				return;
			}
			if (node == null) {
				node = new Node(bank.getPrimaryKey(), "[" + bank.getCode() + "] " + nameText.getText(), new Integer(1));
			} else {
				node.setLabel("[" + bank.getCode() + "] " + nameText.getText());
			}
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		} finally {
			if (banksFrame != null) {
				cityText.setText("");
				regionText.setText("");
				banksFrame.refreshButtonActionPerformed(null);
			}
		}
		dispose();
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_cancelButtonActionPerformed
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
	private javax.swing.JLabel bankGroupgLabel;
	private javax.swing.JTextField bankGroupText;
	private javax.swing.JLabel codeLabel;
	private javax.swing.JTextField codeText;
	private javax.swing.JLabel swiftLabel;
	private javax.swing.JTextField swiftText;
	private javax.swing.JLabel snameLabel;
	private javax.swing.JTextField snameText;
	private javax.swing.JLabel nameLabel;
	private javax.swing.JTextField nameText;
	private javax.swing.JLabel cityLabel;
	private javax.swing.JTextField cityText;
	private javax.swing.JLabel regionLabel;
	private javax.swing.JTextField regionText;
	private javax.swing.JLabel addressLabel;
	private javax.swing.JTextField addressText;
	private javax.swing.JLabel phoneLabel;
	private javax.swing.JTextField phoneText;
	private javax.swing.JLabel faxLabel;
	private javax.swing.JTextField faxText;
	private javax.swing.JLabel emailLabel;
	private javax.swing.JTextField emailText;
	private javax.swing.JLabel telexLabel;
	private javax.swing.JTextField telexText;
	private javax.swing.JLabel bankTypeLabel;
	private javax.swing.JTextField bankTypeText;
	private javax.swing.JButton selBankGroupButton;
	private javax.swing.JButton selBankTypeButton;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JButton helpButton;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JButton okButton;
	private javax.swing.JButton cancelButton;
	private javax.swing.JButton regionCityButton;
	// End of variables declaration//GEN-END:variables
}
