/*
 * SelectMathFunctionDialog.java
 *
 * Created on December 14, 2001, 5:12 PM
 */

package fina2.script;

import javax.swing.DefaultListModel;

/**
 *
 * @author  David Shalamberidze
 */
public class SelectMathFunctionDialog extends javax.swing.JDialog {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private String f = null;

    /** Creates new form SelectMathFunctionDialog */
    public SelectMathFunctionDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        DefaultListModel model = new DefaultListModel();
        model.addElement("sum");
        model.addElement("average");
        model.addElement("average+");
        model.addElement("min");
        model.addElement("max");
        model.addElement("count");
        model.addElement("pct");
        list.setModel(model);
    }

    public String getFunction() {
        return f;
    }

    public void show() {
        setLocationRelativeTo(getParent());
        super.show();
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
        list = new javax.swing.JList();

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

        scrollPane.setPreferredSize(new java.awt.Dimension(280, 140));
        scrollPane.setViewportView(list);

        getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        f = null;
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (list.getSelectedIndex() == -1) {
            f = null;
            dispose();
            return;
        }
        f = (String) list.getSelectedValue();
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        f = null;
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
    private javax.swing.JList list;
    // End of variables declaration//GEN-END:variables

}
