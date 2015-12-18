/*
 * SchedulesFrame.java
 *
 * Created on November 6, 2001, 1:00 PM
 */

package fina2.returns;

import java.awt.GridBagConstraints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import fina2.BaseFrame;
import fina2.Main;
import fina2.bank.BankSession;
import fina2.bank.BankSessionHome;
import fina2.calendar.FinaCalendar;
import fina2.i18n.LocaleUtil;
import fina2.ui.MessageBox;
import fina2.ui.UIManager;
import fina2.ui.UIManager.IndeterminateLoading;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableReviewFrame;
import fina2.ui.table.TableRow;

/**
 * 
 * @author David Shalamberidze
 */
public class SchedulesFrame extends BaseFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTable table;

	private boolean canAmend = false;
	private boolean canDelete = false;
	private boolean canReview = false;

	private ScheduleCreateAction createAction;
	private ScheduleAmendAction amendAction;
	private ScheduleReviewAction reviewAction;
	private ScheduleDeleteAction deleteAction;

	private static boolean initial = false;

	private FinaCalendar fcalendar;

	/** Creates new form SchedulesFrame */
	public SchedulesFrame() {
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.refresh", "refresh.gif");
		ui.loadIcon("fina2.close", "cancel.gif");

		table = new EJBTable();

		table.addMouseListener(new java.awt.event.MouseListener() {
			public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() > 1) {
					if (amendAction.isEnabled()) {
						amendAction.actionPerformed(null);
					} else {
						if (reviewAction.isEnabled()) {
							reviewAction.actionPerformed(null);
						}
					}
				}
			}

			public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
			}
		});

		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.addSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				if (table.getSelectedRow() == -1) {
					createAction.setEnabled(canAmend);
					amendAction.setEnabled(false);
					reviewAction.setEnabled(false);
					deleteAction.setEnabled(false);
				} else {
					createAction.setEnabled(canAmend);
					amendAction.setEnabled(canAmend);
					deleteAction.setEnabled(canDelete);
					reviewAction.setEnabled(canReview || canAmend);
				}
			}
		});

		createAction = new ScheduleCreateAction(main.getMainFrame(), table);
		amendAction = new ScheduleAmendAction(main.getMainFrame(), table);
		reviewAction = new ScheduleReviewAction(main.getMainFrame(), table);
		deleteAction = new ScheduleDeleteAction(main.getMainFrame(), table, SchedulesFrame.this);

		initComponents();

		table.setPopupMenu(popupMenu);

		scrollPane.setViewportView(table);
		BaseFrame.ensureVisible(this);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
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
				typeComboBox.setModel(new javax.swing.DefaultComboBoxModel(type));
			}

			if (!initial) {
				Object bankCodeObject = ui.getConfigValue("fina2.returns.SchedulesFrame.bankCodeListItem");
				if (bankCodeObject != null) {
					bankCodeList.setSelectedItem(bankCodeObject);
				} else {
					bankCodeList.setSelectedIndex(0);
				}

				Object codeObject = ui.getConfigValue("fina2.returns.SchedulesFrame.codeListItem");
				if (codeObject != null) {
					codeList.setSelectedItem(codeObject);
				} else {
					codeList.setSelectedIndex(0);
				}

				Object typeObject = ui.getConfigValue("fina2.returns.SchedulesFrame.typeListItem");
				if (typeObject != null) {
					typeComboBox.setSelectedItem(typeObject);
				} else {
					typeComboBox.setSelectedIndex(0);
				}

				Object doDate = ui.getConfigValue("fina2.returns.SchedulesFrame.dodateTextField");

				Date fromDate = (Date) ui.getConfigValue("fina2.returns.SchedulesFrame.fromText");
				Date toDate = (Date) ui.getConfigValue("fina2.returns.SchedulesFrame.toText");

				if (fromDate != null && toDate != null) {
					if (fromDate.equals(new Date(0L))) {
						fromText.setText("");
					} else {
						fromText.setText(LocaleUtil.date2string(main.getLanguageHandle(), (Date) ui.getConfigValue("fina2.returns.SchedulesFrame.fromText")));
					}
					if (toDate.equals(new Date(0L))) {
						toText.setText("");
					} else {
						toText.setText(LocaleUtil.date2string(main.getLanguageHandle(), (Date) ui.getConfigValue("fina2.returns.SchedulesFrame.toText")));
					}
				}
			}

			saveState();

			Object bankCodeObject = ui.getConfigValue("fina2.returns.SchedulesFrame.bankCodeListItem");
			if (bankCodeObject != null) {
				bankCodeList.setSelectedItem(bankCodeObject);
			} else {
				bankCodeList.setSelectedIndex(0);
			}

			Object codeObject = ui.getConfigValue("fina2.returns.SchedulesFrame.codeListItem");
			if (codeObject != null) {
				codeList.setSelectedItem(codeObject);
			} else {
				codeList.setSelectedIndex(0);
			}

			Date fromDate = (Date) ui.getConfigValue("fina2.returns.SchedulesFrame.fromText");
			Date toDate = (Date) ui.getConfigValue("fina2.returns.SchedulesFrame.toText");

			if (fromDate != null && toDate != null) {
				if (fromDate.equals(new Date(0L))) {
					fromText.setText("");
				} else {
					fromText.setText(LocaleUtil.date2string(main.getLanguageHandle(), (Date) ui.getConfigValue("fina2.returns.SchedulesFrame.fromText")));
				}
				if (toDate.equals(new Date(0L))) {
					toText.setText("");
				} else {
					toText.setText(LocaleUtil.date2string(main.getLanguageHandle(), (Date) ui.getConfigValue("fina2.returns.SchedulesFrame.toText")));
				}
			}

			Vector colNames = new Vector();
			colNames.add(ui.getString("fina2.code"));
			colNames.add(ui.getString("fina2.period.fromDate"));
			colNames.add(ui.getString("fina2.period.toDate"));
			colNames.add(ui.getString("fina2.returns.returnDefinition"));
			colNames.add(ui.getString("fina2.bank.bank"));
			colNames.add(ui.getString("fina2.type"));
			colNames.add(ui.getString("fina2.returns.acceptableDelay"));

			String cL = (codeList.getSelectedItem() != null) ? codeList.getSelectedItem().toString() : "";
			String tCB = (typeComboBox.getSelectedItem() != null) ? typeComboBox.getSelectedItem().toString().toString() : "";

			if (fromDate == null && toDate == null) {
				fromDate = new Date(0L);
				toDate = new Date(0L);
			}

			Collection tableCollection = session.getSchedulesRows(main.getUserHandle(), main.getLanguageHandle(), bankCodeList.getSelectedItem().toString(), cL, tCB, "", "", fromDate, toDate);

			table.initTable(colNames, tableCollection);

			initial = true;

		} catch (Exception e) {
			Main.generalErrorHandler(e);
			ui.putConfigValue("fina2.returns.SchedulesFrame.visible", new Boolean(false));
		}
	}

	public void show() {
		if (isVisible())
			return;

		try {
			fina2.security.User user = (fina2.security.User) main.getUserHandle().getEJBObject();
			canAmend = user.hasPermission("fina2.returns.schedule.amend");
			canDelete = user.hasPermission("fina2.returns.schedule.delete");
			canReview = user.hasPermission("fina2.returns.schedule.review");
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

		amendAction.setEnabled(canAmend);
		amendButton.setVisible(canAmend);
		amendItem.setVisible(canAmend);

		createAction.setEnabled(canAmend);
		createButton.setVisible(canAmend);
		createItem.setVisible(canAmend);

		deleteAction.setEnabled(canDelete);
		deleteButton.setVisible(canDelete);
		deleteItem.setVisible(canDelete);

		reviewAction.setEnabled(canAmend || canReview);
		reviewButton.setVisible(canAmend || canReview);
		printButton.setVisible(canAmend || canReview);
		reviewItem.setVisible(canAmend || canReview);

		initTable();

		super.show();
	}

	public void removeFromCollection(TableRow tableRow) {
		initTable();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() { // GEN-BEGIN:initComponents

		popupMenu = new javax.swing.JPopupMenu();
		createItem = new javax.swing.JMenuItem();
		amendItem = new javax.swing.JMenuItem();
		jSeparator1 = new javax.swing.JSeparator();
		reviewItem = new javax.swing.JMenuItem();
		jSeparator2 = new javax.swing.JSeparator();
		deleteItem = new javax.swing.JMenuItem();
		jPanel1 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();
		jPanel5 = new javax.swing.JPanel();
		scrollPane = new javax.swing.JScrollPane();
		jPanel2 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		bankCodeList = new javax.swing.JComboBox();
		jLabel2 = new javax.swing.JLabel();
		fromText = new JTextField();
		jLabel3 = new javax.swing.JLabel();
		toText = new JTextField();
		jLabel4 = new javax.swing.JLabel();
		codeList = new javax.swing.JComboBox();
		filterButton = new javax.swing.JButton();
		jPanel8 = new javax.swing.JPanel();
		jPanel9 = new javax.swing.JPanel();
		jPanel6 = new javax.swing.JPanel();
		createButton = new javax.swing.JButton();
		amendButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();
		reviewButton = new javax.swing.JButton();
		jPanel10 = new javax.swing.JPanel();
		jLabel7 = new javax.swing.JLabel();

		setTitle(ui.getString("fina2.returns.schedulesAction"));
		initBaseComponents();

		createItem.setFont(ui.getFont());
		createItem.setText("Item");
		createItem.setAction(createAction);
		popupMenu.add(createItem);

		amendItem.setFont(ui.getFont());
		amendItem.setText("Item");
		amendItem.setAction(amendAction);
		popupMenu.add(amendItem);

		popupMenu.add(jSeparator1);

		reviewItem.setFont(ui.getFont());
		reviewItem.setText("Item");
		reviewItem.setAction(reviewAction);
		popupMenu.add(reviewItem);

		popupMenu.add(jSeparator2);

		deleteItem.setFont(ui.getFont());
		deleteItem.setText("Item");
		deleteItem.setAction(deleteAction);
		popupMenu.add(deleteItem);

		jPanel1.setLayout(new java.awt.BorderLayout());

		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "Schedule_Definition");
		} else {
			helpButton.setEnabled(false);
		}
		jPanel3.add(helpButton);

		jPanel1.add(jPanel3, java.awt.BorderLayout.WEST);

		jPanel4.add(printButton);

		jPanel4.add(refreshButton);

		jPanel4.add(closeButton);

		jPanel1.add(jPanel4, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

		jPanel5.setLayout(new java.awt.BorderLayout());

		jPanel5.add(scrollPane, java.awt.BorderLayout.CENTER);

		jPanel2.setLayout(new java.awt.GridBagLayout());

		jPanel2.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));

		// code
		jLabel4.setText(ui.getString("fina2.code"));
		jLabel4.setFont(ui.getFont());
		jPanel2.add(jLabel4, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 5, 0, 2)));

		codeList.setFont(ui.getFont());
		jPanel2.add(codeList, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(0, 0, 0, 10)));

		// from
		jLabel2.setText(ui.getString("fina2.from"));
		jLabel2.setFont(ui.getFont());
		jPanel2.add(jLabel2, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 5, 0, 2)));

		fromText.setColumns(10);
		fromText.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
			}

			@Override
			public void focusGained(FocusEvent e) {
				showCalendar(fromText);
			}
		});
		fromText.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2) {
					showCalendar(fromText);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
		fromText.setFont(ui.getFont());
		fromText.setMinimumSize(new java.awt.Dimension(80, 20));
		jPanel2.add(fromText, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(0, 0, 0, 5)));

		// to Date
		jLabel3.setText(ui.getString("fina2.to"));
		jLabel3.setFont(ui.getFont());
		jPanel2.add(jLabel3, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 5, 0, 2)));

		toText.setColumns(10);
		toText.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
			}

			@Override
			public void focusGained(FocusEvent e) {
				showCalendar(toText);
			}
		});
		toText.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2) {
					showCalendar(toText);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
		toText.setFont(ui.getFont());
		toText.setMinimumSize(new java.awt.Dimension(80, 20));
		jPanel2.add(toText, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(0, 0, 0, 5)));

		// bank
		jLabel1.setText(ui.getString("fina2.bank.bank"));
		jLabel1.setFont(ui.getFont());
		jPanel2.add(jLabel1, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(10, 10, 10, 2)));

		bankCodeList.setFont(ui.getFont());
		jPanel2.add(bankCodeList, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(0, 0, 0, 5)));

		JLabel typeLabel = new JLabel(ui.getString("fina2.type"));
		typeLabel.setFont(ui.getFont());
		jPanel2.add(typeLabel, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(10, 10, 10, 2)));

		typeComboBox = new JComboBox();
		typeComboBox.setFont(ui.getFont());
		jPanel2.add(typeComboBox, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(0, 0, 0, 5)));

		filterButton.setIcon(ui.getIcon("fina2.filter"));
		filterButton.setFont(ui.getFont());
		filterButton.setText(ui.getString("fina2.filter"));
		filterButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(final java.awt.event.ActionEvent evt) {
				final IndeterminateLoading loading = ui.createIndeterminateLoading(main.getMainFrame());
				Thread thread = new Thread() {
					public void run() {
						loading.start();
						filterButtonActionPerformed(evt);
						loading.stop();
					}
				};
				thread.start();
			}
		});

		jPanel2.add(filterButton, new java.awt.GridBagConstraints());

		jPanel5.add(jPanel2, java.awt.BorderLayout.NORTH);

		getContentPane().add(jPanel5, java.awt.BorderLayout.CENTER);

		jPanel8.setLayout(new java.awt.BorderLayout());

		jPanel8.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 5, 1, 5)));
		jPanel6.setLayout(new java.awt.GridBagLayout());

		createButton.setFont(ui.getFont());
		createButton.setAction(createAction);
		jPanel6.add(createButton, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, null));

		amendButton.setFont(ui.getFont());
		amendButton.setAction(amendAction);
		jPanel6.add(amendButton, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		deleteButton.setFont(ui.getFont());
		deleteButton.setAction(deleteAction);
		jPanel6.add(deleteButton, UIManager.getGridBagConstraints(0, 3, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		reviewButton.setFont(ui.getFont());
		reviewButton.setAction(reviewAction);
		jPanel6.add(reviewButton, UIManager.getGridBagConstraints(0, 2, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		jPanel9.add(jPanel6);

		jPanel8.add(jPanel9, java.awt.BorderLayout.CENTER);

		jPanel10.setLayout(new java.awt.GridBagLayout());

		jPanel10.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
		jLabel7.setText(" ");
		jLabel7.setFont(ui.getFont());
		jPanel10.add(jLabel7, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, -1, -1, new java.awt.Insets(10, 0, 10, 0)));

		jPanel8.add(jPanel10, java.awt.BorderLayout.NORTH);

		getContentPane().add(jPanel8, java.awt.BorderLayout.EAST);

	}

	private void showCalendar(JTextField textField) {
		if (fcalendar != null && FinaCalendar.ACTIVE) {
			fcalendar.exit();
			return;
		}
		fcalendar = new FinaCalendar(textField);

		// previously selected date
		java.util.Date selectedDate = fcalendar.parseDate(textField.getText());

		fcalendar.setSelectedDate(selectedDate);
		if (!FinaCalendar.ACTIVE) {
			int x = textField.getX() + textField.getWidth();
			int y = textField.getY() + textField.getHeight();
			java.awt.Component comp = textField.getParent();
			while (comp != null) {
				x += comp.getX();
				y += comp.getY();
				comp = comp.getParent();
			}
			fcalendar.start(textField, x - 85, y);
		}
	}

	protected void printButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		TableReviewFrame printFrame = new TableReviewFrame();
		printFrame.show(getTitle(), table);
	}

	private synchronized void filterButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		try {
			ui.putConfigValue("fina2.returns.SchedulesFrame.bankCodeListItem", bankCodeList.getSelectedItem());
			ui.putConfigValue("fina2.returns.SchedulesFrame.codeListItem", codeList.getSelectedItem());

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

			initTable();
		} catch (java.text.ParseException e) {
			Main.errorHandler(main.getMainFrame(), Main.getString("fina2.title"), Main.getString("fina2.invalidDate"));
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}

	} // GEN-LAST:event_filterButtonActionPerformed

	private void saveState() {
		try {
			ui.putConfigValue("fina2.returns.SchedulesFrame.bankCodeListItem", bankCodeList.getSelectedItem());
			ui.putConfigValue("fina2.returns.SchedulesFrame.codeListItem", codeList.getSelectedItem());
			ui.putConfigValue("fina2.returns.SchedulesFrame.typeListItem", (typeComboBox.getSelectedItem() != null) ? (typeComboBox.getSelectedItem()) : "");

			ui.putConfigValue("fina2.returns.SchedulesFrame.fromText", LocaleUtil.string2date(main.getLanguageHandle(), fromText.getText().trim()));

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

	protected void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		formComponentHidden(null);
		dispose();
	}

	protected void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {
		initTable();
	}

	/** Exit the Application */

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton amendButton;
	private javax.swing.JMenuItem amendItem;
	private javax.swing.JComboBox bankCodeList;
	private javax.swing.JComboBox codeList;
	private javax.swing.JButton createButton;
	private javax.swing.JMenuItem createItem;
	private javax.swing.JButton deleteButton;
	private javax.swing.JMenuItem deleteItem;
	private javax.swing.JButton filterButton;
	private javax.swing.JTextField fromText;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel10;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JPanel jPanel9;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JSeparator jSeparator2;
	private javax.swing.JPopupMenu popupMenu;
	private javax.swing.JButton reviewButton;
	private javax.swing.JMenuItem reviewItem;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JTextField toText;

	private JComboBox typeComboBox;
}

class ScheduleCreateAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private java.awt.Frame parent;
	private EJBTable table;

	private ScheduleAmendDialog dialog;

	ScheduleCreateAction(java.awt.Frame parent, EJBTable table) {
		super();

		dialog = new ScheduleAmendDialog(parent, true);

		ui.loadIcon("fina2.create", "insert.gif");

		this.parent = parent;
		this.table = table;

		putValue(AbstractAction.NAME, ui.getString("fina2.create"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.create"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		/*
		 * dialog.show(null, true); if (dialog.getTableRow() != null)
		 * table.addRow(dialog.getTableRow());
		 */
		ui.getAllAction("fina2.returns.autoSchedule").actionPerformed(actionEvent);
	}
}

class ScheduleAmendAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private java.awt.Frame parent;
	private EJBTable table;

	private ScheduleAmendDialog dialog;

	ScheduleAmendAction(java.awt.Frame parent, EJBTable table) {
		super();

		dialog = new ScheduleAmendDialog(parent, true);

		ui.loadIcon("fina2.amend", "amend.gif");

		this.parent = parent;
		this.table = table;

		putValue(AbstractAction.NAME, ui.getString("fina2.amend"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.amend"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		int index = table.getSelectedRow();
		if (index == -1)
			return;

		dialog.show(table.getSelectedTableRow(), true);

		table.updateRow(index, dialog.getTableRow());
	}

}

class ScheduleReviewAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private java.awt.Frame parent;
	private EJBTable table;

	private ScheduleAmendDialog dialog;

	ScheduleReviewAction(java.awt.Frame parent, EJBTable table) {
		super();

		dialog = new ScheduleAmendDialog(parent, true);

		ui.loadIcon("fina2.review", "review.gif");

		this.parent = parent;
		this.table = table;

		putValue(AbstractAction.NAME, ui.getString("fina2.review"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.review"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		int index = table.getSelectedRow();
		if (index == -1)
			return;
		dialog.show(table.getSelectedTableRow(), false);
	}

}

class ScheduleDeleteAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private java.awt.Frame parent;
	private EJBTable table;
	private SchedulesFrame scheduleFrame;

	ScheduleDeleteAction(java.awt.Frame parent, EJBTable table, SchedulesFrame schedule) {
		super();

		ui.loadIcon("fina2.delete", "delete.gif");

		this.parent = parent;
		this.table = table;
		this.scheduleFrame = schedule;

		putValue(AbstractAction.NAME, ui.getString("fina2.delete"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.delete"));
	}

	public synchronized void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		if (!ui.showConfirmBox(parent, ui.getString("fina2.bank.scheduleDeleteQuestion")))
			return;

		final IndeterminateLoading loading = ui.createIndeterminateLoading(parent);

		Thread thread = new Thread() {
			public void run() {
				try {
					InitialContext jndi = fina2.Main.getJndiContext();
					Object ref = jndi.lookup("fina2/returns/Schedule");
					ScheduleHome home = (ScheduleHome) PortableRemoteObject.narrow(ref, ScheduleHome.class);

					int[] indexes = table.getSelectedRows();
					Object[] selSchedulsePks = table.getSelectedPks();

					if (indexes.length > 10) {
						loading.start();
					}

					if (indexes != null && selSchedulsePks != null) {

						ArrayList<String> schedulesList = new ArrayList<String>();
						ArrayList<Integer> tableRemoweIndexes = new ArrayList<Integer>();

						int count = 0;
						for (int index : indexes) {
							if (index == -1) {
								loading.stop();
								return;
							}

							String noDeleteSchedule = "";

							Schedule schedule = home.findByPrimaryKey((SchedulePK) selSchedulsePks[count]);

							if (schedule.canDelete((((SchedulePK) selSchedulsePks[count])).getId())) {
								schedule.remove();
								tableRemoweIndexes.add(index);
							} else {
								TableRow row = table.getTableRow(index);
								noDeleteSchedule = "\n" + (schedulesList.size() + 1) + ". " + row.getValue(0) + " (" + row.getValue(1) + "-" + row.getValue(2) + ") Bank("
										+ table.getTableRow(index).getValue(4) + ")";
								schedulesList.add(noDeleteSchedule);
							}

							count++;
						}

						// Remove rows form table.
						Collections.sort(tableRemoweIndexes);
						for (int i = tableRemoweIndexes.size() - 1; i >= 0; i--) {
							table.removeRow(tableRemoweIndexes.get(i));
						}
						table.updateUI();

						if (schedulesList.size() > 0) {
							StringBuffer buff = new StringBuffer();
							for (String s : schedulesList) {
								buff.append(s);
							}
							String messageText = ui.getString("fina2.shcedules.isUsed") + "\n" + ui.getString("fina2.shcedules.couldNotDelete") + buff.toString();
							JTextArea textArea = new JTextArea(6, 36);
							textArea.setFont(ui.getFont());
							textArea.setText(messageText);
							textArea.setEditable(false);
							JScrollPane scrollPane = new JScrollPane(textArea);
							loading.stop();
							JOptionPane.showMessageDialog(parent, scrollPane, "Fina International", JOptionPane.WARNING_MESSAGE);
						}
						schedulesList.clear();
					}
				} catch (Exception ex) {
					loading.stop();
					Main.generalErrorHandler(ex);
				}
				loading.stop();
			}
		};
		thread.start();
	}
}