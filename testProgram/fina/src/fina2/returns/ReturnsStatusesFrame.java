package fina2.returns;

import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import fina2.BaseFrame;
import fina2.FinaTypeException;
import fina2.Main;
import fina2.FinaTypeException.Type;
import fina2.bank.BankSession;
import fina2.bank.BankSessionHome;
import fina2.period.PeriodPK;
import fina2.period.SelectPeriodDialog;
import fina2.ui.ProcessDialog;
import fina2.ui.UIManager;
import fina2.ui.UIManager.IndeterminateLoading;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;

public class ReturnsStatusesFrame extends BaseFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private boolean canAmend = false;

	private Vector banksRows = new Vector();
	private Vector banktypesRows = new Vector();
	private Vector bankgroupsRows = new Vector();
	private Vector returnsRows = new Vector();
	private Vector returntypesRows = new Vector();
	private Vector periodsRows = new Vector();
	private IndeterminateLoading loading;

	public PeriodPK periodPk = null;
	public int pk;

	/** Creates new form ReturnsStatusesFrame */
	public ReturnsStatusesFrame() {
		initComponents();

		loading = ui.createIndeterminateLoading(main.getMainFrame());
		BaseFrame.ensureVisible(this);
		setResizable(true);
		setIconifiable(true);
		setClosable(true);
		setMaximizable(true);

		java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screen.width / 2 - this.getSize().width / 2, screen.height / 2 - this.getSize().height / 2);
	}

	public void show() {
		if (isVisible())
			return;
		try {

			initTable();

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/bank/BankSession");
			BankSessionHome bankHome = (BankSessionHome) PortableRemoteObject.narrow(ref, BankSessionHome.class);

			BankSession bankSession = bankHome.create();

			Vector bankCode = new Vector();
			bankCode.add("ALL");
			Vector rows = (Vector) bankSession.getBanksRows(main.getUserHandle(), main.getLanguageHandle());
			for (java.util.Iterator iter = rows.iterator(); iter.hasNext();) {
				TableRowImpl row = (TableRowImpl) iter.next();
				bankCode.add(row.getValue(1).trim());
			}
			banksRows = rows;
			banksList.setModel(new javax.swing.DefaultComboBoxModel(bankCode));

			Vector bankgroupsCode = new Vector();
			bankgroupsCode.add("ALL");
			rows = (Vector) bankSession.getBankGroupsRows(main.getUserHandle(), main.getLanguageHandle());
			for (java.util.Iterator iter = rows.iterator(); iter.hasNext();) {
				TableRowImpl row = (TableRowImpl) iter.next();
				bankgroupsCode.add(row.getValue(1).trim());
			}
			bankgroupsRows = rows;
			bankgroupsList.setModel(new javax.swing.DefaultComboBoxModel(bankgroupsCode));

			Vector banktypesCode = new Vector();
			banktypesCode.add("ALL");
			rows = (Vector) bankSession.getBankTypesRows(main.getUserHandle(), main.getLanguageHandle());
			for (java.util.Iterator iter = rows.iterator(); iter.hasNext();) {
				TableRowImpl row = (TableRowImpl) iter.next();
				banktypesCode.add(row.getValue(1).trim());
			}
			banktypesRows = rows;
			banktypesList.setModel(new javax.swing.DefaultComboBoxModel(banktypesCode));

			ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome returnHome = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);
			ReturnSession returnSession = returnHome.create();

			Vector returnsCode = new Vector();
			returnsCode.add("ALL");
			rows = (Vector) returnSession.getReturnDefinitionsRows(main.getUserHandle(), main.getLanguageHandle());
			for (java.util.Iterator iter = rows.iterator(); iter.hasNext();) {
				TableRowImpl row = (TableRowImpl) iter.next();
				returnsCode.add(row.getValue(1).trim());
			}
			returnsRows = rows;
			returnsList.setModel(new javax.swing.DefaultComboBoxModel(returnsCode));

			Vector returntypesCode = new Vector();
			returntypesCode.add("ALL");
			rows = (Vector) returnSession.getReturnTypesRows(main.getUserHandle(), main.getLanguageHandle());
			for (java.util.Iterator iter = rows.iterator(); iter.hasNext();) {
				TableRowImpl row = (TableRowImpl) iter.next();
				returntypesCode.add(row.getValue(1).trim());
			}
			returntypesRows = rows;
			returntypesList.setModel(new javax.swing.DefaultComboBoxModel(returntypesCode));

			/*
			 * Vector statusCode = new Vector();
			 * statusCode.add(ui.getString("fina2.all")); statusCode.add("No");
			 * statusCode.add(ui.getString(ReturnConstants.STATUS_CREATED_STR));
			 * statusCode.add(ui.getString(ReturnConstants.STATUS_AMENDED_STR));
			 * statusCode
			 * .add(ui.getString(ReturnConstants.STATUS_IMPORTED_STR));
			 * statusCode
			 * .add(ui.getString(ReturnConstants.STATUS_PROCESSED_STR));
			 * statusCode
			 * .add(ui.getString(ReturnConstants.STATUS_VALIDATED_STR));
			 * statusCode.add(ui.getString(ReturnConstants.STATUS_RESETED_STR));
			 * statusCode
			 * .add(ui.getString(ReturnConstants.STATUS_ACCEPTED_STR));
			 * statusCode
			 * .add(ui.getString(ReturnConstants.STATUS_REJECTED_STR));
			 * statusList.setModel( new
			 * javax.swing.DefaultComboBoxModel(statusCode) );
			 */

			/*
			 * ref = jndi.lookup("fina2/period/PeriodSession");
			 * PeriodSessionHome home =
			 * (PeriodSessionHome)PortableRemoteObject.narrow (ref,
			 * PeriodSessionHome.class); PeriodSession periodsSession =
			 * home.create();
			 * 
			 * Vector periodsCode = new Vector();
			 * //periodsCode.add(ui.getString("fina2.all")); rows =
			 * (Vector)periodsSession.getPeriodRows(main.getUserHandle(),
			 * main.getLanguageHandle(),"ALL",new Date(0L),new Date(0L));
			 * for(java.util.Iterator iter=rows.iterator(); iter.hasNext(); ) {
			 * TableRowImpl row = (TableRowImpl)iter.next();
			 * periodsCode.add(row.
			 * getValue(0).trim()+" "+row.getValue(1).trim()+
			 * " "+row.getValue(2).trim()+"-"+row.getValue(3).trim()); }
			 * periodsRows = rows; periodsList.setModel( new
			 * javax.swing.DefaultComboBoxModel(periodsCode) );
			 */

			banktypesList.setEnabled(banktypesRadioButton.isSelected());
			banktypesList.setVisible(banktypesRadioButton.isSelected());

			banksList.setEnabled(banksRadioButton.isSelected());
			banksList.setVisible(banksRadioButton.isSelected());

			bankgroupsList.setEnabled(bankgroupsRadioButton.isSelected());
			bankgroupsList.setVisible(bankgroupsRadioButton.isSelected());

			returntypesList.setEnabled(returntypesRadioButton.isSelected());
			returntypesList.setVisible(returntypesRadioButton.isSelected());
			super.show();

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

	}

	private void initTable() {
		try {
			fina2.security.User user = (fina2.security.User) main.getUserHandle().getEJBObject();
			canAmend = user.hasPermission("fina2.returns.statuses");
			if (!canAmend) {
				throw new FinaTypeException(Type.PERMISSIONS_DENIED, new String[] { "fina2.returns.statuses" });
			}
			loadVersions();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents

		buttonGroup1 = new javax.swing.ButtonGroup();
		buttonGroup2 = new javax.swing.ButtonGroup();
		jPanel1 = new javax.swing.JPanel();
		banksRadioButton = new javax.swing.JRadioButton();
		banksList = new javax.swing.JComboBox();
		bankgroupsRadioButton = new javax.swing.JRadioButton();
		bankgroupsList = new javax.swing.JComboBox();
		banktypesRadioButton = new javax.swing.JRadioButton();
		banktypesList = new javax.swing.JComboBox();
		returnsRadioButton = new javax.swing.JRadioButton();
		returnsList = new javax.swing.JComboBox();
		returntypesRadioButton = new javax.swing.JRadioButton();
		returntypesList = new javax.swing.JComboBox();
		jButton4 = new javax.swing.JButton();
		jPanel6 = new javax.swing.JPanel();
		fromText = new javax.swing.JTextField();
		toText = new javax.swing.JTextField();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();
		jPanel5 = new javax.swing.JPanel();
		versionLabel = new javax.swing.JLabel();
		versionCombo = new javax.swing.JComboBox();

		setTitle(ui.getString("fina2.returns.statuses"));
		initBaseComponents();
		jPanel1.setLayout(new java.awt.GridBagLayout());

		banksRadioButton.setFont(ui.getFont());
		banksRadioButton.setSelected(true);
		banksRadioButton.setText(ui.getString("fina2.report.banks"));
		buttonGroup1.add(banksRadioButton);
		banksRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
				banksRadioButtonStateChanged(evt);
			}
		});
		jPanel1.add(banksRadioButton, UIManager.getGridBagConstraints(0, 2, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(30, 30, 0, 0)));

		banksList.setFont(ui.getFont());
		banksList.setPreferredSize(new java.awt.Dimension(230, 26));
		jPanel1.add(banksList, UIManager.getGridBagConstraints(1, 2, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(30, 10, 0, 30)));

		bankgroupsRadioButton.setFont(ui.getFont());
		bankgroupsRadioButton.setText(ui.getString("fina2.bank.bankGroups"));
		buttonGroup1.add(bankgroupsRadioButton);
		bankgroupsRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
				bankgroupsRadioButtonStateChanged(evt);
			}
		});
		jPanel1.add(bankgroupsRadioButton, UIManager.getGridBagConstraints(0, 3, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(10, 30, 0, 0)));

		bankgroupsList.setFont(ui.getFont());
		bankgroupsList.setPreferredSize(new java.awt.Dimension(230, 26));
		jPanel1.add(bankgroupsList, UIManager.getGridBagConstraints(1, 3, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(10, 10, 0, 30)));

		banktypesRadioButton.setFont(ui.getFont());
		banktypesRadioButton.setText(ui.getString("fina2.bank.bankType"));
		buttonGroup1.add(banktypesRadioButton);
		banktypesRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
				banktypesRadioButtonStateChanged(evt);
			}
		});
		jPanel1.add(banktypesRadioButton, UIManager.getGridBagConstraints(0, 4, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(10, 30, 0, 0)));

		banktypesList.setFont(ui.getFont());
		banktypesList.setPreferredSize(new java.awt.Dimension(230, 26));
		jPanel1.add(banktypesList, UIManager.getGridBagConstraints(1, 4, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(10, 10, 0, 30)));

		returnsRadioButton.setFont(ui.getFont());
		returnsRadioButton.setSelected(true);
		returnsRadioButton.setText(ui.getString("fina2.returns.return"));
		buttonGroup2.add(returnsRadioButton);
		returnsRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
				returnsRadioButtonStateChanged(evt);
			}
		});
		jPanel1.add(returnsRadioButton, UIManager.getGridBagConstraints(0, 6, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(30, 30, 0, 0)));

		returnsList.setFont(ui.getFont());
		returnsList.setPreferredSize(new java.awt.Dimension(230, 26));
		jPanel1.add(returnsList, UIManager.getGridBagConstraints(1, 6, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(30, 10, 0, 30)));

		returntypesRadioButton.setFont(ui.getFont());
		returntypesRadioButton.setText(ui.getString("fina2.returns.returnTypesAction"));
		buttonGroup2.add(returntypesRadioButton);
		returntypesRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
				returntypesRadioButtonStateChanged(evt);
			}
		});
		jPanel1.add(returntypesRadioButton, UIManager.getGridBagConstraints(0, 7, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(10, 30, 30, 0)));

		returntypesList.setFont(ui.getFont());
		returntypesList.setPreferredSize(new java.awt.Dimension(230, 26));
		jPanel1.add(returntypesList, UIManager.getGridBagConstraints(1, 7, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(10, 10, 30, 30)));

		jButton4.setIcon(ui.getIcon("fina2.lookup"));
		jButton4.setFont(ui.getFont());
		jButton4.setText(ui.getString("fina2.period.period"));
		jButton4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton4ActionPerformed(evt);
			}
		});

		versionLabel.setFont(ui.getFont());
		versionLabel.setText(ui.getString("fina2.returns.returnVersion"));
		jPanel1.add(versionLabel, UIManager.getGridBagConstraints(0, 8, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(10, 30, 30, 0)));

		versionCombo.setFont(ui.getFont());
		versionCombo.setPreferredSize(new java.awt.Dimension(230, 26));
		jPanel1.add(versionCombo, UIManager.getGridBagConstraints(1, 8, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(10, 10, 30, 30)));
		jPanel1.add(jButton4, UIManager.getGridBagConstraints(1, 0, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(30, 10, 0, 30)));

		jPanel6.setLayout(new java.awt.GridBagLayout());

		fromText.setEditable(false);
		fromText.setColumns(10);
		fromText.setFont(ui.getFont());
		fromText.setHorizontalAlignment(javax.swing.JTextField.CENTER);
		jPanel6.add(fromText, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 0, 5)));

		toText.setEditable(false);
		toText.setColumns(10);
		toText.setFont(ui.getFont());
		toText.setHorizontalAlignment(javax.swing.JTextField.CENTER);
		jPanel6.add(toText, UIManager.getGridBagConstraints(1, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, null));
		jPanel1.add(jPanel6, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, -1, new java.awt.Insets(30, 30, 0, 0)));

		getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

		jPanel2.setLayout(new java.awt.BorderLayout());

		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "Return_Statuses");
		} else {
			helpButton.setEnabled(false);
		}
		jPanel3.add(helpButton);

		jPanel2.add(jPanel3, java.awt.BorderLayout.WEST);

		jPanel4.add(printButton);

		jPanel4.add(closeButton);

		jPanel2.add(jPanel4, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

		jPanel5.setLayout(new java.awt.BorderLayout());

		getContentPane().add(jPanel5, java.awt.BorderLayout.NORTH);

		pack();
	}

	private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
		SelectPeriodDialog selectPeriodDialog = new SelectPeriodDialog(main.getMainFrame(), true);
		selectPeriodDialog.show();
		TableRow row = selectPeriodDialog.getTableRow();
		if (row == null)
			return;
		PeriodPK periodPk = (PeriodPK) row.getPrimaryKey();
		pk = periodPk.getId();

		fromText.setText(row.getValue(2));
		toText.setText(row.getValue(3));
		printButton.setEnabled(true);

	}

	protected void printButtonActionPerformed(java.awt.event.ActionEvent evt) {

		Thread t = new Thread() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public void run() {

				Vector banks = null;
				Vector needReturns = null;
				Vector isReturns = null;
				Collection g = null;
				try {
					loading.start();
					InitialContext jndi = fina2.Main.getJndiContext();
					Object ref = jndi.lookup("fina2/returns/ReturnSession");
					ReturnSessionHome returnHome = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);
					ReturnSession returnSession = returnHome.create();

					String banksCode = ""; // =ui.getString("fina2.all");
					String bankGroupsCode = "";
					String bankTypesCode = "";
					String returnsCode = ""; // ui.getString("fina2.all");
					String returnTypesCode = "";

					if (banksRadioButton.isSelected()) {
						int banksListIndex = banksList.getSelectedIndex();
						if (banksListIndex > 0) {
							TableRowImpl row = (TableRowImpl) banksRows.get(banksListIndex - 1);
							banksCode = row.getValue(0).trim();
						} else {
							banksCode = "ALL";
						}
					}

					if (bankgroupsRadioButton.isSelected()) {
						int bankgroupsListIndex = bankgroupsList.getSelectedIndex();
						if (bankgroupsListIndex > 0) {
							TableRowImpl row = (TableRowImpl) bankgroupsRows.get(bankgroupsListIndex - 1);
							bankGroupsCode = row.getValue(0).trim();
						} else {
							bankGroupsCode = "ALL";
						}
					}

					if (banktypesRadioButton.isSelected()) {
						int banktypesListIndex = banktypesList.getSelectedIndex();
						if (banktypesListIndex > 0) {
							TableRowImpl row = (TableRowImpl) banktypesRows.get(banktypesListIndex - 1);
							bankTypesCode = row.getValue(0).trim();
						} else {
							bankTypesCode = "ALL";
						}
					}

					if (returnsRadioButton.isSelected()) {
						int returnsListIndex = returnsList.getSelectedIndex();
						if (returnsListIndex > 0) {
							TableRowImpl row = (TableRowImpl) returnsRows.get(returnsListIndex - 1);
							returnsCode = row.getValue(0).trim();
						} else {
							returnsCode = "ALL";
						}
					}

					if (returntypesRadioButton.isSelected()) {

						int returntypesListIndex = returntypesList.getSelectedIndex();
						if (returntypesListIndex > 0) {
							TableRowImpl row = (TableRowImpl) returntypesRows.get(returntypesListIndex - 1);
							returnTypesCode = row.getValue(0).trim();
						} else {
							returnTypesCode = "ALL";
						}

					}

					g = returnSession.getReturnsStatuses(main.getLanguageHandle(), banksCode, bankGroupsCode, bankTypesCode, returnsCode, returnTypesCode, versionCombo.getSelectedItem().toString(),
							pk);

					// Sort Return Statuses
					Collections.sort((List) g, new ReturnStatusesComparator());

					ReturnsStatusesView view = new ReturnsStatusesView(getTitle(), g, banksCode, bankGroupsCode, bankTypesCode, returnsCode, returnTypesCode, fromText.getText(), toText.getText());

					view.show();

					loading.stop();

				} catch (Exception e) {
					Main.generalErrorHandler(e);
				}
			}
		};
		t.start();
	}

	@SuppressWarnings({ "rawtypes" })
	private class ReturnStatusesComparator implements Comparator<Vector> {
		@Override
		public int compare(Vector o1, Vector o2) {
			String codeO1 = (String) o1.get(0);
			String codeO2 = (String) o2.get(0);
			if (codeO1.equals("Code")) {
				return -1;
			}
			if (codeO2.equals("Code")) {
				return 1;
			}
			return codeO1.compareTo(codeO2);
		}
	}

	private void loadVersions() {
		try {
			InitialContext jndi = Main.getJndiContext();
			Object ref = jndi.lookup("fina2/returns/ReturnVersionSession");
			ReturnVersionSessionHome home = (ReturnVersionSessionHome) PortableRemoteObject.narrow(ref, ReturnVersionSessionHome.class);
			ReturnVersionSession session = home.create();

			Collection versions = session.getReturnVersions(main.getLanguageHandle(), main.getUserHandle());

			Vector versionCodes = new Vector();

			for (Iterator iter = versions.iterator(); iter.hasNext();) {
				ReturnVersion rv = (ReturnVersion) iter.next();
				versionCodes.add(rv.getCode());
			}

			versionCombo.setModel(new javax.swing.DefaultComboBoxModel(versionCodes));
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private void returnsRadioButtonStateChanged(javax.swing.event.ChangeEvent evt) {// GEN-FIRST:
		// event_returnsRadioButtonStateChanged
		returnsList.setEnabled(returnsRadioButton.isSelected());
		returnsList.setVisible(returnsRadioButton.isSelected());
	}// GEN-LAST:event_returnsRadioButtonStateChanged

	private void returntypesRadioButtonStateChanged(javax.swing.event.ChangeEvent evt) {// GEN-FIRST:
		// event_returntypesRadioButtonStateChanged
		returntypesList.setEnabled(returntypesRadioButton.isSelected());
		returntypesList.setVisible(returntypesRadioButton.isSelected());
	}// GEN-LAST:event_returntypesRadioButtonStateChanged

	private void banktypesRadioButtonStateChanged(javax.swing.event.ChangeEvent evt) {// GEN-FIRST:
		// event_banktypesRadioButtonStateChanged
		banktypesList.setEnabled(banktypesRadioButton.isSelected());
		banktypesList.setVisible(banktypesRadioButton.isSelected());
	}// GEN-LAST:event_banktypesRadioButtonStateChanged

	private void bankgroupsRadioButtonStateChanged(javax.swing.event.ChangeEvent evt) {// GEN-FIRST:
		// event_bankgroupsRadioButtonStateChanged
		bankgroupsList.setEnabled(bankgroupsRadioButton.isSelected());
		bankgroupsList.setVisible(bankgroupsRadioButton.isSelected());
	}// GEN-LAST:event_bankgroupsRadioButtonStateChanged

	private void banksRadioButtonStateChanged(javax.swing.event.ChangeEvent evt) {
		banksList.setEnabled(banksRadioButton.isSelected());
		banksList.setVisible(banksRadioButton.isSelected());
	}// GEN-LAST:event_banksRadioButtonStateChanged

	protected void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();
	}

	/** Exit the Application */

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JComboBox bankgroupsList;
	private javax.swing.JRadioButton bankgroupsRadioButton;
	private javax.swing.JComboBox banksList;
	private javax.swing.JRadioButton banksRadioButton;
	private javax.swing.JComboBox banktypesList;
	private javax.swing.JRadioButton banktypesRadioButton;
	private javax.swing.ButtonGroup buttonGroup1;
	private javax.swing.ButtonGroup buttonGroup2;
	private javax.swing.JTextField fromText;
	private javax.swing.JButton jButton4;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JComboBox returnsList;
	private javax.swing.JRadioButton returnsRadioButton;
	private javax.swing.JComboBox returntypesList;
	private javax.swing.JRadioButton returntypesRadioButton;
	private javax.swing.JTextField toText;
	private javax.swing.JLabel versionLabel;
	private javax.swing.JComboBox versionCombo;
	// End of variables declaration//GEN-END:variables

}
