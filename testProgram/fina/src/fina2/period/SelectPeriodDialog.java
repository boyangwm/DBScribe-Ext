/*
 * SelectPeriodDialog.java
 *
 * Created on November 6, 2001, 5:27 PM
 */

package fina2.period;

import java.awt.GridBagConstraints;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.ListSelectionModel;

import fina2.Main;
import fina2.i18n.Language;
import fina2.i18n.LocaleUtil;
import fina2.ui.UIManager;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableRow;
import fina2.ui.table.TableRowImpl;
import fina2.util.Comparators;

@SuppressWarnings("serial")
public class SelectPeriodDialog extends javax.swing.JDialog {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private EJBTable table;
	private TableRow tableRow;

	/**
	 * Creates new form SelectPeriodDialog
	 * 
	 * @param parent
	 *            Frame
	 * @param modal
	 *            boolean
	 */
	public SelectPeriodDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);

		ui.loadIcon("fina2.cancel", "cancel.gif");
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.ok", "ok.gif");

		table = new EJBTable();

		initComponents();

		table.addMouseListener(new java.awt.event.MouseListener() {
			public void mouseExited(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
			}

			public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() > 1)
					okButtonActionPerformed(null);
			}

			public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {
			}
		});

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scrollPane.setViewportView(table);

		loadConf();
	}

	private void loadConf() {

		int w = 550, h = 400;

		try {
			w = ((Integer) ui.getConfigValue("fina2.period.SelectPeriodDialog.width")).intValue();
			h = ((Integer) ui.getConfigValue("fina2.period.SelectPeriodDialog.height")).intValue();
		} catch (Exception e) {
		}

		setSize(w, h);
	}

	public TableRow getTableRow() {
		return tableRow;
	}

	@SuppressWarnings("deprecation")
	public void show() {
		initTable();

		setLocationRelativeTo(getParent());
		super.show();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initTable() {
		try {

			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/period/PeriodSession");
			PeriodSessionHome home = (PeriodSessionHome) PortableRemoteObject.narrow(ref, PeriodSessionHome.class);

			PeriodSession session = home.create();

			Vector rows = (Vector) session.getPeriodRows(main.getUserHandle(), main.getLanguageHandle(), ui.getString("fina2.all"), new Date(0L), new Date(0L));
			Vector type = new Vector();
			type.add(ui.getString("fina2.all"));
			Hashtable typeH = new Hashtable();
			for (java.util.Iterator iter = rows.iterator(); iter.hasNext();) {
				TableRowImpl row = (TableRowImpl) iter.next();
				typeH.put(row.getValue(0), row.getValue(0));
			}

			Collection typeC = typeH.values();

			for (java.util.Iterator iter = typeC.iterator(); iter.hasNext();) {
				type.add(iter.next());
			}

			typeList.setModel(new javax.swing.DefaultComboBoxModel(type));

			Object typeListObject = ui.getConfigValue("fina2.period.selectPeriodDialog.typeListItem");
			if (typeListObject != null) {
				typeList.setSelectedItem(typeListObject);
			} else {
				typeList.setSelectedIndex(0);
			}

			Date fromDate = (Date) ui.getConfigValue("fina2.period.selectPeriodDialog.fromText");
			Date toDate = (Date) ui.getConfigValue("fina2.period.selectPeriodDialog.toText");

			if (fromDate != null) {
				if (fromDate.equals(new Date(0L))) {
					fromText.setText("");
				} else
					fromText.setText(LocaleUtil.date2string(main.getLanguageHandle(), (Date) ui.getConfigValue("fina2.period.selectPeriodDialog.fromText")));
			}

			if (toDate != null) {
				if (toDate.equals(new Date(0L))) {
					toText.setText("");
				} else
					toText.setText(LocaleUtil.date2string(main.getLanguageHandle(), (Date) ui.getConfigValue("fina2.period.selectPeriodDialog.toText")));
			}

			Vector colNames = new Vector();
			colNames.add(ui.getString("fina2.period.periodType"));
			colNames.add(ui.getString("fina2.period.periodNumber"));
			colNames.add(ui.getString("fina2.period.fromDate"));
			colNames.add(ui.getString("fina2.period.toDate"));

			List<TableRowImpl> v = null;
			if (fromDate != null && toDate != null) {
				v = (List<TableRowImpl>) session.getPeriodRows(main.getUserHandle(), main.getLanguageHandle(), typeList.getSelectedItem().toString(),
						(Date) ui.getConfigValue("fina2.period.selectPeriodDialog.fromText"), (Date) ui.getConfigValue("fina2.period.selectPeriodDialog.toText"));
			} else {
				v = (List<TableRowImpl>) session.getPeriodRows(main.getUserHandle(), main.getLanguageHandle(), typeList.getSelectedItem().toString(), new Date(0L), new Date());

			}

			Language lang = (Language) main.getLanguageHandle().getEJBObject();
			DateFormat dateFormat = new SimpleDateFormat(lang.getDateFormat());
			Collections.sort(v, new Comparators.TableRowComparatorValueDate(dateFormat, 2, 3));

			table.initTable(colNames, v);

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
		cancelButton = new javax.swing.JButton();
		scrollPane = new javax.swing.JScrollPane();
		jPanel4 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		typeList = new javax.swing.JComboBox();
		jLabel2 = new javax.swing.JLabel();
		fromText = new javax.swing.JTextField();
		jLabel3 = new javax.swing.JLabel();
		toText = new javax.swing.JTextField();
		filterButton = new javax.swing.JButton();

		setTitle(ui.getString("fina2.period.periods"));
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

		jPanel4.setLayout(new java.awt.GridBagLayout());

		jLabel1.setText(ui.getString("fina2.type"));
		jLabel1.setFont(ui.getFont());
		jPanel4.add(jLabel1, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(10, 10, 10, 2)));

		typeList.setFont(ui.getFont());
		jPanel4.add(typeList, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(0, 0, 0, 5)));

		jLabel2.setText(ui.getString("fina2.from"));
		jLabel2.setFont(ui.getFont());
		jPanel4.add(jLabel2, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 5, 0, 2)));

		fromText.setColumns(10);
		fromText.setFont(ui.getFont());
		fromText.setMinimumSize(new java.awt.Dimension(80, 20));
		jPanel4.add(fromText, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(0, 0, 0, 5)));

		jLabel3.setText(ui.getString("fina2.to"));
		jLabel3.setFont(ui.getFont());
		jPanel4.add(jLabel3, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(0, 5, 0, 2)));

		toText.setColumns(10);
		toText.setFont(ui.getFont());
		toText.setMinimumSize(new java.awt.Dimension(80, 20));
		jPanel4.add(toText, UIManager.getGridBagConstraints(-1, -1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(0, 0, 0, 5)));

		filterButton.setIcon(ui.getIcon("fina2.filter"));
		filterButton.setFont(ui.getFont());
		filterButton.setText(ui.getString("fina2.filter"));
		filterButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				filterButtonActionPerformed(evt);
			}
		});
		jPanel4.add(filterButton, new GridBagConstraints());

		getContentPane().add(jPanel4, java.awt.BorderLayout.NORTH);
	} // GEN-END:initComponents

	private void filterButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_filterButtonActionPerformed
		try {
			ui.putConfigValue("fina2.period.selectPeriodDialog.typeListItem", typeList.getSelectedItem());

			if (fromText.getText().trim().equals("")) {
				ui.putConfigValue("fina2.period.selectPeriodDialog.fromText", new Date(0L));
			} else
				ui.putConfigValue("fina2.period.selectPeriodDialog.fromText", LocaleUtil.string2date(main.getLanguageHandle(), fromText.getText().trim()));
			if (toText.getText().trim().equals("")) {
				ui.putConfigValue("fina2.period.selectPeriodDialog.toText", new Date(0L));
			} else
				ui.putConfigValue("fina2.period.selectPeriodDialog.toText", LocaleUtil.string2date(main.getLanguageHandle(), toText.getText().trim()));

			initTable();

		} catch (java.text.ParseException e) {
			Main.errorHandler((java.awt.Frame) this.getParent(), Main.getString("fina2.title"), Main.getString("fina2.invalidDate"));
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	} // GEN-LAST:event_filterButtonActionPerformed

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-
		// FIRST
		// :
		// event_okButtonActionPerformed
		tableRow = table.getSelectedTableRow();
		dispose();
	} // GEN-LAST:event_okButtonActionPerformed

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_cancelButtonActionPerformed
		tableRow = null;
		dispose();
	} // GEN-LAST:event_cancelButtonActionPerformed

	/**
	 * Closes the dialog
	 * 
	 * @param evt
	 *            WindowEvent
	 */
	private void closeDialog(java.awt.event.WindowEvent evt) { // GEN-FIRST:
		// event_closeDialog
		tableRow = null;
		setVisible(false);
		dispose();
	} // GEN-LAST:event_closeDialog

	private void formComponentResized(java.awt.event.ComponentEvent evt) { // GEN-
		// FIRST
		// :
		// event_formComponentResized
		ui.putConfigValue("fina2.period.SelectPeriodDialog.width", new Integer(getWidth()));
		ui.putConfigValue("fina2.period.SelectPeriodDialog.height", new Integer(getHeight()));
	} // GEN-LAST:event_formComponentResized

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JButton helpButton;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JButton okButton;
	private javax.swing.JButton cancelButton;
	private javax.swing.JScrollPane scrollPane;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JComboBox typeList;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JTextField fromText;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JTextField toText;
	private javax.swing.JButton filterButton;
	// End of variables declaration//GEN-END:variables
}
