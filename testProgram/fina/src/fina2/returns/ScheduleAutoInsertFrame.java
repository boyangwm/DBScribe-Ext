/*
 * SceduelAutoInsertFrame.java
 *
 * Created on November 14, 2001, 3:13 AM
 */

package fina2.returns;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import fina2.BaseFrame;
import fina2.FinaTypeException;
import fina2.FinaTypeException.Type;
import fina2.Main;
import fina2.bank.BankSession;
import fina2.bank.BankSessionHome;
import fina2.calendar.FinaCalendar;
import fina2.i18n.LocaleUtil;
import fina2.period.PeriodSession;
import fina2.period.PeriodSessionHome;
import fina2.ui.UIManager;
import fina2.ui.UIManager.IndeterminateLoading;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableRowImpl;

/**
 * 
 * @author vasop
 */
public class ScheduleAutoInsertFrame extends BaseFrame implements FocusListener {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTable allBankTable;
	private EJBTable selectedBankTable;
	private EJBTable allReturnTable;
	private EJBTable selectedReturnTable;
	private EJBTable allPeriodTable;
	private EJBTable selectedPeriodTable;

	private ScheduleAutoInsertConfirmDialog confirmDialog;
	private CanceledSchedulesDialog cs;

	private boolean canAmend;

	private IndeterminateLoading loading;

	Collection<TableRowImpl> allBankCollecton;
	Collection<TableRowImpl> allReturnCollection;
	Collection<TableRowImpl> allPeriodCollection;

	FinaCalendar fcalendar;

