package fina2.reportoo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.ejb.Handle;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import fina2.Main;
import fina2.i18n.LocaleUtil;
import fina2.ui.UIManager;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableRow;

public class PeriodFilterPannel extends javax.swing.JPanel {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;
    private Vector tableRows;
    private EJBTable table;
    private Collection tableColumns;

    private Vector periodRowsCache;

    public PeriodFilterPannel(Collection tableColumns, Vector tableRows,
            EJBTable table) {
        this.tableRows = tableRows;
        this.table = table;
        this.tableColumns = tableColumns;
        initComponents();

        // Applying the pervious saved filter
        applySavedFilter();
    }

    private void initComponents() {

        this.setLayout(new fina2.ui.FlowLayoutEx());

        jLabelPeriodType.setText(ui
                .getString("fina2.reportoo.periodfilter.type"));
        jLabelPeriodType.setFont(ui.getFont());
        this.add(jLabelPeriodType);

        Vector periodTypes = new Vector();
        periodTypes.addElement(ui
                .getString("fina2.reportoo.periodfilter.type.all"));
        for (Iterator iter = tableRows.iterator(); iter.hasNext();) {
            TableRow item = (TableRow) iter.next();
            if (!periodTypes.contains(item.getValue(0))) {
                periodTypes.addElement(item.getValue(0));
            }
        }
        jComboBoxPeriodType.setModel(new javax.swing.DefaultComboBoxModel(
                periodTypes));
        jComboBoxPeriodType.setFont(ui.getFont());
        this.add(jComboBoxPeriodType);

        jLabelFrom.setText(ui.getString("fina2.reportoo.periodfilter.from"));
        jLabelFrom.setFont(ui.getFont());
        this.add(jLabelFrom);

        jTextFieldFrom.setColumns(9);
        jTextFieldFrom.setFont(ui.getFont());
        this.add(jTextFieldFrom);

        jLabelTo.setText(ui.getString("fina2.reportoo.periodfilter.to"));
        jLabelTo.setFont(ui.getFont());
        this.add(jLabelTo);

        jTextFieldTo.setColumns(9);
        jTextFieldTo.setFont(ui.getFont());
        this.add(jTextFieldTo);

        jButtonFilter.setText(ui
                .getString("fina2.reportoo.periodfilter.filter"));
        jButtonFilter.setFont(ui.getFont());
        jButtonFilter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                filterButtonPressed(e);
            }
        });
        this.add(jButtonFilter);

    }

    /** Handles filterButton */
    private void filterButtonPressed(java.awt.event.ActionEvent evt) {

        String fromDate = jTextFieldFrom.getText().trim();
        String toDate = jTextFieldTo.getText().trim();
        String periodType = (String) jComboBoxPeriodType.getSelectedItem();

        boolean result = applyFilter(fromDate, toDate, periodType);

        if (result) {
            // If filter is valid, save the given filter to a conf file
            saveFilter();
        }

    }

    /**
     * Applies a filter with specified values. Result is true, if there is no
     * error while performing the filter.
     */
    private boolean applyFilter(String fromDateString, String toDateString,
            String periodType) {

        Vector filteredRows = new Vector(tableRows);
        Handle langHandle = main.getLanguageHandle();

        try {
            Date fromDate = fromDateString.length() == 0 ? null : LocaleUtil
                    .string2date(langHandle, fromDateString);

            Date toDate = toDateString.length() == 0 ? null : LocaleUtil
                    .string2date(langHandle, toDateString);

            for (Iterator iter = filteredRows.iterator(); iter.hasNext();) {

                TableRow row = (TableRow) iter.next();

                // Filter by period type
                boolean periodTypeMismatch = false;
                String rowPeriodType = row.getValue(0);
                if (periodType != null && !periodType.equals(ui.getString("fina2.reportoo.periodfilter.type.all"))
                        && !periodType.equals(rowPeriodType)) {
                    // Period type mismatch
                    periodTypeMismatch = true;
                }

                // Filter by start date
                boolean startDateMismatch = false;
                Date rowStartDate = LocaleUtil.string2date(langHandle, row
                        .getValue(2));
                if (fromDate != null && rowStartDate.before(fromDate)) {
                    // Start date mismatch
                    startDateMismatch = true;
                }

                // Filter by end date
                boolean toDateMismatch = false;
                Date rowToDate = LocaleUtil.string2date(langHandle, row
                        .getValue(3));
                if (toDate != null && rowToDate.after(toDate)) {
                    // To date mismatch
                    toDateMismatch = true;
                }

                // Apply the filter results
                if (periodTypeMismatch || startDateMismatch || toDateMismatch) {
                    // The current row doesn't fit to the given filter.
                    // Delete it form the result.
                    iter.remove();
                }
            }

            // Pass the result rows to the table
            table.initTable(tableColumns, filteredRows);

        } catch (ParseException e) {
            Main.errorHandler(null, Main.getString("fina2.title"), Main
                    .getString("fina2.invalidDate"));
            return false;
        } catch (Exception e) {
            Main.generalErrorHandler(e);
            return false;
        }

        return true;
    }

    /** Saves the current filter */
    private void saveFilter() {

        UIManager ui = fina2.Main.main.ui;

        // From date
        String fromDate = jTextFieldFrom.getText().trim();
        ui.putConfigValue("periodFilter.fromDate", fromDate);

        // To date
        String toDate = jTextFieldTo.getText().trim();
        ui.putConfigValue("periodFilter.toDate", toDate);

        // Period type
        String periodType = (String) jComboBoxPeriodType.getSelectedItem();
        periodType = (periodType == null) ? "" : periodType;
        ui.putConfigValue("periodFilter.periodType", periodType);
    }

    /** Loads and applies the saved filter */
    private void applySavedFilter() {

        String fromDate = (String) ui.getConfigValue("periodFilter.fromDate");

        if (fromDate == null) {
            // No saved filter. Exit.
            return;
        }

        String toDate = (String) ui.getConfigValue("periodFilter.toDate");
        String periodType = (String) ui
                .getConfigValue("periodFilter.periodType");

        // Apply filter with loaded values
        applyFilter(fromDate, toDate, periodType);

        // Set filter values in filter panel fields
        jComboBoxPeriodType.setSelectedItem(periodType);
        jTextFieldFrom.setText(fromDate);
        jTextFieldTo.setText(toDate);
    }

    private JLabel jLabelPeriodType = new JLabel();
    private JLabel jLabelFrom = new JLabel();
    private JLabel jLabelTo = new JLabel();
    private JComboBox jComboBoxPeriodType = new JComboBox();
    private JTextField jTextFieldFrom = new JTextField();
    private JTextField jTextFieldTo = new JTextField();
    private JButton jButtonFilter = new JButton();
}
