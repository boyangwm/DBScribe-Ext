package fina2.returns;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import fina2.Main;
import fina2.bank.BankSession;
import fina2.bank.BankSessionHome;
import fina2.calendar.FinaCalendar;
import fina2.i18n.LocaleUtil;
import fina2.ui.UIManager.IndeterminateLoading;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;

public class SelectScheduleDialog extends javax.swing.JDialog implements FocusListener {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTable table;

	private Collection<TableRow> selectedTableRows;

	private Collection tableCollection;

	private static boolean initial = false;

	private FinaCalendar fcalendar;

	private IndeterminateLoading loading;

	public SelectScheduleDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		ui.loadIcon("fina2.cancel", "cancel.gif");
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.ok", "ok.gif");

		table = new EJBTable();

		initComponents();

		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		scrollPane.setViewportView(table);

		loading = ui.createIndeterminateLoading(main.getMainFrame());

		loadConf();

	}

	private void loadConf() {

		int w = 550, h = 400;

		try {
			w = ((Integer) ui.getConfigValue("fina2.returns.selectScheduleDialog.width")).intValue();
			h = ((Integer) ui.getConfigValue("fina2.returns.selectScheduleDialog.height")).intValue();
		} catch (Exception e) {
		}
		setSize(w, h);
	}

	public Collection<TableRow> getSeleTableRows() {
		return selectedTableRows;
	}

	public void show() {
		setLocationRelativeTo(getParent());

		Thread t = new Thread() {
			public void run() {
				loading.start();
				initTable();
				loading.stop();
			}
		};
		t.start();
		showSuper();

	}

	public void showSuper() {
		super.show();
	}

	public String getVersionCode() {
		return versionCombo.getSelectedItem().toString();
	}

	private String buffReturnDefinition = "";
	private String buffdodate = "";
	private String buffstartDate = "";
	private String buffendDate = "";
	private boolean buffbankCode = false;
	private boolean buffcode = false;
	private boolean bufftype = false;

	private void initTable() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/bank/BankSession");
			BankSessionHome bankHome = (BankSessionHome) PortableRemoteObject.narrow(ref, BankSessionHome.class);

			BankSession bankSession = bankHome.create();

			if (!initial) {
				Vector bankCode = new Vector();
				bankCode.add(ui.getString("fina2.all"));
				bankCode.addAll((Vector) bankSession.getBanksRows(main.getUserHandle(), main.getLanguageHandle()));
				bankCodeList.setModel(new javax.swing.DefaultComboBoxModel(bankCode));
			}

			ref = jndi.lookup("fina2/returns/ReturnSession");
			ReturnSessionHome returnHome = (ReturnSessionHome) PortableRemoteObject.narrow(ref, ReturnSessionHome.class);

			ReturnSession session = returnHome.create();

			if (!initial) {
				Vector code = new Vector();
				code.add(ui.getString("fina2.all"));
				code.addAll((Vector) session.getReturnDefinitionsRows(main.getUserHandle(), main.getLanguageHandle()));
				codeList.setModel(new javax.swing.DefaultComboBoxModel(code));

				Vector type = new Vector();
				type.add(ui.getString("fina2.all"));
				type.addAll((Vector) session.getReturnTypesRows(main.getUserHandle(), main.getLanguageHandle()));
				typeCombBox.setModel(new javax.swing.DefaultComboBoxModel(type));
			}

			if (!initial) {
				Object bankCodeListObject = ui.getConfigValue("fina2.returns.SchedulesFrame.bankCodeListItem");
				if (bankCodeListObject != null) {
					bankCodeList.setSelectedItem(bankCodeListObject);
				} else {
					bankCodeList.setSelectedIndex(0);
				}

				Object codeListObject = ui.getConfigValue("fina2.returns.SchedulesFrame.codeListItem");
				if (codeListObject != null) {
					codeList.setSelectedItem(codeListObject);
				} else {
					codeList.setSelectedIndex(0);
				}

				Object typeCombBoxObject = ui.getConfigValue("fina2.returns.SchedulesFrame.typeListItem");
				if (typeCombBoxObject != null) {
					typeCombBox.setSelectedItem(typeCombBoxObject);
				} else {
					typeCombBox.setSelectedIndex(0);
				}

				Object dodateTextFieldObject = ui.getConfigValue("fina2.returns.SchedulesFrame.dodateTextField");
				if (dodateTextFieldObject != null) {
					dodateTextField.setText(dodateTextFieldObject.toString());
				}

				Object retrunTextFildObject = ui.getConfigValue("fina2.returns.SchedulesFrame.returnTextField");
				if (retrunTextFildObject != null) {
					returnTextField.setText(retrunTextFildObject.toString());
				}

				buffReturnDefinition = returnTextField.getText();
				buffdodate = dodateTextField.getText();
				buffbankCode = bankCodeList.getSelectedItem() == ui.getString("fina2.all") ? true : false;
				buffcode = codeList.getSelectedItem() == ui.getString("fina2.all") ? true : false;
				bufftype = typeCombBox.getSelectedItem() == ui.getString("fina2.all") ? true : false;

				Date fromDate = (Date) ui.getConfigValue("fina2.returns.SchedulesFrame.fromText");
				if (fromDate != null)
					if (fromDate.equals(new Date(0L))) {
						fromText.setText("");
					} else
						fromText.setText(LocaleUtil.date2string(main.getLanguageHandle(), (Date) ui.getConfigValue("fina2.returns.SchedulesFrame.fromText")));

				Date toDate = (Date) ui.getConfigValue("fina2.returns.SchedulesFrame.toText");
				if (toDate != null)
					if (toDate.equals(new Date(0L))) {
						toText.setText("");
					} else
						toText.setText(LocaleUtil.date2string(main.getLanguageHandle(), (Date) ui.getConfigValue("fina2.returns.SchedulesFrame.toText")));

				buffstartDate = fromText.getText();
				buffendDate = toText.getText();
			} else {
				buffReturnDefinition = returnTextField.getText();
				buffdodate = dodateTextField.getText();
				buffstartDate = fromText.getText();
				buffendDate = toText.getText();
				buffbankCode = bankCodeList.getSelectedItem() == ui.getString("fina2.all") ? true : false;
				buffcode = codeList.getSelectedItem() == ui.getString("fina2.all") ? true : false;
				bufftype = typeCombBox.getSelectedItem() == ui.getString("fina2.all") ? true : false;
			}

			saveState();

			Vector colNames = new Vector();
			colNames.add(ui.getString("fina2.code"));
			colNames.add(ui.getString("fina2.period.fromDate"));
			colNames.add(ui.getString("fina2.period.toDate"));
			colNames.add(ui.getString("fina2.returns.returnDefinition"));
			colNames.add(ui.getString("fina2.bank.bank"));
			colNames.add(ui.getString("fina2.type"));
			colNames.add(ui.getString("fina2.returns.acceptableDelay"));
			tableCollection = session.getSchedulesRows(main.getUserHandle(), main.getLanguageHandle(), bankCodeList.getSelectedItem().toString(), codeList.getSelectedItem().toString(), typeCombBox
					.getSelectedItem().toString(), dodateTextField.getText().toString(), returnTextField.getText().toString(), (Date) ui.getConfigValue("fina2.returns.SchedulesFrame.fromText"),
					(Date) ui.getConfigValue("fina2.returns.SchedulesFrame.toText"));
			table.initTable(colNames, tableCollection);

			if (!initial)
				loadVersions();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
			ui.putConfigValue("fina2.returns.SchedulesFrame.visible", new Boolean(false));
		}
		initial = true;
	}

	private void loadVersions() {
		try {
			InitialContext jndi = main.getJndiContext();
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

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() { // GEN-BEGIN:initComponents

		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		helpButton = new javax.swing.JButton();
		jPanel3 = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		versionCombo = new javax.swing.JComboBox();
		cancelButton = new javax.swing.JButton();
		scrollPane = new javax.swing.JScrollPane();
		jPanel4 = new javax.swing.JPanel();
		jLabel2 = new javax.swing.JLabel();
		bankCodeList = new javax.swing.JComboBox();
		jLabel3 = new javax.swing.JLabel();
		fromText = new javax.swing.JTextField();
		jLabel4 = new javax.swing.JLabel();
		toText = new javax.swing.JTextField();
		jLabel5 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		codeList = new javax.swing.JComboBox();
		filterButton = new javax.swing.JButton();

		typeCombBox = new javax.swing.JComboBox();
		returnTextField = new javax.swing.JTextField();
		dodateTextField = new javax.swing.JTextField();

		setTitle(ui.getString("fina2.returns.scheduleSelect"));
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent evt) {
				formComponentResized(evt);
			}
		});

		jPanel1.setLayout(new java.awt.BorderLayout());

		helpButton.setIcon(ui.getIcon("fina2.help"));
		helpButton.setFont(ui.getFont());
		helpButton.setText(ui.getString("fina2.help"));
		helpButton.setEnabled(false);
		jPanel2.add(helpButton);

		jPanel1.add(jPanel2, java.awt.BorderLayout.WEST);

		jLabel6.setText(ui.getString("fina2.returns.createAsVersion"));
		jLabel6.setFont(ui.getFont());
		jPanel3.add(jLabel6);

		versionCombo.setFont(ui.getFont());
		jPanel3.add(versionCombo);

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

		scrollPane.setPreferredSize(new java.awt.Dimension(320, 200));
		getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

		// jPanel4.setLayout(new java.awt.GridBagLayout());
		jPanel4.setLayout(new GridLayout());

		codeList.setFont(ui.getFont());
		jPanel4.add(codeList);

		toText.setFont(ui.getFont());
		jPanel4.add(toText);

		fromText.setFont(ui.getFont());
		jPanel4.add(fromText);

		returnTextField.setFont(ui.getFont());
		jPanel4.add(returnTextField);

		bankCodeList.setFont(ui.getFont());
		jPanel4.add(bankCodeList);

		typeCombBox.setFont(ui.getFont());
		jPanel4.add(typeCombBox);

		dodateTextField.setFont(ui.getFont());
		jPanel4.add(dodateTextField);

		filterButton.setIcon(ui.getIcon("fina2.filter"));
		filterButton.setFont(ui.getFont());
		filterButton.setText(ui.getString("fina2.filter"));
		filterButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				filterButtonActionPerformed(evt);
			}
		});

		typeCombBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (initial) {
					if (bufftype)
						doFilter();
					else
						initTable();
				}
			}
		});
		codeList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (initial) {
					if (buffcode)
						doFilter();
					else
						initTable();
				}
			}
		});
		bankCodeList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (initial) {
					if (buffbankCode)
						doFilter();
					else
						initTable();
				}

			}
		});

		DocumentListener periodDocumentListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent documentEvent) {
				// ignore
			}

			public void insertUpdate(DocumentEvent documentEvent) {
				doFilter();
			}

			public void removeUpdate(DocumentEvent documentEvent) {
				if (documentEvent.getDocument().equals(returnTextField.getDocument())) {
					if (!buffReturnDefinition.isEmpty() && (!buffReturnDefinition.endsWith(returnTextField.getText()) || returnTextField.getText().isEmpty()))
						initTable();
					else
						doFilter();
					return;
				}
				if (documentEvent.getDocument().equals(dodateTextField.getDocument())) {
					if (!buffdodate.isEmpty() && (!buffdodate.endsWith(dodateTextField.getText()) || dodateTextField.getText().isEmpty()))
						initTable();
					else
						doFilter();
					return;
				}

				if (documentEvent.getDocument().equals(fromText.getDocument())) {
					if (!buffstartDate.isEmpty() && (!buffstartDate.endsWith(fromText.getText()) || fromText.getText().isEmpty()))
						initTable();
					else
						doFilter();
					return;
				}
				if (documentEvent.getDocument().equals(toText.getDocument())) {
					if (!buffendDate.isEmpty() && (!buffendDate.endsWith(toText.getText()) || toText.getText().isEmpty()))
						initTable();
					else
						doFilter();
					return;
				}
			}

		};
		fromText.addFocusListener(this);
		toText.addFocusListener(this);

		fromText.getDocument().addDocumentListener(periodDocumentListener);
		toText.getDocument().addDocumentListener(periodDocumentListener);
		returnTextField.getDocument().addDocumentListener(periodDocumentListener);
		dodateTextField.getDocument().addDocumentListener(periodDocumentListener);

		// jPanel4.add(filterButton, new java.awt.GridBagConstraints());

		getContentPane().add(jPanel4, java.awt.BorderLayout.NORTH);
	} // GEN-END:initComponents

	private void filterButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
																				// -
																				// FIRST
																				// :
																				// event_filterButtonActionPerformed
		try {
			ui.putConfigValue("fina2.returns.selectScheduleDialog.bankCodeListItem", bankCodeList.getSelectedItem());
			ui.putConfigValue("fina2.returns.selectScheduleDialog.codeListItem", codeList.getSelectedItem());

			if (fromText.getText().trim().equals("")) {
				ui.putConfigValue("fina2.returns.selectScheduleDialog.fromText", new Date(0L));
			} else
				ui.putConfigValue("fina2.returns.selectScheduleDialog.fromText", LocaleUtil.string2date(main.getLanguageHandle(), fromText.getText().trim()));
			if (toText.getText().trim().equals("")) {
				ui.putConfigValue("fina2.returns.selectScheduleDialog.toText", new Date(0L));
			} else
				ui.putConfigValue("fina2.returns.selectScheduleDialog.toText", LocaleUtil.string2date(main.getLanguageHandle(), toText.getText().trim()));

			initTable();
		} catch (java.text.ParseException e) {
			Main.errorHandler((java.awt.Frame) this.getParent(), Main.getString("fina2.title"), Main.getString("fina2.invalidDate"));

		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

	} // GEN-LAST:event_filterButtonActionPerformed

	/**
	 * @author Davit Beradze
	 */
	private void saveState() {
		try {
			ui.putConfigValue("fina2.returns.SchedulesFrame.bankCodeListItem", bankCodeList.getSelectedItem());
			ui.putConfigValue("fina2.returns.SchedulesFrame.codeListItem", codeList.getSelectedItem());
			ui.putConfigValue("fina2.returns.SchedulesFrame.typeListItem", typeCombBox.getSelectedItem());
			ui.putConfigValue("fina2.returns.SchedulesFrame.dodateTextField", dodateTextField.getText());
			ui.putConfigValue("fina2.returns.SchedulesFrame.returnTextField", returnTextField.getText());

			if (fromText.getText().trim().equals("")) {
				ui.putConfigValue("fina2.returns.SchedulesFrame.fromText", new Date(0L)); // LocaleUtil.string2date(main.
				// getLanguageHandle
				// (),fromText.getText().trim()));
			} else
				ui.putConfigValue("fina2.returns.SchedulesFrame.fromText", LocaleUtil.string2date(main.getLanguageHandle(), fromText.getText().trim()));
			if (toText.getText().trim().equals("")) {
				ui.putConfigValue("fina2.returns.SchedulesFrame.toText", new Date(0L));
			} else
				ui.putConfigValue("fina2.returns.SchedulesFrame.toText", LocaleUtil.string2date(main.getLanguageHandle(), toText.getText().trim()));
		} catch (java.text.ParseException e) {

			// Main.errorHandler(main.getMainFrame(),
			// Main.getString("fina2.title"),
			// Main.getString("fina2.invalidDate"));

			// ignore invalid data
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	private void doFilter() {
		if (tableCollection == null)
			return;

		long start = System.currentTimeMillis();

		@SuppressWarnings("unchecked")
		Iterator<TableRowImpl> iter = tableCollection.iterator();
		List<TableRowImpl> list = new ArrayList<TableRowImpl>();

		String codeListSelVal = codeList.getSelectedItem().toString().trim().toLowerCase();
		String returnVal = returnTextField.getText().trim().toLowerCase();
		String bankCodeListSelVal = bankCodeList.getSelectedItem().toString().trim().toLowerCase();
		String typecomboBoxSelVal = typeCombBox.getSelectedItem().toString().trim().toLowerCase();
		String doDateVal = dodateTextField.getText().trim().toLowerCase();

		DateFormat dateFormat = ui.getDateFromat();

		Date from = null;
		Date to = null;
		try {
			from = dateFormat.parse(fromText.getText().trim());
		} catch (ParseException e) {
		}
		try {
			to = dateFormat.parse(toText.getText().trim());
		} catch (ParseException e) {
		}

		while (iter.hasNext()) {
			TableRowImpl row = iter.next();

			/* Equals code */
			if (codeListSelVal.equals(ui.getString("fina2.all").trim().toLowerCase()) || row.getValue(0).toLowerCase().equals(codeListSelVal)) {

				/* Equals Dates */
				if (equalsFromDate(row.getValue(1).trim(), from, dateFormat)) {
					if (equalsToDate(row.getValue(2).trim(), to, dateFormat)) {

						/* Equals Return */
						if (row.getValue(3).trim().toLowerCase().contains(returnVal)) {

							/* Equals bank */
							if (bankCodeListSelVal.equals(ui.getString("fina2.all").toLowerCase().trim()) || row.getValue(4).trim().toLowerCase().equals(bankCodeListSelVal)) {
								/* Equals Type */
								if (typecomboBoxSelVal.equals(ui.getString("fina2.all").toLowerCase().trim()) || row.getValue(5).trim().toLowerCase().equals(typecomboBoxSelVal)) {
									/* Equals do Date */
									if (doDateVal.trim().equals("") || row.getValue(6).trim().toLowerCase().contains(doDateVal)) {
										list.add(row);
									}
								}
							}
						}
					}
				}
			}

		}

		System.out.println("SelectScheduleDialog : List Size = " + list.size());

		List<String> colNames = new ArrayList<String>();
		colNames.add(ui.getString("fina2.code"));
		colNames.add(ui.getString("fina2.period.fromDate"));
		colNames.add(ui.getString("fina2.period.toDate"));
		colNames.add(ui.getString("fina2.returns.returnDefinition"));
		colNames.add(ui.getString("fina2.bank.bank"));
		colNames.add(ui.getString("fina2.type"));
		colNames.add(ui.getString("fina2.returns.acceptableDelay"));

		System.out.println("SelectScheduleDialog : Collection Filter Time = " + (System.currentTimeMillis() - start));

		table.initTable(colNames, list);
		table.resizeAndRepaint();

		System.out.println("SelectScheduleDialog : END To Filter work Time is = " + (System.currentTimeMillis() - start));

		System.gc();

		saveState();

	}

	private boolean equalsFromDate(String a, Date b, DateFormat dateFormat) {
		try {
			if (a != null && (!a.equals("")) && b != null) {
				return dateFormat.parse(a).compareTo(b) <= 0;
			} else {
				return true;
			}
		} catch (ParseException e) {
			return true;
		}
	}

	private boolean equalsToDate(String a, Date b, DateFormat dateFormat) {
		try {
			if (a != null && (!a.equals("")) && b != null) {
				return dateFormat.parse(a).compareTo(b) >= 0;
			} else {
				return true;
			}
		} catch (ParseException e) {
			return true;
		}
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		selectedTableRows = null;
		dispose();
	} // GEN-LAST:event_cancelButtonActionPerformed

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
		selectedTableRows = table.getSelectedTableRows();
		dispose();
	}

	private void closeDialog(java.awt.event.WindowEvent evt) { // GEN-FIRST:
		selectedTableRows = null; // event_closeDialog
		setVisible(false);
		dispose();
	} // GEN-LAST:event_closeDialog

	private void formComponentResized(java.awt.event.ComponentEvent evt) { // GEN-
																			// FIRST
																			// :
																			// event_formComponentResized
		ui.putConfigValue("fina2.returns.selectScheduleDialog.width", new Integer(getWidth()));
		ui.putConfigValue("fina2.returns.selectScheduleDialog.height", new Integer(getHeight()));
	} // GEN-LAST:event_formComponentResized

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
			fcalendar.getScreen().setModal(true);
		}

	}

	@Override
	public void focusLost(FocusEvent e) {
		// ignore
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JComboBox bankCodeList;
	private javax.swing.JButton cancelButton;
	private javax.swing.JComboBox codeList;
	private javax.swing.JButton filterButton;
	private javax.swing.JTextField fromText;
	private javax.swing.JButton helpButton;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JButton okButton;
	private javax.swing.JComboBox versionCombo;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JTextField toText;

	private javax.swing.JComboBox typeCombBox;
	private javax.swing.JTextField returnTextField;
	private javax.swing.JTextField dodateTextField;
	// End of variables declaration//GEN-END:variables
}
