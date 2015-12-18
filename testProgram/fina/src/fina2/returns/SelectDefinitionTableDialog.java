/*
 * SelectDefinitionTableDialog.java
 *
 * Created on 11 ������ 2002 �., 17:54
 */

package fina2.returns;

import java.util.Vector;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.ListSelectionModel;

import fina2.Main;
import fina2.ui.table.EJBTable;
import fina2.ui.table.TableRow;

/**
 *
 * @author  David Shalamberidze
 */
public class SelectDefinitionTableDialog extends javax.swing.JDialog {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private EJBTable table;
    private TableRow tableRow;

    /** Creates new form SelectDefinitionTableDialog */
    public SelectDefinitionTableDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        ui.loadIcon("fina2.cancel", "cancel.gif");
        ui.loadIcon("fina2.help", "help.gif");
        ui.loadIcon("fina2.ok", "ok.gif");

        table = new EJBTable();

        initComponents();

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scrollPane.setViewportView(table);
    }

    public TableRow getTableRow() {
        return tableRow;
    }

    public void show() {
        initTable();

        setLocationRelativeTo(getParent());
        super.show();
    }

    private void initTable() {
        try {
            InitialContext jndi = fina2.Main.getJndiContext();
            Object ref = jndi.lookup("fina2/returns/ReturnSession");
            ReturnSessionHome home = (ReturnSessionHome) PortableRemoteObject
                    .narrow(ref, ReturnSessionHome.class);

            ReturnSession session = home.create();

            Vector colNames = new Vector();
            colNames.add(ui.getString("fina2.code"));
            colNames.add(ui.getString("fina2.returns.returnDefinition"));
            colNames.add(ui.getString("fina2.code"));
            colNames.add(ui.getString("fina2.report.table"));

            table.initTable(colNames, session.getAllDefinitionTables(main
                    .getLanguageHandle()));

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
        jPanel2 = new javax.swing.JPanel();
        helpButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();

        setTitle(ui.getString("fina2.returns.returnDefinitionTable"));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
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

        scrollPane.setPreferredSize(new java.awt.Dimension(440, 300));
        getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        tableRow = null;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        tableRow = table.getSelectedTableRow();
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        tableRow = null;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton helpButton;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton okButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

}