	/** Creates new form SceduelAutoInsertFrame */
	public ScheduleAutoInsertFrame() {
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.ok", "ok.gif");
		ui.loadIcon("fina2.close", "cancel.gif");

		ui.loadIcon("fina2.forwardall", "forwardall.gif");
		ui.loadIcon("fina2.forward", "forward.gif");
		ui.loadIcon("fina2.back", "back.gif");
		ui.loadIcon("fina2.backall", "backall.gif");

		allBankTable = new EJBTable();
		selectedBankTable = new EJBTable();
		allReturnTable = new EJBTable();
		selectedReturnTable = new EJBTable();
		allPeriodTable = new EJBTable();
		selectedPeriodTable = new EJBTable();

		confirmDialog = new ScheduleAutoInsertConfirmDialog(main.getMainFrame(), true);
		cs = new CanceledSchedulesDialog(main.getMainFrame(), true);

		allBankTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		allReturnTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		allPeriodTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectedBankTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectedReturnTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectedPeriodTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		allBankTable.addSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				if (allBankTable.getSelectedRow() == -1) {
					bSelectAllButton.setEnabled(false);
					bSelectCurrentButton.setEnabled(false);
				} else {
					bSelectAllButton.setEnabled(true);
					bSelectCurrentButton.setEnabled(true);
				}
			}
		});

		allReturnTable.addSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				if (allReturnTable.getSelectedRow() == -1) {
					rSelectAllButton.setEnabled(false);
					rSelectCurrentButton.setEnabled(false);
				} else {
					rSelectAllButton.setEnabled(true);
					rSelectCurrentButton.setEnabled(true);
				}
			}
		});

		allPeriodTable.addSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				if (allPeriodTable.getSelectedRow() == -1) {
					pSelectAllButton.setEnabled(false);
					pSelectCurrentButton.setEnabled(false);
				} else {
					pSelectAllButton.setEnabled(true);
					pSelectCurrentButton.setEnabled(true);
				}
			}
		});

		selectedBankTable.addSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				if (selectedBankTable.getSelectedRow() == -1) {
					bRemoveAllButton.setEnabled(false);
					bRemoveCurrentButton.setEnabled(false);
				} else {
					bRemoveAllButton.setEnabled(true);
					bRemoveCurrentButton.setEnabled(true);
				}
			}
		});

		selectedReturnTable.addSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				if (selectedReturnTable.getSelectedRow() == -1) {
					rRemoveAllButton.setEnabled(false);
					rRemoveCurrentButton.setEnabled(false);
				} else {
					rRemoveAllButton.setEnabled(true);
					rRemoveCurrentButton.setEnabled(true);
				}
			}
		});

		selectedPeriodTable.addSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				if (selectedPeriodTable.getSelectedRow() == -1) {
					pRemoveAllButton.setEnabled(false);
					pRemoveCurrentButton.setEnabled(false);
				} else {
					pRemoveAllButton.setEnabled(true);
					pRemoveCurrentButton.setEnabled(true);
				}
			}
		});

		initComponents();

		allBankScrollPane.setViewportView(allBankTable);
		selectedBankScrollPane.setViewportView(selectedBankTable);
		allReturnScrollPane.setViewportView(allReturnTable);
		selectedReturnScrollPane.setViewportView(selectedReturnTable);
		allPeriodScrollPane.setViewportView(allPeriodTable);
		selectedPeriodScrollPane.setViewportView(selectedPeriodTable);

		BaseFrame.ensureVisible(this);
		loading = ui.createIndeterminateLoading(main.getMainFrame());
	}

	private void initTable() {
		try {
			fina2.security.User user = (fina2.security.User) main.getUserHandle().getEJBObject();
			canAmend = user.hasPermission("fina2.returns.schedule.amend");
			if (!canAmend) {
				ui.putConfigValue("fina2.returns.ScheduleAutoInsertFrame.visible", new Boolean(false));
				throw new FinaTypeException(Type.PERMISSIONS_DENIED, new String[] { "fina2.returns.schedule.amend" });
			}

			initAllBankTable();
			initAllReturnTable();
			initAllPeriodTable();

			if (allBankTable.getRowCount() > 0)
				allBankTable.setRowSelectionInterval(0, 0);
			if (allReturnTable.getRowCount() > 0)
				allReturnTable.setRowSelectionInterval(0, 0);
			if (allPeriodTable.getRowCount() > 0)
				allPeriodTable.setRowSelectionInterval(0, 0);
			if (selectedBankTable.getRowCount() > 0)
				selectedBankTable.setRowSelectionInterval(0, 0);
			if (selectedReturnTable.getRowCount() > 0)
				selectedReturnTable.setRowSelectionInterval(0, 0);
			if (selectedPeriodTable.getRowCount() > 0)
				selectedPeriodTable.setRowSelectionInterval(0, 0);
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private void initAllBankTable() throws FinaTypeException, Exception {

		Thread t = new Thread() {
			public void run() {

				// fina2.ui.ProcessDialog pdlg = ui.showProcessDialog(null,
				// "Loading...");
				try {
					InitialContext jndi = fina2.Main.getJndiContext();
					Object ref = jndi.lookup("fina2/bank/BankSession");
					BankSessionHome home = (BankSessionHome) PortableRemoteObject.narrow(ref, BankSessionHome.class);
					BankSession session = home.create();

					Vector bankColNames = new Vector();
					bankColNames.add(ui.getString("fina2.code"));
					bankColNames.add(ui.getString("fina2.bank.name"));

					allBankCollecton = session.getBanksRows(main.getUserHandle(), main.getLanguageHandle());
					allBankTable.initTable(bankColNames, allBankCollecton);
					selectedBankTable.initTable(bankColNames, new Vector());

					selectedBankTable.resizeAndRepaint();
					allBankTable.resizeAndRepaint();
				} catch (Exception ex) {
					Main.generalErrorHandler(ex);
				}
				// pdlg.dispose();
			}
		};
		t.start();
	}

	/**
	 * @author Davit Beradze
	 */
	private void doBankFilter() {
		Iterator<TableRowImpl> iter = allBankCollecton.iterator();
		Collection filteredColl = new Vector();
		Vector bankColNames = new Vector();
		bankColNames.add(ui.getString("fina2.code"));
		bankColNames.add(ui.getString("fina2.bank.name"));
		while (iter.hasNext()) {
			TableRowImpl row = iter.next();
			if (row.getValue(0).toLowerCase().contains(banksCodeTextField.getText().trim().toLowerCase()) && row.getValue(1).toLowerCase().contains(banksNameTextField.getText().trim().toLowerCase()))
				filteredColl.add(row);
		}
		allBankTable.initTable(bankColNames, filteredColl);
		allBankTable.resizeAndRepaint();

	}

	private void initAllReturnTable() throws FinaTypeException, Exception {
		InitialContext jndi = fina2.Main.getJndiContext();
		Object ref = jndi.lookup("fina2/returns/ReturnSession");
		ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);
		ReturnSession session = home.create();

		Vector returnColNames = new Vector();
		returnColNames.add(ui.getString("fina2.code"));
		returnColNames.add(ui.getString("fina2.description"));
		returnColNames.add(ui.getString("fina2.type"));

		allReturnCollection = session.getReturnDefinitionsRows(main.getUserHandle(), main.getLanguageHandle());
		allReturnTable.initTable(returnColNames, allReturnCollection);
		selectedReturnTable.initTable(returnColNames, new Vector());

		Iterator<TableRowImpl> iter = allReturnCollection.iterator();
		Vector vector = new Vector();
		vector.add(ui.getString("fina2.all"));
		while (iter.hasNext()) {
			TableRowImpl row = iter.next();
			if (!vector.contains(row.getValue(2)))
				vector.add(row.getValue(2));
		}
		returnsTypeComboBox.setModel(new DefaultComboBoxModel(vector));
		selectedReturnTable.resizeAndRepaint();
		allReturnTable.resizeAndRepaint();
	}

	/**
	 * @author Davit Beradze
	 */
	private void doReturnFilter() {
		Iterator<TableRowImpl> iter = allReturnCollection.iterator();
		Collection filteredColl = new Vector();
		Vector returnColNames = new Vector();
		returnColNames.add(ui.getString("fina2.code"));
		returnColNames.add(ui.getString("fina2.description"));
		returnColNames.add(ui.getString("fina2.type"));

		String typeComboBox = returnsTypeComboBox.getSelectedItem().toString();
		if (typeComboBox.equals(ui.getString("fina2.all")))
			typeComboBox = "";

		while (iter.hasNext()) {
			TableRowImpl row = iter.next();
			if (row.getValue(0).toLowerCase().contains(returnsCodeTextField.getText().trim().toLowerCase())
					&& row.getValue(1).toLowerCase().contains(returnsDescriptorTextField.getText().trim().toLowerCase())
					&& (row.getValue(2).toLowerCase().equals(typeComboBox.trim().toLowerCase()) || typeComboBox.equals("")))
				filteredColl.add(row);
		}
		allReturnTable.initTable(returnColNames, filteredColl);
		allReturnTable.resizeAndRepaint();

	}

	private void initAllPeriodTable() throws FinaTypeException, Exception {
		InitialContext jndi = fina2.Main.getJndiContext();
		Object ref = jndi.lookup("fina2/period/PeriodSession");
		PeriodSessionHome home = (PeriodSessionHome) PortableRemoteObject.narrow(ref, PeriodSessionHome.class);
		PeriodSession session = home.create();

		Vector periodColNames = new Vector();
		periodColNames.add(ui.getString("fina2.period.periodType"));
		periodColNames.add(ui.getString("fina2.period.periodNumber"));
		periodColNames.add(ui.getString("fina2.period.fromDate"));
		periodColNames.add(ui.getString("fina2.period.toDate"));

		allPeriodCollection = session.getPeriodRows(main.getUserHandle(), main.getLanguageHandle());
		allPeriodTable.initTable(periodColNames, allPeriodCollection);
		selectedPeriodTable.initTable(periodColNames, new Vector());

		Iterator<TableRowImpl> iter = allPeriodCollection.iterator();
		Vector vector = new Vector();
		vector.add(ui.getString("fina2.all"));
		while (iter.hasNext()) {
			TableRowImpl row = iter.next();
			if (!vector.contains(row.getValue(0)))
				vector.add(row.getValue(0));
		}
		periodsTypeComboBox.setModel(new DefaultComboBoxModel(vector));

		selectedPeriodTable.resizeAndRepaint();
		allPeriodTable.resizeAndRepaint();
	}

	/**
	 * @author Davit Beradze
	 */
	private void doPeriodFilter() {
		Iterator<TableRowImpl> iter = allPeriodCollection.iterator();
		Collection filteredColl = new Vector();
		Vector periodColNames = new Vector();
		periodColNames.add(ui.getString("fina2.period.periodType"));
		periodColNames.add(ui.getString("fina2.period.periodNumber"));
		periodColNames.add(ui.getString("fina2.period.fromDate"));
		periodColNames.add(ui.getString("fina2.period.toDate"));

		String typeComboBox = periodsTypeComboBox.getSelectedItem().toString();
		if (typeComboBox.equals(ui.getString("fina2.all")))
			typeComboBox = "";

		while (iter.hasNext()) {
			TableRowImpl row = iter.next();
			if ((row.getValue(0).toLowerCase().equals(typeComboBox.trim().toLowerCase()) || typeComboBox.equals(""))
					&& row.getValue(1).toLowerCase().contains(periodsNumberTextField.getText().trim().toLowerCase())
					&& row.getValue(2).toLowerCase().contains(periodsStartDataTextField.getText().trim().toLowerCase())
					&& row.getValue(3).toLowerCase().contains(periodsEndDataTextField.getText().trim().toLowerCase()))
				filteredColl.add(row);
		}
		allPeriodTable.initTable(periodColNames, filteredColl);
		allPeriodTable.resizeAndRepaint();

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
		allBankPanel = new javax.swing.JPanel();
		allBankScrollPane = new javax.swing.JScrollPane();
		jPanel5 = new javax.swing.JPanel();
		bSelectAllButton = new javax.swing.JButton();
		bSelectCurrentButton = new javax.swing.JButton();
		bRemoveCurrentButton = new javax.swing.JButton();
		bRemoveAllButton = new javax.swing.JButton();
		selectedBankPanel = new javax.swing.JPanel();
		selectedBankScrollPane = new javax.swing.JScrollPane();
		allReturnPanel = new javax.swing.JPanel();
		allReturnScrollPane = new javax.swing.JScrollPane();
		jPanel6 = new javax.swing.JPanel();
		rSelectAllButton = new javax.swing.JButton();
		rSelectCurrentButton = new javax.swing.JButton();
		rRemoveCurrentButton = new javax.swing.JButton();
		rRemoveAllButton = new javax.swing.JButton();
		selectedReturnPanel = new javax.swing.JPanel();
		selectedReturnScrollPane = new javax.swing.JScrollPane();
		allPeriodPanel = new javax.swing.JPanel();
		allPeriodScrollPane = new javax.swing.JScrollPane();
		jPanel7 = new javax.swing.JPanel();
		pSelectAllButton = new javax.swing.JButton();
		pSelectCurrentButton = new javax.swing.JButton();
		pRemoveCurrentButton = new javax.swing.JButton();
		pRemoveAllButton = new javax.swing.JButton();
		selectedPeriodPanel = new javax.swing.JPanel();
		selectedPeriodScrollPane = new javax.swing.JScrollPane();
		jPanel2 = new javax.swing.JPanel();
		jPanel9 = new javax.swing.JPanel();
		jPanel10 = new javax.swing.JPanel();
		jLabel4 = new javax.swing.JLabel();
		jPanel61 = new javax.swing.JPanel();
		doaText = new javax.swing.JTextField();
		jLabel5 = new javax.swing.JLabel();
		jPanel8 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		banksFilterPanel = new javax.swing.JPanel();
		banksCodeTextField = new javax.swing.JTextField();
		banksNameTextField = new javax.swing.JTextField();
		returnsFilterPanel = new javax.swing.JPanel();
		returnsCodeTextField = new javax.swing.JTextField();
		returnsDescriptorTextField = new javax.swing.JTextField();
		returnsTypeComboBox = new javax.swing.JComboBox();
		periodsFilterPanel = new javax.swing.JPanel();
		periodsTypeComboBox = new javax.swing.JComboBox();
		periodsNumberTextField = new javax.swing.JTextField();
		periodsStartDataTextField = new javax.swing.JTextField();
		periodsEndDataTextField = new javax.swing.JTextField();

		setTitle(ui.getString("fina2.returns.autoScheduel"));
		initBaseComponents();

		jPanel1.setLayout(new java.awt.GridLayout(3, 2));

		allBankPanel.setLayout(new java.awt.BorderLayout());

		allBankPanel.setBorder(new javax.swing.border.TitledBorder(null, ui.getString("fina2.returns.autoScheduleAllBanks"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION, ui.getFont()));

		banksFilterPanel.setLayout(new GridBagLayout());
		((GridBagLayout) banksFilterPanel.getLayout()).columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		((GridBagLayout) banksFilterPanel.getLayout()).rowHeights = new int[] { 0, 0, 0, 0 };
		((GridBagLayout) banksFilterPanel.getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4 };
		((GridBagLayout) banksFilterPanel.getLayout()).rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0E-4 };
		banksFilterPanel.add(banksCodeTextField);
		banksFilterPanel.add(banksNameTextField);

		DocumentListener documentListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent documentEvent) {
				// ignore
			}

			public void insertUpdate(DocumentEvent documentEvent) {
				doBankFilter();
			}

			public void removeUpdate(DocumentEvent documentEvent) {
				doBankFilter();
			}

		};
		banksCodeTextField.getDocument().addDocumentListener(documentListener);
		banksNameTextField.getDocument().addDocumentListener(documentListener);

		allBankPanel.add(banksFilterPanel, java.awt.BorderLayout.NORTH);
		allBankPanel.add(allBankScrollPane, java.awt.BorderLayout.CENTER);

		jPanel5.setLayout(new java.awt.GridBagLayout());

		bSelectAllButton.setIcon(ui.getIcon("fina2.forwardall"));
		bSelectAllButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				bSelectAllButtonActionPerformed(evt);
			}
		});
		jPanel5.add(bSelectAllButton, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, -1, -1, new java.awt.Insets(5, 0, 5, 0)));

		bSelectCurrentButton.setIcon(ui.getIcon("fina2.forward"));
		bSelectCurrentButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				bSelectCurrentButtonActionPerformed(evt);
			}
		});
		jPanel5.add(bSelectCurrentButton, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, -1, new java.awt.Insets(5, 0, 5, 0)));

		bRemoveCurrentButton.setIcon(ui.getIcon("fina2.back"));
		bRemoveCurrentButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				bRemoveCurrentButtonActionPerformed(evt);
			}
		});
		jPanel5.add(bRemoveCurrentButton, UIManager.getGridBagConstraints(0, 2, -1, -1, -1, -1, -1, -1, new java.awt.Insets(5, 0, 5, 0)));

		bRemoveAllButton.setIcon(ui.getIcon("fina2.backall"));
		bRemoveAllButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				bRemoveAllButtonActionPerformed(evt);
			}
		});
		jPanel5.add(bRemoveAllButton, UIManager.getGridBagConstraints(0, 3, -1, -1, -1, -1, -1, -1, new java.awt.Insets(5, 0, 5, 0)));

		allBankPanel.add(jPanel5, java.awt.BorderLayout.EAST);

		jPanel1.add(allBankPanel);

		selectedBankPanel.setLayout(new java.awt.BorderLayout());

		selectedBankPanel.setBorder(new javax.swing.border.TitledBorder(null, ui.getString("fina2.returns.autoScheduleSelectedBanks"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION, ui.getFont()));
		selectedBankPanel.add(selectedBankScrollPane, java.awt.BorderLayout.CENTER);

		jPanel1.add(selectedBankPanel);

		allReturnPanel.setLayout(new java.awt.BorderLayout());

		allReturnPanel.setBorder(new javax.swing.border.TitledBorder(null, ui.getString("fina2.returns.autoScheduleAllReturns"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION, ui.getFont()));

		returnsFilterPanel.setLayout(new GridBagLayout());
		((GridBagLayout) returnsFilterPanel.getLayout()).columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		((GridBagLayout) returnsFilterPanel.getLayout()).rowHeights = new int[] { 0, 0, 0, 0 };
		((GridBagLayout) returnsFilterPanel.getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4 };
		((GridBagLayout) returnsFilterPanel.getLayout()).rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0E-4 };
		returnsFilterPanel.add(returnsCodeTextField);
		returnsFilterPanel.add(returnsDescriptorTextField);
		returnsFilterPanel.add(returnsTypeComboBox);

		returnsTypeComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doReturnFilter();
			}
		});

		DocumentListener returnDocumentListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent documentEvent) {
				// ignore
			}

			public void insertUpdate(DocumentEvent documentEvent) {
				doReturnFilter();
			}

			public void removeUpdate(DocumentEvent documentEvent) {
				doReturnFilter();
			}

		};
		returnsCodeTextField.getDocument().addDocumentListener(returnDocumentListener);
		returnsDescriptorTextField.getDocument().addDocumentListener(returnDocumentListener);

		allReturnPanel.add(returnsFilterPanel, java.awt.BorderLayout.NORTH);

		allReturnPanel.add(allReturnScrollPane, java.awt.BorderLayout.CENTER);
		jPanel6.setLayout(new java.awt.GridBagLayout());

		rSelectAllButton.setIcon(ui.getIcon("fina2.forwardall"));
		rSelectAllButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rSelectAllButtonActionPerformed(evt);
			}
		});
		jPanel6.add(rSelectAllButton, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, -1, -1, new java.awt.Insets(5, 0, 5, 0)));

		rSelectCurrentButton.setIcon(ui.getIcon("fina2.forward"));
		rSelectCurrentButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rSelectCurrentButtonActionPerformed(evt);
			}
		});
		jPanel6.add(rSelectCurrentButton, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, -1, new java.awt.Insets(5, 0, 5, 0)));

		rRemoveCurrentButton.setIcon(ui.getIcon("fina2.back"));
		rRemoveCurrentButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rRemoveCurrentButtonActionPerformed(evt);
			}
		});
		jPanel6.add(rRemoveCurrentButton, UIManager.getGridBagConstraints(0, 2, -1, -1, -1, -1, -1, -1, new java.awt.Insets(5, 0, 5, 0)));

		rRemoveAllButton.setIcon(ui.getIcon("fina2.backall"));
		rRemoveAllButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				rRemoveAllButtonActionPerformed(evt);
			}
		});
		jPanel6.add(rRemoveAllButton, UIManager.getGridBagConstraints(0, 3, -1, -1, -1, -1, -1, -1, new java.awt.Insets(5, 0, 5, 0)));

		allReturnPanel.add(jPanel6, java.awt.BorderLayout.EAST);

		jPanel1.add(allReturnPanel);

		selectedReturnPanel.setLayout(new java.awt.BorderLayout());

		selectedReturnPanel.setBorder(new javax.swing.border.TitledBorder(null, ui.getString("fina2.returns.autoScheduleSelectedReturns"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION, ui.getFont()));
		selectedReturnPanel.add(selectedReturnScrollPane, java.awt.BorderLayout.CENTER);

		jPanel1.add(selectedReturnPanel);

		allPeriodPanel.setLayout(new java.awt.BorderLayout());

		allPeriodPanel.setBorder(new javax.swing.border.TitledBorder(null, ui.getString("fina2.returns.autoScheduleAllPeriods"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION, ui.getFont()));
		periodsFilterPanel.setLayout(new GridBagLayout());
		((GridBagLayout) periodsFilterPanel.getLayout()).columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		((GridBagLayout) periodsFilterPanel.getLayout()).rowHeights = new int[] { 0, 0, 0, 0 };
		((GridBagLayout) periodsFilterPanel.getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4 };
		((GridBagLayout) periodsFilterPanel.getLayout()).rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0E-4 };
		periodsFilterPanel.add(periodsTypeComboBox);
		periodsFilterPanel.add(periodsNumberTextField);
		periodsFilterPanel.add(periodsStartDataTextField);
		periodsFilterPanel.add(periodsEndDataTextField);

		periodsStartDataTextField.addFocusListener(this);
		periodsEndDataTextField.addFocusListener(this);

		periodsTypeComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doPeriodFilter();
			}
		});

		DocumentListener periodDocumentListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent documentEvent) {
				// ignore
			}

			public void insertUpdate(DocumentEvent documentEvent) {
				doPeriodFilter();
			}

			public void removeUpdate(DocumentEvent documentEvent) {
				doPeriodFilter();
			}

		};
		periodsNumberTextField.getDocument().addDocumentListener(periodDocumentListener);
		periodsStartDataTextField.getDocument().addDocumentListener(periodDocumentListener);
		periodsEndDataTextField.getDocument().addDocumentListener(periodDocumentListener);

		allPeriodPanel.add(periodsFilterPanel, java.awt.BorderLayout.NORTH);
		allPeriodPanel.add(allPeriodScrollPane, java.awt.BorderLayout.CENTER);

		jPanel7.setLayout(new java.awt.GridBagLayout());

		pSelectAllButton.setIcon(ui.getIcon("fina2.forwardall"));
		pSelectAllButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				pSelectAllButtonActionPerformed(evt);
			}
		});
		jPanel7.add(pSelectAllButton, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, -1, -1, new java.awt.Insets(5, 0, 5, 0)));

		pSelectCurrentButton.setIcon(ui.getIcon("fina2.forward"));
		pSelectCurrentButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				pSelectCurrentButtonActionPerformed(evt);
			}
		});
		jPanel7.add(pSelectCurrentButton, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, -1, new java.awt.Insets(5, 0, 5, 0)));

		pRemoveCurrentButton.setIcon(ui.getIcon("fina2.back"));
		pRemoveCurrentButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				pRemoveCurrentButtonActionPerformed(evt);
			}
		});
		jPanel7.add(pRemoveCurrentButton, UIManager.getGridBagConstraints(0, 2, -1, -1, -1, -1, -1, -1, new java.awt.Insets(5, 0, 5, 0)));

		pRemoveAllButton.setIcon(ui.getIcon("fina2.backall"));
		pRemoveAllButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				pRemoveAllButtonActionPerformed(evt);
			}
		});
		jPanel7.add(pRemoveAllButton, UIManager.getGridBagConstraints(0, 3, -1, -1, -1, -1, -1, -1, new java.awt.Insets(5, 0, 5, 0)));

		allPeriodPanel.add(jPanel7, java.awt.BorderLayout.EAST);

		jPanel1.add(allPeriodPanel);

		selectedPeriodPanel.setLayout(new java.awt.BorderLayout());

		selectedPeriodPanel.setBorder(new javax.swing.border.TitledBorder(null, ui.getString("fina2.returns.autoScheduleSelectedPeriods"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION, ui.getFont()));
		selectedPeriodPanel.add(selectedPeriodScrollPane, java.awt.BorderLayout.CENTER);

		jPanel1.add(selectedPeriodPanel);

		getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

		jPanel2.setLayout(new java.awt.BorderLayout());

		jPanel9.setLayout(new java.awt.BorderLayout());

		jLabel4.setText(UIManager.formatedHtmlString(ui.getString("fina2.returns.acceptableDelay")));
		jLabel4.setFont(ui.getFont());
		jPanel10.add(jLabel4);

		jPanel61.setLayout(new java.awt.BorderLayout());

		doaText.setColumns(5);
		doaText.setFont(ui.getFont());
		jPanel61.add(doaText, java.awt.BorderLayout.WEST);

		jLabel5.setText(ui.getString("fina2.returns.days"));
		jLabel5.setFont(ui.getFont());
		jPanel61.add(jLabel5, java.awt.BorderLayout.CENTER);

		jPanel10.add(jPanel61);

		jPanel9.add(jPanel10, java.awt.BorderLayout.WEST);

		jPanel2.add(jPanel9, java.awt.BorderLayout.NORTH);

		jPanel8.setLayout(new java.awt.BorderLayout());

		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "Schedule_Auto_Insert");
		} else {
			helpButton.setEnabled(false);
		}
		jPanel3.add(helpButton);

		jPanel8.add(jPanel3, java.awt.BorderLayout.WEST);

		okButton.setIcon(ui.getIcon("fina2.ok"));
		okButton.setFont(ui.getFont());
		okButton.setText(ui.getString("fina2.preview"));
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				Thread t = new Thread() {
					public void run() {
						loading.start();
						okButtonActionPerformed(evt);
						loading.stop();
					}
				};
				t.start();
			}
		});

		jPanel4.add(okButton);

		jPanel4.add(closeButton);

		jPanel8.add(jPanel4, java.awt.BorderLayout.EAST);

		jPanel2.add(jPanel8, java.awt.BorderLayout.CENTER);

		getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

		this.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent e) {
				/*
				 * banksCodeTextField.setPreferredSize(new
				 * Dimension(e.getComponent().getWidth() / 4 - 44, 17));
				 * banksNameTextField.setPreferredSize(new
				 * Dimension(e.getComponent().getWidth() / 4 - 44, 17));
				 * 
				 * returnsCodeTextField.setPreferredSize(new
				 * Dimension(e.getComponent().getWidth() / 6 - 29, 17));
				 * returnsDescriptorTextField.setPreferredSize(new
				 * Dimension(e.getComponent().getWidth() / 6 - 29, 17));
				 * returnsTypeComboBox.setPreferredSize(new
				 * Dimension(e.getComponent().getWidth() / 6 - 29, 17));
				 * 
				 * periodsTypeComboBox.setPreferredSize(new
				 * Dimension(e.getComponent().getWidth() / 8 - 21, 17));
				 * periodsNumberTextField.setPreferredSize(new
				 * Dimension(e.getComponent().getWidth() / 8 - 21, 17));
				 * periodsStartDataTextField.setPreferredSize(new
				 * Dimension(e.getComponent().getWidth() / 8 - 21, 17));
				 * periodsEndDataTextField.setPreferredSize(new
				 * Dimension(e.getComponent().getWidth() / 8 - 21, 17));
				 */

				banksCodeTextField.setPreferredSize(new Dimension(e.getComponent().getWidth() / 4 - 44, 18));
				banksNameTextField.setPreferredSize(new Dimension(e.getComponent().getWidth() / 4 - 25, 18));

				returnsCodeTextField.setPreferredSize(new Dimension(e.getComponent().getWidth() / 6 - 29, 18));
				returnsDescriptorTextField.setPreferredSize(new Dimension(e.getComponent().getWidth() / 6 - 29, 18));
				returnsTypeComboBox.setPreferredSize(new Dimension(e.getComponent().getWidth() / 6 - 10, 18));

				periodsTypeComboBox.setPreferredSize(new Dimension(e.getComponent().getWidth() / 8 - 21, 18));
				periodsNumberTextField.setPreferredSize(new Dimension(e.getComponent().getWidth() / 8 - 21, 18));
				periodsStartDataTextField.setPreferredSize(new Dimension(e.getComponent().getWidth() / 8 - 21, 18));
				periodsEndDataTextField.setPreferredSize(new Dimension(e.getComponent().getWidth() / 8 - 2, 18));
			}

		});

	}// GEN-END:initComponents

	// TODO "synchronized" - es aucilebelia!
	private synchronized void okButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-

		// event_okButtonActionPerformed
		int doa;

		if (selectedBankTable.getRowCount() == 0) {
			ui.showMessageBox(null, ui.getString("fina2.returns.autoScheduleMissingBanks"));
			return;
		}
		if (selectedReturnTable.getRowCount() == 0) {
			ui.showMessageBox(null, ui.getString("fina2.returns.autoScheduleMissingReturns"));
			return;
		}
		if (selectedPeriodTable.getRowCount() == 0) {
			ui.showMessageBox(null, ui.getString("fina2.returns.autoScheduleMissingPeriods"));
			return;
		}
		if (doaText.getText().trim().length() == 0) {
			ui.showMessageBox(null, ui.getString("fina2.returns.missingAcceptableDelay"));
			return;
		}
		try {
			doa = Integer.parseInt(doaText.getText());
			if (doa >= 0) {
				confirmDialog.show(selectedBankTable.getRows(), selectedReturnTable.getRows(), selectedPeriodTable.getRows(), doa);
				Collection canceledRows = confirmDialog.getCanceledRows();
				if (confirmDialog.isInsert() && canceledRows != null) {
					cs.show(canceledRows, confirmDialog.getRowCount());
				}
			} else {
				ui.showMessageBox(null, ui.getString("fina2.returns.acceptableDeleyValue"));
				doaText.setText("");
			}
		} catch (NumberFormatException e) {
			Main.errorHandler(null, Main.getString("fina2.title"), Main.getString("fina2.returns.integerConvertException"));
			doaText.setText("");
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}// GEN-LAST:event_okButtonActionPerformed

	protected void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		formComponentHidden(null);
		dispose();
	}

	private void pRemoveAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			initAllPeriodTable();
			doPeriodFilter();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private void pRemoveCurrentButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:
		// event_pRemoveCurrentButtonActionPerformed
		allPeriodTable.addRow(selectedPeriodTable.getSelectedTableRow());
		selectedPeriodTable.removeRow(selectedPeriodTable.getSelectedRow());
		doPeriodFilter();
	}// GEN-LAST:event_pRemoveCurrentButtonActionPerformed

	private void pSelectCurrentButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:
		// event_pSelectCurrentButtonActionPerformed
		int index = allPeriodTable.getSelectedRow();
		selectedPeriodTable.addRow(allPeriodTable.getSelectedTableRow());
		allPeriodTable.removeRow(allPeriodTable.getSelectedRow());

		if (index != 0)
			allPeriodTable.getSelectionModel().setSelectionInterval(index - 1, index - 1);
	}// GEN-LAST:event_pSelectCurrentButtonActionPerformed

	private void pSelectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			String typeComboBox = periodsTypeComboBox.getSelectedItem().toString();
			if (typeComboBox.equals(ui.getString("fina2.all")))
				typeComboBox = "";

			Iterator<TableRowImpl> iter = allPeriodCollection.iterator();
			while (iter.hasNext()) {
				TableRowImpl row = iter.next();
				if ((row.getValue(0).toLowerCase().equals(typeComboBox.trim().toLowerCase()) || typeComboBox.equals(""))
						&& row.getValue(1).toLowerCase().contains(periodsNumberTextField.getText().trim().toLowerCase())
						&& row.getValue(2).toLowerCase().contains(periodsStartDataTextField.getText().trim().toLowerCase())
						&& row.getValue(3).toLowerCase().contains(periodsEndDataTextField.getText().trim().toLowerCase())) {
					selectedPeriodTable.addRow(row);
					iter.remove();
				}
			}
			doPeriodFilter();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private void rRemoveAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			initAllReturnTable();
			selectedReturnTable.resizeAndRepaint();
			allReturnTable.resizeAndRepaint();
			doReturnFilter();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private void rRemoveCurrentButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:
		// event_rRemoveCurrentButtonActionPerformed
		allReturnTable.addRow(selectedReturnTable.getSelectedTableRow());
		selectedReturnTable.removeRow(selectedReturnTable.getSelectedRow());
		doReturnFilter();
	}// GEN-LAST:event_rRemoveCurrentButtonActionPerformed

	private void rSelectCurrentButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:
		// event_rSelectCurrentButtonActionPerformed
		int index = allReturnTable.getSelectedRow();
		selectedReturnTable.addRow(allReturnTable.getSelectedTableRow());
		allReturnTable.removeRow(allReturnTable.getSelectedRow());
		if (index != 0)
			allReturnTable.getSelectionModel().setSelectionInterval(index - 1, index - 1);
	}// GEN-LAST:event_rSelectCurrentButtonActionPerformed

	private void rSelectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			String typeComboBox = returnsTypeComboBox.getSelectedItem().toString();
			if (typeComboBox.equals(ui.getString("fina2.all")))
				typeComboBox = "";

			Iterator<TableRowImpl> iter = allReturnCollection.iterator();
			while (iter.hasNext()) {
				TableRowImpl row = iter.next();
				if (row.getValue(0).toLowerCase().contains(returnsCodeTextField.getText().trim().toLowerCase())
						&& row.getValue(1).toLowerCase().contains(returnsDescriptorTextField.getText().trim().toLowerCase())
						&& (row.getValue(2).toLowerCase().equals(typeComboBox.trim().toLowerCase()) || typeComboBox.equals(""))) {
					selectedReturnTable.addRow(row);
					iter.remove();
				}
			}
			doReturnFilter();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private void bRemoveAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			initAllBankTable();
			doBankFilter();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private void bRemoveCurrentButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:
		// event_bRemoveCurrentButtonActionPerformed
		allBankTable.addRow(selectedBankTable.getSelectedTableRow());
		selectedBankTable.removeRow(selectedBankTable.getSelectedRow());
		doBankFilter();
	}// GEN-LAST:event_bRemoveCurrentButtonActionPerformed

	private void bSelectCurrentButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:
		// event_bSelectCurrentButtonActionPerformed
		int index = allBankTable.getSelectedRow();

		selectedBankTable.addRow(allBankTable.getSelectedTableRow());
		allBankTable.removeRow(allBankTable.getSelectedRow());

		if (index != 0)
			allBankTable.getSelectionModel().setSelectionInterval(index - 1, index - 1);
	}// GEN-LAST:event_bSelectCurrentButtonActionPerformed

	private void bSelectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			Iterator<TableRowImpl> iter = allBankCollecton.iterator();
			while (iter.hasNext()) {
				TableRowImpl row = iter.next();
				if (row.getValue(0).toLowerCase().contains(banksCodeTextField.getText().trim().toLowerCase())
						&& row.getValue(1).toLowerCase().contains(banksNameTextField.getText().trim().toLowerCase())) {
					selectedBankTable.addRow(row);
					iter.remove();
				}
			}
			doBankFilter();

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (fcalendar != null && fcalendar.ACTIVE) {
			fcalendar.exit();
			return;
		}
		fcalendar = new FinaCalendar((JTextField) e.getComponent());

		// previously selected date
		java.util.Date selectedDate = fcalendar.parseDate(((JTextField) (e.getComponent())).getText());
		fcalendar.setSelectedDate(selectedDate);
		if (!fcalendar.ACTIVE) {
			fcalendar.start(e.getComponent());
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		// ignore
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel allBankPanel;
	private javax.swing.JScrollPane allBankScrollPane;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JButton bSelectAllButton;
	private javax.swing.JButton bSelectCurrentButton;
	private javax.swing.JButton bRemoveCurrentButton;
	private javax.swing.JButton bRemoveAllButton;
	private javax.swing.JPanel selectedBankPanel;
	private javax.swing.JScrollPane selectedBankScrollPane;
	private javax.swing.JPanel allReturnPanel;
	private javax.swing.JScrollPane allReturnScrollPane;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JButton rSelectAllButton;
	private javax.swing.JButton rSelectCurrentButton;
	private javax.swing.JButton rRemoveCurrentButton;
	private javax.swing.JButton rRemoveAllButton;
	private javax.swing.JPanel selectedReturnPanel;
	private javax.swing.JScrollPane selectedReturnScrollPane;
	private javax.swing.JPanel allPeriodPanel;
	private javax.swing.JScrollPane allPeriodScrollPane;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JButton pSelectAllButton;
	private javax.swing.JButton pSelectCurrentButton;
	private javax.swing.JButton pRemoveCurrentButton;
	private javax.swing.JButton pRemoveAllButton;
	private javax.swing.JPanel selectedPeriodPanel;
	private javax.swing.JScrollPane selectedPeriodScrollPane;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel9;
	private javax.swing.JPanel jPanel10;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JPanel jPanel61;
	private javax.swing.JTextField doaText;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JButton okButton;
	private javax.swing.JPanel banksFilterPanel;
	private javax.swing.JTextField banksCodeTextField;
	private javax.swing.JTextField banksNameTextField;
	private javax.swing.JPanel returnsFilterPanel;
	private javax.swing.JTextField returnsCodeTextField;
	private javax.swing.JTextField returnsDescriptorTextField;
	private javax.swing.JComboBox returnsTypeComboBox;
	private javax.swing.JPanel periodsFilterPanel;
	private javax.swing.JComboBox periodsTypeComboBox;
	private javax.swing.JTextField periodsNumberTextField;
	private javax.swing.JTextField periodsStartDataTextField;
	private javax.swing.JTextField periodsEndDataTextField;
	// End of variables declaration//GEN-END:variables

}
