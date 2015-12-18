/*
 * GeneratorProgress.java
 *
 * Created on January 4, 2002, 7:14 PM
 */

package fina2.returns;

/**
 *
 * @author  David Shalamberidze
 */
public class DiagramProgress extends javax.swing.JDialog {

    private Thread t;

    /** Creates new form GeneratorProgress */
    public DiagramProgress(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public void show(Thread t, int cur, int total) {
        this.t = t;

        numLabel.setText(cur + " of " + total);
        progress.setMaximum(total);
        progress.setValue(0);
        setLocationRelativeTo(getParent());
        show();
    }

    public void setProgress(int p) {
        numLabel.setText(p + " of " + progress.getMaximum());
        progress.setValue(p);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        progress = new javax.swing.JProgressBar();
        jPanel2 = new javax.swing.JPanel();
        numLabel = new javax.swing.JLabel();

        setTitle("Generating");
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(new javax.swing.border.EmptyBorder(
                new java.awt.Insets(20, 30, 20, 30)));
        progress.setMaximum(10);
        jPanel1.add(progress, java.awt.BorderLayout.CENTER);

        numLabel.setText("                      ");
        jPanel2.add(numLabel);

        jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        t.start();
    }//GEN-LAST:event_formComponentShown

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JProgressBar progress;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel numLabel;
    // End of variables declaration//GEN-END:variables

}
