/*
 * PeriodFrame.java
 *
 * Created on October 30, 2001, 3:15 AM
 */

package fina2.period;

import java.awt.GridBagConstraints;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.AbstractAction;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import com.sun.star.awt.FocusEvent;

import fina2.BaseFrame;
import fina2.Main;
import fina2.calendar.FinaCalendar;
import fina2.i18n.Language;
import fina2.i18n.LocaleUtil;
import fina2.ui.UIManager;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableReviewFrame;
import fina2.ui.table.TableRowImpl;
import fina2.util.Comparators;

/**
 * 
 * @author vasop
 */
@SuppressWarnings("serial")
public class PeriodFrame extends BaseFrame {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTable table;

	private boolean canAmend = false;
	private boolean canDelete = false;
	private boolean canReview = false;

	private PeriodAmendAction periodAmendAction;
	private PeriodInsertAction periodInsertAction;
	private PeriodReviewAction periodReviewAction;
	private PeriodDeleteAction periodDeleteAction;

	private FinaCalendar fcalendar;

	/** Creates new form PeriodFrame */
	public PeriodFrame() {
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.print", "print.gif");
		ui.loadIcon("fina2.refresh", "refresh.gif");
		ui.loadIcon("fina2.close", "cancel.gif");

		ui.loadIcon("fina2.amend", "amend.gif");
		ui.loadIcon("fina2.create", "insert.gif");
		ui.loadIcon("fina2.review", "review.gif");
		ui.loadIcon("fina2.delete", "delete.gif");

		table = new EJBTable();

		periodAmendAction = new PeriodAmendAction(main.getMainFrame(), table);
		periodInsertAction = new PeriodInsertAction(main.getMainFrame(), table);
		periodReviewAction = new PeriodReviewAction(main.getMainFrame(), table);
		periodDeleteAction = new PeriodDeleteAction(main.getMainFrame(), table);

		table.addMouseListener(new java.awt.event.MouseListener() {
			public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() > 1) {
					if (periodAmendAction.isEnabled()) {
						periodAmendAction.actionPerformed(null);
					} else {
						if (periodReviewAction.isEnabled()) {
							periodReviewAction.actionPerformed(null);
						}
					}
				}
			}

			public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
			}
		});

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		table.addSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				if (table.getSelectedRow() == -1) {
					periodInsertAction.setEnabled(canAmend);
					periodAmendAction.setEnabled(false);
					periodReviewAction.setEnabled(false);
					periodDeleteAction.setEnabled(false);
				} else {
					periodInsertAction.setEnabled(canAmend);
					periodAmendAction.setEnabled(canAmend);
					periodDeleteAction.setEnabled(canDelete);
					periodReviewAction.setEnabled(canReview || canAmend);
				}
			}
		});

		initComponents();

		table.setPopupMenu(periodPopupMenu);
		scrollPane.setViewportView(table);
		BaseFrame.ensureVisible(this);
	}

	private void initTable() {
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/period/PeriodSession");
			PeriodSessionHome home = (PeriodSessionHome) PortableRemoteObject.narrow(ref, PeriodSessionHome.class);

			PeriodSession session = home.create();

			Vector rows = (Vector) session.getPeriodTypeRows(main.getUserHandle(), main.getLanguageHandle()); // ,
			// ui.getString
			// (
			// "fina2.all"
			// ), new
			// Date(0L),
			// new
			// Date(0L))
			// ;
			Vector type = new Vector();
			type.add(ui.getString("fina2.all"));
			for (java.util.Iterator iter = rows.iterator(); iter.hasNext();) {
				TableRowImpl row = (TableRowImpl) iter.next();
				type.add(row.getValue(1));
			}

			typeList.setModel(new javax.swing.DefaultComboBoxModel(type));

			// Default Selection
			typeList.setSelectedIndex(0);

			Object typeObject = ui.getConfigValue("fina2.period.PeriodFrame.typeListItem");
			if (typeObject != null)
				typeList.setSelectedItem(typeObject);

			Date fromDate = (Date) ui.getConfigValue("fina2.period.PeriodFrame.fromText");
			Date toDate = (Date) ui.getConfigValue("fina2.period.PeriodFrame.toText");

			if (fromDate != null && toDate != null) {
				if (fromDate.equals(new Date(0L))) {
					fromText.setText("");
				} else {
					fromText.setText(LocaleUtil.date2string(main.getLanguageHandle(), (Date) ui.getConfigValue("fina2.period.PeriodFrame.fromText")));
				}
				if (toDate.equals(new Date(0L))) {
					toText.setText("");
				} else {
					toText.setText(LocaleUtil.date2string(main.getLanguageHandle(), (Date) ui.getConfigValue("fina2.period.PeriodFrame.toText")));
				}
			}

			Vector colNames = new Vector();
			colNames.add(ui.getString("fina2.period.periodType"));
			colNames.add(ui.getString("fina2.period.periodNumber"));
			colNames.add(ui.getString("fina2.period.fromDate"));
			colNames.add(ui.getString("fina2.period.toDate"));

			if (fromDate == null && toDate == null) {
				fromDate = new Date(0L);
				toDate = new Date(0L);
			}

			List periodRowsList = (List) session.getPeriodRows(main.getUserHandle(), main.getLanguageHandle(), typeList.getSelectedItem().toString(), fromDate, toDate);

			Language lang = (Language) main.getLanguageHandle().getEJBObject();
			DateFormat dateFormat = new SimpleDateFormat(lang.getDateFormat());
			Collections.sort(periodRowsList, new Comparators.TableRowComparatorValueDate(dateFormat, 2, 3));

			table.initTable(colNames, periodRowsList);

			if (table.getRowCount() > 0)
				table.setRowSelectionInterval(0, 0);
		} catch (Exception e) {
			Main.generalErrorHandler(e);
			ui.putConfigValue("fina2.period.PeriodFrame.visible", new Boolean(false));
		}
	}

	public void show() {
		setFont(ui.getFont());
		if (isVisible())
			return;

		try {
			fina2.security.User user = (fina2.security.User) main.getUserHandle().getEJBObject();
			canAmend = user.hasPermission("fina2.periods.amend");
			canDelete = user.hasPermission("fina2.periods.delete");
			canReview = user.hasPermission("fina2.periods.review");
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		periodAmendAction.setEnabled(canAmend);
		amendButton.setVisible(canAmend);
		amendItem.setVisible(canAmend);

		periodInsertAction.setEnabled(canAmend);
		createButton.setVisible(canAmend);
		createItem.setVisible(canAmend);

		periodDeleteAction.setEnabled(canDelete);
		deleteButton.setVisible(canDelete);
		deleteItem.setVisible(canDelete);

		periodReviewAction.setEnabled(canAmend || canReview);
		reviewButton.setVisible(canAmend || canReview);
		printButton.setVisible(canAmend || canReview);
		reviewItem.setVisible(canAmend || canReview);
		initTable();
		super.show();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		periodPopupMenu = new javax.swing.JPopupMenu();
		createItem = new javax.swing.JMenuItem();
		amendItem = new javax.swing.JMenuItem();
		reviewItem = new javax.swing.JMenuItem();
		deleteItem = new javax.swing.JMenuItem();
		jSeparator1 = new javax.swing.JSeparator();
		printItem = new javax.swing.JMenuItem();
		jSeparator2 = new javax.swing.JSeparator();
		refreshItem = new javax.swing.JMenuItem();
		jPanel1 = new javax.swing.JPanel();
		scrollPane = new javax.swing.JScrollPane();
		jPanel7 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		typeList = new javax.swing.JComboBox();
		jLabel2 = new javax.swing.JLabel();
		fromText = new javax.swing.JTextField();
		jLabel3 = new javax.swing.JLabel();
		toText = new javax.swing.JTextField();
		filterButton = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		jPanel4 = new javax.swing.JPanel();
		jPanel8 = new javax.swing.JPanel();
		jPanel9 = new javax.swing.JPanel();
		jLabel4 = new javax.swing.JLabel();
		jPanel5 = new javax.swing.JPanel();
		jPanel6 = new javax.swing.JPanel();
		createButton = new javax.swing.JButton();
		amendButton = new javax.swing.JButton();
		reviewButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();
		createItem.setFont(ui.getFont());
		createItem.setIcon(ui.getIcon("fina2.insert"));
		createItem.setAction(periodInsertAction);
		periodPopupMenu.add(createItem);
		amendItem.setFont(ui.getFont());
		amendItem.setIcon(ui.getIcon("fina2.amend"));
		amendItem.setAction(periodAmendAction);
		periodPopupMenu.add(amendItem);
		reviewItem.setFont(ui.getFont());
		reviewItem.setIcon(ui.getIcon("fina2.review"));
		reviewItem.setAction(periodReviewAction);
		periodPopupMenu.add(reviewItem);
		deleteItem.setFont(ui.getFont());
		deleteItem.setIcon(ui.getIcon("fina2.delete"));
		deleteItem.setAction(periodDeleteAction);
		periodPopupMenu.add(deleteItem);
		periodPopupMenu.add(jSeparator1);
		printItem.setFont(ui.getFont());
		printItem.setText(ui.getString("fina2.print"));
		printItem.setIcon(ui.getIcon("fina2.print"));
		periodPopupMenu.add(printItem);
		periodPopupMenu.add(jSeparator2);
		refreshItem.setFont(ui.getFont());
		refreshItem.setText(ui.getString("fina2.refresh"));
		refreshItem.setIcon(ui.getIcon("fina2.refresh"));
		refreshItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				refreshItemActionPerformed(evt);
			}
		});

		periodPopupMenu.add(refreshItem);

		setTitle(ui.getString("fina2.period.periods"));
		initBaseComponents();
		jPanel1.setLayout(new java.awt.BorderLayout());

		jPanel1.add(scrollPane, java.awt.BorderLayout.CENTER);

		jPanel7.setLayout(new java.awt.GridBagLayout());

		jLabel1.setText(ui.getString("fina2.type"));
		jLabel1.setFont(ui.getFont());
		jPanel7.add(jLabel1, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(10, 10, 10, 2)));

		typeList.setFont(ui.getFont());
		jPanel7.add(typeList, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(0, 0, 0, 5)));

		jLabel2.setText(ui.getString("fina2.from"));
		jLabel2.setFont(ui.getFont());
		jPanel7.add(jLabel2, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 5, 0, 2)));

		fromText.setColumns(10);
		fromText.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(java.awt.event.FocusEvent e) {
			}

			@Override
			public void focusGained(java.awt.event.FocusEvent e) {
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
		jPanel7.add(fromText, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(0, 0, 0, 5)));

		jLabel3.setText(ui.getString("fina2.to"));
		jLabel3.setFont(ui.getFont());
		jPanel7.add(jLabel3, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 5, 0, 2)));

		toText.setColumns(10);
		toText.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(java.awt.event.FocusEvent e) {
			}

			@Override
			public void focusGained(java.awt.event.FocusEvent e) {
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
		jPanel7.add(toText, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(0, 0, 0, 5)));

		filterButton.setIcon(ui.getIcon("fina2.filter"));
		filterButton.setFont(ui.getFont());
		filterButton.setText(ui.getString("fina2.filter"));
		filterButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				filterButtonActionPerformed(evt);
			}
		});
		jPanel7.add(filterButton, new GridBagConstraints());

		jPanel1.add(jPanel7, java.awt.BorderLayout.NORTH);

		getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

		jPanel2.setLayout(new java.awt.BorderLayout());

		if (ui.getHelpManager().IsHelpSystem()) {
			ui.getHelpManager().createDisplayHelpFromFocus(helpButton, "Period_Definition");
		} else {
			helpButton.setEnabled(false);
		}
		jPanel3.add(helpButton);

		jPanel2.add(jPanel3, java.awt.BorderLayout.WEST);

		jPanel4.add(printButton);

		jPanel4.add(refreshButton);

		jPanel4.add(closeButton);

		jPanel2.add(jPanel4, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

		jPanel8.setLayout(new java.awt.BorderLayout());

		jPanel9.setLayout(new java.awt.GridBagLayout());

		jPanel9.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
		jLabel4.setText(" ");
		jLabel4.setFont(ui.getFont());
		jPanel9.add(jLabel4, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, -1, -1, new java.awt.Insets(10, 0, 10, 0)));

		jPanel8.add(jPanel9, java.awt.BorderLayout.NORTH);

		jPanel5.setLayout(new java.awt.BorderLayout());

		jPanel5.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 5, 1, 5)));
		jPanel6.setLayout(new java.awt.GridBagLayout());

		createButton.setIcon(ui.getIcon("fina2.insert"));
		createButton.setFont(ui.getFont());
		createButton.setAction(periodInsertAction);
		jPanel6.add(createButton, UIManager.getGridBagConstraints(0, 0, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, null));

		amendButton.setIcon(ui.getIcon("fina2.amend"));
		amendButton.setFont(ui.getFont());
		amendButton.setAction(periodAmendAction);
		jPanel6.add(amendButton, UIManager.getGridBagConstraints(0, 1, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		reviewButton.setIcon(ui.getIcon("fina2.review"));
		reviewButton.setFont(ui.getFont());
		reviewButton.setAction(periodReviewAction);
		jPanel6.add(reviewButton, UIManager.getGridBagConstraints(0, 2, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		deleteButton.setIcon(ui.getIcon("fina2.delete"));
		deleteButton.setFont(ui.getFont());
		deleteButton.setAction(periodDeleteAction);
		jPanel6.add(deleteButton, UIManager.getGridBagConstraints(0, 3, -1, -1, -1, -1, -1, GridBagConstraints.HORIZONTAL, new java.awt.Insets(5, 0, 0, 0)));

		jPanel5.add(jPanel6, java.awt.BorderLayout.NORTH);

		jPanel8.add(jPanel5, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel8, java.awt.BorderLayout.EAST);

	}// GEN-END:initComponents

	protected void printButtonActionPerformed(java.awt.event.ActionEvent evt) {
		TableReviewFrame printFrame = new TableReviewFrame();
		printFrame.show(getTitle(), table);
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

	private void filterButtonActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			// ui.putConfigValue("fina2.returns.SchedulesFrame.bankCodeListItem",
			// bankCodeList.getSelectedItem());
			ui.putConfigValue("fina2.period.PeriodFrame.typeListItem", typeList.getSelectedItem());

			if (fromText.getText().trim().equals("")) {
				ui.putConfigValue("fina2.period.PeriodFrame.fromText", new Date(0L));
			} else
				ui.putConfigValue("fina2.period.PeriodFrame.fromText", LocaleUtil.string2date(main.getLanguageHandle(), fromText.getText().trim()));
			if (toText.getText().trim().equals("")) {
				ui.putConfigValue("fina2.period.PeriodFrame.toText", new Date(0L));
			} else
				ui.putConfigValue("fina2.period.PeriodFrame.toText", LocaleUtil.string2date(main.getLanguageHandle(), toText.getText().trim()));

			initTable();
		} catch (java.text.ParseException e) {
			Main.errorHandler(main.getMainFrame(), Main.getString("fina2.title"), Main.getString("fina2.invalidDate"));
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	protected void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {
		initTable();
	}

	private void refreshItemActionPerformed(java.awt.event.ActionEvent evt) {
		initTable();
	}

	protected void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		formComponentHidden(null);
		dispose();
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPopupMenu periodPopupMenu;
	private javax.swing.JMenuItem createItem;
	private javax.swing.JMenuItem amendItem;
	private javax.swing.JMenuItem reviewItem;
	private javax.swing.JMenuItem deleteItem;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JMenuItem printItem;
	private javax.swing.JSeparator jSeparator2;
	private javax.swing.JMenuItem refreshItem;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JComboBox typeList;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JTextField fromText;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JTextField toText;
	private javax.swing.JButton filterButton;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JPanel jPanel9;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JButton createButton;
	private javax.swing.JButton amendButton;
	private javax.swing.JButton reviewButton;
	private javax.swing.JButton deleteButton;
	// End of variables declaration//GEN-END:variables

}

class PeriodAmendAction extends javax.swing.AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private PeriodAmendDialog dialog;
	private java.awt.Frame parent;
	private EJBTable table;

	/** Creates new PeriodTypeAmendAction */
	PeriodAmendAction(java.awt.Frame parent, EJBTable table) {
		super();

		dialog = new PeriodAmendDialog(parent, true);

		ui.loadIcon("fina2.amend", "amend.gif");

		this.parent = parent;
		this.table = table;

		putValue(AbstractAction.NAME, ui.getString("fina2.amend"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.amend"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		int index = table.getSelectedRow();

		dialog.show(table.getSelectedTableRow(), true);

		table.updateRow(index, dialog.getTableRow());
	}

}

class PeriodInsertAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	PeriodInsertAction(java.awt.Frame parent, EJBTable table) {
		super();
		ui.loadIcon("fina2.insert", "insert.gif");
		putValue(AbstractAction.NAME, ui.getString("fina2.create"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.insert"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		ui.getAllAction("fina2.period.periodAutoInsert").actionPerformed(actionEvent);
	}

}

class PeriodReviewAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private PeriodAmendDialog dialog;
	private java.awt.Frame parent;
	private EJBTable table;

	PeriodReviewAction(java.awt.Frame parent, EJBTable table) {
		super();

		dialog = new PeriodAmendDialog(parent, true);
		ui.loadIcon("fina2.review", "review.gif");

		this.parent = parent;
		this.table = table;

		putValue(AbstractAction.NAME, ui.getString("fina2.review"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.review"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		dialog.show(table.getSelectedTableRow(), false);

	}

}

class PeriodDeleteAction extends AbstractAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private java.awt.Frame parent;
	private EJBTable table;
	private Period period;

	PeriodDeleteAction(java.awt.Frame parent, EJBTable table) {
		super();

		ui.loadIcon("fina2.delete", "delete.gif");

		this.parent = parent;
		this.table = table;

		putValue(AbstractAction.NAME, ui.getString("fina2.delete"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.delete"));
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		if (!ui.showConfirmBox(parent, ui.getString("fina2.period.periodDeleteQuestion")))
			return;

		try {
			int index = table.getSelectedRow();

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/period/Period");
			PeriodHome home = (PeriodHome) PortableRemoteObject.narrow(ref, PeriodHome.class);

			period = home.findByPrimaryKey((PeriodPK) table.getSelectedPK());
			period.remove();

			table.removeRow(index);
		} catch (Exception e) {
			Main.errorHandler(parent, Main.getString("fina2.title"), Main.getString("fina2.delete.period"));
		}

	}

}
