/*
 * CanceledPeriods.java
 *
 * Created on November 8, 2002, 4:08 PM
 */

package fina2.returns;

import java.util.Collection;
import java.util.Vector;

import javax.swing.ListSelectionModel;

import fina2.Main;
import fina2.ui.table.EJBTable;

/**
 *
 * @author  vasop
 */
public class CanceledSchedulesDialog extends javax.swing.JDialog {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private EJBTable table;
    private Collection canceledRows;
    private int rowCount;

    /** Creates new form CanceledPeriods */
    public CanceledSchedulesDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        ui.loadIcon("fina2.ok", "ok.gif");

        table = new EJBTable();
        table.setAllowSort(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        initComponents();

        scrollPane.setViewportView(table);
    }

    public void show(Collection canceledRows, int rowCount) {
        this.canceledRows = canceledRows;
        this.rowCount = rowCount;

        initTable();

        appendRowsLabel.setText(String.valueOf(rowCount - table.getRowCount()));
        canceledRowsLabel.setText(String.valueOf(table.getRowCount()));

        setLocationRelativeTo(getParent());
        super.show();
    }

    private void initTable() {
        try {
            Vector colNames = new Vector();
            colNames.add(ui.getString("fina2.returns.bankCode"));
            colNames.add(ui.getString("fina2.returns.retDefCode"));
            colNames.add(ui.getString("fina2.period.fromDate"));
            colNames.add(ui.getString("fina2.period.toDate"));

            table.initTable(colNames, canceledRows);

        } catch (Exception e) {
            Main.generalErrorHandler(e);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        appendRowsLabel = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        canceledRowsLabel = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();

        setTitle(ui.getString("fina2.returns.canceledRows"));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel1.setLayout(new java.awt.GridLayout(3, 0));

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel2.setText(ui.getString("fina2.returns.appendRows"));
        jLabel2.setToolTipText("null");
        jLabel2.setFont(ui.getFont());
        jPanel4.add(jLabel2);

        appendRowsLabel.setFont(ui.getFont());
        jPanel4.add(appendRowsLabel);

        jPanel1.add(jPanel4);

        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel4.setText(ui.getString("fina2.returns.canceledRowsCounter"));
        jLabel4.setFont(ui.getFont());
        jPanel5.add(jLabel4);

        canceledRowsLabel.setFont(ui.getFont());
        jPanel5.add(canceledRowsLabel);

        jPanel1.add(jPanel5);

        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jLabel1.setText(ui.getString("fina2.returns.scheduleCanceledRows"));
        jLabel1.setFont(ui.getFont());
        jPanel6.add(jLabel1);

        jPanel1.add(jPanel6);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.BorderLayout());

        scrollPane.setPreferredSize(new java.awt.Dimension(350, 350));
        jPanel2.add(scrollPane, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        jButton1.setIcon(ui.getIcon("fina2.ok"));
        jButton1.setFont(ui.getFont());
        jButton1.setText(ui.getString("fina2.ok"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel3.add(jButton1);

        getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        pack();
    }//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JLabel appendRowsLabel;
    private javax.swing.JLabel canceledRowsLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

}