/*
 * PeriodsForAutoInsertDialog.java
 *
 * Created on November 6, 2001, 5:05 AM
 */

package fina2.period;

import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.ListSelectionModel;

import fina2.Main;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableRow;

/**
 *
 * @author  vasop
 */
public class PeriodAutoInsertConfirmDialog extends javax.swing.JDialog {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private String type;
    private int frequencyType;
    private Date fromDate;
    private int startPeriodNumber;
    private int numberOfPeriods;
    private int daysInPeriods;
    private int daysBetweenPeriods;

    private EJBTable table;
    private TableRow tableRow;
    private Period period;
    private PeriodTypePK typePK;

    private Collection tableRows;
    private Vector canceledRows;
    private int rowCount;
    private boolean ins;

    /** Creates new form PeriodsForAutoInsertDialog */
    public PeriodAutoInsertConfirmDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        ui.loadIcon("fina2.cancel", "cancel.gif");
        ui.loadIcon("fina2.ok", "ok.gif");

        table = new EJBTable();
        table.setAllowSort(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table
                .addSelectionListener(new javax.swing.event.ListSelectionListener() {
                    public void valueChanged(
                            javax.swing.event.ListSelectionEvent evt) {
                        if (table.getSelectedRow() == -1) {
                            insertButton.setEnabled(false);
                        } else {
                            insertButton.setEnabled(true);
                        }
                    }
                });

        initComponents();

        scrollPane.setViewportView(table);
    }

    public Collection getCanceledRows() {
        return canceledRows;
    }

    public int getRowCount() {
        return rowCount;
    }

    public boolean isInsert() {
        return ins;
    }

    private void initTable() {
        try {
            InitialContext jndi = fina2.Main.getJndiContext();
            Object ref = jndi.lookup("fina2/period/PeriodSession");
            PeriodSessionHome home = (PeriodSessionHome) PortableRemoteObject
                    .narrow(ref, PeriodSessionHome.class);

            PeriodSession session = home.create();

            Vector colNames = new Vector();
            colNames.add(ui.getString("fina2.period.periodType"));
            colNames.add(ui.getString("fina2.period.periodNumber"));
            colNames.add(ui.getString("fina2.period.fromDate"));
            colNames.add(ui.getString("fina2.period.toDate"));

            if (daysInPeriods < 0 || daysBetweenPeriods < 0)
                tableRows = session.getPeriodInsertRows(main
                        .getLanguageHandle(), type, frequencyType, fromDate,
                        startPeriodNumber, numberOfPeriods);
            else
                tableRows = session.getPeriodInsertRows(main
                        .getLanguageHandle(), type, fromDate,
                        startPeriodNumber, numberOfPeriods, daysInPeriods,
                        daysBetweenPeriods);
            table.initTable(colNames, tableRows);

            rowCount = table.getRowCount();
        } catch (Exception e) {
            Main.generalErrorHandler(e);
        }
    }

    public void show(PeriodTypePK typePK, String type, int frequencyType,
            Date fromDate, int startPeriodNumber, int numberOfPeriods) {

        this.typePK = typePK;
        this.type = type;
        this.frequencyType = frequencyType;
        this.fromDate = fromDate;
        this.startPeriodNumber = startPeriodNumber;
        this.numberOfPeriods = numberOfPeriods;
        this.daysInPeriods = -1;
        this.daysBetweenPeriods = -1;

        if (isVisible())
            return;

        initTable();

        setLocationRelativeTo(getParent());
        show();
    }

    public void show(PeriodTypePK typePK, String type, int frequencyType,
            Date fromDate, int startPeriodNumber, int numberOfPeriods,
            int daysInPeriods, int daysBetweenPeriods) {

        this.typePK = typePK;
        this.type = type;
        this.frequencyType = frequencyType;
        this.fromDate = fromDate;
        this.startPeriodNumber = startPeriodNumber;
        this.numberOfPeriods = numberOfPeriods;
        this.daysInPeriods = daysInPeriods;
        this.daysBetweenPeriods = daysBetweenPeriods;

        if (isVisible())
            return;

        initTable();

        setLocationRelativeTo(getParent());
        show();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        insertButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setTitle(ui.getString("fina2.period.periodAutoInsertConfirm"));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel1.setPreferredSize(new java.awt.Dimension(350, 350));
        jPanel1.add(scrollPane, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.GridLayout(2, 0));

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText(ui.getString("fina2.period.oneYear"));
        jLabel1.setFont(ui.getFont());
        jPanel4.add(jLabel1);

        jPanel3.add(jPanel4);

        insertButton.setIcon(ui.getIcon("fina2.ok"));
        insertButton.setFont(ui.getFont());
        insertButton.setText(ui.getString("fina2.insert"));
        insertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertButtonActionPerformed(evt);
            }
        });

        jPanel2.add(insertButton);

        cancelButton.setIcon(ui.getIcon("fina2.cancel"));
        cancelButton.setFont(ui.getFont());
        cancelButton.setText(ui.getString("fina2.cancel"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jPanel2.add(cancelButton);

        jPanel3.add(jPanel2);

        getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        pack();
    }//GEN-END:initComponents

    private void insertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertButtonActionPerformed
        try {
            InitialContext jndi = fina2.Main.getJndiContext();
            Object ref = jndi.lookup("fina2/period/PeriodSession");
            PeriodSessionHome home = (PeriodSessionHome) PortableRemoteObject
                    .narrow(ref, PeriodSessionHome.class);

            PeriodSession session = home.create();
            canceledRows = (Vector) session.savePeriods(main
                    .getLanguageHandle(), typePK, tableRows);
            ins = true;
        } catch (Exception e) {
            ins = false;
            Main.generalErrorHandler(e);
        }
        dispose();
    }//GEN-LAST:event_insertButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        ins = false;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JButton insertButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

}
