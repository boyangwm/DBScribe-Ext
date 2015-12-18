/*
 * LongMessageBox.java
 *
 * Created on December 15, 2001, 11:19 PM
 */

package fina2.ui;

/**
 *
 * @author  David Shalamberidze
 */
public class LongMessageBox extends javax.swing.JDialog {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;

    /** Creates new form LongMessageBox */
    public LongMessageBox(java.awt.Frame parent) {
        super(parent, true);
        initComponents();
        //longMessageText.setBackground(getBackground());
    }

    public void setMessage(String msg) {
        messageLabel.setText(msg);
    }

    public void setLongMessage(String msg) {
        longMessageText.setText(msg);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        iconLabel = new javax.swing.JLabel();
        messageLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        longMessageText = new javax.swing.JTextArea();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        okButton.setIcon(ui.getIcon("fina2.ok"));
        okButton.setFont(ui.getFont());
        okButton.setText(ui.getString("fina2.ok"));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        jPanel1.add(okButton);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel2.setBorder(new javax.swing.border.EmptyBorder(
                new java.awt.Insets(20, 20, 20, 20)));
        iconLabel.setFont(ui.getFont());
        jPanel2.add(iconLabel, java.awt.BorderLayout.WEST);

        messageLabel.setFont(ui.getFont());
        jPanel2.add(messageLabel, java.awt.BorderLayout.CENTER);

        jScrollPane1.setBorder(new javax.swing.border.EtchedBorder());
        jScrollPane1.setPreferredSize(new java.awt.Dimension(320, 150));
        longMessageText.setEditable(false);
        longMessageText.setColumns(30);
        longMessageText.setRows(20);
        longMessageText.setFont(ui.getFont());
        longMessageText.setBackground(new java.awt.Color(204, 204, 204));
        longMessageText.setBorder(null);
        jScrollPane1.setViewportView(longMessageText);

        jPanel2.add(jScrollPane1, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea longMessageText;
    // End of variables declaration//GEN-END:variables

}
